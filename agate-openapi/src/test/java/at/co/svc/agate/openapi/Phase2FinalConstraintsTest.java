package at.co.svc.agate.openapi;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase2.AgatePhase2TestGenerator;

import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestPlan;
import at.co.svc.agate.openapi.phase2.model.AgateTestCaseType;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class Phase2FinalConstraintsTest {


    private final AgateOpenApiParser parser =
            new AgateOpenApiParser();


    private final AgateOperationModelBuilder operationBuilder =
            new AgateOperationModelBuilder();


    private final AgatePhase2TestGenerator generator =
            new AgatePhase2TestGenerator();


    @Test
    void shouldGenerateMultipleOfCases()
            throws Exception {

        AgateGeneratedTestPlan plan =
                plan();


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.MULTIPLE_OF_VALID
                )
        );


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.MULTIPLE_OF_INVALID
                )
        );


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_MULTIPLE_OF_VALID
                )
        );


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_MULTIPLE_OF_INVALID
                )
        );
    }


    @Test
    void shouldGenerateExclusiveBoundaryCases()
            throws Exception {

        AgateGeneratedTestPlan plan =
                plan();


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.EXCLUSIVE_MINIMUM_VALID
                )
        );


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.EXCLUSIVE_MINIMUM_INVALID
                )
        );


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.EXCLUSIVE_MAXIMUM_VALID
                )
        );


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.EXCLUSIVE_MAXIMUM_INVALID
                )
        );


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_EXCLUSIVE_MINIMUM_VALID
                )
        );


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_EXCLUSIVE_MINIMUM_INVALID
                )
        );
    }


    @Test
    void shouldGenerateFormatCases()
            throws Exception {

        AgateGeneratedTestPlan plan =
                plan();


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.FORMAT_VALID
                )
        );


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.FORMAT_INVALID
                )
        );


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_FORMAT_VALID
                )
        );


        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_FORMAT_INVALID
                )
        );
    }


    private boolean hasType(
            AgateGeneratedTestPlan plan,
            AgateTestCaseType type) {

        return plan
                .getTestCases()
                .stream()
                .anyMatch(testCase ->
                        testCase.getType()
                                == type
                );
    }


    private AgateGeneratedTestPlan plan()
            throws Exception {

        AgateOpenApiModel model =
                parse(
                        "test-openapi-v13-phase2-final.yaml"
                );


        AgateEndpoint endpoint =
                model
                        .getEndpoints()
                        .stream()
                        .filter(value ->
                                "POST".equals(
                                        value.getMethod()
                                )
                        )
                        .filter(value ->
                                "/validation-demo".equals(
                                        value.getPath()
                                )
                        )
                        .findFirst()
                        .orElseThrow();


        AgateOperationModel operation =
                operationBuilder.build(
                        endpoint
                );


        return generator.generate(
                operation
        );
    }


    private AgateOpenApiModel parse(
            String resourceName) throws Exception {

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