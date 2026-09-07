package at.co.svc.tosca.testcases;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class PropagateConditionStep2 {

    private static final ObjectMapper mapper =
            new ObjectMapper();

    public static String run(String inputPath) throws Exception {

        File inputFile =
                new File(inputPath);

        JsonNode root =
                mapper.readTree(inputFile);

        Map<String, JsonNode> index =
                indexBySurrogate(root);

        List<JsonNode> ifItems =
                findIfItems(root);

        for (JsonNode ifItem : ifItems) {

            List<String> folderIds =
                    new ArrayList<>();

            for (JsonNode n :
                    ifItem.path("Assocs")
                            .path("ControlFlowFolders")) {

                folderIds.add(
                        n.asText()
                );
            }

            MigrationLog.debug(
                    "IF found: "
                            + ifItem.path("Surrogate").asText()
                            + " | ControlFlowFolders: "
                            + folderIds
            );

            String condition =
                    buildCondition(
                            folderIds,
                            index
                    );

            MigrationLog.debug(
                    "Final condition: "
                            + condition
            );

            String negated =
                    "NOT (" + condition + ")";

            for (String id : folderIds) {

                JsonNode folder =
                        index.get(id);

                if (folder == null) {
                    continue;
                }

                String statementType =
                        folder.path("Attributes")
                                .path("StatementType")
                                .asText();

                if ("1".equals(statementType)) {

                    propagate(
                            folder,
                            index,
                            condition
                    );

                } else if ("2".equals(statementType)) {

                    propagate(
                            folder,
                            index,
                            negated
                    );
                }
            }
        }

        File outFile =
                new File(
                        inputFile.getParent(),
                        inputFile.getName()
                                .replace(
                                        ".json",
                                        "_step3.json"
                                )
                );

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        outFile,
                        root
                );

        return outFile.getAbsolutePath();
    }

    // ----------------------------
    // INDEX
    // ----------------------------
    private static Map<String, JsonNode> indexBySurrogate(
            JsonNode root) {

        Map<String, JsonNode> map =
                new HashMap<>();

        for (JsonNode node : root) {

            String s =
                    node.path("Surrogate")
                            .asText(null);

            if (s != null) {
                map.put(
                        s,
                        node
                );
            }
        }

        return map;
    }

    private static List<JsonNode> findIfItems(
            JsonNode root) {

        List<JsonNode> list =
                new ArrayList<>();

        for (JsonNode node : root) {

            if ("TestCaseControlFlowItem".equals(
                    node.path("ObjectClass").asText())) {

                list.add(node);
            }
        }

        return list;
    }

    // ----------------------------
    // BUILD CONDITION
    // ----------------------------
    private static String buildCondition(
            List<String> folderIds,
            Map<String, JsonNode> index) {

        List<String> conditions =
                new ArrayList<>();

        for (String folderId : folderIds) {

            JsonNode folder =
                    index.get(folderId);

            if (folder == null) {
                continue;
            }

            String statementType =
                    folder.path("Attributes")
                            .path("StatementType")
                            .asText();

            MigrationLog.debug(
                    "Folder: "
                            + folderId
                            + " | StatementType: "
                            + statementType
            );

            // Condition block (Evaluation Tool)
            if ("0".equals(statementType)) {

                for (JsonNode itemId :
                        folder.path("Assocs")
                                .path("Items")) {

                    JsonNode item =
                            index.get(
                                    itemId.asText()
                            );

                    if (item == null) {
                        continue;
                    }

                    String cond =
                            extractEvaluationCondition(item);

                    if (cond != null) {

                        MigrationLog.debug(
                                "Extracted evaluation condition: "
                                        + cond
                        );

                        conditions.add(
                                "(" + cond + ")"
                        );

                    } else {

                        MigrationLog.debug(
                                "No evaluation condition in: "
                                        + itemId.asText()
                        );
                    }
                }
            }

            // THEN/ELSE folders are not used to build the condition.
        }

        return conditions.isEmpty()
                ? "true"
                : String.join(
                        " AND ",
                        conditions
                );
    }

    // ----------------------------
    // CONDITION EXTRACTOR
    // ----------------------------
    private static String extractEvaluationCondition(
            JsonNode node) {

        if (!"XTestStep".equals(
                node.path("ObjectClass").asText())) {

            return null;
        }

        for (JsonNode p :
                node.path("Parameters")) {

            String mode =
                    p.path("ActionMode")
                            .asText();

            String value =
                    p.path("Value")
                            .asText();

            String attrId =
                    p.path("ModuleAttributeSurrogate")
                            .asText();

            if (!"Verify".equalsIgnoreCase(mode)) {
                continue;
            }

            // 1. Explicit Evaluation Tool condition
            if (value.contains("==")) {
                return value;
            }

            // 2. Set Buffer fallback logic
            if (!value.isEmpty()
                    && !attrId.isEmpty()) {

                String attrName =
                        resolveAttributeName(
                                node,
                                attrId
                        );

                if (attrName != null) {

                    String cond =
                            "{B["
                                    + attrName
                                    + "]}=='"
                                    + value
                                    + "'";

                    MigrationLog.debug(
                            "Set Buffer condition built: "
                                    + cond
                    );

                    return cond;
                }
            }
        }

        return null;
    }

    private static void processIf(
            JsonNode ifNode,
            Map<String, JsonNode> index,
            String parentCondition) {

        List<String> folderIds =
                new ArrayList<>();

        for (JsonNode n :
                ifNode.path("Assocs")
                        .path("ControlFlowFolders")) {

            folderIds.add(
                    n.asText()
            );
        }

        MigrationLog.debug(
                "IF: "
                        + ifNode.path("Surrogate").asText()
                        + " | PARENT: "
                        + parentCondition
        );

        String localCondition =
                buildCondition(
                        folderIds,
                        index
                );

        String fullCondition =
                combine(
                        parentCondition,
                        localCondition
                );

        MigrationLog.debug(
                "LOCAL: "
                        + localCondition
                        + " | FULL: "
                        + fullCondition
        );

        String negated =
                "NOT (" + fullCondition + ")";

        for (String id : folderIds) {

            JsonNode folder =
                    index.get(id);

            if (folder == null) {
                continue;
            }

            String type =
                    folder.path("Attributes")
                            .path("StatementType")
                            .asText();

            // THEN
            if ("1".equals(type)) {

                propagate(
                        folder,
                        index,
                        fullCondition
                );

                // Process nested IF blocks inside THEN.
                for (JsonNode child :
                        folder.path("Items")) {

                    JsonNode node =
                            index.get(
                                    child.asText()
                            );

                    if (isIf(node)) {
                        processIf(
                                node,
                                index,
                                fullCondition
                        );
                    }
                }
            }

            // ELSE
            if ("2".equals(type)) {

                propagate(
                        folder,
                        index,
                        negated
                );

                // Process nested IF blocks inside ELSE.
                for (JsonNode child :
                        folder.path("Items")) {

                    JsonNode node =
                            index.get(
                                    child.asText()
                            );

                    if (isIf(node)) {
                        processIf(
                                node,
                                index,
                                negated
                        );
                    }
                }
            }
        }
    }

    private static String combine(
            String parent,
            String local) {

        if (parent == null
                || parent.isEmpty()
                || "true".equals(parent)) {

            return local;
        }

        if (local == null
                || local.isEmpty()
                || "true".equals(local)) {

            return parent;
        }

        return "("
                + parent
                + ") AND ("
                + local
                + ")";
    }

    private static boolean isIf(JsonNode node) {

        return node != null
                && "TestCaseControlFlowItem".equals(
                        node.path("ObjectClass").asText()
                );
    }

    private static String resolveAttributeName(
            JsonNode node,
            String attrId) {

        // Fallback heuristic because the module registry is not available here.
        // Use ExplicitName when present.
        for (JsonNode p :
                node.path("Parameters")) {

            String id =
                    p.path("ModuleAttributeSurrogate")
                            .asText();

            if (attrId.equals(id)) {

                String name =
                        p.path("ExplicitName")
                                .asText(null);

                if (name != null
                        && !name.isEmpty()) {

                    return name;
                }
            }
        }

        return null;
    }

    // ----------------------------
    // PROPAGATION
    // ----------------------------
    private static void propagate(
            JsonNode folder,
            Map<String, JsonNode> index,
            String condition) {

        for (JsonNode idNode :
                folder.path("Assocs")
                        .path("Items")) {

            JsonNode item =
                    index.get(
                            idNode.asText()
                    );

            if (item == null) {
                continue;
            }

            String type =
                    item.path("ObjectClass")
                            .asText();

            if ("XTestStep".equals(type)) {

                ((ObjectNode) item.get("Attributes"))
                        .put(
                                "Condition",
                                condition
                        );
            }

            if ("TestStepFolder".equals(type)) {

                propagate(
                        item,
                        index,
                        condition
                );
            }
        }
    }
}
