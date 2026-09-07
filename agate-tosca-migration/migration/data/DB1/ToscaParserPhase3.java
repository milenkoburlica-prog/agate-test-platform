package at.co.svc.tosca.transformation;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

        // Mapira: "roditeljskaPutanja_imePolja" -> mapiranje ID-ja na indeks
        Map<String, Map<String, Integer>> parentContexts = new HashMap<>();

        // 1. Prvi prolaz: Popunjavanje indeksa (samo za nivoe koji su liste)
        for (JsonNode val : originalValues) {
            String path = val.path("toscaPath").asText("");
            String pathId = val.path("toscaPathID").asText("");
            String[] pParts = path.split("\\.");
            String[] idParts = pathId.split("\\.");

            for (int i = 0; i < pParts.length; i++) {
                // Očisti ime polja od starih indeksa ako ih ima
                String fieldName = pParts[i].replaceAll("\\[\\d+\\]", "");
                
                // Indeksiramo samo ako je to 'quittung' ili 'meldungsdaten' (nivoi koji su liste)
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

     // Dodaj ovo pre "2. Drugi prolaz":
     // --- INICIJALIZACIJA CACHE-A ---
        Map<String, JsonNode> cache519 = new HashMap<>();
        for (JsonNode val : originalValues) {
            if ("519".equals(val.path("actionMode").asText())) {
                String path = val.path("toscaPath").asText("");
                cache519.put(getParentPath(path), val); 
            }
        }
        
        // 2. Drugi prolaz: Kreiranje novih putanja
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
                    // Leaf elementi (id, ungeprueft, strasse) uvek dobijaju [0] 
                    // jer nisu liste, već property-ji objekta
                    sb.append(fieldName).append("[0]");
                }
                
                if (i < p.length - 1) sb.append(".");
            }

            String indexedPath = sb.toString();

            if ("Constraint".equals(actionMode) && "SQL Statement".equals(val.path("name").asText())) {
                ObjectNode c = mapper.createObjectNode();
                String sqlParentPath = getParentPath(path); // Koristi 'path' iz petlje
                
                // 1. Injekcija podataka iz 519
                if (cache519.containsKey(sqlParentPath)) {
                    JsonNode node519 = cache519.get(sqlParentPath);
                    c.put("path", node519.path("name").asText()); 
                    c.put("expected", node519.path("value").asText());
                } else {
                    c.put("path", "");
                    c.put("expected", val.path("value").asText());
                }
                
                c.put("action", "EQUALS");
                c.put("toscaPath", indexedPath);
                constraints.add(c);
                
                // 2. OVO JE BILO KLJUČNO: Dodavanje u newValues
                ObjectNode valCopy = val.deepCopy();
                valCopy.put("toscaPath", indexedPath); // Obavezno setuj toscaPath
                valCopy.set("constrain", c);           // Prikači mu constrain
                newValues.add(valCopy);                // Dodaj ga u listu!
                
            } else
            if ("Constraint".equals(actionMode) || "519".equals(actionMode)) {
                ObjectNode c = mapper.createObjectNode();
                c.put("path", val.path("xmlPath").asText());
                c.put("expected", val.path("value").asText());
                c.put("action", "EQUALS");
                
                int lastDot = indexedPath.lastIndexOf(".");
                c.put("toscaPath", (lastDot != -1) ? indexedPath.substring(0, lastDot) : indexedPath);
                constraints.add(c);
                
             // Dodaj "constrain" objekat direktno u kopiju
                ObjectNode valCopy = val.deepCopy();
                valCopy.set("constrain", c); 
                
                // Obavezno dodaj u newValues
                newValues.add(valCopy);
            } else {
                ObjectNode valCopy = val.deepCopy();
                valCopy.put("toscaPath", indexedPath);
                newValues.add(valCopy);
            }
        }

        newStep.set("values", newValues);
        newStep.set("constraints", constraints);
        
//        enrichValuesWithConstraints(newStep, mapper);
//        cleanPathsAndRemoveConstraints(newStep);
//        applyXConditions(newStep);
        
        return newStep;
    }

    private static String getParentPath(String path) {
        if (path == null || !path.contains(".")) {
            return "root"; // Ili prazan string, zavisno od tvoje logike
        }
        return path.substring(0, path.lastIndexOf("."));
    }
    
//    public static void applyXConditions(ObjectNode stepNode) {
//        ArrayNode values = (ArrayNode) stepNode.path("values");
//        
//        // 1. Pronađi sve Constraint elemente i formiraj mapu: xmlPath -> xCondition
//        Map<String, String> conditions = new HashMap<>();
//        for (JsonNode valNode : values) {
//            ObjectNode val = (ObjectNode) valNode;
//            String actionMode = val.path("actionMode").asText("");
//            
//            if ("Constraint".equals(actionMode)) { // || "519".equals(actionMode)) {
//                String xmlPath = val.path("xmlPath").asText("");
//                String value = val.path("value").asText("");
//                if (!xmlPath.isEmpty() && !value.isEmpty()) {
//                    String condition = value + " != NULL";
//                    conditions.put(xmlPath, condition);
//                }
//            }
//        }
//
//        // 2. Dodaj xCondition elementima i ukloni originalni Constraint element
//        Iterator<JsonNode> it = values.elements();
//        while (it.hasNext()) {
//            ObjectNode val = (ObjectNode) it.next();
//            String currentXmlPath = val.path("xmlPath").asText("");
//            String actionMode = val.path("actionMode").asText("");
//            
//            // Provera da li je trenutni element sam po sebi Constraint
//            boolean isConstraint = "Constraint".equals(actionMode) || "519".equals(actionMode);
//            
//            // Provera da li ovaj element treba da dobije xCondition od nekog od roditelja
//            for (Map.Entry<String, String> entry : conditions.entrySet()) {
//                String constraintXmlPath = entry.getKey();
//                
//                // Ako je putanja elementa počinje sa putanjom constraint-a
//                if (currentXmlPath.startsWith(constraintXmlPath)) {
//                    // Ako nije sam taj Constraint element, dodaj mu xCondition
//                    if (!currentXmlPath.equals(constraintXmlPath)) {
//                        val.put("xCondition", entry.getValue());
//                    }
//                }
//            }
//            
//            // 3. Uklanjanje: Ako je element bio Constraint, brišemo ga iz liste
//            if (isConstraint) {
//                it.remove();
//            }
//        }
//    }

    public static void applyXConditions(ObjectNode stepNode) {
        ArrayNode values = (ArrayNode) stepNode.path("values");
        
        // 1. Pronađi SAMO "519" elemente i formiraj mapu
        Map<String, String> conditions = new HashMap<>();
        for (JsonNode valNode : values) {
            ObjectNode val = (ObjectNode) valNode;
            String actionMode = val.path("actionMode").asText("");
            
            if ("519".equals(actionMode)) { // Menjamo: Constraint više nije ovde
                String xmlPath = val.path("xmlPath").asText("");
                String value = val.path("value").asText("");
                if (!xmlPath.isEmpty() && !value.isEmpty()) {
                    String condition = value + " != NULL";
                    conditions.put(xmlPath, condition);
                }
            }
        }

        // 2. Prođi kroz listu, dodaj xCondition i ukloni SAMO 519
        Iterator<JsonNode> it = values.elements();
        while (it.hasNext()) {
            ObjectNode val = (ObjectNode) it.next();
            String currentXmlPath = val.path("xmlPath").asText("");
            String actionMode = val.path("actionMode").asText("");
            
            // Dodaj xCondition ako je element dete nekog 519 constraint-a
            for (Map.Entry<String, String> entry : conditions.entrySet()) {
                String constraintXmlPath = entry.getKey();
                if (currentXmlPath.startsWith(constraintXmlPath) && !currentXmlPath.equals(constraintXmlPath)) {
                    val.put("xCondition", entry.getValue());
                }
            }
            
            // 3. Uklanjanje: Brišemo SAMO ako je element "519"
            // "Constraint" elementi više ne ulaze u ovaj if, pa ostaju u listi
            if ("519".equals(actionMode)) {
                it.remove();
            }
        }
    }
    
    public static void cleanPathsAndRemoveConstraints(ObjectNode stepNode) {
        ArrayNode values = (ArrayNode) stepNode.path("values");

        for (JsonNode valNode : values) {
            ObjectNode val = (ObjectNode) valNode;

            // 1. Očisti toscaPath
            if (val.has("toscaPath")) {
                val.put("toscaPath", val.get("toscaPath").asText().replaceAll("\\[\\d+\\]", ""));
            }

            // 2. Očisti toscaPathBase ako postoji
            if (val.has("toscaPathBase")) {
                val.put("toscaPathBase", val.get("toscaPathBase").asText().replaceAll("\\[\\d+\\]", ""));
            }

            // 3. Očisti constrain.toscaPath ako postoji
            if (val.has("constrain")) {
                ObjectNode constrain = (ObjectNode) val.get("constrain");
                if (constrain.has("toscaPath")) {
                    constrain.put("toscaPath", constrain.get("toscaPath").asText().replaceAll("\\[\\d+\\]", ""));
                }
            }
        }

        // 4. Ukloni listu constraints
        stepNode.remove("constraints");
    }   
    public static void enrichValuesWithConstraints(ObjectNode stepNode, ObjectMapper mapper) {
        ArrayNode values = (ArrayNode) stepNode.path("values");
        
        
        ArrayNode constraints = (ArrayNode) stepNode.path("constraints");

        for (JsonNode valNode : values) {
            ObjectNode val = (ObjectNode) valNode;
        
         // --- KLJUČNA IZMENA ---
            // Ako je ovo naš SQL Statement, preskoči ga – on već ima svoj constrain
            if ("Constraint".equals(val.path("actionMode").asText()) && 
                "SQL Statement".equals(val.path("name").asText())) {
                continue; 
            }
            // -----------------------
            
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
                            
                            // OVDE VRŠIMO IZMENU: Umesto toscaPath, dodajemo 'path'
                            // Vrednost uzimamo iz originalnog 'c' objekta (xmlPath)
                            constrObj.put("path", c.path("path").asText()); 
                        }
                    }
                }
            }
        }
    }
    

}