package at.co.svc.aga.transformator.utils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/** Lowers migration control-flow carriers to AGATE LOOP/condition and response operations. */
public final class AgateWindowDsl {
    private AgateWindowDsl() {}
    public static List<Map<String,Object>> normalize(List<Map<String,Object>> input) {
        List<Map<String,Object>> result=new ArrayList<>();
        for(Map<String,Object> step:input) result.addAll(lower(step));
        return result;
    }
    private static List<Map<String,Object>> lower(Map<String,Object> original) {
        Map<String,Object> step=new LinkedHashMap<>(original);
        // Apply customer-specific Tosca buffer mappings to conditions as well as values.
        // Unmapped/runtime buffers (including generated agate_cf_* guards) stay unchanged.
        if (step.get("condition") instanceof String condition && !condition.isBlank()) {
            step.put("condition", ToscaValueTranslator.translateToscaValues(condition));
        }
        String op=text(step.get("op"));
        if(Set.of("IF","WHILE","DO_WHILE").contains(op)&&step.containsKey("predicate")) return control(step,op);
        if("WINDOW".equals(step.get("type")) && !Set.of("EXEC","BUFFER").contains(op)) {
            Map<String,Object> window=map(step.remove("window"));
            if(window.containsKey("titleGlob"))step.put("caption",window.get("titleGlob"));
            for(var entry:window.entrySet())if(!entry.getKey().equals("titleGlob"))step.put(entry.getKey(),entry.getValue());
            Map<String,Object> assertion=map(step.remove("assertion"));
            if(op.equals("ASSERT")||op.equals("WAIT")) {
                String kind=text(assertion.get("type"));
                boolean negative=Set.of("NOT_EXISTS","HIDDEN","DISABLED","PROPERTY_NOT_EQUALS").contains(kind);
                String action=switch(kind){case "NOT_EXISTS"->"EXISTS";case "HIDDEN"->"VISIBLE";case "DISABLED"->"ENABLED";case "PROPERTY_NOT_EQUALS","PROPERTY_EQUALS"->"PROPERTY_EQUALS";default->kind;};
                step.put("op","ASSERT");step.put("action",action);
                if(assertion.containsKey("expected"))step.put("expected",assertion.get("expected"));
                else step.put("expected",!negative);
                if(kind.startsWith("PROPERTY_"))step.put("negate",negative);
                if(assertion.containsKey("property"))step.put("property",assertion.get("property"));
                if(op.equals("WAIT"))step.put("wait",true);
            } else {step.put("op","EXEC");step.put("action",op);}
        }
        edgeRestoreTarget(step);
        if(step.get("steps") instanceof List<?>)step.put("steps",normalize(maps(step.get("steps"))));
        return List.of(step);
    }
    private static List<Map<String,Object>> control(Map<String,Object> step,String mode) {
        String id=text(step.get("id"));
        String prefix="agate_cf_"+UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
        List<Map<String,Object>> leaves=maps(map(step.get("predicate")).get("all"));
        if(leaves.isEmpty())throw new IllegalArgumentException("Control-flow predicate.all must not be empty");
        List<Map<String,Object>> queries=new ArrayList<>(),defaults=new ArrayList<>();List<String> terms=new ArrayList<>();
        for(int i=0;i<leaves.size();i++) {
            Map<String,Object> leaf=leaves.get(i);String check=text(leaf.get("op"));
            if(!Set.of("EXISTS","VISIBLE","ENABLED").contains(check))throw new IllegalArgumentException("Unsupported control predicate: "+check);
            String name=prefix+"_p"+i,response=prefix+"_response"+i;
            Map<String,Object> find=new LinkedHashMap<>(leaf);find.remove("expected");find.remove("condition");find.remove("negate");find.put("id",name+"_find");find.put("op","EXEC");find.put("action","FIND");find.put("response",response);
            Map<String,Object> window=map(find.remove("window"));if(window.containsKey("titleGlob"))find.put("caption",window.get("titleGlob"));
            for(var entry:window.entrySet())if(!entry.getKey().equals("titleGlob"))find.put(entry.getKey(),entry.getValue());
            edgeRestoreTarget(find);
            Map<String,Object> buffer=base(name+"_buffer",text(leaf.get("type")),"BUFFER");buffer.put("action",check);buffer.put("response",response);buffer.put("name",name);
            queries.add(find);queries.add(buffer);defaults.add(bufferValue(name+"_init",name,"false",null));
            Object expected=leaf.getOrDefault("expected",true);
            String expectedText=Objects.toString(expected);
            if(!expectedText.equalsIgnoreCase("true")&&!expectedText.equalsIgnoreCase("false")&&!expectedText.startsWith("{"))throw new IllegalArgumentException("Invalid predicate expectation: "+expected);
            String term="'{B["+name+"]}' == '"+expectedText.toLowerCase(Locale.ROOT)+"'";
            // Preserve dynamic placeholder spelling.
            if(expectedText.startsWith("{"))term="'{B["+name+"]}' == '"+expectedText+"'";
            if(Boolean.TRUE.equals(leaf.get("negate")))term="NOT ("+term+")";
            terms.add("("+term+")");
        }
        String parent=text(step.get("condition")),guard="";
        List<Map<String,Object>> out=new ArrayList<>();
        if(!parent.isBlank()) {
            String guardName=prefix+"_parent";
            out.add(bufferValue(guardName+"_init",guardName,"false",null));
            out.add(bufferValue(guardName+"_set",guardName,"true",parent));
            guard="'{B["+guardName+"]}' == 'true'";
        }
        String combined=String.join(" AND ",terms);
        List<Map<String,Object>> body=normalize(desktopCloses(maps(step.get("steps")),leaves));
        if(mode.equals("IF")) {
            out.addAll(defaults);
            for(Map<String,Object> query:queries)gate(query,guard);
            out.addAll(queries);
            String decision=prefix+"_decision";
            out.add(bufferValue(decision+"_init",decision,"false",null));
            out.add(bufferValue(decision+"_set",decision,"true",and(guard,combined)));
            String branch="'{B["+decision+"]}' == 'true'";
            for(Map<String,Object> child:body)gate(child,and(guard,branch));out.addAll(body);
            List<Map<String,Object>> otherwise=normalize(desktopCloses(maps(step.get("elseSteps")),leaves));
            for(Map<String,Object> child:otherwise)gate(child,and(guard,"NOT ("+branch+")"));out.addAll(otherwise);
        } else {
            out.addAll(defaults);
            if(mode.equals("WHILE")){for(Map<String,Object> query:queries)gate(query,guard);out.addAll(queries);}
            for(Map<String,Object> child:body)gate(child,guard);
            for(Map<String,Object> query:queries)gate(query,guard);
            body.addAll(queries);
            Map<String,Object> loop=base(id,"LOOP",null);loop.put("mode",mode);
            loop.put("maxIterations",step.getOrDefault("maxIterations",10));
            loop.put("timeoutMs",step.getOrDefault("timeoutMs",60000));
            loop.put("condition",and(guard,combined));loop.put("steps",body);out.add(loop);
        }
        return out;
    }
    /** A native window predicate provides the explicit scope for legacy CloseBrowser. */
    private static List<Map<String,Object>> desktopCloses(List<Map<String,Object>> steps,
            List<Map<String,Object>> predicates) {
        Set<String> captions=new LinkedHashSet<>();
        for(Map<String,Object> predicate:predicates) {
            if(!"WINDOW".equals(predicate.get("type")) || !"EXISTS".equals(predicate.get("op"))
                    || predicate.containsKey("target") || Boolean.TRUE.equals(predicate.get("negate"))
                    || !"true".equalsIgnoreCase(text(predicate.getOrDefault("expected",true))))continue;
            String caption=text(map(predicate.get("window")).get("titleGlob"));
            if(caption.isBlank())caption=text(predicate.get("caption"));
            if(!caption.isBlank())captions.add(caption);
        }
        List<Map<String,Object>> result=new ArrayList<>();
        for(Map<String,Object> original:steps) {
            Map<String,Object> child=new LinkedHashMap<>(original);
            // NativeWindowIfContract emits CloseBrowser as op=EXEC,
            // action=CLOSE_BROWSER and does not set page.
            // Only rewrite when the native WINDOW predicate supplies
            // one unambiguous window caption.
            boolean legacyClose="GUI".equals(child.get("type"))
                    && "EXEC".equals(child.get("op"))
                    && "CLOSE_BROWSER".equals(child.get("action"));
            if(legacyClose && !captions.isEmpty()) {
                if(captions.size()!=1)throw new IllegalArgumentException(
                        "CloseBrowser has multiple native window captions; explicit mapping required");
                child.put("type","WINDOW");child.put("op","EXEC");child.put("action","CLOSE");
                child.remove("page");child.put("caption",captions.iterator().next());
            }
            result.add(child);
        }
        return result;
    }
    /** Explicit semantic mapping for the known Edge restore-session controls only. */
    private static void edgeRestoreTarget(Map<String,Object> step) {
        if(!"WINDOW".equals(step.get("type")) || !step.containsKey("target"))return;
        Map<String,Object> target=map(step.get("target"));
        if(!target.containsKey("locatorRef"))return;
        Map<String,Object> source=map(step.get("source"));
        String caption=text(step.get("caption")).replace("\u200B", "").replace("\uFEFF", "");
        if(!caption.toLowerCase(Locale.ROOT).contains("microsoft edge"))return;
        String engine=text(map(source.get("properties")).get("Engine"));
        if(!Set.of("AnyUI","UIA").contains(engine))return;
        String role=switch(text(source.get("businessName"))) {
            case "Seiten wiederherstellen", "Restore pages" -> "PANEL";
            case "Wiederherstellen", "Restore" -> "RESTORE";
            case "Schließen", "Close" -> "CLOSE";
            default -> "";
        };
        if(role.isEmpty())return;
        step.put("caption",caption);
        step.put("target",Map.of("edgeRestore",role));
        // Preserve Tosca source metadata for traceability; runtime uses explicit semantic mapping.
    }
    private static void gate(Map<String,Object> step,String guard) {
        if(guard.isBlank())return;
        if("LOOP".equals(step.get("type"))) {
            step.put("condition",and(guard,text(step.get("condition"))));
            for(Map<String,Object> child:maps(step.get("steps")))gate(child,guard);
        } else step.put("condition",and(guard,text(step.get("condition"))));
    }
    private static Map<String,Object> bufferValue(String id,String name,String value,String condition) {
        Map<String,Object> out=base(id,"BUFFER","EXEC");out.put("name",name);out.put("value",value);if(condition!=null&&!condition.isBlank())out.put("condition",condition);return out;
    }
    private static Map<String,Object> base(String id,String type,String op) {
        Map<String,Object> out=new LinkedHashMap<>();out.put("id",id);out.put("type",type);if(op!=null)out.put("op",op);return out;
    }
    private static String and(String a,String b){if(a.isBlank())return b;if(b.isBlank())return a;return "("+a+") AND ("+b+")";}
    private static String text(Object value){return Objects.toString(value,"");}
    @SuppressWarnings("unchecked") private static Map<String,Object> map(Object value){return value instanceof Map<?,?>?(Map<String,Object>)value:Map.of();}
    private static List<Map<String,Object>> maps(Object value){List<Map<String,Object>> out=new ArrayList<>();if(value instanceof List<?> list)for(Object item:list)out.add(map(item));return out;}
}
