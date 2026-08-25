package at.co.svc.agate.openapi;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import at.co.svc.agate.openapi.parser.AgateOpenApiParser;

import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Phase1RegressionTest {

    private final AgateOpenApiParser parser =
            new AgateOpenApiParser();

    private final AgateOperationModelBuilder builder =
            new AgateOperationModelBuilder();


    @Test
    void shouldBuildRequestModel() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "test-openapi-v2.yaml"
                );

        AgateEndpoint endpoint =
                findEndpoint(
                        model,
                        "POST",
                        "/users"
                );

        AgateOperationModel operation =
                builder.build(
                        endpoint
                );

        assertEquals(
                "POST:/users",
                operation.getIdentity()
        );

        assertEquals(
                "POST",
                operation
                        .getRequest()
                        .getMethod()
        );

        assertEquals(
                "/users",
                operation
                        .getRequest()
                        .getPath()
        );

        assertNotNull(
                operation
                        .getRequest()
                        .getBody()
        );

        assertFalse(
                operation
                        .getRequest()
                        .getBody()
                        .getContents()
                        .isEmpty()
        );
    }


    @Test
    void shouldSeparateParameterLocations() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "test-openapi-v1.yaml"
                );

        AgateEndpoint endpoint =
                findEndpoint(
                        model,
                        "GET",
                        "/users/{id}"
                );

        AgateOperationModel operation =
                builder.build(
                        endpoint
                );

        assertEquals(
                1,
                operation
                        .getRequest()
                        .getPathParameters()
                        .size()
        );

        assertEquals(
                1,
                operation
                        .getRequest()
                        .getQueryParameters()
                        .size()
        );

        assertEquals(
                0,
                operation
                        .getRequest()
                        .getHeaderParameters()
                        .size()
        );

        assertEquals(
                0,
                operation
                        .getRequest()
                        .getCookieParameters()
                        .size()
        );

        assertNull(
                operation
                        .getRequest()
                        .getBody()
        );
    }


    @Test
    void shouldKeepParameterSerializationMetadata() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "test-openapi-v1.yaml"
                );

        AgateEndpoint endpoint =
                findEndpoint(
                        model,
                        "GET",
                        "/users/{id}"
                );

        AgateOperationModel operation =
                builder.build(
                        endpoint
                );

        AgateRequestParameterModel parameter =
                operation
                        .getRequest()
                        .getQueryParameters()
                        .get(0);

        assertEquals(
                "details",
                parameter.getName()
        );

        assertEquals(
                "query",
                parameter.getLocation()
        );

        assertEquals(
                "boolean",
                parameter
                        .getSchema()
                        .getType()
        );
    }


    @Test
    void shouldKeepResponsesSeparatedByStatusCode() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "test-openapi-v11-content-examples.yaml"
                );

        AgateEndpoint endpoint =
                findEndpoint(
                        model,
                        "GET",
                        "/example"
                );

        AgateOperationModel operation =
                builder.build(
                        endpoint
                );

        assertEquals(
                2,
                operation
                        .getResponses()
                        .size()
        );

        var response200 =
                operation
                        .getResponses()
                        .stream()
                        .filter(response ->
                                "200".equals(
                                        response.getStatusCode()
                                )
                        )
                        .findFirst()
                        .orElseThrow();

        var response400 =
                operation
                        .getResponses()
                        .stream()
                        .filter(response ->
                                "400".equals(
                                        response.getStatusCode()
                                )
                        )
                        .findFirst()
                        .orElseThrow();

        assertNotNull(
                response200
                        .getContents()
                        .get(0)
                        .getExample()
        );

        assertEquals(
                2,
                response400
                        .getContents()
                        .get(0)
                        .getExamples()
                        .size()
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