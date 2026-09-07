package at.co.svc.tosca.testcases;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ToscaIfConditionPropagator {

    private final ObjectMapper mapper = new ObjectMapper();

    // ulaz -> izlaz fajl
    public String process(File inputJson) throws IOException {

        JsonNode root = mapper.readTree(inputJson);

        // index svih objekata po Surrogate
        Map<String, JsonNode> index = indexBySurrogate(root);

        // 1. pronađi IF control flow items
        List<JsonNode> ifItems = findIfItems(root);

        for (JsonNode ifItem : ifItems) {
            String ifSurrogate = ifItem.get("Surrogate").asText();

            JsonNode ifFoldersNode = ifItem.path("Assocs").path("ControlFlowFolders");

            List<String> folderIds = new ArrayList<>();
            if (ifFoldersNode.isArray()) {
                for (JsonNode n : ifFoldersNode) {
                    folderIds.add(n.asText());
                }
            }

            // 2. izgradi condition iz Condition foldera
            String condition = buildCondition(folderIds, index);

            String negatedCondition = "NOT (" + condition + ")";

            // 3. pronađi THEN / ELSE foldere
            for (String folderId : folderIds) {
                JsonNode folder = index.get(folderId);
                if (folder == null) continue;

                String statementType = folder.path("Attributes").path("StatementType").asText();

                if ("1".equals(statementType)) {
                    // THEN
                    propagate(folder, index, condition);
                } else if ("2".equals(statementType)) {
                    // ELSE
                    propagate(folder, index, negatedCondition);
                }
            }
        }

        // 4. upiši nazad (ili vrati novi JSON)
        File out = new File(inputJson.getParent(), "processed_" + inputJson.getName());
        mapper.writerWithDefaultPrettyPrinter().writeValue(out, root);

        return out.getAbsolutePath();
    }

    // ----------------------------
    // INDEX
    // ----------------------------
    private Map<String, JsonNode> indexBySurrogate(JsonNode root) {
        Map<String, JsonNode> map = new HashMap<>();

        for (JsonNode node : root) {
            String surrogate = node.path("Surrogate").asText(null);
            if (surrogate != null) {
                map.put(surrogate, node);
            }
        }
        return map;
    }

    // ----------------------------
    // IF detection
    // ----------------------------
    private List<JsonNode> findIfItems(JsonNode root) {
        List<JsonNode> list = new ArrayList<>();

        for (JsonNode node : root) {
            if ("TestCaseControlFlowItem".equals(node.path("ObjectClass").asText())) {
                list.add(node);
            }
        }
        return list;
    }

    // ----------------------------
    // BUILD CONDITION (AND logic)
    // ----------------------------
    private String buildCondition(List<String> folderIds, Map<String, JsonNode> index) {

        List<String> conditions = new ArrayList<>();

        for (String id : folderIds) {
            JsonNode folder = index.get(id);
            if (folder == null) continue;

            String statementType = folder.path("Attributes").path("StatementType").asText();

            // samo CONDITION blokovi (StatementType = 0)
            if ("0".equals(statementType)) {
                // uzmi sve stepove u folderu koji imaju Condition logiku
                JsonNode items = folder.path("Assocs").path("Items");

                for (JsonNode itemIdNode : items) {
                    JsonNode item = index.get(itemIdNode.asText());
                    if (item == null) continue;

                    String cond = extractCondition(item);
                    if (cond != null && !cond.isBlank()) {
                        conditions.add("(" + cond + ")");
                    }
                }
            }
        }

        if (conditions.isEmpty()) return "true";
        return String.join(" AND ", conditions);
    }

    // ----------------------------
    // extract condition from evaluation steps
    // ----------------------------
    private String extractCondition(JsonNode node) {

        if (!"XTestStep".equals(node.path("ObjectClass").asText())) {
            return null;
        }

        JsonNode params = node.path("Parameters");
        if (!params.isArray()) return null;

        for (JsonNode p : params) {
            String value = p.path("Value").asText("");
            String mode = p.path("ActionMode").asText("");

            if ("Verify".equalsIgnoreCase(mode) && value.contains("==")) {
                return value;
            }
        }
        return null;
    }

    // ----------------------------
    // PROPAGATION (recursive)
    // ----------------------------
    private void propagate(JsonNode folder, Map<String, JsonNode> index, String condition) {

        JsonNode items = folder.path("Assocs").path("Items");

        for (JsonNode itemIdNode : items) {
            JsonNode item = index.get(itemIdNode.asText());
            if (item == null) continue;

            String type = item.path("ObjectClass").asText();

            if ("XTestStep".equals(type)) {
                // upisi condition
                ((com.fasterxml.jackson.databind.node.ObjectNode)
                        item.get("Attributes")).put("Condition", condition);
            }

            if ("TestStepFolder".equals(type)) {
                // ako ima nested folder → propagiraj dalje
                propagate(item, index, condition);
            }
        }
    }
}