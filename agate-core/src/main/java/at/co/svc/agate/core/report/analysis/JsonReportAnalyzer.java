package at.co.svc.agate.core.report.analysis;

import at.co.svc.agate.core.report.JsonReportEngine;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonReportAnalyzer {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public AnalysisResult analyze(
            String jsonReportPath)
            throws Exception {

        JsonReportEngine.Report report =
                objectMapper.readValue(
                        new File(jsonReportPath),
                        JsonReportEngine.Report.class
                );

        AnalysisResult result =
                new AnalysisResult();

        result.setApplication(
                report.application
        );

        result.setEnvironment(
                report.environment
        );

        List<JsonReportEngine.TestResult> tests =
                report.tests != null
                        ? report.tests
                        : List.of();

        result.setTotalTests(
                tests.size()
        );

        int passedTests =
                0;

        int failedTests =
                0;

        Map<FailureSignature, ClusterAccumulator> grouped =
                new LinkedHashMap<>();

        for (JsonReportEngine.TestResult test : tests) {

            if ("PASSED".equalsIgnoreCase(
                    test.status
            )) {

                passedTests++;

            } else if ("FAILED".equalsIgnoreCase(
                    test.status
            )) {

                failedTests++;
            }

            if (test.failures == null) {
                continue;
            }

            for (JsonReportEngine.Failure failure
                    : test.failures) {

                FailureSignature signature =
                        new FailureSignature(
                                failure.stepType,
                                failure.reason,
                                failure.field,
                                failure.action,
                                failure.source
                        );

                ClusterAccumulator accumulator =
                        grouped.computeIfAbsent(
                                signature,
                                key ->
                                        new ClusterAccumulator(
                                                signature
                                        )
                        );

                accumulator.occurrences++;

                accumulator.testCases.add(
                        test.id
                );

                if (accumulator.reusableStep == null
                        && failure.reusableStep != null
                        && !failure.reusableStep.isBlank()) {

                    accumulator.reusableStep =
                            failure.reusableStep;
                }

                if (accumulator.hint == null
                        && failure.hint != null
                        && !failure.hint.isBlank()) {

                    accumulator.hint =
                            failure.hint;
                }
            }
        }

        result.setPassedTests(
                passedTests
        );

        result.setFailedTests(
                failedTests
        );

        List<FailureCluster> clusters =
                new ArrayList<>();

        for (ClusterAccumulator accumulator
                : grouped.values()) {

            FailureCluster cluster =
                    new FailureCluster();

            cluster.setSignature(
                    accumulator.signature
            );

            cluster.setOccurrences(
                    accumulator.occurrences
            );

            cluster.setTestCases(
                    new ArrayList<>(
                            accumulator.testCases
                    )
            );

            cluster.setAffectedTests(
                    accumulator.testCases.size()
            );

            cluster.setReusableStep(
                    accumulator.reusableStep
            );

            cluster.setHint(
                    accumulator.hint
            );

            applyDeterministicAnalysis(
                    cluster,
                    failedTests
            );

            clusters.add(
                    cluster
            );
        }

        /*
         * Najčešći problemi prvi.
         */
        clusters.sort(
                Comparator
                        .comparingInt(
                                FailureCluster::getAffectedTests
                        )
                        .reversed()
                        .thenComparingInt(
                                FailureCluster::getOccurrences
                        )
                        .reversed()
        );

        result.setFailureClusters(
                clusters
        );

        return result;
    }

    private void applyDeterministicAnalysis(
            FailureCluster cluster,
            int totalFailedTests) {

        FailureSignature signature =
                cluster.getSignature();

        String stepType =
                signature.getStepType();

        String reason =
                signature.getReason();

        String field =
                signature.getField();

        String action =
                signature.getAction();

        String source =
                signature.getSource();

        int affected =
                cluster.getAffectedTests();

        // ---------------------------------------------------------
        // RULE 1:
        // SQL ROW_COUNT requiring column
        // ---------------------------------------------------------

        if ("column".equalsIgnoreCase(field)
                && containsIgnoreCase(
                        reason,
                        "Required property is missing"
                )
                && (
                source.isBlank()
                        || "ROW_COUNT".equalsIgnoreCase(
                        source
                )
        )) {

            cluster.setAssessment(
                    "The same SQL assertion failure appears in "
                            + affected
                            + " test case(s). "
                            + "The failure requires a 'column' property. "
                            + "If the underlying assertion uses ROW_COUNT, "
                            + "this is suspicious because ROW_COUNT operates "
                            + "on the result-set size rather than on a specific column."
            );

            cluster.setSuggestedFocus(
                    "Inspect SQL ASSERT validation for source=ROW_COUNT "
                            + "combined with action="
                            + display(action)
                            + ". Check whether the validator incorrectly "
                            + "requires 'column' for this assertion type."
            );

            return;
        }

        // ---------------------------------------------------------
        // RULE 2:
        // Same error affects all failed tests
        // ---------------------------------------------------------

        if (totalFailedTests > 1
                && affected == totalFailedTests) {

            cluster.setAssessment(
                    "This failure occurs in every failed test case. "
                            + "That strongly indicates one common technical "
                            + "cause rather than independent testcase defects."
            );

            cluster.setSuggestedFocus(
                    "Investigate the shared engine, reusable step, "
                            + "validation rule or common test setup before "
                            + "debugging individual test cases."
            );

            return;
        }

        // ---------------------------------------------------------
        // RULE 3:
        // Common repeated failure
        // ---------------------------------------------------------

        if (affected >= 2) {

            cluster.setAssessment(
                    "The same failure signature is shared by "
                            + affected
                            + " test cases."
            );

            cluster.setSuggestedFocus(
                    "Check shared reusable components and common "
                            + "migration/execution logic used by these tests."
            );

            return;
        }

        // ---------------------------------------------------------
        // Default
        // ---------------------------------------------------------

        cluster.setAssessment(
                "This failure currently appears to be isolated "
                        + "to one test case."
        );

        cluster.setSuggestedFocus(
                "Inspect the failing step and its input values "
                        + "for this testcase."
        );
    }

    private static boolean containsIgnoreCase(
            String text,
            String expected) {

        if (text == null
                || expected == null) {
            return false;
        }

        return text
                .toLowerCase()
                .contains(
                        expected.toLowerCase()
                );
    }

    private static String display(
            String value) {

        if (value == null
                || value.isBlank()) {
            return "<unknown>";
        }

        return value;
    }

    private static class ClusterAccumulator {

        private final FailureSignature signature;

        private int occurrences;

        private final Set<String> testCases =
                new LinkedHashSet<>();

        private String reusableStep;

        private String hint;

        private ClusterAccumulator(
                FailureSignature signature) {

            this.signature =
                    signature;
        }
    }
}