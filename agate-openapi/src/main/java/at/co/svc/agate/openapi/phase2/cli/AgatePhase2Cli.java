package at.co.svc.agate.openapi.phase2.cli;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestValues;

import at.co.svc.agate.openapi.phase2.AgatePhase2TestGenerator;
import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestCase;
import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestPlan;
import at.co.svc.agate.openapi.phase2.model.AgatePhase2Options;

import java.util.Map;

public class AgatePhase2Cli {


    public static void main(
            String[] args) {

        if (args.length < 3) {

            printUsage();

            System.exit(1);
        }

        String source =
                args[0];

        String method =
                args[1]
                        .toUpperCase();

        String path =
                args[2];


        AgatePhase2Options options =
                new AgatePhase2Options();


        try {

            parseOptions(
                    args,
                    options
            );


            AgateOpenApiParser parser =
                    new AgateOpenApiParser();

            AgateOpenApiModel openApiModel =
                    parser.parse(
                            source
                    );


            AgateEndpoint endpoint =
                    findEndpoint(
                            openApiModel,
                            method,
                            path
                    );


            AgateOperationModelBuilder operationBuilder =
                    new AgateOperationModelBuilder();

            AgateOperationModel operation =
                    operationBuilder.build(
                            endpoint
                    );


            AgatePhase2TestGenerator generator =
                    new AgatePhase2TestGenerator();

            AgateGeneratedTestPlan plan =
                    generator.generate(
                            operation,
                            options
                    );


            printPlan(
                    operation,
                    plan
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


    private static void parseOptions(
            String[] args,
            AgatePhase2Options options) {

        int index =
                3;

        while (index < args.length) {

            String option =
                    args[index];

            if ("--no-negative".equals(option)) {

                options.setGenerateNegativeTests(
                        false
                );

            } else if ("--no-optional".equals(option)) {

                options.setGenerateOptionalParameterTests(
                        false
                );

            } else if ("--no-enum".equals(option)) {

                options.setGenerateEnumTests(
                        false
                );

            } else if ("--no-boundary".equals(option)) {

                options.setGenerateBoundaryTests(
                        false
                );

            } else if ("--max-enum".equals(option)) {

                index++;

                if (index >= args.length) {

                    throw new IllegalArgumentException(
                            "Missing value for --max-enum"
                    );
                }

                options.setMaximumEnumCases(
                        Integer.parseInt(
                                args[index]
                        )
                );

            } else {

                throw new IllegalArgumentException(
                        "Unknown option: "
                                + option
                );
            }

            index++;
        }
    }


    private static AgateEndpoint findEndpoint(
            AgateOpenApiModel model,
            String method,
            String path) {

        return model
                .getEndpoints()
                .stream()
                .filter(endpoint ->
                        method.equalsIgnoreCase(
                                endpoint.getMethod()
                        )
                )
                .filter(endpoint ->
                        path.equals(
                                endpoint.getPath()
                        )
                )
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Endpoint not found: "
                                                + method
                                                + " "
                                                + path
                                )
                );
    }


    private static void printPlan(
            AgateOperationModel operation,
            AgateGeneratedTestPlan plan) {

        System.out.println(
                "========================================"
        );

        System.out.println(
                "AGATE PHASE 2"
        );

        System.out.println(
                "========================================"
        );

        System.out.println(
                "identity : "
                        + operation.getIdentity()
        );

        System.out.println(
                "method   : "
                        + operation.getMethod()
        );

        System.out.println(
                "path     : "
                        + operation.getPath()
        );

        System.out.println(
                "tests    : "
                        + plan.size()
        );

        System.out.println();


        for (AgateGeneratedTestCase testCase :
                plan.getTestCases()) {

            printTestCase(
                    testCase
            );
        }
    }


    private static void printTestCase(
            AgateGeneratedTestCase testCase) {

        System.out.println(
                "----------------------------------------"
        );

        System.out.println(
                testCase.getId()
        );

        System.out.println(
                "name="
                        + testCase.getName()
        );

        System.out.println(
                "type="
                        + testCase.getType()
        );

        System.out.println(
                "expectedOutcome="
                        + testCase.getExpectedOutcome()
        );

        System.out.println(
                "expectedStatusCode="
                        + (
                                testCase.getExpectedStatusCode() != null
                                        ? testCase.getExpectedStatusCode()
                                        : "<unspecified>"
                        )
        );

        System.out.println(
                "reason="
                        + testCase.getReason()
        );

        printRequestValues(
                testCase.getRequestValues()
        );

        System.out.println();
    }


    private static void printRequestValues(
            AgateRequestValues values) {

        System.out.println(
                "request:"
        );

        if (values == null) {

            System.out.println(
                    "  <none>"
            );

            return;
        }

        printMap(
                "path",
                values.getPath()
        );

        printMap(
                "query",
                values.getQuery()
        );

        printMap(
                "headers",
                values.getHeaders()
        );

        printMap(
                "cookies",
                values.getCookies()
        );


        if (values.getBodyMediaType() != null) {

            System.out.println(
                    "  bodyMediaType="
                            + values.getBodyMediaType()
            );
        }


        if (values.getBody() != null) {

            System.out.println(
                    "  body="
                            + values.getBody()
            );

        } else {

            System.out.println(
                    "  body=<none>"
            );
        }
    }


    private static void printMap(
            String name,
            Map<String, Object> values) {

        System.out.println(
                "  "
                        + name
                        + ":"
        );

        if (values == null ||
                values.isEmpty()) {

            System.out.println(
                    "    <none>"
            );

            return;
        }

        values.forEach(
                (key, value) ->
                        System.out.println(
                                "    "
                                        + key
                                        + "="
                                        + value
                        )
        );
    }


    private static void printUsage() {

        System.err.println(
                "Usage:"
        );

        System.err.println(
                "  AgatePhase2Cli "
                        + "<openapi-source> "
                        + "<METHOD> "
                        + "<PATH> "
                        + "[options]"
        );

        System.err.println();

        System.err.println(
                "Options:"
        );

        System.err.println(
                "  --no-negative"
        );

        System.err.println(
                "  --no-optional"
        );

        System.err.println(
                "  --no-enum"
        );

        System.err.println(
                "  --no-boundary"
        );

        System.err.println(
                "  --max-enum <number>"
        );

        System.err.println();

        System.err.println(
                "Examples:"
        );

        System.err.println(
                "  src/test/resources/test-openapi-v1.yaml "
                        + "GET "
                        + "/users/{id}"
        );

        System.err.println();

        System.err.println(
                "  src/test/resources/test-openapi-v2.yaml "
                        + "POST "
                        + "/users"
        );
    }
}