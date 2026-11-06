package at.co.svc.agate.core.validation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Validates the command-line startup context before AGATE initializes the
 * environment, loads YAML files, or starts executing tests.
 */
public final class StartupValidator {

    private static final Path ENV_CONFIG = Paths.get("env", "env.conf");
    private static final Path USERS_CONFIG = Paths.get("env", "users.conf");
    private static final Path DATA_ROOT = Paths.get("data");

    private StartupValidator() {
        // Utility class
    }

    public static void validateOrThrow(
            String user,
            String instance,
            String app,
            String testSuiteFile) {

        List<ValidationIssue> issues = new ArrayList<>();

        String normalizedUser = trimToNull(user);
        String normalizedInstance = trimToNull(instance);
        String normalizedApp = trimToNull(app);

        validateRequiredArgument("User", normalizedUser, issues);
        validateRequiredArgument("Instance", normalizedInstance, issues);
        validateRequiredArgument("Application", normalizedApp, issues);

        Properties envProperties = loadProperties(
                ENV_CONFIG,
                "Environment configuration file was not found",
                issues);

        if (envProperties != null && normalizedInstance != null) {
            String instancePrefix = normalizedInstance + ".";

            if (!containsKeyWithPrefix(envProperties, instancePrefix)) {
                issues.add(new ValidationIssue(
                        "Test instance was not found",
                        "Configuration",
                        ENV_CONFIG.toString(),
                        "Instance",
                        normalizedInstance,
                        "Expected",
                        "Configuration entries starting with '" + instancePrefix + "'",
                        "Hint",
                        "Define the instance in env/env.conf or check the instance argument."));
            }
        }

        Properties userProperties = loadProperties(
                USERS_CONFIG,
                "User configuration file was not found",
                issues);

        if (userProperties != null
                && normalizedInstance != null
                && normalizedUser != null) {

            String userPrefix = normalizedInstance + "." + normalizedUser + ".";

            if (!containsKeyWithPrefix(userProperties, userPrefix)) {
                issues.add(new ValidationIssue(
                        "User configuration was not found",
                        "Configuration",
                        USERS_CONFIG.toString(),
                        "Instance",
                        normalizedInstance,
                        "User",
                        normalizedUser,
                        "Expected",
                        "Configuration entries starting with '" + userPrefix + "'",
                        "Hint",
                        "Add the user to env/users.config or check the user/instance arguments."));
            }
        }

        if (normalizedApp != null) {
            String sanitizedApp = normalizedApp.toLowerCase(Locale.ROOT);
            Path appDirectory = DATA_ROOT.resolve(sanitizedApp);

            if (!Files.exists(appDirectory) || !Files.isDirectory(appDirectory)) {
                issues.add(new ValidationIssue(
                        "Application test directory was not found",
                        "Application",
                        normalizedApp,
                        "Path",
                        appDirectory.toString(),
                        "Hint",
                        "Create " + appDirectory + " or check the app argument."));
            } else if (trimToNull(testSuiteFile) == null) {
                if (!containsYamlFiles(appDirectory)) {
                    issues.add(new ValidationIssue(
                            "No test suite files were found",
                            "Application",
                            normalizedApp,
                            "Path",
                            appDirectory.toString(),
                            "Expected",
                            "At least one .yaml or .yml file",
                            "Hint",
                            "Add a test suite to the application directory or specify another app."));
                }
            } else {
                String normalizedFile = normalizeYamlFileName(testSuiteFile);
                Path suitePath = appDirectory.resolve(normalizedFile);

                if (!Files.exists(suitePath) || !Files.isRegularFile(suitePath)) {
                    issues.add(new ValidationIssue(
                            "Test suite file was not found",
                            "Application",
                            normalizedApp,
                            "Test Suite",
                            normalizedFile,
                            "Path",
                            suitePath.toString(),
                            "Hint",
                            "Check the test-suite argument or place the YAML file in the application directory."));
                }
            }
        }

        if (!issues.isEmpty()) {
            printErrors(issues);
            throw new StartupValidationException("AGATE startup validation failed");
        }
    }

    private static void validateRequiredArgument(
            String name,
            String value,
            List<ValidationIssue> issues) {

        if (value == null) {
            issues.add(new ValidationIssue(
                    "Required startup argument is missing",
                    "Field",
                    name,
                    "Hint",
                    "Provide a non-empty " + name.toLowerCase(Locale.ROOT) + " argument."));
        }
    }

    private static Properties loadProperties(
            Path path,
            String missingReason,
            List<ValidationIssue> issues) {

        // 1) External/runtime configuration next to the application.
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Properties properties = new Properties();
                properties.load(reader);
                return properties;
            } catch (IOException e) {
                issues.add(new ValidationIssue(
                        "Configuration file could not be read",
                        "Configuration",
                        path.toString(),
                        "Actual",
                        safeMessage(e),
                        "Hint",
                        "Check file permissions and configuration file syntax."));
                return null;
            }
        }

        // 2) Maven/packaged runtime: src/main/resources/env/... is available
        //    as classpath resource env/....  This also avoids depending on the
        //    current working directory used to start the JVM.
        String resourceName = path.toString().replace('\\', '/');
        ClassLoader classLoader = StartupValidator.class.getClassLoader();

        try (InputStream input = classLoader.getResourceAsStream(resourceName)) {
            if (input != null) {
                try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                    Properties properties = new Properties();
                    properties.load(reader);
                    return properties;
                }
            }
        } catch (IOException e) {
            issues.add(new ValidationIssue(
                    "Configuration file could not be read",
                    "Configuration",
                    resourceName,
                    "Actual",
                    safeMessage(e),
                    "Hint",
                    "Check file permissions and configuration file syntax."));
            return null;
        }

        issues.add(new ValidationIssue(
                missingReason,
                "Configuration",
                path.toString(),
                "Hint",
                "Configuration was checked both as filesystem path and classpath resource: "
                        + resourceName));
        return null;
    }

    private static boolean containsKeyWithPrefix(
            Properties properties,
            String prefix) {

        String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);

        return properties.stringPropertyNames().stream()
                .map(key -> key.toLowerCase(Locale.ROOT))
                .anyMatch(key -> key.startsWith(normalizedPrefix));
    }

    private static boolean containsYamlFiles(Path appDirectory) {
        try (var stream = Files.list(appDirectory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString().toLowerCase(Locale.ROOT))
                    .anyMatch(name -> name.endsWith(".yaml") || name.endsWith(".yml"));
        } catch (IOException e) {
            return false;
        }
    }

    private static String normalizeYamlFileName(String file) {
        String normalized = file.trim();
        String lower = normalized.toLowerCase(Locale.ROOT);

        if (!lower.endsWith(".yaml") && !lower.endsWith(".yml")) {
            normalized += ".yaml";
        }

        return normalized;
    }

    private static void printErrors(List<ValidationIssue> issues) {
        System.err.println();
        System.err.println("================================================================================");
        System.err.println("[ERROR] AGATE cannot start");
        System.err.println("================================================================================");
        System.err.println("Startup configuration contains " + issues.size() + " error(s):");
        System.err.println();

        for (int i = 0; i < issues.size(); i++) {
            ValidationIssue issue = issues.get(i);

            System.err.println("  [" + (i + 1) + "] " + issue.reason);

            for (int j = 0; j < issue.details.length; j += 2) {
                String label = issue.details[j];
                String value = issue.details[j + 1];

                System.err.printf("      %-13s: %s%n", label, value);
            }

            if (i < issues.size() - 1) {
                System.err.println();
            }
        }

        System.err.println();
        System.err.println("Execution aborted.");
        System.err.println("================================================================================");
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String safeMessage(Exception e) {
        if (e.getMessage() == null || e.getMessage().isBlank()) {
            return e.getClass().getSimpleName();
        }
        return e.getMessage().trim();
    }

    private static final class ValidationIssue {
        private final String reason;
        private final String[] details;

        private ValidationIssue(String reason, String... details) {
            this.reason = reason;
            this.details = details;
        }
    }

    public static final class StartupValidationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public StartupValidationException(String message) {
            super(message);
        }
    }
}
