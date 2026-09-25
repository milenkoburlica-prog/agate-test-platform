package at.co.svc.agate.core.report.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AnalysisReportWriter {

    private final ObjectMapper mapper;

    public AnalysisReportWriter() {

        mapper =
                new ObjectMapper();

        mapper.enable(
                SerializationFeature.INDENT_OUTPUT
        );
    }

    public void writeJson(
            AnalysisResult result,
            String outputPath)
            throws Exception {

        Path path =
                Paths.get(
                        outputPath
                );

        Path parent =
                path.getParent();

        if (parent != null) {

            Files.createDirectories(
                    parent
            );
        }

        mapper.writeValue(
                path.toFile(),
                result
        );
    }

    public void printConsole(
            AnalysisResult result) {

        System.out.println();
        System.out.println(
                "================================================================================"
        );
        System.out.println(
                "                         AGATE FAILURE ANALYSIS"
        );
        System.out.println(
                "================================================================================"
        );

        System.out.println(
                "Application : "
                        + result.getApplication()
        );

        System.out.println(
                "Environment : "
                        + result.getEnvironment()
        );

        System.out.println(
                "Total Tests : "
                        + result.getTotalTests()
        );

        System.out.println(
                "Passed      : "
                        + result.getPassedTests()
        );

        System.out.println(
                "Failed      : "
                        + result.getFailedTests()
        );

        System.out.println(
                "--------------------------------------------------------------------------------"
        );

        int index =
                1;

        for (FailureCluster cluster
                : result.getFailureClusters()) {

            FailureSignature signature =
                    cluster.getSignature();

            System.out.println();
            System.out.println(
                    "COMMON ISSUE #"
                            + index++
            );

            System.out.println(
                    "Affected Tests : "
                            + cluster.getAffectedTests()
                            + " / "
                            + result.getFailedTests()
            );

            System.out.println(
                    "Occurrences    : "
                            + cluster.getOccurrences()
            );

            System.out.println(
                    "Step Type      : "
                            + display(
                            signature.getStepType()
                    )
            );

            System.out.println(
                    "Source         : "
                            + display(
                            signature.getSource()
                    )
            );

            System.out.println(
                    "Action         : "
                            + display(
                            signature.getAction()
                    )
            );

            System.out.println(
                    "Field          : "
                            + display(
                            signature.getField()
                    )
            );

            System.out.println(
                    "Reason         : "
                            + display(
                            signature.getReason()
                    )
            );

            if (cluster.getReusableStep() != null) {

                System.out.println(
                        "Reusable Step  : "
                                + cluster.getReusableStep()
                );
            }

            if (cluster.getHint() != null) {

                System.out.println(
                        "Hint           : "
                                + cluster.getHint()
                );
            }

            System.out.println();
            System.out.println(
                    "Assessment:"
            );

            System.out.println(
                    "  "
                            + cluster.getAssessment()
            );

            System.out.println();
            System.out.println(
                    "Suggested Focus:"
            );

            System.out.println(
                    "  "
                            + cluster.getSuggestedFocus()
            );

            System.out.println();
            System.out.println(
                    "Affected Test Cases:"
            );

            for (String testCase
                    : cluster.getTestCases()) {

                System.out.println(
                        "  - "
                                + testCase
                );
            }

            System.out.println(
                    "--------------------------------------------------------------------------------"
            );
        }
    }

    private static String display(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "<unknown>";
        }

        return value;
    }
}