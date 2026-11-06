package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

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

public final class PlaceholderSyntaxValidator
        implements AgateValidator {

    private static final ObjectMapper YAML_MAPPER =
            new ObjectMapper(
                    new YAMLFactory()
            );

    /*
     * Valid AGATE buffer placeholder:
     *
     * {B[name]}
     *
     * Name must not be empty.
     */
    private static final Pattern VALID_BUFFER_PLACEHOLDER =
            Pattern.compile(
                    "\\{B\\[[^\\[\\]{}]+]}");

    /*
     * Finds things which look like an AGATE buffer placeholder,
     * including malformed variants:
     *
     * {b[name]}
     * {B[name]
     * {B[]}
     * {B[name}}
     *
     * We deliberately search broadly first and then validate.
     */
    private static final Pattern POSSIBLE_BUFFER_PLACEHOLDER =
            Pattern.compile(
                    "\\{[Bb]\\[[^\\r\\n]*?(?:]|})");

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

        /*
         * Main YAML file.
         */
        validateFile(
                context.file(),
                issues);

        /*
         * Reusable files.
         */
        Set<Path> visited =
                new HashSet<>();

        for (ValidationUtil.TestCaseRef tc :
                ValidationUtil.testCases(
                        context.root())) {

            JsonNode steps =
                    tc.node()
                            .get("steps");

            validateReusableCalls(
                    context.file(),
                    steps,
                    visited,
                    issues);
        }

        return issues;
    }

    /*
     * =============================================================
     * Raw YAML validation
     * =============================================================
     */

    private void validateFile(
            Path file,
            List<ValidationIssue> issues) {

        if (file == null
                || !Files.isRegularFile(file)) {

            return;
        }

        List<String> lines;

        try {

            lines =
                    Files.readAllLines(
                            file,
                            StandardCharsets.UTF_8);

        } catch (IOException e) {

            return;
        }

        for (int lineIndex = 0;
             lineIndex < lines.size();
             lineIndex++) {

            String line =
                    lines.get(lineIndex);

            validateLine(
                    file,
                    line,
                    lineIndex + 1,
                    issues);
        }
    }

    private void validateLine(
            Path file,
            String line,
            int lineNumber,
            List<ValidationIssue> issues) {

        if (line == null
                || line.isBlank()) {

            return;
        }

        /*
         * =========================================================
         * Case 1:
         *
         * Lowercase placeholder:
         *
         * {b[l_logdir]}
         *
         * =========================================================
         */
        Pattern lowercasePattern =
                Pattern.compile(
                        "\\{b\\[([^\\]]+)]}");

        Matcher lowercaseMatcher =
                lowercasePattern.matcher(line);

        while (lowercaseMatcher.find()) {

            String actual =
                    lowercaseMatcher.group();

            String variableName =
                    lowercaseMatcher.group(1);

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V013",
                            "Malformed buffer placeholder '"
                                    + actual
                                    + "'. "
                                    + "Buffer placeholders are case-sensitive. "
                                    + "Use '{B["
                                    + variableName
                                    + "]}' instead.",
                            file,
                            lineNumber
                    )
            );
        }

        /*
         * If lowercase placeholders were found, do not report
         * the same token again as a generic malformed placeholder.
         */
        String withoutLowercase =
                lowercaseMatcher.reset()
                        .replaceAll("");

        /*
         * =========================================================
         * Case 2:
         *
         * Empty placeholder:
         *
         * {B[]}
         *
         * =========================================================
         */
        Pattern emptyPattern =
                Pattern.compile(
                        "\\{B\\[\\]}");

        Matcher emptyMatcher =
                emptyPattern.matcher(
                        withoutLowercase);

        while (emptyMatcher.find()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V014",
                            "Empty buffer placeholder '{B[]}'. "
                                    + "A buffer variable name is required.",
                            file,
                            lineNumber
                    )
            );
        }

        String remaining =
                emptyMatcher.reset()
                        .replaceAll("");

        /*
         * =========================================================
         * Case 3:
         *
         * Something still looks like {B[... but is not a valid
         * complete placeholder.
         *
         * Examples:
         *
         * {B[test]
         * {B[test}}
         * {B[test
         *
         * =========================================================
         */
        int position =
                remaining.indexOf(
                        "{B[");

        while (position >= 0) {

            String tail =
                    remaining.substring(
                            position);

            Matcher validMatcher =
                    VALID_BUFFER_PLACEHOLDER.matcher(
                            tail);

            /*
             * Valid placeholder starting exactly here.
             */
            if (validMatcher.lookingAt()) {

                position =
                        remaining.indexOf(
                                "{B[",
                                position
                                        + validMatcher.end());

                continue;
            }

            String actual =
                    extractMalformedPlaceholder(
                            tail);

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V015",
                            "Malformed buffer placeholder '"
                                    + actual
                                    + "'. "
                                    + "Expected syntax is '{B[variableName]}'.",
                            file,
                            lineNumber
                    )
            );

            position =
                    remaining.indexOf(
                            "{B[",
                            position + 3);
        }
    }

    private String extractMalformedPlaceholder(
            String text) {

        if (text == null
                || text.isBlank()) {

            return "{B[...]}";
        }

        /*
         * Try to show only the relevant token instead of the
         * complete YAML line.
         */
        int space =
                text.indexOf(' ');

        int quote =
                text.indexOf('"');

        int singleQuote =
                text.indexOf('\'');

        int comma =
                text.indexOf(',');

        int end =
                text.length();

        if (space > 0) {
            end = Math.min(
                    end,
                    space);
        }

        if (quote > 0) {
            end = Math.min(
                    end,
                    quote);
        }

        if (singleQuote > 0) {
            end = Math.min(
                    end,
                    singleQuote);
        }

        if (comma > 0) {
            end = Math.min(
                    end,
                    comma);
        }

        /*
         * Avoid huge error messages.
         */
        end =
                Math.min(
                        end,
                        80);

        return text.substring(
                0,
                end);
    }

    /*
     * =============================================================
     * Reusable traversal
     * =============================================================
     */

    private void validateReusableCalls(
            Path testSuiteFile,
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
                    || !command.startsWith(
                            "reusable.")) {

                continue;
            }

            Path reusableFile =
                    resolveReusableFile(
                            testSuiteFile,
                            command);

            if (reusableFile == null
                    || !Files.isRegularFile(
                            reusableFile)) {

                continue;
            }

            Path normalized =
                    reusableFile
                            .toAbsolutePath()
                            .normalize();

            if (!visited.add(
                    normalized)) {

                continue;
            }

            /*
             * Validate RAW text first.
             */
            validateFile(
                    normalized,
                    issues);

            /*
             * Parse only to discover nested CALLs.
             *
             * Syntax errors themselves belong to
             * ReusableYamlSyntaxValidator.
             */
            try {

                JsonNode root =
                        YAML_MAPPER.readTree(
                                normalized.toFile());

                if (root != null) {

                    validateReusableCalls(
                            testSuiteFile,
                            root.get("steps"),
                            visited,
                            issues);
                }

            } catch (Exception ignored) {

                /*
                 * Invalid YAML is reported by
                 * ReusableYamlSyntaxValidator.
                 */
            }
        }
    }

    /*
     * =============================================================
     * Reusable path resolution
     * =============================================================
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
}