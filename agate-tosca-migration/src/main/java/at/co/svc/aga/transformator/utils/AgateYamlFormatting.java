package at.co.svc.aga.transformator.utils;

import java.io.StringReader;
import java.util.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;

/** Inserts visual separators at actual YAML step nodes, never inside scalar content. */
public final class AgateYamlFormatting {
    private AgateYamlFormatting() {}
    public static String dump(Yaml yaml,Object document) {
        String text=yaml.dump(orderGuiFields(document, new IdentityHashMap<>()));
        Node root=yaml.compose(new StringReader(text));
        Set<Integer> starts=new HashSet<>();
        collect(root,starts,Collections.newSetFromMap(new IdentityHashMap<>()));
        String[] lines=text.split("\\R",-1);StringBuilder result=new StringBuilder();
        for(int i=0;i<lines.length;i++) {
            if(starts.contains(i)&&i>0&&!lines[i-1].isBlank())result.append('\n');
            result.append(lines[i]);if(i<lines.length-1)result.append('\n');
        }
        return result.toString();
    }
    /** Presentation only: preserve values and order other keys; never mutate caller maps. */
    private static Object orderGuiFields(Object value, IdentityHashMap<Object,Object> copies) {
        if (!(value instanceof Map<?,?>) && !(value instanceof List<?>)) return value;
        if (copies.containsKey(value)) return copies.get(value);
        if (value instanceof Map<?,?> source) {
            Map<Object,Object> ordered = new LinkedHashMap<>();
            copies.put(value, ordered);
            if ("GUI".equalsIgnoreCase(Objects.toString(source.get("type"), ""))) {
                for (String key : List.of("id", "type", "op", "page")) {
                    if (source.containsKey(key)) ordered.put(key, orderGuiFields(source.get(key), copies));
                }
            }
            for (var entry : source.entrySet()) {
                if (!ordered.containsKey(entry.getKey())) {
                    ordered.put(entry.getKey(), orderGuiFields(entry.getValue(), copies));
                }
            }
            return ordered;
        }
        List<Object> ordered = new ArrayList<>();
        copies.put(value, ordered);
        for (Object element : (List<?>) value) ordered.add(orderGuiFields(element, copies));
        return ordered;
    }

    private static void collect(Node node,Set<Integer> starts,Set<Node> visited) {
        if(node==null||!visited.add(node))return;
        if(node instanceof SequenceNode sequence) {
            for(Node child:sequence.getValue()) {
                if(child instanceof MappingNode mapping) {
                    Set<String> keys=new HashSet<>();
                    for(NodeTuple entry:mapping.getValue())if(entry.getKeyNode() instanceof ScalarNode key)keys.add(key.getValue());
                    if(keys.contains("id")&&keys.contains("type"))starts.add(mapping.getStartMark().getLine());
                }
                collect(child,starts,visited);
            }
        } else if(node instanceof MappingNode mapping) {
            for(NodeTuple entry:mapping.getValue())collect(entry.getValueNode(),starts,visited);
        }
    }
}
