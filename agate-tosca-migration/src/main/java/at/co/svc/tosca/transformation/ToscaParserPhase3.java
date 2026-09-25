package at.co.svc.tosca.transformation;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class ToscaParserPhase3 {

    // =========================================================
    // PROCESS FILE
    // =========================================================

    public static String processFile(
            String inputFile) throws Exception {

        ObjectMapper mapper =
                new ObjectMapper();

        JsonNode root =
                mapper.readTree(
                        new File(inputFile)
                );

        ObjectNode output =
                mapper.createObjectNode();

        output.put(
                "name",
                root.path("name")
                        .asText()
        );

        output.put(
                "surrogate",
                root.path("surrogate")
                        .asText()
        );

        ArrayNode stepsOut =
                mapper.createArrayNode();

        JsonNode steps =
                root.path("steps");

        if (steps.isArray()) {

            for (JsonNode stepNode :
                    steps) {

                stepsOut.add(
                        transformStepPhase3(
                                stepNode,
                                mapper
                        )
                );
            }
        }

        output.set(
                "steps",
                stepsOut
        );

        String outFile =
                inputFile.replace(
                        "_agate-step2.json",
                        "_agate-step3.json"
                );

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File(outFile),
                        output
                );

        MigrationLog.success(
                "Tosca parser phase 3 output: "
                        + outFile
        );

        return outFile;
    }


    // =========================================================
    // STEP TRANSFORMATION
    // =========================================================

    public static ObjectNode transformStepPhase3(
            JsonNode stepNode,
            ObjectMapper mapper) {

        ObjectNode newStep =
                stepNode.deepCopy();


        // =====================================================
        // 1. STRUCTURED / NESTED STEPS
        // =====================================================
        //
        // Example:
        //
        // LOOP
        //   steps:
        //     SQL
        //     BREAK
        //     WAIT
        //
        // Old Phase3 only supported flat steps.
        //
        // Nested steps are now processed recursively.
        // =====================================================

        JsonNode nestedSteps =
                stepNode.get("steps");

        if (nestedSteps != null
                && nestedSteps.isArray()) {

            ArrayNode transformedNestedSteps =
                    mapper.createArrayNode();

            for (JsonNode nestedStep :
                    nestedSteps) {

                transformedNestedSteps.add(
                        transformStepPhase3(
                                nestedStep,
                                mapper
                        )
                );
            }

            newStep.set(
                    "steps",
                    transformedNestedSteps
            );
        }


        // =====================================================
        // 2. CHECK VALUES
        // =====================================================
        //
        // LOOP and BREAK do not necessarily have:
        //
        //   values: [...]
        //
        // In that case Phase3 has nothing more to transform.
        //
        // IMPORTANT:
        // Normal legacy steps continue below and use exactly
        // the existing values transformation.
        // =====================================================

        JsonNode valuesNode =
                stepNode.get("values");

        if (valuesNode == null
                || !valuesNode.isArray()) {

            return newStep;
        }

        ArrayNode originalValues =
                (ArrayNode) valuesNode;


        // =====================================================
        // 3. BUILD INDEXES FOR LIST-LIKE LEVELS
        // =====================================================

        Map<String, Map<String, Integer>> parentContexts =
                new HashMap<>();

        for (JsonNode val :
                originalValues) {

            String path =
                    val.path("toscaPath")
                            .asText("");

            String pathId =
                    val.path("toscaPathID")
                            .asText("");

            String[] pParts =
                    path.split("\\.");

            String[] idParts =
                    pathId.split("\\.");

            for (int i = 0;
                 i < pParts.length;
                 i++) {

                String fieldName =
                        pParts[i]
                                .replaceAll(
                                        "\\[\\d+\\]",
                                        ""
                                );

                /*
                 * Index only list-like levels.
                 */
                if (fieldName.equals("quittung")
                        || fieldName.equals("meldungsdaten")) {

                    String parentPath =
                            i == 0
                                    ? "root"
                                    : String.join(
                                            ".",
                                            java.util.Arrays.copyOf(
                                                    pParts,
                                                    i
                                            )
                                    );

                    String key =
                            parentPath
                                    + "_"
                                    + fieldName;

                    String id =
                            i < idParts.length
                                    ? idParts[i]
                                    : "default";

                    parentContexts.putIfAbsent(
                            key,
                            new HashMap<>()
                    );

                    Map<String, Integer> idMap =
                            parentContexts.get(
                                    key
                            );

                    if (!idMap.containsKey(id)) {

                        idMap.put(
                                id,
                                idMap.size()
                        );
                    }
                }
            }
        }


        // =====================================================
        // 4. BUILD INDEXED PATHS
        // =====================================================

        ArrayNode newValues =
                mapper.createArrayNode();

        ArrayNode constraints =
                mapper.createArrayNode();


        for (JsonNode val :
                originalValues) {

            String path =
                    val.path("toscaPath")
                            .asText("");

            String pathId =
                    val.path("toscaPathID")
                            .asText("");

            String actionMode =
                    val.path("actionMode")
                            .asText("");


            /*
             * Some values may not have ToscaPath.
             *
             * Preserve them unchanged instead of creating
             * invalid paths such as:
             *
             *   [0]
             */
            if (path.isBlank()) {

                ObjectNode valCopy =
                        val.deepCopy();

                newValues.add(
                        valCopy
                );

                continue;
            }


            String[] p =
                    path.split("\\.");

            String[] id =
                    pathId.split("\\.");

            StringBuilder sb =
                    new StringBuilder();


            for (int i = 0;
                 i < p.length;
                 i++) {

                String fieldName =
                        p[i].replaceAll(
                                "\\[\\d+\\]",
                                ""
                        );


                if (fieldName.equals("quittung")
                        || fieldName.equals("meldungsdaten")) {

                    String parentPath =
                            i == 0
                                    ? "root"
                                    : String.join(
                                            ".",
                                            java.util.Arrays.copyOf(
                                                    p,
                                                    i
                                            )
                                    );

                    String key =
                            parentPath
                                    + "_"
                                    + fieldName;

                    String currentId =
                            i < id.length
                                    ? id[i]
                                    : "default";

                    Map<String, Integer> idMap =
                            parentContexts.get(
                                    key
                            );

                    int idx =
                            0;

                    if (idMap != null
                            && idMap.containsKey(
                            currentId)) {

                        idx =
                                idMap.get(
                                        currentId
                                );
                    }

                    sb.append(
                            fieldName
                    )
                            .append("[")
                            .append(idx)
                            .append("]");

                } else {

                    /*
                     * Leaf elements always receive [0].
                     */
                    sb.append(
                            fieldName
                    )
                            .append("[0]");
                }


                if (i < p.length - 1) {

                    sb.append(".");
                }
            }


            String indexedPath =
                    sb.toString();


            // =================================================
            // CONSTRAINT VALUE
            // =================================================

            if ("Constraint".equals(
                    actionMode)
                    || "519".equals(
                    actionMode)) {

                ObjectNode constraint =
                        mapper.createObjectNode();

                constraint.put(
                        "path",
                        val.path("xmlPath")
                                .asText()
                );

                constraint.put(
                        "expected",
                        val.path("value")
                                .asText()
                );

                constraint.put(
                        "action",
                        "EQUALS"
                );

                int lastDot =
                        indexedPath.lastIndexOf(
                                "."
                        );

                constraint.put(
                        "toscaPath",
                        lastDot != -1
                                ? indexedPath.substring(
                                0,
                                lastDot
                        )
                                : indexedPath
                );

                constraints.add(
                        constraint
                );


                ObjectNode valCopy =
                        val.deepCopy();

                valCopy.set(
                        "constrain",
                        constraint
                );

                newValues.add(
                        valCopy
                );

            } else {

                ObjectNode valCopy =
                        val.deepCopy();

                valCopy.put(
                        "toscaPath",
                        indexedPath
                );

                newValues.add(
                        valCopy
                );
            }
        }


        newStep.set(
                "values",
                newValues
        );

        newStep.set(
                "constraints",
                constraints
        );


        // =====================================================
        // EXISTING POST PROCESSING
        // =====================================================

        enrichValuesWithConstraints(
                newStep,
                mapper
        );

        cleanPathsAndRemoveConstraints(
                newStep
        );

        applyXConditions(
                newStep
        );


        return newStep;
    }


    // =========================================================
    // APPLY X CONDITIONS
    // =========================================================

    public static void applyXConditions(
            ObjectNode stepNode) {

        String moduleName =
                stepNode.path("module")
                        .asText("");

        /*
         * Existing behavior:
         * DB Expert module does not use XCondition propagation here.
         */
        if ("TBox DB Expert module".equals(
                moduleName)) {

            return;
        }


        JsonNode valuesNode =
                stepNode.get("values");

        if (valuesNode == null
                || !valuesNode.isArray()) {

            return;
        }

        ArrayNode values =
                (ArrayNode) valuesNode;


        // =====================================================
        // 1. FIND CONSTRAINTS
        // =====================================================

        Map<String, String> conditions =
                new HashMap<>();

        for (JsonNode valNode :
                values) {

            if (!(valNode instanceof ObjectNode)) {
                continue;
            }

            ObjectNode val =
                    (ObjectNode) valNode;

            String actionMode =
                    val.path("actionMode")
                            .asText("");


            if ("Constraint".equals(
                    actionMode)
                    || "519".equals(
                    actionMode)) {

                String xmlPath =
                        val.path("xmlPath")
                                .asText("");

                String value =
                        val.path("value")
                                .asText("");

                String name =
                        val.path("name")
                                .asText("");


                MigrationLog.debug(
                        "Constraint found: name=["
                                + name
                                + "], value=["
                                + value
                                + "], xmlPath=["
                                + xmlPath
                                + "]"
                );


                if (!xmlPath.isEmpty()
                        && !value.isEmpty()) {

                    if (!value.equalsIgnoreCase(
                            "{NULL}")) {

                        String condition =
                                value
                                        + " != NULL";

                        conditions.put(
                                xmlPath,
                                condition
                        );
                    }
                }
            }
        }


        // =====================================================
        // 2. APPLY CONDITIONS
        // =====================================================

        Iterator<JsonNode> it =
                values.elements();

        while (it.hasNext()) {

            JsonNode valueNode =
                    it.next();

            if (!(valueNode instanceof ObjectNode)) {
                continue;
            }

            ObjectNode val =
                    (ObjectNode) valueNode;

            String currentXmlPath =
                    val.path("xmlPath")
                            .asText("");

            String actionMode =
                    val.path("actionMode")
                            .asText("");


            boolean isConstraint =
                    "Constraint".equals(
                            actionMode)
                            || "519".equals(
                            actionMode);


            for (Map.Entry<String, String> entry :
                    conditions.entrySet()) {

                String constraintXmlPath =
                        entry.getKey();

                if (currentXmlPath.startsWith(
                        constraintXmlPath)) {

                    if (!currentXmlPath.equals(
                            constraintXmlPath)) {

                        MigrationLog.debug(
                                "Applied xCondition: "
                                        + entry.getValue()
                        );

                        val.put(
                                "xCondition",
                                entry.getValue()
                        );
                    }
                }
            }


            /*
             * Existing behavior:
             * remove original Constraint values after propagation.
             */
            if (isConstraint) {

                it.remove();
            }
        }
    }


    // =========================================================
    // CLEAN PATHS AND REMOVE CONSTRAINTS
    // =========================================================

    public static void cleanPathsAndRemoveConstraints(
            ObjectNode stepNode) {

        JsonNode valuesNode =
                stepNode.get("values");

        if (valuesNode == null
                || !valuesNode.isArray()) {

            stepNode.remove(
                    "constraints"
            );

            return;
        }

        ArrayNode values =
                (ArrayNode) valuesNode;


        for (JsonNode valNode :
                values) {

            if (!(valNode instanceof ObjectNode)) {
                continue;
            }

            ObjectNode val =
                    (ObjectNode) valNode;


            // =================================================
            // 1. CLEAN TOSCA PATH
            // =================================================

            if (val.has(
                    "toscaPath")) {

                val.put(
                        "toscaPath",
                        val.get(
                                "toscaPath")
                                .asText()
                                .replaceAll(
                                        "\\[\\d+\\]",
                                        ""
                                )
                );
            }


            // =================================================
            // 2. CLEAN TOSCA PATH BASE
            // =================================================

            if (val.has(
                    "toscaPathBase")) {

                val.put(
                        "toscaPathBase",
                        val.get(
                                "toscaPathBase")
                                .asText()
                                .replaceAll(
                                        "\\[\\d+\\]",
                                        ""
                                )
                );
            }


            // =================================================
            // 3. CLEAN CONSTRAINT PATH
            // =================================================

            if (val.has(
                    "constrain")
                    && val.get(
                    "constrain")
                    instanceof ObjectNode) {

                ObjectNode constrain =
                        (ObjectNode) val.get(
                                "constrain"
                        );

                if (constrain.has(
                        "toscaPath")) {

                    constrain.put(
                            "toscaPath",
                            constrain.get(
                                    "toscaPath")
                                    .asText()
                                    .replaceAll(
                                            "\\[\\d+\\]",
                                            ""
                                    )
                    );
                }
            }
        }


        // =====================================================
        // 4. REMOVE CONSTRAINT ARRAY
        // =====================================================

        stepNode.remove(
                "constraints"
        );
    }


    // =========================================================
    // ENRICH VALUES WITH CONSTRAINTS
    // =========================================================

    public static void enrichValuesWithConstraints(
            ObjectNode stepNode,
            ObjectMapper mapper) {

        JsonNode valuesNode =
                stepNode.get("values");

        JsonNode constraintsNode =
                stepNode.get("constraints");


        if (valuesNode == null
                || !valuesNode.isArray()) {

            return;
        }

        if (constraintsNode == null
                || !constraintsNode.isArray()) {

            return;
        }


        ArrayNode values =
                (ArrayNode) valuesNode;

        ArrayNode constraints =
                (ArrayNode) constraintsNode;


        for (JsonNode valNode :
                values) {

            if (!(valNode instanceof ObjectNode)) {
                continue;
            }

            ObjectNode val =
                    (ObjectNode) valNode;

            String valPath =
                    val.path("toscaPath")
                            .asText();


            for (JsonNode constraintNode :
                    constraints) {

                String constraintPath =
                        constraintNode.path("toscaPath")
                                .asText();

                int lastDot =
                        constraintPath.lastIndexOf(
                                "."
                        );


                if (lastDot != -1) {

                    String parentPath =
                            constraintPath.substring(
                                    0,
                                    lastDot
                            );


                    if (valPath.startsWith(
                            parentPath)) {

                        val.put(
                                "toscaPathBase",
                                parentPath
                        );


                        if (!"{NULL}".equals(
                                constraintNode.path(
                                        "expected")
                                        .asText())) {

                            ObjectNode constrObj =
                                    val.putObject(
                                            "constrain"
                                    );

                            constrObj.put(
                                    "expected",
                                    constraintNode.path(
                                            "expected")
                                            .asText()
                            );

                            constrObj.put(
                                    "action",
                                    constraintNode.path(
                                            "action")
                                            .asText()
                            );

                            constrObj.put(
                                    "path",
                                    constraintNode.path(
                                            "path")
                                            .asText()
                            );
                        }
                    }
                }
            }
        }
    }
}