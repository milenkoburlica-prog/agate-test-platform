package at.co.svc.agate.openapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.model.AgateResponse;
import at.co.svc.agate.openapi.model.AgateValidationResult;
import at.co.svc.agate.openapi.output.AgateModelJsonWriter;
import at.co.svc.agate.openapi.parser.AgateOpenApiParser;
import at.co.svc.agate.openapi.validator.AgateOpenApiValidator;

public class Phase0RegressionTest {

    private final AgateOpenApiParser parser =
            new AgateOpenApiParser();

    private final AgateOpenApiValidator validator =
            new AgateOpenApiValidator();


    @Test
    void shouldParseBasicOpenApi() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v1.yaml"
                );

        assertEquals(
                2,
                model.getEndpoints().size()
        );

        assertEndpoint(
                model,
                "GET",
                "/users/{id}"
        );

        assertEndpoint(
                model,
                "POST",
                "/users"
        );

        assertValid(
                model
        );
    }


    @Test
    void shouldExtractSchemaConstraints() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v2.yaml"
                );

        AgateEndpoint endpoint =
                findEndpoint(
                        model,
                        "POST",
                        "/users"
                );

        assertNotNull(
                endpoint.getRequestBody()
        );

        assertFalse(
                endpoint
                        .getRequestBody()
                        .getContents()
                        .isEmpty()
        );

        var schema =
                endpoint
                        .getRequestBody()
                        .getContents()
                        .get(0)
                        .getSchema();

        assertNotNull(
                schema
        );

        assertTrue(
                schema
                        .getRequired()
                        .contains("username")
        );

        assertTrue(
                schema
                        .getRequired()
                        .contains("role")
        );

        assertEquals(
                3,
                schema
                        .getProperties()
                        .get("username")
                        .getMinLength()
        );

        assertEquals(
                50,
                schema
                        .getProperties()
                        .get("username")
                        .getMaxLength()
        );

        assertEquals(
                List.of(
                        "ADMIN",
                        "USER",
                        "GUEST"
                ),
                schema
                        .getProperties()
                        .get("role")
                        .getEnumValues()
        );

        assertValid(
                model
        );
    }


    @Test
    void shouldExtractSchemaComposition() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v3.yaml"
                );

        AgateEndpoint employee =
                findEndpoint(
                        model,
                        "POST",
                        "/employees"
                );

        var employeeSchema =
                employee
                        .getRequestBody()
                        .getContents()
                        .get(0)
                        .getSchema();

        assertEquals(
                3,
                employeeSchema
                        .getAllOf()
                        .size()
        );

        AgateEndpoint payment =
                findEndpoint(
                        model,
                        "POST",
                        "/payment"
                );

        var paymentSchema =
                payment
                        .getRequestBody()
                        .getContents()
                        .get(0)
                        .getSchema();

        assertEquals(
                2,
                paymentSchema
                        .getProperties()
                        .get("payment")
                        .getOneOf()
                        .size()
        );

        assertEquals(
                2,
                paymentSchema
                        .getProperties()
                        .get("fallback")
                        .getAnyOf()
                        .size()
        );

        assertValid(
                model
        );
    }


    @Test
    void shouldResolveExternalSchemas() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v4.yaml"
                );

        AgateEndpoint endpoint =
                findEndpoint(
                        model,
                        "GET",
                        "/companies/{id}"
                );

        var responseSchema =
                endpoint
                        .getResponses()
                        .stream()
                        .filter(response ->
                                "200".equals(
                                        response.getStatusCode()
                                )
                        )
                        .findFirst()
                        .orElseThrow()
                        .getContents()
                        .get(0)
                        .getSchema();

        assertEquals(
                "./common-v1.yaml#/components/schemas/Company",
                responseSchema.getSourceRef()
        );

        assertNotNull(
                responseSchema
                        .getProperties()
                        .get("address")
        );

        assertEquals(
                "#/components/schemas/Address",
                responseSchema
                        .getProperties()
                        .get("address")
                        .getSourceRef()
        );

        assertValid(
                model
        );
    }


    @Test
    void shouldResolveExternalApiParts() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v5.yaml"
                );

        AgateEndpoint get =
                findEndpoint(
                        model,
                        "GET",
                        "/companies/{id}"
                );

        assertEquals(
                1,
                get.getParameters().size()
        );

        assertEquals(
                "id",
                get
                        .getParameters()
                        .get(0)
                        .getName()
        );

        assertEquals(
                "path",
                get
                        .getParameters()
                        .get(0)
                        .getLocation()
        );

        AgateEndpoint post =
                findEndpoint(
                        model,
                        "POST",
                        "/companies"
                );

        assertNotNull(
                post.getRequestBody()
        );

        assertTrue(
                post
                        .getRequestBody()
                        .isRequired()
        );

        assertValid(
                model
        );
    }


    @Test
    void shouldResolveMultiLevelRefsAndCycles() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v6.yaml"
                );

        AgateEndpoint endpoint =
                findEndpoint(
                        model,
                        "GET",
                        "/employees"
                );

        var rootSchema =
                endpoint
                        .getResponses()
                        .get(0)
                        .getContents()
                        .get(0)
                        .getSchema();

        assertEquals(
                "array",
                rootSchema.getType()
        );

        var employee =
                rootSchema.getItems();

        assertEquals(
                "./common-a.yaml#/components/schemas/Employee",
                employee.getSourceRef()
        );

        var address =
                employee
                        .getProperties()
                        .get("address");

        assertEquals(
                "./common-b.yaml#/components/schemas/Address",
                address.getSourceRef()
        );

        var colleagues =
                employee
                        .getProperties()
                        .get("colleagues");

        assertEquals(
                "array",
                colleagues.getType()
        );

        assertNotNull(
                colleagues.getItems()
        );

        assertEquals(
                "#/components/schemas/Employee",
                colleagues
                        .getItems()
                        .getSourceRef()
        );

        assertTrue(
                colleagues
                        .getItems()
                        .getProperties()
                        .isEmpty()
        );

        assertValid(
                model
        );
    }


    @Test
    void shouldExtractMetadata() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v7.yaml"
                );

        assertEquals(
                2,
                model.getServers().size()
        );

        assertEquals(
                2,
                model.getTags().size()
        );

        AgateEndpoint legacy =
                findEndpoint(
                        model,
                        "GET",
                        "/companies/legacy"
                );

        assertTrue(
                legacy.isDeprecated()
        );

        assertEquals(
                List.of(
                        "companies",
                        "administration"
                ),
                legacy.getTags()
        );

        assertFalse(
                legacy.getSecurity().isEmpty()
        );

        assertTrue(
                legacy
                        .getSecurity()
                        .get(0)
                        .containsKey("oauth2")
        );

        assertValid(
                model
        );
    }


    @Test
    void shouldResolveExternalPathItems() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v8.yaml"
                );

        assertEquals(
                5,
                model.getEndpoints().size()
        );

        assertEndpoint(
                model,
                "GET",
                "/alive"
        );

        assertEndpoint(
                model,
                "HEAD",
                "/alive"
        );

        assertEndpoint(
                model,
                "GET",
                "/v1/alive"
        );

        assertEndpoint(
                model,
                "HEAD",
                "/v1/alive"
        );

        assertEndpoint(
                model,
                "GET",
                "/users"
        );

        assertValid(
                model
        );
    }


    @Test
    void shouldCreateDeterministicEndpointIdentity() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v8.yaml"
                );

        AgateEndpoint get =
                findEndpoint(
                        model,
                        "GET",
                        "/alive"
                );

        AgateEndpoint head =
                findEndpoint(
                        model,
                        "HEAD",
                        "/alive"
                );

        assertEquals(
                "GET:/alive",
                get.getIdentity()
        );

        assertEquals(
                "HEAD:/alive",
                head.getIdentity()
        );
    }


    @Test
    void shouldProduceDeterministicModel() throws Exception {

        AgateOpenApiModel first =
                parse(
                        "src/test/resources/test-openapi-v6.yaml"
                );

        AgateOpenApiModel second =
                parse(
                        "src/test/resources/test-openapi-v6.yaml"
                );

        AgateModelJsonWriter writer =
                new AgateModelJsonWriter();

        String firstJson =
                writer.write(
                        first
                );

        String secondJson =
                writer.write(
                        second
                );

        assertEquals(
                firstJson,
                secondJson
        );
    }

    @Test
    void shouldReportUnsupportedCallback() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v9-unsupported-callback.yaml"
                );

        AgateValidationResult validation =
                validator.validate(
                        model
                );

        assertTrue(
                validation.isValid()
        );

        assertEquals(
                0,
                validation.getUnresolvedRefCount()
        );

        assertEquals(
                1,
                validation.getUnsupportedCount()
        );

        assertEquals(
                0,
                validation.getWarningCount()
        );

        assertTrue(
                validation
                        .getUnsupported()
                        .stream()
                        .anyMatch(value ->
                                value.contains("CALLBACKS")
                        )
        );
    }


    @Test
    void shouldReportBrokenReference() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v10-broken-ref.yaml"
                );

        AgateValidationResult validation =
                validator.validate(
                        model
                );

        assertFalse(
                validation.isValid()
        );

        assertEquals(
                1,
                validation.getUnresolvedRefCount()
        );

        assertTrue(
                validation
                        .getErrors()
                        .stream()
                        .anyMatch(value ->
                                value.contains(
                                        "DoesNotExist"
                                )
                        )
        );
    }
    
    @Test
    void shouldExtractResponseContentExamples() throws Exception {

        AgateOpenApiModel model =
                parse(
                        "src/test/resources/test-openapi-v11-content-examples.yaml"
                );

        AgateEndpoint endpoint =
                findEndpoint(
                        model,
                        "GET",
                        "/example"
                );

        AgateResponse response200 =
                endpoint
                        .getResponses()
                        .stream()
                        .filter(response ->
                                "200".equals(
                                        response.getStatusCode()
                                )
                        )
                        .findFirst()
                        .orElseThrow();

        var content200 =
                response200
                        .getContents()
                        .get(0);

        assertNotNull(
                content200.getExample()
        );

        assertTrue(
                content200
                        .getExample()
                        .toString()
                        .contains("42")
        );


        AgateResponse response400 =
                endpoint
                        .getResponses()
                        .stream()
                        .filter(response ->
                                "400".equals(
                                        response.getStatusCode()
                                )
                        )
                        .findFirst()
                        .orElseThrow();

        var content400 =
                response400
                        .getContents()
                        .get(0);

        assertEquals(
                2,
                content400
                        .getExamples()
                        .size()
        );

        assertTrue(
                content400
                        .getExamples()
                        .containsKey("invalidId")
        );

        assertTrue(
                content400
                        .getExamples()
                        .containsKey("missingId")
        );

        assertValid(
                model
        );
    }
    
    
    private AgateOpenApiModel parse(
            String resourceName) throws Exception {

        String normalizedResourceName =
                resourceName;

        String prefix =
                "src/test/resources/";

        if (normalizedResourceName.startsWith(prefix)) {

            normalizedResourceName =
                    normalizedResourceName.substring(
                            prefix.length()
                    );
        }

        var resource =
                Thread
                        .currentThread()
                        .getContextClassLoader()
                        .getResource(
                                normalizedResourceName
                        );

        if (resource == null) {

            throw new IllegalStateException(
                    "Test resource not found: "
                            + normalizedResourceName
            );
        }

        return parser.parse(
                Path
                        .of(resource.toURI())
                        .toString()
        );
    }


    private void assertValid(
            AgateOpenApiModel model) {

        AgateValidationResult validation =
                validator.validate(
                        model
                );

        assertTrue(
                validation.isValid(),
                () ->
                        "Validation errors: "
                                + validation.getErrors()
        );

        assertEquals(
                0,
                validation.getUnresolvedRefCount()
        );

        assertEquals(
                0,
                validation.getWarningCount()
        );

        assertEquals(
                0,
                validation.getUnsupportedCount()
        );
    }


    private void assertEndpoint(
            AgateOpenApiModel model,
            String method,
            String path) {

        assertNotNull(
                findEndpoint(
                        model,
                        method,
                        path
                )
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