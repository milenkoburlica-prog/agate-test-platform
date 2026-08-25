package at.co.svc.agate.openapi.inspector;

import at.co.svc.agate.openapi.model.AgateDiagnostic;
import at.co.svc.agate.openapi.model.AgateDiagnosticSeverity;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.resolver.PathItemResolver;
import at.co.svc.agate.openapi.resolver.ResolvedPathItem;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.lang.reflect.Method;
import java.util.Map;

public class OpenApiSourceInspector {

    private final PathItemResolver pathItemResolver =
            new PathItemResolver();


    public void inspect(
            OpenAPI openApi,
            String sourceDocument,
            AgateOpenApiModel model) {

        if (openApi == null ||
                model == null) {

            return;
        }

        inspectWebhooks(
                openApi,
                model
        );

        if (openApi.getPaths() == null) {
            return;
        }

        openApi.getPaths().forEach(
                (path, originalPathItem) ->
                        inspectPath(
                                openApi,
                                sourceDocument,
                                path,
                                originalPathItem,
                                model
                        )
        );
    }


    private void inspectPath(
            OpenAPI openApi,
            String sourceDocument,
            String path,
            PathItem originalPathItem,
            AgateOpenApiModel model) {

        if (originalPathItem == null) {

            addError(
                    model,
                    "NULL_PATH_ITEM",
                    path,
                    "PathItem is null"
            );

            return;
        }

        ResolvedPathItem resolution;

        try {

            resolution =
                    pathItemResolver.resolve(
                            openApi,
                            sourceDocument,
                            originalPathItem
                    );

        } catch (Exception e) {

            addError(
                    model,
                    "UNRESOLVED_PATH_ITEM_REF",
                    path,
                    message(
                            originalPathItem.get$ref(),
                            e
                    )
            );

            return;
        }

        if (originalPathItem.get$ref() != null &&
                !originalPathItem.get$ref().isBlank()) {

            if (resolution == null ||
                    resolution.getPathItem() == null ||
                    resolution.getPathItem()
                            == originalPathItem) {

                addError(
                        model,
                        "UNRESOLVED_PATH_ITEM_REF",
                        path,
                        "Unable to resolve "
                                + originalPathItem.get$ref()
                );

                return;
            }
        }

        if (resolution == null ||
                resolution.getPathItem() == null) {

            return;
        }

        PathItem pathItem =
                resolution.getPathItem();

        Map<PathItem.HttpMethod, Operation> operations =
                pathItem.readOperationsMap();

        if (operations == null) {
            return;
        }

        operations.forEach(
                (method, operation) ->
                        inspectOperation(
                                path,
                                method,
                                operation,
                                model
                        )
        );
    }


    private void inspectOperation(
            String path,
            PathItem.HttpMethod method,
            Operation operation,
            AgateOpenApiModel model) {

        if (operation == null) {
            return;
        }

        String location =
                method.name()
                        + ":"
                        + path;

        if (operation.getCallbacks() != null &&
                !operation.getCallbacks().isEmpty()) {

            addUnsupported(
                    model,
                    "CALLBACKS",
                    location,
                    "OpenAPI callbacks are currently not modeled"
            );
        }

        inspectLinks(
                location,
                operation.getResponses(),
                model
        );
    }


    private void inspectLinks(
            String location,
            ApiResponses responses,
            AgateOpenApiModel model) {

        if (responses == null) {
            return;
        }

        responses.forEach(
                (statusCode, response) -> {

                    if (response == null) {
                        return;
                    }

                    if (response.getLinks() != null &&
                            !response
                                    .getLinks()
                                    .isEmpty()) {

                        addUnsupported(
                                model,
                                "RESPONSE_LINKS",
                                location
                                        + " response "
                                        + statusCode,
                                "OpenAPI response links are currently not modeled"
                        );
                    }
                }
        );
    }


    private void inspectWebhooks(
            OpenAPI openApi,
            AgateOpenApiModel model) {

        try {

            Method method =
                    openApi
                            .getClass()
                            .getMethod(
                                    "getWebhooks"
                            );

            Object value =
                    method.invoke(
                            openApi
                    );

            if (value instanceof Map<?, ?> map &&
                    !map.isEmpty()) {

                addUnsupported(
                        model,
                        "WEBHOOKS",
                        "openapi",
                        "OpenAPI webhooks are currently not modeled"
                );
            }

        } catch (NoSuchMethodException e) {

            return;

        } catch (Exception e) {

            addError(
                    model,
                    "WEBHOOK_INSPECTION_ERROR",
                    "openapi",
                    e.getMessage()
            );
        }
    }


    private void addError(
            AgateOpenApiModel model,
            String code,
            String location,
            String message) {

        model.addDiagnostic(
                new AgateDiagnostic(
                        AgateDiagnosticSeverity.ERROR,
                        code,
                        location,
                        message
                )
        );
    }


    private void addUnsupported(
            AgateOpenApiModel model,
            String code,
            String location,
            String message) {

        model.addDiagnostic(
                new AgateDiagnostic(
                        AgateDiagnosticSeverity.UNSUPPORTED,
                        code,
                        location,
                        message
                )
        );
    }


    private String message(
            String ref,
            Exception exception) {

        String result =
                "Unable to resolve "
                        + ref;

        if (exception.getMessage() != null) {

            result += ": "
                    + exception.getMessage();
        }

        return result;
    }
}