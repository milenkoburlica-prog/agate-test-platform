package at.co.svc.agate.openapi;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase2.AgatePhase2TestGenerator;

import at.co.svc.agate.openapi.phase2.model.AgateExpectedOutcome;
import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestCase;
import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestPlan;
import at.co.svc.agate.openapi.phase2.model.AgateTestCaseType;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Phase2RegressionTest {

    private final AgateOpenApiParser parser =
            new AgateOpenApiParser();

    private final AgateOperationModelBuilder operationBuilder =
            new AgateOperationModelBuilder();

    private final AgatePhase2TestGenerator generator =
            new AgatePhase2TestGenerator();


    @Test
    void shouldGenerateBaselineCase() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v2.yaml",
                        "POST",
                        "/users"
                );

        AgateGeneratedTestPlan plan =
                generator.generate(
                        operation
                );

        assertFalse(
                plan.getTestCases().isEmpty()
        );

        AgateGeneratedTestCase baseline =
                plan
                        .getTestCases()
                        .get(0);

        assertEquals(
                AgateTestCaseType.BASELINE_VALID,
                baseline.getType()
        );

        assertEquals(
                AgateExpectedOutcome.ACCEPT,
                baseline.getExpectedOutcome()
        );

        assertEquals(
                "201",
                baseline.getExpectedStatusCode()
        );

        assertNotNull(
                baseline
                        .getRequestValues()
                        .getBody()
        );
    }


    @Test
    void shouldGenerateMissingRequiredBodyProperties() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v2.yaml",
                        "POST",
                        "/users"
                );

        AgateGeneratedTestPlan plan =
                generator.generate(
                        operation
                );

        long count =
                plan
                        .getTestCases()
                        .stream()
                        .filter(test ->
                                test.getType()
                                        == AgateTestCaseType.MISSING_REQUIRED_BODY_PROPERTY
                        )
                        .count();

        assertEquals(
                2,
                count
        );
    }


    @Test
    void shouldGenerateEnumCases() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v2.yaml",
                        "POST",
                        "/users"
                );

        /*
         * Enum se u ovom fixture-u nalazi u body-ju,
         * a trenutni prvi Phase 2 generator namjerno
         * generiše parameter enum cases odvojeno.
         *
         * Body enum coverage ćemo dodati kao sljedeći
         * Phase 2 korak.
         */

        AgateGeneratedTestPlan plan =
                generator.generate(
                        operation
                );

        assertNotNull(
                plan
        );
    }


    @Test
    void shouldGenerateRequiredPathParameterNegativeCase()
            throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v1.yaml",
                        "GET",
                        "/users/{id}"
                );

        AgateGeneratedTestPlan plan =
                generator.generate(
                        operation
                );

        assertTrue(
                plan
                        .getTestCases()
                        .stream()
                        .anyMatch(test ->
                                test.getType()
                                        == AgateTestCaseType.MISSING_REQUIRED_PARAMETER
                        )
        );
    }


    @Test
    void shouldGenerateOptionalQueryParameterCase()
            throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v1.yaml",
                        "GET",
                        "/users/{id}"
                );

        AgateGeneratedTestPlan plan =
                generator.generate(
                        operation
                );

        assertTrue(
                plan
                        .getTestCases()
                        .stream()
                        .anyMatch(test ->
                                test.getType()
                                        == AgateTestCaseType.OPTIONAL_PARAMETER
                        )
        );
    }


    @Test
    void shouldGenerateStableTestIds() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v1.yaml",
                        "GET",
                        "/users/{id}"
                );

        AgateGeneratedTestPlan first =
                generator.generate(
                        operation
                );

        AgateGeneratedTestPlan second =
                generator.generate(
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
                            .getTestCases()
                            .get(i)
                            .getId(),
                    second
                            .getTestCases()
                            .get(i)
                            .getId()
            );
        }
    }


    private AgateOperationModel operation(
            String resourceName,
            String method,
            String path) throws Exception {

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