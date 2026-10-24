package at.co.svc.agate.ai;

import java.util.*;
import java.util.regex.*;

/** Parses the failed SOAP ASSERT, not unrelated earlier success steps or XML contents. */
public final class AssertionMismatchAnalyzer {
    private static final Pattern FAILED = Pattern.compile("(?m)^\\s*>>> SOAP ASSERT FAILED: (\\w+) \\| Expected: \\[([^\r\n]*?)\\], Actual: \\[([^\r\n]*?)\\]\\s*$");
    private static final Pattern PATH = Pattern.compile("(?m)^\\s*>>> DSL\\s+path: (.+)$");
    private static final Pattern OFFSET = Pattern.compile("(?m)^\\s*insert_fotoinfo\\.SPERRE_AB_OFFSET\\s*=\\s*(.*)$");
    private static final Pattern AKTEUR = Pattern.compile("(?m)^\\s*insert_fotoinfo\\.SPERR_AKTEUR_KZ\\s*=\\s*(.*)$");
    private static final Pattern TYP = Pattern.compile("(?m)^\\s*insert_fotoinfo\\.TYP\\s*=\\s*(.*)$");
    public record Mismatch(String action, String path, String expected, String actual,
                           int startLine, int endLine, List<String> setupEvidence) {}
    public Optional<Mismatch> inspect(String log) {
        if(log==null || !log.contains("SOAP ASSERT FAILED")) return Optional.empty();
        Matcher m=FAILED.matcher(log);
        if(!m.find()) return Optional.empty();
        // Only EQUALS mismatch supported. Other actions remain with existing diagnostics.
        if(!"EQUALS".equals(m.group(1)) || Objects.equals(m.group(2),m.group(3)))return Optional.empty();
        String preceding=log.substring(0,m.start());
        int dsl=preceding.lastIndexOf(">>> DSL      - type: SOAP");
        if(dsl<0)return Optional.empty();
        String block=preceding.substring(dsl);
        if(!block.contains("op: ASSERT") || !block.contains("source: BODY"))return Optional.empty();
        Matcher path=PATH.matcher(block);
        if(!path.find())return Optional.empty();
        List<String> setup=new ArrayList<>();
        add(setup,log,OFFSET,"SPERRE_AB_OFFSET");
        add(setup,log,AKTEUR,"SPERR_AKTEUR_KZ");
        add(setup,log,TYP,"TYP");
        if(log.contains("reusable.fotoinfo_db_insert_foto_info")) setup.add("Vorbereitung: reusable.fotoinfo_db_insert_foto_info");
        int first=1+(int)preceding.chars().filter(c->c=='\n').count();
        return Optional.of(new Mismatch(m.group(1),path.group(1).strip(),m.group(2),m.group(3),first,first,List.copyOf(setup)));
    }
    private static void add(List<String> out,String log,Pattern pattern,String name){
        Matcher m=pattern.matcher(log);if(m.find())out.add(name+"="+m.group(1).strip());
    }
}
