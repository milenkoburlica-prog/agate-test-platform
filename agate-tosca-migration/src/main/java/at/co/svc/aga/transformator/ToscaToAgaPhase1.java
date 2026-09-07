package at.co.svc.aga.transformator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.CleanTestCase;
import at.co.svc.aga.transformator.dto.Module;
import at.co.svc.aga.transformator.dto.StepValueDetails;
import at.co.svc.aga.transformator.utils.MigrationLog;
import at.co.svc.aga.transformator.utils.ProcessAPILogic;
import at.co.svc.aga.transformator.utils.ProcessBuffer;
import at.co.svc.aga.transformator.utils.ProcessDbStep;
import at.co.svc.aga.transformator.utils.ProcessEvaluationStep;
import at.co.svc.aga.transformator.utils.ProcessReusableCall;
import at.co.svc.aga.transformator.utils.ProcessStartProgram;
import at.co.svc.aga.transformator.utils.ProcessVariablesBlock;

public class ToscaToAgaPhase1 {
    private static PrintWriter yamlWriter;
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    public static String lastApiResponseName = "";
    public static String currentFolderPath = "unknown_path";
    public static String currentResourcePath = "unknown_resource";

    public static void main(String[] args) throws Exception {
        // Path to the folder and input file (assuming phase 1 is complete)
        
        String appID= "VAGA";
        System.setProperty("APPLICATION", "VAGA");
        String baseFileName =  "DMP_11_isDMPPatient";
        String jsonDir = "jsonOut";
        String migrationDir = "migration\\\\data";

        String path = System.getProperty("user.dir") + "\\" + jsonDir + "\\";

//        startMigrationTestcases(appID, baseFileName, migrationDir, path);
        startMigrationTemplates(appID, baseFileName, migrationDir, path);
        
        String inputModules = path + baseFileName + ".json";
        String inputLibs = path + baseFileName + "_reusables.json";
        
        List<String> migModules = new ArrayList<String>();
        migModules = ToscaLibraryMigrator.extractedMain(System.getProperty("APPLICATION"), inputModules, inputLibs, migModules);
    }

    public static void startMigrationTestcases(String appID, String baseFileName, String migrationDir, String path) throws IOException {
        String migrationOutDir =
                System.getProperty("user.dir") + "\\" + migrationDir + "\\"
                + appID; //System.getProperty("APPLICATION");

        String yamlOut =
                migrationOutDir + File.separator + baseFileName + ".yaml";

        MigrationLog.debug("Creating migration output directory: " + migrationOutDir);
        Files.createDirectories(Paths.get(migrationOutDir));
        
        yamlWriter = new PrintWriter(new BufferedWriter(new FileWriter(yamlOut, false)));

        MigrationLog.success("YAML output: " + yamlOut);
        
        extractedMainTestcases(baseFileName, path, appID);
        
        if (yamlWriter != null) {
            yamlWriter.flush();
            yamlWriter.close();
        }
    }
    public static List<String> startMigrationTemplates(String appID, String baseFileName, String migrationDir, String path) throws IOException {

//        String yamlOut =
//                migrationOutDir + File.separator + baseFileName + ".yaml";

        
//        yamlWriter = new PrintWriter(new BufferedWriter(new FileWriter(yamlOut, false)));

        //MigrationLog.success("YAML output: " + yamlOut);
        
        List<String> fileTemplatelist = extractedMainTemplates(baseFileName, path, migrationDir, appID);
        
        if (yamlWriter != null) {
            yamlWriter.flush();
            yamlWriter.close();
        }
        return fileTemplatelist;
    }

        public static void writeLine(String line) {
            System.out.println(line);
    
            if (yamlWriter != null) {
                yamlWriter.println(line);
            }
        }
        
        
        public static void extractedMainTestcases(
                String baseFileName,
                String path,
                String appID) {

            String inputTestCases =
                    path + baseFileName + "_testcases.json";

            String inputModules =
                    path + baseFileName + ".json";

            try {

                File testCaseFile =
                        new File(inputTestCases);

                File moduleFile =
                        new File(inputModules);

                MigrationLog.debugSection("TESTCASE MIGRATION INPUT");

                MigrationLog.debug("TestCase file = " + testCaseFile.getAbsolutePath());

                MigrationLog.debug("TestCase exists = " + testCaseFile.exists());

                MigrationLog.debug("Module file = " + moduleFile.getAbsolutePath());

                MigrationLog.debug("Module exists = " + moduleFile.exists());

                if (!testCaseFile.exists()
                        || !moduleFile.exists()) {

                                        MigrationLog.error("Required testcase migration file is missing.");

                    if (!testCaseFile.exists()) {

                        MigrationLog.error("Missing testcase file: " + testCaseFile.getAbsolutePath());
                    }

                    if (!moduleFile.exists()) {

                        MigrationLog.error("Missing module file: " + moduleFile.getAbsolutePath());
                    }

                    return;
                }

                Map<String, Object> root =
                        mapper.readValue(
                                moduleFile,
                                new TypeReference<Map<String, Object>>() {
                                }
                        );

                List<Module> modulesList =
                        mapper.convertValue(
                                root.get("Entities"),
                                new TypeReference<List<Module>>() {
                                }
                        );

                Map<String, Module> moduleMap =
                        new HashMap<>();

                if (modulesList != null) {

                    for (Module m : modulesList) {

                        if (m.getSurrogate() != null) {
                            moduleMap.put(
                                    m.getSurrogate(),
                                    m
                            );
                        }
                    }
                }

                MigrationLog.debug("[TESTCASE] Loaded modules = " + moduleMap.size());

                List<CleanTestCase> testCases =
                        mapper.readValue(
                                testCaseFile,
                                new TypeReference<List<CleanTestCase>>() {
                                }
                        );

                MigrationLog.debug("[TESTCASE] Loaded testcases = " + testCases.size());

                ToscaToAgaPhase1.writeLine(
                        "testCases:"
                );

                for (CleanTestCase tc : testCases) {

                    generateAgateDsl(
                            "",
                            tc,
                            moduleMap,
                            false,
                            appID
                    );
                }

            } catch (Exception e) {

                MigrationLog.error("Error during testcase migration: " + e.getMessage());
            }
        }
        
        public static List<String> extractedMainTemplates(
                String baseFileName,
                String path,
                String migrationDir,
                String appID) {

            List<String> fileTemplatelist = new ArrayList<>();

            String inputTestCases =
                    path + baseFileName + "_templates.json";

            String inputModules =
                    path + baseFileName + ".json";

            try {

                File templateFile =
                        new File(inputTestCases);

                File moduleFile =
                        new File(inputModules);

                /*
                 * Templates are optional.
                 *
                 * If _templates.json does not exist, the TSU simply
                 * contains no Tosca Templates and this phase is skipped.
                 */
                if (!templateFile.exists()) {

                    MigrationLog.info("No Tosca Templates found. Template migration skipped.");

                    return fileTemplatelist;
                }

                /*
                 * If templates exist, the module JSON file is required.
                 */
                if (!moduleFile.exists()) {

                    MigrationLog.error("Module file required for template migration is missing: " + moduleFile.getAbsolutePath());

                    return fileTemplatelist;
                }

                Map<String, Object> root =
                        mapper.readValue(
                                moduleFile,
                                new TypeReference<Map<String, Object>>() {
                                }
                        );

                List<Module> modulesList =
                        mapper.convertValue(
                                root.get("Entities"),
                                new TypeReference<List<Module>>() {
                                }
                        );

                Map<String, Module> moduleMap =
                        new HashMap<>();

                if (modulesList != null) {

                    for (Module m : modulesList) {

                        if (m.getSurrogate() != null) {

                            moduleMap.put(
                                    m.getSurrogate(),
                                    m
                            );
                        }
                    }
                }

                mapper.configure(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        false
                );

                List<CleanTestCase> testCases =
                        mapper.readValue(
                                templateFile,
                                new TypeReference<List<CleanTestCase>>() {
                                }
                        );

                String migrationOutDir =
                        System.getProperty("user.dir")
                                + "\\"
                                + migrationDir
                                + "\\"
                                + appID
                                + "\\template";

                Files.createDirectories(
                        Paths.get(migrationOutDir)
                );

                ToscaToAgaPhase1.writeLine(
                        "testCases:"
                );

                for (CleanTestCase tc : testCases) {

                    String templateYamlOut =
                            migrationOutDir
                                    + "\\"
                                    + tc.name;

                    yamlWriter =
                            new PrintWriter(
                                    new BufferedWriter(
                                            new FileWriter(
                                                    templateYamlOut + ".yaml",
                                                    false
                                            )
                                    )
                            );

                    fileTemplatelist.add(
                            templateYamlOut + ".yaml"
                    );

                    generateAgateDslTemplate(
                            "",
                            tc,
                            moduleMap,
                            true,
                            appID
                    );

                    if (yamlWriter != null) {

                        yamlWriter.flush();
                        yamlWriter.close();
                    }
                }

            } catch (Exception e) {

                MigrationLog.error("Error during template migration: " + e.getMessage());
            }

            return fileTemplatelist;
        }
        
        
    private static void generateAgateDsl(String ident, CleanTestCase tc, Map<String, Module> moduleMap, boolean isTemplate, String appID) throws Exception {
        ToscaToAgaPhase1.writeLine("");
        ToscaToAgaPhase1.writeLine(ident + "  - id: " + tc.getName().replace(" ", "_"));
        ToscaToAgaPhase1.writeLine(ident + "    description: \"" + (tc.getName()) + "\"");
        ToscaToAgaPhase1.writeLine(ident + "    stage: \"*\"");
//        ToscaToAgaPhase1.writeLine("    dataSource: \"data/demo/templates/testdata/users_dd.csv\"");
        //if (isTemplate) 
        {
            ToscaToAgaPhase1.writeLine(ident + "    priority: HIGH");
        }
        
        // 1. Variables - processVariablesBlock ignores steps with conditions
        ProcessVariablesBlock.processVariablesBlock(tc);

        ToscaToAgaPhase1.writeLine(ident + "\n    steps:");
        
        List<CleanStep> steps = tc.getSteps();
//        for (CleanStep step : tc.getSteps()) {
        for (int i = 0; i < steps.size(); i++) {
            CleanStep step = steps.get(i);
            
            String moduleName = step.getModule();
            String condition = step.getCondition();

         // Look ahead to the next step
            CleanStep nextStep = null;
            if (i + 1 < steps.size()) {
                nextStep = steps.get(i + 1);
            }
            
            // 2. Handling for conditional buffers
            if ("TBox Set Buffer".equalsIgnoreCase(moduleName)) {
                {
                    // If a condition exists, handle it here as an EXEC step instead of a variable
                    ProcessBuffer.processInlineBuffer(step, ident); 
                } 
                continue;
            }

            // 3. Other modules
            if ("TBox DB Expert module".equalsIgnoreCase(moduleName) || "TBox DB Run SQL Statement".equalsIgnoreCase(moduleName)) {
                ProcessDbStep.processDbStep(step, ident);
            }
            else if ("TBox Evaluation Tool".equalsIgnoreCase(moduleName)) {
                ProcessEvaluationStep.processEvaluationStep(step, ident);
            }
            else if ("TBox Start Program".equalsIgnoreCase(moduleName)) {
                ProcessStartProgram.processStartProgram(step, ident);
            }
            else if ("ApiModule".equalsIgnoreCase(step.getModuleClass())) {
                ProcessAPILogic.processAPILogic(moduleMap, step, nextStep, appID);   
            }            
            else if ("Reusable".equalsIgnoreCase(step.getType())) {
                ProcessReusableCall.processReusableCall(step);
            }
            else if (moduleName != null && (moduleName.contains("Delete Buffer") || moduleName.contains("DB Open") || moduleName.contains("DB Close"))) {
                MigrationLog.debug("[Skipped] " + moduleName);
            }
            else {
                if (moduleName != null)  { 
                    ToscaToAgaPhase1.writeLine(ident + "      #[Pending] No mapping for: " + moduleName);
                }
            }
        }
    }
    private static void generateAgateDslTemplate(String ident, CleanTestCase tc, Map<String, Module> moduleMap, boolean isTemplate, String appID) throws Exception {
        ToscaToAgaPhase1.writeLine("");
        ToscaToAgaPhase1.writeLine(ident + "  - id: " + tc.getName().replace(" ", "_"));
        ToscaToAgaPhase1.writeLine(ident + "    description: \"" + (tc.getName()) + "\"");
        ToscaToAgaPhase1.writeLine(ident + "    stage: \"*\"");
//        ToscaToAgaPhase1.writeLine("    dataSource: \"data/demo/templates/testdata/users_dd.csv\"");
        //if (isTemplate) 
        {
            ToscaToAgaPhase1.writeLine(ident + "    priority: HIGH");
        }
        
        // 1. Variables - processVariablesBlock ignores steps with conditions
        ProcessVariablesBlock.processVariablesBlock(tc);

        ToscaToAgaPhase1.writeLine(ident + "\n    steps:");
        
        List<CleanStep> steps = tc.getSteps();
        //for (CleanStep step : tc.getSteps()) {
        for (int i = 0; i < steps.size(); i++) {
            CleanStep step = steps.get(i);
            String moduleName = step.getModule();
            //String moduleName = step.getModule();
            ////String condition = step.getCondition();
            
         // Look ahead to the next step
            CleanStep nextStep = null;
            if (i + 1 < steps.size()) {
                nextStep = steps.get(i + 1);
            }
            
            // 2. Handling for conditional buffers
            if ("TBox Set Buffer".equalsIgnoreCase(moduleName)) {
                {
                    // If a condition exists, handle it here as an EXEC step instead of a variable
                    ProcessBuffer.processInlineBuffer(step, ident); 
                } 
                continue;
            }

            // 3. Other modules
            if ("TBox DB Expert module".equalsIgnoreCase(moduleName) || "TBox DB Run SQL Statement".equalsIgnoreCase(moduleName)) {
                ProcessDbStep.processDbStep(step, ident);
            }
            else if ("TBox Evaluation Tool".equalsIgnoreCase(moduleName)) {
                ProcessEvaluationStep.processEvaluationStep(step, ident);
            }
            else if ("TBox Start Program".equalsIgnoreCase(moduleName)) {
                ProcessStartProgram.processStartProgram(step, ident);
            }
            else if ("ApiModule".equalsIgnoreCase(step.getModuleClass())) {
                ProcessAPILogic.processAPILogic(moduleMap, step, nextStep,appID);   
            }            
            else if ("Reusable".equalsIgnoreCase(step.getType())) {
                ProcessReusableCall.processReusableCall(step);
            }
            else if (moduleName != null && (moduleName.contains("Delete Buffer") || moduleName.contains("DB Open") || moduleName.contains("DB Close"))) {
                MigrationLog.debug("[Skipped] " + moduleName);
            }
            else {
                if (moduleName != null)  { 
                    ToscaToAgaPhase1.writeLine(ident + "      #[Pending] No mapping for: " + moduleName);
                }
            }
        }
    }
    
}
