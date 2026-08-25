package at.co.svc.agate.openapi;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.parser.AgateOpenApiParser;
import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.AgateTestInteractionBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestValues;
import at.co.svc.agate.openapi.phase1.model.AgateResponseContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateResponseModel;
import at.co.svc.agate.openapi.phase1.model.AgateTestInteractionModel;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestInteractionBuilderTest {

    private final AgateOpenApiParser parser =
            new AgateOpenApiParser();

    private final AgateOperationModelBuilder operationBuilder =
            new AgateOperationModelBuilder();

    private final AgateTestInteractionBuilder interactionBuilder =
            new AgateTestInteractionBuilder();


    @Test
    void shouldBuildCompleteInteraction() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v11-content-examples.yaml",
                        "GET",
                        "/example"
                );

        AgateTestInteractionModel interaction =
                interactionBuilder.build(
                        operation,
                        new AgateRequestValues(),
                        "200",
                        "application/json"
                );

        assertEquals(
                "GET:/example",
                interaction.getIdentity()
        );

        assertNotNull(
                interaction.getRequest()
        );

        assertEquals(
                "GET",
                interaction
                        .getRequest()
                        .getMethod()
        );

        assertEquals(
                "/example",
                interaction
                        .getRequest()
                        .getPath()
        );

        assertNotNull(
                interaction.getExpectedResponse()
        );

        assertEquals(
                "200",
                interaction
                        .getExpectedResponse()
                        .getResolvedStatusCode()
        );

        assertEquals(
                "application/json",
                interaction
                        .getExpectedResponse()
                        .getContent()
                        .getMediaType()
        );

        assertNotNull(
                interaction
                        .getExpectedResponse()
                        .getContent()
                        .getExample()
        );
    }


    @Test
    void shouldBuildInteractionWithPathAndQuery() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v1.yaml",
                        "GET",
                        "/users/{id}"
                );

        AgateRequestValues values =
                new AgateRequestValues();

        values.putPath(
                "id",
                42
        );

        values.putQuery(
                "details",
                true
        );

        AgateTestInteractionModel interaction =
                interactionBuilder.build(
                        operation,
                        values,
                        "200"
                );

        assertEquals(
                "/users/42",
                interaction
                        .getRequest()
                        .getPath()
        );

        assertEquals(
                List.of("true"),
                interaction
                        .getRequest()
                        .getQueryParameters()
                        .get("details")
        );

        assertEquals(
                "200",
                interaction
                        .getExpectedResponse()
                        .getResolvedStatusCode()
        );

        assertNotNull(
                interaction
                        .getExpectedResponse()
                        .getContent()
                        .getSchema()
        );
    }


    @Test
    void shouldBuildReadersUrlInteraction() {

        AgateOperationModel operation =
                readersUrlOperation();

        AgateRequestValues values =
                new AgateRequestValues();

        values.putQuery(
                "groups",
                List.of(
                        "Gruppe-1",
                        "Gruppe-2"
                )
        );

        AgateTestInteractionModel interaction =
                interactionBuilder.build(
                        operation,
                        values,
                        "200"
                );

        assertEquals(
                "GET:/readersUrl",
                interaction.getIdentity()
        );

        assertEquals(
                "/readersUrl",
                interaction
                        .getRequest()
                        .getPath()
        );

        assertEquals(
                List.of(
                        "Gruppe-1",
                        "Gruppe-2"
                ),
                interaction
                        .getRequest()
                        .getQueryParameters()
                        .get("groups")
        );

        assertEquals(
                "200",
                interaction
                        .getExpectedResponse()
                        .getResolvedStatusCode()
        );

        assertEquals(
                "application/json",
                interaction
                        .getExpectedResponse()
                        .getContent()
                        .getMediaType()
        );
    }


    @Test
    void shouldBuildInteractionWithoutResponseContent() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v1.yaml",
                        "GET",
                        "/users/{id}"
                );

        AgateRequestValues values =
                new AgateRequestValues();

        values.putPath(
                "id",
                42
        );

        AgateTestInteractionModel interaction =
                interactionBuilder.build(
                        operation,
                        values,
                        "404"
                );

        assertEquals(
                "404",
                interaction
                        .getExpectedResponse()
                        .getResolvedStatusCode()
        );

        assertNull(
                interaction
                        .getExpectedResponse()
                        .getContent()
        );
    }


    private AgateOperationModel readersUrlOperation() {

        AgateOperationModel operation =
                new AgateOperationModel();

        operation.setIdentity(
                "GET:/readersUrl"
        );

        operation.setMethod(
                "GET"
        );

        operation.setPath(
                "/readersUrl"
        );

        AgateRequestModel request =
                new AgateRequestModel();

        request.setMethod(
                "GET"
        );

        request.setPath(
                "/readersUrl"
        );

        AgateRequestParameterModel groups =
                new AgateRequestParameterModel();

        groups.setName(
                "groups"
        );

        groups.setLocation(
                "query"
        );

        groups.setRequired(
                false
        );

        groups.setStyle(
                "form"
        );

        groups.setExplode(
                true
        );

        request.addQueryParameter(
                groups
        );

        operation.setRequest(
                request
        );


        AgateResponseModel response200 =
                new AgateResponseModel();

        response200.setStatusCode(
                "200"
        );

        response200.setDescription(
                "Available readers"
        );

        AgateResponseContentModel content =
                new AgateResponseContentModel();

        content.setMediaType(
                "application/json"
        );

        response200.addContent(
                content
        );

        operation.addResponse(
                response200
        );

        return operation;
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
                findEndpoint(
                        model,
                        method,
                        path
                );

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


    private AgateEndpoint findEndpoint(
            AgateOpenApiModel model,
            String method,
            String path) {

        return model
                .getEndpoints()
                .stream()
                .filter(endpoint ->
                        method.equals(
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
                                new AssertionError(
                                        "Endpoint not found: "
                                                + method
                                                + " "
                                                + path
                                )
                );
    }
}