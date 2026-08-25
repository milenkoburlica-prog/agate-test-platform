package at.co.svc.agate.openapi.resolver;

import at.co.svc.agate.openapi.loader.OpenApiLoader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.nio.file.Path;

public class ResponseResolver {

    private final OpenApiLoader loader =
            new OpenApiLoader();


    public ResolvedResponse resolve(
            OpenAPI openApi,
            String sourceDocument,
            ApiResponse response) {

        if (response == null) {
            return null;
        }

        String ref =
                response.get$ref();

        if (ref == null ||
                ref.isBlank()) {

            return new ResolvedResponse(
                    openApi,
                    response,
                    sourceDocument,
                    null,
                    null
            );
        }

        if (ref.startsWith("#/")) {

            ApiResponse resolved =
                    resolveLocal(
                            openApi,
                            ref
                    );

            return new ResolvedResponse(
                    openApi,
                    resolved != null
                            ? resolved
                            : response,
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

        ApiResponse resolved =
                fragment != null
                        ? resolveLocal(
                                externalOpenApi,
                                fragment
                        )
                        : null;

        return new ResolvedResponse(
                externalOpenApi,
                resolved != null
                        ? resolved
                        : response,
                externalSource,
                ref,
                fragment
        );
    }


    private ApiResponse resolveLocal(
            OpenAPI openApi,
            String ref) {

        if (openApi == null ||
                openApi.getComponents() == null ||
                openApi.getComponents().getResponses() == null) {

            return null;
        }

        String prefix =
                "#/components/responses/";

        if (!ref.startsWith(prefix)) {
            return null;
        }

        String name =
                ref.substring(
                        prefix.length()
                );

        return openApi
                .getComponents()
                .getResponses()
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