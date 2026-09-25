package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ReusableYamlSyntaxValidator
        implements AgateValidator {

    private static final ObjectMapper YAML_MAPPER =
            new ObjectMapper(
                    new YAMLFactory()
            );

    @Override
    public List<ValidationIssue> validate(
            ValidationContext context) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        if (context == null
                || context.root() == null
                || context.file() == null) {

            return issues;
        }

        /*
         * Prevent endless recursion:
         *
         * reusable.a
         *   -> reusable.b
         *      -> reusable.a
         */
        Set<Path> callStack =
                new HashSet<>();

        /*
         * Main test suites contain:
         *
         * testCases:
         *   - ...
         *     steps:
         *       - type: CALL
         *         command: reusable.xxx
         */
        for (ValidationUtil.TestCaseRef tc :
                ValidationUtil.testCases(
                        context.root())) {

            JsonNode steps =
                    tc.node().get("steps");

            validateCallsInSteps(
                    context.file(),
                    steps,
                    callStack,
                    issues);
        }

        return issues;
    }

    /**
     * Finds CALL steps inside a steps array and recursively validates
     * referenced reusable YAML files.
     */
    private void validateCallsInSteps(
            Path testSuiteFile,
            JsonNode steps,
            Set<Path> callStack,
            List<ValidationIssue> issues) {

        if (steps == null
                || !steps.isArray()) {

            return;
        }

        for (JsonNode step :
                steps) {

            if (step == null
                    || !step.isObject()) {

                continue;
            }

            String type =
                    ValidationUtil.type(step);

            if (!"CALL".equals(type)) {
                continue;
            }

            String command =
                    ValidationUtil.text(
                            step,
                            "command");

            if (command == null
                    || !command.startsWith(
                            "reusable.")) {

                continue;
            }

            validateReusable(
                    testSuiteFile,
                    command,
                    callStack,
                    issues);
        }
    }

    /**
     * Loads and parses one reusable YAML file.
     *
     * If its YAML syntax is invalid, an AGATE validation issue is produced.
     *
     * If parsing succeeds, nested CALLs are analyzed recursively.
     */
    private void validateReusable(
            Path testSuiteFile,
            String command,
            Set<Path> callStack,
            List<ValidationIssue> issues) {

        Path reusableFile =
                resolveReusableFile(
                        testSuiteFile,
                        command);

        if (reusableFile == null) {

            return;
        }

        Path normalized =
                reusableFile
                        .toAbsolutePath()
                        .normalize();

        /*
         * Missing reusable files are intentionally not handled here.
         *
         * This validator has one responsibility:
         * YAML syntax.
         *
         * Missing-file validation can remain in another validator.
         */
        if (!Files.isRegularFile(normalized)) {

            return;
        }

        /*
         * Prevent recursive CALL cycles.
         */
        if (!callStack.add(normalized)) {

            return;
        }

        try {

            JsonNode root;

            try {

                root =
                        YAML_MAPPER.readTree(
                                normalized.toFile());

            } catch (JsonProcessingException e) {

                addYamlSyntaxIssue(
                        normalized,
                        e,
                        issues);

                return;

            } catch (IOException e) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V011",
                                "Reusable YAML file could not be read: "
                                        + safeMessage(e),
                                normalized,
                                null
                        )
                );

                return;
            }

            if (root == null) {

                return;
            }

            /*
             * Reusable files contain:
             *
             * steps:
             *   - ...
             */
            JsonNode reusableSteps =
                    root.get("steps");

            if (reusableSteps == null
                    || !reusableSteps.isArray()) {

                /*
                 * Structure validation is not the responsibility
                 * of this syntax validator.
                 */
                return;
            }

            /*
             * Validate nested reusable CALLs as well.
             */
            validateCallsInSteps(
                    testSuiteFile,
                    reusableSteps,
                    callStack,
                    issues);

        } finally {

            /*
             * callStack represents the current recursive chain,
             * not every reusable file ever visited.
             */
            callStack.remove(normalized);
        }
    }

    /**
     * Converts a Jackson/SnakeYAML parsing error into a normal
     * AGATE validation issue.
     */
    private void addYamlSyntaxIssue(
            Path reusableFile,
            JsonProcessingException exception,
            List<ValidationIssue> issues) {

        Integer line =
                null;

        Integer column =
                null;

        JsonLocation location =
                exception.getLocation();

        if (location != null) {

            if (location.getLineNr() > 0) {
                line =
                        location.getLineNr();
            }

            if (location.getColumnNr() > 0) {
                column =
                        location.getColumnNr();
            }
        }

        String problem =
                exception.getOriginalMessage();

        if (problem == null
                || problem.isBlank()) {

            problem =
                    safeMessage(exception);
        }

        String message =
                "Invalid YAML syntax in reusable fragment: "
                        + cleanMessage(problem);

        /*
         * Add a useful hint for the very common Windows-path problem.
         *
         * Example:
         *
         * value: "C:\TCLogs"
         *
         * YAML interprets \T as an escape sequence.
         */
        String lower =
                problem.toLowerCase();

        if (lower.contains("unknown escape")
                || lower.contains("escape character")) {

            message +=
                    " Hint: For Windows paths, prefer single quotes, "
                            + "for example 'C:\\TCLogs', "
                            + "or escape backslashes inside double quotes, "
                            + "for example \"C:\\\\TCLogs\".";
        }

        issues.add(
                new ValidationIssue(
                        ValidationSeverity.ERROR,
                        "AGATE-V010",
                        message,
                        reusableFile,
                        line,
                        column
                )
        );
    }

    /**
     * Resolves:
     *
     * reusable.01_execute_ssh
     *
     * against:
     *
     * data/ssh/new_ssh.yaml
     *
     * to:
     *
     * data/ssh/reusable/01_execute_ssh.yaml
     */
    private Path resolveReusableFile(
            Path testSuiteFile,
            String command) {

        if (testSuiteFile == null
                || command == null
                || !command.startsWith(
                        "reusable.")) {

            return null;
        }

        Path parent =
                testSuiteFile
                        .toAbsolutePath()
                        .normalize()
                        .getParent();

        if (parent == null) {

            return null;
        }

        String reusableName =
                command.substring(
                        "reusable.".length());

        if (reusableName.isBlank()) {

            return null;
        }

        /*
         * Also supports:
         *
         * reusable.folder.module
         *
         * ->
         *
         * reusable/folder/module.yaml
         */
        String relative =
                reusableName.replace(
                        '.',
                        '/');

        return parent
                .resolve("reusable")
                .resolve(
                        relative + ".yaml")
                .normalize();
    }

    private String cleanMessage(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "Unknown YAML syntax error";
        }

        return value
                .replace("\r", " ")
                .replace("\n", " ")
                .replaceAll(
                        "\\s+",
                        " ")
                .trim();
    }

    private String safeMessage(
            Throwable throwable) {

        if (throwable == null) {

            return "Unknown error";
        }

        String message =
                throwable.getMessage();

        if (message == null
                || message.isBlank()) {

            return throwable
                    .getClass()
                    .getSimpleName();
        }

        return cleanMessage(
                message);
    }
}