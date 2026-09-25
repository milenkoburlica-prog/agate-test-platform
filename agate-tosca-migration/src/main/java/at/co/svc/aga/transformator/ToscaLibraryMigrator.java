package at.co.svc.aga.transformator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.CleanTestCase;
import at.co.svc.aga.transformator.dto.Module;
import at.co.svc.aga.transformator.dto.StepValueDetails;
import at.co.svc.aga.transformator.utils.MigrationLog;
import at.co.svc.aga.transformator.utils.MigrationReport;
import at.co.svc.aga.transformator.utils.ProcessBuffer;
import at.co.svc.aga.transformator.utils.ProcessDbStep;
import at.co.svc.aga.transformator.utils.ProcessJsonAssert;
import at.co.svc.aga.transformator.utils.ProcessAPILogic;
import at.co.svc.aga.transformator.utils.ProcessReusableCall;
import at.co.svc.aga.transformator.utils.ProcessStartProgram;
import at.co.svc.aga.transformator.utils.ProcessTBoxFileRead_Create;
import at.co.svc.aga.transformator.utils.ProcessTBoxEvaluationRuntime;
import at.co.svc.aga.transformator.utils.ProcessWait;
import at.co.svc.aga.transformator.utils.ToscaValueTranslator;
import at.co.svc.tosca.util.FileNameSanitizer;

public class ToscaLibraryMigrator {

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    private final String appId;
    private final Map<String, Module> moduleMap;

    public static void main(String[] args) {
        // Path configuration
        String path = System.getProperty("user.dir") + "\\";
        String appId = "TAGA";
        System.setProperty("APPLICATION", "TAGA");
        String baseFileName = "006_EcSrvCts";
        baseFileName = "Clv-svsc";
        String jsonDir = "jsonOut";
        
        String inputModules = path + jsonDir + File.separator + baseFileName + ".json";        // Modules (Entities)
        String inputLibs = path + jsonDir + File.separator + baseFileName + "_reusables.json";     // Reusable blocks

        List<String> migModules = new ArrayList<String>();
        migModules = extractedMain(appId, inputModules, inputLibs, migModules);
    }

    public static List<String> extractedMain(String appId, String inputModules, String inputLibs, List<String> migModules) {
        try {
            // Load modules
            Map<String, Module> moduleMap = loadModulesFromOriginalJson(inputModules);

            System.out.println("--- 1. Generate API definitions (files) ---");

            System.out.println("--- 2. Migrate reusable libraries ---");
            ToscaLibraryMigrator tlm = new ToscaLibraryMigrator(appId, moduleMap);
            migModules = tlm.migrate(inputLibs, migModules);
            return migModules;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return migModules;
    }

    public ToscaLibraryMigrator(String appId, Map<String, Module> moduleMap) {
        this.appId = appId;
        this.moduleMap = moduleMap;
    }

    public List<String> migrate(String libsJsonPath, List<String> migModules) {
        try {
            File jsonFile = new File(libsJsonPath);
            if (!jsonFile.exists()) {
                System.err.println("[ERROR] Library file does not exist: " + libsJsonPath);
                return migModules;
            }

            // Load JSON as a map (BlockName -> StepList)
//            Map<String, List<CleanStep>> libsMap = mapper.readValue(jsonFile, 
//                new TypeReference<Map<String, List<CleanStep>>>() {});
            List<CleanTestCase> libs = mapper.readValue(
                    jsonFile,
                    new TypeReference<List<CleanTestCase>>() {});
            
            Map<String, List<CleanStep>> libsMap = new LinkedHashMap<>();
            for (CleanTestCase tc : libs) {
                libsMap.put(tc.getName(), tc.getSteps());
            }
            
            for (Map.Entry<String, List<CleanStep>> entry : libsMap.entrySet()) {
                migModules=processLibraryBlock(entry.getKey(), entry.getValue(),migModules);
            }

            return migModules;
        } catch (Exception e) {
            System.err.println("[ERROR] Error during reusable library migration: " + e.getMessage());
            e.printStackTrace();
        }
        return migModules;
    }

    private List<String> processLibraryBlock(
            String rawName,
            List<CleanStep> steps,
            List<String> migModules) throws Exception {

        System.out.println();
        System.out.println("========== REUSABLE CREATE TRACE ==========");
        System.out.println("[CREATE-TRACE] rawName = '" + rawName + "'");

        String cleanName =
                FileNameSanitizer.sanitize(rawName);

        System.out.println(
                "[CREATE-TRACE] cleanName = '" + cleanName + "'"
        );

        StringBuilder yaml =
                new StringBuilder();

        yaml.append("steps:");

        String outputPath =
                "migration/data/"
                        + appId
                        + "/reusable/"
                        + cleanName
                        + ".yaml";

        System.out.println(
                "[CREATE-TRACE] outputPath = " + outputPath
        );

        File outputFile =
                new File(
                        System.getProperty("user.dir"),
                        outputPath
                );

        System.out.println(
                "[CREATE-TRACE] absolute outputPath = "
                        + outputFile.getAbsolutePath()
        );

        System.out.println(
                "[CREATE-TRACE] exists BEFORE write = "
                        + outputFile.exists()
        );

        for (int i = 0; i < steps.size(); i++) {

            CleanStep step = steps.get(i);

            CleanStep nextStep =
                    i + 1 < steps.size()
                            ? steps.get(i + 1)
                            : null;

            yaml.append(
                    captureOutput(
                            step,
                            nextStep,
                            this.moduleMap,
                            appId,
                            outputPath
                    )
            );
        }

        saveToFile(
                outputPath,
                yaml.toString()
        );

        System.out.println(
                "[CREATE-TRACE] exists AFTER write = "
                        + outputFile.exists()
        );

        System.out.println(
                "[CREATE-TRACE] registered migrated module = "
                        + outputPath
        );

        System.out.println(
                "==========================================="
        );

        System.out.println(
                "Migrated reusable block: "
                        + outputPath
        );

        migModules.add(outputPath);

        return migModules;
    }
    
    
    /**
     * Captures output from the existing utility classes without changing their code
     * @throws Exception 
     */
    private String captureOutput(
            CleanStep step,
            CleanStep nextStep,
            Map<String, Module> moduleMap,
            String appId,
            String yamlOutputPath) throws Exception {
        PrintStream oldOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (PrintStream newOut = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            System.setOut(newOut);
            
         // 1. Prepare basic strings
            String moduleName = step.getModule() != null ? step.getModule() : "";
            String moduleClass = step.getModuleClass() != null ? step.getModuleClass() : "";
            String type = step.getType() != null ? step.getType() : "";
            String stepName = step.getName() != null ? step.getName() : "";

            // LOOP / BREAK are AGATE control-flow steps and do not have a Tosca module.
            // Handle them before the legacy module dispatch so the old behaviour stays unchanged.
            if (type.equalsIgnoreCase("LOOP")) {
                processLoop(step, moduleMap, appId, yamlOutputPath);
                System.out.flush();
                return baos.toString();
            }

            if (type.equalsIgnoreCase("BREAK")) {
                processBreak(step);
                System.out.flush();
                return baos.toString();
            }

            // API modules use the same processor as normal testcase migration.
            // This must happen before the legacy default-op logic so Verify-only
            // response steps are not forced to EXEC.
            if (moduleClass.equalsIgnoreCase("ApiModule")) {
                ProcessAPILogic.processAPILogic(
                        moduleMap,
                        step,
                        nextStep,
                        appId
                );
                System.out.flush();
                return baos.toString();
            }

            // 2. Set "OP" if it has not already been set
            if (step.getOp() == null || step.getOp().isEmpty()) {
                if (moduleName.contains("DB Expert") || moduleName.contains("SQL")) {
                    step.setOp("EXEC");
                } else if (moduleName.contains("Set Buffer")) {
                    step.setOp("BUFFER");
                } else if (type.equalsIgnoreCase("Reusable")) {
                    step.setOp("CALL");
                } else if (moduleName.toLowerCase().contains("put")) {
                    step.setOp("PUT");
                } else if (moduleName.toLowerCase().contains("get")) {
                    step.setOp("GET");
                } else {
                    step.setOp("EXEC"); // Default
                }
            }
            

            // 1. DB modules
            if (moduleName != null
                    && (moduleName.startsWith("TBox DB Expert module")
                    || moduleName.startsWith("TBox DB Run SQL Statement"))) {
                
                ProcessDbStep.processDbStep(step, "");
            }
            // 3. Reusable Call
            else if (type.equalsIgnoreCase("Reusable")) {
                ProcessReusableCall.processReusableCall(step);
            }
            // 4. Standard buffer
            else if (moduleName.equalsIgnoreCase("TBox Set Buffer")) {
                ProcessBuffer.processBuffer(step, "");
            }
            else if (moduleName.equalsIgnoreCase("TBox Wait")) {
                ProcessWait.processWait(step, "");
            }
            else if (moduleName.equalsIgnoreCase("TBox Start Program")) {
                ProcessStartProgram.processStartProgram(step, "");
            } else if (moduleName.equalsIgnoreCase("TBox Evaluation Tool")) {

                /*
                 * IF-condition Evaluation Tool steps should already have been
                 * consumed by IfPropagationEngine.
                 *
                 * If one reaches this point it is an executable evaluation
                 * inside a THEN/ELSE branch and must be migrated as an ASSERT.
                 */
                boolean migrated =
                        ProcessTBoxEvaluationRuntime.processEvaluation(
                                step,
                                ""
                        );

                if (!migrated) {
                    registerUnsupportedStep(
                            moduleName,
                            stepName,
                            yamlOutputPath
                    );
                }

            } else if (moduleName.equalsIgnoreCase("TBox Delete Buffer")) {

                System.err.println(
                        "[INFO] Tosca module TBox Delete Buffer will not be migrated "
                        + "because AGATE buffers are cleared automatically after test execution."
                );
                

            } else if (moduleName.equalsIgnoreCase("TBox Delete Resource")) {

                registerUnsupportedStep(
                        moduleName,
                        stepName,
                        yamlOutputPath
                );
            } else {
                if ("LogCheckCloud_Test01 Request".equals(moduleName) ||
                "LogCheckCloud _Test02 Request".equals(moduleName) ||
                "LogCheckCloud _KAMS_V3".equals(moduleName) ||
                "LogCheckCloud_Test Request INFO".equals(moduleName) ||
                "LogCheckCloud_Test Request WARN/ERROR".equals(moduleName)) {
                    ProcessJsonAssert.processJsonAssert(step);
                } else if ("Open/Create JSON file".equals(moduleName)) {
                    ProcessJsonAssert.processJsonOpenFile(step);
                } else if (moduleName.equalsIgnoreCase("TBox Read/Create File")) {

                    ProcessTBoxFileRead_Create.processReadCreate(step, "");

                } else {
                     System.out.println("      ");
                     System.out.println("      # ERROR");
                     System.out.println("        Unprocessed moduleName=" + moduleName);
                     System.out.println("      ");

                     MigrationReport.registerUnprocessedModule(
                             moduleName,
                             stepName,
                             yamlOutputPath
                     );
                }
                //Open/Create JSON file
                //LogCheckCloud _Test02 Request
                //TBox Delete Resource
                //TBox Delete Buffer
                //TBox Evaluation Tool
                //ProcessJsonAssert.processJsonAssert(step);
            }
            
            System.out.flush();
        } finally {
            System.setOut(oldOut);
        }
        
        return baos.toString();
    }
    
    
    /**
     * Writes an AGATE LOOP and reuses the existing reusable-step dispatcher
     * for every nested step. The nested YAML is indented by four additional
     * spaces, so the existing processors do not need to be changed.
     * @throws Exception 
     */
    private void processLoop(
            CleanStep step,
            Map<String, Module> moduleMap,
            String appId,
            String yamlOutputPath) throws Exception {

        System.out.println();
        System.out.println("      - type: LOOP");

        if (step.getMaxIterations() != null) {
            System.out.println("        maxIterations: " + step.getMaxIterations());
        }

        System.out.println("        steps:");

        List<CleanStep> nestedSteps = step.getSteps();

        if (nestedSteps == null || nestedSteps.isEmpty()) {
            return;
        }

        for (int i = 0; i < nestedSteps.size(); i++) {

            CleanStep nestedStep = nestedSteps.get(i);

            CleanStep nextNestedStep =
                    i + 1 < nestedSteps.size()
                            ? nestedSteps.get(i + 1)
                            : null;

            String nestedYaml =
                    captureOutput(
                            nestedStep,
                            nextNestedStep,
                            moduleMap,
                            appId,
                            yamlOutputPath
                    );

            System.out.print(
                    indentCapturedYaml(
                            nestedYaml,
                            "    "
                    )
            );
        }
    }


    /**
     * Writes an AGATE BREAK control-flow step.
     */
    private void processBreak(CleanStep step) {

        System.out.println();
        System.out.println("      - type: BREAK");

        String condition = step.getCondition();

        if (condition != null && !condition.isBlank()) {

            System.out.println(
                    "        condition: \""
                            + escapeYamlDoubleQuoted(condition)
                            + "\""
            );
        }
    }


    /**
     * Existing processors write reusable steps with their normal top-level
     * indentation. A LOOP only has to shift that generated YAML one level
     * deeper. Blank lines stay blank.
     */
    private String indentCapturedYaml(
            String yaml,
            String indent) {

        if (yaml == null || yaml.isEmpty()) {
            return "";
        }

        String normalized =
                yaml.replace("\r\n", "\n")
                    .replace("\r", "\n");

        String[] lines =
                normalized.split("\n", -1);

        StringBuilder result =
                new StringBuilder();

        for (int i = 0; i < lines.length; i++) {

            String line = lines[i];

            if (!line.isEmpty()) {
                result.append(indent);
            }

            result.append(line);

            if (i < lines.length - 1) {
                result.append(System.lineSeparator());
            }
        }

        return result.toString();
    }


    private String escapeYamlDoubleQuoted(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


    private void registerUnsupportedStep(String moduleName, String stepName, String yamlOutputPath) {
        System.out.println();
        System.out.println("      # WARNING: Unsupported Tosca module - step was NOT silently discarded");
        System.out.println("      # module: " + moduleName);
        System.out.println("      # step: " + stepName);

        MigrationReport.registerUnprocessedModule(
                moduleName,
                stepName,
                yamlOutputPath
        );
    }

    private void saveToFile(String path, String content) throws Exception {
        System.out.println("createDirectories6="+path);
        Files.createDirectories(Paths.get(path).getParent());
        Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8));
    }
    private void processRestStepWithTranslation(
            CleanStep step,
            Map<String, Module> moduleMap,
            String appId) {

        Module myModule =
                moduleMap.get(
                        step.getModuleSurrogate());

        // --- Extract endpointValue ---

        String endpointValue = "";

        // 1. First check whether it is defined directly in the step values
        if (step.getValues() != null
                && step.getValuesAsMap()
                        .containsKey("Endpoint")) {

            endpointValue =
                    step.getValuesAsMap()
                            .get("Endpoint")
                            .getValue();
        }

        // 2. If it is not defined in the step,
        //    read it from the module attributes
        if ((endpointValue == null
                || endpointValue.isEmpty())
                && myModule != null) {

            endpointValue =
                    myModule.getAttributes()
                            .getOrDefault(
                                    "Endpoint",
                                    "");
        }

        // 3. Optionally convert Tosca variables
        //    to the Agate {{var}} format
        if (endpointValue != null
                && endpointValue.contains("{CP[")) {

            endpointValue =
                    endpointValue
                            .replace(
                                    "{CP[",
                                    "{{")
                            .replace(
                                    "]}",
                                    "}}");
        }

        // --------------------------------------

        // 1. Determine the protocol

        String protocol =
                "rest";

        String decodedPayload = null;

        if (myModule != null) {

            decodedPayload =
                    decodeBase64(
                            myModule.getAttributes()
                                    .get("Payload"));

            if (decodedPayload != null
                    && decodedPayload.contains("Envelope")) {

                protocol =
                        "soap";
            }
        }

        // 2. Determine the context (simulatord.insert_card)

        String contextPrefix = "";

        if (myModule != null) {

            // First try to extract the path
            // from TCProperties (GZIP)
            String tcProps =
                    myModule.getAttributes()
                            .get("TCProperties");

            String resource =
                    extractResourceFromTCProperties(
                            tcProps);

            if (resource != null
                    && !resource.isEmpty()) {

                contextPrefix =
                        resource.replace(
                                "/",
                                ".");

            } else {

                // If TCProperties does not contain it,
                // use the existing fallback logic

                String res =
                        myModule.getAttributes()
                                .get("Resource");

                String path =
                        myModule.getAttributes()
                                .get("Path");

                String explicit =
                        myModule.getAttributes()
                                .get("ExplicitConnection");

                if (res != null
                        && !res.isEmpty()) {

                    contextPrefix =
                            res.replace(
                                    "/",
                                    ".");

                } else if (path != null
                        && !path.isEmpty()) {

                    contextPrefix =
                            path.replace(
                                    "/",
                                    ".");

                } else if (explicit != null
                        && !explicit.startsWith("IFt7")) {

                    contextPrefix =
                            explicit;
                }
            }
        }

        // Normalize the prefix

        contextPrefix =
                contextPrefix
                        .replaceAll(
                                "^\\.+|\\.+$",
                                "")
                        .toLowerCase()
                        .trim();

        // -------------------------------------------------------
        // 3. Build ONE canonical module name
        //
        // IMPORTANT:
        // The YAML command and the filesystem folder MUST use
        // exactly the same normalized module name.
        //
        // Example:
        // Math-Request -> math_request
        // -------------------------------------------------------

        String rawName =
                myModule != null
                        ? myModule.getAttributes()
                                .get("Name")
                        : step.getModule();

        String cleanModuleName =
                normalizeApiModuleName(
                        rawName);

        // Determine the dot prefix, e.g. "v4."
        String dotPrefix =
                getDotPrefixFormat(
                        step,
                        myModule,
                        cleanModuleName);

        // Final AGATE command:
        // rest.v4.math_request
        String fullDotAction =
                protocol
                        + "."
                        + dotPrefix
                        + cleanModuleName;

        // 5. Build the filesystem path

        String folderStructure =
                dotPrefix.replace(
                        ".",
                        "/");

        String finalFolderPath =
                "migration/data/"
                        + appId
                        + "/modules/"
                        + protocol
                        + "/"
                        + folderStructure
                        + cleanModuleName;

        finalFolderPath =
                finalFolderPath.replace(
                        "//",
                        "/");

        finalFolderPath =
                sanitizePathSegments(
                        finalFolderPath);

        /*
         * IMPORTANT:
         *
         * Previously:
         *
         *   step.setAction(fullAction);
         *
         * could produce:
         *
         *   rest.v4.mathrequest
         *
         * while the generated folder was:
         *
         *   modules/rest/v4/math_request
         *
         * Now both use the same canonical name:
         *
         *   rest.v4.math_request
         *   modules/rest/v4/math_request
         */
        step.setAction(
                fullDotAction);

        // 4. Create files on disk
        //    (metadata and request)

        createMetadataJson(
                step,
                protocol,
                myModule,
                contextPrefix,
                finalFolderPath,
                appId);

        createRequestFile(
                step,
                protocol,
                contextPrefix,
                decodedPayload,
                finalFolderPath,
                appId);

        // 5. Generate YAML output
        //    (buffers and step)

        endpointValue =
                ToscaValueTranslator
                        .translateToscaValues(
                                endpointValue);

        printYamlStep(
                step,
                protocol,
                endpointValue);
    }
    
    
    
    private void createRequestFile(CleanStep step, String protocol, String contextPrefix, String decodedPayload, String finalFolderPath, String appId) {
        if ("SOAP".equalsIgnoreCase(protocol)) {
            createRequestXml(step, protocol, contextPrefix, decodedPayload, finalFolderPath, appId);
        } else {
            createRequestJson(step, protocol, contextPrefix, decodedPayload, finalFolderPath, appId); // Existing REST implementation
        }
    }
    // Helper method for decoding TCProperties
    private String extractResourceFromTCProperties(String tcPropsBase64) {
        if (tcPropsBase64 == null || tcPropsBase64.isEmpty()) return "";
        try {
            byte[] compressed = Base64.getDecoder().decode(tcPropsBase64);
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                 Scanner s = new Scanner(gis, "UTF-8").useDelimiter("\\A")) {
                
                String xml = s.hasNext() ? s.next() : "";

                // Regex: find the TCProperty with Name="Resource" and capture Value="..."
                // Pattern: Name="Resource"\s+Value="([^"]+)"
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Name=\"Resource\"\\s+Value=\"([^\"]+)\"");
                java.util.regex.Matcher matcher = pattern.matcher(xml);

                if (matcher.find()) {
                    return matcher.group(1); // Returns "/simulatord/remove_card"
                }
            }
        } catch (Exception e) {
            // Ignore decompression errors
        }
        return "";
    }    

    private String extractMethodeFromTCProperties(String tcPropsBase64) {
        if (tcPropsBase64 == null || tcPropsBase64.isEmpty()) return "";
        try {
            byte[] compressed = Base64.getDecoder().decode(tcPropsBase64);
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                 Scanner s = new Scanner(gis, "UTF-8").useDelimiter("\\A")) {
                
                String xml = s.hasNext() ? s.next() : "";

                // Regex: find the TCProperty with Name="Method" and capture Value="..."
                // Pattern: Name="Resource"\s+Value="([^"]+)"
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Name=\"Method\"\\s+Value=\"([^\"]+)\"");
                java.util.regex.Matcher matcher = pattern.matcher(xml);

                if (matcher.find()) {
                    return matcher.group(1); // Returns "/simulatord/remove_card"
                }
            }
        } catch (Exception e) {
            // Ignore decompression errors
        }
        return "";
    }    

    
    private static String decodeBase64(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) {
            return "";
        }
        try {
            // Remove whitespace that may occur in the JSON export
            String sanitizedData = base64Data.trim().replaceAll("\\s", "");
            byte[] decodedBytes = Base64.getDecoder().decode(sanitizedData);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
    private String cleanExplicitConnection(String rawConn) {
        if (rawConn == null || rawConn.isEmpty()) return "";
        
        // If the value looks like Base64 (a long string without dots, ending with ==)
        if (rawConn.length() > 20 && !rawConn.contains(".")) {
            try {
                String decoded = decodeBase64(rawConn);
                // If the decoded value is JSON, look for a key such as "Name" or "Path"
                // As a fallback, a fixed prefix can be returned if necessary 
                // or parse the JSON if its structure is known.
                if (decoded.contains("\"Name\":\"")) {
                    // Basic extraction of the name from JSON without additional libraries
                    return decoded.split("\"Name\":\"")[1].split("\"")[0].toLowerCase();
                }
            } catch (Exception e) {
                // If decoding fails, return the normalized original value
            }
        }
        return rawConn.toLowerCase().trim();
    }
   
    
    private static Map<String, Module> loadModulesFromOriginalJson(String filePath) throws Exception {
        Map<String, Module> moduleMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(filePath));

        // Read the "Entities" array because it contains all exported objects
        JsonNode entities = root.get("Entities");
        if (entities == null || !entities.isArray()) {
            System.out.println("GRESKA: JSON nema 'Entities' listu!");
            return moduleMap;
        }

        for (JsonNode node : entities) {
            String objectClass = node.path("ObjectClass").asText();
            String surrogate = node.path("Surrogate").asText();

            // Tosca API modules are usually of type "XModule" 
            // but all objects with attributes are mapped
            Module m = new Module();
            m.setSurrogate(surrogate);

            Map<String, String> attrMap = new HashMap<>();
            JsonNode attributes = node.get("Attributes");
            
            if (attributes != null) {
                attributes.fields().forEachRemaining(entry -> {
                    attrMap.put(entry.getKey(), entry.getValue().asText());
                });
            }

            // Also add ObjectClass to the attributes so the existing main logic can use it
            attrMap.put("ModuleClass", objectClass); 
            
            // If the JSON stores Payload elsewhere, add it here
            // TCProperties contains Base64-encoded Tosca configuration data
            m.setAttributes(attrMap);
            
            if (!surrogate.isEmpty()) {
                moduleMap.put(surrogate, m);
            }
        }
        System.out.println("Loaded " + moduleMap.size() + " entities from the Entities array.");
        return moduleMap;
    }
    
 // Pass String contextPrefix as the fourth parameter
    private void createMetadataJson(CleanStep step, String protocol, Module myModule, String contextPrefix, String finalFolderPath, String appId) {
        try {
            Map<String, Object> meta = new LinkedHashMap<>();
            
            // --- Build the URL ---
            String finalContext = contextPrefix;

            // 1. If contextPrefix is empty, try to read it directly from the step values
            if (finalContext == null || finalContext.isEmpty()) {
                if (step.getValues() != null && step.getValuesAsMap().containsKey("Resource")) {
                    finalContext = step.getValuesAsMap().get("Resource").getValue();
                }
            }

            // 2. If it is still empty, check the module attributes (Resource or Path)
            if ((finalContext == null || finalContext.isEmpty()) && myModule != null) {
                finalContext = myModule.getAttributes().getOrDefault("Resource", 
                               myModule.getAttributes().getOrDefault("Path", ""));
            }

            // Normalize the path by replacing dots with slashes and removing leading slashes
            String urlPath = finalContext.replace(".", "/").replaceAll("^/+", "");
            
            // Build the final URL as {{endpoint}}/path
            meta.put("url", "{{endpoint}}/" + urlPath);
            // -----------------------

            String method = myModule != null ? myModule.getAttributes().get("Method") : null;
            if (method == null) {
                String tcProps = myModule.getAttributes().get("TCProperties");
                method = extractMethodeFromTCProperties(tcProps); 
            } else {
                method = "POST";
            }
            
            meta.put("method", (method != null && !method.isEmpty()) ? method.toUpperCase() : "POST");

         // --- Create headers based on the protocol ---
            Map<String, String> headers = new LinkedHashMap<>();
            
            if ("soap".equalsIgnoreCase(protocol)) {
                // SOAP-specific headers
                headers.put("Content-Type", "text/xml;charset=UTF-8");
                headers.put("SOAPAction", "\"\"");
                headers.put("User-Agent", "Agate-HttpClient (Version 1.0.0)");
            } else {
                // Standard REST (JSON) headers
                String ct = myModule != null ? myModule.getAttributes().get("ContentType") : "application/json";
                if (ct == null || ct.isEmpty()) {
                    ct = "application/json;charset=UTF-8";
                }
                headers.put("Content-Type", ct);
            }
            
            meta.put("headers", headers);
            // ---------------------------------------------

            saveApiFile(step, protocol, finalContext, "metadata.json", meta, finalFolderPath, appId);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        
    }
    
    
    
    private void createRequestJson(CleanStep step, String protocol, String contextPrefix, String decodedPayload, String finalFolderPath, String appId) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> finalBody = new LinkedHashMap<>();

            // 1. STEP: Load the base payload from the module first, if available
            if (decodedPayload != null && !decodedPayload.trim().isEmpty()) {
                try {
                    // Convert the static JSON string from the module into a map
                    finalBody = mapper.readValue(decodedPayload, LinkedHashMap.class);
                } catch (Exception jsonEx) {
                    // Handle the case where the module payload is not valid JSON
                    System.err.println("[WARNING] Decoded payload is not valid JSON: " + jsonEx.getMessage());
                }
            }

            // 2. STEP: Override base values with values defined in the test step
            if (step.getValues() != null) {
                Map<String, Object> finalBodyRef = finalBody; // Required for the lambda expression
                step.getValuesAsMap().forEach((key, valObj) -> {
                    if (!key.equalsIgnoreCase("Endpoint") && !key.equalsIgnoreCase("Resource")) {
                        
                        String rawValue = valObj.getValue();
                        String processedValue;
                        
                        // Special handling for card_slot
                        if (key.equalsIgnoreCase("card_slot") && !rawValue.startsWith("{")) {
                            processedValue = "{B[card_slot]}";
                        } else {
                            // Standard translation from Tosca syntax to Agate syntax
                            processedValue = ToscaValueTranslator.translateToscaValues(rawValue);
                        }
                        
                        // Write or override the value in the map. 
                        // For example, a module value such as "pin": "1111" is overridden by a step value such as "{B[pin]}".
                        finalBodyRef.put(key, processedValue);
                    }
                });
            }

//            if ("gino.v2.status".equals(contextPrefix)) {
//                System.out.println("Generišem request za gino.v2.status. Trenutno stanje tela: " + finalBody);
//            }
//
            // 3. STEP: Save the file whenever either payload data or step values are available
            if (!finalBody.isEmpty()) {
                saveApiFile(step, protocol, contextPrefix, "request.json", finalBody, finalFolderPath, appId);
            } else {
                //System.out.println("⚠️ request.json nije kreiran jer su i payload i step.getValues() prazni za korak: " + step.getName());
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to create request.json for step: " + step.getName());
            e.printStackTrace();
        }
    }


    private void createRequestXml(CleanStep step, String protocol, String contextPrefix, String decodedPayload, String finalFolderPath, String appId) {
        try {
            String finalXml = decodedPayload;

            if (decodedPayload != null && !decodedPayload.trim().isEmpty()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(decodedPayload)));

                // Update values from the step using the existing logic
                if (step.getValues() != null && !step.getValues().isEmpty()) {
                    step.getValuesAsMap().forEach((key, valObj) -> {
                        if (!key.equalsIgnoreCase("Endpoint") && !key.equalsIgnoreCase("Resource")) {
                            String rawValue = valObj.getValue();
                            String processedValue = key.equalsIgnoreCase("card_slot") && !rawValue.startsWith("{") 
                                    ? "{B[card_slot]}" 
                                    : ToscaValueTranslator.translateToscaValues(rawValue);

                            NodeList nodes = doc.getElementsByTagName(key);
                            if (nodes.getLength() == 0) {
                                nodes = doc.getElementsByTagNameNS("*", key);
                            }

                            for (int i = 0; i < nodes.getLength(); i++) {
                                Node node = nodes.item(i);
                                node.setTextContent(processedValue);
                            }
                        }
                    });
                }

                // 👉 Remove empty text nodes and unnecessary whitespace from the XML structure
                XPathFactory xpathFactory = XPathFactory.newInstance();
                XPath xpath = xpathFactory.newXPath();
                // This expression finds text nodes that are empty or contain only whitespace
                NodeList emptyTextNodes = (NodeList) xpath.evaluate("//text()[normalize-space()='']", doc, XPathConstants.NODESET);
                
                for (int i = 0; i < emptyTextNodes.getLength(); i++) {
                    Node emptyNode = emptyTextNodes.item(i);
                    emptyNode.getParentNode().removeChild(emptyNode);
                }

                // Transform the cleaned DOM back into an indented XML string
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                // Use indentation for readability
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); 
                
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                finalXml = writer.toString();
            }

            if (finalXml != null && !finalXml.trim().isEmpty()) {
                // Save the XML request in the same module folder
                saveApiXMLFileString(step, protocol, contextPrefix, "request.xml", finalXml, finalFolderPath, appId);
            } else {
                System.out.println("⚠️ request.xml nije kreiran jer je payload prazan za korak: " + step.getName());
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to create request.xml for step: " + step.getName());
            e.printStackTrace();
        }
    }
    
    private void saveApiFile(
            CleanStep step,
            String protocol,
            String contextPrefix,
            String fileName,
            Object content,
            String finalFolderPath,
            String appId) throws Exception {

        System.err.println(
                "[SAVE-API-TRACE-1]"
                + " step='" + step.getName() + "'"
                + " file='" + fileName + "'"
                + " receivedPath=[" + finalFolderPath + "]"
        );

        String resolvedPath =
                ToscaValueTranslator.translateToscaValues(finalFolderPath);

        System.err.println(
                "[SAVE-API-TRACE-2]"
                + " resolvedPath=[" + resolvedPath + "]"
        );

        String sanitizedPath =
                sanitizePathSegments(resolvedPath);

        System.err.println(
                "[SAVE-API-TRACE-3]"
                + " sanitizedPath=[" + sanitizedPath + "]"
        );

        Path path = Paths.get(sanitizedPath);

        Files.createDirectories(path);

        String json =
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(content);

        Files.write(
                path.resolve(fileName),
                json.getBytes(StandardCharsets.UTF_8)
        );
    }
    
    
    // String-based variant used for XML content
    private void saveApiXMLFileString(CleanStep step, String protocol, String contextPrefix, String fileName, String content, String finalFolderPath, String appId) {
        try {
            // 1. Build the path in the same way as for metadata.json
            String cleanContext = contextPrefix.replace(".", "/");
            String moduleFolder = step.getModule().replace(" ", "_").toLowerCase();
            Path path = Paths.get("migration", "data", appId, "modules", protocol, cleanContext, moduleFolder);
            path = Paths.get(finalFolderPath);
            // 2. Create the directory inside the try block
//            System.out.println("createDirectories4="+path);
            Files.createDirectories(path);
            
            // 3. Write the content using Files.writeString
            
         // 1. Build the file path
            Path finalFilePath = path.resolve(fileName);
            
            // 2. Check whether the file already exists before writing
            if (Files.exists(finalFilePath)) {
                //System.out.println("File " + finalFilePath + " already exists");
            } else {
                // 3. Write the file only if it does not already exist
                Files.writeString(finalFilePath, content, StandardCharsets.UTF_8);
                
                //System.out.println("[SUCCESS] Created file: " + finalFilePath);
            }
            
            //System.out.println("✔ SOAP OUTPUT: " + finalFilePath.toAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("❌ Greška prilikom upisa SOAP XML fajla " + fileName + " za korak: " + step.getName());
            e.printStackTrace();
        }
    }
    
    
    private void printYamlStep(CleanStep step, String protocol, String endpointValue) {
        String type = protocol.toUpperCase();
        
        System.out.println("\n      # " + step.getName());
        System.out.println("      - type: " + type);
        System.out.println("        op: " + (step.getOp() != null ? step.getOp().toUpperCase() : "EXEC"));
        
        String rawCondition = step.getCondition();
        if (rawCondition != null && !rawCondition.isBlank()) {
            String formattedCondition = rawCondition.replace("\"", "'");
            System.out.println("        condition: \"" + formattedCondition + "\"");
        }

        // For REST/SOAP, group applicable values under parameters
        if (type.equals("REST") || type.equals("SOAP")) {
            System.out.println("        endpoint: \"" + endpointValue + "\"");
            System.out.println("        command: " + step.getAction());
            System.out.println("        response: " + step.getAction() + "_response");
            
            // Parameter handling
            boolean headerPrinted = false;
            if (step.getValues() != null) {
                for (Map.Entry<String, StepValueDetails> entry : step.getValuesAsMap().entrySet()) {
                    String key = entry.getKey();
                    String val = entry.getValue().getValue();
                    
                    // Skip system keys and variables that are already mapped
                    if (!key.equalsIgnoreCase("Endpoint") && !key.equalsIgnoreCase("Resource") && !val.startsWith("{")) {
                        if (!headerPrinted) {
                            System.out.println("        parameters:");
                            headerPrinted = true;
                        }
                        System.out.println("          " + key + ": \"" + val + "\"");
                    }
                }
            }
        } else {
            // Handling for other module types (GUI, SQL, CMD, etc.)
            // ...
        }
    }
    
    /**
     * Helper method for safely reading a value from CleanStep
     */
    private String getStepValue(CleanStep step, String key) {
        if (step.getValues() != null && step.getValuesAsMap().containsKey(key)) {
            return step.getValuesAsMap().get(key).getValue();
        }
        return ""; // Return an empty string if the key does not exist to avoid null values in YAML
    }
    
    public String getResourceFromTCProperties(String tcPropsBase64) {
        if (tcPropsBase64 == null || tcPropsBase64.isEmpty()) return "";
        
        try {
            // 1. Base64 decoding
            byte[] compressed = Base64.getDecoder().decode(tcPropsBase64);
            
            // 2. GZIP decompression
            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
            Scanner s = new Scanner(gis).useDelimiter("\\A");
            String xml = s.hasNext() ? s.next() : "";
            
            // 3. Extract the Resource tag without using a full XML parser
            if (xml.contains("<Resource>")) {
                return xml.split("<Resource>")[1].split("</Resource>")[0];
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to read TCProperties: " + e.getMessage());
        }
        return "";
    }

    public static String getDotPrefixFormat(CleanStep step, Module myModule, String cleanModuleName) {
        String resource = "";
        if (step.getValues() != null && step.getValuesAsMap().containsKey("Resource")) {
            resource = step.getValuesAsMap().get("Resource").getValue();
        } else {
            resource = getResourceFromModule(myModule);
        }

        String moduleResources = resource; //myModule.getAttributes().get("Resources");
        String dotPrefix = "";
        if (moduleResources != null && !moduleResources.isEmpty()) {
            dotPrefix = moduleResources.replace("/", ".").replace("\\", ".");
            if (dotPrefix.startsWith(".")) dotPrefix = dotPrefix.substring(1);
            if (!dotPrefix.endsWith(".")) dotPrefix += ".";
        }

        return dotPrefix;
    }
    private static String getResourceFromModule(Module module) {
        // Same approach as getHttpMethodFromModule, but search for Name="Resource"
        // Tosca stores the default API path in TCProperties
        String content = getDecompressedTCProperties(module); // existing GZIP logic
        if (content.contains("Name=\"Resource\"")) {
            int index = content.indexOf("Name=\"Resource\"");
            int start = content.indexOf("Value=\"", index) + 7;
            int end = content.indexOf("\"", start);
            return content.substring(start, end);
        }
        return "";
    }    
    private static String getDecompressedTCProperties(Module module) {
        if (module == null) return "";

        String tcPropsBase64 = module.getAttributes().get("TCProperties");
        if (tcPropsBase64 == null || tcPropsBase64.isEmpty()) return "";

        try {
            // 1. Base64 Decode
            byte[] compressed = Base64.getDecoder().decode(tcPropsBase64);
            
            // 2. GZIP Decompress
            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                 BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"))) {
                
                StringBuilder xmlContent = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    xmlContent.append(line);
                }
                return xmlContent.toString();
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to decompress TCProperties: " + e.getMessage());
            return "";
        }
    }
    private static String sanitizePathSegments(String path) {

        if (path == null || path.isBlank()) {
            return path;
        }

        String normalized = path.replace("\\", "/");

        String[] parts = normalized.split("/");

        StringBuilder result = new StringBuilder();

        for (String part : parts) {

            if (part.isBlank()) {
                continue;
            }

            String cleanPart = part
                    .replaceAll("[<>:\"|?*]", "_")
                    .trim();

            if (!result.isEmpty()) {
                result.append("/");
            }

            result.append(cleanPart);
        }

        return result.toString();
    }
    private static String normalizeApiModuleName(
            String name) {

        if (name == null
                || name.isBlank()) {

            return "";
        }

        return name
                .trim()
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9]+",
                        "_")
                .replaceAll(
                        "^_+|_+$",
                        "");
    }   
}