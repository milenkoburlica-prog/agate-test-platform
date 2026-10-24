package at.co.svc.agate.ai;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
@ApplicationScoped
public class ToolRegistry {
    @Inject SoapEvidence soap;
    public record Capability(String id, String name, boolean available, String description) {}
    private static PatternDiagnosticTool.Rule rule(String pattern,String description,String tip){
        return new PatternDiagnosticTool.Rule(pattern, description, tip);
    }
    private final Map<String,AgentTool> tools = createTools();
    private static Map<String,AgentTool> createTools(){
        Map<String,AgentTool> m=new LinkedHashMap<>();
        m.put("SQL",new PatternDiagnosticTool("SQL","SQL / JDBC / Oracle / PostgreSQL (Loganalyse)",List.of(
            rule("ORA-00942","Oracle meldet eine fehlende oder nicht zugängliche Tabelle/View.","Tabellennamen, Schema und SELECT-Rechte prüfen."),
            rule("ORA-01017","Oracle meldet ungültige Anmeldedaten.","Credentials und verwendeten Datenbankdienst prüfen; keine Passwörter in Logs übertragen."),
            rule("ORA-00001","Oracle meldet eine Unique-Constraint-Verletzung.","Schlüsselwerte und Testdatenvorbereitung prüfen."),
            rule("ORA-12154|ORA-12514","Oracle meldet ein Verbindungs-/Serviceauflösungsproblem.","TNS-/Service-Konfiguration prüfen."),
            rule("SQLSTATE.?23505|duplicate key value","PostgreSQL meldet einen doppelten Schlüssel.","Constraint und Testdaten auf Duplikate prüfen."),
            rule("SQLSTATE.?42P01|relation .+ does not exist","PostgreSQL meldet eine fehlende Relation.","Schema, Suchpfad und Migrationen prüfen."),
            rule("SQLSTATE.?08006|connection refused|connection timed out","Die Datenbankverbindung scheint fehlzuschlagen.","Host, Port, Erreichbarkeit und Pool-Konfiguration untersuchen."))));
        m.put("CMD",new PatternDiagnosticTool("CMD","CMD / PowerShell / Exit-Codes (Loganalyse)",List.of(
            rule("exit.?code\\s*[:=]\\s*[1-9][0-9]*|process exited with code [1-9]","Der Prozess endete mit einem Fehlercode.","Exit-Code und stderr des betroffenen Befehls prüfen."),
            rule("is not recognized as an internal|command not found|not recognized as the name of a cmdlet","Ein aufgerufenes Kommando wurde nicht gefunden.","PATH, ausführbare Datei und Shell-Typ prüfen."),
            rule("access is denied|permission denied|zugriff verweigert","Berechtigungsfehler beim Kommando.","Benutzerkontext, Dateirechte und Zielpfad prüfen."),
            rule("file not found|no such file|das system kann den angegebenen pfad nicht finden","Eine Datei oder ein Pfad fehlt.","Arbeitsverzeichnis, Pfadexpansion und Escape-Sequenzen prüfen."))));
        m.put("OPENSHIFT",new PatternDiagnosticTool("OPENSHIFT","OpenShift / Kubernetes Events und Pod-Logs (Offline-Analyse)",List.of(
            rule("CrashLoopBackOff|Back-off restarting failed container","Container wird wiederholt neu gestartet.","Vorherige Containerlogs, Termination Reason und Startup-Probes prüfen."),
            rule("OOMKilled|exit code 137","Mögliche Speichergrenze überschritten oder SIGKILL (Exit 137).","Memory-Limits, letzten Termination Reason und Verbrauch prüfen."),
            rule("ImagePullBackOff|ErrImagePull","Container-Image konnte nicht geladen werden.","Image-Tag, Registry-Zugriff und ImagePullSecrets prüfen."),
            rule("Readiness probe failed|Liveness probe failed|Startup probe failed","Eine Kubernetes-Probe ist fehlgeschlagen.","Probe-Pfad, Port, Initial-Delay und Anwendungsstart prüfen."),
            rule("must be less than or equal to|exceeded quota|Forbidden.*quota","Quota-/Limit-Vorgaben blockieren Ressourcen.","Requests/Limits und Namespace-Quota vergleichen."))));
        m.put("REST",new PatternDiagnosticTool("REST","REST / HTTP / JSON (Loganalyse)",List.of(
            rule("HTTP[/ ].*500|status.?[:=]\\s*500|HTTP_500","HTTP 500: Serverfehler gemeldet.","Korrelations-ID, Server-Stacktrace und abhängige Dienste prüfen."),
            rule("HTTP[/ ].*503|status.?[:=]\\s*503|Service Unavailable","HTTP 503: Dienst derzeit nicht verfügbar.","Service-Endpunkte, Deployment/Pod-Zustand und Proxy prüfen."),
            rule("HTTP[/ ].*401|status.?[:=]\\s*401|Unauthorized","HTTP 401: Authentifizierung fehlgeschlagen.","Token-Gültigkeit und Authentifizierungsmechanismus prüfen."),
            rule("HTTP[/ ].*404|status.?[:=]\\s*404|Not Found","HTTP 404: Ressource nicht gefunden.","URL, Pfadparameter und API-Version kontrollieren."),
            rule("SocketTimeoutException|Read timed out|connect timed out","Timeout bei REST-Kommunikation.","Latenz, Client-Timeout und Upstream-Verfügbarkeit prüfen."))));
        m.put("GUI",new PatternDiagnosticTool("GUI","GUI / Playwright / Browser (Loganalyse)",List.of(
            rule("TimeoutError.*locator|locator.*timeout|waiting for locator","GUI-Locator hat innerhalb des Timeouts kein Element gefunden.","Selektor, Frame/Window-Kontext und Ladezustand prüfen."),
            rule("strict mode violation","Playwright-Selektor ist nicht eindeutig.","Selektor präzisieren oder erwartete Anzahl kontrollieren."),
            rule("Target page, context or browser has been closed","Browser- oder Page-Context wurde vorzeitig geschlossen.","BrowserContext-Lifecycle und parallele Tests prüfen."),
            rule("Element is not visible|not visible.*element","Das UI-Element war nicht sichtbar.","Rendering, Overlay und Sichtbarkeitsbedingungen prüfen."))));
        m.put("BUFFER",new PatternDiagnosticTool("BUFFER","AGATE BUFFER / Variablen (Loganalyse)",List.of(
            rule("MISSING_BUFFER|buffer .*not found|unresolved buffer","Ein referenzierter BUFFER-Wert fehlt.","Erzeugenden Schritt, Variablennamen und Ausführungspfad prüfen."),
            rule("AGATE-V420|variable defined but never referenced","Eine möglicherweise ungenutzte Variable wurde gemeldet.","Verwendung in Templates und CSV-Testdaten berücksichtigen."))));
        return Collections.unmodifiableMap(m);
    }
    public Optional<AgentTool> get(String id){return Optional.ofNullable(tools.get(id));}
    public List<Capability> capabilities(){
        List<Capability> out=new ArrayList<>();
        out.add(new Capability("SOAP_XPATH","SOAP / XPath",true,"Groq + lokale Java-XPath-Prüfung"));
        for(AgentTool t:tools.values())out.add(new Capability(t.id(),t.description(),true,"Offline-Loganalyse; keine Verbindung oder Ausführung"));
        out.add(new Capability("YAML","AGATE YAML DSL",false,"Validierung noch nicht angebunden"));
        out.add(new Capability("REUSABLE","Reusable Dependencies",false,"Noch nicht angebunden"));
        return out;
    }
    public String inspectSoap(String log){return soap.inspect(log);}
}
