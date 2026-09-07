package at.co.svc.aga.transformator.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.Map;

public class JsonPathFinder {

    public static String findPath(String json, String targetKey) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        String result = findPathRecursive(root, targetKey, "$");
        return result;
    }

    private static String findPathRecursive(JsonNode node, String targetKey, String currentPath) {

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();

                String fieldName = field.getKey();
                JsonNode child = field.getValue();

                String newPath = currentPath + "." + fieldName;

                if (fieldName.equals(targetKey)) {
                    return newPath;
                }

                String found = findPathRecursive(child, targetKey, newPath);
                if (found != null) {
                    return found;
                }
            }
        }

        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String found = findPathRecursive(node.get(i), targetKey, currentPath + "[" + i + "]");
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }
}