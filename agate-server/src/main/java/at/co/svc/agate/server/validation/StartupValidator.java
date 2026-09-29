package at.co.svc.agate.server.validation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import at.co.svc.agate.core.project.ProjectContext;

/**
 * Validates the command-line startup context before AGATE initializes the
 * environment, loads YAML files, or starts executing tests.
 */
public final class StartupValidator {

    private StartupValidator() {
        // Utility class
    }

    public static void validateOrThrow(
            ProjectContext projectContext,
            String user,
            String instance,
            String app,
            String testSuiteFile) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        if (projectContext == null) {
            issues.add(new ValidationIssue(
                    "AGATE project context is missing",
                    "Hint",
                    "Initialize ProjectContext before startup validation."));

            printErrors(issues);
            throw new StartupValidationException(
                    "AGATE startup validation failed");
        }

        String normalizedUser =
                trimToNull(user);

        String normalizedInstance =
                trimToNull(instance);

        String normalizedApp =
                trimToNull(app);

        validateRequiredArgument(
                "User",
                normalizedUser,
                issues);

        validateRequiredArgument(
                "Instance",
                normalizedInstance,
                issues);

        validateRequiredArgument(
                "Application",
                normalizedApp,
                issues);

        Path envConfig =
                projectContext.getEnvironmentConfig();

        Path usersConfig =
                projectContext.getUsersConfig();

        Path testsRoot =
                projectContext.getTestsRoot();

        Properties envProperties =
                loadProperties(
                        envConfig,
                        "Environment configuration file was not found",
                        issues);

        if (envProperties != null
                && normalizedInstance != null) {

            String instancePrefix =
                    normalizedInstance + ".";

            if (!containsKeyWithPrefix(
                    envProperties,
                    instancePrefix)) {

                issues.add(new ValidationIssue(
                        "Test instance was not found",
                        "Configuration",
                        envConfig.toString(),
                        "Instance",
                        normalizedInstance,
                        "Expected",
                        "Configuration entries starting with '"
                                + instancePrefix
                                + "'",
                        "Hint",
                        "Define the instance in "
                                + envConfig
                                + " or check the instance argument."));
            }
        }

        Properties userProperties =
                loadProperties(
                        usersConfig,
                        "User configuration file was not found",
                        issues);

        if (userProperties != null
                && normalizedInstance != null
                && normalizedUser != null) {

            String userPrefix =
                    normalizedInstance
                            + "."
                            + normalizedUser
                            + ".";

            if (!containsKeyWithPrefix(
                    userProperties,
                    userPrefix)) {

                issues.add(new ValidationIssue(
                        "User configuration was not found",
                        "Configuration",
                        usersConfig.toString(),
                        "Instance",
                        normalizedInstance,
                        "User",
                        normalizedUser,
                        "Expected",
                        "Configuration entries starting with '"
                                + userPrefix
                                + "'",
                        "Hint",
                        "Add the user to "
                                + usersConfig
                                + " or check the user/instance arguments."));
            }
        }

        if (normalizedApp != null) {

            String sanitizedApp =
                    normalizedApp
                            .toLowerCase(Locale.ROOT);

            Path appDirectory =
                    testsRoot
                            .resolve(sanitizedApp)
                            .normalize();

            if (!Files.exists(appDirectory)
                    || !Files.isDirectory(appDirectory)) {

                issues.add(new ValidationIssue(
                        "Application test directory was not found",
                        "Application",
                        normalizedApp,
                        "Path",
                        appDirectory.toString(),
                        "Tests Root",
                        testsRoot.toString(),
                        "Hint",
                        "Create "
                                + appDirectory
                                + " or check the app argument."));

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
                            "Add a test suite to the application directory "
                                    + "or specify another app."));
                }

            } else {

                String normalizedFile =
                        normalizeYamlFileName(
                                testSuiteFile);

                Path suitePath =
                        appDirectory
                                .resolve(normalizedFile)
                                .normalize();

                if (!suitePath.startsWith(appDirectory)) {

                    issues.add(new ValidationIssue(
                            "Test suite path is outside the application directory",
                            "Application",
                            normalizedApp,
                            "Test Suite",
                            normalizedFile,
                            "Path",
                            suitePath.toString(),
                            "Hint",
                            "Use a test-suite path located inside "
                                    + appDirectory
                                    + "."));

                } else if (!Files.exists(suitePath)
                        || !Files.isRegularFile(suitePath)) {

                    issues.add(new ValidationIssue(
                            "Test suite file was not found",
                            "Application",
                            normalizedApp,
                            "Test Suite",
                            normalizedFile,
                            "Path",
                            suitePath.toString(),
                            "Hint",
                            "Check the test-suite argument or place the YAML file "
                                    + "in the application directory."));
                }
            }
        }

        if (!issues.isEmpty()) {
            printErrors(issues);
            throw new StartupValidationException(
                    "AGATE startup validation failed");
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
                    "Provide a non-empty "
                            + name.toLowerCase(Locale.ROOT)
                            + " argument."));
        }
    }

    private static Properties loadProperties(
            Path path,
            String missingReason,
            List<ValidationIssue> issues) {

        if (!Files.exists(path)
                || !Files.isRegularFile(path)) {

            issues.add(new ValidationIssue(
                    missingReason,
                    "Configuration",
                    path.toString(),
                    "Hint",
                    "Check the corresponding path in project-agate.yaml."));

            return null;
        }

        try (var reader =
                     Files.newBufferedReader(
                             path,
                             StandardCharsets.UTF_8)) {

            Properties properties =
                    new Properties();

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

    private static boolean containsKeyWithPrefix(
            Properties properties,
            String prefix) {

        String normalizedPrefix =
                prefix.toLowerCase(Locale.ROOT);

        return properties
                .stringPropertyNames()
                .stream()
                .map(key ->
                        key.toLowerCase(Locale.ROOT))
                .anyMatch(key ->
                        key.startsWith(normalizedPrefix));
    }

    private static boolean containsYamlFiles(
            Path appDirectory) {

        try (var stream =
                     Files.list(appDirectory)) {

            return stream
                    .filter(Files::isRegularFile)
                    .map(path ->
                            path.getFileName()
                                    .toString()
                                    .toLowerCase(Locale.ROOT))
                    .anyMatch(name ->
                            name.endsWith(".yaml")
                                    || name.endsWith(".yml"));

        } catch (IOException e) {
            return false;
        }
    }

    private static String normalizeYamlFileName(
            String file) {

        String normalized =
                file.trim();

        String lower =
                normalized.toLowerCase(Locale.ROOT);

        if (!lower.endsWith(".yaml")
                && !lower.endsWith(".yml")) {

            normalized += ".yaml";
        }

        return normalized;
    }

    private static void printErrors(
            List<ValidationIssue> issues) {

        System.err.println();
        System.err.println(
                "================================================================================");
        System.err.println(
                "[ERROR] AGATE cannot start");
        System.err.println(
                "================================================================================");
        System.err.println(
                "Startup configuration contains "
                        + issues.size()
                        + " error(s):");
        System.err.println();

        for (int i = 0; i < issues.size(); i++) {

            ValidationIssue issue =
                    issues.get(i);

            System.err.println(
                    "  ["
                            + (i + 1)
                            + "] "
                            + issue.reason);

            for (int j = 0;
                 j < issue.details.length;
                 j += 2) {

                String label =
                        issue.details[j];

                String value =
                        issue.details[j + 1];

                System.err.printf(
                        "      %-13s: %s%n",
                        label,
                        value);
            }

            if (i < issues.size() - 1) {
                System.err.println();
            }
        }

        System.err.println();
        System.err.println(
                "Execution aborted.");
        System.err.println(
                "================================================================================");
    }

    private static String trimToNull(
            String value) {

        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }

    private static String safeMessage(
            Exception e) {

        if (e.getMessage() == null
                || e.getMessage().isBlank()) {

            return e.getClass()
                    .getSimpleName();
        }

        return e.getMessage()
                .trim();
    }

    private static final class ValidationIssue {

        private final String reason;
        private final String[] details;

        private ValidationIssue(
                String reason,
                String... details) {

            this.reason =
                    reason;

            this.details =
                    details;
        }
    }

    public static final class StartupValidationException
            extends RuntimeException {

        private static final long serialVersionUID =
                1L;

        public StartupValidationException(
                String message) {

            super(message);
        }
    }
}
