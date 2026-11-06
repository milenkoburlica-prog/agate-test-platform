package at.co.svc.agate.engine.gui;

import java.util.*;
import java.util.regex.*;

/** Normalizes Studio documents once for both execution and server validation. */
public final class GuiDocumentAdapter {
    private static final Pattern LOCAL = Pattern.compile("\\{L\\[([^]]+)]}");
    private GuiDocumentAdapter() {}
    public static Map<String,Object> normalize(Map<String,Object> input) {
        Map<String,Object> root = new LinkedHashMap<>(input);
        if (!root.containsKey("testCases") && root.get("steps") instanceof List) {
            root = new LinkedHashMap<>(Map.of("testCases", List.of(root)));
        }
        if (root.get("testCases") instanceof List<?> cases) {
            List<Object> normalized = new ArrayList<>();
            for (Object entry : cases) {
                if (!(entry instanceof Map)) { normalized.add(entry); continue; }
                Map<String,Object> tc = new LinkedHashMap<>(GuiStepConfig.map(entry));
                if (!tc.containsKey("id") && tc.containsKey("name")) tc.put("id",tc.get("name"));
                if (tc.get("steps") instanceof List<?> steps) {
                    tc.put("steps", expand(steps, GuiStepConfig.map(tc.get("reusable")), Map.of(), new ArrayList<>(), ""));
                }
                normalized.add(tc);
            }
            root.put("testCases", normalized);
        }
        return root;
    }
    private static List<Object> expand(List<?> steps, Map<String,Object> modules, Map<String,Object> inherited,
                                       List<String> stack, String prefix) {
        List<Object> result = new ArrayList<>();
        for (Object entry : steps) {
            if (!(entry instanceof Map)) { result.add(entry); continue; }
            Map<String,Object> step = new LinkedHashMap<>(GuiStepConfig.map(entry));
            if ("GUI".equalsIgnoreCase(Objects.toString(step.get("type"),""))
                    && "CALL".equalsIgnoreCase(Objects.toString(step.get("op"),""))) {
                String name = Objects.toString(step.get("command"),"");
                if (stack.contains(name)) throw new IllegalArgumentException("Recursive GUI module: " + name);
                if (!(modules.get(name) instanceof List<?> module)) throw new IllegalArgumentException("Unknown GUI module: " + name);
                Map<String,Object> parameters = new LinkedHashMap<>(inherited);
                GuiStepConfig.map(step.get("parameters")).forEach((k,v) -> parameters.put(k, substitute(v,inherited)));
                List<String> nested = new ArrayList<>(stack); nested.add(name);
                String call = Objects.toString(step.get("id"),name);
                result.addAll(expand(module,modules,parameters,nested,prefix+call+"/"));
            } else {
                if ("GUI".equalsIgnoreCase(Objects.toString(step.get("type"),""))) {
                    step = GuiStepConfig.map(substitute(step,inherited));
                    if (!prefix.isEmpty()) step.put("id",prefix+Objects.toString(step.get("id"),"step_"+result.size()));
                }
                result.add(step);
            }
        }
        return result;
    }
    private static Object substitute(Object value,Map<String,Object> params) {
        if (value instanceof Map<?,?> map) {
            Map<String,Object> out = new LinkedHashMap<>(); map.forEach((k,v) -> out.put(k.toString(),substitute(v,params))); return out;
        }
        if (value instanceof List<?> list) return list.stream().map(v -> substitute(v,params)).toList();
        if (!(value instanceof String text)) return value;
        Matcher matcher=LOCAL.matcher(text); StringBuffer output=new StringBuffer();
        while (matcher.find()) {
            Object replacement=params.get(matcher.group(1));
            if (replacement==null) throw new IllegalArgumentException("Missing GUI module parameter: " + matcher.group(1));
            matcher.appendReplacement(output,Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(output); return output.toString();
    }
}
