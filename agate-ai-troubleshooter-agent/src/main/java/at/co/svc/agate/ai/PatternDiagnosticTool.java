package at.co.svc.agate.ai;
import java.util.*;
import java.util.regex.*;
/** Deterministic rule set: findings are grounded in submitted log lines, not generated assumptions. */
public final class PatternDiagnosticTool implements AgentTool {
    public record Rule(String regex, String summary, String recommendation) {}
    private final String id, description;
    private final List<Rule> rules;
    public PatternDiagnosticTool(String id, String description, List<Rule> rules) {
        this.id=id; this.description=description; this.rules=List.copyOf(rules);
    }
    public String id(){return id;}
    public String description(){return description;}
    public ToolFinding analyze(String log){
        List<String> evidence=new ArrayList<>(), tips=new ArrayList<>(), findings=new ArrayList<>();
        String[] lines=log.split("\\R");
        for(Rule rule:rules){
            Pattern p=Pattern.compile(rule.regex(), Pattern.CASE_INSENSITIVE);
            for(String line:lines){
                if(p.matcher(line).find()){
                    findings.add(rule.summary());
                    evidence.add(line.strip().length()>350?line.strip().substring(0,350)+" …":line.strip());
                    tips.add(rule.recommendation()); break;
                }
            }
        }
        if(findings.isEmpty()) return new ToolFinding(id,"Im eingereichten Protokoll wurde kein unterstütztes Fehlermuster erkannt.",List.of(),List.of("Vollständigen Fehlerabschnitt und relevante Konfiguration prüfen."),"NICHT_PRÜFBAR");
        return new ToolFinding(id,String.join(" ",new LinkedHashSet<>(findings)),List.copyOf(new LinkedHashSet<>(evidence)),List.copyOf(new LinkedHashSet<>(tips)),"MUSTER_ERKANNT_NICHT_VERIFIZIERT");
    }
}
