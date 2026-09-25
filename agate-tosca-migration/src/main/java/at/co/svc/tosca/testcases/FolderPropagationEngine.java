package at.co.svc.tosca.testcases;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class FolderPropagationEngine {

    private final Map<String, JsonNode> index;

    public FolderPropagationEngine(
            Map<String, JsonNode> index) {

        this.index = index;
    }


    // =========================================================
    // STATIC ENTRY
    // =========================================================

    public static String processFile(
            String input) throws Exception {

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

                index.put(
                        id,
                        node
                );
            }
        }

        FolderPropagationEngine engine =
                new FolderPropagationEngine(
                        index
                );

        engine.process(
                root
        );

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


    // =========================================================
    // MAIN
    // =========================================================

    public static void main(
            String[] args) throws Exception {

        String input =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KRAYCQDX6FQZZY5B4FFZQJ4S_templates-extended-compressed_step2_step3.json";

        processFile(input);
    }


    // =========================================================
    // ENTRY
    // =========================================================

    public void process(
            JsonNode root) {

        MigrationLog.debugSection(
                "FOLDER PROPAGATION ENGINE"
        );

        for (JsonNode node : root) {

            if (!"TestStepFolder".equals(
                    node.path("ObjectClass")
                            .asText())) {

                continue;
            }

            String rootCondition =
                    node.path("Attributes")
                            .path("Condition")
                            .asText("");

            if (isTrueOrBlank(
                    rootCondition)) {

                continue;
            }

            MigrationLog.debug(
                    "Root folder: "
                            + node.path("Surrogate")
                            .asText()
                            + " | condition: "
                            + rootCondition
            );

            propagate(
                    (ObjectNode) node,
                    rootCondition,
                    0
            );
        }
    }


    // =========================================================
    // CORE PROPAGATION
    // =========================================================

    private void propagate(
            ObjectNode folderNode,
            String context,
            int level) {

        JsonNode items =
                folderNode.path("Assocs")
                        .path("Items");

        if (!items.isArray()) {
            return;
        }

        for (JsonNode idNode :
                items) {

            JsonNode child =
                    index.get(
                            idNode.asText()
                    );

            System.err.println(
                    "[FOLDER-TRACE] parent="
                            + folderNode.path("Surrogate").asText()
                            + " child="
                            + idNode.asText()
                            + " childType="
                            + (child != null
                                ? child.path("ObjectClass").asText()
                                : "<null>")
                            + " context=["
                            + context
                            + "]"
            );
            
            
            if (child == null) {
                continue;
            }

            String type =
                    child.path("ObjectClass")
                            .asText();

            // =================================================
            // XTestStep
            // =================================================

            if ("XTestStep".equals(
                    type)) {

                ObjectNode attrs =
                        ensureAttributesObject(
                                child
                        );

                String oldCondition =
                        attrs.path("Condition")
                                .asText("");

                String merged =
                        merge(
                                oldCondition,
                                context
                        );

                MigrationLog.debug(
                        indent(level)
                                + "STEP: "
                                + child.path("Surrogate")
                                .asText()
                                + " | OLD: "
                                + oldCondition
                                + " | ADD: "
                                + context
                                + " | NEW: "
                                + merged
                );

                if (!isBlank(merged)) {

                    attrs.put(
                            "Condition",
                            merged
                    );
                }

                continue;
            }


            // =================================================
            // TEST STEP FOLDER REFERENCE / REUSABLE CALL
            // =================================================

            if ("TestStepFolderReference".equals(
                    type)) {

                ObjectNode attrs =
                        ensureAttributesObject(
                                child
                        );

                String oldCondition =
                        attrs.path("Condition")
                                .asText("");

                String merged =
                        merge(
                                oldCondition,
                                context
                        );

                MigrationLog.debug(
                        indent(level)
                                + "FOLDER-REFERENCE: "
                                + child.path("Surrogate")
                                .asText()
                                + " | OLD: "
                                + oldCondition
                                + " | ADD: "
                                + context
                                + " | NEW: "
                                + merged
                );

                System.err.println(
                        "[FOLDER-REF-TRACE] surrogate="
                                + child.path("Surrogate").asText()
                                + " oldCondition=["
                                + oldCondition
                                + "] context=["
                                + context
                                + "] merged=["
                                + merged
                                + "]"
                );
                
                if (!isBlank(merged)) {

                    attrs.put(
                            "Condition",
                            merged
                    );
                }
                System.err.println(
                        "[FOLDER-REF-TRACE-AFTER] surrogate="
                                + child.path("Surrogate").asText()
                                + " condition=["
                                + attrs.path("Condition").asText("")
                                + "]"
                );
                
                continue;
            }


            // =================================================
            // NESTED NORMAL FOLDER
            // =================================================

            if ("TestStepFolder".equals(
                    type)) {

                String folderCondition =
                        child.path("Attributes")
                                .path("Condition")
                                .asText("");

                String newContext =
                        merge(
                                context,
                                folderCondition
                        );

                MigrationLog.debug(
                        indent(level)
                                + "FOLDER: "
                                + child.path("Surrogate")
                                .asText()
                                + " | CONDITION: "
                                + folderCondition
                                + " | NEW CONTEXT: "
                                + newContext
                );

                propagate(
                        (ObjectNode) child,
                        newContext,
                        level + 1
                );

                continue;
            }


            // =================================================
            // IF OBJECT
            // =================================================

            if ("TestCaseControlFlowItem".equals(
                    type)) {

                /*
                 * IF/THEN/ELSE semantics are already handled by
                 * IfPropagationEngine. Do not recurse into the IF
                 * subtree from here.
                 */
                ObjectNode attrs =
                        ensureAttributesObject(
                                child
                        );

                String oldCondition =
                        attrs.path("Condition")
                                .asText("");

                String merged =
                        merge(
                                oldCondition,
                                context
                        );

                MigrationLog.debug(
                        indent(level)
                                + "IF: "
                                + child.path("Surrogate")
                                .asText()
                                + " | OLD: "
                                + oldCondition
                                + " | ADD: "
                                + context
                                + " | NEW: "
                                + merged
                );

                if (!isBlank(merged)) {

                    attrs.put(
                            "Condition",
                            merged
                    );
                }

                continue;
            }


            // =================================================
            // GENERIC WRAPPER
            // =================================================

            if (child.has("Assocs")
                    && child.path("Assocs")
                    .path("Items")
                    .isArray()) {

                if (!"TestCaseControlFlowFolder".equals(
                        type)) {

                    propagate(
                            (ObjectNode) child,
                            context,
                            level + 1
                    );
                }
            }
        }
    }


    // =========================================================
    // SMART CONDITION MERGE
    // =========================================================

    private String merge(
            String a,
            String b) {

        String left =
                normalizeCondition(
                        a
                );

        String right =
                normalizeCondition(
                        b
                );

        if (isTrueOrBlank(
                left)) {

            return right;
        }

        if (isTrueOrBlank(
                right)) {

            return left;
        }

        if (equivalent(
                left,
                right)) {

            return left;
        }

        /*
         * Flatten every AND level recursively.
         *
         * Example:
         *
         *   ((A AND B) AND C)
         *
         * becomes:
         *
         *   A
         *   B
         *   C
         *
         * OR expressions remain atomic:
         *
         *   (A || B)
         *
         * stays one condition.
         */
        List<String> mergedParts =
                new ArrayList<>();

        for (String part :
                flattenAnd(
                        left
                )) {

            addUniqueCondition(
                    mergedParts,
                    part
            );
        }

        for (String part :
                flattenAnd(
                        right
                )) {

            addUniqueCondition(
                    mergedParts,
                    part
            );
        }

        return buildAndExpression(
                mergedParts
        );
    }


    // =========================================================
    // RECURSIVE AND FLATTENING
    // =========================================================

    private List<String> flattenAnd(
            String expression) {

        List<String> result =
                new ArrayList<>();

        String normalized =
                removeRedundantOuterParentheses(
                        normalizeWhitespace(
                                expression
                        )
                );

        if (normalized.isEmpty()) {
            return result;
        }

        List<String> topLevelParts =
                splitTopLevelAndOnce(
                        normalized
                );

        /*
         * No top-level AND -> this expression is atomic.
         * It may contain OR or comparison operators; preserve them.
         */
        if (topLevelParts.size() <= 1) {

            result.add(
                    normalized
            );

            return result;
        }

        /*
         * Recursively flatten each part because one side may itself be:
         *
         *   (A AND B)
         */
        for (String part :
                topLevelParts) {

            result.addAll(
                    flattenAnd(
                            part
                    )
            );
        }

        return result;
    }

    private List<String> splitTopLevelAndOnce(
            String expression) {

        List<String> result =
                new ArrayList<>();

        if (expression == null
                || expression.isBlank()) {

            return result;
        }

        int depth =
                0;

        int start =
                0;

        boolean inSingleQuote =
                false;

        boolean inDoubleQuote =
                false;

        String upper =
                expression.toUpperCase(
                        Locale.ROOT
                );

        for (int i = 0;
             i < expression.length();
             i++) {

            char c =
                    expression.charAt(i);

            /*
             * Do not parse parentheses/operators while inside quotes.
             */
            if (c == '\''
                    && !inDoubleQuote) {

                inSingleQuote =
                        !inSingleQuote;

                continue;
            }

            if (c == '"'
                    && !inSingleQuote) {

                inDoubleQuote =
                        !inDoubleQuote;

                continue;
            }

            if (inSingleQuote
                    || inDoubleQuote) {

                continue;
            }

            if (c == '(') {

                depth++;

                continue;
            }

            if (c == ')') {

                depth =
                        Math.max(
                                0,
                                depth - 1
                        );

                continue;
            }

            if (depth == 0
                    && upper.startsWith(
                    " AND ",
                    i)) {

                result.add(
                        expression.substring(
                                start,
                                i
                        ).trim()
                );

                i +=
                        " AND ".length()
                                - 1;

                start =
                        i + 1;
            }
        }

        result.add(
                expression.substring(
                        start
                ).trim()
        );

        return result;
    }


    // =========================================================
    // CONDITION HELPERS
    // =========================================================

    private void addUniqueCondition(
            List<String> target,
            String candidate) {

        String normalizedCandidate =
                normalizeCondition(
                        candidate
                );

        if (normalizedCandidate.isEmpty()
                || "true".equalsIgnoreCase(
                normalizedCandidate)) {

            return;
        }

        for (String existing :
                target) {

            if (equivalent(
                    existing,
                    normalizedCandidate)) {

                return;
            }
        }

        target.add(
                normalizedCandidate
        );
    }

    private String buildAndExpression(
            List<String> parts) {

        if (parts == null
                || parts.isEmpty()) {

            return "";
        }

        if (parts.size() == 1) {

            return normalizeCondition(
                    parts.get(0)
            );
        }

        StringBuilder result =
                new StringBuilder();

        for (String part :
                parts) {

            if (result.length() > 0) {

                result.append(
                        " AND "
                );
            }

            result.append(
                    wrapForAnd(
                            part
                    )
            );
        }

        return result.toString();
    }

    private String wrapForAnd(
            String condition) {

        String value =
                normalizeCondition(
                        condition
                );

        if (value.isEmpty()) {
            return "";
        }

        /*
         * Keep expressions with top-level OR grouped.
         */
        if (hasTopLevelOr(
                value)) {

            return "("
                    + value
                    + ")";
        }

        if (isFullyWrapped(
                value)) {

            return value;
        }

        return "("
                + value
                + ")";
    }

    private boolean hasTopLevelOr(
            String expression) {

        if (expression == null
                || expression.isBlank()) {

            return false;
        }

        int depth =
                0;

        boolean inSingleQuote =
                false;

        boolean inDoubleQuote =
                false;

        String upper =
                expression.toUpperCase(
                        Locale.ROOT
                );

        for (int i = 0;
             i < expression.length();
             i++) {

            char c =
                    expression.charAt(i);

            if (c == '\''
                    && !inDoubleQuote) {

                inSingleQuote =
                        !inSingleQuote;

                continue;
            }

            if (c == '"'
                    && !inSingleQuote) {

                inDoubleQuote =
                        !inDoubleQuote;

                continue;
            }

            if (inSingleQuote
                    || inDoubleQuote) {

                continue;
            }

            if (c == '(') {

                depth++;

                continue;
            }

            if (c == ')') {

                depth =
                        Math.max(
                                0,
                                depth - 1
                        );

                continue;
            }

            if (depth == 0) {

                if (upper.startsWith(
                        " OR ",
                        i)
                        || upper.startsWith(
                        " || ",
                        i)) {

                    return true;
                }
            }
        }

        return false;
    }

    private boolean equivalent(
            String a,
            String b) {

        return canonical(
                a
        ).equals(
                canonical(
                        b
                )
        );
    }

    private String canonical(
            String value) {

        return removeRedundantOuterParentheses(
                normalizeWhitespace(
                        value
                )
        );
    }

    private String normalizeCondition(
            String value) {

        if (value == null) {
            return "";
        }

        return removeRedundantOuterParentheses(
                normalizeWhitespace(
                        value
                )
        );
    }

    private String normalizeWhitespace(
            String value) {

        return value == null
                ? ""
                : value.trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    private String removeRedundantOuterParentheses(
            String value) {

        String result =
                value == null
                        ? ""
                        : value.trim();

        while (isFullyWrapped(
                result)) {

            result =
                    result.substring(
                            1,
                            result.length() - 1
                    ).trim();
        }

        return result;
    }

    private boolean isFullyWrapped(
            String value) {

        if (value == null
                || value.length() < 2
                || value.charAt(0) != '('
                || value.charAt(
                value.length() - 1) != ')') {

            return false;
        }

        int depth =
                0;

        boolean inSingleQuote =
                false;

        boolean inDoubleQuote =
                false;

        for (int i = 0;
             i < value.length();
             i++) {

            char c =
                    value.charAt(i);

            if (c == '\''
                    && !inDoubleQuote) {

                inSingleQuote =
                        !inSingleQuote;

                continue;
            }

            if (c == '"'
                    && !inSingleQuote) {

                inDoubleQuote =
                        !inDoubleQuote;

                continue;
            }

            if (inSingleQuote
                    || inDoubleQuote) {

                continue;
            }

            if (c == '(') {

                depth++;

            } else if (c == ')') {

                depth--;

                if (depth == 0
                        && i
                        < value.length() - 1) {

                    return false;
                }
            }

            if (depth < 0) {
                return false;
            }
        }

        return depth == 0;
    }

    private boolean isTrueOrBlank(
            String value) {

        return isBlank(value)
                || "true".equalsIgnoreCase(
                value.trim()
        );
    }

    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }


    // =========================================================
    // JSON HELPERS
    // =========================================================

    private ObjectNode ensureAttributesObject(
            JsonNode node) {

        JsonNode attrs =
                node.get(
                        "Attributes"
                );

        if (attrs instanceof ObjectNode) {

            return (ObjectNode) attrs;
        }

        ObjectNode objectNode =
                new ObjectMapper()
                        .createObjectNode();

        ((ObjectNode) node).set(
                "Attributes",
                objectNode
        );

        return objectNode;
    }


    // =========================================================
    // DEBUG
    // =========================================================

    private String indent(
            int level) {

        return "   ".repeat(
                Math.max(
                        0,
                        level
                )
        );
    }
}
