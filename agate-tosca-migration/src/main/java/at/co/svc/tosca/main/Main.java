package at.co.svc.tosca.main;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import at.co.svc.tosca.tsu.cleaner.ReusableDependencyAnalyzer;
import at.co.svc.tosca.tsu.cleaner.TestCaseYamlSanitizer;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            printUsage();
            return;
        }

        String jsonDir = "jsonOut";
        String migrationDir = "migration\\data";

        String command = args[0];

        switch (command.toLowerCase()) {

            case "migrate":

                if (args.length < 3) {

                    System.err.println(
                            "[Error] Missing arguments for 'migrate'. "
                                    + "Expected: migrate <appID> <baseFileName>"
                    );

                    System.exit(1);
                }

                MainMigrationApp.start(
                        args[1],
                        jsonDir,
                        args[2],
                        migrationDir
                );

                break;

            case "clean":

                executeClean(args);

                break;

            default:

                System.out.println(
                        "[Error] Unknown command: "
                                + command
                );

                printUsage();
        }
    }

    private static void executeClean(
            String[] args) throws Exception {

        /*
         * ---------------------------------------------------------
         * TESTCASE CLEAN
         *
         * clean <appID> <testsuite.yaml>
         *
         * Example:
         *
         * clean PST PST-004_Report.yaml
         *
         * Processes:
         *
         * data/PST/PST-004_Report.yaml
         * data/PST/reusable/*.yaml
         * ---------------------------------------------------------
         */
        if (args.length == 3) {

            String appName =
                    args[1];

            String testSuiteFileName =
                    args[2];

            cleanTestCase(
                    appName,
                    testSuiteFileName
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * TEMPLATE CLEAN
         *
         * Existing functionality:
         *
         * clean <appName> <template.yaml> <template.csv>
         * ---------------------------------------------------------
         */
        if (args.length == 4) {

            String appName =
                    args[1];

            String yamlFileName =
                    args[2];

            String csvFileName =
                    args[3];

            cleanTemplate(
                    appName,
                    yamlFileName,
                    csvFileName
            );

            return;
        }

        System.err.println(
                "[Error] Invalid arguments for 'clean'."
        );

        System.err.println();

        printUsage();

        System.exit(1);
    }

    private static void cleanTestCase(
            String appName,
            String testSuiteFileName)
            throws Exception {

        String userDir =
                System.getProperty("user.dir");

        Path appPath =
                Paths.get(
                        userDir,
                        "migration",
                        "data",
                        appName
                );

        Path testSuitePath =
                appPath.resolve(testSuiteFileName);

        Path reusablePath =
                appPath.resolve("reusable");

        System.out.println();
        System.out.println("============================================================");
        System.out.println("              AGATE TESTCASE YAML CLEAN");
        System.out.println("============================================================");
        System.out.println(" App        : " + appName);
        System.out.println(" Test Suite : " + testSuiteFileName);
        System.out.println(" Test Path  : " + testSuitePath);
        System.out.println(" Reusable   : " + reusablePath);
        System.out.println("============================================================");

        TestCaseYamlSanitizer.sanitize(
                testSuitePath,
                reusablePath
        );
    }
    
    
    private static void cleanTemplate(
            String appName,
            String yamlFileName,
            String csvFileName)
            throws Exception {

        System.out.println(
                "\n>>> PHASE 1: Running TestSanitizerApp "
                        + "(Sanitizing CSV keys and YAML conditions)..."
        );

        String userDir =
                System.getProperty("user.dir");

        Path basePath =
                Paths.get(
                        userDir,
                        "migration",
                        "data",
                        appName,
                        "template"
                );

        Path yamlPath =
                basePath.resolve(
                        yamlFileName
                );

        Path csvPath =
                basePath.resolve(
                        csvFileName
                );

        TestSanitizerApp.sanitizeTestFiles(
                csvPath,
                yamlPath
        );

        TestSanitizerApp.sanitizeTestFiles2(
                yamlPath
        );

        System.out.println(
                "\n>>> PHASE 2: Running ReusableDependencyAnalyzer "
                        + "(Cleaning up unused files/folders from disk)..."
        );

        String analyzerPath =
                userDir
                        + File.separator
                        + "migration"
                        + File.separator
                        + "data"
                        + File.separator
                        + appName;

        ReusableDependencyAnalyzer analyzer =
                new ReusableDependencyAnalyzer(
                        analyzerPath
                );

        analyzer.analyze();
    }

    private static void printUsage() {

        System.out.println();
        System.out.println("Usage:");
        System.out.println();

        System.out.println(
                "  java -jar <jarName> migrate "
                        + "<appID> <baseFileName>"
        );

        System.out.println();

        System.out.println(
                "  java -jar <jarName> clean "
                        + "<appID> <testsuite.yaml>"
        );

        System.out.println();

        System.out.println(
                "  java -jar <jarName> clean "
                        + "<appID> <template.yaml> <template.csv>"
        );

        System.out.println();
        System.out.println("Examples:");
        System.out.println();

        System.out.println(
                "  java -jar <jarName> "
                        + "clean PST PST-004_Report.yaml"
        );

        System.out.println(
                "  java -jar <jarName> "
                        + "clean MUHI "
                        + "absolutesBeschaeftigungsverbotEinmelden.yaml "
                        + "TS_absolutesBeschaeftigungsverbotEinmelden.csv"
        );

        System.out.println();
    }
}