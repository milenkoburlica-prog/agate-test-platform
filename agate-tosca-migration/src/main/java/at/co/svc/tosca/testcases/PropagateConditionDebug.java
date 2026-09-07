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

public class PropagateConditionDebug {

    private static final ObjectMapper mapper =
            new ObjectMapper();

    public static void main(String[] args) throws Exception {

        String inputPath =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KRAYCQDX6FQZZY5B4FFZQJ4S_templates-extended-compressed_step2.json";

        MigrationLog.debugSection(
                "TOSCA IF PROPAGATION DEBUG RUN"
        );

        MigrationLog.debug(
                "Input: "
                        + inputPath
        );

        // STEP 1: debug only
        PropagateConditionDebug.run(inputPath);

        MigrationLog.debugSection(
                "RUN STEP2 (REAL PROPAGATION)"
        );

        // STEP 2: real propagation logic
        String out =
                PropagateConditionStep2.run(inputPath);

        MigrationLog.debug(
                "Output file: "
                        + out
        );

        MigrationLog.debugSection(
                "FINISHED FULL PIPELINE"
        );
    }

    public static void run(String inputPath) throws Exception {

        File inputFile =
                new File(inputPath);

        JsonNode root =
                mapper.readTree(inputFile);

        Map<String, JsonNode> index =
                indexBySurrogate(root);

        List<JsonNode> ifItems =
                findIfItems(root);

        MigrationLog.debugSection(
                "DEBUG IF PROPAGATION START"
        );

        for (JsonNode ifItem : ifItems) {

            MigrationLog.debug(
                    "IF found: "
                            + ifItem.path("Surrogate").asText()
            );

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
                    "ControlFlowFolders: "
                            + folderIds
            );

            // STEP 1: extract conditions
            List<String> extracted =
                    new ArrayList<>();

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

                MigrationLog.debug(
                        "Folder: "
                                + folder.path("Surrogate").asText()
                                + " | StatementType: "
                                + statementType
                );

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
                                extractCondition(item);

                        if (cond != null) {

                            MigrationLog.debug(
                                    "Extracted condition: "
                                            + cond
                            );

                            extracted.add(
                                    "(" + cond + ")"
                            );

                        } else {

                            MigrationLog.debug(
                                    "No condition found in step: "
                                            + item.path("Surrogate").asText()
                            );
                        }
                    }
                }
            }

            // STEP 2: build IF condition
            String condition =
                    extracted.isEmpty()
                            ? "true"
                            : String.join(
                                    " AND ",
                                    extracted
                            );

            MigrationLog.debug(
                    "Aggregated IF condition: "
                            + condition
            );

            String negated =
                    "NOT (" + condition + ")";

            MigrationLog.debug(
                    "Else condition: "
                            + negated
            );

            // STEP 3: propagate
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

                MigrationLog.debug(
                        "Applying to folder: "
                                + id
                                + " | StatementType: "
                                + statementType
                );

                if ("1".equals(statementType)) {

                    MigrationLog.debug(
                            "THEN <- "
                                    + condition
                    );

                    propagateDebug(
                            folder,
                            index,
                            condition
                    );

                } else if ("2".equals(statementType)) {

                    MigrationLog.debug(
                            "ELSE <- "
                                    + negated
                    );

                    propagateDebug(
                            folder,
                            index,
                            negated
                    );
                }
            }
        }

        MigrationLog.debugSection(
                "DEBUG IF PROPAGATION END"
        );
    }

    // ----------------------------
    // PROPAGATION DEBUG
    // ----------------------------
    private static void propagateDebug(
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

                String old =
                        item.path("Attributes")
                                .path("Condition")
                                .asText();

                MigrationLog.debug(
                        "STEP: "
                                + item.path("Surrogate").asText()
                                + " | OLD: "
                                + old
                                + " | NEW: "
                                + condition
                );

                ((ObjectNode) item.get("Attributes"))
                        .put(
                                "Condition",
                                condition
                        );
            }

            if ("TestStepFolder".equals(type)) {

                propagateDebug(
                        item,
                        index,
                        condition
                );
            }
        }
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

    // ----------------------------
    // IF FIND
    // ----------------------------
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
    // EXTRACT CONDITION
    // ----------------------------
    private static String extractCondition(
            JsonNode node) {

        if (!"XTestStep".equals(
                node.path("ObjectClass").asText())) {

            return null;
        }

        for (JsonNode p :
                node.path("Parameters")) {

            String value =
                    p.path("Value")
                            .asText("");

            String mode =
                    p.path("ActionMode")
                            .asText("");

            if ("Verify".equalsIgnoreCase(mode)
                    && value.contains("==")) {

                return value;
            }
        }

        return null;
    }
}
