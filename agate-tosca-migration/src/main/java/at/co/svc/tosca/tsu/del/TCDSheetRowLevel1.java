package at.co.svc.tosca.tsu.del;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TCDSheetRowLevel1 {

    public static void generateRowDataLevel1(
            String level0FilePath, 
            String outputFilePath, 
            String originalTsuJsonPath
    ) throws Exception {
        
        System.out.println("DEBUG: Pokušavam da kreiram fajl na: " + new File(outputFilePath).getAbsolutePath());
        
        ObjectMapper mapper = new ObjectMapper();
        Map<String, JsonNode> polazniTsuIndex = new HashMap<>();
        
        // 1. Indeksiranje polaznog TSU JSON-a
        File tsuJsonFile = new File(originalTsuJsonPath);
        if (tsuJsonFile.exists()) {
            JsonNode rootNode = mapper.readTree(tsuJsonFile);
            JsonNode entitiesNode = rootNode.path("Entities");
            if (entitiesNode.isArray()) {
                for (JsonNode node : entitiesNode) {
                    String surrogate = node.path("Surrogate").asText();
                    if (!surrogate.isEmpty()) {
                        polazniTsuIndex.put(surrogate, node);
                    }
                }
                System.out.println("[TCD-Level1] Uspešno indeksirano " + polazniTsuIndex.size() + " entiteta.");
            }
        } else {
            System.err.println("[TCD-Level1] Greška: Polazni JSON ne postoji: " + originalTsuJsonPath);
            return;
        }

        // 2. Čitanje Level 0 fajla
        File level0File = new File(level0FilePath);
        if (!level0File.exists()) {
            System.err.println("[TCD-Level1] Greška: Level0 fajl ne postoji: " + level0FilePath);
            return;
        }

        JsonNode level0Root = mapper.readTree(level0File);
        List<JsonNode> processingList = new ArrayList<>();
        if (level0Root.isArray()) {
            for (JsonNode item : level0Root) {
                processingList.add(item.deepCopy());
            }
        }

        int totalExpandedCount = 0;
        boolean hasChanges = true;

        // Glavna petlja za rekurzivnu ekspanziju uz očuvanje redosleda
        while (hasChanges) {
            hasChanges = false;

            for (int i = 0; i < processingList.size(); i++) {
                JsonNode currentItem = processingList.get(i);
                String surrogate = currentItem.path("Surrogate").asText();
                String currentName = currentItem.path("Attributes").path("Name").asText();
                
                JsonNode originalNode = polazniTsuIndex.get(surrogate);
                if (originalNode == null) {
                    continue; 
                }

                JsonNode refClassNode = originalNode.path("Assocs").path("ReferencedClass");
                JsonNode ownClassNode = originalNode.path("Assocs").path("OwningClass");

                List<String> childrenSurrogates = new ArrayList<>();
                boolean hasSubStructure = false;

                // Slučaj A: Ekspanzija preko ReferencedClass
                if (refClassNode.isArray() && refClassNode.size() > 0) {
                    String classSurrogate = refClassNode.get(0).asText();
                    JsonNode classNode = polazniTsuIndex.get(classSurrogate);
                    if (classNode != null && "TDClass".equals(classNode.path("ObjectClass").asText())) {
                        JsonNode classAttributes = classNode.path("Assocs").path("Attributes");
                        if (classAttributes.isArray()) {
                            for (JsonNode attrNode : classAttributes) {
                                childrenSurrogates.add(attrNode.asText());
                            }
                            hasSubStructure = true;
                        }
                    }
                } 
                // Slučaj B: Ekspanzija preko OwningClass
                else if (ownClassNode.isArray() && ownClassNode.size() > 0) {
                    JsonNode itemsNode = originalNode.path("Assocs").path("Items");
                    if (itemsNode.isArray() && itemsNode.size() > 0) {
                        for (JsonNode itemNode : itemsNode) {
                            childrenSurrogates.add(itemNode.asText());
                        }
                        hasSubStructure = true;
                    }
                }

                // --- SCENARIO 1: ELEMENT IMA DECU (EKSPANZIJA) ---
                if (hasSubStructure && !childrenSurrogates.isEmpty()) {
                    System.out.println("[EKSPANZIJA] Razbijam element: " + currentName);
                    
                    JsonNode currentTestSheet = currentItem.path("Assocs").path("TestSheet");
                    
                    List<JsonNode> generatedChildren = new ArrayList<>();

                    for (String childSurr : childrenSurrogates) {
                        JsonNode targetAttrNode = polazniTsuIndex.get(childSurr);
                        
                        if (targetAttrNode != null && "TDAttribute".equals(targetAttrNode.path("ObjectClass").asText())) {
                            String childName = targetAttrNode.path("Attributes").path("Name").asText();
                            String fullPathName = currentName + "." + childName;

                            ObjectNode cleanAttribute = mapper.createObjectNode();
                            cleanAttribute.put("ObjectClass", "TDAttribute");
                            cleanAttribute.put("Surrogate", childSurr);
                            
                            ObjectNode attrs = mapper.createObjectNode();
                            attrs.put("Name", fullPathName);
                            cleanAttribute.set("Attributes", attrs);
                            
                            ObjectNode assocs = mapper.createObjectNode();
                            
                            // 1. TestSheet (Propagacija sa roditelja ili direktno čitanje)
                            if (currentTestSheet.isArray() && currentTestSheet.size() > 0) {
                                assocs.set("TestSheet", currentTestSheet.deepCopy());
                            } else {
                                JsonNode origTestSheet = targetAttrNode.path("Assocs").path("TestSheet");
                                assocs.set("TestSheet", origTestSheet.isArray() ? origTestSheet.deepCopy() : mapper.createArrayNode());
                            }
                            
                            // 2. ParentItem (Uzima se direktno iz originalnog čvora deteta u TSU-u)
                            JsonNode origParent = targetAttrNode.path("Assocs").path("ParentItem");
                            assocs.set("ParentItem", origParent.isArray() ? origParent.deepCopy() : mapper.createArrayNode());
                            
                            // 3. OwningClass (Uzima se direktno iz originalnog čvora deteta u TSU-u)
                            JsonNode origOwning = targetAttrNode.path("Assocs").path("OwningClass");
                            assocs.set("OwningClass", origOwning.isArray() ? origOwning.deepCopy() : mapper.createArrayNode());

                            // 4. Values (Direktne vrednosti pod-atributa) - OSIGURANJE OD NESTAJANJA
                            JsonNode valuesNode = targetAttrNode.path("Assocs").path("Values");
                            ArrayNode cleanValuesArray = mapper.createArrayNode();

                            if (valuesNode.isArray() && valuesNode.size() > 0) {
                                for (JsonNode val : valuesNode) {
                                    cleanValuesArray.add(val.asText());
                                }
                            } else {
                                String instanceName = findInstanceName(mapper, polazniTsuIndex, valuesNode); 
                                if (instanceName != null && !instanceName.isEmpty()) {
                                    cleanValuesArray.add(instanceName);
                                } else {
                                    // Spasavamo VPNR-Karte i slične prazne parametre u strukturi
                                    cleanValuesArray.add(""); 
                                    System.out.println("[INFO] Sačuvan prazan strukturalni parametar: " + fullPathName);
                                }
                            }
                            assocs.set("Values", cleanValuesArray);
                            
                            cleanAttribute.set("Assocs", assocs);
                            generatedChildren.add(cleanAttribute);
                        }
                    }

                    if (!generatedChildren.isEmpty()) {
                        processingList.remove(i);
                        processingList.addAll(i, generatedChildren);
                        totalExpandedCount++;
                        hasChanges = true;
                        i--; 
                    }
                } 
                // --- SCENARIO 2: RAVAN ELEMENT (NEMA POD-STRUKTURU) ---
                else {
                    ObjectNode writableItem = (ObjectNode) currentItem;
                    
                    // Provera: Ako element već ima formiran Assocs sa Values iz Level0, nemoj ga gaziti praznim vrednostima!
                    if (!writableItem.has("Assocs") || writableItem.path("Assocs").path("Values").size() == 0) {
                        ObjectNode assocs = mapper.createObjectNode();
                        
                        // 1. TestSheet
                        JsonNode origTestSheet = originalNode.path("Assocs").path("TestSheet");
                        assocs.set("TestSheet", origTestSheet.isArray() ? origTestSheet.deepCopy() : mapper.createArrayNode());
                        
                        // 2. ParentItem
                        JsonNode origParent = originalNode.path("Assocs").path("ParentItem");
                        assocs.set("ParentItem", origParent.isArray() ? origParent.deepCopy() : mapper.createArrayNode());
                        
                        // 3. OwningClass
                        JsonNode origOwning = originalNode.path("Assocs").path("OwningClass");
                        assocs.set("OwningClass", origOwning.isArray() ? origOwning.deepCopy() : mapper.createArrayNode());

                        // 4. Values - Prvo pogledaj da li već imamo vrednosti iz Level0
                        JsonNode existingValues = currentItem.path("Assocs").path("Values");
                        ArrayNode cleanValuesArray = mapper.createArrayNode();
                        
                        if (existingValues.isArray() && existingValues.size() > 0) {
                            cleanValuesArray.addAll((ArrayNode) existingValues);
                        } else {
                            // Ako nema, tek onda uzmi iz originalnog čvora - OSIGURANJE ZA RAVNE ELEMENTE (npr. SVNR)
                            JsonNode origValues = originalNode.path("Assocs").path("Values");
                            if (origValues.isArray() && origValues.size() > 0) {
                                for (JsonNode val : origValues) {
                                    cleanValuesArray.add(val.asText());
                                }
                            } else {
                                // Ako je i u originalu prazan, forsiramo prazan string da ne nestane ključ
                                cleanValuesArray.add("");
                            }
                        }
                        assocs.set("Values", cleanValuesArray);
                        writableItem.set("Assocs", assocs);
                    }
                }
                
            }
        }

        // 3. Upisivanje u izlazni fajl
        ArrayNode level1Result = mapper.createArrayNode();
        for (JsonNode finalItem : processingList) {
            level1Result.add(finalItem);
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFilePath), level1Result);
        System.out.println("✔ [TCD-Level1] Kompletan izvoz završen. Sadrži TestSheet, ParentItem, OwningClass i Values. Putanja: " + outputFilePath);
    }
    
    private static String findInstanceName(ObjectMapper mapper, Map<String, JsonNode> polazniTsuIndex, JsonNode valuesNode) {
        if (valuesNode.isArray() && valuesNode.size() > 0) {
            String valueSurrogate = valuesNode.get(0).asText();
            JsonNode valNode = polazniTsuIndex.get(valueSurrogate);
            
            if (valNode != null && valNode.has("Assocs")) {
                JsonNode assocs = valNode.path("Assocs");
                
                // POKUŠAJ 1: Direktna instanca
                if (assocs.has("Instance")) {
                    String instSurr = assocs.path("Instance").get(0).asText();
                    return getInstName(polazniTsuIndex, instSurr);
                }
                
                // POKUŠAJ 2: Ako je u pitanju ValueInstance (kao kod doAusschreiben)
                if (assocs.has("ValueInstance")) {
                    String valInstSurr = assocs.path("ValueInstance").get(0).asText();
                    return getInstName(polazniTsuIndex, valInstSurr);
                }
            }
        }
        return null;
    }

    // Pomoćna metoda
    private static String getInstName(Map<String, JsonNode> index, String surrogate) {
        JsonNode node = index.get(surrogate);
        return (node != null) ? node.path("Attributes").path("Name").asText() : null;
    }
    
    
}