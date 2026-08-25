package at.co.svc.agate.openapi.change.cli;

import at.co.svc.agate.openapi.change.detection.AgateApiChange;
import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeDetector;
import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeSet;

import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.parser.AgateOpenApiParser;


public class AgateOpenApiChangeCli {


    private static final String OPTION_CHANGES =
            "--changes";




    public static void main(
            String[] args) {

        CliArguments cliArguments;


        try {

            cliArguments =
                    parseArguments(
                            args
                    );

        } catch (IllegalArgumentException e) {

            System.err.println(
                    "ERROR: "
                            + e.getMessage()
            );


            System.err.println();


            printUsage();


            System.exit(1);

            return;
        }


        try {

            AgateOpenApiParser parser =
                    new AgateOpenApiParser();


            AgateOpenApiModel oldModel =
                    parser.parse(
                            cliArguments.oldSource
                    );


            AgateOpenApiModel newModel =
                    parser.parse(
                            cliArguments.newSource
                    );


            AgateOpenApiChangeSet changeSet =
                    new AgateOpenApiChangeDetector()
                            .detect(
                                    oldModel,
                                    newModel
                            );


            printReport(
                    cliArguments.oldSource,
                    cliArguments.newSource,
                    changeSet
            );


        } catch (Exception e) {

            System.err.println(
                    "ERROR: "
                            + e.getMessage()
            );


            e.printStackTrace();


            System.exit(2);
        }
    }




    /*
     * =========================================================
     * ARGUMENTS
     * =========================================================
     */


    private static CliArguments parseArguments(
            String[] args) {

        if (args == null) {

            throw new IllegalArgumentException(
                    "Arguments must not be null"
            );
        }


        if (args.length != 3) {

            throw new IllegalArgumentException(
                    "Expected 3 arguments"
            );
        }


        if (!OPTION_CHANGES.equalsIgnoreCase(
                args[0]
        )) {

            throw new IllegalArgumentException(
                    "Unknown option: "
                            + args[0]
            );
        }


        String oldSource =
                requireSource(
                        args[1],
                        "OLD_OPENAPI_SOURCE"
                );


        String newSource =
                requireSource(
                        args[2],
                        "NEW_OPENAPI_SOURCE"
                );


        return new CliArguments(
                oldSource,
                newSource
        );
    }




    private static String requireSource(
            String value,
            String name) {

        if (value == null ||
                value.isBlank()) {

            throw new IllegalArgumentException(
                    name
                            + " must not be blank"
            );
        }


        return value;
    }




    /*
     * =========================================================
     * REPORT
     * =========================================================
     */


    private static void printReport(
            String oldSource,
            String newSource,
            AgateOpenApiChangeSet changeSet) {

        System.out.println(
                "============================================================"
        );


        System.out.println(
                "AGATE OPENAPI CHANGE DETECTION"
        );


        System.out.println(
                "============================================================"
        );


        System.out.println();


        System.out.println(
                "old source : "
                        + oldSource
        );


        System.out.println(
                "new source : "
                        + newSource
        );


        System.out.println();


        System.out.println(
                "changes    : "
                        + changeSet.size()
        );


        System.out.println(
                "breaking   : "
                        + changeSet.countBreaking()
        );


        System.out.println(
                "review     : "
                        + changeSet.countReview()
        );


        System.out.println();


        if (changeSet.isEmpty()) {

            System.out.println(
                    "No API contract changes detected."
            );


            System.out.println();


            System.out.println(
                    "============================================================"
            );


            return;
        }


        String currentOperation =
                null;


        int number =
                1;


        for (AgateApiChange change :
                changeSet.getChanges()) {

            if (!same(
                    currentOperation,
                    change.getOperationIdentity()
            )) {

                currentOperation =
                        change.getOperationIdentity();


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


            printChange(
                    number,
                    change
            );


            number++;
        }


        System.out.println(
                "============================================================"
        );
    }




    private static void printChange(
            int number,
            AgateApiChange change) {

        System.out.println(
                String.format(
                        "CHANGE %03d",
                        number
                )
        );


        System.out.println(
                "  severity    : "
                        + change.getSeverity()
        );


        System.out.println(
                "  type        : "
                        + change.getChangeType()
        );


        System.out.println(
                "  location    : "
                        + valueOrNone(
                                change.getLocation()
                        )
        );


        if (change.getProperty() != null) {

            System.out.println(
                    "  property    : "
                            + change.getProperty()
            );
        }


        if (change.getOldValue() != null) {

            System.out.println(
                    "  old         : "
                            + change.getOldValue()
            );
        }


        if (change.getNewValue() != null) {

            System.out.println(
                    "  new         : "
                            + change.getNewValue()
            );
        }


        if (change.getDescription() != null) {

            System.out.println(
                    "  description : "
                            + change.getDescription()
            );
        }


        System.out.println();
    }




    private static String valueOrNone(
            String value) {

        if (value == null ||
                value.isBlank()) {

            return "<none>";
        }


        return value;
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




    /*
     * =========================================================
     * USAGE
     * =========================================================
     */


    private static void printUsage() {

        System.err.println(
                "Usage:"
        );


        System.err.println();


        System.err.println(
                "  AgateOpenApiChangeCli "
                        + "--changes "
                        + "<OLD_OPENAPI_SOURCE> "
                        + "<NEW_OPENAPI_SOURCE>"
        );


        System.err.println();


        System.err.println(
                "Examples:"
        );


        System.err.println();


        System.err.println(
                "  --changes "
                        + "src/test/resources/change/test-change-v1.yaml "
                        + "src/test/resources/change/test-change-v2a.yaml"
        );


        System.err.println();


        System.err.println(
                "  --changes "
                        + "src/test/resources/change/test-change-v1.yaml "
                        + "src/test/resources/change/test-change-v2b.yaml"
        );
    }




    /*
     * =========================================================
     * CLI ARGUMENT MODEL
     * =========================================================
     */


    private static class CliArguments {


        private final String oldSource;

        private final String newSource;




        private CliArguments(
                String oldSource,
                String newSource) {

            this.oldSource =
                    oldSource;


            this.newSource =
                    newSource;
        }
    }
}