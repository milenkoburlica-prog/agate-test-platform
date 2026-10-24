package at.co.svc.agate.ai;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Path("/api/v1/chat")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChatResource {
    @Inject DiagnosticOrchestrator orchestrator;
    @Inject ToolRegistry registry;
    @Inject RequestRouter router;
    private final AgateExecutionParser executionParser = new AgateExecutionParser();
    private final SqlResponseCorrelationTool sqlCorrelation = new SqlResponseCorrelationTool();
    private final EvidenceExplorer evidenceExplorer = new EvidenceExplorer();
    @ConfigProperty(name="agate.agent.max-chars", defaultValue="5242880") int maxChars;
    private static final int MAX_SESSIONS = 100;
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();
    private record SessionContext(String lastAnalysis, String lastStatus, String lastEngine) {}
    public record ChatRequest(String sessionId, String message, String caseText) {}
    public record ChatReply(String sessionId, String intent, String answer, String verificationStatus, boolean verifiedByAgate, EvidenceExplorer.View diagnostic) {
        public ChatReply(String sid,String intent,String answer,String status,boolean verified){this(sid,intent,answer,status,verified,null);}
    }

    @GET @Path("/capabilities") public Object capabilities() { return registry.capabilities(); }

    @POST public Response chat(ChatRequest req) {
        if (req == null || req.message() == null || req.message().isBlank())
            return Response.status(400).entity(Map.of("error", "Eine Nachricht ist erforderlich.")).build();
        if (req.message().length() > 5000 || (req.caseText() != null && req.caseText().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > maxChars))
            return Response.status(413).entity(Map.of("error", "Anfrage oder Protokoll ist zu groß.")).build();
        String sid = req.sessionId() != null && req.sessionId().matches("[a-zA-Z0-9-]{1,80}") ? req.sessionId() : UUID.randomUUID().toString();
        String message = req.message().trim();
        boolean freshEvidence = req.caseText() != null && !req.caseText().isBlank();
        SessionContext previous = sessions.get(sid);
        RequestRouter.Route route = router.classify(message, previous != null, freshEvidence);
        switch (route.intent()) {
            case CAPABILITIES -> {
                return ok(sid,"CAPABILITIES","Ich analysiere SOAP-/XPath-Fehler mit Groq und lokaler Java-Prüfung. SQL, CMD, OpenShift, REST, GUI und BUFFER kann ich aus hochgeladenen Logs offline untersuchen. Live-Verbindungen und Ausführung sind nicht verfügbar.","NICHT_ERFORDERLICH");
            }
            case CREATE_TEST -> {
                return ok(sid,"UNSUPPORTED","Die Erstellung neuer AGATE-Tests unterstütze ich derzeit nicht. Ich kann vorhandene fehlgeschlagene SOAP-/XPath-Tests analysieren und Korrekturen lokal prüfen.","NICHT_UNTERSTÜTZT");
            }
            case FORBIDDEN_EXECUTION -> {
                return ok(sid,"REQUIRES_APPROVAL","Ich führe keine Tests, Shell-Befehle, Datenbankänderungen oder Clusteraktionen aus. Für Diagnosezwecke kannst du vorhandene Protokolle bereitstellen.","NICHT_AUSGEFÜHRT");
            }
            case ENGINE_DIAGNOSE -> { return analyzeEngine(sid, route.engine(), req.caseText()); }
            case UNSUPPORTED_ENGINE -> {
                // A new subject must not reuse the previous SOAP case.
                return ok(sid,"UNSUPPORTED_ENGINE","Für " + route.engine() + " kann ich bereits hochgeladene Logdateien offline auf typische Fehlermuster prüfen. Bitte füge das passende Protokoll bei. Direkten Systemzugriff habe ich nicht.","NICHT_UNTERSTÜTZT");
            }
            case EXPLAIN_PREVIOUS -> {
                // No Groq call and no re-run of SOAP diagnosis. Ground the answer in previously verified output.
                return ok(sid,"EXPLAIN_PREVIOUS", explanation(previous),"NICHT_ERFORDERLICH");
            }
            case NEEDS_INPUT -> {
                return ok(sid,"NEEDS_INPUT","Bitte lade ein passendes AGATE-Ausführungsprotokoll (.log/.txt) hoch. Für SOAP benötige ich den fehlgeschlagenen Schritt und die Antwort; andere Engines kann ich anhand ihrer Logmeldungen offline untersuchen.","DATEN_FEHLEN");
            }
            case DIAGNOSE -> { return dispatch(sid, message, req.caseText()); }
        }
        return ok(sid,"UNKNOWN","Die Anfrage konnte nicht zugeordnet werden.","NICHT_PRÜFBAR");
    }

    private Response dispatch(String sid, String message, String log) {
        Optional<AgateExecutionParser.Failure> agateFailure=executionParser.findFailure(log);
        if(agateFailure.isPresent()) {
            var f=agateFailure.get();
            if("SQL".equals(f.engine())) {
                ToolFinding finding=sqlCorrelation.analyze(f);
                if(finding!=null)return emitFinding(sid,finding,log);
            }
            if("SOAP".equals(f.engine()))return diagnose(sid,message,log);
            if(registry.get(f.engine()).isPresent()) return analyzeEngine(sid,f.engine(),log);
            return ok(sid,"UNSUPPORTED_FORMAT","Fehlgeschlagener AGATE-Schritt erkannt ("+f.engine()+"), aber noch kein passendes Werkzeug verfügbar.","NICHT_PRÜFBAR");
        }
        String engine = detectEngine(log);
        if ("SOAP".equals(engine)) return diagnose(sid,message,log);
        if ("UNKNOWN".equals(engine)) return ok(sid,"UNSUPPORTED_FORMAT",
            "Das Protokoll enthält kein eindeutig unterstütztes Engine-Fehlermuster. Bitte Engine und Fehlerabschnitt angeben.","NICHT_PRÜFBAR");
        return analyzeEngine(sid,engine,log);
    }
    private static String detectEngine(String log) {
        String s=log.toLowerCase(Locale.ROOT);
        if (s.contains("soap step finished") && s.contains("soap")) return "SOAP";
        if (s.contains("crashloopbackoff") || s.contains("oomkilled") || s.contains("imagepullbackoff") || s.contains("readiness probe failed")) return "OPENSHIFT";
        if (s.contains("ora-") || s.contains("sqlstate") || s.contains("psqlexception") || s.contains("sqlexception")) return "SQL";
        if (s.contains("playwright") || s.contains("strict mode violation") || s.contains("locator(")) return "GUI";
        if (s.contains("missing_buffer") || s.contains("unresolved buffer") || s.contains("agate-v420")) return "BUFFER";
        if (s.contains("exit code") || s.contains("exitcode") || s.contains("command not found") || s.contains("stderr:")) return "CMD";
        if (s.matches("(?s).*\\b(http[/ ]?1\\.[01] +[45][0-9]{2}|http_500|status *[:=] *[45][0-9]{2}|sockettimeoutexception).*")) return "REST";
        return "UNKNOWN";
    }
    private Response analyzeEngine(String sid,String engine,String log) {
        if(log==null || log.isBlank()) return ok(sid,"NEEDS_INPUT","Bitte füge ein Protokoll des betroffenen " + engine + "-Engines hinzu.","DATEN_FEHLEN");
        Optional<AgentTool> tool=registry.get(engine);
        if(tool.isEmpty()) return ok(sid,"UNSUPPORTED_ENGINE","Für " + engine + " ist noch kein Diagnosewerkzeug verfügbar.","NICHT_UNTERSTÜTZT");
        Optional<AgateExecutionParser.Failure> agateFailure=executionParser.findFailure(log);
        if(agateFailure.isPresent() && "SQL".equals(agateFailure.get().engine())) {
            ToolFinding correlated=sqlCorrelation.analyze(agateFailure.get());
            if(correlated!=null)return emitFinding(sid,correlated,log);
        }
        ToolFinding r=tool.get().analyze(log);
        return emitFinding(sid,r,log);
    }
    private Response emitFinding(String sid, ToolFinding r, String log) {
        StringBuilder b=new StringBuilder("ENGINE: ").append(r.engine()).append("\nBEFUND: ").append(r.summary());
        if(!r.evidence().isEmpty()){
            b.append("\n\nBELEGE AUS DEM PROTOKOLL:");
            for(String item:r.evidence())b.append("\n- ").append(item);
        }
        if(!r.recommendations().isEmpty()){
            b.append("\n\nEMPFOHLENE PRÜFSCHRITTE:");
            for(String item:r.recommendations())b.append("\n- ").append(item);
        }
        b.append("\n\nHINWEIS: Nur Offline-Loganalyse. Keine externen Systeme abgefragt, keine Befehle ausgeführt, keine Korrektur verifiziert.");
        if(sessions.size()>=MAX_SESSIONS)sessions.clear();
        sessions.put(sid,new SessionContext(b.toString(),r.status(),r.engine()));
        return Response.ok(new ChatReply(sid,"ENGINE_DIAGNOSE",b.toString(),r.status(),false,evidenceExplorer.forFinding(log,r))).build();
    }
    private Response diagnose(String sid, String message, String log) {
        if (!log.contains("SOAP STEP FINISHED") || !log.contains("SOAP"))
            return ok(sid,"UNSUPPORTED_FORMAT","Die verifizierte Diagnose unterstützt derzeit nur AGATE-SOAP-Protokolle mit fehlgeschlagenem ASSERT. Die übermittelte Datei wurde nicht als solcher Fall erkannt.","NICHT_PRÜFBAR");
        try {
            DiagnosticResult r = orchestrator.analyze(new DiagnosticRequest(log, message));
            if (sessions.size() >= MAX_SESSIONS) sessions.clear();
            sessions.put(sid, new SessionContext(r.analysis(), r.verificationStatus(), "SOAP"));
            return Response.ok(new ChatReply(sid,"DIAGNOSE_FAILURE",r.analysis(),r.verificationStatus(),r.verified(),evidenceExplorer.forSoap(log,r))).build();
        } catch (GroqClient.GroqException ex) {
            return Response.status(ex.httpStatus).entity(Map.of("error",ex.getMessage())).build();
        } catch (IllegalStateException ex) {
            return Response.status(503).entity(Map.of("error",ex.getMessage())).build();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Response.status(503).entity(Map.of("error","Die Anfrage wurde unterbrochen.")).build();
        } catch (Exception ex) {
            return Response.status(502).entity(Map.of("error","Diagnose fehlgeschlagen. Server-Protokoll prüfen.")).build();
        }
    }

    static String explanation(SessionContext previous) {
        if (previous == null) return "Es liegt noch keine vorherige Diagnose vor.";
        String text = previous.lastAnalysis();
        // Only make these claims if they are present in the verified case evidence.
        boolean mainHit = text.contains("treffer=1") && text.contains("HAUPT_XPATH:");
        boolean constraintMissing = text.contains("treffer=0") && text.contains("CONSTRAINT_XPATH:");
        boolean verified = "XPATH_LOKAL_BESTÄTIGT".equals(previous.lastStatus());
        if (mainHit && constraintMissing && verified)
            return "Der Haupt-XPath wurde nicht geändert, weil er bereits einen XML-Knoten gefunden hat (1 Treffer). " +
                "Der ursprüngliche Constraint-XPath hatte dagegen 0 Treffer. " +
                "Die lokale Java-Prüfung bestätigte für den korrigierten XPath den erwarteten Wert. " +
                "Wichtig: Ein AGATE-Testlauf wurde nicht durchgeführt; die AGATE-Constraint-Semantik bleibt ungeprüft.";
        return "Die vorherige Diagnose hat den Status " + previous.lastStatus() +
            ". Eine zusätzliche Ursache lässt sich aus den gespeicherten Befunden nicht sicher ableiten. " +
            "Die Details stehen im vorherigen Diagnosebericht; ein AGATE-Testlauf wurde nicht bestätigt.";
    }

    private Response ok(String sid, String intent, String answer, String state) {
        return Response.ok(new ChatReply(sid,intent,answer,state,false)).build();
    }
}
