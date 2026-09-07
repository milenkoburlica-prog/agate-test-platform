package at.co.svc.tosca.testcases;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class FolderPropagationEngine {

    private final Map<String, JsonNode> index;

    public FolderPropagationEngine(Map<String, JsonNode> index) {
        this.index = index;
    }

    // =========================
    // STATIC ENTRY
    // =========================
    public static String processFile(String input)
            throws Exception {

        ObjectMapper mapper =
                new ObjectMapper();

        JsonNode root =
                mapper.readTree(
                        new File(input)
                );

        Map<String, JsonNode> index =
                new HashMap<>();

        for (JsonNode node : root) {

            String id =
                    node.path("Surrogate")
                            .asText(null);

            if (id != null) {
                index.put(id, node);
            }
        }

        FolderPropagationEngine engine =
                new FolderPropagationEngine(index);

        engine.process(root);

        String outPath =
                input.replace(
                        ".json",
                        "_step4.json"
                );

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File(outPath),
                        root
                );

        MigrationLog.success(
                "Folder propagation output: "
                        + outPath
        );

        return outPath;
    }

    // =========================
    // MAIN
    // =========================
    public static void main(String[] args)
            throws Exception {

        String input =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KRAYCQDX6FQZZY5B4FFZQJ4S_templates-extended-compressed_step2_step3.json";

        processFile(input);
    }

    // =========================
    // ENTRY
    // =========================
    public void process(JsonNode root) {

        MigrationLog.debugSection(
                "FOLDER PROPAGATION ENGINE"
        );

        for (JsonNode node : root) {

            if (!"TestStepFolder".equals(
                    node.path("ObjectClass").asText())) {

                continue;
            }

            String rootCond =
                    node.path("Attributes")
                            .path("Condition")
                            .asText("");

            if (rootCond == null
                    || rootCond.isBlank()) {

                continue;
            }

            MigrationLog.debug(
                    "Root folder: "
                            + node.path("Surrogate").asText()
                            + " | condition: "
                            + rootCond
            );

            propagate(
                    (ObjectNode) node,
                    rootCond,
                    0
            );
        }
    }

    // =========================
    // CORE PROPAGATION
    // =========================
    private void propagate(
            ObjectNode folderNode,
            String ctx,
            int level) {

        JsonNode items =
                folderNode.path("Assocs")
                        .path("Items");

        for (JsonNode idNode : items) {

            JsonNode child =
                    index.get(
                            idNode.asText()
                    );

            if (child == null) {
                continue;
            }

            String type =
                    child.path("ObjectClass")
                            .asText();

            // =========================
            // XTestStep
            // =========================
            if ("XTestStep".equals(type)) {

                ObjectNode attrs =
                        (ObjectNode) child.get("Attributes");

                String old =
                        attrs.path("Condition")
                                .asText("");

                String merged =
                        merge(
                                old,
                                ctx
                        );

                MigrationLog.debug(
                        indent(level)
                                + "STEP: "
                                + child.path("Surrogate").asText()
                                + " | OLD: "
                                + old
                                + " | ADD: "
                                + ctx
                                + " | NEW: "
                                + merged
                );

                attrs.put(
                        "Condition",
                        merged
                );
            }

            // =========================
            // NESTED FOLDER
            // =========================
            else if ("TestStepFolder".equals(type)) {

                String folderCond =
                        child.path("Attributes")
                                .path("Condition")
                                .asText("");

                String newCtx =
                        merge(
                                ctx,
                                folderCond
                        );

                MigrationLog.debug(
                        indent(level)
                                + "FOLDER: "
                                + child.path("Surrogate").asText()
                                + " | CONDITION: "
                                + folderCond
                                + " | NEW CONTEXT: "
                                + newCtx
                );

                propagate(
                        (ObjectNode) child,
                        newCtx,
                        level + 1
                );
            }

            // =========================
            // IF OBJECT
            // =========================
            else if ("TestCaseControlFlowItem".equals(type)) {

                ObjectNode attrs =
                        (ObjectNode) child.get("Attributes");

                String old =
                        attrs.path("Condition")
                                .asText("");

                String merged =
                        merge(
                                old,
                                ctx
                        );

                MigrationLog.debug(
                        indent(level)
                                + "IF: "
                                + child.path("Surrogate").asText()
                                + " | OLD: "
                                + old
                                + " | ADD: "
                                + ctx
                                + " | NEW: "
                                + merged
                );

                attrs.put(
                        "Condition",
                        merged
                );
            }
        }
    }

    // =========================
    // MERGE
    // =========================
    private String merge(
            String a,
            String b) {

        if (isEmpty(a)) {
            return clean(b);
        }

        if (isEmpty(b)) {
            return clean(a);
        }

        if (clean(a).equals(clean(b))) {
            return clean(a);
        }

        return "("
                + clean(a)
                + ") AND ("
                + clean(b)
                + ")";
    }

    private boolean isEmpty(String s) {

        return s == null
                || s.isBlank()
                || "true".equalsIgnoreCase(
                        s.trim()
                );
    }

    private String clean(String s) {

        if (s == null) {
            return "";
        }

        return s.trim();
    }

    // =========================
    // DEBUG
    // =========================
    private String indent(int level) {

        return "   ".repeat(
                Math.max(
                        0,
                        level
                )
        );
    }
}
