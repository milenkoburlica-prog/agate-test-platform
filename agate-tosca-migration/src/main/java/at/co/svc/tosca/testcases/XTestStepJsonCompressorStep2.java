package at.co.svc.tosca.testcases;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class XTestStepJsonCompressorStep2 {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws Exception {

        String inputFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KES2VE001YTSWWGQQQ2YY28M_testcases-extended-compressed.json";
        inputFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\3a0ae521-a4bb-d67d-c70e-2d019bfb33f2_reusables-extended-compressed.json";
        inputFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KH6JVGKVDS1614ZS90RBX03C_testcases-extended-compressed.json";
        
        String tsuFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\EcSrvCTS.tsu";

        compressStep2(inputFile, tsuFile);
    }

    // =========================================================
    // STEP 2 COMPRESS
    // =========================================================
    public static void compressStep2(
            String inputFile,
            String tsuFile
    ) throws Exception {

        List<Map<String, Object>> nodes =
                MAPPER.readValue(new File(inputFile),
                        new TypeReference<List<Map<String, Object>>>() {});

        Map<String, Map<String, Object>> tsuIndex =
                loadTsuIndex(tsuFile);

        // moduleId -> ApiModule
        Map<String, Map<String, Object>> apiModules =
                indexApiModules(tsuIndex);

        // XModuleAttribute index
        Map<String, Map<String, Object>> moduleAttributes =
                indexModuleAttributes(tsuIndex);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> node : nodes) {

            String objectClass = asText(node.get("ObjectClass"));

            if (!"XTestStep".equals(objectClass)) {
                result.add(node);
                continue;
            }

            Map<String, Object> copy =
                    MAPPER.convertValue(node,
                            new TypeReference<Map<String, Object>>() {});

            Map<String, Object> assocs =
                    asMap(copy.get("Assocs"));

            List<String> moduleIds =
                    asStringList(assocs.get("Module"));

            if (moduleIds.isEmpty()) {
                result.add(copy);
                continue;
            }

            String moduleId = moduleIds.get(0);

            Map<String, Object> apiModule =
                    apiModules.get(moduleId);

            if (apiModule == null) {
                result.add(copy);
                continue;
            }

            // =====================================================
            // expected attributes from module
            // =====================================================
            Map<String, Object> moduleAssocs =
                    asMap(apiModule.get("Assocs"));

            List<String> expectedAttrs =
                    asStringList(moduleAssocs.get("Attributes"));

            // =====================================================
            // existing parameters
            // =====================================================
            List<Map<String, Object>> params =
                    (List<Map<String, Object>>) copy.get("Parameters");

            if (params == null) {
                params = new ArrayList<>();
            }

            Set<String> existingNames = new HashSet<>();

            for (Map<String, Object> p : params) {
                existingNames.add(asText(p.get("ExplicitName")));
            }
            
            
//            // =====================================================
//            // ADD MISSING PARAMETERS
//            // =====================================================
//            for (String attrId : expectedAttrs) {
//
//                Map<String, Object> attr =
//                        moduleAttributes.get(attrId);
//
//                if (attr == null) {
//                    continue;
//                }
//
//                Map<String, Object> attrDef =
//                        asMap(attr.get("Attributes"));
//
//                String name = asText(attrDef.get("Name"));
//                
//                String apimoduleinhalt = "{NULL}";
//                if ("Resource".equalsIgnoreCase(name)) { // only for Resource, Endpoint i Method
//                    MigrationLog.debug("DEBUG Resource attribute found!");
//                    apimoduleinhalt = debugApiModuleResource(attr, tsuIndex);
//                }
//                
//                
//                // =====================================================
//                // NOVI DEO: Detekcija ApiAttachment (Upload)
//                // =====================================================
//                boolean hasApiAttachment = false;
//                Map<String, Object> attrAssocs = asMap(attr.get("Assocs"));
//                List<String> subAttrIds = asStringList(attrAssocs.get("Attributes"));
//                
//                for (String subAttrId : subAttrIds) {
//                    Map<String, Object> subAttr = moduleAttributes.get(subAttrId);
//                    if (subAttr != null) {
//                        Map<String, Object> subAttrDef = asMap(subAttr.get("Attributes"));
//                        if ("ApiAttachment".equals(subAttrDef.get("SpecialIcon"))) {
//                            hasApiAttachment = true;
//                            MigrationLog.debug("DEBUG Found ApiAttachment sub-attribute for: " + name);
//                            break; // The required sub-attribute has been found.
//                        }
//                    }
//                }
//                // =====================================================
//                
//                
//                if (existingNames.contains(name)) {
//                    continue;
//                }
//
//                Map<String, Object> newParam =
//                        new LinkedHashMap<>();
//
//                newParam.put("ModuleAttributeSurrogate", attrId);
//                newParam.put("Value", apimoduleinhalt);
//                newParam.put("ExplicitName", name);
//                newParam.put(
//                        "ActionMode",
//                        mapActionMode(asText(attrDef.get("DefaultActionMode")))
//                );
//
//                params.add(newParam);
//            }

            // =====================================================
            // ADD MISSING PARAMETERS
            // =====================================================
            for (String attrId : expectedAttrs) {

                Map<String, Object> attr =
                        moduleAttributes.get(attrId);

                if (attr == null) {
                    continue;
                }

                Map<String, Object> attrDef =
                        asMap(attr.get("Attributes"));

                String name = asText(attrDef.get("Name"));
                
                String apimoduleinhalt = "{NULL}";
                if ("Resource".equalsIgnoreCase(name)) { // keep the existing Resource handling unchanged
                    MigrationLog.debug("DEBUG Resource attribute found!");
                    apimoduleinhalt = debugApiModuleResource(attr, tsuIndex);
                }
                
                // -----------------------------------------------------------------
                // Initialize variables with the existing default values
                // -----------------------------------------------------------------
                // =====================================================
                // Resolve dynamic values from XTestStepValue
                // =====================================================
                boolean isSoapUpload = false;
                String finalValue = apimoduleinhalt; 
                String finalActionMode = asText(attrDef.get("DefaultActionMode"));

                Map<String, Object> attrAssocs = asMap(attr.get("Assocs"));
                List<String> subAttrIds = asStringList(attrAssocs.get("Attributes"));
                
                for (String subAttrId : subAttrIds) {
                    Map<String, Object> subAttr = moduleAttributes.get(subAttrId);
                    if (subAttr != null) {
                        Map<String, Object> subAttrDef = asMap(subAttr.get("Attributes"));
                        
                        if ("ApiAttachment".equals(subAttrDef.get("SpecialIcon"))) {
                            isSoapUpload = true;
                            
                            // 1. Resolve the sub-attribute (39fb03f2-9bce-02b6-db83-63b712478211)
                            Map<String, Object> subAttrAssocs = asMap(subAttr.get("Assocs"));
                            List<String> tsvIds = asStringList(subAttrAssocs.get("TestStepValues"));
                            
                            if (!tsvIds.isEmpty()) {
                                // Use the first ID from the list (01KWAQFA8NC2M3YNPA8NCNHRYJ)
                                String tsvId = tsvIds.get(0); 
                                
                                // Resolve the XTestStepValue object directly from tsuIndex
                                Map<String, Object> tsvObject = tsuIndex.get(tsvId);
                                
                                if (tsvObject != null && "XTestStepValue".equals(asText(tsvObject.get("ObjectClass")))) {
                                    Map<String, Object> tsvAttrs = asMap(tsvObject.get("Attributes"));
                                    
                                    // Read the dynamic value from the test
                                    finalValue = asText(tsvAttrs.get("Value")); 
                                    
                                    // Read ActionProperty (for example, "File") and use it as the resulting ActionMode
                                    finalActionMode = asText(tsvAttrs.get("ActionProperty")); 
                                    
                                    MigrationLog.debug("DEBUG [SoapUpload] Mapped XTestStepValue for: " + name);
                                    MigrationLog.debug("DEBUG [SoapUpload] Value: " + finalValue);
                                    MigrationLog.debug("DEBUG [SoapUpload] ActionProperty used as ActionMode: " + finalActionMode);
                                }
                            }
                            
                            // If no XTestStepValue object is available, keep the default values as a fallback 
                            // and stop searching once the matching attachment attribute has been found
                            break; 
                        }
                    }
                }
                // =====================================================
                // -----------------------------------------------------------------
                
             // =====================================================
                // Preserve the existing continue behavior except for SOAP uploads
                // =====================================================
                if (existingNames.contains(name)) {
                    if (isSoapUpload) {
                        // Only update the existing parameter when a file upload was detected
                        for (Map<String, Object> existingParam : params) {
                            if (name.equals(asText(existingParam.get("ExplicitName")))) {
                                existingParam.put("Value", finalValue);
                                existingParam.put("ActionMode", mapActionMode(finalActionMode));
                                existingParam.put("IsSoapUpload", true);
                                MigrationLog.debug("DEBUG [SoapUpload] Updated attachment parameter in JSON for: " + name);
                                break;
                            }
                        }
                    } else {
                        // For all other parameters, preserve the existing behavior
                        continue;
                    }
                } else {
                    // If the parameter does not exist in the test, add it using the standard logic
                    Map<String, Object> newParam = new LinkedHashMap<>();
                    newParam.put("ModuleAttributeSurrogate", attrId);
                    newParam.put("Value", finalValue);
                    newParam.put("ExplicitName", name);
                    newParam.put("ActionMode", mapActionMode(finalActionMode));
                    
                    if (isSoapUpload) {
                        newParam.put("IsSoapUpload", true);
                    }
                    params.add(newParam);
                    }
            }
                          
            copy.put("Parameters", params);
            result.add(copy);
        }

        // =========================================================
        // SAVE
        // =========================================================
        Path inputPath = Path.of(inputFile);

        String outputFile =
                inputPath.toString()
                        .replace(".json", "_step2.json");

        MAPPER.writeValue(new File(outputFile), result);

        MigrationLog.success(
                "Step2 written: "
                        + outputFile
        );
    }

    // =========================================================
    // INDEX API MODULES
    // =========================================================
    private static Map<String, Map<String, Object>> indexApiModules(
            Map<String, Map<String, Object>> tsuIndex
    ) {

        Map<String, Map<String, Object>> result = new HashMap<>();

        for (Map<String, Object> entity : tsuIndex.values()) {

            if ("ApiModule".equals(asText(entity.get("ObjectClass")))) {
                result.put(asText(entity.get("Surrogate")), entity);
            }
        }

        return result;
    }

    // =========================================================
    // INDEX MODULE ATTRIBUTES
    // =========================================================
    private static Map<String, Map<String, Object>> indexModuleAttributes(
            Map<String, Map<String, Object>> tsuIndex
    ) {

        Map<String, Map<String, Object>> result = new HashMap<>();

        for (Map<String, Object> entity : tsuIndex.values()) {

            if ("XModuleAttribute".equals(asText(entity.get("ObjectClass")))) {
                result.put(asText(entity.get("Surrogate")), entity);
            }
        }

        return result;
    }

    // =========================================================
    // LOAD TSU
    // =========================================================
    private static Map<String, Map<String, Object>> loadTsuIndex(
            String tsuFile
    ) throws Exception {

        Map<String, Map<String, Object>> result = new HashMap<>();

        try (
                FileInputStream fis = new FileInputStream(tsuFile);
                GZIPInputStream gis = new GZIPInputStream(fis);
                InputStreamReader reader = new InputStreamReader(gis)
        ) {

            Map<String, Object> root =
                    MAPPER.readValue(reader,
                            new TypeReference<Map<String, Object>>() {});

            List<Map<String, Object>> entities =
                    (List<Map<String, Object>>) root.get("Entities");

            for (Map<String, Object> e : entities) {
                result.put(asText(e.get("Surrogate")), e);
            }
        }

        return result;
    }

    // =========================================================
    // HELPERS
    // =========================================================
    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        return o == null ? new LinkedHashMap<>() : (Map<String, Object>) o;
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object o) {
        return o == null ? new ArrayList<>() : (List<String>) o;
    }

    private static String asText(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
    
    
    private static String debugApiModuleResource(
            Map<String, Object> attr,
            Map<String, Map<String, Object>> tsuIndex
    ) {

        MigrationLog.debug("=== DEBUG RESOURCE HIT ===");

        Map<String, Object> assocs =
                asMap(attr.get("Assocs"));

        List<String> modules =
                asStringList(assocs.get("Module"));

        MigrationLog.debug("Modules: " + modules);

        if (modules.isEmpty()) {
            MigrationLog.debug("No module found!");
            return null;
        }

        String moduleId = modules.get(0);

        MigrationLog.debug("ModuleId = " + moduleId);

        Map<String, Object> apiModule = tsuIndex.get(moduleId);

        if (apiModule == null) {
            MigrationLog.debug("API Module NOT FOUND in tsuIndex!");
            return null;
        }

        MigrationLog.debug("API Module FOUND");
        String objectClass = asText(apiModule.get("ObjectClass"));

        if (!"ApiModule".equals(objectClass)) {
            MigrationLog.debug("SKIP - Not ApiModule, but: " + objectClass);
            return null;
        }
        

        String ret = debugApiModuleDecoded(apiModule, "Resource");

        MigrationLog.debug("=== BACK TO CALLER (BREAKPOINT HERE) ===");
        
        return ret;
    }
    
    
    private static String debugApiModuleDecoded(Map<String, Object> apiModule, String option) {

        MigrationLog.debug("=== API MODULE DECODE DEBUG ===");

        Map<String, Object> attributes =
                asMap(apiModule.get("Attributes"));

        // =====================================================
        // 1. TCProperties (Base64 -> GZIP -> XML)
        // =====================================================
        String tcPropsBase64 = asText(attributes.get("TCProperties"));

        String tcXml = decodeGzipBase64(tcPropsBase64);

        MigrationLog.debug("\n--- TCProperties XML ---\n");
        MigrationLog.debug(tcXml);

        // =====================================================
        // 2. ExplicitConnection (Base64 -> XML)
        // =====================================================
        String explicitBase64 = asText(attributes.get("ExplicitConnection"));

        String explicitXml = decodeBase64(explicitBase64);

        MigrationLog.debug("\n--- ExplicitConnection XML ---\n");
        MigrationLog.debug(explicitXml);

        // =====================================================
        // DEBUG STOP POINT
        // =====================================================
        MigrationLog.debug("\n=== STOP HERE FOR DEBUG ===");

        String resource = extractTcPropertyValue(tcXml, "Resource");
        MigrationLog.debug("RESOURCE = " + resource);

        String method = extractTcPropertyValue(tcXml, "Method");
        MigrationLog.debug("METHOD = " + method);
        
        String endpoint = extractExplicitValue(explicitXml, "Endpoint");
        MigrationLog.debug("ENDPOINT = " + endpoint);
        
        String ret = resource;
        if ((option != null) && (option.equals("Method")))
            ret = method;
        if ((option != null) && (option.equals("Endpoint")))
            ret = endpoint;
        
        // Breakpoint location
        return ret;
    }
    private static String extractTcPropertyValue(String xml, String propertyName) {

        if (xml == null || xml.isEmpty()) return "";

        try {
            String search = "Name=\"" + propertyName + "\"";

            int idx = xml.indexOf(search);
            if (idx == -1) return "";

            int valueStart = xml.indexOf("Value=\"", idx);
            if (valueStart == -1) return "";

            valueStart += 7;

            int valueEnd = xml.indexOf("\"", valueStart);
            if (valueEnd == -1) return "";

            return xml.substring(valueStart, valueEnd);

        } catch (Exception e) {
            MigrationLog.error("TCProperty extract failed: " + e.getMessage());
            return "";
        }
    }
    
    private static String extractExplicitValue(String json, String keyName) {

        if (json == null || json.isEmpty()) return "";

        try {
            ObjectMapper mapper = new ObjectMapper();

            List<Map<String, Object>> list =
                    mapper.readValue(json, new TypeReference<>() {});

            for (Map<String, Object> entry : list) {

                String key = String.valueOf(entry.get("Key"));
                if (keyName.equalsIgnoreCase(key)) {
                    return String.valueOf(entry.get("Value"));
                }
            }

        } catch (Exception e) {
            MigrationLog.error("ExplicitConnection parse failed: " + e.getMessage());
        }

        return "";
    }
    
    private static String decodeGzipBase64(String base64String) {

        if (base64String == null || base64String.isEmpty()) {
            return "";
        }

        try {
            byte[] compressed = Base64.getDecoder().decode(base64String);

            try (GZIPInputStream gis =
                         new GZIPInputStream(new ByteArrayInputStream(compressed));
                 InputStreamReader isr =
                         new InputStreamReader(gis);
                 BufferedReader br = new BufferedReader(isr)) {

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                return sb.toString();
            }

        } catch (Exception e) {
            MigrationLog.error("TCProperties decode failed: " + e.getMessage());
            return "";
        }
    }
    
    
    private static String decompressBase64Gzip(String value) {

        if (value == null || value.isBlank()) return "";

        try {
            byte[] compressed = Base64.getDecoder().decode(value);

            try (GZIPInputStream gis =
                         new GZIPInputStream(new ByteArrayInputStream(compressed));
                 BufferedReader br =
                         new BufferedReader(new InputStreamReader(gis))) {

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                return sb.toString();
            }

        } catch (Exception e) {
            return "ERROR GZIP: " + e.getMessage();
        }
    }

    private static String decodeBase64(String value) {

        if (value == null || value.isBlank()) return "";

        try {
            return new String(Base64.getDecoder().decode(value));
        } catch (Exception e) {
            return "ERROR BASE64: " + e.getMessage();
        }
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

            default -> value;
        };
    }

    
    
    
}