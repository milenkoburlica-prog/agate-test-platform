package at.co.svc.agate.openapi.extractor;

import at.co.svc.agate.openapi.model.AgateEndpoint;
import at.co.svc.agate.openapi.resolver.PathItemResolver;
import at.co.svc.agate.openapi.resolver.ResolvedPathItem;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EndpointExtractor {

    private final ParameterExtractor parameterExtractor =
            new ParameterExtractor();

    private final RequestBodyExtractor requestBodyExtractor =
            new RequestBodyExtractor();

    private final ResponseExtractor responseExtractor =
            new ResponseExtractor();

    private final PathItemResolver pathItemResolver =
            new PathItemResolver();


    public List<AgateEndpoint> extract(
            OpenAPI openApi,
            String sourceDocument) {

        List<AgateEndpoint> result =
                new ArrayList<>();

        if (openApi == null ||
                openApi.getPaths() == null) {

            return result;
        }

        openApi.getPaths().forEach(
                (path, pathItem) ->
                        extractPath(
                                openApi,
                                sourceDocument,
                                path,
                                pathItem,
                                result
                        )
        );

        return result;
    }


    private void extractPath(
            OpenAPI openApi,
            String sourceDocument,
            String path,
            PathItem originalPathItem,
            List<AgateEndpoint> result) {

        if (originalPathItem == null) {
            return;
        }

        ResolvedPathItem resolution =
                pathItemResolver.resolve(
                        openApi,
                        sourceDocument,
                        originalPathItem
                );

        if (resolution == null ||
                resolution.getPathItem() == null) {

            return;
        }

        OpenAPI effectiveOpenApi =
                resolution.getOpenApi();

        String effectiveSourceDocument =
                resolution.getSourceDocument();

        PathItem pathItem =
                resolution.getPathItem();

        Map<PathItem.HttpMethod, Operation> operations =
                pathItem.readOperationsMap();

        if (operations == null ||
                operations.isEmpty()) {

            return;
        }

        operations.forEach(
                (method, operation) -> {

                    if (operation == null) {
                        return;
                    }

                    AgateEndpoint endpoint =
                            new AgateEndpoint();

                    endpoint.setPath(
                            path
                    );

                    endpoint.setMethod(
                            method.name()
                    );

                    endpoint.setOperationId(
                            operation.getOperationId()
                    );

                    endpoint.setSummary(
                            operation.getSummary()
                    );

                    endpoint.setDescription(
                            operation.getDescription()
                    );

                    endpoint.setDeprecated(
                            Boolean.TRUE.equals(
                                    operation.getDeprecated()
                            )
                    );

                    endpoint.setTags(
                            operation.getTags()
                    );

                    endpoint.setSecurity(
                            extractSecurity(
                                    effectiveOpenApi,
                                    operation
                            )
                    );

                    endpoint.setParameters(
                            parameterExtractor.extract(
                                    effectiveOpenApi,
                                    effectiveSourceDocument,
                                    pathItem,
                                    operation
                            )
                    );

                    endpoint.setRequestBody(
                            requestBodyExtractor.extract(
                                    effectiveOpenApi,
                                    effectiveSourceDocument,
                                    operation.getRequestBody()
                            )
                    );

                    endpoint.setResponses(
                            responseExtractor.extract(
                                    effectiveOpenApi,
                                    effectiveSourceDocument,
                                    operation.getResponses()
                            )
                    );

                    result.add(
                            endpoint
                    );
                }
        );
    }


    private List<Map<String, List<String>>> extractSecurity(
            OpenAPI openApi,
            Operation operation) {

        List<SecurityRequirement> requirements =
                operation.getSecurity();

        if (requirements == null) {

            requirements =
                    openApi.getSecurity();
        }

        List<Map<String, List<String>>> result =
                new ArrayList<>();

        if (requirements == null) {
            return result;
        }

        for (SecurityRequirement requirement :
                requirements) {

            if (requirement == null) {
                continue;
            }

            Map<String, List<String>> security =
                    new LinkedHashMap<>();

            requirement.forEach(
                    (name, scopes) -> {

                        security.put(
                                name,
                                scopes != null
                                        ? new ArrayList<>(scopes)
                                        : new ArrayList<>()
                        );
                    }
            );

            result.add(
                    security
            );
        }

        return result;
    }
}