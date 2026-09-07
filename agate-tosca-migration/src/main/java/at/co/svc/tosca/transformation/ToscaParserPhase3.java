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

    public static String processFile(String inputFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(inputFile));

        ObjectNode output = mapper.createObjectNode();
        output.put("name", root.path("name").asText());
        output.put("surrogate", root.path("surrogate").asText());

        ArrayNode stepsOut = mapper.createArrayNode();
        JsonNode steps = root.path("steps");

        if (steps.isArray()) {
            for (JsonNode stepNode : steps) {
                stepsOut.add(transformStepPhase3(stepNode, mapper));
            }
        }
        output.set("steps", stepsOut);

        String outFile = inputFile.replace("_agate-step2.json", "_agate-step3.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outFile), output);
        return outFile;
    }

    public static ObjectNode transformStepPhase3(JsonNode stepNode, ObjectMapper mapper) {
        ObjectNode newStep = stepNode.deepCopy();
        ArrayNode originalValues = (ArrayNode) stepNode.path("values");

        // Maps "parentPath_fieldName" to an ID-to-index mapping.
        Map<String, Map<String, Integer>> parentContexts = new HashMap<>();

        // 1. First pass: build indexes for list-like levels.
        for (JsonNode val : originalValues) {
            String path = val.path("toscaPath").asText("");
            String pathId = val.path("toscaPathID").asText("");
            String[] pParts = path.split("\\.");
            String[] idParts = pathId.split("\\.");

            for (int i = 0; i < pParts.length; i++) {
                // Remove old indexes from the field name.
                String fieldName = pParts[i].replaceAll("\\[\\d+\\]", "");
                
                // Index only 'quittung' and 'meldungsdaten' because they represent list levels.
                if (fieldName.equals("quittung") || fieldName.equals("meldungsdaten")) {
                    String parentPath = i == 0 ? "root" : String.join(".", java.util.Arrays.copyOf(pParts, i));
                    String key = parentPath + "_" + fieldName;
                    String id = i < idParts.length ? idParts[i] : "default";

                    parentContexts.putIfAbsent(key, new HashMap<>());
                    Map<String, Integer> idMap = parentContexts.get(key);
                    if (!idMap.containsKey(id)) {
                        idMap.put(id, idMap.size());
                    }
                }
            }
        }

        // 2. Second pass: build the new indexed paths.
        ArrayNode newValues = mapper.createArrayNode();
        ArrayNode constraints = mapper.createArrayNode();

        for (JsonNode val : originalValues) {
            String path = val.path("toscaPath").asText("");
            String pathId = val.path("toscaPathID").asText("");
            String actionMode = val.path("actionMode").asText("");

            String[] p = path.split("\\.");
            String[] id = pathId.split("\\.");
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < p.length; i++) {
                String fieldName = p[i].replaceAll("\\[\\d+\\]", "");
                
                if (fieldName.equals("quittung") || fieldName.equals("meldungsdaten")) {
                    String parentPath = i == 0 ? "root" : String.join(".", java.util.Arrays.copyOf(p, i));
                    String key = parentPath + "_" + fieldName;
                    String currentId = i < id.length ? id[i] : "default";
                    int idx = parentContexts.get(key).get(currentId);
                    sb.append(fieldName).append("[").append(idx).append("]");
                } else {
                    // Leaf elements always receive index [0]. 
                    // They are object properties rather than lists.
                    sb.append(fieldName).append("[0]");
                }
                
                if (i < p.length - 1) sb.append(".");
            }

            String indexedPath = sb.toString();

            if ("Constraint".equals(actionMode) || "519".equals(actionMode)) {
                ObjectNode c = mapper.createObjectNode();
                c.put("path", val.path("xmlPath").asText());
                c.put("expected", val.path("value").asText());
                c.put("action", "EQUALS");
                
                int lastDot = indexedPath.lastIndexOf(".");
                c.put("toscaPath", (lastDot != -1) ? indexedPath.substring(0, lastDot) : indexedPath);
                constraints.add(c);
                
             // Add the "constrain" object directly to the copied value.
                ObjectNode valCopy = val.deepCopy();
                valCopy.set("constrain", c); 
                
                // Add the copied value to newValues.
                newValues.add(valCopy);
            } else {
                ObjectNode valCopy = val.deepCopy();
                valCopy.put("toscaPath", indexedPath);
                newValues.add(valCopy);
            }
        }

        newStep.set("values", newValues);
        newStep.set("constraints", constraints);
        
        enrichValuesWithConstraints(newStep, mapper);
        cleanPathsAndRemoveConstraints(newStep);
        applyXConditions(newStep);
        
        return newStep;
    }

    public static void applyXConditions(ObjectNode stepNode) {
        String moduleName = stepNode.path("module").asText("");
        if ("TBox DB Expert module".equals(moduleName)) {
            return; // Ignore this step and exit the method.
        }
        
        ArrayNode values = (ArrayNode) stepNode.path("values");
        
        // 1. Find all Constraint elements and build an xmlPath -> xCondition map.
        Map<String, String> conditions = new HashMap<>();
        for (JsonNode valNode : values) {
            ObjectNode val = (ObjectNode) valNode;
            String actionMode = val.path("actionMode").asText("");
            
            if ("Constraint".equals(actionMode) || "519".equals(actionMode)) {
                String xmlPath = val.path("xmlPath").asText("");
                String value = val.path("value").asText("");
                String name = val.path("name").asText(""); // Keep the name for diagnostics.
                
                // Diagnostic trace.
                MigrationLog.debug("Constraint found: name=[" + name + "], value=[" + value + "], xmlPath=[" + xmlPath + "]");
                
                if (!xmlPath.isEmpty() && !value.isEmpty()) {
                    // Determine whether the value should be treated as a variable expression.
                    // Skip "{NULL}" because it does not represent a condition.
                    if (!value.equalsIgnoreCase("{NULL}")) {
                        String condition = value + " != NULL";
                        conditions.put(xmlPath, condition);
                    }
                }
            }
            
        
        }

        // 2. Apply xCondition to matching elements and remove the original Constraint element.
        Iterator<JsonNode> it = values.elements();
        while (it.hasNext()) {
            ObjectNode val = (ObjectNode) it.next();
            String currentXmlPath = val.path("xmlPath").asText("");
            String actionMode = val.path("actionMode").asText("");
            
            // Check whether the current element itself is a Constraint.
            boolean isConstraint = "Constraint".equals(actionMode) || "519".equals(actionMode);
            
            // Check whether this element should inherit xCondition from a parent constraint.
            for (Map.Entry<String, String> entry : conditions.entrySet()) {
                String constraintXmlPath = entry.getKey();
                
                // Check whether the element path starts with the constraint path.
                if (currentXmlPath.startsWith(constraintXmlPath)) {
                    // Apply xCondition when this is not the Constraint element itself.
                    if (!currentXmlPath.equals(constraintXmlPath)) {
                        MigrationLog.debug("Applied xCondition: " + entry.getValue());
                        val.put("xCondition", entry.getValue());
                    }
                }
            }
            
            // 3. Remove the element when it was a Constraint.
            if (isConstraint) {
                it.remove();
            }
        }
    }
    
    public static void cleanPathsAndRemoveConstraints(ObjectNode stepNode) {
        ArrayNode values = (ArrayNode) stepNode.path("values");

        for (JsonNode valNode : values) {
            ObjectNode val = (ObjectNode) valNode;

            // 1. Clean toscaPath.
            if (val.has("toscaPath")) {
                val.put("toscaPath", val.get("toscaPath").asText().replaceAll("\\[\\d+\\]", ""));
            }

            // 2. Clean toscaPathBase when present.
            if (val.has("toscaPathBase")) {
                val.put("toscaPathBase", val.get("toscaPathBase").asText().replaceAll("\\[\\d+\\]", ""));
            }

            // 3. Clean constrain.toscaPath when present.
            if (val.has("constrain")) {
                ObjectNode constrain = (ObjectNode) val.get("constrain");
                if (constrain.has("toscaPath")) {
                    constrain.put("toscaPath", constrain.get("toscaPath").asText().replaceAll("\\[\\d+\\]", ""));
                }
            }
        }

        // 4. Remove the constraints array.
        stepNode.remove("constraints");
    }   
    public static void enrichValuesWithConstraints(ObjectNode stepNode, ObjectMapper mapper) {
        ArrayNode values = (ArrayNode) stepNode.path("values");
        ArrayNode constraints = (ArrayNode) stepNode.path("constraints");

        for (JsonNode valNode : values) {
            ObjectNode val = (ObjectNode) valNode;
            String valPath = val.path("toscaPath").asText();

            for (JsonNode c : constraints) {
                String cPath = c.path("toscaPath").asText();
                
                int lastDot = cPath.lastIndexOf(".");
                if (lastDot != -1) {
                    String parentPath = cPath.substring(0, lastDot);
                    
                    if (valPath.startsWith(parentPath)) {
                        val.put("toscaPathBase", parentPath);
                        
                        if (!"{NULL}".equals(c.path("expected").asText())) {
                            ObjectNode constrObj = val.putObject("constrain");
                            constrObj.put("expected", c.path("expected").asText());
                            constrObj.put("action", c.path("action").asText());
                            
                            // Store the XML path in 'path' instead of toscaPath.
                            // Use the value from the original constraint object.
                            constrObj.put("path", c.path("path").asText()); 
                        }
                    }
                }
            }
        }
    }
    

}