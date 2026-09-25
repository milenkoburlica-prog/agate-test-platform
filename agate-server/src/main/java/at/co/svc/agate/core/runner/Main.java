package at.co.svc.agate.core.runner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import at.co.svc.agate.core.dsl.register.YamlTestInstantiator;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.server.validation.ValidationIssue;
import at.co.svc.agate.server.validation.ValidationOptions;
import at.co.svc.agate.server.validation.ValidationResult;
import at.co.svc.agate.server.validation.ValidationService;

public class Main {

    static {
        System.setProperty(
                "java.util.logging.manager",
                "org.jboss.logmanager.LogManager");

        java.util.logging.Logger
                .getLogger("org.jboss.logmanager")
                .setLevel(java.util.logging.Level.SEVERE);
    }

    public static void main(String[] args) {

        System.setProperty("allure.enabled", "false");

        try {

            // ================================================================
            // MODE 1: TEST CASE INSTANTIATION
            // ================================================================

            if (args.length > 0
                    && "instantiate".equalsIgnoreCase(args[0])) {

                if (args.length < 4) {
                    System.err.println(
                            "Usage for instantiation: "
                                    + "java Main instantiate "
                                    + "<appName> <templateFile> <dataFile>");

                    System.exit(1);
                }

                startInstantiator(
                        args[1],
                        args[2],
                        args[3]);

                return;
            }

            // ================================================================
            // MODE 2: YAML VALIDATION
            // ================================================================

            if (args.length > 0
                    && "validate".equalsIgnoreCase(args[0])) {

                if (args.length < 2) {
                    System.err.println(
                            "Usage for validation: "
                                    + "java Main validate <yamlFile>");

                    System.exit(1);
                }

                startValidation(args[1]);

                return;
            }
         // ================================================================
         // MODE 3: DSL DESCRIPTION
         // ================================================================

         if (args.length > 0
                 && "describe".equalsIgnoreCase(args[0])) {

             if (args.length < 2) {
                 AgateDescribe.describe(null);
                 return;
             }

             AgateDescribe.describe(args[1]);
             return;
         }
            // ================================================================
            // MODE 4: NORMAL TEST EXECUTION
            // ================================================================

            if (args.length < 3) {

                System.err.println(
                        "Error: Insufficient parameters provided!");

                System.err.println();
                System.err.println("Usage:");
                System.err.println(
                        "  java Main "
                                + "<user> <instance> <app> "
                                + "[testSuite] [testCase] [priority]");

                System.err.println(
                        "  java Main instantiate "
                                + "<appName> <templateFile> <dataFile>");

                System.err.println(
                        "  java Main describe <type>");
                System.err.println(
                        "  java Main validate <yamlFile>");

                System.exit(1);
            }

            String user = args[0];
            String instance = args[1];
            String apps = args[2];

            String file =
                    args.length >= 4
                            ? args[3]
                            : null;

            String testCase =
                    args.length >= 5
                            ? args[4]
                            : null;

            String testPriority =
                    args.length >= 6
                            ? args[5]
                            : null;

            MainTestCaseExecute.start2(
                    user,
                    instance,
                    apps,
                    file,
                    testCase,
                    testPriority);

        } catch (Exception e) {

            /*
             * Lower layers should print structured errors.
             *
             * Do not print the stack trace here for expected
             * YAML / DSL / validation errors.
             */
            System.exit(1);
        }
    }

    // ========================================================================
    // YAML VALIDATION
    // ========================================================================

    private static void startValidation(
            String yamlFile) {

        Path file =
                Paths.get(yamlFile)
                        .toAbsolutePath()
                        .normalize();

        System.out.println("=".repeat(80));
        System.out.println(
                "                         AGATE YAML VALIDATION");
        System.out.println("=".repeat(80));

        System.out.println(
                "  File   : " + file);

        System.out.println("=".repeat(80));
        System.out.println();

        ValidationService validationService =
                ValidationService.defaultService();

        ValidationOptions options =
                ValidationOptions.defaults();

        ValidationResult result =
                validationService.validate(
                        file,
                        options);

        for (ValidationIssue issue :
                result.sortedIssues()) {

            printValidationIssue(issue);
        }

        System.out.println();

        System.out.println("-".repeat(80));

        System.out.println(
                "  Errors   : " + result.errors());

        System.out.println(
                "  Warnings : " + result.warnings());

        System.out.println(
                "  Info     : " + result.infos());

        System.out.println("-".repeat(80));

        if (result.valid()) {

            System.out.println(
                    ConsoleColors.GREEN
                            + "  STATUS : VALIDATION SUCCESSFUL"
                            + ConsoleColors.RESET);

            System.out.println("=".repeat(80));

            return;
        }

        System.out.println(
                ConsoleColors.RED
                        + "  STATUS : VALIDATION FAILED"
                        + ConsoleColors.RESET);

        System.out.println("=".repeat(80));

        System.exit(2);
    }
    
    
    private static void printValidationIssue(
            ValidationIssue issue) {

        String color =
                switch (issue.severity()) {

                    case ERROR ->
                            ConsoleColors.RED;

                    case WARNING ->
                            ConsoleColors.YELLOW;

                    case INFO ->
                            ConsoleColors.RESET;
                };

        StringBuilder location =
                new StringBuilder();

        if (issue.file() != null) {

            location.append(
                    issue.file()
                            .getFileName());

            if (issue.line() != null) {

                location.append(":")
                        .append(issue.line());

                if (issue.column() != null) {

                    location.append(":")
                            .append(issue.column());
                }
            }
        }

        System.out.println(
                color
                        + "["
                        + issue.severity()
                        + "] "
                        + issue.code()
                        + ConsoleColors.RESET);

        if (!location.isEmpty()) {

            System.out.println(
                    "        "
                            + location);
        }

        System.out.println(
                "        "
                        + issue.message());

        System.out.println();
    }

    // ========================================================================
    // TEST CASE INSTANTIATOR
    // ========================================================================

    private static void startInstantiator(
            String appName,
            String templateFile,
            String dataFile) {

        System.out.println("=".repeat(80));

        System.out.println(
                "            BATCH TEST CASE INSTANTIATION");

        System.out.println("=".repeat(80));

        try {

            YamlTestInstantiator instantiator =
                    new YamlTestInstantiator();

            instantiator.instantiate(
                    appName,
                    templateFile,
                    dataFile);

            System.out.println("=".repeat(80));

            System.out.println(
                    "  STATUS : "
                            + ConsoleColors.GREEN
                            + "ALL CASES GENERATED"
                            + ConsoleColors.RESET);

        } catch (Exception e) {

            System.err.println(
                    "\n"
                            + ConsoleColors.RED
                            + "FATAL ERROR:"
                            + ConsoleColors.RESET);

            e.printStackTrace();

            System.exit(1);
        }
    }

    // ========================================================================
    // OPTIONAL CLEANUP FOR GENERATED TEST INSTANCES
    // ========================================================================

    private static void cleanupOldInstances(
            String appPath) throws Exception {

        Path path =
                Paths.get(appPath);

        if (!Files.exists(path)) {
            return;
        }

        System.out.println(
                "[INFO] Cleaning up old instances in: "
                        + appPath);

        try (var stream = Files.walk(path, 1)) {

            stream
                    .filter(Files::isRegularFile)
                    .filter(p ->
                            p.getFileName()
                                    .toString()
                                    .startsWith("Instance_")
                                    && p.getFileName()
                                            .toString()
                                            .endsWith(".yaml"))
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        System.out.println(
                "[INFO] Cleanup finished.");
    }
}
