package at.co.svc.agate.openapi;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase2.AgatePhase2TestGenerator;

import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestCase;
import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestPlan;
import at.co.svc.agate.openapi.phase2.model.AgateTestCaseType;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class Phase2ConstraintIsolationTest {


    private final AgateOpenApiParser parser =
            new AgateOpenApiParser();


    private final AgateOperationModelBuilder operationBuilder =
            new AgateOperationModelBuilder();


    private final AgatePhase2TestGenerator generator =
            new AgatePhase2TestGenerator();


    @Test
    void stringLengthCasesShouldStillMatchPattern()
            throws Exception {

        AgateGeneratedTestPlan plan =
                plan();


        AgateGeneratedTestCase minLength =
                find(
                        plan,
                        AgateTestCaseType.BODY_MIN_LENGTH
                );


        String username =
                bodyValue(
                        minLength,
                        "username"
                );


        assertTrue(
                username.matches(
                        "^[a-zA-Z0-9]+$"
                )
        );


        AgateGeneratedTestCase maxLength =
                find(
                        plan,
                        AgateTestCaseType.BODY_MAX_LENGTH
                );


        username =
                bodyValue(
                        maxLength,
                        "username"
                );


        assertTrue(
                username.matches(
                        "^[a-zA-Z0-9]+$"
                )
        );
    }


    @Test
    void invalidLengthCaseShouldOnlyViolateLength()
            throws Exception {

        AgateGeneratedTestPlan plan =
                plan();


        AgateGeneratedTestCase testCase =
                find(
                        plan,
                        AgateTestCaseType.BODY_ABOVE_MAX_LENGTH
                );


        String username =
                bodyValue(
                        testCase,
                        "username"
                );


        assertEquals(
                51,
                username.length()
        );


        assertTrue(
                username.matches(
                        "^[a-zA-Z0-9]+$"
                )
        );
    }


    @Test
    void maxItemsCaseShouldKeepUniqueItemsValid()
            throws Exception {

        AgateGeneratedTestPlan plan =
                plan();


        AgateGeneratedTestCase testCase =
                find(
                        plan,
                        AgateTestCaseType.BODY_MAX_ITEMS
                );


        Object raw =
                testCase
                        .getRequestValues()
                        .getBody();


        @SuppressWarnings("unchecked")
        var body =
                (java.util.Map<String, Object>) raw;


        @SuppressWarnings("unchecked")
        List<Object> tags =
                (List<Object>)
                        body.get(
                                "tags"
                        );


        assertEquals(
                10,
                tags.size()
        );


        assertEquals(
                tags.size(),
                new HashSet<>(
                        tags
                ).size()
        );
    }


    private String bodyValue(
            AgateGeneratedTestCase testCase,
            String name) {

        @SuppressWarnings("unchecked")
        var body =
                (java.util.Map<String, Object>)
                        testCase
                                .getRequestValues()
                                .getBody();


        return String.valueOf(
                body.get(
                        name
                )
        );
    }


    private AgateGeneratedTestCase find(
            AgateGeneratedTestPlan plan,
            AgateTestCaseType type) {

        return plan
                .getTestCases()
                .stream()
                .filter(testCase ->
                        testCase.getType()
                                == type
                )
                .findFirst()
                .orElseThrow();
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