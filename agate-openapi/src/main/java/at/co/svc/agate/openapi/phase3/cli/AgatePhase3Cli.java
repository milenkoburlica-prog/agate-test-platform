package at.co.svc.agate.openapi.phase3.cli;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase2.AgatePhase2TestGenerator;
import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestPlan;

import at.co.svc.agate.openapi.phase3.AgatePhase3Compiler;

import at.co.svc.agate.openapi.phase3.csv.AgateCsvGenerator;
import at.co.svc.agate.openapi.phase3.csv.AgateCsvSerializer;
import at.co.svc.agate.openapi.phase3.csv.model.AgateCsvTable;

import at.co.svc.agate.openapi.phase3.dsl.AgateRestDslCompiler;
import at.co.svc.agate.openapi.phase3.dsl.model.AgateRestDslExpectation;
import at.co.svc.agate.openapi.phase3.dsl.model.AgateRestDslRequest;
import at.co.svc.agate.openapi.phase3.dsl.model.AgateRestDslTest;

import at.co.svc.agate.openapi.phase3.model.AgateExecutableExpectation;
import at.co.svc.agate.openapi.phase3.model.AgateExecutableRequest;
import at.co.svc.agate.openapi.phase3.model.AgateExecutableTest;
import at.co.svc.agate.openapi.phase3.model.AgateExecutableTestPlan;

import at.co.svc.agate.openapi.phase3.output.AgatePhase3ApplicationGenerator;
import at.co.svc.agate.openapi.phase3.output.AgatePhase3ArtifactGenerator;

import at.co.svc.agate.openapi.phase3.template.AgateYamlTemplateGenerator;

import java.util.Map;


public class AgatePhase3Cli {


    private static final String OPTION_LIST =
            "--list";


    private static final String OPTION_TEST =
            "--test";


    private static final String OPTION_DSL =
            "--dsl";


    private static final String OPTION_CSV =
            "--csv";


    private static final String OPTION_YAML =
            "--yaml";


    private static final String OPTION_GENERATE =
            "--generate";




    public static void main(
            String[] args) {

        CliArguments cliArguments;


        /*
         * =====================================================
         * PARSE CLI ARGUMENTS
         * =====================================================
         */

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

            /*
             * =================================================
             * LOAD OPENAPI
             * =================================================
             */

            AgateOpenApiParser parser =
                    new AgateOpenApiParser();


            AgateOpenApiModel model =
                    parser.parse(
                            cliArguments.source
                    );


            /*
             * =================================================
             * APPLICATION GENERATION
             * =================================================
             *
             * IMPORTANT:
             *
             * --generate does NOT work with one endpoint.
             *
             * It processes the complete OpenAPI document.
             *
             * Therefore this branch MUST happen before
             * findEndpoint().
             */

            if (cliArguments.mode
                    == CliMode.GENERATE) {

                generateApplication(
                        cliArguments.appId,
                        model
                );

                return;
            }


            /*
             * =================================================
             * SINGLE OPERATION MODES
             * =================================================
             */

            AgateEndpoint endpoint =
                    findEndpoint(
                            model,
                            cliArguments.method,
                            cliArguments.path
                    );


            AgateOperationModel operation =
                    new AgateOperationModelBuilder()
                            .build(
                                    endpoint
                            );


            /*
             * =================================================
             * PHASE 2
             * =================================================
             */

            AgateGeneratedTestPlan generatedPlan =
                    new AgatePhase2TestGenerator()
                            .generate(
                                    operation
                            );


            /*
             * =================================================
             * PHASE 3
             * =================================================
             */

            AgateExecutableTestPlan executablePlan =
                    new AgatePhase3Compiler()
                            .compile(
                                    operation,
                                    generatedPlan
                            );


            /*
             * =================================================
             * CLI MODE
             * =================================================
             */

            switch (
                    cliArguments.mode
            ) {

                case LIST -> {

                    printTestList(
                            executablePlan
                    );

                    return;
                }


                case TEST -> {

                    AgateExecutableTest test =
                            findTest(
                                    executablePlan,
                                    cliArguments.testName
                            );


                    printSingleTest(
                            executablePlan,
                            test
                    );

                    return;
                }


                case DSL -> {

                    AgateExecutableTest executableTest =
                            findTest(
                                    executablePlan,
                                    cliArguments.testName
                            );


                    AgateRestDslTest dslTest =
                            new AgateRestDslCompiler()
                                    .compile(
                                            executableTest
                                    );


                    printDslTest(
                            dslTest
                    );

                    return;
                }


                case CSV -> {

                    printCsv(
                            operation,
                            executablePlan
                    );

                    return;
                }


                case YAML -> {

                    printYaml(
                            operation
                    );

                    return;
                }


                case FULL -> {

                    printPlan(
                            executablePlan
                    );

                    return;
                }


                case GENERATE -> {

                    /*
                     * Already handled above.
                     */

                    return;
                }
            }


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
     * ARGUMENT PARSING
     * =========================================================
     */


    private static CliArguments parseArguments(
            String[] args) {

        if (args == null) {

            throw new IllegalArgumentException(
                    "Arguments must not be null"
            );
        }


        /*
         * =====================================================
         * GENERATE COMPLETE APPLICATION
         * =====================================================
         *
         * --generate <APP_ID> <OPENAPI_SOURCE>
         *
         * Example:
         *
         * --generate
         * petstore
         * https://petstore3.swagger.io/api/v3/openapi.json
         *
         * IMPORTANT:
         *
         * This check MUST come before the generic
         * args.length == 3 check.
         */

        if (args.length == 3 &&
                OPTION_GENERATE.equalsIgnoreCase(
                        args[0]
                )) {

            return new CliArguments(
                    args[2],
                    null,
                    null,
                    CliMode.GENERATE,
                    null,
                    requireAppId(
                            args[1]
                    )
            );
        }


        /*
         * =====================================================
         * FULL OPERATION OUTPUT
         * =====================================================
         *
         * <source> <METHOD> <PATH>
         */

        if (args.length == 3) {

            return new CliArguments(
                    args[0],
                    args[1].toUpperCase(),
                    args[2],
                    CliMode.FULL,
                    null,
                    null
            );
        }


        /*
         * =====================================================
         * LIST
         * =====================================================
         *
         * --list <source> <METHOD> <PATH>
         */

        if (args.length == 4 &&
                OPTION_LIST.equalsIgnoreCase(
                        args[0]
                )) {

            return new CliArguments(
                    args[1],
                    args[2].toUpperCase(),
                    args[3],
                    CliMode.LIST,
                    null,
                    null
            );
        }


        /*
         * =====================================================
         * CSV
         * =====================================================
         *
         * --csv <source> <METHOD> <PATH>
         */

        if (args.length == 4 &&
                OPTION_CSV.equalsIgnoreCase(
                        args[0]
                )) {

            return new CliArguments(
                    args[1],
                    args[2].toUpperCase(),
                    args[3],
                    CliMode.CSV,
                    null,
                    null
            );
        }


        /*
         * =====================================================
         * YAML
         * =====================================================
         *
         * --yaml <source> <METHOD> <PATH>
         */

        if (args.length == 4 &&
                OPTION_YAML.equalsIgnoreCase(
                        args[0]
                )) {

            return new CliArguments(
                    args[1],
                    args[2].toUpperCase(),
                    args[3],
                    CliMode.YAML,
                    null,
                    null
            );
        }


        /*
         * =====================================================
         * TEST
         * =====================================================
         *
         * --test <TECHNICAL_NAME> <source> <METHOD> <PATH>
         */

        if (args.length == 5 &&
                OPTION_TEST.equalsIgnoreCase(
                        args[0]
                )) {

            return new CliArguments(
                    args[2],
                    args[3].toUpperCase(),
                    args[4],
                    CliMode.TEST,
                    requireTestName(
                            args[1]
                    ),
                    null
            );
        }


        /*
         * =====================================================
         * DSL
         * =====================================================
         *
         * --dsl <TECHNICAL_NAME> <source> <METHOD> <PATH>
         */

        if (args.length == 5 &&
                OPTION_DSL.equalsIgnoreCase(
                        args[0]
                )) {

            return new CliArguments(
                    args[2],
                    args[3].toUpperCase(),
                    args[4],
                    CliMode.DSL,
                    requireTestName(
                            args[1]
                    ),
                    null
            );
        }


        throw new IllegalArgumentException(
                "Invalid arguments"
        );
    }




    private static String requireTestName(
            String value) {

        if (value == null ||
                value.isBlank()) {

            throw new IllegalArgumentException(
                    "Technical test name must not be blank"
            );
        }


        return value;
    }




    private static String requireAppId(
            String value) {

        if (value == null ||
                value.isBlank()) {

            throw new IllegalArgumentException(
                    "appId must not be blank"
            );
        }


        if (!value.matches(
                "[a-zA-Z0-9._-]+"
        )) {

            throw new IllegalArgumentException(
                    "Invalid appId: "
                            + value
            );
        }


        if (".".equals(value) ||
                "..".equals(value)) {

            throw new IllegalArgumentException(
                    "Invalid appId: "
                            + value
            );
        }


        return value;
    }




    /*
     * =========================================================
     * COMPLETE APPLICATION GENERATION
     * =========================================================
     */


    private static void generateApplication(
            String appId,
            AgateOpenApiModel model)
            throws Exception {

        AgatePhase3ApplicationGenerator.GenerationResult result =
                new AgatePhase3ApplicationGenerator()
                        .generate(
                                appId,
                                model
                        );


        System.out.println(
                "========================================"
        );


        System.out.println(
                "AGATE PHASE 3 - APPLICATION GENERATED"
        );


        System.out.println(
                "========================================"
        );


        System.out.println();


        System.out.println(
                "appId      : "
                        + result.getAppId()
        );


        System.out.println(
                "directory  : "
                        + result
                                .getAppDirectory()
                                .toAbsolutePath()
        );


        System.out.println(
                "operations : "
                        + result.getOperationCount()
        );


        System.out.println();


        for (AgatePhase3ArtifactGenerator.GenerationResult operation :
                result.getOperations()) {

            System.out.println(
                    operation.getOperationIdentity()
            );


            System.out.println(
                    "  suite   : "
                            + operation.getTestSuiteName()
            );


            System.out.println(
                    "  csv     : "
                            + operation.getCsvFile()
            );


            System.out.println(
                    "  yaml    : "
                            + operation.getYamlFile()
            );


            System.out.println(
                    "  module  : "
                            + operation.getModuleDirectory()
            );


            System.out.println(
                    "  metdata : "
                            + operation.getMetadataFile()
            );


            if (operation.getRequestFile() != null) {

                System.out.println(
                        "  request : "
                                + operation.getRequestFile()
                );
            }


            System.out.println();
        }


        System.out.println(
                "========================================"
        );
    }




    /*
     * =========================================================
     * ENDPOINT / TEST LOOKUP
     * =========================================================
     */


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




    private static AgateExecutableTest findTest(
            AgateExecutableTestPlan plan,
            String technicalName) {

        return plan
                .getTests()
                .stream()
                .filter(test ->
                        technicalName.equals(
                                test.getTechnicalName()
                        )
                )
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Test not found: "
                                                + technicalName
                                )
                );
    }




    /*
     * =========================================================
     * CSV
     * =========================================================
     */


    private static void printCsv(
            AgateOperationModel operation,
            AgateExecutableTestPlan plan) {

        AgateCsvTable table =
                new AgateCsvGenerator()
                        .generate(
                                operation,
                                plan
                        );


        String csv =
                new AgateCsvSerializer()
                        .serialize(
                                table
                        );


        System.out.print(
                csv
        );
    }




    /*
     * =========================================================
     * YAML
     * =========================================================
     */


    private static void printYaml(
            AgateOperationModel operation) {

        String yaml =
                new AgateYamlTemplateGenerator()
                        .generate(
                                operation
                        );


        System.out.print(
                yaml
        );
    }




    /*
     * =========================================================
     * LIST
     * =========================================================
     */


    private static void printTestList(
            AgateExecutableTestPlan plan) {

        System.out.println(
                "========================================"
        );


        System.out.println(
                "AGATE PHASE 3 - TEST LIST"
        );


        System.out.println(
                "========================================"
        );


        System.out.println();


        System.out.println(
                "identity : "
                        + plan.getOperationIdentity()
        );


        System.out.println(
                "tests    : "
                        + plan.size()
        );


        System.out.println();


        for (AgateExecutableTest test :
                plan.getTests()) {

            System.out.println(
                    test.getTechnicalName()
            );
        }


        System.out.println();


        System.out.println(
                "========================================"
        );
    }




    /*
     * =========================================================
     * SINGLE TEST
     * =========================================================
     */


    private static void printSingleTest(
            AgateExecutableTestPlan plan,
            AgateExecutableTest test) {

        System.out.println(
                "========================================"
        );


        System.out.println(
                "AGATE PHASE 3 - TEST"
        );


        System.out.println(
                "========================================"
        );


        System.out.println();


        System.out.println(
                "identity      : "
                        + plan.getOperationIdentity()
        );


        System.out.println(
                "technicalName : "
                        + test.getTechnicalName()
        );


        System.out.println();


        printTest(
                test
        );


        System.out.println(
                "========================================"
        );
    }




    /*
     * =========================================================
     * DSL
     * =========================================================
     */


    private static void printDslTest(
            AgateRestDslTest test) {

        System.out.println(
                "========================================"
        );


        System.out.println(
                "AGATE PHASE 3 - REST DSL"
        );


        System.out.println(
                "========================================"
        );


        System.out.println();


        System.out.println(
                "technicalName="
                        + test.getTechnicalName()
        );


        System.out.println(
                "sourceTestId="
                        + test.getSourceTestId()
        );


        System.out.println(
                "operationIdentity="
                        + test.getOperationIdentity()
        );


        System.out.println(
                "sourceType="
                        + test.getSourceType()
        );


        System.out.println();


        printDslRequest(
                test.getRequest()
        );


        System.out.println();


        printDslExpectation(
                test.getExpectation()
        );


        System.out.println();


        System.out.println(
                "========================================"
        );
    }




    private static void printDslRequest(
            AgateRestDslRequest request) {

        System.out.println(
                "REST:"
        );


        System.out.println(
                "  method="
                        + request.getMethod()
        );


        System.out.println(
                "  path="
                        + request.getPath()
        );


        printMap(
                "query",
                request.getQuery()
        );


        printMap(
                "headers",
                request.getHeaders()
        );


        printMap(
                "cookies",
                request.getCookies()
        );


        if (request.getBodyMediaType() != null) {

            System.out.println(
                    "  bodyMediaType="
                            + request.getBodyMediaType()
            );
        }


        if (request.getBody() != null) {

            System.out.println(
                    "  body="
                            + request.getBody()
            );

        } else {

            System.out.println(
                    "  body=<none>"
            );
        }
    }




    private static void printDslExpectation(
            AgateRestDslExpectation expectation) {

        System.out.println(
                "EXPECTED:"
        );


        System.out.println(
                "  outcome="
                        + expectation.getOutcome()
        );


        System.out.println(
                "  status="
                        + (
                                expectation.getStatusCode()
                                        != null
                                        ? expectation.getStatusCode()
                                        : "<unspecified>"
                        )
        );


        System.out.println(
                "  exactStatusRequired="
                        + expectation.isExactStatusRequired()
        );
    }




    /*
     * =========================================================
     * FULL PLAN
     * =========================================================
     */


    private static void printPlan(
            AgateExecutableTestPlan plan) {

        System.out.println(
                "========================================"
        );


        System.out.println(
                "AGATE PHASE 3"
        );


        System.out.println(
                "========================================"
        );


        System.out.println();


        System.out.println(
                "identity : "
                        + plan.getOperationIdentity()
        );


        System.out.println(
                "tests    : "
                        + plan.size()
        );


        System.out.println();


        for (AgateExecutableTest test :
                plan.getTests()) {

            printTest(
                    test
            );
        }
    }




    private static void printTest(
            AgateExecutableTest test) {

        System.out.println(
                "----------------------------------------"
        );


        System.out.println(
                test.getId()
        );


        System.out.println();


        System.out.println(
                "technicalName="
                        + test.getTechnicalName()
        );


        System.out.println(
                "name="
                        + test.getName()
        );


        System.out.println(
                "sourceType="
                        + test.getSourceType()
        );


        System.out.println(
                "reason="
                        + test.getReason()
        );


        System.out.println();


        printRequest(
                test.getRequest()
        );


        System.out.println();


        printExpectation(
                test.getExpectation()
        );


        System.out.println();
    }




    private static void printRequest(
            AgateExecutableRequest request) {

        System.out.println(
                "request:"
        );


        System.out.println();


        System.out.println(
                "  method="
                        + request.getMethod()
        );


        System.out.println(
                "  pathTemplate="
                        + request.getPathTemplate()
        );


        System.out.println(
                "  resolvedPath="
                        + request.getResolvedPath()
        );


        System.out.println();


        printMap(
                "pathParameters",
                request.getPathParameters()
        );


        printMap(
                "queryParameters",
                request.getQueryParameters()
        );


        printMap(
                "headers",
                request.getHeaders()
        );


        printMap(
                "cookies",
                request.getCookies()
        );


        if (request.getBodyMediaType() != null) {

            System.out.println(
                    "  bodyMediaType="
                            + request.getBodyMediaType()
            );
        }


        if (request.getBody() != null) {

            System.out.println(
                    "  body="
                            + request.getBody()
            );

        } else {

            System.out.println(
                    "  body=<none>"
            );
        }
    }




    private static void printExpectation(
            AgateExecutableExpectation expectation) {

        System.out.println(
                "expectation:"
        );


        System.out.println();


        System.out.println(
                "  outcome="
                        + expectation.getExpectedOutcome()
        );


        System.out.println(
                "  status="
                        + (
                                expectation.getExpectedStatusCode()
                                        != null
                                        ? expectation.getExpectedStatusCode()
                                        : "<unspecified>"
                        )
        );


        System.out.println(
                "  exactStatusRequired="
                        + expectation.isExactStatusRequired()
        );
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
                "  AgatePhase3Cli "
                        + "<source> "
                        + "<METHOD> "
                        + "<PATH>"
        );


        System.err.println();


        System.err.println(
                "  AgatePhase3Cli "
                        + "--list "
                        + "<source> "
                        + "<METHOD> "
                        + "<PATH>"
        );


        System.err.println();


        System.err.println(
                "  AgatePhase3Cli "
                        + "--csv "
                        + "<source> "
                        + "<METHOD> "
                        + "<PATH>"
        );


        System.err.println();


        System.err.println(
                "  AgatePhase3Cli "
                        + "--yaml "
                        + "<source> "
                        + "<METHOD> "
                        + "<PATH>"
        );


        System.err.println();


        System.err.println(
                "  AgatePhase3Cli "
                        + "--test "
                        + "<TECHNICAL_NAME> "
                        + "<source> "
                        + "<METHOD> "
                        + "<PATH>"
        );


        System.err.println();


        System.err.println(
                "  AgatePhase3Cli "
                        + "--dsl "
                        + "<TECHNICAL_NAME> "
                        + "<source> "
                        + "<METHOD> "
                        + "<PATH>"
        );


        System.err.println();


        System.err.println(
                "  AgatePhase3Cli "
                        + "--generate "
                        + "<APP_ID> "
                        + "<OPENAPI_SOURCE>"
        );
    }




    /*
     * =========================================================
     * CLI TYPES
     * =========================================================
     */


    private enum CliMode {

        FULL,

        LIST,

        TEST,

        DSL,

        CSV,

        YAML,

        GENERATE
    }




    private static class CliArguments {


        private final String source;

        private final String method;

        private final String path;

        private final CliMode mode;

        private final String testName;

        private final String appId;




        private CliArguments(
                String source,
                String method,
                String path,
                CliMode mode,
                String testName,
                String appId) {

            this.source =
                    source;


            this.method =
                    method;


            this.path =
                    path;


            this.mode =
                    mode;


            this.testName =
                    testName;


            this.appId =
                    appId;
        }
    }
}