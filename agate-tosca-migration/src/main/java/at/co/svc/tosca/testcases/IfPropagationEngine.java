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
import at.co.svc.tosca.transformation.ToscaLoopSupport;

public class IfPropagationEngine {

    private final Map<String, JsonNode> index;

    /*
     * Condition steps that were successfully converted into AGATE
     * IF conditions.
     *
     * IMPORTANT:
     * LOOP condition steps are never added here.
     */
    private static final Set<String> conditionItemIdsToRemove =
            new HashSet<>();


    public IfPropagationEngine(
            Map<String, JsonNode> index) {

        this.index =
                index;
    }


    // =========================================================
    // STATIC ENTRY
    // =========================================================

    public static String processFile(
            String input) throws Exception {

        File inputFile =
                new File(
                        input
                );

        ObjectMapper mapper =
                new ObjectMapper();

        JsonNode root =
                mapper.readTree(
                        inputFile
                );

        Map<String, JsonNode> index =
                new HashMap<>();

        for (JsonNode node :
                root) {

            String surrogate =
                    getNodeId(
                            node
                    );

            if (surrogate != null
                    && !surrogate.isBlank()) {

                index.put(
                        surrogate,
                        node
                );
            }
        }

        /*
         * processFile can be called several times in the same JVM.
         */
        conditionItemIdsToRemove.clear();

        IfPropagationEngine engine =
                new IfPropagationEngine(
                        index
                );

        List<JsonNode> topLevelIfItems =
                new ArrayList<>();


        for (JsonNode node :
                root) {

            if (!"TestCaseControlFlowItem".equals(
                    node.path("ObjectClass")
                            .asText())) {

                continue;
            }


            /*
             * LOOP must stay structurally intact.
             *
             * Both:
             *
             *   StatementType 2 = WHILE_DO
             *   StatementType 3 = DO_WHILE
             *
             * are deliberately ignored by the IF propagation engine.
             */
            if (ToscaLoopSupport.isLoopControlFlow(
                    node)) {

                MigrationLog.debug(
                        "[IF] LOOP preserved: "
                                + getNodeId(node)
                                + " mode="
                                + ToscaLoopSupport.getLoopMode(node)
                );

                continue;
            }


            if (isDisabled(node)) {

                MigrationLog.debug(
                        "[IF] Disabled control-flow ignored: "
                                + getNodeId(node)
                                + " name="
                                + node.path("Attributes")
                                .path("Name")
                                .asText("")
                );

                continue;
            }

            if (!hasControlFlowAncestor(
                    node,
                    index)) {

                topLevelIfItems.add(
                        node
                );
            }
        }


        engine.process(
                topLevelIfItems
        );


        String outPath =
                input.replace(
                        ".json",
                        "_step3.json"
                );


        String outPathB =
                input.replace(
                        ".json",
                        "_step3B.json"
                );


        cleanupControlFlowFolders(
                root,
                outPathB,
                mapper,
                index
        );


        JsonNode cleanedRoot =
                mapper.readTree(
                        new File(
                                outPathB
                        )
                );


        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File(outPath),
                        cleanedRoot
                );


        MigrationLog.success(
                "IF propagation output: "
                        + outPath
        );


        conditionItemIdsToRemove.clear();

        return outPath;
    }


    // =========================================================
    // MAIN
    // =========================================================

    public static void main(
            String[] args) throws Exception {

        String input =
                "C:\\work\\projects\\agate-studio\\agate-tosca-migration-svc\\jsonOut\\test_step2.json";

        processFile(
                input
        );
    }


    // =========================================================
    // ENTRY
    // =========================================================

    public void process(
            List<JsonNode> ifItems) {

        MigrationLog.debugSection(
                "IF PROPAGATION ENGINE"
        );

        for (JsonNode ifItem :
                ifItems) {

            /*
             * Safety guard in case process() is called directly.
             */
            if (ToscaLoopSupport.isLoopControlFlow(
                    ifItem)
                    || isDisabled(ifItem)) {

                continue;
            }

            processIf(
                    ifItem,
                    "",
                    0
            );
        }
    }


    // =========================================================
    // CORE IF LOGIC
    // =========================================================

    private void processIf(
            JsonNode ifNode,
            String parentContext,
            int level) {

        /*
         * LOOP is explicitly not an IF.
         */
        if (ToscaLoopSupport.isLoopControlFlow(
                ifNode)
                || isDisabled(ifNode)) {

            return;
        }


        String ifId =
                getNodeId(
                        ifNode
                );


        MigrationLog.debug(
                indent(level)
                        + "▶ IF: "
                        + ifId
        );


        MigrationLog.debug(
                indent(level)
                        + "  PARENT: "
                        + parentContext
        );


        List<String> folderIds =
                getControlFlowFolderIds(
                        ifNode
                );


        String localCondition =
                buildCondition(
                        folderIds
                );


        MigrationLog.debug(
                indent(level)
                        + "  LOCAL: "
                        + localCondition
        );


        if (isBlank(
                localCondition)) {

            MigrationLog.info(
                    "[Review] IF condition could not be migrated"
                            + " - IF="
                            + ifId
                            + ". Control-flow branch was not flattened."
            );

            return;
        }


        String thenContext =
                combine(
                        parentContext,
                        localCondition
                );


        String elseContext =
                combine(
                        parentContext,
                        negate(
                                localCondition
                        )
                );


        MigrationLog.debug(
                indent(level)
                        + "  THEN: "
                        + thenContext
        );


        MigrationLog.debug(
                indent(level)
                        + "  ELSE: "
                        + elseContext
        );


        for (String folderId :
                folderIds) {

            JsonNode folder =
                    index.get(
                            folderId
                    );

            if (folder == null) {
                continue;
            }


            String statementType =
                    folder.path("Attributes")
                            .path("StatementType")
                            .asText();


            MigrationLog.debug(
                    indent(level)
                            + "  Folder: "
                            + folderId
                            + " TYPE="
                            + statementType
            );


            if ("1".equals(
                    statementType)) {

                propagateBranch(
                        folder,
                        thenContext,
                        level + 1
                );

                processNestedIfs(
                        folder,
                        thenContext,
                        level + 1
                );

            } else if ("2".equals(
                    statementType)) {

                propagateBranch(
                        folder,
                        elseContext,
                        level + 1
                );

                processNestedIfs(
                        folder,
                        elseContext,
                        level + 1
                );
            }
        }
    }


    // =========================================================
    // PROPAGATION
    // =========================================================

    private void propagateBranch(
            JsonNode container,
            String context,
            int level) {

        JsonNode items =
                container.path("Assocs")
                        .path("Items");

        if (!items.isArray()) {
            return;
        }


        for (JsonNode idNode :
                items) {

            JsonNode item =
                    index.get(
                            idNode.asText()
                    );

            if (item == null) {
                continue;
            }


            String objectClass =
                    item.path("ObjectClass")
                            .asText();


            /*
             * Nested control flow is handled separately.
             */
            if ("TestCaseControlFlowItem".equals(
                    objectClass)) {

                continue;
            }


            if ("TestCaseControlFlowFolder".equals(
                    objectClass)) {

                continue;
            }


            if ("XTestStep".equals(
                    objectClass)) {

                ObjectNode attrs =
                        ensureAttributesObject(
                                item
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
                                + "STEP "
                                + getNodeId(item)
                                + " | OLD="
                                + oldCondition
                                + " | ADD="
                                + context
                                + " | NEW="
                                + merged
                );


                if (!isBlank(
                        merged)) {

                    attrs.put(
                            "Condition",
                            merged
                    );
                }

                continue;
            }


            if ("TestStepFolder".equals(
                    objectClass)) {

                propagateBranch(
                        item,
                        context,
                        level + 1
                );

                continue;
            }


            if (item.has("Assocs")
                    && item.path("Assocs")
                    .path("Items")
                    .isArray()) {

                propagateBranch(
                        item,
                        context,
                        level + 1
                );
            }
        }
    }


    // =========================================================
    // NESTED IF
    // =========================================================

    private void processNestedIfs(
            JsonNode container,
            String parentContext,
            int level) {

        JsonNode items =
                container.path("Assocs")
                        .path("Items");

        if (!items.isArray()) {
            return;
        }


        for (JsonNode idNode :
                items) {

            JsonNode node =
                    index.get(
                            idNode.asText()
                    );

            if (node == null) {
                continue;
            }


            String objectClass =
                    node.path("ObjectClass")
                            .asText();


            if ("TestCaseControlFlowItem".equals(
                    objectClass)) {

                if (ToscaLoopSupport.isLoopControlFlow(
                        node)
                        || isDisabled(node)) {

                    continue;
                }

                processIf(
                        node,
                        parentContext,
                        level + 1
                );

                continue;
            }


            if ("TestStepFolder".equals(
                    objectClass)) {

                processNestedIfs(
                        node,
                        parentContext,
                        level + 1
                );

                continue;
            }


            if (!"TestCaseControlFlowFolder".equals(
                    objectClass)
                    && node.has("Assocs")
                    && node.path("Assocs")
                    .path("Items")
                    .isArray()) {

                processNestedIfs(
                        node,
                        parentContext,
                        level + 1
                );
            }
        }
    }


    // =========================================================
    // BUILD CONDITION
    // =========================================================

    private String buildCondition(
            List<String> folderIds) {

        List<String> conditions =
                new ArrayList<>();


        for (String folderId :
                folderIds) {

            JsonNode folder =
                    index.get(
                            folderId
                    );

            if (folder == null) {
                continue;
            }


            String statementType =
                    folder.path("Attributes")
                            .path("StatementType")
                            .asText();


            if (!"0".equals(
                    statementType)) {

                continue;
            }


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


                String condition =
                        extractCondition(
                                item
                        );


                if (!isBlank(
                        condition)) {

                    conditionItemIdsToRemove.add(
                            itemId.asText()
                    );

                    addUniqueCondition(
                            conditions,
                            condition
                    );

                } else {

                    MigrationLog.info(
                            "[Review] Could not extract IF condition from step "
                                    + itemId.asText()
                    );
                }
            }
        }


        return joinConditions(
                conditions
        );
    }


    // =========================================================
    // CONDITION EXTRACTION
    // =========================================================

    private String extractCondition(
            JsonNode node) {

        if (!"XTestStep".equals(
                node.path("ObjectClass")
                        .asText())) {

            return null;
        }


        for (JsonNode parameter :
                node.path("Parameters")) {

            String mode =
                    parameter.path("ActionMode")
                            .asText("");

            if (!"Verify".equalsIgnoreCase(
                    mode)) {

                continue;
            }


            String value =
                    parameter.path("Value")
                            .asText("")
                            .trim();

            String explicitName =
                    parameter.path("ExplicitName")
                            .asText("")
                            .trim();


            if (value.isEmpty()) {
                continue;
            }


            if (looksLikeConditionExpression(
                    value)) {

                return normalizeCondition(
                        value
                );
            }


            if (!explicitName.isEmpty()) {

                return "'{B["
                        + explicitName
                        + "]}' == '"
                        + escapeSingleQuotes(
                        value
                )
                        + "'";
            }
        }


        return null;
    }


    private boolean looksLikeConditionExpression(
            String value) {

        String v =
                value == null
                        ? ""
                        : value.trim();

        if (v.isEmpty()) {
            return false;
        }


        return v.contains("==")
                || v.contains("!=")
                || v.contains(">=")
                || v.contains("<=")
                || containsStandaloneGreaterThan(v)
                || containsStandaloneLessThan(v)
                || v.contains("&&")
                || v.contains("||")
                || containsWordIgnoreCase(v, "AND")
                || containsWordIgnoreCase(v, "OR")
                || startsWithIgnoreCase(v, "NOT ");
    }


    // =========================================================
    // CONDITION MERGING
    // =========================================================

    private String merge(
            String oldCondition,
            String newCondition) {

        String oldCond =
                normalizeCondition(
                        oldCondition
                );

        String newCond =
                normalizeCondition(
                        newCondition
                );


        if (isTrueOrBlank(
                oldCond)) {

            return newCond;
        }


        if (isTrueOrBlank(
                newCond)) {

            return oldCond;
        }


        if (equivalent(
                oldCond,
                newCond)) {

            return oldCond;
        }


        if (containsConjunct(
                oldCond,
                newCond)) {

            return oldCond;
        }


        if (containsConjunct(
                newCond,
                oldCond)) {

            return newCond;
        }


        return wrapForAnd(
                oldCond
        )
                + " AND "
                + wrapForAnd(
                newCond
        );
    }


    private String combine(
            String parent,
            String local) {

        return merge(
                parent,
                local
        );
    }


    private String negate(
            String condition) {

        String c =
                normalizeCondition(
                        condition
                );

        if (isBlank(
                c)) {

            return "";
        }


        if (startsWithIgnoreCase(
                c,
                "NOT (")
                && c.endsWith(")")) {

            return c.substring(
                    5,
                    c.length() - 1
            ).trim();
        }


        return "NOT ("
                + c
                + ")";
    }


    // =========================================================
    // CLEANUP
    // =========================================================

    private static void cleanupControlFlowFolders(
            JsonNode root,
            String outPathB,
            ObjectMapper mapper,
            Map<String, JsonNode> index) throws Exception {

        Set<String> idsToPreserve =
                collectLoopOwnedIds(
                        root,
                        index
                );

        Set<String> controlFlowIds =
                new HashSet<>();

        for (JsonNode node :
                root) {

            String objectClass =
                    node.path("ObjectClass")
                            .asText("");

            String id =
                    getNodeId(
                            node
                    );

            if (id == null
                    || id.isBlank()) {

                continue;
            }

            if (("TestCaseControlFlowItem".equals(
                    objectClass)
                    || "TestCaseControlFlowFolder".equals(
                    objectClass))
                    && !idsToPreserve.contains(id)) {

                controlFlowIds.add(
                        id
                );
            }
        }

        Set<String> toRemove =
                new HashSet<>(
                        controlFlowIds
                );

        toRemove.addAll(
                conditionItemIdsToRemove
        );

        toRemove.removeAll(
                idsToPreserve
        );

        ArrayNode newRoot =
                mapper.createArrayNode();

        Set<String> emitted =
                new HashSet<>();

        boolean foundExecutionRoot =
                false;

        for (JsonNode node :
                root) {

            if (!isExecutionRoot(
                    node)) {

                continue;
            }

            foundExecutionRoot =
                    true;

            emitOrdered(
                    node,
                    newRoot,
                    emitted,
                    toRemove,
                    idsToPreserve,
                    index
            );
        }

        if (!foundExecutionRoot) {

            for (JsonNode node :
                    root) {

                String id =
                        getNodeId(
                                node
                        );

                if (id == null
                        || !toRemove.contains(id)) {

                    newRoot.add(
                            node
                    );
                }
            }
        }

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File(outPathB),
                        newRoot
                );

        MigrationLog.debug(
                "Cleaned and reordered control-flow output: "
                        + outPathB
        );
    }


    private static boolean isExecutionRoot(
            JsonNode node) {

        String objectClass =
                node.path("ObjectClass")
                        .asText("");

        return "TestCase".equals(
                objectClass)
                || "ReuseableTestStepBlock".equals(
                objectClass);
    }


    private static void emitOrdered(
            JsonNode node,
            ArrayNode out,
            Set<String> emitted,
            Set<String> toRemove,
            Set<String> idsToPreserve,
            Map<String, JsonNode> index) {

        if (node == null) {
            return;
        }

        String id =
                getNodeId(
                        node
                );

        String objectClass =
                node.path("ObjectClass")
                        .asText("");

        if (isDisabled(
                node)) {

            MigrationLog.debug(
                    "[IF] Disabled node ignored during ordering: "
                            + id
                            + " name="
                            + node.path("Attributes")
                            .path("Name")
                            .asText("")
            );

            return;
        }

        if ("TestCaseControlFlowItem".equals(
                objectClass)
                && !idsToPreserve.contains(id)) {

            emitFlattenedControlFlowItem(
                    node,
                    out,
                    emitted,
                    toRemove,
                    idsToPreserve,
                    index
            );

            return;
        }

        if ("TestCaseControlFlowFolder".equals(
                objectClass)
                && !idsToPreserve.contains(id)) {

            emitFlattenedControlFlowFolder(
                    node,
                    out,
                    emitted,
                    toRemove,
                    idsToPreserve,
                    index
            );

            return;
        }

        if (id != null
                && !id.isBlank()) {

            if (toRemove.contains(id)) {
                return;
            }

            if (!emitted.add(id)) {
                return;
            }
        }

        out.add(
                node
        );

        if ("TestCaseControlFlowItem".equals(
                objectClass)) {

            JsonNode controlFlowFolders =
                    node.path("Assocs")
                            .path("ControlFlowFolders");

            if (controlFlowFolders.isArray()) {

                for (JsonNode folderId :
                        controlFlowFolders) {

                    emitOrdered(
                            index.get(
                                    folderId.asText()
                            ),
                            out,
                            emitted,
                            toRemove,
                            idsToPreserve,
                            index
                    );
                }
            }

            return;
        }

        JsonNode items =
                node.path("Assocs")
                        .path("Items");

        if (!items.isArray()) {
            return;
        }

        for (JsonNode childId :
                items) {

            JsonNode child =
                    index.get(
                            childId.asText()
                    );

            if (child == null) {

                MigrationLog.debug(
                        "[IF] Missing referenced node ignored during ordering: "
                                + childId.asText()
                );

                continue;
            }

            emitOrdered(
                    child,
                    out,
                    emitted,
                    toRemove,
                    idsToPreserve,
                    index
            );
        }
    }


    private static void emitFlattenedControlFlowItem(
            JsonNode controlFlowItem,
            ArrayNode out,
            Set<String> emitted,
            Set<String> toRemove,
            Set<String> idsToPreserve,
            Map<String, JsonNode> index) {

        if (isDisabled(
                controlFlowItem)) {

            return;
        }

        JsonNode folders =
                controlFlowItem.path("Assocs")
                        .path("ControlFlowFolders");

        if (!folders.isArray()) {
            return;
        }

        for (JsonNode folderId :
                folders) {

            JsonNode folder =
                    index.get(
                            folderId.asText()
                    );

            if (folder == null) {
                continue;
            }

            String statementType =
                    folder.path("Attributes")
                            .path("StatementType")
                            .asText("");

            if ("0".equals(
                    statementType)) {

                continue;
            }

            emitFlattenedControlFlowFolder(
                    folder,
                    out,
                    emitted,
                    toRemove,
                    idsToPreserve,
                    index
            );
        }
    }


    private static void emitFlattenedControlFlowFolder(
            JsonNode folder,
            ArrayNode out,
            Set<String> emitted,
            Set<String> toRemove,
            Set<String> idsToPreserve,
            Map<String, JsonNode> index) {

        String statementType =
                folder.path("Attributes")
                        .path("StatementType")
                        .asText("");

        if ("0".equals(
                statementType)) {

            return;
        }

        JsonNode items =
                folder.path("Assocs")
                        .path("Items");

        if (!items.isArray()) {
            return;
        }

        for (JsonNode childId :
                items) {

            JsonNode child =
                    index.get(
                            childId.asText()
                    );

            if (child == null) {
                continue;
            }

            emitOrdered(
                    child,
                    out,
                    emitted,
                    toRemove,
                    idsToPreserve,
                    index
            );
        }
    }



    /*
     * Collect:
     *
     * LOOP item
     *   -> ControlFlowFolders
     *      -> Items
     *
     * recursively.
     *
     * Everything belonging to this subtree must survive IF cleanup.
     */
    private static Set<String> collectLoopOwnedIds(
            JsonNode root,
            Map<String, JsonNode> index) {

        Set<String> result =
                new HashSet<>();


        for (JsonNode node :
                root) {

            if (!ToscaLoopSupport.isLoopControlFlow(
                    node)) {

                continue;
            }


            collectLoopOwnedIdsRecursive(
                    node,
                    index,
                    result
            );
        }


        return result;
    }


    private static void collectLoopOwnedIdsRecursive(
            JsonNode node,
            Map<String, JsonNode> index,
            Set<String> result) {

        if (node == null) {
            return;
        }


        String id =
                getNodeId(
                        node
                );


        if (id != null
                && !id.isBlank()) {

            if (!result.add(id)) {
                return;
            }
        }


        JsonNode controlFlowFolders =
                node.path("Assocs")
                        .path("ControlFlowFolders");


        if (controlFlowFolders.isArray()) {

            for (JsonNode folderId :
                    controlFlowFolders) {

                JsonNode folder =
                        index.get(
                                folderId.asText()
                        );

                collectLoopOwnedIdsRecursive(
                        folder,
                        index,
                        result
                );
            }
        }


        JsonNode items =
                node.path("Assocs")
                        .path("Items");


        if (items.isArray()) {

            for (JsonNode itemId :
                    items) {

                JsonNode child =
                        index.get(
                                itemId.asText()
                        );

                collectLoopOwnedIdsRecursive(
                        child,
                        index,
                        result
                );
            }
        }
    }


    // =========================================================
    // STRUCTURE HELPERS
    // =========================================================

    private List<String> getControlFlowFolderIds(
            JsonNode ifNode) {

        List<String> result =
                new ArrayList<>();


        for (JsonNode node :
                ifNode.path("Assocs")
                        .path("ControlFlowFolders")) {

            result.add(
                    node.asText()
            );
        }


        return result;
    }


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


    private static boolean hasControlFlowAncestor(
            JsonNode node,
            Map<String, JsonNode> index) {

        if (node == null) {
            return false;
        }

        String parentId =
                node.path("Assocs")
                        .path("ParentFolder")
                        .path(0)
                        .asText(null);

        Set<String> visited =
                new HashSet<>();

        while (parentId != null
                && !parentId.isBlank()
                && visited.add(parentId)) {

            JsonNode parent =
                    index.get(parentId);

            if (parent == null) {
                return false;
            }

            String objectClass =
                    parent.path("ObjectClass")
                            .asText("");

            if ("TestCaseControlFlowFolder".equals(
                    objectClass)) {

                return true;
            }

            parentId =
                    parent.path("Assocs")
                            .path("ParentFolder")
                            .path(0)
                            .asText(null);
        }

        return false;
    }


    private static boolean isDisabled(
            JsonNode node) {

        if (node == null) {
            return false;
        }

        return !node.path("Attributes")
                .path("DisabledDescription")
                .asText("")
                .isBlank();
    }


    private static String getNodeId(
            JsonNode node) {

        if (node == null) {
            return null;
        }


        String id =
                node.path("Surrogate")
                        .asText("");


        if (id.isBlank()) {

            id =
                    node.path("surrogate")
                            .asText("");
        }


        return id;
    }


    // =========================================================
    // STRING / CONDITION HELPERS
    // =========================================================

    private void addUniqueCondition(
            List<String> conditions,
            String condition) {

        String normalized =
                normalizeCondition(
                        condition
                );


        if (normalized.isEmpty()) {
            return;
        }


        for (String existing :
                conditions) {

            if (equivalent(
                    existing,
                    normalized)) {

                return;
            }
        }


        conditions.add(
                normalized
        );
    }


    private String joinConditions(
            List<String> conditions) {

        if (conditions == null
                || conditions.isEmpty()) {

            return "";
        }


        String result =
                "";


        for (String condition :
                conditions) {

            result =
                    merge(
                            result,
                            condition
                    );
        }


        return result;
    }


    private boolean containsConjunct(
            String expression,
            String candidate) {

        String normalizedExpression =
                removeRedundantOuterParentheses(
                        normalizeWhitespace(
                                expression
                        )
                );


        String normalizedCandidate =
                removeRedundantOuterParentheses(
                        normalizeWhitespace(
                                candidate
                        )
                );


        if (normalizedExpression.equals(
                normalizedCandidate)) {

            return true;
        }


        for (String part :
                splitTopLevelAnd(
                        normalizedExpression
                )) {

            if (removeRedundantOuterParentheses(
                    normalizeWhitespace(
                            part
                    ))
                    .equals(
                            normalizedCandidate
                    )) {

                return true;
            }
        }


        return false;
    }


    private List<String> splitTopLevelAnd(
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

        String upper =
                expression.toUpperCase(
                        java.util.Locale.ROOT
                );


        for (int i = 0;
             i < expression.length();
             i++) {

            char c =
                    expression.charAt(i);


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


    private String wrapForAnd(
            String condition) {

        String c =
                normalizeCondition(
                        condition
                );


        if (c.isEmpty()) {
            return "";
        }


        if (isFullyWrapped(
                c)) {

            return c;
        }


        return "("
                + c
                + ")";
    }


    private boolean equivalent(
            String a,
            String b) {

        return removeRedundantOuterParentheses(
                normalizeWhitespace(
                        a
                ))
                .equals(
                        removeRedundantOuterParentheses(
                                normalizeWhitespace(
                                        b
                                )
                        )
                );
    }


    private String normalizeCondition(
            String value) {

        if (value == null) {
            return "";
        }


        String result =
                normalizeWhitespace(
                        value.trim()
                );


        return removeRedundantOuterParentheses(
                result
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
                value.length() - 1
        ) != ')') {

            return false;
        }


        int depth =
                0;


        for (int i = 0;
             i < value.length();
             i++) {

            char c =
                    value.charAt(i);


            if (c == '(') {

                depth++;

            } else if (c == ')') {

                depth--;


                if (depth == 0
                        && i < value.length()
                        - 1) {

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

        return isBlank(
                value)
                || "true".equalsIgnoreCase(
                value.trim()
        );
    }


    private boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }


    private boolean containsStandaloneGreaterThan(
            String value) {

        for (int i = 0;
             i < value.length();
             i++) {

            if (value.charAt(i) == '>') {

                if (i == 0
                        || value.charAt(
                        i - 1
                ) != '=') {

                    return true;
                }
            }
        }


        return false;
    }


    private boolean containsStandaloneLessThan(
            String value) {

        for (int i = 0;
             i < value.length();
             i++) {

            if (value.charAt(i) == '<') {

                if (i == 0
                        || value.charAt(
                        i - 1
                ) != '=') {

                    return true;
                }
            }
        }


        return false;
    }


    private boolean containsWordIgnoreCase(
            String value,
            String word) {

        String upper =
                " "
                        + value.toUpperCase(
                        java.util.Locale.ROOT
                )
                        + " ";


        return upper.contains(
                " "
                        + word.toUpperCase(
                        java.util.Locale.ROOT
                )
                        + " "
        );
    }


    private boolean startsWithIgnoreCase(
            String value,
            String prefix) {

        return value != null
                && prefix != null
                && value.length()
                >= prefix.length()
                && value.regionMatches(
                true,
                0,
                prefix,
                0,
                prefix.length()
        );
    }


    private String escapeSingleQuotes(
            String value) {

        return value == null
                ? ""
                : value.replace(
                        "'",
                        "''"
                );
    }


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