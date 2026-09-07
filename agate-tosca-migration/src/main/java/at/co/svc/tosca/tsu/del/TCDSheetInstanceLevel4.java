package at.co.svc.tosca.tsu.del;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TCDSheetInstanceLevel4 {

    public static void resolveInstanceNames(String tsuPath, String level3Path, String outputLevel4Path) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(new File(level3Path));

        // Iteriramo kroz svaki objekat u listi
        for (JsonNode item : rootNode) {
            if (item.has("TDInstance") && item.get("TDInstance").isArray()) {
                
                // Iteriramo kroz svaki parametar unutar TDInstance
                for (JsonNode param : (ArrayNode) item.get("TDInstance")) {
                    
                    String value = param.path("Parameter.Value").asText();
                    
                    // Ako je vrednost prazna, tražimo zamenu preko Parameter.Surrogate
                    if (value == null || value.isEmpty()) {
                        
                        String paramSurr = param.path("Parameter.Surrogate").asText();
                        
                        if (!paramSurr.isEmpty()) {
                            String resolvedName = findInstanceNameForMainParameter(tsuPath, paramSurr);
                            
                            if (!resolvedName.isEmpty()) {
                                ((ObjectNode) param).put("Parameter.Value", resolvedName);
                                System.out.println("SUCCESS: Mapirano " + param.path("Parameter.Name").asText() + " u " + resolvedName);
                            } else {
                                System.out.println("INFO: Nije pronadjena ValueInstance za: " + param.path("Parameter.Name").asText());
                            }
                        }
                    }
                }
            }
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputLevel4Path), rootNode);
    }
    
    
    
    private static String findInstanceNameForMainParameter(String tsuPath, String paramSurrogate) throws Exception {
        // 1. Učitavanje i indeksiranje (originalni TSU fajl)
        JsonObject tsuRoot = loadTsu(tsuPath);
        JsonArray entities = tsuRoot.getAsJsonArray("Entities");
        Map<String, JsonObject> tsuIndex = new HashMap<>();
        for (JsonElement el : entities) {
            JsonObject obj = el.getAsJsonObject();
            String id = getString(obj, "Surrogate");
            tsuIndex.put(id, obj);
        }

        // 2. Pretraga kroz sve objekte da nađemo TDInstanceValue koji sadrži naš paramSurrogate u "Element" listi
        for (JsonObject obj : tsuIndex.values()) {
            if (!"TDInstanceValue".equals(getString(obj, "ObjectClass"))) continue;

            JsonObject assocs = obj.getAsJsonObject("Assocs");
            if (assocs == null || !assocs.has("Element")) continue;

            // Provera da li "Element" lista sadrži naš paramSurrogate
            JsonArray elementArr = assocs.getAsJsonArray("Element");
            boolean matches = false;
            for (JsonElement el : elementArr) {
                if (el.getAsString().equals(paramSurrogate)) {
                    matches = true;
                    break;
                }
            }

            if (matches) {
                // 3. Tražimo Assocs.ValueInstance
                if (assocs.has("ValueInstance")) {
                    JsonArray valInstArr = assocs.getAsJsonArray("ValueInstance");
                    if (valInstArr != null && valInstArr.size() > 0) {
                        String instanceId = valInstArr.get(0).getAsString();

                        // 4. Tražimo TDInstance objekat sa tim ID-jem
                        JsonObject targetInstance = tsuIndex.get(instanceId);
                        if (targetInstance != null && "TDInstance".equals(getString(targetInstance, "ObjectClass"))) {
                            // 5. Vraćamo ime instance (Attributes.Name)
                            return getString(targetInstance.getAsJsonObject("Attributes"), "Name");
                        }
                    }
                }
            }
        }
        return ""; // Ako nema ValueInstance ili ne nađe instancu, vraćamo prazno
    }
    
    private static String findInstanceNameInTsuMap(String tsuPath, String subSurr, String subName) throws Exception {
        // 1. Učitavanje i indeksiranje
        JsonObject tsuRoot = loadTsu(tsuPath);
        JsonArray entities = tsuRoot.getAsJsonArray("Entities");
        Map<String, JsonObject> tsuIndex = new HashMap<>();
        for (JsonElement el : entities) {
            JsonObject obj = el.getAsJsonObject();
            String id = getString(obj, "Surrogate");
            tsuIndex.put(id, obj);
        }

        String suche = subName.substring(subName.lastIndexOf(".") + 1);

        // 2. Pretraga: Tražimo TDInstanceValue gde je subSurr u "Assocs.Element"
        for (JsonObject tdInstanceValue : tsuIndex.values()) {
            if (!"TDInstanceValue".equals(getString(tdInstanceValue, "ObjectClass"))) continue;

            JsonArray elementArr = tdInstanceValue.getAsJsonObject("Assocs").getAsJsonArray("Element");
            boolean elementMatches = false;
            for (JsonElement el : elementArr) {
                if (el.getAsString().equals(subSurr)) {
                    elementMatches = true;
                    break;
                }
            }

            if (elementMatches) {
                // 3. Provera da li "vlasnik" (TDAttribute) ima traženo ime (suche)
                JsonObject ownerAttr = tsuIndex.get(subSurr);
                if (ownerAttr != null && suche.equals(getString(ownerAttr.getAsJsonObject("Attributes"), "Name"))) {
                    
                    // 4. Sada tražimo TDInstance koji u svojoj listi "Values" sadrži Surrogate ID od ovog tdInstanceValue
                    String tdInstanceValueId = getString(tdInstanceValue, "Surrogate");
                    
                    for (JsonObject potentialInstance : tsuIndex.values()) {
                        if ("TDInstance".equals(getString(potentialInstance, "ObjectClass"))) {
                            JsonArray valuesArr = potentialInstance.getAsJsonObject("Assocs").getAsJsonArray("Values");
                            
                            // Provera da li TDInstance u listi "Values" sadrži ID našeg TDInstanceValue
                            for (JsonElement valId : valuesArr) {
                                if (valId.getAsString().equals(tdInstanceValueId)) {
                                    // 5. Našli smo instancu! Vraćamo Attributes.Name (npr. "i_1")
                                    return getString(potentialInstance.getAsJsonObject("Attributes"), "Name");
                                }
                            }
                        }
                    }
                }
            }
        }
        return "";
    }    
    
    // =====================================================
    // HELPER
    // =====================================================
    private static String getString(JsonObject obj, String key) {

        if (obj == null || obj.get(key) == null || obj.get(key).isJsonNull()) {
            return "";
        }

        return obj.get(key).getAsString();
    }
    // =====================================================
    // LOAD TSU
    // =====================================================
    private static JsonObject loadTsu(String tsuFile) throws Exception {

        try (
                FileInputStream fis = new FileInputStream(tsuFile);
                GZIPInputStream gis = new GZIPInputStream(fis);
                InputStreamReader reader =
                        new InputStreamReader(gis, StandardCharsets.UTF_8)
        ) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    
}