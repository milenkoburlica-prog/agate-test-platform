package at.co.svc.tosca.transformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class ToscaParserPhase1 {

    private final Map<String, JsonNode> moduleIndex = new HashMap<>();
    private final Map<String, JsonNode> reusableIndex = new HashMap<>();
    private final Map<String, Integer> tsuOrderIndex = new HashMap<>();

    // =========================
    // MAIN
    // =========================
    public static void main(String[] args) throws Exception {

        String input =
                "C:\\work\\projects\\agate-studio\\agate-tosca-migration-svc\\jsonOut\\01KTFF9GQE1KGEW9PHQGGR2JQX_testcases-extended-compressed_step2_step3_step4.json";

        String tsuFile =
                "C:\\work\\projects\\agate-studio\\agate-tosca-migration-svc\\tsu\\GINO LETZTER_KONTAKT Checkup_2.tsu";

        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(new File(input));
        JsonNode tsuRoot = loadTsu(tsuFile, mapper);

        ToscaParserPhase1 parser = new ToscaParserPhase1();

        parser.indexModules(tsuRoot);
        parser.indexReusables(tsuRoot);

        parser.indexTsuOrder(tsuRoot);
        
        ArrayNode result = mapper.createArrayNode();

        for (JsonNode node : root) {

            String oc = node.path("ObjectClass").asText();

            // ONLY REAL FOLDERS REMOVED
            if ("TestStepFolder".equals(oc)) {
                continue;
            }

            // XTestStep
            if ("XTestStep".equals(oc)) {

                JsonNode moduleArr = node.path("Assocs").path("Module");

                if (moduleArr.isArray() && moduleArr.size() > 0) {
                    result.add(parser.transformXTestStep(node, mapper));
                } else {
                    result.add(node);
                }

                continue;
            }

            // FolderReference
            if ("TestStepFolderReference".equals(oc)) {
                result.add(parser.transformFolderReference(node, mapper));
                continue;
            }

            // EVERYTHING ELSE
            result.add(node);
        }

        String out = input.replace("_step4.json", "_agate-step1.json");

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(out), result);

        MigrationLog.success("Tosca parser phase 1 output: " + out);
    }

    // =========================
    // LOAD TSU (GZIP)
    // =========================
    private static JsonNode loadTsu(String path, ObjectMapper mapper) throws Exception {

        try (InputStream fis = new FileInputStream(path);
             GZIPInputStream gis = new GZIPInputStream(fis)) {

            return mapper.readTree(gis);
        }
    }

    // =========================
    // INDEX MODULES
    // =========================
    private void indexModules(JsonNode tsuRoot) {

        JsonNode entities = tsuRoot.path("Entities");

        for (JsonNode node : entities) {

            String oc = node.path("ObjectClass").asText();

            if ("XModule".equals(oc) || "ApiModule".equals(oc)) {

                String surrogate = node.path("Surrogate").asText();

                moduleIndex.put(surrogate, node);
            }
        }

        MigrationLog.debug("Modules indexed: " + moduleIndex.size());
    }

    // =========================
    // INDEX REUSABLES
    // =========================
    private void indexReusables(JsonNode tsuRoot) {

        JsonNode entities = tsuRoot.path("Entities");

        for (JsonNode node : entities) {

            if ("ReuseableTestStepBlock".equals(node.path("ObjectClass").asText())) {

                reusableIndex.put(
                        node.path("Surrogate").asText(),
                        node
                );
            }
        }

        MigrationLog.debug("Reusables indexed: " + reusableIndex.size());
    }

    // =========================
    // XTESTSTEP
    // =========================
    private ObjectNode transformXTestStep(JsonNode node, ObjectMapper mapper) {

        ObjectNode out = mapper.createObjectNode();

        String surrogate = node.path("Surrogate").asText();
        String name = node.path("Attributes").path("Name").asText();
        String condition = node.path("Attributes").path("Condition").asText(null);

        String moduleSurrogate = null;

        JsonNode moduleArr = node.path("Assocs").path("Module");
        if (moduleArr.isArray() && moduleArr.size() > 0) {
            moduleSurrogate = moduleArr.get(0).asText();
        }

        JsonNode module = moduleIndex.get(moduleSurrogate);

        String moduleName = module != null ? module.path("Attributes").path("Name").asText("") : "";
        String moduleClass = module != null ? module.path("ObjectClass").asText("") : "";

        out.put("ObjectClass", "XTestStep");
        out.put("Name", name);
        out.putNull("type");
        out.putNull("op");

        out.put("surrogate", surrogate);
        out.put("moduleSurrogate", moduleSurrogate);

        out.put("module", module != null ? module.path("Attributes").path("Name").asText(null) : null);
        out.put("moduleClass", module != null ? module.path("ObjectClass").asText(null) : null);

        out.putNull("reusableName");
        out.putNull("reusableSurrogate");

        //out.put("condition", condition);
        out.put("condition", normalizeCondition(condition));

        // values is now represented as an ArrayNode.
        ArrayNode values = mapper.createArrayNode();

        if ("XModule".equals(moduleClass) && "TBox Start Program".equals(moduleName)) {
            // Note: transformTBoxStartProgramValues currently returns an ObjectNode.
            // If it remains an ObjectNode, it must be converted here when an array is required.
            out.set("values", transformTBoxStartProgramValues(node.path("Parameters"), mapper));
            return out; 
        } 
        // Additional special cases can remain here when needed.
        else {
            // 1. Copy parameters into a temporary list.
            List<JsonNode> paramList = new ArrayList<>();
            for (JsonNode p : node.path("Parameters")) {
                paramList.add(p);
            }

            // 2. Sort the list according to TSU order.
            paramList.sort((p1, p2) -> {
                String surr1 = p1.path("ModuleAttributeSurrogate").asText("");
                String surr2 = p2.path("ModuleAttributeSurrogate").asText("");
                int order1 = tsuOrderIndex.getOrDefault(surr1, Integer.MAX_VALUE);
                int order2 = tsuOrderIndex.getOrDefault(surr2, Integer.MAX_VALUE);
                return Integer.compare(order1, order2);
            });

            // 3. Build the output ArrayNode.
            for (JsonNode p : paramList) {
                String key = p.path("ExplicitName").asText();
                if (key.isBlank()) continue; 

                ObjectNode v = mapper.createObjectNode();
                v.put("name", key); // Preserve the parameter name in the object.
                v.put("value", p.path("Value").asText());
                v.put("actionMode", p.path("ActionMode").asText());
                
                // Optional fields.
                String actionProperty = p.path("ActionProperty").asText(null);
                if (actionProperty != null && !actionProperty.isBlank()) v.put("actionProperty", actionProperty);

                String toscaPath = p.path("ToscaPath").asText(null);
                if (toscaPath != null && !toscaPath.isBlank()) v.put("toscaPath", toscaPath);

                String toscaPathID = p.path("ToscaPathID").asText(null);
                if (toscaPath != null && !toscaPath.isBlank()) v.put("toscaPathID", toscaPathID);

                String xmlPath = p.path("XmlPath").asText(null);
                if (xmlPath != null && !xmlPath.isBlank()) v.put("xmlPath", xmlPath);

                String jsonPath = p.path("JsonPath").asText(null);
                if (jsonPath != null && !jsonPath.isBlank()) v.put("jsonPath", jsonPath);

                // Add XCondition when present.
                String xCondition = p.path("XCondition").asText(null);
                if (xCondition != null && !xCondition.isBlank()) {
                    v.put("xCondition", xCondition);
                }

                // Add each parameter to the output array.
                values.add(v);
            }
        }
        
        out.set("values", values);
        return out;
    }
    
    // =========================
    // FOLDER REFERENCE
    // =========================
    private ObjectNode transformFolderReference(JsonNode node, ObjectMapper mapper) {

        ObjectNode out = mapper.createObjectNode();

        String surrogate = node.path("Surrogate").asText();
        String name = node.path("Attributes").path("Name").asText();

        out.put("ObjectClass", "TestStepFolderReference");
        out.put("Name", name);
        out.putNull("type");
        out.putNull("op");
        out.put("surrogate", surrogate);

        // 2. Add condition to the output object.
        String condition = node.path("Attributes").path("Condition").asText(null);
        if (condition != null && !condition.isEmpty()) {
            //out.put("condition", condition);
            out.put("condition", normalizeCondition(condition));
        } else {
            out.putNull("condition");
        }
        
        String reusedId = node.path("Assocs").path("ReusedItem").isArray()
                ? node.path("Assocs").path("ReusedItem").get(0).asText()
                : null;

        JsonNode reused = reusableIndex.get(reusedId);

        String reusableName = reused != null
                ? reused.path("Attributes").path("Name").asText(null)
                : null;

        String moduleSurrogate = null;

        if (reused != null) {
            JsonNode modArr = reused.path("Assocs").path("Module");
            if (modArr.isArray() && modArr.size() > 0) {
                moduleSurrogate = modArr.get(0).asText();
            }
        }

        JsonNode module = moduleIndex.get(moduleSurrogate);

        out.put("reusableSurrogate", reusedId);
        out.put("reusableName", reusableName);

        out.put("moduleSurrogate", moduleSurrogate);
        out.put("module",
                module != null ? module.path("Attributes").path("Name").asText(null) : null);

        out.put("moduleClass",
                module != null ? module.path("ObjectClass").asText(null) : null);

        ObjectNode values = mapper.createObjectNode();

        for (JsonNode p : node.path("Parameters")) {

            String key = p.path("ExplicitName").asText();
            if (key.isBlank()) continue;

            ObjectNode v = mapper.createObjectNode();
            v.put("value", p.path("Value").asText());
            v.put("actionMode", "BusinessParameter");

            values.set(key, v);
        }

        out.set("values", values);

        return out;
    }
    private String normalizeCondition(String condition) {
        //if (condition == null || condition.isBlank()) 
        {
            return condition;
        }
        
//        String trimmed = condition.trim();
//        
//        // If the expression is already fully wrapped in parentheses, leave it unchanged.
//        if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
//            return trimmed;
//        }
//        
//        // If the expression contains both AND and OR, additional grouping may be required. 
//        // This preserves operator precedence in the AGATE DSL.
//        if (trimmed.contains(" AND ") && trimmed.contains(" OR ")) {
//            return "(" + trimmed + ")";
//        }
//        
//        return trimmed;
    }
    
    public static String processFile(String inputFile, String tsuFile) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(new File(inputFile));
        JsonNode tsuRoot = loadTsu(tsuFile, mapper);
        

        ToscaParserPhase1 parser = new ToscaParserPhase1();
        parser.indexTsuOrder(tsuRoot);

        parser.indexModules(tsuRoot);
        parser.indexReusables(tsuRoot);

        ArrayNode result = mapper.createArrayNode();

        for (JsonNode node : root) {

            String oc = node.path("ObjectClass").asText();

            if ("TestStepFolder".equals(oc)) {
                continue;
            }

            if ("XTestStep".equals(oc)) {

                JsonNode moduleArr = node.path("Assocs").path("Module");

                if (moduleArr.isArray() && moduleArr.size() > 0) {
                    result.add(parser.transformXTestStep(node, mapper));
                } else {
                    result.add(node);
                }

                continue;
            }

            if ("TestStepFolderReference".equals(oc)) {
                result.add(parser.transformFolderReference(node, mapper));
                continue;
            }

            result.add(node);
        }

        String out = inputFile.replace(".json", "_agate-step1.json");

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(out), result);

        return out;
    }
    
    private void indexTsuOrder(JsonNode tsuRoot) {
        JsonNode entities = tsuRoot.path("Entities");
        int order = 0;
        for (JsonNode node : entities) {
            String surrogate = node.path("Surrogate").asText("");
            if (!surrogate.isBlank()) {
                tsuOrderIndex.put(surrogate, order);
                order++;
            }
        }
        MigrationLog.debug("TSU order indexed: " + tsuOrderIndex.size() + " elements.");
    }   
    

    
    private ObjectNode transformTBoxStartProgramValues(JsonNode parameters, ObjectMapper mapper) {
        ObjectNode values = mapper.createObjectNode();
        
        // 1. Copy parameters into a list so they can be sorted.
        List<JsonNode> paramList = new ArrayList<>();
        if (parameters.isArray()) {
            for (JsonNode p : parameters) {
                paramList.add(p);
            }
        }

        // 2. Sort parameters according to their occurrence order in the TSU file.
        paramList.sort((p1, p2) -> {
            String surr1 = p1.path("ModuleAttributeSurrogate").asText("");
            String surr2 = p2.path("ModuleAttributeSurrogate").asText("");
            
            // Compare surrogate positions according to their TSU occurrence order.
            int order1 = tsuOrderIndex.getOrDefault(surr1, Integer.MAX_VALUE);
            int order2 = tsuOrderIndex.getOrDefault(surr2, Integer.MAX_VALUE);
            
            return Integer.compare(order1, order2);
        });

        // 3. Iterate over sorted parameters and build the output.
        StringBuilder argumentsBuilder = new StringBuilder();

        for (JsonNode p : paramList) {
            String toscaPath = p.path("ToscaPath").asText("");
            String value = p.path("Value").asText("");
            
            if (toscaPath.isBlank()) continue;

            if ("Arguments.Argument".equals(toscaPath)) {
                if (argumentsBuilder.length() > 0) {
                    argumentsBuilder.append(" ");
                }
                argumentsBuilder.append(value);
            } else {
                String finalKey = toscaPath.contains(".") 
                        ? toscaPath.substring(toscaPath.lastIndexOf(".") + 1) 
                        : toscaPath;

                ObjectNode v = mapper.createObjectNode();
                v.put("value", value);
                v.put("actionMode", p.path("ActionMode").asText());
                
                String actionProperty = p.path("ActionProperty").asText(null);
                if (actionProperty != null && !actionProperty.isBlank()) {
                    v.put("actionProperty", actionProperty);
                }
                v.put("toscaPath", toscaPath);

                values.set(finalKey, v);
            }
        }

        if (argumentsBuilder.length() > 0) {
            ObjectNode argsObj = mapper.createObjectNode();
            argsObj.put("value", argumentsBuilder.toString());
            argsObj.put("actionMode", "Insert");
            argsObj.put("toscaPath", "Arguments");
            
            values.set("arguments", argsObj);
        }

        return values;
    }
    private ObjectNode transformTBoxWaitValues(JsonNode parameters, ObjectMapper mapper) {
        ObjectNode values = mapper.createObjectNode();
        
        // 1. Copy parameters into a list so they can be sorted.
        List<JsonNode> paramList = new ArrayList<>();
        if (parameters.isArray()) {
            for (JsonNode p : parameters) {
                paramList.add(p);
            }
        }

        // 2. Sort parameters according to their occurrence order in the TSU file.
        paramList.sort((p1, p2) -> {
            String surr1 = p1.path("ModuleAttributeSurrogate").asText("");
            String surr2 = p2.path("ModuleAttributeSurrogate").asText("");
            
            // Compare surrogate positions according to their TSU occurrence order.
            int order1 = tsuOrderIndex.getOrDefault(surr1, Integer.MAX_VALUE);
            int order2 = tsuOrderIndex.getOrDefault(surr2, Integer.MAX_VALUE);
            
            return Integer.compare(order1, order2);
        });

        // 3. Iterate over sorted parameters and build the output.
        StringBuilder argumentsBuilder = new StringBuilder();

        for (JsonNode p : paramList) {
            String toscaPath = p.path("ToscaPath").asText("");
            String value = p.path("Value").asText("");
            
            if (toscaPath.isBlank()) continue;

            if ("Arguments.Argument".equals(toscaPath)) {
                if (argumentsBuilder.length() > 0) {
                    argumentsBuilder.append(" ");
                }
                argumentsBuilder.append(value);
            } else {
                String finalKey = toscaPath.contains(".") 
                        ? toscaPath.substring(toscaPath.lastIndexOf(".") + 1) 
                        : toscaPath;

                ObjectNode v = mapper.createObjectNode();
                v.put("value", value);
                v.put("actionMode", p.path("ActionMode").asText());
                
                String actionProperty = p.path("ActionProperty").asText(null);
                if (actionProperty != null && !actionProperty.isBlank()) {
                    v.put("actionProperty", actionProperty);
                }
                v.put("toscaPath", toscaPath);

                values.set(finalKey, v);
            }
        }

        if (argumentsBuilder.length() > 0) {
            ObjectNode argsObj = mapper.createObjectNode();
            argsObj.put("value", argumentsBuilder.toString());
            argsObj.put("actionMode", "Insert");
            argsObj.put("toscaPath", "Arguments");
            
            values.set("arguments", argsObj);
        }

        return values;
    }

    private ObjectNode transformOpenCreateJsonValues(JsonNode parameters, ObjectMapper mapper) {
        ObjectNode values = mapper.createObjectNode();
        
        // 1. Copy parameters into a list for sorting.
        List<JsonNode> paramList = new ArrayList<>();
        if (parameters.isArray()) {
            for (JsonNode p : parameters) {
                paramList.add(p);
            }
        }

        // 2. Sort them according to TSU order.
        paramList.sort((p1, p2) -> {
            String surr1 = p1.path("ModuleAttributeSurrogate").asText("");
            String surr2 = p2.path("ModuleAttributeSurrogate").asText("");
            
            int order1 = tsuOrderIndex.getOrDefault(surr1, Integer.MAX_VALUE);
            int order2 = tsuOrderIndex.getOrDefault(surr2, Integer.MAX_VALUE);
            
            return Integer.compare(order1, order2);
        });

        // 3. Map parameters directly into values.
        for (JsonNode p : paramList) {
            String toscaPath = p.path("ToscaPath").asText("");
            if (toscaPath.isBlank()) continue;

            String finalKey = toscaPath.contains(".") 
                    ? toscaPath.substring(toscaPath.lastIndexOf(".") + 1) 
                    : toscaPath;

            ObjectNode v = mapper.createObjectNode();
            v.put("value", p.path("Value").asText());
            v.put("actionMode", p.path("ActionMode").asText());
            
            String actionProperty = p.path("ActionProperty").asText(null);
            if (actionProperty != null && !actionProperty.isBlank()) {
                v.put("actionProperty", actionProperty);
            }
            v.put("toscaPath", toscaPath);

            values.set(finalKey, v);
        }

        return values;
    }
    
    
    private List<JsonNode> getSortedParameters(JsonNode parameters) {
        List<JsonNode> paramList = new ArrayList<>();
        for (JsonNode p : parameters) {
            paramList.add(p);
        }
        
        // Sort according to the order in the TSU file.
        paramList.sort((p1, p2) -> {
            String surr1 = p1.path("ModuleAttributeSurrogate").asText("");
            String surr2 = p2.path("ModuleAttributeSurrogate").asText("");
            
            int order1 = tsuOrderIndex.getOrDefault(surr1, Integer.MAX_VALUE);
            int order2 = tsuOrderIndex.getOrDefault(surr2, Integer.MAX_VALUE);
            
            return Integer.compare(order1, order2);
        });
        
        return paramList;
    }
    
}