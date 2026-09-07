package at.co.svc.tosca.testcases;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class IfPropagationEngine {

    private final Map<String, JsonNode> index;
 // Tracks condition-step IDs that must be removed during cleanup
    private static final Set<String> conditionItemIdsToRemove = new HashSet<>();
    
    public IfPropagationEngine(Map<String, JsonNode> index) {
        this.index = index;
    }

    // =========================
    // STATIC ENTRY
    // =========================
    public static String processFile(String input) throws Exception {

        File inputFile = new File(input);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(inputFile);

        Map<String, JsonNode> index = new HashMap<>();

        for (JsonNode node : root) {

            String s = node.path("Surrogate").asText(null);

            if (s != null) {
                index.put(s, node);
            }
        }

        IfPropagationEngine engine = new IfPropagationEngine(index);

        List<JsonNode> ifItems = new ArrayList<>();

        for (JsonNode node : root) {
            if ("TestCaseControlFlowItem".equals(node.path("ObjectClass").asText())) {
                
                // Resolve the parent of this IF through the index
                String parentId = node.path("Assocs").path("ParentFolder").path(0).asText(null);
                JsonNode parentNode = index.get(parentId);
                
                // If the parent does not exist or is not a TestCaseControlFlowFolder, 
                // this is a top-level IF
                if (parentNode == null || !"TestCaseControlFlowFolder".equals(parentNode.path("ObjectClass").asText())) {
                    ifItems.add(node);
                }
            }
        }

        // Start processing only top-level IF items; 
        // nested IF items receive the correct parent context through recursion.
        engine.process(ifItems);
        
        

        String outPath = input.replace(".json", "_step3.json");

        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outPath), root);

        MigrationLog.success(
                "IF propagation output: "
                        + outPath
        );

//        outPath =
//                input.replace(".json", "_step3.json");
//
//        mapper.writerWithDefaultPrettyPrinter()
//                .writeValue(new File(outPath), root);

        // 👉 SECOND PASS CLEANUP
        String outPathB =
                input.replace(".json", "_step3B.json");

        cleanupControlFlowFolders(root, outPathB, mapper);
        
        mapper.writerWithDefaultPrettyPrinter()
        .writeValue(new File(outPath), mapper.readTree(new File(outPathB)));
        
        return outPath;
    }

    // =========================
    // MAIN
    // =========================
    public static void main(String[] args) throws Exception {

        String input = "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KRAYCQDX6FQZZY5B4FFZQJ4S_templates-extended-compressed_step2.json";
        input = "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\3a13c917-0a3b-dce6-9baf-9e89b2c959ac_reusables-extended-compressed-temp-compressed_step2.json";

        processFile(input);
    }

    // =========================
    // ENTRY
    // =========================
    public void process(List<JsonNode> ifItems) {

        MigrationLog.debugSection(
                "IF PROPAGATION ENGINE"
        );

        for (JsonNode ifItem : ifItems) {
            processIf(ifItem, "true", 0);
        }
    }

    // =========================
    // CORE IF LOGIC
    // =========================
    private void processIf(JsonNode ifNode, String parent, int level) {

        MigrationLog.debug("▶ IF: " + ifNode.path("Surrogate").asText());

        MigrationLog.debug("   PARENT: " + parent);

        List<String> folderIds = new ArrayList<>();

        for (JsonNode n : ifNode.path("Assocs").path("ControlFlowFolders")) {

            folderIds.add(n.asText());
        }

        String local = buildCondition(folderIds);

        MigrationLog.debug("   LOCAL: " + local);

        String thenCtx = combine(parent, local);

        String elseCtx = combine(parent, "NOT (" + local + ")");

        MigrationLog.debug("   THEN: " + thenCtx);

        MigrationLog.debug("   ELSE: " + elseCtx);

        for (String id : folderIds) {

            JsonNode folder = index.get(id);

            if (folder == null) {
                continue;
            }

            String type = folder.path("Attributes").path("StatementType").asText();

            MigrationLog.debug("  ├─ Folder: " + folder.path("Surrogate").asText() + " TYPE=" + type);

            if ("1".equals(type)) {

                MigrationLog.debug("  → THEN APPLY");

                propagate(folder, thenCtx, level + 1);

                processNestedIf(folder, thenCtx, level + 1);
            }

            if ("2".equals(type)) {

                MigrationLog.debug("  → ELSE APPLY");

                propagate(folder, elseCtx, level + 1);

                processNestedIf(folder, elseCtx, level + 1);
            }
        }
    }

    // =========================
    // PROPAGATION
    // =========================
    private void propagate(JsonNode folder, String ctx, int level) {

        for (JsonNode idNode : folder.path("Assocs").path("Items")) {

            JsonNode item = index.get(idNode.asText());

            if (item == null) {
                continue;
            }

            String type = item.path("ObjectClass").asText();

            if ("XTestStep".equals(type)) {

                ObjectNode attrs = (ObjectNode) item.get("Attributes");

                String old = attrs.path("Condition").asText("");

                String merged = merge(old, ctx);

                MigrationLog.debug("     STEP: " + item.path("Surrogate").asText());

                MigrationLog.debug("       OLD: " + old);

                MigrationLog.debug("       INC: " + ctx);

                MigrationLog.debug("       NEW: " + merged);

                attrs.put("Condition", merged);
            }

            if ("TestStepFolder".equals(type)) {

                propagate(item, ctx, level + 1);
            } else if (item.has("Assocs") && item.path("Assocs").has("Items")) {
                // If the object has child items, treat it as a folder regardless of its class name
                propagate(item, ctx, level + 1);
            }
        }
    }


    // =========================
    // NESTED IF
    // =========================
    private void processNestedIf(JsonNode folder, String ctx, int level) {
        for (JsonNode idNode : folder.path("Assocs").path("Items")) {
            JsonNode node = index.get(idNode.asText());
            if (node == null) {
                continue;
            }

            String type = node.path("ObjectClass").asText();

            // 1. If this is a nested IF, process it with the current context
            if ("TestCaseControlFlowItem".equals(type)) {
                processIf(node, ctx, level + 1);
            }
            
            // 2. If this is a regular folder, recurse into it and search for nested IF items using the same context
            if ("TestStepFolder".equals(type)) {
                processNestedIf(node, ctx, level + 1);
            }
        }
    }
    private boolean isIf(JsonNode node) {

        return node != null && "TestCaseControlFlowItem".equals(node.path("ObjectClass").asText());
    }

    // =========================
    // BUILD CONDITION
    // =========================
    private String buildCondition(List<String> folderIds) {
        List<String> conditions = new ArrayList<>();

        for (String folderId : folderIds) {
            JsonNode folder = index.get(folderId);
            if (folder == null) {
                continue;
            }

            if ("0".equals(folder.path("Attributes").path("StatementType").asText())) {
                for (JsonNode itemId : folder.path("Assocs").path("Items")) {
                    
                    // 👉 Record the condition-step ID so it can be removed during cleanup
                    conditionItemIdsToRemove.add(itemId.asText());

                    JsonNode item = index.get(itemId.asText());
                    if (item == null) {
                        continue;
                    }

                    String c = extractCondition(item);
                    if (c != null) {
                        conditions.add("(" + c + ")");
                    }
                }
            }
        }

        return conditions.isEmpty() ? "true" : String.join(" AND ", conditions);
    }
    
    
    private String extractCondition(JsonNode node) {

        if (!"XTestStep".equals(node.path("ObjectClass").asText())) {

            return null;
        }

        for (JsonNode p : node.path("Parameters")) {

            String mode = p.path("ActionMode").asText();

            String value = p.path("Value").asText();

            String name = p.path("ExplicitName").asText();

            if (!"Verify".equalsIgnoreCase(mode)) {
                continue;
            }

            if (value.contains("==")) {
                return value;
            }

            if (!value.isEmpty() && !name.isEmpty()) {

                return "'{B[" + name + "]}'=='" + value + "'";
            }
        }

        return null;
    }

    // =========================
    // HELPERS
    // =========================
    private String merge(String oldCond, String newCond) {
        if (oldCond == null || oldCond.isEmpty() || "true".equals(oldCond)) {
            return newCond;
        }

        if (newCond == null || newCond.isEmpty() || "true".equals(newCond)) {
            return oldCond;
        }

        if (oldCond.equals(newCond)) {
            return oldCond;
        }
        
        // Do not duplicate the new condition if it is already contained in the old condition
        if (oldCond.contains(newCond)) {
            return oldCond;
        }
        if (newCond.contains(oldCond)) {
            return newCond;
        }

        // --- Condition grouping ---
        // Ensure OR expressions are grouped before appending AND
        String safeOld = (oldCond.contains(" OR ") && !oldCond.startsWith("(")) 
                         ? "(" + oldCond + ")" 
                         : oldCond;
        
        String safeNew = (newCond.contains(" OR ") && !newCond.startsWith("(")) 
                         ? "(" + newCond + ")" 
                         : newCond;

        return safeOld + " AND " + safeNew;
    }
    private String combine(String parent, String local) {
        if (parent == null || parent.equals("true") || parent.isEmpty()) {
            // Ensure the local condition is wrapped in parentheses
            if (local != null && !local.startsWith("(")) {
                return "(" + local + ")";
            }
            return local;
        }

        if (local == null || local.equals("true") || local.isEmpty()) {
            if (parent != null && !parent.startsWith("(")) {
                return "(" + parent + ")";
            }
            return parent;
        }

        // Do not append the local condition if the parent already contains it
        if (parent.contains(local)) {
            return parent;
        }

        // Format the result as (PARENT) AND (LOCAL) without unnecessary nested parentheses
        String cleanParent = parent.startsWith("(") && parent.endsWith(")") ? parent : "(" + parent + ")";
        String cleanLocal = local.startsWith("(") && local.endsWith(")") ? local : "(" + local + ")";

        return cleanParent + " AND " + cleanLocal;
    }
    
    
    private String indent(int level) {

        return "   ".repeat(
                Math.max(0, level)
        );
    }

    private static void cleanupControlFlowFolders(JsonNode root, String outPathB, ObjectMapper mapper) throws Exception {
        Set<String> toRemove = new HashSet<>();

        for (JsonNode node : root) {
            String objectClass = node.path("ObjectClass").asText("");
            
            if ("TestCaseControlFlowItem".equals(objectClass) || 
                "TestCaseControlFlowFolder".equals(objectClass)) {
                
                toRemove.add(node.path("Surrogate").asText());
            }
        }

        // 👉 Add collected condition-step IDs to the removal set
        toRemove.addAll(conditionItemIdsToRemove);

        // Create a new root array without control-flow objects and their condition steps
        ArrayNode newRoot = mapper.createArrayNode();
        for (JsonNode node : root) {
            String id = node.path("Surrogate").asText();
            if (!toRemove.contains(id)) {
                newRoot.add(node);
            }
        }

        // Write the cleaned file
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outPathB), newRoot);
        MigrationLog.debug("Cleaned control-flow output: " + outPathB);
        
        // Clear the set in case processFile is called multiple times in the same JVM
        conditionItemIdsToRemove.clear();
    }
    
    
}