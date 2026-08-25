package at.co.svc.agate.openapi.impact.cli;

import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeDetector;
import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeSet;

import at.co.svc.agate.openapi.impact.analysis.AgateImpactAnalyzer;

import at.co.svc.agate.openapi.impact.analysis.model.AgateArtifactType;
import at.co.svc.agate.openapi.impact.analysis.model.AgateImpactReport;
import at.co.svc.agate.openapi.impact.analysis.model.AgateRecommendedAction;
import at.co.svc.agate.openapi.impact.analysis.model.AgateTestCaseImpact;

import at.co.svc.agate.openapi.impact.analysis.summary.AgateImpactSummary;
import at.co.svc.agate.openapi.impact.analysis.summary.AgateImpactSummaryBuilder;

import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import java.nio.file.Path;

import java.util.Map;


public class AgateOpenApiImpactCli {


    private static final String OPTION_IMPACT =
            "--impact";


    private static final int EXIT_OK =
            0;


    private static final int EXIT_USAGE_ERROR =
            1;


    private static final int EXIT_EXECUTION_ERROR =
            2;


    private static final int EXIT_OPEN_IMPACTS =
            10;




    public static void main(
            String[] args) {

        if (args == null ||
                args.length != 4 ||
                !OPTION_IMPACT.equalsIgnoreCase(
                        args[0]
                )) {

            printUsage();


            System.exit(
                    EXIT_USAGE_ERROR
            );


            return;
        }


        String oldOpenApi =
                args[1];


        String newOpenApi =
                args[2];


        Path appDirectory =
                Path.of(
                        args[3]
                );


        try {

            AgateOpenApiParser parser =
                    new AgateOpenApiParser();


            AgateOpenApiModel oldModel =
                    parser.parse(
                            oldOpenApi
                    );


            AgateOpenApiModel newModel =
                    parser.parse(
                            newOpenApi
                    );


            AgateOpenApiChangeSet changes =
                    new AgateOpenApiChangeDetector()
                            .detect(
                                    oldModel,
                                    newModel
                            );


            AgateImpactReport report =
                    new AgateImpactAnalyzer()
                            .analyze(
                                    changes,
                                    newModel,
                                    appDirectory
                            );


            printReport(
                    oldOpenApi,
                    newOpenApi,
                    appDirectory,
                    report
            );


            AgateImpactSummary summary =
                    new AgateImpactSummaryBuilder()
                            .build(
                                    report
                            );


            printSummary(
                    summary
            );


            if (!report.isClean()) {

                System.exit(
                        EXIT_OPEN_IMPACTS
                );


                return;
            }


            System.exit(
                    EXIT_OK
            );


        } catch (Exception e) {

            System.err.println(
                    "ERROR: "
                            + e.getMessage()
            );


            e.printStackTrace();


            System.exit(
                    EXIT_EXECUTION_ERROR
            );
        }
    }




    private static void printReport(
            String oldOpenApi,
            String newOpenApi,
            Path appDirectory,
            AgateImpactReport report) {

        separator();


        System.out.println(
                "AGATE OPENAPI IMPACT ANALYSIS"
        );


        separator();


        System.out.println();


        System.out.println(
                "old contract : "
                        + oldOpenApi
        );


        System.out.println(
                "new contract : "
                        + newOpenApi
        );


        System.out.println(
                "application  : "
                        + appDirectory
        );


        System.out.println();


        System.out.println(
                "API changes  : "
                        + report
                                .getChangeSet()
                                .size()
        );


        System.out.println(
                "OPEN IMPACTS : "
                        + report
                                .getOpenImpactCount()
        );


        System.out.println();


        if (report.isClean()) {

            System.out.println(
                    "CURRENT AGATE ARTIFACTS ARE ALIGNED "
                            + "WITH THE NEW API CONTRACT."
            );


            System.out.println();


            separator();


            return;
        }


        String currentOperation =
                null;


        int number =
                1;


        for (AgateTestCaseImpact impact :
                report.getImpacts()) {

            if (!same(
                    currentOperation,
                    impact.getOperationIdentity()
            )) {

                currentOperation =
                        impact.getOperationIdentity();


                System.out.println(
                        "------------------------------------------------------------"
                );


                System.out.println(
                        currentOperation
                );


                System.out.println(
                        "------------------------------------------------------------"
                );


                System.out.println();
            }


            System.out.println(
                    String.format(
                            "IMPACT %03d",
                            number
                    )
            );


            System.out.println(
                    "  change    : "
                            + buildChangeDescription(
                                    impact
                            )
            );


            System.out.println(
                    "  artifact  : "
                            + valueOrNone(
                                    impact.getArtifactType()
                            )
            );


            System.out.println(
                    "  file      : "
                            + valueOrNone(
                                    impact.getArtifact()
                            )
            );


            System.out.println(
                    "  location  : "
                            + valueOrNone(
                                    impact.getArtifactLocation()
                            )
            );


            if (impact.getTestcaseName() != null) {

                System.out.println(
                        "  testcase  : "
                                + impact.getTestcaseName()
                );
            }


            if (impact.getCurrentValue() != null) {

                System.out.println(
                        "  current   : "
                                + impact.getCurrentValue()
                );
            }


            if (impact.getExpectedValue() != null) {

                System.out.println(
                        "  expected  : "
                                + impact.getExpectedValue()
                );
            }


            System.out.println(
                    "  action    : "
                            + valueOrNone(
                                    impact.getAction()
                            )
            );


            System.out.println(
                    "  reason    : "
                            + valueOrNone(
                                    impact.getReason()
                            )
            );


            System.out.println();


            number++;
        }


        separator();


        System.out.println(
                "OPEN IMPACTS : "
                        + report
                                .getOpenImpactCount()
        );


        separator();
    }




    private static void printSummary(
            AgateImpactSummary summary) {

        System.out.println();


        separator();


        System.out.println(
                "IMPACT SUMMARY"
        );


        separator();


        System.out.println();


        System.out.println(
                "OPEN IMPACTS : "
                        + summary.getOpenImpacts()
        );


        System.out.println();


        /*
         * =====================================================
         * BY ACTION
         * =====================================================
         */


        System.out.println(
                "BY ACTION"
        );


        System.out.println(
                "------------------------------------------------------------"
        );


        boolean actionPrinted =
                false;


        for (Map.Entry<
                AgateRecommendedAction,
                Integer
                > entry :
                summary
                        .getByAction()
                        .entrySet()) {

            if (entry.getValue() == null ||
                    entry.getValue() == 0) {

                continue;
            }


            printSummaryLine(
                    entry
                            .getKey()
                            .name(),
                    entry.getValue()
            );


            actionPrinted =
                    true;
        }


        if (!actionPrinted) {

            System.out.println(
                    "<none>"
            );
        }


        System.out.println();


        /*
         * =====================================================
         * BY OPERATION
         * =====================================================
         */


        System.out.println(
                "BY OPERATION"
        );


        System.out.println(
                "------------------------------------------------------------"
        );


        if (summary
                .getByOperation()
                .isEmpty()) {

            System.out.println(
                    "<none>"
            );

        } else {

            for (Map.Entry<String, Integer> entry :
                    summary
                            .getByOperation()
                            .entrySet()) {

                printSummaryLine(
                        entry.getKey(),
                        entry.getValue()
                );
            }
        }


        System.out.println();


        /*
         * =====================================================
         * BY ARTIFACT
         * =====================================================
         */


        System.out.println(
                "BY ARTIFACT"
        );


        System.out.println(
                "------------------------------------------------------------"
        );


        for (AgateArtifactType artifactType :
                AgateArtifactType.values()) {

            Integer count =
                    summary
                            .getByArtifact()
                            .get(
                                    artifactType
                            );


            printSummaryLine(
                    artifactType.name(),
                    count != null
                            ? count
                            : 0
            );
        }


        System.out.println();


        separator();


        if (summary.getOpenImpacts() == 0) {

            System.out.println(
                    "STATUS : ALIGNED"
            );

        } else {

            System.out.println(
                    "STATUS : CHANGES REQUIRED"
            );
        }


        separator();
    }




    private static void printSummaryLine(
            String label,
            int count) {

        System.out.printf(
                "%-30s : %d%n",
                label,
                count
        );
    }




    private static String buildChangeDescription(
            AgateTestCaseImpact impact) {

        String location =
                impact.getChangeLocation();


        String property =
                impact.getChangeProperty();


        if (location == null &&
                property == null) {

            return "<none>";
        }


        if (location == null) {

            return property;
        }


        if (property == null ||
                property.isBlank()) {

            return location;
        }


        /*
         * Coverage impact currently contains properties such as:
         *
         * coverage.enum_value
         *
         * Produces:
         *
         * request.query.language.coverage.enum_value
         */

        return location
                + "."
                + property;
    }




    private static Object valueOrNone(
            Object value) {

        return value != null
                ? value
                : "<none>";
    }




    private static boolean same(
            String first,
            String second) {

        if (first == null) {

            return second == null;
        }


        return first.equals(
                second
        );
    }




    private static void separator() {

        System.out.println(
                "============================================================"
        );
    }




    private static void printUsage() {

        System.err.println(
                "Usage:"
        );


        System.err.println();


        System.err.println(
                "  AgateOpenApiImpactCli "
                        + "--impact "
                        + "<OLD_OPENAPI> "
                        + "<NEW_OPENAPI> "
                        + "<CURRENT_APP_DIRECTORY>"
        );


        System.err.println();


        System.err.println(
                "Example:"
        );


        System.err.println();


        System.err.println(
                "  --impact "
                        + "src/test/resources/change/test-constraints-v1.yaml "
                        + "src/test/resources/change/test-constraints-v2.yaml "
                        + "data/demo"
        );


        System.err.println();


        System.err.println(
                "Exit codes:"
        );


        System.err.println(
                "  0  = no open impacts"
        );


        System.err.println(
                "  1  = invalid CLI arguments"
        );


        System.err.println(
                "  2  = execution error"
        );


        System.err.println(
                "  10 = open impacts found"
        );
    }
}