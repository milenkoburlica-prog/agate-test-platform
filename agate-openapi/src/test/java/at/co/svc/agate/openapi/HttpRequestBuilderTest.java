package at.co.svc.agate.openapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.parser.AgateOpenApiParser;
import at.co.svc.agate.openapi.phase1.AgateOperationModelBuilder;
import at.co.svc.agate.openapi.phase1.AgateHttpRequestBuilder;
import at.co.svc.agate.openapi.phase1.model.AgateHttpRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestValues;

public class HttpRequestBuilderTest {

    private final AgateOpenApiParser parser =
            new AgateOpenApiParser();

    private final AgateOperationModelBuilder operationBuilder =
            new AgateOperationModelBuilder();

    private final AgateHttpRequestBuilder requestBuilder =
            new AgateHttpRequestBuilder();


    @Test
    void shouldBuildPathAndQueryRequest() throws Exception {

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
                operationBuilder.build(
                        endpoint
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

        AgateHttpRequestModel request =
                requestBuilder.build(
                        operation,
                        values
                );

        assertEquals(
                "GET",
                request.getMethod()
        );

        assertEquals(
                "/users/42",
                request.getPath()
        );

        assertEquals(
                List.of("true"),
                request
                        .getQueryParameters()
                        .get("details")
        );

        assertTrueEmpty(
                request.getHeaders()
        );

        assertTrueEmpty(
                request.getCookies()
        );

        assertNull(
                request.getBody()
        );
    }


    @Test
    void shouldBuildExplodedQueryArray() {

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

        AgateHttpRequestModel request =
                requestBuilder.build(
                        operation,
                        values
                );

        assertEquals(
                List.of(
                        "Gruppe-1",
                        "Gruppe-2"
                ),
                request
                        .getQueryParameters()
                        .get("groups")
        );
    }


    @Test
    void shouldAllowMissingOptionalQueryParameter() {

        AgateOperationModel operation =
                readersUrlOperation();

        AgateHttpRequestModel request =
                requestBuilder.build(
                        operation,
                        new AgateRequestValues()
                );

        assertTrueEmpty(
                request.getQueryParameters()
        );
    }


    @Test
    void shouldRejectMissingRequiredPathParameter() throws Exception {

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
                operationBuilder.build(
                        endpoint
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        requestBuilder.build(
                                operation,
                                new AgateRequestValues()
                        )
        );
    }


    @Test
    void shouldBuildJsonRequestBody() throws Exception {

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
                operationBuilder.build(
                        endpoint
                );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "username",
                "tester"
        );

        body.put(
                "role",
                "USER"
        );

        body.put(
                "age",
                30
        );

        AgateRequestValues values =
                new AgateRequestValues();

        values.setBodyMediaType(
                "application/json"
        );

        values.setBody(
                body
        );

        AgateHttpRequestModel request =
                requestBuilder.build(
                        operation,
                        values
                );

        assertNotNull(
                request.getBody()
        );

        assertEquals(
                "application/json",
                request
                        .getBody()
                        .getMediaType()
        );

        assertEquals(
                body,
                request
                        .getBody()
                        .getValue()
        );
    }


    @Test
    void shouldRejectMissingRequiredBody() throws Exception {

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
                operationBuilder.build(
                        endpoint
                );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        requestBuilder.build(
                                operation,
                                new AgateRequestValues()
                        )
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

        var request =
                new at.co.svc.agate.openapi.phase1.model.AgateRequestModel();

        request.setMethod(
                "GET"
        );

        request.setPath(
                "/readersUrl"
        );

        var groups =
                new at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel();

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

        return operation;
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


    private void assertTrueEmpty(
            Map<?, ?> value) {

        assertEquals(
                0,
                value.size()
        );
    }
}