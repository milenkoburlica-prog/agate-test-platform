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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Phase2ParameterArrayTest {

    private final AgateOpenApiParser parser =
            new AgateOpenApiParser();

    private final AgateOperationModelBuilder operationBuilder =
            new AgateOperationModelBuilder();

    private final AgatePhase2TestGenerator generator =
            new AgatePhase2TestGenerator();


    @Test
    void shouldGenerateParameterMaxItemsCases()
            throws Exception {

        AgateGeneratedTestPlan plan =
                plan();


        AgateGeneratedTestCase maxItems =
                find(
                        plan,
                        AgateTestCaseType.MAX_ITEMS
                );


        @SuppressWarnings("unchecked")
        List<Object> maxValue =
                (List<Object>)
                        maxItems
                                .getRequestValues()
                                .getQuery()
                                .get("groups");


        assertEquals(
                20,
                maxValue.size()
        );


        AgateGeneratedTestCase aboveMaxItems =
                find(
                        plan,
                        AgateTestCaseType.ABOVE_MAX_ITEMS
                );


        @SuppressWarnings("unchecked")
        List<Object> aboveValue =
                (List<Object>)
                        aboveMaxItems
                                .getRequestValues()
                                .getQuery()
                                .get("groups");


        assertEquals(
                21,
                aboveValue.size()
        );
    }


    @Test
    void shouldGenerateArrayItemMaxLengthCases()
            throws Exception {

        AgateGeneratedTestPlan plan =
                plan();


        AgateGeneratedTestCase maxLength =
                find(
                        plan,
                        AgateTestCaseType.ARRAY_ITEM_MAX_LENGTH
                );


        @SuppressWarnings("unchecked")
        List<Object> values =
                (List<Object>)
                        maxLength
                                .getRequestValues()
                                .getQuery()
                                .get("groups");


        assertEquals(
                1,
                values.size()
        );


        assertEquals(
                50,
                String.valueOf(
                        values.get(0)
                ).length()
        );
    }


    @Test
    void shouldGenerateArrayItemAboveMaxLengthCase()
            throws Exception {

        AgateGeneratedTestPlan plan =
                plan();


        AgateGeneratedTestCase testCase =
                find(
                        plan,
                        AgateTestCaseType.ARRAY_ITEM_ABOVE_MAX_LENGTH
                );


        @SuppressWarnings("unchecked")
        List<Object> values =
                (List<Object>)
                        testCase
                                .getRequestValues()
                                .getQuery()
                                .get("groups");


        assertEquals(
                51,
                String.valueOf(
                        values.get(0)
                ).length()
        );
    }


    @Test
    void shouldGenerateStableReadersUrlPlan()
            throws Exception {

        AgateGeneratedTestPlan first =
                plan();

        AgateGeneratedTestPlan second =
                plan();


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
                .orElseThrow(
                        () ->
                                new AssertionError(
                                        "Test case not found: "
                                                + type
                                )
                );
    }


    private AgateGeneratedTestPlan plan()
            throws Exception {

        AgateOpenApiModel model =
                parse(
                        "test-openapi-v12-readers-url-constraints.yaml"
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
                                "/readersUrl".equals(
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