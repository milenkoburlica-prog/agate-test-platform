package at.co.svc.tosca.tsu.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.co.svc.tosca.tsu.dto.ToscaNode;

public class TSUFastExtractor {

    private final Map<String, ToscaNode> fullMap;
    private final Map<String, ToscaNode> visited = new HashMap<>();

    private final boolean skipDisabled;

    public TSUFastExtractor(Map<String, ToscaNode> fullMap, boolean skipDisabled) {
        this.fullMap = fullMap;
        this.skipDisabled = skipDisabled;
    }

    // ENTRY POINT
    public Map<String, ToscaNode> extractAll(Collection<String> rootIds) {

        for (String root : rootIds) {
            dfs(root);
        }

        return visited;
    }

    // CORE DFS (O(1) memoized)
    private void dfs(String id) {

        if (visited.containsKey(id)) return;

        ToscaNode node = fullMap.get(id);
        if (node == null) return;

        if (skipDisabled && isDisabled(node)) return;

        visited.put(id, node);

        // children (FAST path)
        if (node.childrenIds != null) {
            for (String child : node.childrenIds) {
                dfs(child);
            }
        }

        // other associations (NO REF SET looping!)
        for (List<String> refs : node.extraAssocs.values()) {
            for (String ref : refs) {
                dfs(ref);
            }
        }
    }

    private boolean isDisabled(ToscaNode node) {
        Object d = node.attributes.get("DisabledDescription");
        if ("".equals(d))  
            return false;
        else
           return true;

//        return d != null && !String.valueOf(d).isEmpty();
    }
}