package at.co.svc.agate.core.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import at.co.svc.agate.core.dsl.model.TestCase;

public class JsonReportEngine {

    private final String application;
    private final String environment;

    private final List<TestResult> tests =
            new ArrayList<>();

    public JsonReportEngine(
            String application,
            String environment) {

        this.application = application;
        this.environment = environment;
    }

    public void addRecord(
            TestCase testCase,
            boolean passed,
            long runtimeMs,
            String errorMessage,
            List<String> logs) {

        TestResult result = new TestResult();

        result.id =
                testCase != null && testCase.getName() != null
                        ? testCase.getName()
                        : "Unnamed Test Case";

        result.status =
                passed
                        ? "PASSED"
                        : "FAILED";

        result.runtimeMs =
                runtimeMs;

        if (!passed) {

            List<Failure> failures =
                    extractFailures(
                            logs,
                            errorMessage
                    );

            result.failures.addAll(
                    failures
            );
        }

        tests.add(result);
    }

    public void generateJsonReport(
            String outputPath)
            throws IOException {

        Path path =
                Paths.get(outputPath);

        Path parent =
                path.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Report report =
                new Report();

        report.application =
                application;

        report.environment =
                environment;

        report.tests =
                tests;

        ObjectMapper mapper =
                new ObjectMapper();

        mapper.enable(
                SerializationFeature.INDENT_OUTPUT
        );

        mapper.writeValue(
                path.toFile(),
                report
        );
    }

    private List<Failure> extractFailures(
            List<String> logs,
            String errorMessage) {

        List<Failure> failures =
                new ArrayList<>();

        if (logs == null) {
            return failures;
        }

        Failure current =
                null;

        String currentStepType =
                null;

        String currentSource =
                null;

        String currentAction =
                null;

        String currentReusableStep =
                null;

        for (String rawLine : logs) {

            if (rawLine == null) {
                continue;
            }

            String line =
                    rawLine.trim();

            // -------------------------------------------------
            // Reusable step
            // -------------------------------------------------

            if (line.contains("REUSABLE STEP")) {

                Matcher matcher =
                        Pattern.compile(
                                "REUSABLE STEP\\s+([0-9.]+)"
                        )
                        .matcher(line);

                if (matcher.find()) {
                    currentReusableStep =
                            matcher.group(1);
                }
            }

            // -------------------------------------------------
            // DSL step type
            // -------------------------------------------------

            if (line.contains("- type:")) {

                String value =
                        extractAfterColon(line);

                if (value != null) {
                    currentStepType =
                            value;
                }
            }

            // -------------------------------------------------
            // DSL source
            // -------------------------------------------------

            if (line.contains("source:")) {

                String value =
                        extractAfterColon(line);

                if (value != null) {
                    currentSource =
                            stripQuotes(value);
                }
            }

            // -------------------------------------------------
            // DSL action
            // -------------------------------------------------

            if (line.contains("action:")) {

                String value =
                        extractAfterColon(line);

                if (value != null) {
                    currentAction =
                            stripQuotes(value);
                }
            }

            // -------------------------------------------------
            // Start failure
            // -------------------------------------------------

            if (line.contains("STEP FAILED")) {

                current =
                        new Failure();

                current.stepType =
                        currentStepType;

                current.source =
                        currentSource;

                current.action =
                        currentAction;

                current.reusableStep =
                        currentReusableStep;

                String reason =
                        extractValue(
                                line,
                                "ERROR:"
                        );

                current.reason =
                        reason;

                failures.add(current);
            }

            if (current == null) {
                continue;
            }

            // -------------------------------------------------
            // Failure details
            // -------------------------------------------------

            if (line.startsWith("Reason")) {

                current.reason =
                        extractAfterColon(line);

            } else if (line.startsWith("Field")) {

                current.field =
                        extractAfterColon(line);

            } else if (line.startsWith("Action")) {

                current.action =
                        extractAfterColon(line);

            } else if (line.startsWith("Hint")) {

                current.hint =
                        extractAfterColon(line);
            }
        }

        /*
         * Fallback:
         *
         * If execution failed but we could not extract a structured
         * STEP FAILED block, retain the top-level exception.
         */
        if (failures.isEmpty()
                && errorMessage != null
                && !errorMessage.isBlank()) {

            Failure failure =
                    new Failure();

            failure.reason =
                    errorMessage;

            failures.add(failure);
        }

        return failures;
    }

    private String extractAfterColon(
            String line) {

        int index =
                line.indexOf(':');

        if (index == -1
                || index + 1 >= line.length()) {
            return null;
        }

        return line.substring(
                        index + 1
                )
                .trim();
    }

    private String extractValue(
            String line,
            String marker) {

        int index =
                line.indexOf(marker);

        if (index == -1) {
            return null;
        }

        return line.substring(
                        index + marker.length()
                )
                .trim();
    }

    private String stripQuotes(
            String value) {

        if (value == null) {
            return null;
        }

        String result =
                value.trim();

        if (result.length() >= 2) {

            if ((result.startsWith("\"")
                    && result.endsWith("\""))
                    || (result.startsWith("'")
                    && result.endsWith("'"))) {

                return result.substring(
                        1,
                        result.length() - 1
                );
            }
        }

        return result;
    }

    // =========================================================
    // JSON DTOs
    // =========================================================

    public static class Report {

        public String application;
        public String environment;
        public List<TestResult> tests;
    }

    public static class TestResult {

        public String id;
        public String status;
        public long runtimeMs;

        public List<Failure> failures =
                new ArrayList<>();
    }

    public static class Failure {

        public String stepType;
        public String reason;
        public String field;
        public String action;
        public String source;
        public String reusableStep;
        public String hint;
    }
}