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
import at.co.svc.aga.transformator.utils.MigrationLog;
import at.co.svc.aga.transformator.utils.MigrationReport;
import at.co.svc.aga.transformator.utils.ProcessAPILogic;
import at.co.svc.aga.transformator.utils.ProcessBuffer;
import at.co.svc.aga.transformator.utils.ProcessDbStep;
import at.co.svc.aga.transformator.utils.ProcessEvaluationStep;
import at.co.svc.aga.transformator.utils.ProcessFileStep;
import at.co.svc.aga.transformator.utils.ProcessReusableCall;
import at.co.svc.aga.transformator.utils.ProcessStartProgram;
import at.co.svc.aga.transformator.utils.ProcessVariablesBlock;
import at.co.svc.aga.transformator.utils.ProcessWait;

public class ToscaToAgaPhase1 {

    private static PrintWriter yamlWriter;

    private static final ObjectMapper mapper =
            new ObjectMapper()
                    .configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false
                    );

    public static String lastApiResponseName = "";

    public static String currentFolderPath =
            "unknown_path";

    public static String currentResourcePath =
            "unknown_resource";

    private static String currentYamlOutputPath =
            "<unknown>";


    // =========================================================
    // MAIN
    // =========================================================

    public static void main(
            String[] args) throws Exception {

        String appID =
                "VAGA";

        System.setProperty(
                "APPLICATION",
                "VAGA"
        );

        String baseFileName =
                "DMP_11_isDMPPatient";

        String jsonDir =
                "jsonOut";

        String migrationDir =
                "migration\\\\data";

        String path =
                System.getProperty("user.dir")
                        + "\\"
                        + jsonDir
                        + "\\";

        // startMigrationTestcases(
        //         appID,
        //         baseFileName,
        //         migrationDir,
        //         path
        // );

        startMigrationTemplates(
                appID,
                baseFileName,
                migrationDir,
                path
        );

        String inputModules =
                path
                        + baseFileName
                        + ".json";

        String inputLibs =
                path
                        + baseFileName
                        + "_reusables.json";

        List<String> migModules =
                new ArrayList<>();

        migModules =
                ToscaLibraryMigrator.extractedMain(
                        System.getProperty("APPLICATION"),
                        inputModules,
                        inputLibs,
                        migModules
                );
    }


    // =========================================================
    // START TESTCASE MIGRATION
    // =========================================================

    public static void startMigrationTestcases(
            String appID,
            String baseFileName,
            String migrationDir,
            String path) throws IOException {

        String migrationOutDir =
                System.getProperty("user.dir")
                        + "\\"
                        + migrationDir
                        + "\\"
                        + appID;

        String yamlOut =
                migrationOutDir
                        + File.separator
                        + baseFileName
                        + ".yaml";

        currentYamlOutputPath =
                yamlOut;

        MigrationLog.debug(
                "Creating migration output directory: "
                        + migrationOutDir
        );

        Files.createDirectories(
                Paths.get(
                        migrationOutDir
                )
        );

        yamlWriter =
                new PrintWriter(
                        new BufferedWriter(
                                new FileWriter(
                                        yamlOut,
                                        false
                                )
                        )
                );

        MigrationLog.success(
                "YAML output: "
                        + yamlOut
        );

        extractedMainTestcases(
                baseFileName,
                path,
                appID
        );

        if (yamlWriter != null) {

            yamlWriter.flush();
            yamlWriter.close();
        }
    }


    // =========================================================
    // START TEMPLATE MIGRATION
    // =========================================================

    public static List<String> startMigrationTemplates(
            String appID,
            String baseFileName,
            String migrationDir,
            String path) throws IOException {

        List<String> fileTemplateList =
                extractedMainTemplates(
                        baseFileName,
                        path,
                        migrationDir,
                        appID
                );

        if (yamlWriter != null) {

            yamlWriter.flush();
            yamlWriter.close();
        }

        return fileTemplateList;
    }


    // =========================================================
    // WRITE YAML LINE
    // =========================================================

    public static void writeLine(
            String line) {

        System.out.println(
                line
        );

        if (yamlWriter != null) {

            yamlWriter.println(
                    line
            );
        }
    }


    // =========================================================
    // TESTCASE MIGRATION
    // =========================================================

    public static void extractedMainTestcases(
            String baseFileName,
            String path,
            String appID) {

        String inputTestCases =
                path
                        + baseFileName
                        + "_testcases.json";

        String inputModules =
                path
                        + baseFileName
                        + ".json";

        try {

            File testCaseFile =
                    new File(
                            inputTestCases
                    );

            File moduleFile =
                    new File(
                            inputModules
                    );

            MigrationLog.debugSection(
                    "TESTCASE MIGRATION INPUT"
            );

            MigrationLog.debug(
                    "TestCase file = "
                            + testCaseFile.getAbsolutePath()
            );

            MigrationLog.debug(
                    "TestCase exists = "
                            + testCaseFile.exists()
            );

            MigrationLog.debug(
                    "Module file = "
                            + moduleFile.getAbsolutePath()
            );

            MigrationLog.debug(
                    "Module exists = "
                            + moduleFile.exists()
            );


            if (!testCaseFile.exists()
                    || !moduleFile.exists()) {

                MigrationLog.error(
                        "Required testcase migration file is missing."
                );

                if (!testCaseFile.exists()) {

                    MigrationLog.error(
                            "Missing testcase file: "
                                    + testCaseFile.getAbsolutePath()
                    );
                }

                if (!moduleFile.exists()) {

                    MigrationLog.error(
                            "Missing module file: "
                                    + moduleFile.getAbsolutePath()
                    );
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

                for (Module module :
                        modulesList) {

                    if (module.getSurrogate() != null) {

                        moduleMap.put(
                                module.getSurrogate(),
                                module
                        );
                    }
                }
            }


            MigrationLog.debug(
                    "[TESTCASE] Loaded modules = "
                            + moduleMap.size()
            );


            List<CleanTestCase> testCases =
                    mapper.readValue(
                            testCaseFile,
                            new TypeReference<List<CleanTestCase>>() {
                            }
                    );


            MigrationLog.debug(
                    "[TESTCASE] Loaded testcases = "
                            + testCases.size()
            );


            ToscaToAgaPhase1.writeLine(
                    "testCases:"
            );


            for (CleanTestCase testCase :
                    testCases) {

                generateAgateDsl(
                        "",
                        testCase,
                        moduleMap,
                        false,
                        appID
                );
            }

        } catch (Exception e) {

            MigrationLog.error(
                    "Error during testcase migration: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // TEMPLATE MIGRATION
    // =========================================================

    public static List<String> extractedMainTemplates(
            String baseFileName,
            String path,
            String migrationDir,
            String appID) {

        List<String> fileTemplateList =
                new ArrayList<>();


        String inputTestCases =
                path
                        + baseFileName
                        + "_templates.json";

        String inputModules =
                path
                        + baseFileName
                        + ".json";


        try {

            File templateFile =
                    new File(
                            inputTestCases
                    );

            File moduleFile =
                    new File(
                            inputModules
                    );


            /*
             * Templates are optional.
             */
            if (!templateFile.exists()) {

                MigrationLog.info(
                        "No Tosca Templates found. Template migration skipped."
                );

                return fileTemplateList;
            }


            /*
             * Modules are required if templates exist.
             */
            if (!moduleFile.exists()) {

                MigrationLog.error(
                        "Module file required for template migration is missing: "
                                + moduleFile.getAbsolutePath()
                );

                return fileTemplateList;
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

                for (Module module :
                        modulesList) {

                    if (module.getSurrogate() != null) {

                        moduleMap.put(
                                module.getSurrogate(),
                                module
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
                    Paths.get(
                            migrationOutDir
                    )
            );


            ToscaToAgaPhase1.writeLine(
                    "testCases:"
            );


            for (CleanTestCase testCase :
                    testCases) {

                String templateYamlOut =
                        migrationOutDir
                                + "\\"
                                + testCase.name;


                currentYamlOutputPath =
                        templateYamlOut
                                + ".yaml";


                yamlWriter =
                        new PrintWriter(
                                new BufferedWriter(
                                        new FileWriter(
                                                templateYamlOut
                                                        + ".yaml",
                                                false
                                        )
                                )
                        );


                fileTemplateList.add(
                        templateYamlOut
                                + ".yaml"
                );


                generateAgateDslTemplate(
                        "",
                        testCase,
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

            MigrationLog.error(
                    "Error during template migration: "
                            + e.getMessage()
            );
        }


        return fileTemplateList;
    }


    // =========================================================
    // GENERATE TESTCASE YAML
    // =========================================================

    private static void generateAgateDsl(
            String ident,
            CleanTestCase testCase,
            Map<String, Module> moduleMap,
            boolean isTemplate,
            String appID) throws Exception {


        ToscaToAgaPhase1.writeLine(
                ""
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "  - id: "
                        + testCase.getName()
                        .replace(
                                " ",
                                "_"
                        )
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "    description: \""
                        + testCase.getName()
                        + "\""
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "    stage: \"*\""
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "    priority: HIGH"
        );


        ProcessVariablesBlock.processVariablesBlock(
                testCase
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "\n    steps:"
        );


        /*
         * Existing modules and the new structured control-flow
         * are now processed through one method.
         */
        processSteps(
                testCase.getSteps(),
                ident,
                moduleMap,
                appID
        );
    }


    // =========================================================
    // GENERATE TEMPLATE YAML
    // =========================================================

    private static void generateAgateDslTemplate(
            String ident,
            CleanTestCase testCase,
            Map<String, Module> moduleMap,
            boolean isTemplate,
            String appID) throws Exception {


        ToscaToAgaPhase1.writeLine(
                ""
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "  - id: "
                        + testCase.getName()
                        .replace(
                                " ",
                                "_"
                        )
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "    description: \""
                        + testCase.getName()
                        + "\""
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "    stage: \"*\""
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "    priority: HIGH"
        );


        ProcessVariablesBlock.processVariablesBlock(
                testCase
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "\n    steps:"
        );


        processSteps(
                testCase.getSteps(),
                ident,
                moduleMap,
                appID
        );
    }


    // =========================================================
    // PROCESS STEPS
    // =========================================================
    //
    // IMPORTANT:
    //
    // This method contains the old step dispatch logic.
    //
    // LOOP and BREAK are handled BEFORE module-based dispatch.
    //
    // All normal steps continue through the same module handling
    // as before.
    // =========================================================

    private static void processSteps(
            List<CleanStep> steps,
            String ident,
            Map<String, Module> moduleMap,
            String appID) throws Exception {

        if (steps == null) {
            return;
        }

        for (int i = 0;
             i < steps.size();
             i++) {

            CleanStep step =
                    steps.get(i);

            if (step == null) {
                continue;
            }

            String type =
                    step.getType() != null
                            ? step.getType()
                            : "";


            // =================================================
            // LOOP
            // =================================================

            if ("LOOP".equalsIgnoreCase(
                    type)) {

                writeLoop(
                        step,
                        ident,
                        moduleMap,
                        appID
                );

                continue;
            }


            // =================================================
            // BREAK
            // =================================================

            if ("BREAK".equalsIgnoreCase(
                    type)) {

                writeBreak(
                        step,
                        ident
                );

                continue;
            }


            // =================================================
            // EXISTING NORMAL STEP LOGIC
            // =================================================

            String moduleName =
                    step.getModule();

            System.err.println(
                    "[STEP-DISPATCH-TRACE]"
                            + " name='" + step.getName() + "'"
                            + " module='" + moduleName + "'"
                            + " type='" + step.getType() + "'"
                            + " moduleClass='" + step.getModuleClass() + "'"
            );
            

            CleanStep nextStep =
                    null;


            if (i + 1 < steps.size()) {

                nextStep =
                        steps.get(
                                i + 1
                        );
            }


            // -------------------------------------------------
            // BUFFER
            // -------------------------------------------------

            if ("TBox Set Buffer".equalsIgnoreCase(
                    moduleName)) {

                ProcessBuffer.processInlineBuffer(
                        step,
                        ident
                );

                continue;
            }


            // -------------------------------------------------
            // DATABASE
            // -------------------------------------------------

            if (moduleName != null
                    && (moduleName.startsWith("TBox DB Expert module")
                    || moduleName.startsWith("TBox DB Run SQL Statement"))) {
                
                ProcessDbStep.processDbStep(
                        step,
                        ident
                );

                continue;
            }


            // -------------------------------------------------
            // WAIT
            // -------------------------------------------------

            if ("TBox Wait".equalsIgnoreCase(
                    moduleName)) {

                ProcessWait.processWait(
                        step,
                        ident
                );

                continue;
            }


            // -------------------------------------------------
            // EVALUATION
            // -------------------------------------------------

            if ("TBox Evaluation Tool".equalsIgnoreCase(
                    moduleName)) {

                ProcessEvaluationStep.processEvaluationStep(
                        step,
                        ident
                );

                continue;
            }


            // -------------------------------------------------
            // START PROGRAM
            // -------------------------------------------------

            if ("TBox Start Program".equalsIgnoreCase(
                    moduleName)) {

                ProcessStartProgram.processStartProgram(
                        step,
                        ident
                );

                continue;
            }


            // -------------------------------------------------
            // FILE
            // -------------------------------------------------

            if ("TBox Copy File".equalsIgnoreCase(
                    moduleName)) {

                ProcessFileStep.processCopyFile(
                        step,
                        ident
                );

                continue;
            }


            // -------------------------------------------------
            // API
            // -------------------------------------------------

            if ("ApiModule".equalsIgnoreCase(
                    step.getModuleClass())) {

                ProcessAPILogic.processAPILogic(
                        moduleMap,
                        step,
                        nextStep,
                        appID
                );

                continue;
            }


            // -------------------------------------------------
            // REUSABLE
            // -------------------------------------------------

            if ("Reusable".equalsIgnoreCase(
                    type)) {

                ProcessReusableCall.processReusableCall(
                        step
                );

                continue;
            }


            // -------------------------------------------------
            // KNOWN SKIPPED MODULES
            // -------------------------------------------------

            if (moduleName != null
                    && (moduleName.contains(
                    "Delete Buffer")
                    || moduleName.contains(
                    "DB Open")
                    || moduleName.contains(
                    "DB Close"))) {

                MigrationLog.debug(
                        "[Skipped] "
                                + moduleName
                );

                continue;
            }


            // -------------------------------------------------
            // UNKNOWN MODULE
            // -------------------------------------------------

            if (moduleName != null) {

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "      #[Pending] No mapping for: "
                                + moduleName
                );

                MigrationReport.registerUnprocessedModule(
                        moduleName,
                        step.getName(),
                        currentYamlOutputPath
                );
            }
        }
    }

    // =========================================================
    // LOOP YAML
    // =========================================================

    private static void writeLoop(
            CleanStep step,
            String ident,
            Map<String, Module> moduleMap,
            String appID) throws Exception {


        ToscaToAgaPhase1.writeLine(
                ""
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "      - type: LOOP"
        );


        Integer maxIterations =
                step.getMaxIterations();


        if (maxIterations != null) {

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        maxIterations: "
                            + maxIterations
            );
        }


        if (step.getCondition() != null
                && !step.getCondition()
                .isBlank()) {


            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        condition: \""
                            + escapeYaml(
                            step.getCondition()
                    )
                            + "\""
            );
        }


        ToscaToAgaPhase1.writeLine(
                ident
                        + "        steps:"
        );


        /*
         * Nested steps need four additional spaces.
         *
         * Top level:
         *
         *       - type: LOOP
         *
         * Nested:
         *
         *           - type: SQL
         */
        processSteps(
                step.getSteps(),
                ident + "    ",
                moduleMap,
                appID
        );
    }


    // =========================================================
    // BREAK YAML
    // =========================================================

    private static void writeBreak(
            CleanStep step,
            String ident) {


        ToscaToAgaPhase1.writeLine(
                ""
        );


        ToscaToAgaPhase1.writeLine(
                ident
                        + "      - type: BREAK"
        );


        if (step.getCondition() != null
                && !step.getCondition()
                .isBlank()) {


            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        condition: \""
                            + escapeYaml(
                            step.getCondition()
                    )
                            + "\""
            );

        } else {

            MigrationLog.info(
                    "[Review] BREAK step has no condition"
                            + " | step="
                            + step.getName()
            );
        }
    }


    // =========================================================
    // YAML ESCAPE
    // =========================================================

    private static String escapeYaml(
            String value) {


        if (value == null) {
            return "";
        }


        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                );
    }
}