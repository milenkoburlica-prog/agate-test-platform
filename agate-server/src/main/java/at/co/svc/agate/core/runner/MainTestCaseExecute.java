package at.co.svc.agate.core.runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import at.co.svc.agate.core.debug.DebugQuitException;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.register.TestExecutor;
import at.co.svc.agate.core.dsl.register.YamlTestCaseLoader;
import at.co.svc.agate.core.dsl.resolver.ReusablePathResolver;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.env.EnvironmentManager;
import at.co.svc.agate.core.error.AgateStepException;
import at.co.svc.agate.core.interfaces.TestLogger;
import at.co.svc.agate.core.report.JsonReportEngine;
import at.co.svc.agate.core.report.ReportEngine;
import at.co.svc.agate.core.project.ProjectContext;
import at.co.svc.agate.core.project.ProjectRuntime;
import at.co.svc.agate.core.reference.ReferencePathResolver;
import at.co.svc.agate.server.validation.StartupValidator;
import at.co.svc.agate.engine.gui.GuiEngine;

public class MainTestCaseExecute {

    public static void main(String[] args) {

        try {

            System.setProperty(
                    "allure.results.directory",
                    "target/trash-allure"
            );

            System.setProperty(
                    "allure.enabled",
                    "false"
            );

            // Targeted YAML file
            String yamlFile =
                    "data/demo/001_windows_cmd_basic_test.yaml";

            yamlFile =
                    "data/demo/003_table_buffer_demo_test.yaml";

            yamlFile =
                    "data/demo/004_reusable_basic_test.yaml";


            // SVC
            // yamlFile = "data/svc/102_oc_basic_test.yaml";
            // yamlFile = "data/svc/102_windows_cmd_basic_test.yaml";
            // yamlFile = "data/demo/005_wait_basic_test.yaml";
            // yamlFile = "data/demo/006_buffer_basic_test.yaml";
            // yamlFile = "data/demo/007_rest_basic_test.yaml";
            // yamlFile = "data/svc/102_sql_basic_test.yaml";
            // yamlFile = "data/svc/103_soap_with_resuable_test.yaml";

            yamlFile =
                    "data/muhi/Instance_CheckStatus.yaml";

            yamlFile =
                    "data/muhi/Instance_absolutesBeschaeftigungsverbotEinmelden.yaml";

            yamlFile =
                    "data/CRS/CRS_V3.yaml";

            yamlFile =
                    "data/CPS/CPS.yaml";

            yamlFile =
                    "data/VDAS_SS12/Instance_retrieveVersichertendatenPerStichtag_syst_aut1_a.yaml";

            yamlFile =
                    "data/DMP/Instance_dmp_11_isDMPPatient.yaml";

            yamlFile =
                    "data/DB/TBox_DB_WithConstraint.yaml";

            yamlFile =
                    "data/demo/001_windows_cmd_basic_test.yaml";


            // DEMO
            yamlFile =
                    "data/demo/001_windows_cmd_basic_test.yaml";

            yamlFile =
                    "data/demo/002_env_buffer_demo_test.yaml";

            yamlFile =
                    "data/demo/003_table_buffer_demo_test.yaml";

            yamlFile =
                    "data/demo/004_reusable_basic_test.yaml";

            yamlFile =
                    "data/demo/005_wait_basic_test.yaml";

            yamlFile =
                    "data/demo/006_buffer_basic_test.yaml";

            yamlFile =
                    "data/demo/007_rest_basic_test.yaml";

            yamlFile =
                    "data/demo/008_windows_cmd_extendet_stage_prio_test.yaml";


            // SVC
            yamlFile =
                    "data/svc/002_rest_basic_test.yaml";

            yamlFile =
                    "data/svc/002_soap_basic_test.yaml";

            yamlFile =
                    "data/svc/002_sql_basic_test.yaml";

            yamlFile =
                    "data/svc/002_windows_cmd_basic_test.yaml";

            yamlFile =
                    "data/svc/003_soap_with_resuable_test.yaml";

            yamlFile =
                    "data/svc/101_env_env_buffer_demo_test.yaml";

            yamlFile =
                    "data/svc/101_env_users_buffer_demo_test.yaml";

            yamlFile =
                    "data/svc/101_table_buffer_demo_test.yaml";

            yamlFile =
                    "data/svc/102_oc_basic_test.yaml";


            // demo
            yamlFile =
                    "data/cmd/cmd_engine_demo.yaml";

            yamlFile =
                    "data/cmd/variables_demo.yaml";

            yamlFile =
                    "data/cmd/buffer_engine_demo.yaml";

            yamlFile =
                    "data/cmd/wait_engine_demo.yaml";

            yamlFile =
                    "data/AUM/Instance_meldungAnlegen_V8_SYST_AUT1.yaml";

            yamlFile =
                    "data/cmd/sql_engine_demo.yaml";

            yamlFile =
                    "data/cmd/variables_demo.yaml";

            yamlFile =
                    "data/cmd/oc_engine_svc.yaml";

            yamlFile =
                    "data/cmd/reusable_engine_demo.yaml";

            yamlFile =
                    "data/cmd/soap_engine_svc_ws_security.yaml";

            yamlFile =
                    "data/cmd/soap_engine_demo.yaml";

            yamlFile =
                    "data/cmd/soap_engine_svc_mtom_download_attachement.yaml";

            yamlFile =
                    "data/cmd/soap_engine_svc_mtom_upload_attachement.yaml";

            yamlFile =
                    "data/AUM/Instance_meldungAnlegen_V8_SYST_AUT1.yaml";

            yamlFile =
                    "data/AUM/Orig_Instance_meldungAnlegen_V8_SYST_AUT1.yaml";

            yamlFile =
                    "data/AUM/Korr2_Instance_meldungAnlegen_V8_SYST_AUT1.yaml";

            yamlFile =
                    "data/MUHI/Instance_absolutesBeschaeftigungsverbotEinmelden.yaml";

            yamlFile =
                    "data/petshop/petshop_demo.yaml";

            yamlFile =
                    "data/dmp11/Instance_dmp_11_getAdminPatientenInformationen.yaml";

            yamlFile =
                    "data/svc/003_oc_svc_basic_demo.yaml";


            // Current test
            yamlFile =
                    "data/ssh/new_ssh.yaml";

            String user =
                    "Milenko";

            String instance =
                    "KVW_ECS_SYST";

            instance =
                    "ECS_SYST_AUT1";

            String apps =
                    yamlFile.split("/")[1];

            start(
                    user,
                    instance,
                    apps,
                    yamlFile
            );

        } catch (AgateStepException e) {

            /*
             * AgateStepException has already been formatted and printed
             * by the YAML loading / validation layer.
             *
             * Do NOT print the stack trace again here.
             */
            System.exit(1);

        } catch (Exception e) {

            /*
             * Unexpected technical error.
             * Keep the stack trace because this is not a controlled
             * AGATE validation error.
             */
            e.printStackTrace();
            System.exit(1);
        }
    }


    // =========================================================================
    // ORIGINAL START METHOD (UNTOUCHED FOR RETROGRADE/TEST COMPLIANCE)
    // =========================================================================

    public static void start(
            String user,
            String instance,
            String apps,
            String yamlFile)
            throws Exception {

        System.setProperty(
                "PERSON",
                user
        );

        System.setProperty(
                "INSTANCE",
                instance
        );

        EnvironmentManager.init();

        System.setProperty(
                "APPLICATION",
                apps
        );

        System.setProperty(
                "PLAYWRIGHT_BROWSERS_PATH",
                "0"
        );

        String sessionId =
                "LOCAL-SESSION";

        // 1. Define the Console Logger
        TestLogger consoleLogger =
                msg -> System.out.println(msg);

        // 2. Initialize the Executor and ReportEngine
        TestExecutor executor =
                new TestExecutor(consoleLogger);

        ReportEngine reportEngine =
                new ReportEngine();

        // Load test cases from the specified YAML file
        long loadStartTime =
                System.currentTimeMillis();

        List<TestCase> testCases =
                YamlTestCaseLoader.loadTestCases(
                        yamlFile
                );

        long loadDuration =
                System.currentTimeMillis()
                        - loadStartTime;

        System.out.println(
                ConsoleColors.CYAN
                        + ">>> YAML loaded in: "
                        + loadDuration
                        + " ms"
                        + ConsoleColors.RESET
        );

        int totalTests =
                testCases.size();

        int passedCount =
                0;

        int failedCount =
                0;

        long suiteStartTime =
                System.currentTimeMillis();

        List<String> failedTestNames =
                new ArrayList<>();

        try {

            for (TestCase tc : testCases) {

                printCleanHeader(tc);

                long startTime =
                        System.currentTimeMillis();

                boolean passed =
                        true;

                String errorMessage =
                        null;

                List<String> currentTestLogs =
                        new ArrayList<>();

                TestLogger customLogger =
                        msg -> {

                            System.out.println(msg);
                            currentTestLogs.add(msg);
                        };

                TestExecutor executionWithLogs =
                        new TestExecutor(
                                customLogger
                        );

                try {

                    executionWithLogs.executeTestCase(
                            tc,
                            yamlFile
                    );

                    passedCount++;

                } catch (DebugQuitException e) {

                    throw e;

                } catch (Exception e) {

                    passed =
                            false;

                    failedCount++;

                    failedTestNames.add(
                            (tc.getName() != null
                                    ? tc.getName()
                                    : "Unnamed Test Case")
                                    + " ["
                                    + Paths.get(yamlFile)
                                    .getFileName()
                                    + "]"
                    );

                    errorMessage =
                            e.getMessage() != null
                                    ? e.getMessage().trim()
                                    : "Unknown Error";

                    if (Boolean.getBoolean(
                            "agate.debug"
                    )) {

                        e.printStackTrace();
                    }

                } finally {

                    long duration =
                            System.currentTimeMillis()
                                    - startTime;

                    printCleanFooter(
                            tc,
                            passed,
                            duration,
                            errorMessage
                    );

                    reportEngine.addRecord(
                            tc,
                            yamlFile,
                            passed,
                            duration,
                            errorMessage,
                            currentTestLogs
                    );
                }
            }

        } finally {

            // 3. Global Resource Cleanup
            System.out.println(
                    "\nFinalizing suite and cleaning up resources..."
            );

            GuiEngine.cleanup(
                    sessionId,
                    consoleLogger
            );
        }

        // 4. Final Summary in console
        long totalDuration =
                System.currentTimeMillis()
                        - suiteStartTime;

        printFinalSummary(
                totalTests,
                passedCount,
                failedCount,
                totalDuration,
                failedTestNames
        );

        // 5. Run report pipelines
        try {

            reportEngine.generateHtmlReport(
                    "target/reports/TestReport.html"
            );

            System.out.println(
                    ">>> HTML report successfully generated at: "
                            + "target/reports/TestReport.html"
            );

        } catch (Exception e) {

            e.printStackTrace();

            System.err.println(
                    "Error creating Allure reports: "
                            + e.getMessage()
            );
        }

        // Force exit
        System.exit(
                failedCount == 0
                        ? 0
                        : 1
        );
    }


    // =========================================================================
    // NEW START2 METHOD (DYNAMIC ORCHESTRATION LAYER FOR CLI/BAT POOLS)
    // =========================================================================

    public static void start2(
            ProjectContext projectContext,
            String user,
            String instance,
            String apps,
            String file,
            String targetTestCase,
            String targetPriority)
            throws Exception {

        // Validate the complete startup context before EnvironmentManager,
        // YAML loading or any test execution is started.
        StartupValidator.validateOrThrow(
                projectContext,
                user,
                instance,
                apps,
                file
        );

        ProjectRuntime.initialize(
                projectContext
        );

        ReusablePathResolver.setProjectContext(
                projectContext
        );

        ReferencePathResolver.setProjectContext(
                projectContext
        );

        System.setProperty(
                "PERSON",
                user
        );

        System.setProperty(
                "INSTANCE",
                instance
        );

        // Postavljamo prioritet kao sistemski parametar
        System.setProperty(
                "PRIORITY",
                targetPriority != null
                        ? targetPriority
                        .trim()
                        .toUpperCase()
                        : ""
        );

        EnvironmentManager.init();

        System.setProperty(
                "APPLICATION",
                apps.toLowerCase()
        );

        System.setProperty(
                "PLAYWRIGHT_BROWSERS_PATH",
                "0"
        );

        String sessionId =
                "LOCAL-SESSION";

        List<String> yamlFilesToExecute =
                new ArrayList<>();

        String sanitizedApps =
                apps.toLowerCase()
                        .trim();

        // -----------------------------------------------------------------
        // 1. Resolve execution file scope strategy
        // -----------------------------------------------------------------

        if (file == null
                || file.trim().isEmpty()) {

            Path appFolder =
                    projectContext
                            .getTestsRoot()
                            .resolve(sanitizedApps)
                            .normalize();

            yamlFilesToExecute =
                    Files.walk(
                                    appFolder,
                                    1
                            )
                            .filter(
                                    Files::isRegularFile
                            )
                            .map(
                                    Path::toString
                            )
                            .filter(
                                    f ->
                                            f.endsWith(".yaml")
                                                    || f.endsWith(".yml")
                            )
                            .sorted()
                            .collect(
                                    Collectors.toList()
                            );

            System.out.println(
                    ">>> Found "
                            + yamlFilesToExecute.size()
                            + " test suites inside ["
                            + sanitizedApps
                            + "] for execution."
            );

        } else {

            String targetFile =
                    file.trim();

            if (!targetFile.endsWith(".yaml")
                    && !targetFile.endsWith(".yml")) {

                targetFile +=
                        ".yaml";
            }

            Path singlePath =
                    projectContext
                            .getTestsRoot()
                            .resolve(sanitizedApps)
                            .resolve(targetFile)
                            .normalize();

            yamlFilesToExecute.add(
                    singlePath.toString()
            );
        }

        // -----------------------------------------------------------------
        // 2. Initialize reporting engines
        // -----------------------------------------------------------------

        TestLogger consoleLogger =
                msg -> System.out.println(msg);

        ReportEngine reportEngine =
                new ReportEngine();

        JsonReportEngine jsonReportEngine =
                new JsonReportEngine(
                        sanitizedApps,
                        System.getProperty(
                                "INSTANCE",
                                "UNKNOWN"
                        )
                );

        int totalTestsExecuted =
                0;

        int passedCount =
                0;

        int failedCount =
                0;

        long suiteStartTime =
                System.currentTimeMillis();

        List<String> failedTestNames =
                new ArrayList<>();

        try {

            // -------------------------------------------------------------
            // 3. Process resolved suite iterations
            // -------------------------------------------------------------

            for (String yamlFile : yamlFilesToExecute) {

                String formattedYamlPath =
                        yamlFile.replace(
                                "\\",
                                "/"
                        );

                List<TestCase> allTestCasesInFile;

                try {

                    allTestCasesInFile =
                            YamlTestCaseLoader.loadTestCases(
                                    formattedYamlPath
                            );

                } catch (AgateStepException e) {

                    printAgateError(
                            "Test suite cannot be loaded",
                            formattedYamlPath,
                            e
                    );

                    System.exit(1);
                    return;
                }

                for (TestCase tc : allTestCasesInFile) {

                    // -----------------------------------------------------
                    // Target TestCase filter
                    // -----------------------------------------------------

                    if (targetTestCase != null
                            && !targetTestCase
                            .trim()
                            .isEmpty()) {

                        boolean matchesId =
                                tc.getName() != null
                                        && tc.getName()
                                        .equalsIgnoreCase(
                                                targetTestCase.trim()
                                        );

                        boolean matchesName =
                                tc.getName() != null
                                        && tc.getName()
                                        .equalsIgnoreCase(
                                                targetTestCase.trim()
                                        );

                        if (!matchesId
                                && !matchesName) {

                            continue;
                        }
                    }

                    // -----------------------------------------------------
                    // FILTER 1: STAGE
                    // -----------------------------------------------------

                    if (tc.getStage() != null
                            && !tc.getStage()
                            .trim()
                            .isEmpty()) {

                        String rawStage =
                                tc.getStage()
                                        .trim();

                        if (!rawStage.equals("*")) {

                            boolean stageMatchFound =
                                    false;

                            String currentSystemInstance =
                                    System.getProperty(
                                                    "INSTANCE",
                                                    ""
                                            )
                                            .trim()
                                            .toLowerCase();

                            if (rawStage.contains(",")) {

                                String[] tokens =
                                        rawStage.split(",");

                                for (String token : tokens) {

                                    if (token.trim()
                                            .equalsIgnoreCase(
                                                    currentSystemInstance
                                            )) {

                                        stageMatchFound =
                                                true;

                                        break;
                                    }
                                }

                            } else {

                                if (rawStage
                                        .equalsIgnoreCase(
                                                currentSystemInstance
                                        )) {

                                    stageMatchFound =
                                            true;
                                }
                            }

                            if (!stageMatchFound) {

                                System.out.println(
                                        ConsoleColors.YELLOW
                                                + "[SKIP] Test Case '"
                                                + (
                                                        tc.getName()
                                                                != null
                                                                ? tc.getName()
                                                                : "Unknown ID"
                                                )
                                                + "' is SKIPPED. Reason: "
                                                + "Stage mismatch "
                                                + "(Test requires ["
                                                + rawStage
                                                + "], but current instance is ["
                                                + System.getProperty(
                                                        "INSTANCE"
                                                )
                                                + "])."
                                                + ConsoleColors.RESET
                                );

                                continue;
                            }
                        }
                    }

                    // -----------------------------------------------------
                    // FILTER 2: PRIORITY
                    // -----------------------------------------------------

                    String systemPriority =
                            System.getProperty(
                                            "PRIORITY",
                                            ""
                                    )
                                    .trim()
                                    .toUpperCase();

                    if (!systemPriority.isEmpty()) {

                        String tcPriority =
                                tc.getPriority() != null
                                        ? tc.getPriority()
                                        .trim()
                                        .toUpperCase()
                                        : "LOW";

                        boolean priorityMatch =
                                false;

                        switch (systemPriority) {

                            case "SMOKE":

                                if (tcPriority.equals(
                                        "SMOKE"
                                )) {

                                    priorityMatch =
                                            true;
                                }

                                break;

                            case "LOW":

                                if (tcPriority.equals(
                                        "LOW"
                                )) {

                                    priorityMatch =
                                            true;
                                }

                                break;

                            case "MEDIUM":

                                if (tcPriority.equals(
                                        "LOW"
                                )
                                        || tcPriority.equals(
                                        "MEDIUM"
                                )) {

                                    priorityMatch =
                                            true;
                                }

                                break;

                            case "HIGH":

                                if (tcPriority.equals(
                                        "LOW"
                                )
                                        || tcPriority.equals(
                                        "MEDIUM"
                                )
                                        || tcPriority.equals(
                                        "HIGH"
                                )
                                        || tcPriority.equals(
                                        "SMOKE"
                                )) {

                                    priorityMatch =
                                            true;
                                }

                                break;

                            default:

                                priorityMatch =
                                        true;

                                break;
                        }

                        if (!priorityMatch) {

                            System.out.println(
                                    ConsoleColors.YELLOW
                                            + "[SKIP] Test Case '"
                                            + (
                                                    tc.getName()
                                                            != null
                                                            ? tc.getName()
                                                            : "Unknown ID"
                                            )
                                            + "' is SKIPPED. Reason: "
                                            + "Cumulative Priority restriction "
                                            + "(Test is ["
                                            + tcPriority
                                            + "], but target priority "
                                            + "execution filter is set to ["
                                            + systemPriority
                                            + "])."
                                            + ConsoleColors.RESET
                            );

                            continue;
                        }
                    }

                    // -----------------------------------------------------
                    // EXECUTE TEST
                    // -----------------------------------------------------

                    totalTestsExecuted++;

                    printCleanHeader(
                            tc
                    );

                    long startTime =
                            System.currentTimeMillis();

                    boolean passed =
                            true;

                    String errorMessage =
                            null;

                    List<String> currentTestLogs =
                            new ArrayList<>();

                    TestLogger customLogger =
                            msg -> {

                                System.out.println(msg);
                                currentTestLogs.add(msg);
                            };

                    TestExecutor executionWithLogs =
                            new TestExecutor(
                                    customLogger
                            );

                    try {

                        executionWithLogs.executeTestCase(
                                tc,
                                formattedYamlPath
                        );

                        passedCount++;

                    } catch (DebugQuitException e) {

                    throw e;

                } catch (Exception e) {

                        passed =
                                false;

                        failedCount++;

                        failedTestNames.add(
                                (tc.getName() != null
                                        ? tc.getName()
                                        : "Unnamed Test Case")
                                        + " ["
                                        + Paths.get(formattedYamlPath)
                                        .getFileName()
                                        + "]"
                        );

                        errorMessage =
                                e.getMessage() != null
                                        ? e.getMessage()
                                        .trim()
                                        : "Unknown Error";

                    } finally {

                        long duration =
                                System.currentTimeMillis()
                                        - startTime;

                        printCleanFooter(
                                tc,
                                passed,
                                duration,
                                errorMessage
                        );

                        // HTML report data
                        reportEngine.addRecord(
                                tc,
                                formattedYamlPath,
                                passed,
                                duration,
                                errorMessage,
                                currentTestLogs
                        );

                        // JSON report data
                        jsonReportEngine.addRecord(
                                tc,
                                passed,
                                duration,
                                errorMessage,
                                currentTestLogs
                        );
                    }
                }
            }

        } finally {

            if (totalTestsExecuted > 0) {

                System.out.println(
                        "\nFinalizing suite and cleaning up resources..."
                );

                GuiEngine.cleanup(
                        sessionId,
                        consoleLogger
                );
            }
        }

        // -----------------------------------------------------------------
        // Nothing executed
        // -----------------------------------------------------------------

        if (totalTestsExecuted == 0
                && (
                targetTestCase == null
                        || targetTestCase
                        .trim()
                        .isEmpty()
        )) {

            System.out.println(
                    ConsoleColors.YELLOW
                            + "[INFO] No test cases found matching "
                            + "the provided combination ("
                            + System.getProperty(
                                    "INSTANCE"
                            )
                            + ") and PRIORITY ("
                            + System.getProperty(
                                    "PRIORITY"
                            )
                            + ")."
                            + ConsoleColors.RESET
            );

            System.exit(0);
        }

        if (totalTestsExecuted == 0
                && targetTestCase != null) {

            System.out.println(
                    ConsoleColors.RED
                            + "Error: Targeted Test Case '"
                            + targetTestCase
                            + "' was not found."
                            + ConsoleColors.RESET
            );

            System.exit(1);
        }

        // -----------------------------------------------------------------
        // FINAL SUMMARY
        // -----------------------------------------------------------------

        long totalDuration =
                System.currentTimeMillis()
                        - suiteStartTime;

        printFinalSummary(
                totalTestsExecuted,
                passedCount,
                failedCount,
                totalDuration,
                failedTestNames
        );

        // -----------------------------------------------------------------
        // REPORT ARTIFACT PIPELINE
        // -----------------------------------------------------------------

        try {

            if (totalTestsExecuted > 0) {

                String fileTimestamp =
                        java.time.LocalDateTime
                                .now()
                                .format(
                                        java.time.format
                                                .DateTimeFormatter
                                                .ofPattern(
                                                        "yyyyMMdd_HHmmss"
                                                )
                                );

                String currentInstance =
                        System.getProperty(
                                        "INSTANCE",
                                        "UNKNOWN"
                                )
                                .toUpperCase()
                                .trim();

                String baseFolder =
                        projectContext
                                .getReportsRoot()
                                .resolve(currentInstance)
                                .resolve(sanitizedApps)
                                .toString();

                String reportName;

                if (file != null
                        && !file.trim().isEmpty()) {

                    String cleanFileName =
                            file.replace(
                                            ".yaml",
                                            ""
                                    )
                                    .replace(
                                            ".yml",
                                            ""
                                    )
                                    .trim();

                    baseFolder +=
                            "/"
                                    + cleanFileName;

                    if (targetTestCase != null
                            && !targetTestCase
                            .trim()
                            .isEmpty()) {

                        String cleanTcName =
                                targetTestCase
                                        .replaceAll(
                                                "[^a-zA-Z0-9_-]",
                                                "_"
                                        );

                        reportName =
                                cleanTcName
                                        + "_"
                                        + fileTimestamp
                                        + ".html";

                    } else {

                        reportName =
                                "SuiteReport_"
                                        + fileTimestamp
                                        + ".html";
                    }

                } else {

                    reportName =
                            "FullRunReport_"
                                    + fileTimestamp
                                    + ".html";
                }

                // ---------------------------------------------------------
                // HTML paths
                // ---------------------------------------------------------

                String finalReportPath =
                        baseFolder
                                + "/"
                                + reportName;

                String latestReportPath =
                        baseFolder
                                + "/Latest_Report.html";

                // ---------------------------------------------------------
                // JSON paths
                // ---------------------------------------------------------

                String jsonReportName =
                        reportName.replace(
                                ".html",
                                ".json"
                        );

                String finalJsonReportPath =
                        baseFolder
                                + "/"
                                + jsonReportName;

                String latestJsonReportPath =
                        baseFolder
                                + "/Latest_Report.json";

                // ---------------------------------------------------------
                // HTML archive
                // ---------------------------------------------------------

                reportEngine.generateHtmlReport(
                        finalReportPath
                );

                System.out.println(
                        ">>> Archive HTML report generated at: "
                                + finalReportPath
                );

                // ---------------------------------------------------------
                // HTML latest
                // ---------------------------------------------------------

                reportEngine.generateHtmlReport(
                        latestReportPath
                );

                System.out.println(
                        ">>> Shortcut 'Latest' report updated at: "
                                + latestReportPath
                );

                // ---------------------------------------------------------
                // JSON archive
                // ---------------------------------------------------------

                jsonReportEngine.generateJsonReport(
                        finalJsonReportPath
                );

                System.out.println(
                        ">>> Archive JSON report generated at: "
                                + finalJsonReportPath
                );

                // ---------------------------------------------------------
                // JSON latest
                // ---------------------------------------------------------

                jsonReportEngine.generateJsonReport(
                        latestJsonReportPath
                );

                System.out.println(
                        ">>> Shortcut 'Latest' JSON report updated at: "
                                + latestJsonReportPath
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error building execution report artifacts: "
                            + e.getMessage()
            );

            if (Boolean.getBoolean(
                    "agate.debug"
            )) {

                e.printStackTrace();
            }
        }

        System.exit(
                failedCount == 0
                        ? 0
                        : 1
        );
    }


    // =========================================================================
    // AUXILIARY HELPER METHODS FOR FORMATTED TERMINAL OUTPUTS
    // =========================================================================

    private static void generateAndOpenAllure() {

        try {

            System.out.println(
                    "\n>>> Generating Allure HTML static report artifacts..."
            );

            ProcessBuilder generateBuilder =
                    new ProcessBuilder(
                            "cmd.exe",
                            "/c",
                            "allure generate allure-results --clean -o target/allure-report"
                    );

            generateBuilder.inheritIO();

            Process genProcess =
                    generateBuilder.start();

            genProcess.waitFor();

            System.out.println(
                    ConsoleColors.GREEN
                            + ">>> Allure HTML successfully compiled inside "
                            + "target/allure-report/"
                            + ConsoleColors.RESET
            );

            System.out.println(
                    ">>> Initializing local Allure Server engine "
                            + "on port 15555..."
            );

            ProcessBuilder serveBuilder =
                    new ProcessBuilder(
                            "cmd.exe",
                            "/c",
                            "allure open target/allure-report --port 15555"
                    );

            serveBuilder.start();

            Thread.sleep(
                    4000
            );

        } catch (Exception e) {

            System.err.println(
                    "Error dispatching Allure server process: "
                            + e.getMessage()
            );
        }
    }


    private static void saveAllureAsPdf() {

        try {

            System.out.println(
                    "\n>>> Spawning headless Playwright browser instance "
                            + "(capturing target http://localhost:15555)..."
            );

            try (
                    com.microsoft.playwright.Playwright playwright =
                            com.microsoft.playwright.Playwright.create()
            ) {

                com.microsoft.playwright.Browser browser =
                        playwright.chromium()
                                .launch(
                                        new com.microsoft.playwright
                                                .BrowserType
                                                .LaunchOptions()
                                                .setHeadless(
                                                        true
                                                )
                                );

                com.microsoft.playwright.Page page =
                        browser.newPage();

                page.navigate(
                        "http://localhost:15555/index.html"
                );

                page.waitForTimeout(
                        5000
                );

                page.pdf(
                        new com.microsoft.playwright.Page
                                .PdfOptions()
                                .setPath(
                                        java.nio.file.Paths.get(
                                                "target/reports/AllureReport.pdf"
                                        )
                                )
                                .setPrintBackground(
                                        true
                                )
                                .setLandscape(
                                        true
                                )
                );

                System.out.println(
                        ConsoleColors.GREEN
                                + ">>> [SUCCESS] Comprehensive Allure PDF "
                                + "bundle generated at: "
                                + "target/reports/AllureReport.pdf"
                                + ConsoleColors.RESET
                );

                browser.close();
            }

        } catch (Exception e) {

            System.err.println(
                    "Exception intercepted during Playwright PDF export execution: "
                            + e.getMessage()
            );
        }
    }


    private static void printCleanHeader(
            TestCase tc) {

        String name =
                tc.getName() != null
                        ? tc.getName()
                        : "Unnamed Test Case";

        System.out.println(
                "======================================================================"
        );

        System.out.println(
                "TEST CASE: "
                        + name
        );

        System.out.println(
                "DESC     : "
                        + (
                        tc.getDescription() != null
                                ? tc.getDescription()
                                : "N/A"
                )
        );

        System.out.println(
                "STAGE    : "
                        + (
                        tc.getStage() != null
                                ? tc.getStage()
                                : "N/A"
                )
                        + " | PRIO: "
                        + (
                        tc.getPriority() != null
                                ? tc.getPriority()
                                : "N/A"
                )
        );

        System.out.println(
                "----------------------------------------------------------------------"
        );

        System.out.println(
                "VARIABLES:"
        );

        if (tc.getVariables() != null
                && !tc.getVariables()
                .isEmpty()) {

            tc.getVariables()
                    .forEach(
                            (key, value) -> {

                                String displayValue =
                                        value != null
                                                ? String.valueOf(
                                                value
                                        )
                                                : "null";

                                System.out.printf(
                                        "  %-15s = %s%n",
                                        key,
                                        displayValue
                                );
                            }
                    );
        }

        System.out.println(
                "----------------------------------------------------------------------"
        );
    }


    private static void printCleanFooter(
            TestCase tc,
            boolean passed,
            long durationMs,
            String error) {

        double seconds =
                durationMs
                        / 1000.0;

        String name =
                tc.getName() != null
                        ? tc.getName()
                        : "Unnamed Test Case";

        String statusText =
                passed
                        ? ConsoleColors.GREEN
                        + "PASSED"
                        + ConsoleColors.RESET

                        : ConsoleColors.RED
                        + "FAILED"
                        + ConsoleColors.RESET;

        System.out.println(
                "\n--------------------------------------------------------------------------------"
        );

        System.out.printf(
                "TEST RESULT: %-45s [%s]%n",
                name,
                statusText
        );

        System.out.printf(
                "Time: %.2fs | Steps: %d%n",
                seconds,
                tc.getSteps() != null
                        ? tc.getSteps()
                        .size()
                        : 0
        );

        if (!passed
                && error != null) {

            System.out.println(
                    ConsoleColors.RED
                            + "REASON: "
                            + ConsoleColors.RESET
                            + error
            );
        }

        System.out.println(
                "--------------------------------------------------------------------------------\n"
        );
    }


    private static void printFinalSummary(
            int total,
            int passed,
            int failed,
            long durationMs,
            List<String> failedNames) {

        double seconds =
                durationMs
                        / 1000.0;

        double successRate =
                total > 0
                        ? (
                        passed
                                * 100.0
                                / total
                )
                        : 0;

        System.out.println(
                "=".repeat(
                        80
                )
        );

        System.out.println(
                "                        FINAL EXECUTION SUMMARY"
        );

        System.out.println(
                "=".repeat(
                        80
                )
        );

        System.out.printf(
                "  Total Test Cases : %d%n",
                total
        );

        System.out.printf(
                "  Passed           : %s%d%s%n",
                ConsoleColors.GREEN,
                passed,
                ConsoleColors.RESET
        );

        System.out.printf(
                "  Failed           : %s%d%s%n",
                failed > 0
                        ? ConsoleColors.RED
                        : ConsoleColors.RESET,
                failed,
                ConsoleColors.RESET
        );

        System.out.printf(
                "  Success Rate     : %.1f%%%n",
                successRate
        );

        System.out.printf(
                "  Total Time       : %.2fs%n",
                seconds
        );

        if (!failedNames.isEmpty()) {

            System.out.println(
                    "-".repeat(
                            80
                    )
            );

            System.out.println(
                    ConsoleColors.RED
                            + "  FAILED TEST CASES:"
                            + ConsoleColors.RESET
            );

            failedNames.forEach(
                    name ->
                            System.out.println(
                                    "    - "
                                            + name
                            )
            );
        }

        String finalStatus =
                failed == 0
                        ? ConsoleColors.GREEN
                        + "SUCCESS"
                        + ConsoleColors.RESET

                        : ConsoleColors.RED
                        + "FAILURE"
                        + ConsoleColors.RESET;

        System.out.println(
                "=".repeat(
                        80
                )
        );

        System.out.printf(
                "  OVERALL STATUS   : [%s]%n",
                finalStatus
        );

        System.out.println(
                "=".repeat(
                        80
                )
                        + "\n"
        );
    }


    private static void printAgateError(
            String title,
            String yamlFile,
            AgateStepException e) {

        System.err.println();

        System.err.println(
                "============================================================"
        );

        System.err.println(
                "[ERROR] "
                        + title
        );

        System.err.println(
                "============================================================"
        );

        System.err.println();

        if (yamlFile != null
                && !yamlFile.isBlank()) {

            System.err.println(
                    "File     : "
                            + Paths.get(
                            yamlFile
                    )
                    .toAbsolutePath()
            );
        }

        System.err.println(
                "Reason   : "
                        + e.getReason()
        );

        e.getDetails()
                .forEach(
                        (key, value) ->
                                System.err.printf(
                                        "%-9s: %s%n",
                                        key,
                                        value
                                )
                );

        Throwable cause =
                e.getCause();

        if (cause != null
                && cause.getMessage() != null
                && !cause.getMessage()
                .isBlank()) {

            System.err.println(
                    "Cause    : "
                            + cause.getMessage()
            );
        }

        System.err.println();

        System.err.println(
                "============================================================"
        );
    }
}