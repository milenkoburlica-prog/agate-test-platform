package at.co.svc.agate.engine.gui;

import java.util.*;

/** Shared schema/runtime normalization for human-readable GUI element descriptions. */
public final class GuiElementTarget {
    public static final List<String> KINDS = List.of("link", "button", "textbox", "dropdown", "checkbox", "radio", "text", "heading");
    private GuiElementTarget() {}

    public static Map<String,Object> fromStep(Map<String,Object> step) {
        List<String> found = KINDS.stream().filter(step::containsKey).toList();
        if (found.isEmpty()) return Collections.emptyMap();
        if (found.size()!=1 || step.containsKey("target") || step.containsKey("selector"))
            throw new IllegalArgumentException("Use exactly one simplified element OR target/selector");
        String kind=found.get(0);
        if (!(step.get(kind) instanceof Map<?,?> source)) throw new IllegalArgumentException(kind+" must be an object");
        Map<String,Object> spec=new LinkedHashMap<>();source.forEach((k,v)->spec.put(k.toString(),v));
        List<String> strategies = switch(kind) {
            case "textbox" -> List.of("label","placeholder","id");
            case "dropdown","checkbox","radio" -> List.of("label","id");
            case "text" -> List.of("value","id");
            default -> List.of("text","id");
        };
        Set<String> allowed=new HashSet<>(strategies);allowed.addAll(List.of("exact","index"));
        if (!allowed.containsAll(spec.keySet())) throw new IllegalArgumentException("Unsupported "+kind+" field");
        List<String> chosen=strategies.stream().filter(spec::containsKey).toList();
        if (chosen.size()!=1) throw new IllegalArgumentException(kind+" requires exactly one of "+strategies);
        String strategy=chosen.get(0);
        if (!(spec.get(strategy) instanceof String value) || value.isBlank())
            throw new IllegalArgumentException(kind+"."+strategy+" requires nonempty text");
        if (spec.containsKey("exact") && !(spec.get("exact") instanceof Boolean))
            throw new IllegalArgumentException(kind+".exact must be boolean");
        Map<String,Object> target=new LinkedHashMap<>();
        boolean exact=!Boolean.FALSE.equals(spec.get("exact"));
        if (strategy.equals("id")) {
            if (spec.containsKey("exact"))throw new IllegalArgumentException(kind+".id is always exact; omit exact");
            String tag=switch(kind) {
                case "link" -> "a";
                case "button" -> ":is(button,input[type=button],input[type=submit],input[type=reset])";
                case "textbox" -> ":is(input,textarea)";
                case "dropdown" -> "select";
                case "checkbox" -> "input[type=checkbox]";
                case "radio" -> "input[type=radio]";
                case "heading" -> ":is(h1,h2,h3,h4,h5,h6)";
                default -> "";
            };
            target.put("css",tag+"[id=\""+cssString(value)+"\"]");
        } else {
            target.put("exact",exact);
            switch(kind) {
                case "link" -> target.put("linkText",value);
                case "button","heading","checkbox","radio" -> {
                    target.put("role",kind);target.put("name",value);
                }
                case "textbox","dropdown" -> target.put(strategy,value);
                case "text" -> target.put("text",value);
                default -> throw new IllegalArgumentException("Unsupported element "+kind);
            }
        }
        if (spec.containsKey("index")) {
            Object index=spec.get("index");
            if (!(index instanceof Byte || index instanceof Short || index instanceof Integer || index instanceof Long)
                    || ((Number)index).longValue()<0 || ((Number)index).longValue()>Integer.MAX_VALUE)
                throw new IllegalArgumentException(kind+".index must be a nonnegative integer starting at 0");
            target.put("nth",((Number)index).intValue());
        }
        return target;
    }
    private static String cssString(String value) {
        StringBuilder out=new StringBuilder();
        for(char c:value.toCharArray()) {
            if(c=='\\' || c=='"')out.append('\\').append(c);
            else if(c<32 || c==127)out.append('\\').append(Integer.toHexString(c)).append(' ');
            else out.append(c);
        }
        return out.toString();
    }
}
