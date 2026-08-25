package at.co.svc.agate.openapi;

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

import at.co.svc.agate.openapi.phase3.model.AgateExecutableTestPlan;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class Phase3CsvGenerationTest {


    @Test
    void shouldGenerateUsersCsv()
            throws Exception {

        AgateOpenApiModel model =
                parse(
                        "test-openapi-v1.yaml"
                );


        AgateEndpoint endpoint =
                model
                        .getEndpoints()
                        .stream()
                        .filter(value ->
                                "GET".equals(
                                        value.getMethod()
                                )
                        )
                        .filter(value ->
                                "/users/{id}".equals(
                                        value.getPath()
                                )
                        )
                        .findFirst()
                        .orElseThrow();


        AgateOperationModel operation =
                new AgateOperationModelBuilder()
                        .build(
                                endpoint
                        );


        AgateGeneratedTestPlan phase2 =
                new AgatePhase2TestGenerator()
                        .generate(
                                operation
                        );


        AgateExecutableTestPlan phase3 =
                new AgatePhase3Compiler()
                        .compile(
                                operation,
                                phase2
                        );


        AgateCsvTable table =
                new AgateCsvGenerator()
                        .generate(
                                operation,
                                phase3
                        );


        String csv =
                new AgateCsvSerializer()
                        .serialize(
                                table
                        );


        assertTrue(
                csv.contains(
                        "testcaseName;"
                                + "TC001_Baseline_valid_request;"
                                + "TC002_Full_request;"
                                + "TC003_Missing_required_parameter_id;"
                                + "TC004_Optional_parameter_details"
                )
        );


        assertTrue(
                csv.contains(
                        "apiEndpoint;"
                                + "/users/{id};"
                                + "/users/{id};"
                                + "/users/{id};"
                                + "/users/{id}"
                )
        );


        assertTrue(
                csv.contains(
                        "statusCode;"
                                + "200;"
                                + "200;"
                                + "{NULL};"
                                + "200"
                )
        );


        assertTrue(
                csv.contains(
                        "id;"
                                + "1;"
                                + "1;"
                                + "{NULL};"
                                + "1"
                )
        );


        assertTrue(
                csv.contains(
                        "details;"
                                + "{NULL};"
                                + "true;"
                                + "{NULL};"
                                + "true"
                )
        );
    }




    private AgateOpenApiModel parse(
            String resourceName)
            throws Exception {

        var resource =
                Thread
                        .currentThread()
                        .getContextClassLoader()
                        .getResource(
                                resourceName
                        );


        if (resource == null) {

            throw new IllegalStateException(
                    "Test resource not found: "
                            + resourceName
            );
        }


        return new AgateOpenApiParser()
                .parse(
                        Path
                                .of(
                                        resource.toURI()
                                )
                                .toString()
                );
    }
}