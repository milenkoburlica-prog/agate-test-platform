package at.co.svc.tosca.tsu.del;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import at.co.svc.tosca.tsu.dto.ToscaNode;
import at.co.svc.tosca.tsu.utils.GzipHelper;


public class TSUAnalyzer {

    private final Map<String, ToscaNode> fullObjectMap = new HashMap<>();
    
    // Liste za inventar
    private final List<ToscaNode> listTestCases = new ArrayList<>();
    private final List<ToscaNode> listReusables = new ArrayList<>();
    private final List<ToscaNode> listTemplates = new ArrayList<>();
    private final List<ToscaNode> listTCDSheets = new ArrayList<>();

    public static void main(String[] args) {
        String tsuPath = "C:\\work\\projects\\playwright\\tosca_2025\\CORE-Set.tsu";
        //tsuPath = "C:\\work\\projects\\playwright\\tosca_2025\\IF Testcase V2.tsu";
//        tsuPath = "C:\\work\\projects\\playwright\\tosca_2025\\OclDmpWeb_Einschreibung DM2_mit_SVNR_Min_GF.tsu";
        tsuPath = "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\DMP_getMedPatientenInformationen.tsu";
        //tsuPath = "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\IF-Testcases.tsu";        
        TSUAnalyzer analyzer = new TSUAnalyzer();
        analyzer.runAnalysis(tsuPath);
    }

    public void runAnalysis(String tsuPath) {
        try {
            System.out.println("====================================================");
            System.out.println("TSU DEEP ANALYZER START");
            System.out.println("====================================================");

            // 1. Ekstrakcija GZIP-a (TSU -> JSON string)
            String jsonContent = GzipHelper.extractGzipContent(tsuPath);
            
            // 2. Parsiranje JSON-a u Map<String, ToscaNode>
            // Ovde se pune svi atributi, uključujući i TCProperties
            parseJsonToMap(jsonContent);

            // 3. Kategorizacija osnovnih objekata (Test Cases, Templates...)
            categorizeObjects();
            printInventorySummary();

            // --- NOVI POZIVI METODA ---

            // A) Snimanje uprošćenog inventara u fajl
            saveCoreDataToJson("core_inventory.json");

            // B) Duboka analiza modula (Raspakivanje TCProperties)
            // Ova metoda će sada proći kroz sve module i ispisati Framework/Engine/Version
         // Kreiraš listu onoga što želiš da ignorišeš
            List<String> myBlacklist = new ArrayList<>();
//            myBlacklist.add("Html");
//            myBlacklist.add("UIA"); 
//            myBlacklist.add("AnyUI"); 
//            myBlacklist.add("WinX"); 
//            myBlacklist.add("Svc"); 
//            myBlacklist.add("TextStream"); 
//            myBlacklist.add("WinX"); 

            // Pozoveš metodu i proslediš joj tu listu
            printDeepModuleAnalysis(myBlacklist);
            
            // C) Opciono: Filtrirani prikaz (ako ti i dalje treba)
            // List<String> whiteList = Arrays.asList("XModule", "TestCase");
            // printFilteredInventory(whiteList, null);

            System.out.println("\n====================================================");
            System.out.println("ANALYSIS FINISHED SUCCESSFULLY");
            System.out.println("====================================================");

        } catch (Exception e) {
            System.err.println("!!! CRITICAL ERROR DURING ANALYSIS:");
            e.printStackTrace();
        }
    }

    private void categorizeObjects() {
        for (ToscaNode node : fullObjectMap.values()) {
            String type = node.objectClass;
            
            if ("TestCase".equals(type)) {
                // Ako postoji TemplateDetail ključ (makar i prazan) -> to je Template
                if (node.extraAssocs.containsKey("TemplateDetail")) {
                    listTemplates.add(node);
                } else {
                    listTestCases.add(node);
                }
            } else if ("ReuseableTestStepBlock".equals(type)) {
                    listReusables.add(node);
            } else if ("TestSheet".equals(type)) {
                    listTCDSheets.add(node);
            }
        }
    }

    

    private void parseJsonToMap(String content) {
        System.out.println(">>> Building memory map...");
        JsonObject rootObject = JsonParser.parseString(content).getAsJsonObject();
        JsonArray entities = rootObject.getAsJsonArray("Entities");
        
        for (JsonElement el : entities) {
            JsonObject obj = el.getAsJsonObject();
            ToscaNode node = new ToscaNode();
            
            node.objectClass = obj.get("ObjectClass").getAsString();
            node.surrogate = obj.get("Surrogate").getAsString();

            // Attributes
            JsonObject attrs = obj.getAsJsonObject("Attributes");
            if (attrs != null) {
                for (Map.Entry<String, JsonElement> entry : attrs.entrySet()) {
                    node.attributes.put(entry.getKey(), entry.getValue().isJsonNull() ? "" : entry.getValue().getAsString());
                }
            }

            // Associations
            JsonObject assocs = obj.getAsJsonObject("Assocs");
            if (assocs != null) {
                for (Map.Entry<String, JsonElement> entry : assocs.entrySet()) {
                    if (entry.getValue().isJsonArray()) {
                        List<String> ids = new ArrayList<>();
                        for (JsonElement idEl : entry.getValue().getAsJsonArray()) {
                            ids.add(idEl.getAsString());
                        }
                        node.extraAssocs.put(entry.getKey(), ids);
                        if ("Items".equals(entry.getKey())) node.childrenIds = ids;
                    }
                }
            }
            fullObjectMap.put(node.surrogate, node);
        }
    }

    public void printInventorySummary() {
        printSection("Standard Test Cases", listTestCases);
        printSection("Reusable Blocks", listReusables);
        printSection("Test Case Templates", listTemplates);
        printSection("TCD Sheets", listTCDSheets);
    }

    private void printSection(String title, List<ToscaNode> list) {
        System.out.println("\n>>> " + title.toUpperCase() + " (Count: " + list.size() + ")");
        if (list.isEmpty()) {
            System.out.println("    (No items found)");
            return;
        }
        String rowFormat = "    %-38s | %s%n";
        System.out.println("    " + "-".repeat(90));
        System.out.printf(rowFormat, "SURROGATE ID", "NAME");
        System.out.println("    " + "-".repeat(90));
        for (ToscaNode n : list) {
            System.out.printf(rowFormat, n.surrogate, n.getName());
        }
    }

    public void saveCoreDataToJson(String outputPath) {
        JsonArray outputArray = new JsonArray();
        for (ToscaNode node : fullObjectMap.values()) {
            JsonObject simplified = new JsonObject();
            simplified.addProperty("Surrogate", node.surrogate);
            simplified.addProperty("ObjectClass", node.objectClass);
            simplified.addProperty("Name", node.getName());
            outputArray.add(simplified);
        }
        try (FileWriter writer = new FileWriter(outputPath)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(outputArray, writer);
            System.out.println("\n>>> Core inventory saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("!!! Error saving JSON: " + e.getMessage());
        }
    }

    public void printFilteredInventory(List<String> whiteList, List<String> blackList) {
        System.out.println("\n>>> FILTERED INVENTORY (White/Black list)");
        String rowFormat = "    %-38s | %-20s | %s%n";
        System.out.println("    " + "-".repeat(110));
        System.out.printf(rowFormat, "SURROGATE ID", "CLASS", "NAME");
        System.out.println("    " + "-".repeat(110));

        for (ToscaNode node : fullObjectMap.values()) {
            String type = node.objectClass;
            boolean isAllowed = (whiteList == null || whiteList.isEmpty() || whiteList.contains(type));
            if (blackList != null && blackList.contains(type)) isAllowed = false;

            if (isAllowed) {
                System.out.printf(rowFormat, node.surrogate, type, node.getName());
            }
        }
    }
    
//    private Map<String, String> getModuleMetadata(String base64Content) {
//        Map<String, String> metadata = new HashMap<>();
//        metadata.put("Framework", "Unknown");
//        metadata.put("Version", "N/A");
//        metadata.put("Engine", "N/A");
//
//        String xml = GzipHelper.base64ContentUnGzip(base64Content);
//
//        System.out.println("xml = " + xml);
//        
//        // 3. Extract data using RegEx (tražimo AutomationFramework, Version, Engine)
//        metadata.put("Framework", extractXmlValue(xml, "AutomationFramework"));
//        metadata.put("Version", extractXmlValue(xml, "Version"));
//        metadata.put("Engine", extractXmlValue(xml, "Engine"));
//
//        return metadata;
//    }

    private String extractXmlValue(String xml, String tag) {
        Pattern pattern = Pattern.compile("<" + tag + ">(.*?)</" + tag + ">");
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "N/A";
    }
    
 
    public void printDeepModuleAnalysis(List<String> engineBlacklist) {
        System.out.println("\n>>> MODULE ARCHITECTURE ANALYSIS (Sorted by Engine > Task > Version)");
        List<String> blacklist = (engineBlacklist != null) ? engineBlacklist : new ArrayList<>();

        // Prikupljanje podataka u listu radi sortiranja
        List<Map<String, String>> rows = new ArrayList<>();

        for (ToscaNode node : fullObjectMap.values()) {
            if (!"XModule".equals(node.objectClass) && !"ApiModule".equals(node.objectClass)) {
                continue;
            }

            Map<String, String> meta = getDeepMetadata(node);
            meta.put("Surrogate", node.surrogate);
            meta.put("Name", node.getName());
            if ("ApiModule".equals(node.objectClass) && "n/a".equals(meta.get("Engine"))) {
                meta.put("Engine", "ApiModule");
            }

            // Blacklist provera
            if (blacklist.stream().anyMatch(b -> b.equalsIgnoreCase(meta.get("Engine")))) continue;

            rows.add(meta);
        }

        // Sortiranje: Engine -> Task -> Version
        rows.sort((m1, m2) -> {
            int res = m1.get("Engine").compareToIgnoreCase(m2.get("Engine"));
            if (res != 0) return res;
            res = m1.get("Task").compareToIgnoreCase(m2.get("Task"));
            if (res != 0) return res;
            return m1.get("Version").compareToIgnoreCase(m2.get("Version"));
        });

        // Format bez FRAMEWORK kolone
        // Surrogate (38) | Name (35) | Engine (12) | Task (20) | Version (10)
        String rowFormat = "    %-38s | %-35s | %-12s | %-20s | %s%n";
        
        System.out.println("    " + "-".repeat(130));
        System.out.printf(rowFormat, "SURROGATE ID", "NAME", "ENGINE", "EXECUTION TASK", "VERSION");
        System.out.println("    " + "-".repeat(130));

        for (Map<String, String> m : rows) {
            String name = m.get("Name");
            if (name.length() > 35) name = name.substring(0, 32) + "...";

            // Čistimo "n/a" u prazna polja radi preglednosti
            String task = "n/a".equals(m.get("Task")) ? "" : m.get("Task");
            String version = "n/a".equals(m.get("Version")) ? "" : m.get("Version");

            System.out.printf(rowFormat, 
                m.get("Surrogate"), 
                name, 
                m.get("Engine"), 
                task,
                version
            );
        }
        System.out.println("    " + "-".repeat(130));
        System.out.println("    Total unique modules: " + rows.size());
    }
    
    
    private Map<String, String> getConfigurationParams(ToscaNode moduleNode) {
        Map<String, String> params = new HashMap<>();
        params.put("Framework", "Classic"); // Default ako ništa ne nađemo
        params.put("Engine", "N/A");
        params.put("Version", "N/A");

        // 1. Izvuci verziju iz Attributes (TCProperties smo videli da ima samo to)
        // Možeš ostati pri onom tvom XML parseru za verziju ako je tamo

        // 2. Prođi kroz ConfigurationLinks
        List<String> configIds = moduleNode.extraAssocs.get("ConfigurationLinks");
        if (configIds != null) {
            for (String id : configIds) {
                ToscaNode configNode = fullObjectMap.get(id);
                if (configNode != null) {
                    // Obično su ovi objekti tipa "ConfigurationParameter" ili slično
                    String paramName = configNode.getName();
                    String paramValue = (String) configNode.attributes.getOrDefault("Value", "N/A");

                    if ("AutomationFramework".equals(paramName)) params.put("Framework", paramValue);
                    if ("Engine".equals(paramName)) params.put("Engine", paramValue);
                    if ("SpecialExecutionTask".equals(paramName)) params.put("SET", paramValue);
                }
            }
        }
        return params;
    }
    
    private Map<String, String> getDeepMetadata(ToscaNode moduleNode) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Framework", "n/a");
        metadata.put("Engine", "n/a");
        metadata.put("Task", "n/a");
        metadata.put("Version", "n/a");

        // 1. Pokušaj izvlačenja verzije iz TCProperties (Base64 XML)
        String tcProps = (String) moduleNode.attributes.get("TCProperties");
        if (tcProps != null && !tcProps.isEmpty()) {
            String extractedVersion = extractVersionFromTcProps(tcProps);
            if (!"n/a".equals(extractedVersion)) {
                metadata.put("Version", extractedVersion);
            }
        }

        // 2. Prolaz kroz asocijacije (ConfigurationLinks & Properties)
        List<String> allRelatedIds = new ArrayList<>();
        if (moduleNode.extraAssocs.containsKey("ConfigurationLinks")) allRelatedIds.addAll(moduleNode.extraAssocs.get("ConfigurationLinks"));
        if (moduleNode.extraAssocs.containsKey("Properties")) allRelatedIds.addAll(moduleNode.extraAssocs.get("Properties"));

        for (String id : allRelatedIds) {
            ToscaNode paramNode = fullObjectMap.get(id);
            if (paramNode == null) continue;

            String pName = paramNode.getName();
            String pValue = String.valueOf(paramNode.attributes.getOrDefault("Value", ""));

            if (pValue.isEmpty() || "null".equals(pValue)) continue;

            switch (pName) {
                case "AutomationFramework": metadata.put("Framework", pValue); break;
                case "Engine": metadata.put("Engine", pValue); break;
                case "SpecialExecutionTask": metadata.put("Task", pValue); break;
                case "Version": 
                    // Ako nismo našli u XML-u, uzmi odavde
                    if ("n/a".equals(metadata.get("Version"))) metadata.put("Version", pValue); 
                    break;
            }
        }
        return metadata;
    }

    // Pomoćna metoda za dekompresiju i čitanje verzije iz XML-a
    private String extractVersionFromTcProps(String base64) {
        try {
            byte[] compressed = Base64.getDecoder().decode(base64);
            try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(gzis, StandardCharsets.UTF_8))) {
                
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                
                //System.out.println(base64);
                // RegEx za izvlačenje Value iz TCProperty gde je Name="Version"
                Pattern p = Pattern.compile("Name=\"Version\"\\s+Value=\"(.*?)\"");
                Matcher m = p.matcher(sb.toString());
                if (m.find()) return m.group(1);
            }
        } catch (Exception e) { /* Ignoriši greške pri dekompresiji */ }
        return "n/a";
    }
    
    
}