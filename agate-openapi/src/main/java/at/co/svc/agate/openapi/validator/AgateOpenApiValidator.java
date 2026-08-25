package at.co.svc.agate.openapi.validator;

import at.co.svc.agate.openapi.model.AgateContent;
import at.co.svc.agate.openapi.model.AgateDiagnostic;
import at.co.svc.agate.openapi.model.AgateDiagnosticSeverity;
import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.model.AgateParameter;
import at.co.svc.agate.openapi.model.AgateResponse;
import at.co.svc.agate.openapi.model.AgateSchema;
import at.co.svc.agate.openapi.model.AgateValidationResult;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AgateOpenApiValidator {

    public AgateValidationResult validate(
            AgateOpenApiModel model) {

        AgateValidationResult result =
                new AgateValidationResult();

        if (model == null) {

            result.addError(
                    "OpenAPI model is null"
            );

            finish(result);

            return result;
        }

        validateDiagnostics(
                model,
                result
        );

        List<AgateEndpoint> endpoints =
                model.getEndpoints();

        if (endpoints == null ||
                endpoints.isEmpty()) {

            result.addWarning(
                    "OpenAPI contains no endpoints"
            );

            finish(result);

            return result;
        }

        result.setEndpointCount(
                endpoints.size()
        );

        Set<String> identities =
                new HashSet<>();

        for (AgateEndpoint endpoint :
                endpoints) {

            if (endpoint != null) {

                String identity =
                        endpoint.getIdentity();

                if (!identities.add(identity)) {

                    result.addError(
                            "Duplicate endpoint identity: "
                                    + identity
                    );
                }
            }

            validateEndpoint(
                    endpoint,
                    result
            );
        }

        finish(result);

        return result;
    }


    private void validateDiagnostics(
            AgateOpenApiModel model,
            AgateValidationResult result) {

        if (model.getDiagnostics() == null) {
            return;
        }

        for (AgateDiagnostic diagnostic :
                model.getDiagnostics()) {

            if (diagnostic == null ||
                    diagnostic.getSeverity() == null) {

                continue;
            }

            String message =
                    formatDiagnostic(
                            diagnostic
                    );

            if (diagnostic.getSeverity()
                    == AgateDiagnosticSeverity.ERROR) {

                result.addError(
                        message
                );

                if (diagnostic
                        .getCode()
                        .contains(
                                "UNRESOLVED"
                        )) {

                    result.setUnresolvedRefCount(
                            result.getUnresolvedRefCount()
                                    + 1
                    );
                }

            } else if (diagnostic.getSeverity()
                    == AgateDiagnosticSeverity.UNSUPPORTED) {

                result.addUnsupported(
                        message
                );

            } else {

                result.addWarning(
                        message
                );
            }
        }
    }


    private String formatDiagnostic(
            AgateDiagnostic diagnostic) {

        return String.valueOf(
                diagnostic.getCode()
        )
                + " ["
                + String.valueOf(
                        diagnostic.getLocation()
                )
                + "] "
                + String.valueOf(
                        diagnostic.getMessage()
                );
    }


    private void validateEndpoint(
            AgateEndpoint endpoint,
            AgateValidationResult result) {

        if (endpoint == null) {

            result.addError(
                    "Null endpoint found"
            );

            return;
        }

        String endpointName =
                endpoint.getIdentity();

        if (endpoint.getPath() == null ||
                endpoint.getPath().isBlank()) {

            result.addError(
                    endpointName
                            + ": path is missing"
            );
        }

        if (endpoint.getMethod() == null ||
                endpoint.getMethod().isBlank()) {

            result.addError(
                    endpointName
                            + ": HTTP method is missing"
            );
        }

        validateParameters(
                endpoint,
                result
        );

        if (endpoint.getRequestBody() != null) {

            result.setRequestBodyCount(
                    result.getRequestBodyCount()
                            + 1
            );

            for (AgateContent content :
                    endpoint
                            .getRequestBody()
                            .getContents()) {

                validateContent(
                        endpointName
                                + " requestBody",
                        content,
                        result
                );
            }
        }

        if (endpoint.getResponses() == null ||
                endpoint.getResponses().isEmpty()) {

            result.addWarning(
                    endpointName
                            + ": no responses defined"
            );

            return;
        }

        result.setResponseCount(
                result.getResponseCount()
                        + endpoint
                                .getResponses()
                                .size()
        );

        for (AgateResponse response :
                endpoint.getResponses()) {

            validateResponse(
                    endpointName,
                    response,
                    result
            );
        }
    }


    private void validateParameters(
            AgateEndpoint endpoint,
            AgateValidationResult result) {

        if (endpoint.getParameters() == null) {
            return;
        }

        result.setParameterCount(
                result.getParameterCount()
                        + endpoint
                                .getParameters()
                                .size()
        );

        Set<String> keys =
                new HashSet<>();

        for (AgateParameter parameter :
                endpoint.getParameters()) {

            if (parameter == null) {

                result.addError(
                        endpoint.getIdentity()
                                + ": null parameter"
                );

                continue;
            }

            String key =
                    String.valueOf(
                            parameter.getLocation()
                    )
                            + ":"
                            + String.valueOf(
                                    parameter.getName()
                            );

            if (!keys.add(key)) {

                result.addWarning(
                        endpoint.getIdentity()
                                + ": duplicate parameter "
                                + key
                );
            }

            if (parameter.getName() == null ||
                    parameter.getName().isBlank()) {

                result.addError(
                        endpoint.getIdentity()
                                + ": parameter without name"
                );
            }

            if (parameter.getLocation() == null ||
                    parameter.getLocation().isBlank()) {

                result.addError(
                        endpoint.getIdentity()
                                + ": parameter "
                                + parameter.getName()
                                + " has no location"
                );
            }

            validateSchema(
                    endpoint.getIdentity()
                            + " parameter "
                            + parameter.getName(),
                    parameter.getSchema(),
                    result,
                    new HashSet<>()
            );
        }
    }


    private void validateResponse(
            String endpointName,
            AgateResponse response,
            AgateValidationResult result) {

        if (response == null) {

            result.addError(
                    endpointName
                            + ": null response"
            );

            return;
        }

        if (response.getStatusCode() == null ||
                response.getStatusCode().isBlank()) {

            result.addError(
                    endpointName
                            + ": response without status code"
            );
        }

        for (AgateContent content :
                response.getContents()) {

            validateContent(
                    endpointName
                            + " response "
                            + response.getStatusCode(),
                    content,
                    result
            );
        }
    }


    private void validateContent(
            String location,
            AgateContent content,
            AgateValidationResult result) {

        if (content == null) {

            result.addError(
                    location
                            + ": null content"
            );

            return;
        }

        if (content.getMediaType() == null ||
                content.getMediaType().isBlank()) {

            result.addWarning(
                    location
                            + ": media type missing"
            );
        }

        validateSchema(
                location,
                content.getSchema(),
                result,
                new HashSet<>()
        );
    }


    private void validateSchema(
            String location,
            AgateSchema schema,
            AgateValidationResult result,
            Set<String> visited) {

        if (schema == null) {
            return;
        }

        String identity =
                schemaIdentity(
                        schema
                );

        if (identity != null &&
                !visited.add(identity)) {

            return;
        }

        if (schema.getSourceRef() != null &&
                isUnresolvedReference(schema)) {

            result.setUnresolvedRefCount(
                    result.getUnresolvedRefCount()
                            + 1
            );

            result.addError(
                    location
                            + ": unresolved ref "
                            + schema.getSourceRef()
            );

            return;
        }

        if (schema.getItems() != null) {

            validateSchema(
                    location + " items",
                    schema.getItems(),
                    result,
                    new HashSet<>(visited)
            );
        }

        for (Map.Entry<String, AgateSchema> entry :
                schema
                        .getProperties()
                        .entrySet()) {

            validateSchema(
                    location
                            + "."
                            + entry.getKey(),
                    entry.getValue(),
                    result,
                    new HashSet<>(visited)
            );
        }

        if (schema.getAdditionalProperties() != null) {

            validateSchema(
                    location
                            + " additionalProperties",
                    schema.getAdditionalProperties(),
                    result,
                    new HashSet<>(visited)
            );
        }

        validateSchemaList(
                location + " allOf",
                schema.getAllOf(),
                result,
                visited
        );

        validateSchemaList(
                location + " oneOf",
                schema.getOneOf(),
                result,
                visited
        );

        validateSchemaList(
                location + " anyOf",
                schema.getAnyOf(),
                result,
                visited
        );
    }


    private void validateSchemaList(
            String location,
            List<AgateSchema> schemas,
            AgateValidationResult result,
            Set<String> visited) {

        if (schemas == null) {
            return;
        }

        for (AgateSchema schema :
                schemas) {

            validateSchema(
                    location,
                    schema,
                    result,
                    new HashSet<>(visited)
            );
        }
    }


    private boolean isUnresolvedReference(
            AgateSchema schema) {

        if (schema.getSourceRef() == null) {
            return false;
        }

        if (schema.getType() != null) {
            return false;
        }

        if (!schema.getProperties().isEmpty()) {
            return false;
        }

        if (schema.getItems() != null) {
            return false;
        }

        if (!schema.getAllOf().isEmpty()) {
            return false;
        }

        if (!schema.getOneOf().isEmpty()) {
            return false;
        }

        if (!schema.getAnyOf().isEmpty()) {
            return false;
        }

        return schema.getResolvedRef() == null;
    }


    private String schemaIdentity(
            AgateSchema schema) {

        if (schema.getSourceDocument() == null &&
                schema.getSourceRef() == null) {

            return null;
        }

        return String.valueOf(
                schema.getSourceDocument()
        )
                + "|"
                + String.valueOf(
                        schema.getSourceRef()
                );
    }


    private void finish(
            AgateValidationResult result) {

        result.setWarningCount(
                result
                        .getWarnings()
                        .size()
        );

        result.setUnsupportedCount(
                result
                        .getUnsupported()
                        .size()
        );

        result.setValid(
                result
                        .getErrors()
                        .isEmpty()
        );
    }
}