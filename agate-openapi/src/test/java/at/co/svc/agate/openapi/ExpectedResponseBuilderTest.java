package at.co.svc.agate.openapi;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.parser.AgateOpenApiParser;
import at.co.svc.agate.openapi.phase1.AgateExpectedResponseBuilder;
import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateExpectedResponseModel;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpectedResponseBuilderTest {

    private final AgateOpenApiParser parser =
            new AgateOpenApiParser();

    private final AgateOperationModelBuilder operationBuilder =
            new AgateOperationModelBuilder();

    private final AgateExpectedResponseBuilder responseBuilder =
            new AgateExpectedResponseBuilder();


    @Test
    void shouldBuildResponse200WithExample() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v11-content-examples.yaml",
                        "GET",
                        "/example"
                );

        AgateExpectedResponseModel response =
                responseBuilder.build(
                        operation,
                        "200"
                );

        assertEquals(
                "200",
                response.getRequestedStatusCode()
        );

        assertEquals(
                "200",
                response.getResolvedStatusCode()
        );

        assertFalse(
                response.isDefaultResponse()
        );

        assertEquals(
                "Success",
                response.getDescription()
        );

        assertNotNull(
                response.getContent()
        );

        assertEquals(
                "application/json",
                response
                        .getContent()
                        .getMediaType()
        );

        assertNotNull(
                response
                        .getContent()
                        .getExample()
        );

        assertNotNull(
                response
                        .getContent()
                        .getSchema()
        );

        assertEquals(
                "object",
                response
                        .getContent()
                        .getSchema()
                        .getType()
        );
    }


    @Test
    void shouldBuildResponse400WithNamedExamples() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v11-content-examples.yaml",
                        "GET",
                        "/example"
                );

        AgateExpectedResponseModel response =
                responseBuilder.build(
                        operation,
                        "400",
                        "application/json"
                );

        assertEquals(
                "400",
                response.getResolvedStatusCode()
        );

        assertNotNull(
                response.getContent()
        );

        assertEquals(
                2,
                response
                        .getContent()
                        .getExamples()
                        .size()
        );

        assertTrue(
                response
                        .getContent()
                        .getExamples()
                        .containsKey("invalidId")
        );

        assertTrue(
                response
                        .getContent()
                        .getExamples()
                        .containsKey("missingId")
        );
    }


    @Test
    void shouldKeepResponseSchemaBoundToStatusCode() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v11-content-examples.yaml",
                        "GET",
                        "/example"
                );

        AgateExpectedResponseModel response200 =
                responseBuilder.build(
                        operation,
                        "200"
                );

        AgateExpectedResponseModel response400 =
                responseBuilder.build(
                        operation,
                        "400"
                );

        assertNotNull(
                response200
                        .getContent()
                        .getSchema()
                        .getProperties()
                        .get("id")
        );

        assertNotNull(
                response200
                        .getContent()
                        .getSchema()
                        .getProperties()
                        .get("name")
        );

        assertNull(
                response200
                        .getContent()
                        .getSchema()
                        .getProperties()
                        .get("code")
        );


        assertNotNull(
                response400
                        .getContent()
                        .getSchema()
                        .getProperties()
                        .get("code")
        );

        assertNotNull(
                response400
                        .getContent()
                        .getSchema()
                        .getProperties()
                        .get("text")
        );

        assertNull(
                response400
                        .getContent()
                        .getSchema()
                        .getProperties()
                        .get("id")
        );
    }


    @Test
    void shouldSelectRequestedMediaType() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v1.yaml",
                        "GET",
                        "/users/{id}"
                );

        AgateExpectedResponseModel response =
                responseBuilder.build(
                        operation,
                        "200",
                        "application/json"
                );

        assertEquals(
                "application/json",
                response
                        .getContent()
                        .getMediaType()
        );
    }


    @Test
    void shouldRejectUnknownMediaType() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v11-content-examples.yaml",
                        "GET",
                        "/example"
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        responseBuilder.build(
                                operation,
                                "200",
                                "application/xml"
                        )
        );
    }


    @Test
    void shouldRejectUnknownResponseWithoutDefault() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v11-content-examples.yaml",
                        "GET",
                        "/example"
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        responseBuilder.build(
                                operation,
                                "500"
                        )
        );
    }


    @Test
    void shouldUseDefaultResponse() {

        AgateOperationModel operation =
                new AgateOperationModel();

        var defaultResponse =
                new at.co.svc.agate.openapi.phase1.model.AgateResponseModel();

        defaultResponse.setStatusCode(
                "default"
        );

        defaultResponse.setDescription(
                "Unexpected error"
        );

        var content =
                new at.co.svc.agate.openapi.phase1.model.AgateResponseContentModel();

        content.setMediaType(
                "application/json"
        );

        defaultResponse.addContent(
                content
        );

        operation.addResponse(
                defaultResponse
        );

        AgateExpectedResponseModel response =
                responseBuilder.build(
                        operation,
                        "503"
                );

        assertEquals(
                "503",
                response.getRequestedStatusCode()
        );

        assertEquals(
                "default",
                response.getResolvedStatusCode()
        );

        assertTrue(
                response.isDefaultResponse()
        );

        assertEquals(
                "Unexpected error",
                response.getDescription()
        );

        assertEquals(
                "application/json",
                response
                        .getContent()
                        .getMediaType()
        );
    }


    @Test
    void shouldSupportResponseWithoutContent() throws Exception {

        AgateOperationModel operation =
                operation(
                        "test-openapi-v1.yaml",
                        "GET",
                        "/users/{id}"
                );

        AgateExpectedResponseModel response =
                responseBuilder.build(
                        operation,
                        "404"
                );

        assertEquals(
                "404",
                response.getResolvedStatusCode()
        );

        assertEquals(
                "User not found",
                response.getDescription()
        );

        assertNull(
                response.getContent()
        );
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