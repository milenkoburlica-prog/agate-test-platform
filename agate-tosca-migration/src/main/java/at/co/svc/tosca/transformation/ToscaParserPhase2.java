package at.co.svc.tosca.transformation;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class ToscaParserPhase2 {

    // =====================================================
    // MAIN
    // =====================================================

    public static void main(
            String[] args) throws Exception {

        String input =
                "C:\\work\\projects\\agate-studio\\agate-tosca-migration-svc\\jsonOut\\test_agate-step1.json";

        processFile(
                input,
                null
        );
    }


    // =====================================================
    // PROCESS FILE
    // =====================================================

    public static String processFile(
            String inputFile,
            String tsuFile) throws Exception {

        ObjectMapper mapper =
                new ObjectMapper();

        JsonNode root =
                mapper.readTree(
                        new File(inputFile)
                );


        ObjectNode output =
                mapper.createObjectNode();

        ArrayNode stepsOut =
                mapper.createArrayNode();


        if (!root.isArray()
                || root.isEmpty()) {

            throw new IllegalArgumentException(
                    "Phase2 input must be a non-empty JSON array: "
                            + inputFile
            );
        }


        JsonNode header =
                root.get(0);


        output.put(
                "name",
                extractName(
                        header
                )
        );


        output.put(
                "surrogate",
                getNodeId(
                        header
                )
        );


        /*
         * Build index for control-flow relationships.
         */
        Map<String, JsonNode> nodeIndex =
                buildIndex(
                        root
                );


        /*
         * All nodes structurally owned by LOOPs.
         *
         * They must not be emitted again as top-level steps.
         */
        Set<String> loopOwnedIds =
                collectLoopOwnedIds(
                        root,
                        nodeIndex
                );


        int index =
                1;


        for (int i = 1;
             i < root.size();
             i++) {

            JsonNode node =
                    root.get(i);

            String objectClass =
                    node.path("ObjectClass")
                            .asText("");

            String nodeId =
                    getNodeId(
                            node
                    );


            // =================================================
            // LOOP ROOT
            // =================================================

            if (ToscaLoopSupport.isLoopControlFlow(
                    node)) {

                ObjectNode loop =
                        transformLoop(
                                node,
                                nodeIndex,
                                mapper,
                                index++
                        );

                stepsOut.add(
                        loop
                );

                continue;
            }


            /*
             * Children of LOOP are emitted only within transformLoop().
             */
            if (nodeId != null
                    && loopOwnedIds.contains(nodeId)) {

                continue;
            }


            // =================================================
            // NORMAL OLD STEP
            // =================================================

            if (!"XTestStep".equals(
                    objectClass)
                    && !"TestStepFolderReference".equals(
                    objectClass)) {

                continue;
            }


            ObjectNode step =
                    transformStep(
                            node,
                            mapper,
                            index++
                    );


            stepsOut.add(
                    step
            );
        }


        output.set(
                "steps",
                stepsOut
        );


        String out =
                inputFile.replace(
                        "_agate-step1.json",
                        "_agate-step2.json"
                );


        if (out.equals(
                inputFile)) {

            out =
                    inputFile.replace(
                            ".json",
                            "_agate-step2.json"
                    );
        }


        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File(out),
                        output
                );


        MigrationLog.success(
                "Tosca parser phase 2 output: "
                        + out
        );


        return out;
    }


    // =====================================================
    // LOOP
    // =====================================================

    private static ObjectNode transformLoop(
            JsonNode loopNode,
            Map<String, JsonNode> nodeIndex,
            ObjectMapper mapper,
            int index) {

        LoopMode loopMode =
                ToscaLoopSupport.getLoopMode(
                        loopNode
                );


        if (loopMode == null) {

            throw new IllegalArgumentException(
                    "Unsupported LOOP control flow: "
                            + getNodeId(loopNode)
            );
        }


        ObjectNode out =
                mapper.createObjectNode();


        out.put(
                "index",
                index
        );


        out.put(
                "name",
                getName(
                        loopNode
                )
        );


        out.put(
                "type",
                "LOOP"
        );


        out.putNull(
                "op"
        );


        out.put(
                "surrogate",
                getNodeId(
                        loopNode
                )
        );


        out.putNull(
                "module"
        );

        out.putNull(
                "moduleSurrogate"
        );

        out.putNull(
                "moduleClass"
        );

        out.putNull(
                "reusableName"
        );

        out.putNull(
                "reusableSurrogate"
        );

        out.putNull(
                "condition"
        );

        out.putNull(
                "action"
        );


        out.put(
                "maxIterations",
                ToscaLoopSupport.getMaximumRepetitions(
                        loopNode,
                        1
                )
        );


        /*
         * Kept in intermediate JSON for diagnostics.
         *
         * CleanStep can ignore it.
         */
        out.put(
                "loopMode",
                loopMode.name()
        );


        ArrayNode nestedSteps =
                mapper.createArrayNode();


        JsonNode folderIds =
                loopNode.path("Assocs")
                        .path("ControlFlowFolders");


        /*
         * =====================================================
         * WHILE / DO
         * =====================================================
         *
         * condition
         * BREAK inverse(condition)
         * body
         */
        if (loopMode == LoopMode.WHILE_DO) {

            appendConditionFolders(
                    folderIds,
                    nodeIndex,
                    nestedSteps,
                    mapper
            );


            appendBodyFolders(
                    folderIds,
                    nodeIndex,
                    nestedSteps,
                    mapper
            );

        /*
         * =====================================================
         * DO / WHILE
         * =====================================================
         *
         * body
         * condition
         * BREAK inverse(condition)
         */
        } else {

            appendBodyFolders(
                    folderIds,
                    nodeIndex,
                    nestedSteps,
                    mapper
            );


            appendConditionFolders(
                    folderIds,
                    nodeIndex,
                    nestedSteps,
                    mapper
            );
        }


        out.set(
                "steps",
                nestedSteps
        );


        return out;
    }


    // =====================================================
    // CONDITION FOLDERS
    // =====================================================

    private static void appendConditionFolders(
            JsonNode folderIds,
            Map<String, JsonNode> nodeIndex,
            ArrayNode nestedSteps,
            ObjectMapper mapper) {

        if (!folderIds.isArray()) {
            return;
        }


        for (JsonNode folderIdNode :
                folderIds) {

            JsonNode folder =
                    nodeIndex.get(
                            folderIdNode.asText()
                    );


            if (!ToscaLoopSupport.isConditionFolder(
                    folder)) {

                continue;
            }


            appendLoopConditionFolder(
                    folder,
                    nodeIndex,
                    nestedSteps,
                    mapper
            );
        }
    }


    // =====================================================
    // BODY FOLDERS
    // =====================================================

    private static void appendBodyFolders(
            JsonNode folderIds,
            Map<String, JsonNode> nodeIndex,
            ArrayNode nestedSteps,
            ObjectMapper mapper) {

        if (!folderIds.isArray()) {
            return;
        }


        for (JsonNode folderIdNode :
                folderIds) {

            JsonNode folder =
                    nodeIndex.get(
                            folderIdNode.asText()
                    );


            if (!ToscaLoopSupport.isBodyFolder(
                    folder)) {

                continue;
            }


            appendNormalFolderSteps(
                    folder,
                    nodeIndex,
                    nestedSteps,
                    mapper
            );
        }
    }


    // =====================================================
    // LOOP CONDITION
    // =====================================================

    private static void appendLoopConditionFolder(
            JsonNode folder,
            Map<String, JsonNode> nodeIndex,
            ArrayNode nestedSteps,
            ObjectMapper mapper) {

        JsonNode itemIds =
                folder.path("Assocs")
                        .path("Items");


        if (!itemIds.isArray()) {
            return;
        }


        for (JsonNode itemId :
                itemIds) {

            JsonNode item =
                    nodeIndex.get(
                            itemId.asText()
                    );


            if (item == null) {
                continue;
            }


            String objectClass =
                    item.path("ObjectClass")
                            .asText("");


            /*
             * Nested LOOP inside condition is unusual, but preserve it
             * structurally rather than flattening it.
             */
            if (ToscaLoopSupport.isLoopControlFlow(
                    item)) {

                nestedSteps.add(
                        transformLoop(
                                item,
                                nodeIndex,
                                mapper,
                                nestedSteps.size() + 1
                        )
                );

                continue;
            }


            if (!"XTestStep".equals(
                    objectClass)) {

                continue;
            }


            ObjectNode transformed =
                    transformStep(
                            item,
                            mapper,
                            nestedSteps.size() + 1
                    );


            BreakDefinition breakDefinition =
                    createBreakDefinition(
                            transformed
                    );


            if (breakDefinition == null) {

                /*
                 * We were not able to interpret the Verify.
                 *
                 * Do not silently delete the original step.
                 */
                MigrationLog.info(
                        "[Review] LOOP condition could not be converted to BREAK"
                                + " | step="
                                + transformed.path("name").asText("")
                );

                nestedSteps.add(
                        transformed
                );

                continue;
            }


            /*
             * Verify belongs to LOOP control-flow semantics.
             *
             * It must NOT later become SQL ASSERT.
             */
            removeValue(
                    transformed,
                    breakDefinition.valueNode()
            );


            /*
             * The condition step itself still needs to execute,
             * e.g. SQL SELECT.
             */
            nestedSteps.add(
                    transformed
            );


            ObjectNode breakStep =
                    mapper.createObjectNode();


            breakStep.put(
                    "index",
                    nestedSteps.size() + 1
            );


            breakStep.put(
                    "name",
                    "Break "
                            + transformed.path("name")
                            .asText("")
            );


            breakStep.put(
                    "type",
                    "BREAK"
            );


            breakStep.putNull(
                    "op"
            );

            breakStep.put(
                    "surrogate",
                    getNodeId(item)
                            + "_BREAK"
            );

            breakStep.putNull(
                    "module"
            );

            breakStep.putNull(
                    "moduleSurrogate"
            );

            breakStep.putNull(
                    "moduleClass"
            );

            breakStep.putNull(
                    "reusableName"
            );

            breakStep.putNull(
                    "reusableSurrogate"
            );


            breakStep.put(
                    "condition",
                    breakDefinition.breakCondition()
            );


            breakStep.putNull(
                    "action"
            );


            breakStep.set(
                    "values",
                    mapper.createArrayNode()
            );


            nestedSteps.add(
                    breakStep
            );
        }
    }


    // =====================================================
    // NORMAL LOOP BODY
    // =====================================================

    private static void appendNormalFolderSteps(
            JsonNode folder,
            Map<String, JsonNode> nodeIndex,
            ArrayNode nestedSteps,
            ObjectMapper mapper) {

        JsonNode itemIds =
                folder.path("Assocs")
                        .path("Items");


        if (!itemIds.isArray()) {
            return;
        }


        for (JsonNode itemId :
                itemIds) {

            JsonNode item =
                    nodeIndex.get(
                            itemId.asText()
                    );


            if (item == null) {
                continue;
            }


            String objectClass =
                    item.path("ObjectClass")
                            .asText("");


            if (ToscaLoopSupport.isLoopControlFlow(
                    item)) {

                nestedSteps.add(
                        transformLoop(
                                item,
                                nodeIndex,
                                mapper,
                                nestedSteps.size() + 1
                        )
                );

                continue;
            }


            if ("XTestStep".equals(
                    objectClass)
                    || "TestStepFolderReference".equals(
                    objectClass)) {

                nestedSteps.add(
                        transformStep(
                                item,
                                mapper,
                                nestedSteps.size() + 1
                        )
                );

                continue;
            }


            /*
             * Traverse structural folders.
             */
            if ("TestStepFolder".equals(
                    objectClass)
                    || "TestCaseControlFlowFolder".equals(
                    objectClass)) {

                appendNormalFolderSteps(
                        item,
                        nodeIndex,
                        nestedSteps,
                        mapper
                );
            }
        }
    }


    // =====================================================
    // BREAK DEFINITION
    // =====================================================

    private static BreakDefinition createBreakDefinition(
            ObjectNode transformedStep) {

        JsonNode values =
                transformedStep.path(
                        "values"
                );


        if (!values.isArray()) {
            return null;
        }


        String responseName =
                buildSqlResponseName(
                        transformedStep.path("name")
                                .asText("")
                );


        String sqlStatement =
                findSqlStatement(
                        values
                );


        for (JsonNode value :
                values) {

            String actionMode =
                    value.path("actionMode")
                            .asText("");


            if (!"Verify".equalsIgnoreCase(
                    actionMode)) {

                continue;
            }


            String expected =
                    value.path("value")
                            .asText("");


            String operator =
                    value.path("operator")
                            .asText("");


            String actionProperty =
                    value.path("actionProperty")
                            .asText("");


            // =================================================
            // ROW COUNT
            // =================================================

            if ("RowCount".equalsIgnoreCase(
                    actionProperty)) {

                String condition =
                        buildRowCountBreakCondition(
                                responseName,
                                expected,
                                operator
                        );


                if (condition != null) {

                    return new BreakDefinition(
                            value,
                            condition
                    );
                }


                continue;
            }


            // =================================================
            // CELL VALUE
            // =================================================

            String toscaPath =
                    value.path("toscaPath")
                            .asText("");


            CellPosition cell =
                    parseCellPosition(
                            toscaPath
                    );


            if (cell == null) {
                continue;
            }


            String condition =
                    buildCellBreakCondition(
                            responseName,
                            cell,
                            expected,
                            operator,
                            sqlStatement
                    );


            if (condition != null) {

                return new BreakDefinition(
                        value,
                        condition
                );
            }
        }


        return null;
    }


    // =====================================================
    // ROW COUNT BREAK
    // =====================================================

    private static String buildRowCountBreakCondition(
            String responseName,
            String expected,
            String operator) {

        /*
         * NOTE:
         *
         * This placeholder must be supported by AGATE runtime.
         *
         * It represents the number of rows in the SQL response.
         */
        String actual =
                "{SQL_ROW_COUNT["
                        + responseName
                        + "]}";


        /*
         * LOOP continues while Tosca Verify is TRUE.
         *
         * BREAK therefore uses the INVERSE.
         */
        if (isEqualsOperator(
                operator)) {

            return actual
                    + " != "
                    + formatConditionValue(
                    expected
            );
        }


        if (isNotEqualsOperator(
                operator)) {

            return actual
                    + " == "
                    + formatConditionValue(
                    expected
            );
        }


        return null;
    }


    // =====================================================
    // CELL BREAK
    // =====================================================

    private static String buildCellBreakCondition(
            String responseName,
            CellPosition cell,
            String expected,
            String operator,
            String sqlStatement) {

        String actual =
                "{SQL["
                        + responseName
                        + "]["
                        + cell.row()
                        + "]["
                        + cell.column()
                        + "]}";


        /*
         * Nice readable special case for COUNT(*):
         *
         * while COUNT == 0
         *
         * becomes:
         *
         * BREAK COUNT > 0
         */
        if (isEqualsOperator(operator)
                && "0".equals(expected)
                && isSelectCount(sqlStatement)) {

            return actual
                    + " > 0";
        }


        if (isEqualsOperator(
                operator)) {

            return actual
                    + " != "
                    + formatConditionValue(
                    expected
            );
        }


        if (isNotEqualsOperator(
                operator)) {

            return actual
                    + " == "
                    + formatConditionValue(
                    expected
            );
        }


        return null;
    }


    // =====================================================
    // NORMAL STEP TRANSFORM
    // =====================================================

    private static ObjectNode transformStep(
            JsonNode node,
            ObjectMapper mapper,
            int index) {

        ObjectNode out =
                mapper.createObjectNode();


        String objectClass =
                node.path("ObjectClass")
                        .asText("");


        out.put(
                "index",
                index
        );


        out.put(
                "name",
                getName(
                        node
                )
        );


        out.putNull(
                "type"
        );

        out.putNull(
                "op"
        );


        out.put(
                "surrogate",
                getNodeId(
                        node
                )
        );


        putNullable(
                out,
                "module",
                textField(
                        node,
                        "module"
                )
        );


        putNullable(
                out,
                "moduleSurrogate",
                textField(
                        node,
                        "moduleSurrogate"
                )
        );


        putNullable(
                out,
                "moduleClass",
                textField(
                        node,
                        "moduleClass"
                )
        );


        // =====================================================
        // REUSABLE
        // =====================================================

        if ("TestStepFolderReference".equals(
                objectClass)) {

            out.put(
                    "type",
                    "Reusable"
            );


            putNullable(
                    out,
                    "reusableName",
                    textField(
                            node,
                            "reusableName"
                    )
            );


            putNullable(
                    out,
                    "reusableSurrogate",
                    textField(
                            node,
                            "reusableSurrogate"
                    )
            );

        } else {

            out.putNull(
                    "reusableName"
            );

            out.putNull(
                    "reusableSurrogate"
            );
        }


        putNullable(
                out,
                "condition",
                textField(
                        node,
                        "condition"
                )
        );


        putNullable(
                out,
                "action",
                textField(
                        node,
                        "op"
                )
        );


        // =====================================================
        // VALUES
        // =====================================================

        ArrayNode valuesOut =
                mapper.createArrayNode();


        JsonNode values =
                node.path(
                        "values"
                );


        if (values.isArray()) {

            for (JsonNode entry :
                    values) {

                valuesOut.add(
                        transformValue(
                                entry,
                                entry.path("name")
                                        .asText(""),
                                mapper
                        )
                );
            }

        } else if (values.isObject()) {

            Iterator<Map.Entry<String, JsonNode>> fields =
                    values.fields();


            while (fields.hasNext()) {

                Map.Entry<String, JsonNode> field =
                        fields.next();


                valuesOut.add(
                        transformValue(
                                field.getValue(),
                                field.getKey(),
                                mapper
                        )
                );
            }
        }


        /*
         * Some Phase1 variants still expose Parameters rather than values.
         */
        if (valuesOut.isEmpty()) {

            JsonNode parameters =
                    node.path(
                            "Parameters"
                    );


            if (parameters.isArray()) {

                for (JsonNode parameter :
                        parameters) {

                    ObjectNode v =
                            mapper.createObjectNode();


                    String name =
                            parameter.path("name")
                                    .asText("");


                    if (name.isBlank()) {

                        name =
                                parameter.path("ExplicitName")
                                        .asText("");
                    }


                    v.put(
                            "name",
                            name
                    );


                    String value =
                            parameter.path("value")
                                    .asText("");


                    if (value.isBlank()
                            && parameter.has("Value")) {

                        value =
                                parameter.path("Value")
                                        .asText("");
                    }


                    v.put(
                            "value",
                            value
                    );


                    String actionMode =
                            parameter.path("actionMode")
                                    .asText("");


                    if (actionMode.isBlank()) {

                        actionMode =
                                parameter.path("ActionMode")
                                        .asText("");
                    }


                    v.put(
                            "actionMode",
                            actionMode
                    );


                    copyOptionalAnyCase(
                            parameter,
                            v,
                            "actionProperty",
                            "ActionProperty"
                    );

                    copyOptionalAnyCase(
                            parameter,
                            v,
                            "operator",
                            "Operator"
                    );

                    copyOptionalAnyCase(
                            parameter,
                            v,
                            "toscaPath",
                            "ToscaPath"
                    );

                    copyOptionalAnyCase(
                            parameter,
                            v,
                            "toscaPathID",
                            "ToscaPathID"
                    );

                    copyOptionalAnyCase(
                            parameter,
                            v,
                            "xmlPath",
                            "XmlPath"
                    );

                    copyOptionalAnyCase(
                            parameter,
                            v,
                            "jsonPath",
                            "JsonPath"
                    );

                    copyOptionalAnyCase(
                            parameter,
                            v,
                            "xCondition",
                            "XCondition"
                    );


                    valuesOut.add(
                            v
                    );
                }
            }
        }


        out.set(
                "values",
                valuesOut
        );


        return out;
    }


    private static ObjectNode transformValue(
            JsonNode entry,
            String defaultName,
            ObjectMapper mapper) {

        ObjectNode v =
                mapper.createObjectNode();


        String name =
                entry.path("name")
                        .asText("");


        if (name.isBlank()) {
            name =
                    defaultName;
        }


        v.put(
                "name",
                name
        );


        v.put(
                "value",
                entry.path("value")
                        .asText("")
        );


        v.put(
                "actionMode",
                entry.path("actionMode")
                        .asText("")
        );


        copyOptionalAnyCase(
                entry,
                v,
                "actionProperty",
                "ActionProperty"
        );


        copyOptionalAnyCase(
                entry,
                v,
                "operator",
                "Operator"
        );


        copyOptionalAnyCase(
                entry,
                v,
                "toscaPath",
                "ToscaPath"
        );


        copyOptionalAnyCase(
                entry,
                v,
                "toscaPathID",
                "ToscaPathID"
        );


        copyOptionalAnyCase(
                entry,
                v,
                "xmlPath",
                "XmlPath"
        );


        copyOptionalAnyCase(
                entry,
                v,
                "jsonPath",
                "JsonPath"
        );


        copyOptionalAnyCase(
                entry,
                v,
                "xCondition",
                "XCondition"
        );


        return v;
    }


    // =====================================================
    // REMOVE VERIFY VALUE
    // =====================================================

    private static void removeValue(
            ObjectNode step,
            JsonNode valueToRemove) {

        JsonNode values =
                step.path(
                        "values"
                );


        if (!(values instanceof ArrayNode array)) {
            return;
        }


        for (int i = 0;
             i < array.size();
             i++) {

            JsonNode current =
                    array.get(i);


            if (current == valueToRemove
                    || current.equals(valueToRemove)) {

                array.remove(
                        i
                );

                return;
            }
        }
    }


    // =====================================================
    // LOOP OWNERSHIP
    // =====================================================

    private static Set<String> collectLoopOwnedIds(
            JsonNode root,
            Map<String, JsonNode> nodeIndex) {

        Set<String> result =
                new HashSet<>();


        for (JsonNode node :
                root) {

            if (!ToscaLoopSupport.isLoopControlFlow(
                    node)) {

                continue;
            }


            collectLoopChildren(
                    node,
                    nodeIndex,
                    result,
                    false
            );
        }


        return result;
    }


    private static void collectLoopChildren(
            JsonNode node,
            Map<String, JsonNode> index,
            Set<String> result,
            boolean includeSelf) {

        if (node == null) {
            return;
        }


        if (includeSelf) {

            String id =
                    getNodeId(
                            node
                    );


            if (id != null
                    && !id.isBlank()) {

                if (!result.add(
                        id)) {

                    return;
                }
            }
        }


        JsonNode folders =
                node.path("Assocs")
                        .path("ControlFlowFolders");


        if (folders.isArray()) {

            for (JsonNode folderId :
                    folders) {

                JsonNode folder =
                        index.get(
                                folderId.asText()
                        );


                if (folder == null) {
                    continue;
                }


                collectLoopChildren(
                        folder,
                        index,
                        result,
                        true
                );
            }
        }


        JsonNode items =
                node.path("Assocs")
                        .path("Items");


        if (items.isArray()) {

            for (JsonNode itemId :
                    items) {

                JsonNode item =
                        index.get(
                                itemId.asText()
                        );


                if (item == null) {
                    continue;
                }


                collectLoopChildren(
                        item,
                        index,
                        result,
                        true
                );
            }
        }
    }


    // =====================================================
    // HELPERS
    // =====================================================

    private static Map<String, JsonNode> buildIndex(
            JsonNode root) {

        Map<String, JsonNode> result =
                new HashMap<>();


        for (JsonNode node :
                root) {

            String id =
                    getNodeId(
                            node
                    );


            if (id != null
                    && !id.isBlank()) {

                result.put(
                        id,
                        node
                );
            }
        }


        return result;
    }


    private static String extractName(
            JsonNode header) {

        String name =
                getName(
                        header
                );


        return name == null
                ? ""
                : name;
    }


    private static String getName(
            JsonNode node) {

        if (node == null) {
            return "";
        }


        String name =
                node.path("name")
                        .asText("");


        if (name.isBlank()) {

            name =
                    node.path("Name")
                            .asText("");
        }


        if (name.isBlank()) {

            name =
                    node.path("Attributes")
                            .path("Name")
                            .asText("");
        }


        return name;
    }


    private static String getNodeId(
            JsonNode node) {

        if (node == null) {
            return "";
        }


        String id =
                node.path("surrogate")
                        .asText("");


        if (id.isBlank()) {

            id =
                    node.path("Surrogate")
                            .asText("");
        }


        return id;
    }


    private static String textField(
            JsonNode node,
            String field) {

        if (node == null
                || !node.has(field)
                || node.get(field).isNull()) {

            return null;
        }


        String value =
                node.path(field)
                        .asText(null);


        if (value != null
                && value.isBlank()) {

            return null;
        }


        return value;
    }


    private static void putNullable(
            ObjectNode target,
            String field,
            String value) {

        if (value == null) {

            target.putNull(
                    field
            );

        } else {

            target.put(
                    field,
                    value
            );
        }
    }


    private static void copyOptionalAnyCase(
            JsonNode source,
            ObjectNode target,
            String lowerName,
            String upperName) {

        JsonNode value =
                source.get(
                        lowerName
                );


        if (value == null
                || value.isNull()) {

            value =
                    source.get(
                            upperName
                    );
        }


        if (value == null
                || value.isNull()) {

            return;
        }


        String text =
                value.asText("");


        if (!text.isBlank()) {

            target.put(
                    lowerName,
                    text
            );
        }
    }


    private static String findSqlStatement(
            JsonNode values) {

        for (JsonNode value :
                values) {

            String name =
                    value.path("name")
                            .asText("");

            String path =
                    value.path("toscaPath")
                            .asText("");


            if ("SQL Statement".equalsIgnoreCase(
                    name)
                    || "SQL Statement".equalsIgnoreCase(
                    path)) {

                return value.path("value")
                        .asText("");
            }
        }


        return "";
    }


    private static boolean isSelectCount(
            String sql) {

        if (sql == null) {
            return false;
        }


        String normalized =
                sql.replaceAll(
                        "\\s+",
                        " "
                )
                        .trim()
                        .toUpperCase(
                                java.util.Locale.ROOT
                        );


        return normalized.startsWith(
                "SELECT COUNT("
        );
    }


    private static CellPosition parseCellPosition(
            String toscaPath) {

        if (toscaPath == null
                || toscaPath.isBlank()) {

            return null;
        }


        java.util.regex.Matcher matcher =
                java.util.regex.Pattern
                        .compile(
                                "#(\\d+).*#(\\d+)"
                        )
                        .matcher(
                                toscaPath
                        );


        if (!matcher.find()) {
            return null;
        }


        int toscaRow =
                Integer.parseInt(
                        matcher.group(1)
                );

        int toscaColumn =
                Integer.parseInt(
                        matcher.group(2)
                );


        /*
         * Tosca result table:
         *
         * #2 -> first data row
         * #1 -> first column
         */
        int row =
                Math.max(
                        0,
                        toscaRow - 2
                );


        int column =
                Math.max(
                        0,
                        toscaColumn - 1
                );


        return new CellPosition(
                row,
                column
        );
    }


    private static String buildSqlResponseName(
            String stepName) {

        if (stepName == null
                || stepName.isBlank()) {

            return "sql_res";
        }


        /*
         * Must stay synchronized with ProcessDbStep.
         */
        return stepName
                .toLowerCase()
                .replace(
                        " ",
                        "_"
                )
                + "_res";
    }


    private static boolean isEqualsOperator(
            String operator) {

        return "1".equals(
                operator)
                || "EQUALS".equalsIgnoreCase(
                operator);
    }


    private static boolean isNotEqualsOperator(
            String operator) {

        return "2".equals(
                operator)
                || "NOT_EQUALS".equalsIgnoreCase(
                operator);
    }


    private static String formatConditionValue(
            String value) {

        if (value == null) {
            return "''";
        }


        String trimmed =
                value.trim();


        if (trimmed.matches(
                "-?\\d+(\\.\\d+)?")) {

            return trimmed;
        }


        if ("true".equalsIgnoreCase(
                trimmed)
                || "false".equalsIgnoreCase(
                trimmed)) {

            return trimmed.toLowerCase(
                    java.util.Locale.ROOT
            );
        }


        return "'"
                + trimmed.replace(
                "'",
                "''"
        )
                + "'";
    }


    // =====================================================
    // RECORDS
    // =====================================================

    private record BreakDefinition(
            JsonNode valueNode,
            String breakCondition) {
    }


    private record CellPosition(
            int row,
            int column) {
    }
}