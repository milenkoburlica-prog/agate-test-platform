package at.co.svc.tosca.tsu.del;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class TCDSheetInstanceLevel1 {

    // ---------------------------------------------------------
    // LEVEL 1 - Resolve unique TestSheet ID
    // ---------------------------------------------------------

    public static String getUniqueTestSheetId(String level1FilePath) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(level1FilePath));

        Set<String> foundIds = new HashSet<>();

        if (root.isArray()) {

            for (JsonNode node : root) {

                JsonNode testSheetNode = node.at("/Assocs/TestSheet");

                if (testSheetNode.isArray()) {

                    for (JsonNode idNode : testSheetNode) {

                        if (!idNode.asText().isEmpty()) {
                            foundIds.add(idNode.asText());
                        }
                    }
                }
            }
        }

        if (foundIds.size() > 1) {
            throw new Exception(
                    "Multiple TestSheet IDs found: " + foundIds
            );
        }

        String testSheetId =
                foundIds.isEmpty()
                        ? null
                        : foundIds.iterator().next();

        MigrationLog.debug(
                "[TCD-InstanceLevel1] Resolved TestSheet ID: "
                        + testSheetId
        );

        return testSheetId;
    }


    // ---------------------------------------------------------
    // LEVEL 1 - Generate basic instance structure
    // ---------------------------------------------------------

    public static void generateInstancesJson(
            String validatedTestSheetId,
            String originalTsuJsonPath,
            String outputFilePath
    ) throws Exception {

        MigrationLog.debugSection(
                "TCD Instance Level 1 - Generate Instances"
        );

        MigrationLog.debug(
                "TestSheet ID: " + validatedTestSheetId
        );

        MigrationLog.debug(
                "Input TSU JSON: " + originalTsuJsonPath
        );

        MigrationLog.debug(
                "Output file: " + outputFilePath
        );

        ObjectMapper mapper =
                new ObjectMapper()
                        .enable(SerializationFeature.INDENT_OUTPUT);

        JsonNode root =
                mapper.readTree(
                        new File(originalTsuJsonPath)
                );

        JsonNode entities =
                root.path("Entities");

        Map<String, JsonNode> entityIndex =
                new java.util.HashMap<>();

        for (JsonNode node : entities) {

            String surrogate =
                    node.path("Surrogate").asText();

            if (!surrogate.isEmpty()) {
                entityIndex.put(
                        surrogate,
                        node
                );
            }
        }

        JsonNode targetTdInstances = null;

        for (JsonNode node : entityIndex.values()) {

            if ("TDInstances".equals(
                    node.path("ObjectClass").asText()
            )) {

                JsonNode definingItem =
                        node.at("/Assocs/DefiningItem");

                for (JsonNode item : definingItem) {

                    if (validatedTestSheetId.equals(
                            item.asText()
                    )) {

                        targetTdInstances = node;
                        break;
                    }
                }
            }

            if (targetTdInstances != null) {
                break;
            }
        }

        if (targetTdInstances == null) {
            throw new Exception(
                    "No TDInstances object found for TestSheet ID: "
                            + validatedTestSheetId
            );
        }

        ArrayNode finalInstances =
                mapper.createArrayNode();

        JsonNode items =
                targetTdInstances.at("/Assocs/Items");

        int counter = 1;

        for (JsonNode instSurrNode : items) {

            String instanceSurrogate =
                    instSurrNode.asText();

            JsonNode instanceNode =
                    entityIndex.get(instanceSurrogate);

            String instanceName =
                    (instanceNode != null)
                            ? instanceNode
                                    .path("Attributes")
                                    .path("Name")
                                    .asText("Unknown")
                            : "Unknown";

            ObjectNode instanceObj =
                    mapper.createObjectNode();

            instanceObj.put(
                    "insatanceSurrogate",
                    instanceSurrogate
            );

            instanceObj.put(
                    "TC_NO",
                    String.valueOf(counter++)
            );

            instanceObj.put(
                    "Name",
                    instanceName
            );

            instanceObj.put(
                    "testSheetSurrogate",
                    validatedTestSheetId
            );

            instanceObj.set(
                    "TDInstance",
                    mapper.createObjectNode()
            );

            finalInstances.add(
                    instanceObj
            );
        }

        mapper.writeValue(
                new File(outputFilePath),
                finalInstances
        );

        MigrationLog.success(
                "[TCD-InstanceLevel1] Instance file created successfully: "
                        + outputFilePath
        );
    }


    // ---------------------------------------------------------
    // LEVEL 2 - Populate instance values
    // ---------------------------------------------------------

    public static void populateInstanceValues(
            String inputInstancesFilePath,
            String outputFilePath,
            String originalTsuJsonPath
    ) throws Exception {

        MigrationLog.debugSection(
                "TCD Instance Level 2 - Populate Instance Values"
        );

        MigrationLog.debug(
                "Input instances file: "
                        + inputInstancesFilePath
        );

        MigrationLog.debug(
                "Input TSU JSON: "
                        + originalTsuJsonPath
        );

        MigrationLog.debug(
                "Output file: "
                        + outputFilePath
        );

        ObjectMapper mapper =
                new ObjectMapper()
                        .enable(SerializationFeature.INDENT_OUTPUT);

        ArrayNode instances =
                (ArrayNode) mapper.readTree(
                        new File(inputInstancesFilePath)
                );

        JsonNode root =
                mapper.readTree(
                        new File(originalTsuJsonPath)
                );

        Map<String, JsonNode> index =
                new java.util.HashMap<>();

        for (JsonNode n : root.path("Entities")) {

            index.put(
                    n.path("Surrogate").asText(),
                    n
            );
        }

        for (JsonNode instNode : instances) {

            String instSurrogate =
                    instNode
                            .path("insatanceSurrogate")
                            .asText();

            JsonNode tdInstance =
                    index.get(instSurrogate);

            ArrayNode parameters =
                    mapper.createArrayNode();

            if (tdInstance != null) {

                JsonNode values =
                        tdInstance.at("/Assocs/Values");

                for (JsonNode valSurrogateNode : values) {

                    String valSurrogate =
                            valSurrogateNode.asText();

                    JsonNode valObj =
                            index.get(valSurrogate);

                    if (valObj != null
                            && "TDInstanceValue".equals(
                                    valObj
                                            .path("ObjectClass")
                                            .asText()
                            )) {

                        JsonNode elementNode =
                                valObj.at("/Assocs/Element");

                        if (elementNode.isArray()
                                && elementNode.size() > 0) {

                            String attrSurrogate =
                                    elementNode
                                            .get(0)
                                            .asText();

                            JsonNode attrObj =
                                    index.get(attrSurrogate);

                            if (attrObj != null) {

                                ObjectNode param =
                                        mapper.createObjectNode();

                                param.put(
                                        "Parameter.Surrogate",
                                        attrSurrogate
                                );

                                /*
                                 * Recursively resolve the complete
                                 * hierarchical parameter name.
                                 */

                                String attrName =
                                        attrObj
                                                .path("Attributes")
                                                .path("Name")
                                                .asText();

                                JsonNode currentParentItem =
                                        attrObj.at(
                                                "/Assocs/ParentItem"
                                        );

                                while (currentParentItem.isArray()
                                        && currentParentItem.size() > 0) {

                                    String parentSurr =
                                            currentParentItem
                                                    .get(0)
                                                    .asText();

                                    JsonNode parentObj =
                                            index.get(parentSurr);

                                    if (parentObj != null) {

                                        String parentName =
                                                parentObj
                                                        .path("Attributes")
                                                        .path("Name")
                                                        .asText();

                                        if (parentName != null
                                                && !parentName
                                                        .trim()
                                                        .isEmpty()) {

                                            attrName =
                                                    parentName
                                                            + "."
                                                            + attrName;
                                        }

                                        currentParentItem =
                                                parentObj.at(
                                                        "/Assocs/ParentItem"
                                                );

                                    } else {

                                        break;
                                    }
                                }

                                param.put(
                                        "Parameter.Name",
                                        attrName
                                );

                                param.put(
                                        "Parameter.Value",
                                        valObj
                                                .path("Attributes")
                                                .path("Value")
                                                .asText()
                                );

                                parameters.add(
                                        param
                                );
                            }
                        }
                    }
                }
            }

            ((ObjectNode) instNode).set(
                    "TDInstance",
                    parameters
            );
        }

        mapper.writeValue(
                new File(outputFilePath),
                instances
        );

        MigrationLog.success(
                "[TCD-InstanceLevel2] Hierarchical parameter values generated successfully: "
                        + outputFilePath
        );
    }


    // ---------------------------------------------------------
    // LEVEL 3 - Populate sub-parameters
    // ---------------------------------------------------------

    public static void populateSubParameters(
            String inputFilePath,
            String outputFilePath,
            String originalTsuJsonPath
    ) throws Exception {

        MigrationLog.debugSection(
                "TCD Instance Level 3 - Populate Sub-Parameters"
        );

        MigrationLog.debug(
                "Input instances file: "
                        + inputFilePath
        );

        MigrationLog.debug(
                "Input TSU JSON: "
                        + originalTsuJsonPath
        );

        MigrationLog.debug(
                "Output file: "
                        + outputFilePath
        );

        ObjectMapper mapper =
                new ObjectMapper()
                        .enable(SerializationFeature.INDENT_OUTPUT);

        ArrayNode instances =
                (ArrayNode) mapper.readTree(
                        new File(inputFilePath)
                );

        JsonNode root =
                mapper.readTree(
                        new File(originalTsuJsonPath)
                );

        Map<String, JsonNode> index =
                new java.util.HashMap<>();

        for (JsonNode n : root.path("Entities")) {

            index.put(
                    n.path("Surrogate").asText(),
                    n
            );
        }

        for (JsonNode instNode : instances) {

            String instSurr =
                    instNode
                            .path("insatanceSurrogate")
                            .asText();

            JsonNode originalTdInstance =
                    index.get(instSurr);

            if (originalTdInstance == null) {
                continue;
            }

            ArrayNode parameters =
                    (ArrayNode) instNode.path(
                            "TDInstance"
                    );

            for (JsonNode param : parameters) {

                String paramSurrogate =
                        param
                                .path("Parameter.Surrogate")
                                .asText();

                ObjectNode paramObj =
                        (ObjectNode) param;

                for (JsonNode valSurr :
                        originalTdInstance
                                .path("Assocs")
                                .path("Values")) {

                    JsonNode valObj =
                            index.get(
                                    valSurr.asText()
                            );

                    if (valObj != null
                            && "TDInstanceValue".equals(
                                    valObj
                                            .path("ObjectClass")
                                            .asText()
                            )) {

                        JsonNode element =
                                valObj.at(
                                        "/Assocs/Element"
                                );

                        if (element.isArray()
                                && element.size() > 0
                                && element
                                        .get(0)
                                        .asText()
                                        .equals(paramSurrogate)) {

                            /*
                             * If the parameter references another
                             * TD instance, use the referenced
                             * instance name as parameter value.
                             */

                            if (valObj.has("Assocs")
                                    && valObj
                                            .path("Assocs")
                                            .has("ValueInstance")) {

                                String valInstanceSurr =
                                        valObj
                                                .path("Assocs")
                                                .path("ValueInstance")
                                                .get(0)
                                                .asText();

                                JsonNode targetInstance =
                                        index.get(
                                                valInstanceSurr
                                        );

                                if (targetInstance != null) {

                                    String nameValue =
                                            targetInstance
                                                    .path("Attributes")
                                                    .path("Name")
                                                    .asText();

                                    paramObj.put(
                                            "Parameter.Value",
                                            nameValue
                                    );
                                }
                            }

                            /*
                             * Resolve the values contained
                             * in the referenced instance
                             * as sub-parameters.
                             */

                            if (valObj.has("Assocs")
                                    && valObj
                                            .path("Assocs")
                                            .has("ValueInstance")) {

                                ArrayNode subParameters =
                                        mapper.createArrayNode();

                                for (JsonNode subValSurr :
                                        valObj
                                                .path("Assocs")
                                                .path("ValueInstance")) {

                                    JsonNode subInstance =
                                            index.get(
                                                    subValSurr.asText()
                                            );

                                    if (subInstance != null) {

                                        for (JsonNode subV :
                                                subInstance
                                                        .path("Assocs")
                                                        .path("Values")) {

                                            JsonNode subValObj =
                                                    index.get(
                                                            subV.asText()
                                                    );

                                            if (subValObj != null) {

                                                ObjectNode subP =
                                                        mapper.createObjectNode();

                                                subP.put(
                                                        "SubParameter.Surrogate",
                                                        subValSurr.asText()
                                                );

                                                JsonNode subElem =
                                                        subValObj.at(
                                                                "/Assocs/Element"
                                                        );

                                                if (subElem.isArray()
                                                        && subElem.size() > 0) {

                                                    JsonNode attrObj =
                                                            index.get(
                                                                    subElem
                                                                            .get(0)
                                                                            .asText()
                                                            );

                                                    if (attrObj != null) {

                                                        subP.put(
                                                                "SubParameter.Name",
                                                                attrObj
                                                                        .path("Attributes")
                                                                        .path("Name")
                                                                        .asText()
                                                        );
                                                    }
                                                }

                                                subP.put(
                                                        "SubParameter.Value",
                                                        subValObj
                                                                .path("Attributes")
                                                                .path("Value")
                                                                .asText()
                                                );

                                                subParameters.add(
                                                        subP
                                                );
                                            }
                                        }
                                    }
                                }

                                paramObj.set(
                                        "SubParameters",
                                        subParameters
                                );
                            }
                        }
                    }
                }
            }
        }

        mapper.writeValue(
                new File(outputFilePath),
                instances
        );

        MigrationLog.success(
                "[TCD-InstanceLevel3] CL values and sub-parameters populated successfully: "
                        + outputFilePath
        );
    }


    // ---------------------------------------------------------
    // LEVEL 4 - Flatten instance structure
    // ---------------------------------------------------------

    public static void generateLevel4Instances(
            String inputFilePath,
            String outputFilePath
    ) throws Exception {

        MigrationLog.debugSection(
                "TCD Instance Level 4 - Flatten Instances"
        );

        MigrationLog.debug(
                "Input file: "
                        + inputFilePath
        );

        MigrationLog.debug(
                "Output file: "
                        + outputFilePath
        );

        ObjectMapper mapper =
                new ObjectMapper()
                        .enable(SerializationFeature.INDENT_OUTPUT);

        ArrayNode instances =
                (ArrayNode) mapper.readTree(
                        new File(inputFilePath)
                );

        ArrayNode flatInstances =
                mapper.createArrayNode();

        for (JsonNode instNode : instances) {

            ObjectNode newInstNode =
                    mapper.createObjectNode();

            newInstNode.put(
                    "insatanceSurrogate",
                    instNode
                            .path("insatanceSurrogate")
                            .asText()
            );

            newInstNode.put(
                    "TC_NO",
                    instNode
                            .path("TC_NO")
                            .asText()
            );

            newInstNode.put(
                    "Name",
                    instNode
                            .path("Name")
                            .asText()
            );

            newInstNode.put(
                    "testSheetSurrogate",
                    instNode
                            .path("testSheetSurrogate")
                            .asText()
            );

            ArrayNode flatParams =
                    mapper.createArrayNode();

            for (JsonNode param :
                    instNode.path("TDInstance")) {

                ObjectNode mainParam =
                        mapper.createObjectNode();

                mainParam.put(
                        "Parameter.Surrogate",
                        param
                                .path("Parameter.Surrogate")
                                .asText()
                );

                mainParam.put(
                        "Parameter.Name",
                        param
                                .path("Parameter.Name")
                                .asText()
                );

                mainParam.put(
                        "Parameter.Value",
                        param
                                .path("Parameter.Value")
                                .asText()
                );

                flatParams.add(
                        mainParam
                );

                if (param.has("SubParameters")
                        && param
                                .path("SubParameters")
                                .isArray()) {

                    String parentName =
                            param
                                    .path("Parameter.Name")
                                    .asText();

                    for (JsonNode subP :
                            param.path(
                                    "SubParameters"
                            )) {

                        ObjectNode newSubParam =
                                mapper.createObjectNode();

                        newSubParam.put(
                                "Parameter.Surrogate",
                                subP
                                        .path("SubParameter.Surrogate")
                                        .asText()
                        );

                        newSubParam.put(
                                "Parameter.Name",
                                parentName
                                        + "."
                                        + subP
                                                .path("SubParameter.Name")
                                                .asText()
                        );

                        newSubParam.put(
                                "Parameter.Value",
                                subP
                                        .path("SubParameter.Value")
                                        .asText()
                        );

                        flatParams.add(
                                newSubParam
                        );
                    }
                }
            }

            newInstNode.set(
                    "TDInstance",
                    flatParams
            );

            flatInstances.add(
                    newInstNode
            );
        }

        mapper.writeValue(
                new File(outputFilePath),
                flatInstances
        );

        MigrationLog.success(
                "[TCD-InstanceLevel4] Flattened instance JSON created successfully: "
                        + outputFilePath
        );
    }


    // ---------------------------------------------------------
    // LEVEL 5 - Load all attributes from Level 0
    // ---------------------------------------------------------

    private static List<String> loadAllAttributesFromLevel0(
            String inputFilePath
    ) {

        List<String> allAttributes =
                new ArrayList<>();

        try {

            File level4File =
                    new File(inputFilePath);

            String parentDir =
                    level4File.getParent();

            String name =
                    level4File.getName();

            /*
             * Extract the GUID from the file name.
             * Everything before the first underscore belongs
             * to the generated object identifier.
             */

            String guid =
                    name.split("_")[0];

            /*
             * Reconstruct the corresponding Level 0
             * TestSheet row-data file.
             */

            String level0Path =
                    parentDir
                            + File.separator
                            + guid
                            + "_tcdsheets-rowdata-level0.json";

            MigrationLog.debug(
                    "[TCD-MatrixLevel5] Reconstructed Level 0 path: "
                            + level0Path
            );

            File level0File =
                    new File(level0Path);

            if (level0File.exists()) {

                MigrationLog.debug(
                        "[TCD-MatrixLevel5] Level 0 file found. Loading attributes."
                );

                ObjectMapper mapper =
                        new ObjectMapper();

                JsonNode root =
                        mapper.readTree(
                                level0File
                        );

                if (root.isArray()) {

                    for (JsonNode attrNode : root) {

                        String attrName =
                                attrNode
                                        .path("Attributes")
                                        .path("Name")
                                        .asText();

                        if (!attrName.isEmpty()) {

                            allAttributes.add(
                                    attrName
                            );

                            MigrationLog.debug(
                                    "[TCD-MatrixLevel5] Structure attribute found: "
                                            + attrName
                            );
                        }
                    }
                }

                MigrationLog.debug(
                        "[TCD-MatrixLevel5] Total attributes loaded from Level 0 structure: "
                                + allAttributes.size()
                );

            } else {

                MigrationLog.error(
                        "[TCD-MatrixLevel5] Level 0 file does not exist: "
                                + level0File.getAbsolutePath()
                );
            }

        } catch (Exception e) {

            MigrationLog.error(
                    "[TCD-MatrixLevel5] Failed to read Level 0 file: "
                            + e.getMessage()
            );

            if (MigrationLog.isDebugEnabled()) {
                e.printStackTrace();
            }
        }

        return allAttributes;
    }


    // ---------------------------------------------------------
    // LEVEL 5 - Generate CSV matrix
    // ---------------------------------------------------------

    public static void generateLevel5MatrixCSV(
            String inputFilePath,
            String csvFilePath,
            String appName
    ) throws Exception {

        MigrationLog.debugSection(
                "TCD Matrix Level 5 - Generate CSV Matrix"
        );

        MigrationLog.debug(
                "Input file (Level 4): "
                        + inputFilePath
        );

        MigrationLog.debug(
                "Output CSV: "
                        + csvFilePath
        );

        MigrationLog.debug(
                "Application: "
                        + appName
        );

        ObjectMapper mapper =
                new ObjectMapper();

        ArrayNode instances =
                (ArrayNode) mapper.readTree(
                        new File(inputFilePath)
                );

        Map<String, Map<String, String>> matrix =
                new LinkedHashMap<>();

        List<String> testCaseNames =
                new ArrayList<>();

        /*
         * Initialize the matrix with all attributes
         * defined in the TestSheet structure.
         */

        List<String> allStructureAttributes =
                loadAllAttributesFromLevel0(
                        inputFilePath
                );

        for (String attrName :
                allStructureAttributes) {

            matrix.put(
                    attrName,
                    new LinkedHashMap<>()
            );
        }

        MigrationLog.debug(
                "[TCD-MatrixLevel5] Matrix initialized with "
                        + matrix.size()
                        + " base rows from the TestSheet structure."
        );

        /*
         * Populate matrix values from the generated instances.
         */

        for (JsonNode inst : instances) {

            String tcName =
                    inst
                            .path("Name")
                            .asText();

            testCaseNames.add(
                    tcName
            );

            for (JsonNode param :
                    inst.path("TDInstance")) {

                String pName =
                        param
                                .path("Parameter.Name")
                                .asText();

                String pValue =
                        param
                                .path("Parameter.Value")
                                .asText()
                                .replaceAll(
                                        "[\\r\\n\\t]+",
                                        " "
                                )
                                .replace(
                                        ";",
                                        ","
                                );

                if (!matrix.containsKey(pName)) {

                    MigrationLog.debug(
                            "[TCD-MatrixLevel5] Attribute not found in Level 0 structure. Adding dynamically: "
                                    + pName
                    );
                }

                matrix.putIfAbsent(
                        pName,
                        new LinkedHashMap<>()
                );

                matrix.get(pName).put(
                        tcName,
                        pValue
                );
            }
        }

        MigrationLog.debug(
                "[TCD-MatrixLevel5] Final matrix row count before CSV generation: "
                        + matrix.size()
        );

        MigrationLog.debug(
                "[TCD-MatrixLevel5] Matrix contains 'VPNR-Karte': "
                        + matrix.containsKey(
                                "VPNR-Karte"
                        )
        );

        /*
         * Create the list of output files.
         *
         * The CSV is written both to the requested location
         * and to the application's migration template folder.
         */

        List<File> targetFiles =
                new ArrayList<>();

        targetFiles.add(
                new File(csvFilePath)
        );

        File originalCsvFile =
                new File(csvFilePath);

        String fileName =
                originalCsvFile.getName();

        String templateFolderPath =
                System.getProperty("user.dir")
                        + File.separator
                        + "migration"
                        + File.separator
                        + "data"
                        + File.separator
                        + appName
                        + File.separator
                        + "template";

        File templateDir =
                new File(templateFolderPath);

        if (!templateDir.exists()) {

            boolean created =
                    templateDir.mkdirs();

            MigrationLog.debug(
                    "[TCD-MatrixLevel5] Template directory created: "
                            + created
                            + " -> "
                            + templateDir.getAbsolutePath()
            );
        }

        targetFiles.add(
                new File(
                        templateDir,
                        fileName
                )
        );

        /*
         * Write the CSV matrix to all configured target files.
         */

        for (File targetFile :
                targetFiles) {

            try (PrintWriter writer =
                    new PrintWriter(
                            new FileWriter(targetFile)
                    )) {

                writer.print(
                        "Parameter Name"
                );

                for (String tc :
                        testCaseNames) {

                    writer.print(
                            ";" + tc
                    );
                }

                writer.println();

                for (Map.Entry<String, Map<String, String>> entry :
                        matrix.entrySet()) {

                    writer.print(
                            entry.getKey()
                    );

                    for (String tc :
                            testCaseNames) {

                        writer.print(
                                ";"
                                        + entry
                                                .getValue()
                                                .getOrDefault(
                                                        tc,
                                                        ""
                                                )
                        );
                    }

                    writer.println();
                }
            }

            MigrationLog.success(
                    "[TCD-MatrixLevel5] CSV generated: "
                            + targetFile.getAbsolutePath()
            );
        }

        MigrationLog.debug(
                "[TCD-MatrixLevel5] CSV matrix generation completed."
        );
    }
}