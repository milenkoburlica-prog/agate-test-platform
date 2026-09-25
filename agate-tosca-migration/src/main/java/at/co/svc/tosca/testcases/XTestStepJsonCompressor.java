package at.co.svc.tosca.testcases;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import at.co.svc.aga.transformator.utils.MigrationLog;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.io.FileInputStream;

public class XTestStepJsonCompressor {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) throws Exception {

        String inputFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KES2VE001YTSWWGQQQ2YY28M_testcases-extended.json";

        inputFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KTNMKBVH9MZ60Q2Q9CHGBV5Y_templates-extended.json";

        String tsuFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\TEST_TEMPLATE_V1.tsu";

        String inputFile2 =
                compressTemplates(inputFile, tsuFile);

        compress(inputFile2, tsuFile);
    }
    
    
    // =========================================================
    // COMPRESS
    // =========================================================

    public static void compress(
            String inputFile,
            String tsuFile
    ) throws Exception {

        // -----------------------------------------------------
        // LOAD INPUT JSON
        // -----------------------------------------------------

        List<Map<String, Object>> nodes =
                MAPPER.readValue(
                        new File(inputFile),
                        new TypeReference<List<Map<String, Object>>>() {
                        });

        // -----------------------------------------------------
        // LOAD TSU
        // -----------------------------------------------------

        Map<String, Map<String, Object>> tsuIndex =
                loadTsuIndex(tsuFile);

        // -----------------------------------------------------
        // INDEX
        // -----------------------------------------------------

        Map<String, Map<String, Object>> bySurrogate =
                new HashMap<>();

        for (Map<String, Object> node : nodes) {

            String surrogate =
                    asText(node.get("Surrogate"));

            if (surrogate != null) {
                bySurrogate.put(surrogate, node);
            }
        }

        // -----------------------------------------------------
        // OUTPUT
        // -----------------------------------------------------

        List<Map<String, Object>> compressed =
                new ArrayList<>();

        Set<String> skipObjectClasses = Set.of(
                "XTestStepValue",
                "ParameterLayerReference",
                "ParameterReference",
                "Parameter"
        );

        for (Map<String, Object> node : nodes) {

            String objectClass =
                    asText(node.get("ObjectClass"));

            // skip helper objects
            if (skipObjectClasses.contains(objectClass)) {
                continue;
            }

            Map<String, Object> copy =
                    MAPPER.convertValue(
                            node,
                            new TypeReference<Map<String, Object>>() {
                            });

            // -------------------------------------------------
            // XTESTSTEP
            // -------------------------------------------------

            if ("XTestStep".equals(objectClass)) {

                List<Map<String, Object>> parameters =
                        buildXTestStepParameters(
                                copy,
                                bySurrogate,
                                tsuIndex
                        );

                copy.put("Parameters", parameters);
            }

            // -------------------------------------------------
            // REUSABLE
            // -------------------------------------------------

            if ("TestStepFolderReference".equals(objectClass)) {

                List<Map<String, Object>> parameters =
                        buildReusableParameters(
                                copy,
                                bySurrogate,
                                tsuIndex
                        );

                copy.put("Parameters", parameters);
            }

            if (!copy.containsKey("Parameters")) {
                copy.put("Parameters", new ArrayList<>());
            }

            compressed.add(copy);
        }

        // -----------------------------------------------------
        // SAVE
        // -----------------------------------------------------

        Path inputPath = Path.of(inputFile);

        String outputFile =
                inputPath.toString()
                        .replace(".json", "-compressed.json");

        MAPPER.writeValue(
                new File(outputFile),
                compressed
        );

        MigrationLog.success("Compressed JSON written: " + outputFile);
    }

    // =========================================================
    // XTESTSTEP PARAMETERS
    // =========================================================

    private static List<Map<String, Object>> buildXTestStepParameters(
            Map<String, Object> step,
            Map<String, Map<String, Object>> bySurrogate,
            Map<String, Map<String, Object>> tsuIndex
    ) {

        List<Map<String, Object>> result = new ArrayList<>();

        Map<String, Object> assocs = asMap(step.get("Assocs"));

        List<String> testStepValues =
                asStringList(assocs.get("TestStepValues"));

        boolean isApiModule = false;
        List<String> moduleIds = asStringList(assocs.get("Module"));

        if (!moduleIds.isEmpty()) {
            Map<String, Object> module = tsuIndex.get(moduleIds.get(0));
            if (module != null) {
                isApiModule = "ApiModule".equals(asText(module.get("ObjectClass")));
            }
        }

        // Use LinkedHashSet to preserve the original occurrence order
        Set<String> visitedValues = new LinkedHashSet<>();

        for (String valueId : testStepValues) {
            collectTestStepValues(valueId, bySurrogate, visitedValues);
        }

        traceApiParameterDiscovery(step, isApiModule, testStepValues, visitedValues, bySurrogate, tsuIndex);

        for (String valueId : visitedValues) {

            Map<String, Object> xValue = bySurrogate.get(valueId);
            if (xValue == null) continue;

            Map<String, Object> attrs = asMap(xValue.get("Attributes"));
            Map<String, Object> valueAssocs = asMap(xValue.get("Assocs"));

            // --- XCondition handling ---
            String condition = asText(attrs.get("Condition"));

            // If the current parameter has no condition, resolve it from the parent
            if (condition.isEmpty()) {
                List<String> parentIds = asStringList(valueAssocs.get("ParentValue"));
                if (!parentIds.isEmpty()) {
                    Map<String, Object> parentNode = bySurrogate.get(parentIds.get(0));
                    if (parentNode != null) {
                        condition = asText(asMap(parentNode.get("Attributes")).get("Condition"));
                    }
                }
            }
            // -----------------------------

            List<String> moduleAttrs = asStringList(valueAssocs.get("ModuleAttribute"));
            String moduleAttrId = moduleAttrs.isEmpty() ? "" : moduleAttrs.get(0);
            String value = asText(attrs.get("Value"));
            String actionMode = mapActionMode(asText(attrs.get("ActionMode")));
            String actionProperty = asText(attrs.get("ActionProperty"));
            String operator = asText(attrs.get("Operator"));
            
            String explicitName = "";
            if (isApiModule) {
                Map<String, Object> moduleAttr = tsuIndex.get(moduleAttrId);
                if (moduleAttr != null) {
                    explicitName = asText(asMap(moduleAttr.get("Attributes")).get("Name"));
                }
            } else {
                explicitName = asText(attrs.get("ExplicitName"));
            }

            Map<String, Object> param = new LinkedHashMap<>();

            String valueSurrogate = asText(xValue.get("Surrogate"));
            String xmlPath = buildXmlPath(xValue, bySurrogate, tsuIndex);
            String jsonPath = buildJsonPath(xValue, bySurrogate, tsuIndex);
            
            param.put("XmlPath", xmlPath);
            param.put("JsonPath", jsonPath);
            
//            String toscaPath = buildToscaPath(xValue, bySurrogate, tsuIndex);
            PathResult toscaInfo = buildToscaPathFull(xValue, bySurrogate, tsuIndex);
            if ((toscaInfo.path != null) && (toscaInfo.path.equals("SQL Statement"))) {
                explicitName = "SQL Statement";
            }
            
            value = value.replace("\r", " ").replace("\n", " ").replaceAll("\\s+", " ").trim();
            
            param.put("ModuleAttributeSurrogate", valueSurrogate);
            param.put("Value", value);
            param.put("ExplicitName", explicitName);
            
            // Add XCondition when present
            if (!condition.isEmpty()) {
                param.put("XCondition", condition);
            }
            
            param.put("ActionMode", actionMode);
            param.put("ActionProperty", actionProperty);
            param.put("Operator", operator);
            
            if (toscaInfo.path != null) {
                if (toscaInfo.path.contains("Result Table")) 
                    toscaInfo.path = toscaInfo.path.replace("Result Table.$", "#");
                if (toscaInfo.path.contains("Result Table")) 
                    toscaInfo.path = toscaInfo.path.replace("Result Table.#", "#");
            }
            param.put("ToscaPath", toscaInfo.path);
            param.put("ToscaPathID", toscaInfo.pathId);

            if (("Insert".equals(actionMode)) && ("Open Connection".equals(toscaInfo.path))) continue;
            if (("Insert".equals(actionMode)) && ("Close connection".equals(toscaInfo.path))) continue;
            if ("Select".equals(actionMode)) continue;

            result.add(param);
        }

        return result;
    }
 // =========================================================
    // BUILD XML PATH (RESOLVE XPATH FROM TSU MODULE ATTRIBUTE)
    // =========================================================
    private static String buildXmlPath(
            Map<String, Object> xValue,
            Map<String, Map<String, Object>> bySurrogate,
            Map<String, Map<String, Object>> tsuIndex
    ) {
        if (xValue == null) {
            return "";
        }

        // 1. Read Assocs from the XTestStepValue object
        Map<String, Object> assocs = asMap(xValue.get("Assocs"));
        List<String> moduleAttrs = asStringList(assocs.get("ModuleAttribute"));
        if (moduleAttrs.isEmpty()) {
            return "";
        }

        // 2. Resolve XModuleAttribute from tsuIndex using the associated ID
        String moduleAttrId = moduleAttrs.get(0);
        Map<String, Object> moduleAttr = tsuIndex.get(moduleAttrId);
        if (moduleAttr == null) {
            return "";
        }

        // 3. Read all IDs from the "Properties" array of XModuleAttribute
        Map<String, Object> moduleAttrAssocs = asMap(moduleAttr.get("Assocs"));
        List<String> properties = asStringList(moduleAttrAssocs.get("Properties"));
        if (properties.isEmpty()) {
            return "";
        }

        boolean isXPathType = false;
        String extractedPath = "";

        // 4. Iterate over all associated XParam objects in tsuIndex
        for (String propId : properties) {
            Map<String, Object> propParam = tsuIndex.get(propId);
            if (propParam == null) {
                continue;
            }

            Map<String, Object> propAttrs = asMap(propParam.get("Attributes"));
            String paramName = asText(propAttrs.get("Name"));
            String paramValue = asText(propAttrs.get("Value"));

            // Check whether PathType is set to XPath
            if ("PathType".equals(paramName) && "XPath".equals(paramValue)) {
                isXPathType = true;
            }

            // Store the value of the "Path" parameter
            if ("Path".equals(paramName)) {
                extractedPath = paramValue;
            }
        }

        // 5. Return the extracted path when the expected PathType is present; otherwise return an empty string
        if (isXPathType && !extractedPath.isEmpty()) {
            return extractedPath;
        }

        return "";
    }
    private static String buildJsonPath(
            Map<String, Object> xValue,
            Map<String, Map<String, Object>> bySurrogate,
            Map<String, Map<String, Object>> tsuIndex
    ) {
        if (xValue == null) {
            return "";
        }

        // 1. Read Assocs from the XTestStepValue object
        Map<String, Object> assocs = asMap(xValue.get("Assocs"));
        List<String> moduleAttrs = asStringList(assocs.get("ModuleAttribute"));
        if (moduleAttrs.isEmpty()) {
            return "";
        }

        // 2. Resolve XModuleAttribute from tsuIndex using the associated ID
        String moduleAttrId = moduleAttrs.get(0);
        Map<String, Object> moduleAttr = tsuIndex.get(moduleAttrId);
        if (moduleAttr == null) {
            return "";
        }

        // 3. Read all IDs from the "Properties" array of XModuleAttribute
        Map<String, Object> moduleAttrAssocs = asMap(moduleAttr.get("Assocs"));
        List<String> properties = asStringList(moduleAttrAssocs.get("Properties"));
        if (properties.isEmpty()) {
            return "";
        }

        boolean isJsonPathType = false;
        String extractedPath = "";

        // 4. Iterate over all associated XParam objects in tsuIndex
        for (String propId : properties) {
            Map<String, Object> propParam = tsuIndex.get(propId);
            if (propParam == null) {
                continue;
            }

            Map<String, Object> propAttrs = asMap(propParam.get("Attributes"));
            String paramName = asText(propAttrs.get("Name"));
            String paramValue = asText(propAttrs.get("Value"));

            // Check whether PathType is set to JsonPath
            if ("PathType".equals(paramName) && "JsonPath".equals(paramValue)) {
                isJsonPathType = true;
            }

            // Store the value of the "Path" parameter (npr. [*].urlWithVersion[*].url)
            if ("Path".equals(paramName)) {
                extractedPath = paramValue;
            }
        }

        // 5. Return the extracted path when the expected PathType is present; otherwise return an empty string
        if (isJsonPathType && !extractedPath.isEmpty()) {
            return extractedPath;
        }

        return "";
    }

    private static PathResult buildToscaPathFull(
            Map<String, Object> xValue,
            Map<String, Map<String, Object>> bySurrogate,
            Map<String, Map<String, Object>> tsuIndex
    ) {
        List<String> pathParts = new ArrayList<>();
        List<String> idParts = new ArrayList<>();

        Map<String, Object> current = xValue;

        while (current != null && "XTestStepValue".equals(asText(current.get("ObjectClass")))) {
            Map<String, Object> attrs = asMap(current.get("Attributes"));
            Map<String, Object> assocs = asMap(current.get("Assocs"));
            String surrogate = asText(current.get("Surrogate"));

            // 1. Resolve the path segment name
            String pathPart = asText(attrs.get("ExplicitName"));
            if (pathPart.isBlank()) {
                List<String> moduleAttrs = asStringList(assocs.get("ModuleAttribute"));
                if (!moduleAttrs.isEmpty()) {
                    Map<String, Object> moduleAttr = tsuIndex.get(moduleAttrs.get(0));
                    if (moduleAttr != null) {
                        pathPart = asText(asMap(moduleAttr.get("Attributes")).get("Name"));
                    }
                }
            }

            if (!pathPart.isBlank()) {
                pathParts.add(pathPart);
                idParts.add(surrogate);
            }

            // 2. Move to the parent
            List<String> parentValues = asStringList(assocs.get("ParentValue"));
            if (parentValues.isEmpty()) break;
            current = bySurrogate.get(parentValues.get(0));
        }

        Collections.reverse(pathParts);
        Collections.reverse(idParts);

        return new PathResult(String.join(".", pathParts), String.join(".", idParts));
    }
    
//    private static String buildToscaPath(
//            Map<String, Object> xValue,
//            Map<String, Map<String, Object>> bySurrogate,
//            Map<String, Map<String, Object>> tsuIndex
//    ) {
//
//        List<String> pathParts = new ArrayList<>();
//
//        Map<String, Object> current = xValue;
//
//        while (current != null
//                && "XTestStepValue".equals(
//                asText(current.get("ObjectClass")))) {
//
//            Map<String, Object> attrs =
//                    asMap(current.get("Attributes"));
//
//            Map<String, Object> assocs =
//                    asMap(current.get("Assocs"));
//
//            // -----------------------------------------
//            // PRIORITY:
//            // 1) ExplicitName
//            // 2) ModuleAttribute.Name
//            // -----------------------------------------
//
//            String pathPart =
//                    asText(attrs.get("ExplicitName"));
//
//            // Fallback to ModuleAttribute.Name
//            if (pathPart.isBlank()) {
//
//                List<String> moduleAttrs =
//                        asStringList(
//                                assocs.get("ModuleAttribute")
//                        );
//
//                if (!moduleAttrs.isEmpty()) {
//
//                    String moduleAttrId =
//                            moduleAttrs.get(0);
//
//                    Map<String, Object> moduleAttr =
//                            tsuIndex.get(moduleAttrId);
//
//                    if (moduleAttr != null) {
//
//                        Map<String, Object> moduleAttrsMap =
//                                asMap(moduleAttr.get("Attributes"));
//
//                        pathPart =
//                                asText(
//                                        moduleAttrsMap.get("Name")
//                                );
//                    }
//                }
//            }
//
//            if (!pathPart.isBlank()) {
//                pathParts.add(pathPart);
//            }
//
//            // -----------------------------------------
//            // GO TO PARENT
//            // -----------------------------------------
//
//            List<String> parentValues =
//                    asStringList(
//                            assocs.get("ParentValue")
//                    );
//
//            if (parentValues.isEmpty()) {
//                break;
//            }
//
//            String parentId =
//                    parentValues.get(0);
//
//            current =
//                    bySurrogate.get(parentId);
//        }
//
//        Collections.reverse(pathParts);
//
//        return String.join(".", pathParts);
//    }
    
    private static void collectTestStepValues(
            String valueId,
            Map<String, Map<String, Object>> bySurrogate,
            Set<String> visited
    ) {

        if (valueId == null || visited.contains(valueId)) {
            return;
        }

        Map<String, Object> node = bySurrogate.get(valueId);
        if (node == null) {
            return;
        }

        visited.add(valueId);

        Map<String, Object> assocs = asMap(node.get("Assocs"));

        List<String> subValues =
                asStringList(assocs.get("SubValues"));

        for (String subId : subValues) {
            collectTestStepValues(subId, bySurrogate, visited);
        }
    }
    

    private static void traceApiParameterDiscovery(
            Map<String, Object> step,
            boolean isApiModule,
            List<String> testStepValues,
            Set<String> visitedValues,
            Map<String, Map<String, Object>> bySurrogate,
            Map<String, Map<String, Object>> tsuIndex
    ) {

        if (!isApiModule) {
            return;
        }

        Map<String, Object> stepAttrs = asMap(step.get("Attributes"));
        String stepName = asText(stepAttrs.get("Name"));

        if (!"doEingabe Request V11".equals(stepName)) {
            return;
        }

        System.err.println();
        System.err.println("========== API PARAM DISCOVERY TRACE ==========");
        System.err.println("[API-PARAM-TRACE] step=" + stepName);
        System.err.println("[API-PARAM-TRACE] topLevel TestStepValues=" + testStepValues);
        System.err.println("[API-PARAM-TRACE] recursively visited XTestStepValues=" + visitedValues.size());

        Set<String> referencedModuleAttributeIds = new LinkedHashSet<>();

        for (String valueId : visitedValues) {
            Map<String, Object> xValue = bySurrogate.get(valueId);
            if (xValue == null) {
                System.err.println("[API-XVALUE] id=" + valueId + " <missing in bySurrogate>");
                continue;
            }

            Map<String, Object> attrs = asMap(xValue.get("Attributes"));
            Map<String, Object> assocs = asMap(xValue.get("Assocs"));
            List<String> moduleAttributeIds = asStringList(assocs.get("ModuleAttribute"));
            List<String> subValues = asStringList(assocs.get("SubValues"));

            String moduleAttributeId = moduleAttributeIds.isEmpty() ? "" : moduleAttributeIds.get(0);
            String moduleAttributeName = "";

            if (!moduleAttributeId.isEmpty()) {
                referencedModuleAttributeIds.add(moduleAttributeId);
                Map<String, Object> moduleAttribute = tsuIndex.get(moduleAttributeId);
                if (moduleAttribute != null) {
                    moduleAttributeName = asText(asMap(moduleAttribute.get("Attributes")).get("Name"));
                }
            }

            System.err.println(
                    "[API-XVALUE]"
                            + " id=" + valueId
                            + " moduleAttrId=" + moduleAttributeId
                            + " moduleAttrName='" + moduleAttributeName + "'"
                            + " rawActionMode='" + asText(attrs.get("ActionMode")) + "'"
                            + " mappedActionMode='" + mapActionMode(asText(attrs.get("ActionMode"))) + "'"
                            + " value='" + asText(attrs.get("Value")) + "'"
                            + " subValues=" + subValues
            );
        }

        System.err.println("---------- TARGET MODULE ATTRIBUTES ----------");

        for (Map.Entry<String, Map<String, Object>> entry : tsuIndex.entrySet()) {
            String moduleAttributeId = entry.getKey();
            Map<String, Object> node = entry.getValue();

            if (!"XModuleAttribute".equals(asText(node.get("ObjectClass")))) {
                continue;
            }

            Map<String, Object> attrs = asMap(node.get("Attributes"));
            String name = asText(attrs.get("Name"));

            if (!"cardToken".equalsIgnoreCase(name)
                    && !"stockTuerNummer".equalsIgnoreCase(name)) {
                continue;
            }

            Map<String, Object> assocs = asMap(node.get("Assocs"));

            System.err.println(
                    "[API-MODULE-ATTR]"
                            + " id=" + moduleAttributeId
                            + " name='" + name + "'"
                            + " referencedByVisitedXTestStepValue="
                            + referencedModuleAttributeIds.contains(moduleAttributeId)
                            + " attrs=" + attrs
                            + " assocs=" + assocs
            );
        }

        System.err.println("===============================================");
        System.err.println();
    }

    // =========================================================
    // ACTION MODE MAPPING
    // =========================================================

    private static String mapActionMode(String value) {

        if (value == null) {
            return "";
        }

        return switch (value) {

            case "37" -> "Insert";

            case "515" -> "Insert";
            case "69" -> "Verify";

            case "517" -> "Select";

            case "1" -> "Constraint";

            case "165" -> "Buffer";

            default -> value;
        };
    }

    // =========================================================
    // REUSABLE PARAMETERS
    // =========================================================

    private static List<Map<String, Object>> buildReusableParameters(
            Map<String, Object> reusable,
            Map<String, Map<String, Object>> bySurrogate,
            Map<String, Map<String, Object>> tsuIndex
    ) {

        List<Map<String, Object>> result =
                new ArrayList<>();

        Map<String, Object> assocs =
                asMap(reusable.get("Assocs"));

        List<String> layerRefs =
                asStringList(
                        assocs.get("ParameterLayerReference")
                );

        for (String layerRefId : layerRefs) {

            Map<String, Object> layerRef =
                    findNode(
                            layerRefId,
                            bySurrogate,
                            tsuIndex
                    );

            if (layerRef == null) {
                continue;
            }

            Map<String, Object> layerAssocs =
                    asMap(layerRef.get("Assocs"));

            List<String> allParamRefs =
                    asStringList(
                            layerAssocs.get("AllParameterReferences")
                    );

            for (String paramRefId : allParamRefs) {

                Map<String, Object> paramRef =
                        findNode(
                                paramRefId,
                                bySurrogate,
                                tsuIndex
                        );

                if (paramRef == null) {
                    continue;
                }

                Map<String, Object> paramRefAttrs =
                        asMap(paramRef.get("Attributes"));

                Map<String, Object> paramRefAssocs =
                        asMap(paramRef.get("Assocs"));

                List<String> parameterIds =
                        asStringList(
                                paramRefAssocs.get("Parameter")
                        );

                String explicitName = "";
                Map<String, Object> parameter = null;

                if (!parameterIds.isEmpty()) {

                    String parameterId =
                            parameterIds.get(0);

                    parameter =
                            findNode(
                                    parameterId,
                                    bySurrogate,
                                    tsuIndex
                            );

                    if (parameter != null) {
                        explicitName =
                                buildParameterPath(
                                        parameterId,
                                        bySurrogate,
                                        tsuIndex
                                );
                    }
                }

                /*
                 * A Tosca business parameter may be a structural container,
                 * for example:
                 *
                 *   Request
                 *     SVNR
                 *     DMPCode
                 *
                 * or:
                 *
                 *   Response
                 *     SVTCode
                 *
                 * The container itself is not an executable AGATE parameter.
                 * Its name is preserved as part of the child parameter path
                 * (Request.SVNR, Response.SVTCode, ...), but the empty
                 * container entry itself must not be emitted.
                 */
                if (isStructuralParameterContainer(parameter)
                        && asText(paramRefAttrs.get("Value")).isBlank()) {
                    continue;
                }

                Map<String, Object> compactParam =
                        new LinkedHashMap<>();

                compactParam.put(
                        "Value",
                        paramRefAttrs.get("Value")
                );

                compactParam.put(
                        "ExplicitName",
                        explicitName
                );

                result.add(compactParam);
            }
        }

        return result;
    }

    /**
     * Builds the complete Tosca business-parameter path.
     *
     * Examples:
     *   VSNR                 -> VSNR
     *   Request -> SVNR      -> Request.SVNR
     *   Response -> SVTCode  -> Response.SVTCode
     *
     * The method follows Assocs.ParentParameter recursively and therefore
     * also supports more than one nesting level. A visited set prevents
     * malformed/cyclic Tosca data from causing endless recursion.
     */
    private static String buildParameterPath(
            String parameterId,
            Map<String, Map<String, Object>> bySurrogate,
            Map<String, Map<String, Object>> tsuIndex
    ) {
        return buildParameterPath(
                parameterId,
                bySurrogate,
                tsuIndex,
                new HashSet<>()
        );
    }

    private static String buildParameterPath(
            String parameterId,
            Map<String, Map<String, Object>> bySurrogate,
            Map<String, Map<String, Object>> tsuIndex,
            Set<String> visited
    ) {

        if (parameterId == null
                || parameterId.isBlank()
                || !visited.add(parameterId)) {
            return "";
        }

        Map<String, Object> parameter =
                findNode(
                        parameterId,
                        bySurrogate,
                        tsuIndex
                );

        if (parameter == null) {
            return "";
        }

        Map<String, Object> parameterAttrs =
                asMap(parameter.get("Attributes"));

        String name =
                asText(parameterAttrs.get("Name"));

        Map<String, Object> parameterAssocs =
                asMap(parameter.get("Assocs"));

        List<String> parentParameterIds =
                asStringList(
                        parameterAssocs.get("ParentParameter")
                );

        if (parentParameterIds.isEmpty()) {
            return name;
        }

        String parentPath =
                buildParameterPath(
                        parentParameterIds.get(0),
                        bySurrogate,
                        tsuIndex,
                        visited
                );

        if (parentPath == null || parentPath.isBlank()) {
            return name;
        }

        if (name == null || name.isBlank()) {
            return parentPath;
        }

        return parentPath + "." + name;
    }

    /**
     * Resolves a Tosca node from the current intermediate JSON first and
     * falls back to the complete TSU index when helper nodes have already
     * been removed by an earlier compression phase.
     */
    private static Map<String, Object> findNode(
            String surrogate,
            Map<String, Map<String, Object>> localIndex,
            Map<String, Map<String, Object>> tsuIndex
    ) {

        if (surrogate == null || surrogate.isBlank()) {
            return null;
        }

        Map<String, Object> node =
                localIndex.get(surrogate);

        if (node != null) {
            return node;
        }

        return tsuIndex.get(surrogate);
    }

    /**
     * Returns true for a Tosca Parameter that acts only as a parent/container
     * for nested business parameters.
     */
    private static boolean isStructuralParameterContainer(
            Map<String, Object> parameter
    ) {

        if (parameter == null) {
            return false;
        }

        Map<String, Object> parameterAssocs =
                asMap(parameter.get("Assocs"));

        List<String> childParameterIds =
                asStringList(
                        parameterAssocs.get("Parameters")
                );

        return !childParameterIds.isEmpty();
    }

    // =========================================================
    // LOAD TSU INDEX
    // =========================================================

    private static Map<String, Map<String, Object>> loadTsuIndex(
            String tsuFile
    ) throws Exception {

        Map<String, Map<String, Object>> result =
                new HashMap<>();

        try (
                FileInputStream fis =
                        new FileInputStream(tsuFile);

                GZIPInputStream gis =
                        new GZIPInputStream(fis);

                InputStreamReader reader =
                        new InputStreamReader(gis)
        ) {

            Map<String, Object> root =
                    MAPPER.readValue(
                            reader,
                            new TypeReference<Map<String, Object>>() {
                            });

            List<Map<String, Object>> entities =
                    (List<Map<String, Object>>) root.get("Entities");

            for (Map<String, Object> entity : entities) {

                String surrogate =
                        asText(entity.get("Surrogate"));

                if (surrogate != null) {
                    result.put(surrogate, entity);
                }
            }
        }

        return result;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {

        if (o == null) {
            return new LinkedHashMap<>();
        }

        return (Map<String, Object>) o;
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object o) {

        if (o == null) {
            return new ArrayList<>();
        }

        return (List<String>) o;
    }

    private static String asText(Object o) {

        return o == null
                ? ""
                : String.valueOf(o);
    }
    
    
 // =========================================================
 // COMPRESS TEMPLATES / REUSABLE PARAM LAYERS
 // =========================================================

 public static String compressTemplates(
         String inputFile,
         String tsuFile
 ) throws Exception {

     // -----------------------------------------------------
     // LOAD INPUT JSON
     // -----------------------------------------------------

     List<Map<String, Object>> nodes =
             MAPPER.readValue(
                     new File(inputFile),
                     new TypeReference<List<Map<String, Object>>>() {
                     });

     if (nodes.isEmpty()) {
         return inputFile;
     }

     // -----------------------------------------------------
     // DETECT REUSABLE FILE
     // -----------------------------------------------------

     String rootObjectClass =
             asText(nodes.get(0).get("ObjectClass"));

     if (!"ReuseableTestStepBlock".equals(rootObjectClass)) {
         return inputFile;
     }

     // -----------------------------------------------------
     // LOAD TSU INDEX
     // -----------------------------------------------------

     Map<String, Map<String, Object>> tsuIndex =
             loadTsuIndex(tsuFile);

     // -----------------------------------------------------
     // FILTERED OUTPUT
     // -----------------------------------------------------

     List<Map<String, Object>> result =
             new ArrayList<>();

     Set<String> skipClasses = Set.of(
             "ParameterLayer",
             "Parameter"
     );

     for (Map<String, Object> node : nodes) {

         String objectClass =
                 asText(node.get("ObjectClass"));

         // ---------------------------------------------
         // SKIP INTERNAL TEMPLATE OBJECTS
         // ---------------------------------------------

         if (skipClasses.contains(objectClass)) {
             continue;
         }

         Map<String, Object> copy =
                 MAPPER.convertValue(
                         node,
                         new TypeReference<Map<String, Object>>() {
                         });

         // ---------------------------------------------
         // REUSABLE BLOCK
         // ---------------------------------------------

         if ("ReuseableTestStepBlock".equals(objectClass)) {

             List<Map<String, Object>> businessParams =
                     buildBusinessParameters(
                             copy,
                             tsuIndex
                     );

             copy.put(
                     "BusinessParameters",
                     businessParams
             );
         }

         result.add(copy);
     }

     // -----------------------------------------------------
     // OUTPUT FILE
     // -----------------------------------------------------

     Path inputPath = Path.of(inputFile);

     String outputFile =
             inputPath.toString()
                     .replace(".json", "-compressed-temp.json");

     MAPPER.writeValue(
             new File(outputFile),
             result
     );

     System.out.println("Reusable template compression done:");
     System.out.println(outputFile);

     return outputFile;
 }
 
//=========================================================
//BUSINESS PARAMETERS
//=========================================================

private static List<Map<String, Object>> buildBusinessParameters(
      Map<String, Object> reusable,
      Map<String, Map<String, Object>> tsuIndex
) {

  List<Map<String, Object>> result =
          new ArrayList<>();

  Map<String, Object> assocs =
          asMap(reusable.get("Assocs"));

  List<String> layerIds =
          asStringList(
                  assocs.get("ParameterLayer")
          );

  for (String layerId : layerIds) {

      Map<String, Object> layer =
              tsuIndex.get(layerId);

      if (layer == null) {
          continue;
      }

      Map<String, Object> layerAssocs =
              asMap(layer.get("Assocs"));

      List<String> parameterIds =
              asStringList(
                      layerAssocs.get("Parameters")
              );

      for (String parameterId : parameterIds) {

          Map<String, Object> parameter =
                  tsuIndex.get(parameterId);

          if (parameter == null) {
              continue;
          }

          Map<String, Object> attrs =
                  asMap(parameter.get("Attributes"));

          String name =
                  asText(attrs.get("Name"));

          String valueSelectionGroup =
                  asText(attrs.get("ValueSelectionGroup"));

          Map<String, Object> compact =
                  new LinkedHashMap<>();

          compact.put("Name", name);

          compact.put(
                  "ValueSelectionGroup",
                  valueSelectionGroup
          );

          result.add(compact);
      }
  }

  return result;
}


}