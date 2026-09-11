package at.co.svc.agate.core.runner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import at.co.svc.agate.core.dsl.register.YamlTestInstantiator;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;

public class Main {

    static {
        // Isključuje upozorenje o LogManager-u tako što ga postavlja
        // pre nego što ga JBoss potraži.
        System.setProperty(
                "java.util.logging.manager",
                "org.jboss.logmanager.LogManager");

        // Dodatno ućutkivanje JBoss logger-a za konzolu.
        java.util.logging.Logger
                .getLogger("org.jboss.logmanager")
                .setLevel(java.util.logging.Level.SEVERE);
    }

    /**
     * Option 1: with pom.xml
     *
     * java -jar target/agate-server-1.0.0-SNAPSHOT-jar-with-dependencies.jar
     *      %USER_NAME% %INSTANCE% %APP_NAME% %TEST_SUITE% %TEST_CASE%
     *
     * startTests.bat "Milenko" "DEMOS" "demo" "003_table_buffer_demo_test"
     *
     * Option 2:
     *
     * mvn dependency:copy-dependencies
     *
     * java -cp "target/classes;target/dependency/*"
     *      at.co.svc.test.framework.dsl.main.Main
     *      "Milenko" "DEMOS" "demo"
     *
     * java -cp "target/classes;target/dependency/*"
     *      at.co.svc.test.framework.dsl.main.Main
     *      "Milenko" "DEMOS" "demo"
     *      "003_table_buffer_demo_test"
     *
     * startTests.bat "Milenko" "DEMOS" "DEMO"
     * startTests.bat "Milenko" "DEMOS" "DEMO" "001_windows_cmd_basic_test"
     * startTests.bat "Milenko" "DEMOS" "DEMO"
     *      "001_windows_cmd_basic_test" "TC_CMD_01"
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        System.setProperty("allure.enabled", "false");

        try {

            // ================================================================
            // MODE 1: TEST CASE INSTANTIATION
            // ================================================================
            if (args.length > 0
                    && "instantiate".equalsIgnoreCase(args[0])) {

                // Expected:
                // java Main instantiate <appName> <templateFile> <dataFile>
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
            // MODE 2: NORMAL TEST EXECUTION
            // ================================================================
            if (args.length < 3) {
                System.err.println(
                        "Error: Insufficient parameters provided!");
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
             * The lower execution / loader layers are responsible for
             * printing the tester-facing structured error message.
             *
             * Important:
             * Do NOT print e.printStackTrace() here.
             *
             * Otherwise expected YAML / test-definition errors such as
             * duplicate IDs would again end with:
             *
             * Exception in thread "main" ...
             */
            System.exit(1);
        }
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

            // Optional cleanup:
            // cleanupOldInstances("data/" + appName);

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

            /*
             * Instantiator currently keeps its own technical error output.
             *
             * This can later be migrated to the same structured
             * AGATE error handling model.
             */
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