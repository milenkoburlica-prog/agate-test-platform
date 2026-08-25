package at.co.svc.agate.openapi.resolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class OpenApiResolver {

    private final SchemaResolver schemaResolver =
            new SchemaResolver();

    private final ParameterResolver parameterResolver =
            new ParameterResolver();

    private final RequestBodyResolver requestBodyResolver =
            new RequestBodyResolver();

    private final ResponseResolver responseResolver =
            new ResponseResolver();


    public ResolvedSchema resolveSchema(
            OpenAPI openApi,
            String sourceDocument,
            Schema<?> schema) {

        return schemaResolver.resolve(
                openApi,
                sourceDocument,
                schema
        );
    }


    public ResolvedParameter resolveParameter(
            OpenAPI openApi,
            String sourceDocument,
            Parameter parameter) {

        return parameterResolver.resolve(
                openApi,
                sourceDocument,
                parameter
        );
    }


    public ResolvedRequestBody resolveRequestBody(
            OpenAPI openApi,
            String sourceDocument,
            RequestBody requestBody) {

        return requestBodyResolver.resolve(
                openApi,
                sourceDocument,
                requestBody
        );
    }


    public ResolvedResponse resolveResponse(
            OpenAPI openApi,
            String sourceDocument,
            ApiResponse response) {

        return responseResolver.resolve(
                openApi,
                sourceDocument,
                response
        );
    }
}