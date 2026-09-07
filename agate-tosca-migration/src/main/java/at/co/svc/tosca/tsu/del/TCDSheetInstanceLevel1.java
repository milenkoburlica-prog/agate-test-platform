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

public class TCDSheetInstanceLevel1 {

    // 1. Validacija ID-a
    public static String getUniqueTestSheetId(String level1FilePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(level1FilePath));
        Set<String> foundIds = new HashSet<>();

        if (root.isArray()) {
            for (JsonNode node : root) {
                JsonNode testSheetNode = node.at("/Assocs/TestSheet");
                if (testSheetNode.isArray()) {
                    for (JsonNode idNode : testSheetNode) {
                        if (!idNode.asText().isEmpty()) foundIds.add(idNode.asText());
                    }
                }
            }
        }
        if (foundIds.size() > 1) throw new Exception("Greška: Pronađeno više TestSheet ID-eva: " + foundIds);
        return foundIds.isEmpty() ? null : foundIds.iterator().next();
    }

    // 2. Glavna logika za generisanje fajla
    public static void generateInstancesJson(
            String validatedTestSheetId, 
            String originalTsuJsonPath, 
            String outputFilePath
    ) throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        JsonNode root = mapper.readTree(new File(originalTsuJsonPath));
        JsonNode entities = root.path("Entities");

        Map<String, JsonNode> entityIndex = new java.util.HashMap<>();
        for (JsonNode node : entities) {
            String surrogate = node.path("Surrogate").asText();
            if (!surrogate.isEmpty()) {
                entityIndex.put(surrogate, node);
            }
        }

        JsonNode targetTdInstances = null;
        for (JsonNode node : entityIndex.values()) {
            if ("TDInstances".equals(node.path("ObjectClass").asText())) {
                JsonNode definingItem = node.at("/Assocs/DefiningItem");
                for (JsonNode item : definingItem) {
                    if (validatedTestSheetId.equals(item.asText())) {
                        targetTdInstances = node;
                        break;
                    }
                }
            }
            if (targetTdInstances != null) break;
        }

        if (targetTdInstances == null) {
            throw new Exception("Nije pronađen TDInstances za ID: " + validatedTestSheetId);
        }

        ArrayNode finalInstances = mapper.createArrayNode();
        JsonNode items = targetTdInstances.at("/Assocs/Items");
        
        int counter = 1;
        for (JsonNode instSurrNode : items) {
            String instanceSurrogate = instSurrNode.asText();
            
            JsonNode instanceNode = entityIndex.get(instanceSurrogate);
            String instanceName = (instanceNode != null) 
                                  ? instanceNode.path("Attributes").path("Name").asText("Unknown") 
                                  : "Unknown";

            ObjectNode instanceObj = mapper.createObjectNode();
            instanceObj.put("insatanceSurrogate", instanceSurrogate);
            instanceObj.put("TC_NO", String.valueOf(counter++));
            instanceObj.put("Name", instanceName);
            instanceObj.put("testSheetSurrogate", validatedTestSheetId);
            
            instanceObj.set("TDInstance", mapper.createObjectNode()); 
            
            finalInstances.add(instanceObj);
        }

        mapper.writeValue(new File(outputFilePath), finalInstances);
        System.out.println("✔ [TCD-InstanceLevel1] Uspešno kreiran sa imenima instanci: " + outputFilePath);
    }
 

    public static void populateInstanceValues(String inputInstancesFilePath, String outputFilePath, String originalTsuJsonPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        
        ArrayNode instances = (ArrayNode) mapper.readTree(new File(inputInstancesFilePath));
        
        JsonNode root = mapper.readTree(new File(originalTsuJsonPath));
        Map<String, JsonNode> index = new java.util.HashMap<>();
        for (JsonNode n : root.path("Entities")) index.put(n.path("Surrogate").asText(), n);

        for (JsonNode instNode : instances) {
            String instSurrogate = instNode.path("insatanceSurrogate").asText();
            JsonNode tdInstance = index.get(instSurrogate);
            ArrayNode parameters = mapper.createArrayNode();

            if (tdInstance != null) {
                JsonNode values = tdInstance.at("/Assocs/Values");
                for (JsonNode valSurrogateNode : values) {
                    String valSurrogate = valSurrogateNode.asText();
                    JsonNode valObj = index.get(valSurrogate);
                    
                    if (valObj != null && "TDInstanceValue".equals(valObj.path("ObjectClass").asText())) {
                        JsonNode elementNode = valObj.at("/Assocs/Element");
                        if (elementNode.isArray() && elementNode.size() > 0) {
                            String attrSurrogate = elementNode.get(0).asText();
                            JsonNode attrObj = index.get(attrSurrogate);
                            
                            if (attrObj != null) {
                                ObjectNode param = mapper.createObjectNode();
                                param.put("Parameter.Surrogate", attrSurrogate);
                                
                                // === REKURZIVNO ODREĐIVANJE PUNOG IMENA (ZAMENA JE OVDE) ===
                                String attrName = attrObj.path("Attributes").path("Name").asText();
                                JsonNode currentParentItem = attrObj.at("/Assocs/ParentItem");
                                
                                while (currentParentItem.isArray() && currentParentItem.size() > 0) {
                                    String parentSurr = currentParentItem.get(0).asText();
                                    JsonNode parentObj = index.get(parentSurr);
                                    
                                    if (parentObj != null) {
                                        String parentName = parentObj.path("Attributes").path("Name").asText();
                                        
                                        if (parentName != null && !parentName.trim().isEmpty()) {
                                            attrName = parentName + "." + attrName;
                                        }
                                        // Pomeri se jedan nivo iznad, ka sledećem roditelju
                                        currentParentItem = parentObj.at("/Assocs/ParentItem");
                                    } else {
                                        break; 
                                    }
                                }
                                // ========================================================
                                
                                param.put("Parameter.Name", attrName);
                                param.put("Parameter.Value", valObj.path("Attributes").path("Value").asText());
                                parameters.add(param);
                            }
                        }
                    }
                }
            }
            ((ObjectNode) instNode).set("TDInstance", parameters);
        }
        
        mapper.writeValue(new File(outputFilePath), instances);
        System.out.println("✔ [TCD-InstanceLevel2] Generisan sa hijerarhijskim imenima: " + outputFilePath);
    }
    
    
    public static void populateSubParameters(String inputFilePath, String outputFilePath, String originalTsuJsonPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode instances = (ArrayNode) mapper.readTree(new File(inputFilePath));
        
        JsonNode root = mapper.readTree(new File(originalTsuJsonPath));
        Map<String, JsonNode> index = new java.util.HashMap<>();
        for (JsonNode n : root.path("Entities")) {
            index.put(n.path("Surrogate").asText(), n);
        }

        for (JsonNode instNode : instances) {
            String instSurr = instNode.path("insatanceSurrogate").asText();
            JsonNode originalTdInstance = index.get(instSurr);
            
            if (originalTdInstance == null) continue;

            ArrayNode parameters = (ArrayNode) instNode.path("TDInstance");

            for (JsonNode param : parameters) {
                String paramSurrogate = param.path("Parameter.Surrogate").asText();
                ObjectNode paramObj = (ObjectNode) param;

                for (JsonNode valSurr : originalTdInstance.path("Assocs").path("Values")) {
                    JsonNode valObj = index.get(valSurr.asText());
                    
                    if (valObj != null && "TDInstanceValue".equals(valObj.path("ObjectClass").asText())) {
                        JsonNode element = valObj.at("/Assocs/Element");
                        
                        if (element.isArray() && element.size() > 0 && element.get(0).asText().equals(paramSurrogate)) {
                            
                            if (valObj.has("Assocs") && valObj.path("Assocs").has("ValueInstance")) {
                                String valInstanceSurr = valObj.path("Assocs").path("ValueInstance").get(0).asText();
                                JsonNode targetInstance = index.get(valInstanceSurr);
                                
                                if (targetInstance != null) {
                                    String nameValue = targetInstance.path("Attributes").path("Name").asText();
                                    paramObj.put("Parameter.Value", nameValue);
                                }
                            }

                            if (valObj.has("Assocs") && valObj.path("Assocs").has("ValueInstance")) {
                                ArrayNode subParameters = mapper.createArrayNode();
                                
                                for (JsonNode subValSurr : valObj.path("Assocs").path("ValueInstance")) {
                                    JsonNode subInstance = index.get(subValSurr.asText());
                                    
                                    if (subInstance != null) {
                                        for (JsonNode subV : subInstance.path("Assocs").path("Values")) {
                                            JsonNode subValObj = index.get(subV.asText());
                                            if (subValObj != null) {
                                                ObjectNode subP = mapper.createObjectNode();
                                                
                                                subP.put("SubParameter.Surrogate", subValSurr.asText());
                                                
                                                JsonNode subElem = subValObj.at("/Assocs/Element");
                                                if (subElem.isArray() && subElem.size() > 0) {
                                                    JsonNode attrObj = index.get(subElem.get(0).asText());
                                                    if (attrObj != null) {
                                                        subP.put("SubParameter.Name", attrObj.path("Attributes").path("Name").asText());
                                                    }
                                                }
                                                
                                                subP.put("SubParameter.Value", subValObj.path("Attributes").path("Value").asText());
                                                subParameters.add(subP);
                                            }
                                        }
                                    }
                                }
                                paramObj.set("SubParameters", subParameters);
                            }
                        }
                    }
                }
            }
        }
        mapper.writeValue(new File(outputFilePath), instances);
        System.out.println("✔ [TCD-InstanceLevel3] Uspešno popunjeno sa CL vrednostima i SubParameters.");
    }
    
    

    public static void generateLevel4Instances(String inputFilePath, String outputFilePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        ArrayNode instances = (ArrayNode) mapper.readTree(new File(inputFilePath));
        ArrayNode flatInstances = mapper.createArrayNode();

        for (JsonNode instNode : instances) {
            ObjectNode newInstNode = mapper.createObjectNode();
            newInstNode.put("insatanceSurrogate", instNode.path("insatanceSurrogate").asText());
            newInstNode.put("TC_NO", instNode.path("TC_NO").asText());
            newInstNode.put("Name", instNode.path("Name").asText());
            newInstNode.put("testSheetSurrogate", instNode.path("testSheetSurrogate").asText());

            ArrayNode flatParams = mapper.createArrayNode();

            for (JsonNode param : instNode.path("TDInstance")) {
                ObjectNode mainParam = mapper.createObjectNode();
                mainParam.put("Parameter.Surrogate", param.path("Parameter.Surrogate").asText());
                mainParam.put("Parameter.Name", param.path("Parameter.Name").asText());
                mainParam.put("Parameter.Value", param.path("Parameter.Value").asText());
                flatParams.add(mainParam);

                if (param.has("SubParameters") && param.path("SubParameters").isArray()) {
                    String parentName = param.path("Parameter.Name").asText();
                    
                    for (JsonNode subP : param.path("SubParameters")) {
                        ObjectNode newSubParam = mapper.createObjectNode();
                        newSubParam.put("Parameter.Surrogate", subP.path("SubParameter.Surrogate").asText());
                        newSubParam.put("Parameter.Name", parentName + "." + subP.path("SubParameter.Name").asText());
                        newSubParam.put("Parameter.Value", subP.path("SubParameter.Value").asText());
                        
                        flatParams.add(newSubParam);
                    }
                }
            }
            
            newInstNode.set("TDInstance", flatParams);
            flatInstances.add(newInstNode);
        }

        mapper.writeValue(new File(outputFilePath), flatInstances);
        System.out.println("✔ [TCD-InstanceLevel4] Kreiran 'pravolinijski' JSON: " + outputFilePath);
    }
    
    
 // NOVA POMOĆNA METODA: Čita krovnu strukturu iz level0 fajla da bismo imali sve kolone/redove
 // POMOĆNA METODA SA LOGOVIMA
    private static List<String> loadAllAttributesFromLevel0(String inputFilePath) {
        List<String> allAttributes = new ArrayList<>();
        try {
            File level4File = new File(inputFilePath);
            String parentDir = level4File.getParent();
            String name = level4File.getName(); // npr. "3a0c85e8-abda-45fb-0638-58d4f31dc775_level4Istances.json"
            
            // Izvačimo GUID (sve pre prvog donje crte)
            String guid = name.split("_")[0];
            
            // Eksplicitno sklapamo putanju do level0 fajla koji nam treba
            String level0Path = parentDir + File.separator + guid + "_tcdsheets-rowdata-level0.json";
            
            System.out.println("[DEBUG-Level5] USPEŠNA REKONSTRUKCIJA! Tražim level0 na: " + level0Path);
            File level0File = new File(level0Path);
            
            if (level0File.exists()) {
                System.out.println("[DEBUG-Level5] Fajl uspešno pronađen! Čitam atribute...");
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(level0File);
                if (root.isArray()) {
                    for (JsonNode attrNode : root) {
                        String attrName = attrNode.path("Attributes").path("Name").asText();
                        if (!attrName.isEmpty()) {
                            allAttributes.add(attrName);
                            System.out.println("[DEBUG-Level5] -> Pronađen atribut u strukturi: " + attrName);
                        }
                    }
                }
                System.out.println("[DEBUG-Level5] Ukupno učitano atributa iz strukture: " + allAttributes.size());
            } else {
                System.out.println("[⚠ ERROR-Level5] Fajl NE POSTOJI na putanji: " + level0File.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("[⚠ ERROR-Level5] Greška tokom čitanja level0 fajla: " + e.getMessage());
            e.printStackTrace();
        }
        return allAttributes;
    }
    
    
    public static void generateLevel5MatrixCSV(String inputFilePath, String csvFilePath, String appName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode instances = (ArrayNode) mapper.readTree(new java.io.File(inputFilePath));

        Map<String, Map<String, String>> matrix = new LinkedHashMap<>();
        List<String> testCaseNames = new ArrayList<>();

        System.out.println("\n=== [DEBUG-Level5] START GENERATE CSV MATRIX ===");
        System.out.println("[DEBUG-Level5] Ulazni fajl (Level 4): " + inputFilePath);
        System.out.println("[DEBUG-Level5] Izlazni CSV: " + csvFilePath);

        // 1. Punjenje matrice svim atributima iz krovne strukture
        List<String> allStructureAttributes = loadAllAttributesFromLevel0(inputFilePath);
        for (String attrName : allStructureAttributes) {
            matrix.put(attrName, new LinkedHashMap<>());
        }
        System.out.println("[DEBUG-Level5] Matrica inicijalizovana sa " + matrix.size() + " osnovnih redova iz strukture.");

        // 2. Punjenje vrednosti iz instanci
        for (JsonNode inst : instances) {
            String tcName = inst.path("Name").asText();
            testCaseNames.add(tcName);

            for (JsonNode param : inst.path("TDInstance")) {
                String pName = param.path("Parameter.Name").asText();
                String pValue = param.path("Parameter.Value").asText()
                                    .replaceAll("[\\r\\n\\t]+", " ")
                                    .replace(";", ",");

                if (!matrix.containsKey(pName)) {
                    System.out.println("[DEBUG-Level5] -> Atribut iz instance nije bio u Level0 strukturi, dodajem ga naknadno: " + pName);
                }
                matrix.putIfAbsent(pName, new LinkedHashMap<>());
                matrix.get(pName).put(tcName, pValue);
            }
        }
        
        System.out.println("[DEBUG-Level5] Finalni broj redova u matrici pre upisa u CSV: " + matrix.size());
        System.out.println("[DEBUG-Level5] Da li matrica sadrži 'VPNR-Karte'? -> " + matrix.containsKey("VPNR-Karte"));

        // 3. Kreiranje liste fajlova za upis
        List<File> targetFiles = new ArrayList<>();
        targetFiles.add(new File(csvFilePath));

        File originalCsvFile = new File(csvFilePath);
        String fileName = originalCsvFile.getName();
        
        String templateFolderPath = System.getProperty("user.dir") + File.separator 
                                    + "migration" + File.separator 
                                    + "data" + File.separator 
                                    + appName + File.separator 
                                    + "template";
        
        File templateDir = new File(templateFolderPath);
        if (!templateDir.exists()) {
            templateDir.mkdirs();
        }
        targetFiles.add(new File(templateDir, fileName));

        // 4. Upis u fajlove
        for (File targetFile : targetFiles) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
                writer.print("Parameter Name");
                for (String tc : testCaseNames) {
                    writer.print(";" + tc);
                }
                writer.println();

                for (Map.Entry<String, Map<String, String>> entry : matrix.entrySet()) {
                    writer.print(entry.getKey());
                    for (String tc : testCaseNames) {
                        writer.print(";" + entry.getValue().getOrDefault(tc, ""));
                    }
                    writer.println();
                }
            }
            System.out.println("✔ [TCD-MatrixLevel5] Generisan CSV: " + targetFile.getAbsolutePath());
        }
        System.out.println("=== [DEBUG-Level5] END GENERATE CSV MATRIX ===\n");
    }
    
}