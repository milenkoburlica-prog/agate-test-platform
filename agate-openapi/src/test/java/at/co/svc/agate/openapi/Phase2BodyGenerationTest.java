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

public class Phase2BodyGenerationTest {

    private final AgateOpenApiParser parser =
            new AgateOpenApiParser();

    private final AgateOperationModelBuilder operationBuilder =
            new AgateOperationModelBuilder();

    private final AgatePhase2TestGenerator generator =
            new AgatePhase2TestGenerator();


    @Test
    void shouldGenerateBodyEnumCases() throws Exception {

        AgateGeneratedTestPlan plan =
                plan();

        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_ENUM_VALUE
                )
        );

        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_INVALID_ENUM
                )
        );
    }


    @Test
    void shouldGenerateBodyNumericBoundaryCases()
            throws Exception {

        AgateGeneratedTestPlan plan =
                plan();

        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_MINIMUM
                )
        );

        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_MAXIMUM
                )
        );

        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_BELOW_MINIMUM
                )
        );

        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_ABOVE_MAXIMUM
                )
        );
    }


    @Test
    void shouldGenerateBodyStringBoundaryCases()
            throws Exception {

        AgateGeneratedTestPlan plan =
                plan();

        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_MIN_LENGTH
                )
        );

        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_MAX_LENGTH
                )
        );

        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_BELOW_MIN_LENGTH
                )
        );

        assertTrue(
                hasType(
                        plan,
                        AgateTestCaseType.BODY_ABOVE_MAX_LENGTH
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
                        testCase.getType() == type
                );
    }


    private AgateGeneratedTestPlan plan()
            throws Exception {

        AgateOpenApiModel model =
                parse(
                        "test-openapi-v2.yaml"
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
                                "/users".equals(
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
                        .of(resource.toURI())
                        .toString()
        );
    }
}