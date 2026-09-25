package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SuspiciousYamlEscapeValidator
        implements AgateValidator {

    /*
     * Matches a simple YAML property with a double-quoted scalar:
     *
     * value: "..."
     * command: "..."
     * path: "..."
     *
     * This validator deliberately works on RAW source text.
     *
     * After YAML parsing:
     *
     *   "\remove"
     *
     * has already become:
     *
     *   <CR>emove
     *
     * and it is too late to recognize the user's original mistake.
     */
    private static final Pattern DOUBLE_QUOTED_PROPERTY =
            Pattern.compile(
                    "^\\s*(?:-\\s*)?([A-Za-z0-9_-]+)\\s*:\\s*\"(.*)\"\\s*(?:#.*)?$");

    /*
     * YAML-valid escape sequences which are especially suspicious
     * inside Windows paths / commands.
     *
     * Example:
     *
     * \remove...
     *
     * starts with \r and YAML converts it to carriage return.
     */
    private static final Pattern SUSPICIOUS_ESCAPE =
            Pattern.compile(
                    "(?<!\\\\)\\\\([rntbf])");

    @Override
    public List<ValidationIssue> validate(
            ValidationContext context) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        if (context == null
                || context.file() == null
                || context.root() == null) {

            return issues;
        }

        Set<Path> visited =
                new HashSet<>();

        /*
         * Check main YAML itself.
         */
        validateFile(
                context.file(),
                issues);

        /*
         * Check all reusable modules recursively.
         */
        for (ValidationUtil.TestCaseRef tc :
                ValidationUtil.testCases(context.root())) {

            JsonNode steps =
                    tc.node().get("steps");

            validateReusableCalls(
                    context.file(),
                    steps,
                    visited,
                    issues);
        }

        return issues;
    }

    private void validateFile(
            Path file,
            List<ValidationIssue> issues) {

        if (file == null
                || !Files.isRegularFile(file)) {

            return;
        }

        final List<String> lines;

        try {

            lines =
                    Files.readAllLines(
                            file,
                            StandardCharsets.UTF_8);

        } catch (IOException e) {

            return;
        }

        for (int i = 0;
             i < lines.size();
             i++) {

            String line =
                    lines.get(i);

            Matcher propertyMatcher =
                    DOUBLE_QUOTED_PROPERTY.matcher(line);

            if (!propertyMatcher.matches()) {

                continue;
            }

            String property =
                    propertyMatcher.group(1);

            String value =
                    propertyMatcher.group(2);

            Matcher escapeMatcher =
                    SUSPICIOUS_ESCAPE.matcher(value);

            while (escapeMatcher.find()) {

                String escape =
                        "\\"
                                + escapeMatcher.group(1);

                /*
                 * Avoid making this rule too broad.
                 *
                 * We only report it when the scalar looks like
                 * a path or command where '\' is likely intended
                 * as a path separator.
                 */
                if (!looksLikePathOrCommand(
                        property,
                        value)) {

                    continue;
                }

                String message =
                        "Suspicious escape sequence '"
                                + printableEscape(escape)
                                + "' inside double-quoted YAML value. "
                                + "YAML interprets this as an escape character, "
                                + "not as a Windows path separator. "
                                + "Use single quotes for Windows paths or escape the backslash. "
                                + "Actual: \""
                                + value
                                + "\".";

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V012",
                                message,
                                file,
                                i + 1
                        )
                );
            }
        }
    }

    private boolean looksLikePathOrCommand(
            String property,
            String value) {

        if (value == null
                || value.isBlank()) {

            return false;
        }

        String lowerProperty =
                property != null
                        ? property.toLowerCase()
                        : "";

        String lowerValue =
                value.toLowerCase();

        /*
         * Typical AGATE fields where paths / shell commands occur.
         */
        if (lowerProperty.equals("value")
                || lowerProperty.equals("command")
                || lowerProperty.equals("path")
                || lowerProperty.equals("file")
                || lowerProperty.equals("directory")
                || lowerProperty.equals("source")
                || lowerProperty.equals("target")) {

            return true;
        }

        /*
         * Additional path indicators.
         */
        return value.contains(":\\")
                || value.contains("}\\")
                || lowerValue.contains(".bat")
                || lowerValue.contains(".cmd")
                || lowerValue.contains(".ps1")
                || lowerValue.contains(".exe");
    }

    private void validateReusableCalls(
            Path suiteFile,
            JsonNode steps,
            Set<Path> visited,
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
                    || !command.startsWith("reusable.")) {

                continue;
            }

            Path reusableFile =
                    resolveReusableFile(
                            suiteFile,
                            command);

            if (reusableFile == null
                    || !Files.isRegularFile(reusableFile)) {

                continue;
            }

            Path normalized =
                    reusableFile
                            .toAbsolutePath()
                            .normalize();

            if (!visited.add(normalized)) {

                continue;
            }

            validateFile(
                    normalized,
                    issues);

            /*
             * We need the parsed reusable YAML only for discovering
             * nested CALLs.
             *
             * Invalid YAML syntax is already handled by
             * ReusableYamlSyntaxValidator.
             */
            try {

                com.fasterxml.jackson.databind.ObjectMapper mapper =
                        new com.fasterxml.jackson.databind.ObjectMapper(
                                new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());

                JsonNode root =
                        mapper.readTree(
                                normalized.toFile());

                if (root != null) {

                    validateReusableCalls(
                            suiteFile,
                            root.get("steps"),
                            visited,
                            issues);
                }

            } catch (Exception ignored) {

                /*
                 * Syntax validator owns YAML parse errors.
                 */
            }
        }
    }

    private Path resolveReusableFile(
            Path testSuiteFile,
            String command) {

        if (testSuiteFile == null
                || command == null
                || !command.startsWith("reusable.")) {

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

        String relative =
                reusableName.replace(
                        '.',
                        '/');

        return parent
                .resolve("reusable")
                .resolve(relative + ".yaml")
                .normalize();
    }

    private String printableEscape(
            String escape) {

        if (escape == null) {

            return "";
        }

        /*
         * We deliberately print the source notation:
         *
         * \r
         * \n
         * \t
         *
         * instead of the resulting control character.
         */
        return escape;
    }
}