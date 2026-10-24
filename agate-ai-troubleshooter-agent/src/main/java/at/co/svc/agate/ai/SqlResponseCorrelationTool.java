package at.co.svc.agate.ai;

import java.util.*;

/** Correlates a failed AGATE SQL BUFFER with its preceding SQL EXEC producer. */
public final class SqlResponseCorrelationTool {
    public ToolFinding analyze(AgateExecutionParser.Failure failure) {
        String response=failure.response();
        if (!"SQL".equals(failure.engine()) || failure.reason()==null ||
                !failure.reason().toLowerCase(Locale.ROOT).contains("response was not found")) return null;
        if (response==null || response.isBlank()) return null;
        List<String> evidence=new ArrayList<>(),checks=new ArrayList<>();
        evidence.add("SQL BUFFER benötigt Response: " + response);
        if(failure.reusable()!=null)evidence.add("Reusable: " + failure.reusable());
        List<AgateExecutionParser.Step> producers=new ArrayList<>();
        for(AgateExecutionParser.Step s:failure.steps()){
            if(s.startLine()>=failure.failedStep().startLine()) break;
            if("SQL".equals(s.engine()) && "EXEC".equals(s.op()) && response.equals(s.response()))producers.add(s);
        }
        if(producers.isEmpty()) {
            checks.add("Im protokollierten Ablauf vor dem Fehler keinen SQL EXEC mit Response '" + response + "' gefunden; das Reusable-YAML auf Namen und Kontrollfluss prüfen.");
            return new ToolFinding("SQL","SQL BUFFER liest eine nicht vorhandene Response. Ein passender SQL EXEC ist im Protokoll vorher nicht erkennbar.",evidence,checks,"URSACHE_EINGEGRENZT_NICHT_VERIFIZIERT");
        }
        AgateExecutionParser.Step producer=producers.get(producers.size()-1);
        evidence.add("SQL EXEC, Zeile " + producer.startLine() + ": response=" + response + "; Schrittstatus=" + producer.status() + (producer.skipped() ? "; tatsächlich SKIPPED (keine Response erzeugt)" : ""));
        if(producer.condition()!=null)evidence.add("SQL EXEC Bedingung: " + producer.condition());
        if(producer.skipped()) {
            evidence.add("SQL EXEC übersprungen: " + producer.skipReason());
            String condition=producer.condition();
            if(condition!=null && !condition.isBlank()) {
                checks.add("Prüfen, ob die folgende SQL BUFFER-Aktion dieselbe Ausführungsbedingung benötigt: condition: " + condition);
                checks.add("Bei nicht erfüllter Bedingung müssen auch alle weiteren Verbraucher dieser Response übersprungen oder anderweitig abgesichert werden.");
            } else checks.add("Den Kontrollfluss des übersprungenen SQL EXEC sowie des SQL BUFFER gemeinsam absichern.");
            checks.add("Mit Fällen ohne Datensatz (COUNT=0) und mit vorhandenem Datensatz (COUNT=1) erneut testen; bei mehreren Datensätzen die Fachlogik prüfen.");
            return new ToolFinding("SQL","SQL EXEC für '"+response+"' wurde wegen einer nicht erfüllten Bedingung korrekt übersprungen. SQL BUFFER wurde dennoch ausgeführt und konnte die Response nicht finden.",evidence,checks,"KORREKTUR_VORGESCHLAGEN_NICHT_VERIFIZIERT");
        }
        checks.add("SQL EXEC wurde protokolliert, aber die Response fehlt am SQL BUFFER: Response-Registry, Scope und Reusable-Kontext prüfen.");
        return new ToolFinding("SQL","Die benötigte SQL-Response ist am BUFFER nicht verfügbar; ein SQL EXEC mit diesem Namen ist protokolliert.",evidence,checks,"URSACHE_EINGEGRENZT_NICHT_VERIFIZIERT");
    }
}
