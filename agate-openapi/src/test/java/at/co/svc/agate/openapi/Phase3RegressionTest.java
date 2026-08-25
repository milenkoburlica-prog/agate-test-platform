package at.co.svc.agate.openapi;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase2.AgatePhase2TestGenerator;
import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestPlan;

import at.co.svc.agate.openapi.phase3.AgatePhase3Compiler;

import at.co.svc.agate.openapi.phase3.model.AgateExecutableTest;
import at.co.svc.agate.openapi.phase3.model.AgateExecutableTestPlan;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class Phase3RegressionTest {


    private final AgateOpenApiParser parser =
            new AgateOpenApiParser();


    private final AgateOperationModelBuilder operationBuilder =
            new AgateOperationModelBuilder();


    private final AgatePhase2TestGenerator phase2Generator =
            new AgatePhase2TestGenerator();


    private final AgatePhase3Compiler compiler =
            new AgatePhase3Compiler();




    @Test
    void shouldCompileAllGeneratedTests()
            throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v2.yaml",
                        "POST",
                        "/users"
                );


        AgateGeneratedTestPlan phase2 =
                phase2Generator.generate(
                        operation
                );


        AgateExecutableTestPlan phase3 =
                compiler.compile(
                        operation,
                        phase2
                );


        assertEquals(
                phase2.size(),
                phase3.size()
        );
    }




    @Test
    void shouldPreserveGeneratedTestIdentity()
            throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v2.yaml",
                        "POST",
                        "/users"
                );


        AgateGeneratedTestPlan phase2 =
                phase2Generator.generate(
                        operation
                );


        AgateExecutableTestPlan phase3 =
                compiler.compile(
                        operation,
                        phase2
                );


        assertEquals(
                phase2
                        .getTestCases()
                        .get(0)
                        .getId(),
                phase3
                        .getTests()
                        .get(0)
                        .getId()
        );
    }




    @Test
    void shouldBuildConcretePostRequest()
            throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v2.yaml",
                        "POST",
                        "/users"
                );


        AgateExecutableTestPlan plan =
                compile(
                        operation
                );


        AgateExecutableTest baseline =
                plan
                        .getTests()
                        .get(0);


        assertEquals(
                "POST",
                baseline
                        .getRequest()
                        .getMethod()
        );


        assertEquals(
                "/users",
                baseline
                        .getRequest()
                        .getResolvedPath()
        );


        assertEquals(
                "application/json",
                baseline
                        .getRequest()
                        .getBodyMediaType()
        );


        assertNotNull(
                baseline
                        .getRequest()
                        .getBody()
        );
    }




    @Test
    void shouldResolvePathParameter()
            throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v1.yaml",
                        "GET",
                        "/users/{id}"
                );


        AgateExecutableTestPlan plan =
                compile(
                        operation
                );


        AgateExecutableTest baseline =
                plan
                        .getTests()
                        .get(0);


        assertEquals(
                "/users/{id}",
                baseline
                        .getRequest()
                        .getPathTemplate()
        );


        assertEquals(
                "/users/1",
                baseline
                        .getRequest()
                        .getResolvedPath()
        );
    }




    @Test
    void positiveTestShouldRequireExactStatus()
            throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v2.yaml",
                        "POST",
                        "/users"
                );


        AgateExecutableTestPlan plan =
                compile(
                        operation
                );


        AgateExecutableTest baseline =
                plan
                        .getTests()
                        .get(0);


        assertEquals(
                "201",
                baseline
                        .getExpectation()
                        .getExpectedStatusCode()
        );


        assertTrue(
                baseline
                        .getExpectation()
                        .isExactStatusRequired()
        );
    }




    @Test
    void negativeTestWithoutStatusShouldNotRequireExactStatus()
            throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v2.yaml",
                        "POST",
                        "/users"
                );


        AgateExecutableTestPlan plan =
                compile(
                        operation
                );


        AgateExecutableTest negative =
                plan
                        .getTests()
                        .stream()
                        .filter(test ->
                                test.getExpectation()
                                        .getExpectedStatusCode()
                                        == null
                        )
                        .findFirst()
                        .orElseThrow();


        assertFalse(
                negative
                        .getExpectation()
                        .isExactStatusRequired()
        );
    }




    @Test
    void phase3CompilationShouldBeDeterministic()
            throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v2.yaml",
                        "POST",
                        "/users"
                );


        AgateExecutableTestPlan first =
                compile(
                        operation
                );


        AgateExecutableTestPlan second =
                compile(
                        operation
                );


        assertEquals(
                first.size(),
                second.size()
        );


        for (int i = 0;
             i < first.size();
             i++) {

            assertEquals(
                    first
                            .getTests()
                            .get(i)
                            .getId(),
                    second
                            .getTests()
                            .get(i)
                            .getId()
            );


            assertEquals(
                    first
                            .getTests()
                            .get(i)
                            .getRequest()
                            .getResolvedPath(),
                    second
                            .getTests()
                            .get(i)
                            .getRequest()
                            .getResolvedPath()
            );
        }
    }




    private AgateExecutableTestPlan compile(
            AgateOperationModel operation) {

        AgateGeneratedTestPlan phase2 =
                phase2Generator.generate(
                        operation
                );


        return compiler.compile(
                operation,
                phase2
        );
    }




    private AgateOperationModel operation(
            String resourceName,
            String method,
            String path)
            throws Exception {

        AgateOpenApiModel model =
                parse(
                        resourceName
                );


        AgateEndpoint endpoint =
                model
                        .getEndpoints()
                        .stream()
                        .filter(value ->
                                method.equals(
                                        value.getMethod()
                                )
                        )
                        .filter(value ->
                                path.equals(
                                        value.getPath()
                                )
                        )
                        .findFirst()
                        .orElseThrow();


        return operationBuilder.build(
                endpoint
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


        return parser.parse(
                Path
                        .of(
                                resource.toURI()
                        )
                        .toString()
        );
    }
}