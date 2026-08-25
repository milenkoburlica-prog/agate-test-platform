package at.co.svc.agate.openapi.resolver;

import at.co.svc.agate.openapi.loader.OpenApiLoader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.nio.file.Path;

public class RequestBodyResolver {

    private final OpenApiLoader loader =
            new OpenApiLoader();


    public ResolvedRequestBody resolve(
            OpenAPI openApi,
            String sourceDocument,
            RequestBody requestBody) {

        if (requestBody == null) {
            return null;
        }

        String ref =
                requestBody.get$ref();

        if (ref == null ||
                ref.isBlank()) {

            return new ResolvedRequestBody(
                    openApi,
                    requestBody,
                    sourceDocument,
                    null,
                    null
            );
        }

        if (ref.startsWith("#/")) {

            RequestBody resolved =
                    resolveLocal(
                            openApi,
                            ref
                    );

            return new ResolvedRequestBody(
                    openApi,
                    resolved != null
                            ? resolved
                            : requestBody,
                    sourceDocument,
                    ref,
                    ref
            );
        }

        int separator =
                ref.indexOf('#');

        String documentPart =
                separator >= 0
                        ? ref.substring(0, separator)
                        : ref;

        String fragment =
                separator >= 0
                        ? ref.substring(separator)
                        : null;

        String externalSource =
                resolveDocumentPath(
                        sourceDocument,
                        documentPart
                );

        OpenAPI externalOpenApi =
                loader.load(
                        externalSource
                );

        RequestBody resolved =
                fragment != null
                        ? resolveLocal(
                                externalOpenApi,
                                fragment
                        )
                        : null;

        return new ResolvedRequestBody(
                externalOpenApi,
                resolved != null
                        ? resolved
                        : requestBody,
                externalSource,
                ref,
                fragment
        );
    }


    private RequestBody resolveLocal(
            OpenAPI openApi,
            String ref) {

        if (openApi == null ||
                openApi.getComponents() == null ||
                openApi.getComponents().getRequestBodies() == null) {

            return null;
        }

        String prefix =
                "#/components/requestBodies/";

        if (!ref.startsWith(prefix)) {
            return null;
        }

        String name =
                ref.substring(
                        prefix.length()
                );

        return openApi
                .getComponents()
                .getRequestBodies()
                .get(name);
    }


    private String resolveDocumentPath(
            String sourceDocument,
            String referencedDocument) {

        Path referencedPath =
                Path.of(referencedDocument);

        if (referencedPath.isAbsolute()) {
            return referencedPath
                    .normalize()
                    .toString();
        }

        Path sourcePath =
                Path.of(sourceDocument)
                        .toAbsolutePath()
                        .normalize();

        Path parent =
                sourcePath.getParent();

        if (parent == null) {
            return referencedPath
                    .toAbsolutePath()
                    .normalize()
                    .toString();
        }

        return parent
                .resolve(referencedDocument)
                .normalize()
                .toString();
    }
}