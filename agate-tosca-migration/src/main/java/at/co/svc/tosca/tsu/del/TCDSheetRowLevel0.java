package at.co.svc.tosca.tsu.del;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TCDSheetRowLevel0 {

    /**
     * Prima eksplicitne putanje za extended ulaz, rowdata izlaz, spremljen TSU indeks i surogat.
     */
    public static void generateRowDataLevel0(
            String extendedFilePath, 
            String outputFilePath, 
            Map<String, JsonNode> tsuIndex, 
            String rootSurrogate
    ) throws Exception {
        
        ObjectMapper mapper = new ObjectMapper();

        // 1. Učitavamo lokalni extended fajl u mapu radi lakše pretrage
        JsonNode extendedRoot = mapper.readTree(new File(extendedFilePath));
        Map<String, JsonNode> localElements = new HashMap<>();
        if (extendedRoot.isArray()) {
            for (JsonNode element : extendedRoot) {
                String surrogate = element.path("Surrogate").asText();
                if (!surrogate.isEmpty()) {
                    localElements.put(surrogate, element);
                }
            }
        }

        // Pronalazimo glavni TestSheet objekat da izvučemo ime
        JsonNode testSheetNode = localElements.get(rootSurrogate);
        String testSheetName = "UnknownSheet";
        if (testSheetNode != null) {
            testSheetName = testSheetNode.path("Attributes").path("Name").asText();
        }
        System.out.println("[TCD-Level0] Obrada za TestSheet: " + testSheetName);

        // Lista u koju pakujemo očišćene atribute (Level 0)
        ArrayNode rowDataLevel0 = mapper.createArrayNode();

        // Rekurzivna obrada elemenata iz korena TestSheet-a preko "Items"
        if (testSheetNode != null) {
            JsonNode rootItems = testSheetNode.path("Assocs").path("Items");
            if (rootItems.isArray()) {
                for (JsonNode itemSurr : rootItems) {
                    processAttributeRecursively(itemSurr.asText(), "", localElements, tsuIndex, mapper, rowDataLevel0);
                }
            }
        }

        // Upisivanje direktno u fajl koji je zadat kroz parametar pipeline-a
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFilePath), rowDataLevel0);
        System.out.println("✔ [TCD-Level0] Uspešno kreiran fajl: " + outputFilePath);
    }

    private static void processAttributeRecursively(
            String currentSurrogate,
            String parentPrefix,
            Map<String, JsonNode> localElements,
            Map<String, JsonNode> tsuIndex,
            ObjectMapper mapper,
            ArrayNode resultList
    ) {
        JsonNode attributeNode = localElements.get(currentSurrogate);
        if (attributeNode == null) {
            attributeNode = tsuIndex.get(currentSurrogate);
        }

        if (attributeNode == null) return;

        String objectClass = attributeNode.path("ObjectClass").asText();
        if (!"TDAttribute".equals(objectClass)) return;

        String currentName = attributeNode.path("Attributes").path("Name").asText();
        String fullPathName = parentPrefix.isEmpty() ? currentName : parentPrefix + "." + currentName;

        JsonNode itemsNode = attributeNode.path("Assocs").path("Items");
        JsonNode valuesNode = attributeNode.path("Assocs").path("Values");

        if (itemsNode.isArray() && itemsNode.size() > 0) {
            for (JsonNode childSurr : itemsNode) {
                processAttributeRecursively(childSurr.asText(), fullPathName, localElements, tsuIndex, mapper, resultList);
            }
        } 
        // IZMENA: Sklonjen uslov '&& valuesNode.size() > 0' kako bi hvatali i prazne atribute na dnu strukture
        else {
            ObjectNode cleanAttribute = mapper.createObjectNode();
            cleanAttribute.put("ObjectClass", "TDAttribute");
            cleanAttribute.put("Surrogate", currentSurrogate);
            
            ObjectNode attrs = mapper.createObjectNode();
            attrs.put("Name", fullPathName); 
            cleanAttribute.set("Attributes", attrs);

            ObjectNode assocs = mapper.createObjectNode();
            
            JsonNode parentItem = attributeNode.path("Assocs").path("ParentItem");
            if (!parentItem.isMissingNode()) {
                assocs.set("ParentItem", parentItem);
            }

            ArrayNode cleanValuesArray = mapper.createArrayNode();
            
            // Popunjavamo niz vrednosti samo ako one zapravo postoje
            if (valuesNode.isArray() && valuesNode.size() > 0) {
                for (JsonNode valSurr : valuesNode) {
                    String valSurrStr = valSurr.asText();
                    if (localElements.containsKey(valSurrStr)) {
                        cleanValuesArray.add(valSurrStr);
                    } else {
                        resolveExternalClassValues(valSurrStr, tsuIndex, cleanValuesArray);
                    }
                }
            }
            
            assocs.set("Values", cleanValuesArray);
            cleanAttribute.set("Assocs", assocs);
            
            resultList.add(cleanAttribute);
        }
    }
    
    
    private static void resolveExternalClassValues(String surrogate, Map<String, JsonNode> tsuIndex, ArrayNode cleanValuesArray) {
        JsonNode node = tsuIndex.get(surrogate);
        if (node == null) return;

        String objectClass = node.path("ObjectClass").asText();

        if ("TDInstanceValue".equals(objectClass)) {
            JsonNode valueInstanceNode = node.path("Assocs").path("ValueInstance");
            if (valueInstanceNode.isArray() && valueInstanceNode.size() > 0) {
                for (JsonNode viSurr : valueInstanceNode) {
                    resolveExternalClassValues(viSurr.asText(), tsuIndex, cleanValuesArray);
                }
            } else {
                cleanValuesArray.add(surrogate);
            }
        } 
        else if ("TDInstance".equals(objectClass)) {
            JsonNode valuesNode = node.path("Assocs").path("Values");
            if (valuesNode.isArray()) {
                for (JsonNode vSurr : valuesNode) {
                    resolveExternalClassValues(vSurr.asText(), tsuIndex, cleanValuesArray);
                }
            }
        }
        else if ("TDAttribute".equals(objectClass)) {
            JsonNode valuesNode = node.path("Assocs").path("Values");
            if (valuesNode.isArray()) {
                for (JsonNode vSurr : valuesNode) {
                    boolean exists = false;
                    for (JsonNode existingVal : cleanValuesArray) {
                        if (existingVal.asText().equals(vSurr.asText())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        cleanValuesArray.add(vSurr.asText());
                    }
                }
            }
        }
    }

    /**
     * Javna metoda za učitavanje TSU indeksa (poziva se jednom iz glavnog pipeline-a)
     */
    public static Map<String, JsonNode> loadTsuToMemoryIndex(String tsuPath, ObjectMapper mapper) throws Exception {
        Map<String, JsonNode> index = new HashMap<>();
        File file = new File(tsuPath);
        if (!file.exists()) return index;

        try (InputStream fis = new FileInputStream(file);
             GZIPInputStream gis = new GZIPInputStream(fis)) {
            
            JsonNode root = mapper.readTree(gis);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    String surrogate = node.path("Surrogate").asText();
                    if (!surrogate.isEmpty()) {
                        index.put(surrogate, node);
                    }
                }
            }
        }
        return index;
    }
}