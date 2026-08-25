package at.co.svc.agate.openapi.resolver;

import at.co.svc.agate.openapi.loader.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;

public class PathItemResolver {

    private final OpenApiLoader loader =
            new OpenApiLoader();

    private final SourceDocumentResolver sourceDocumentResolver =
            new SourceDocumentResolver();


    public ResolvedPathItem resolve(
            OpenAPI openApi,
            String sourceDocument,
            PathItem pathItem) {

        if (pathItem == null) {
            return null;
        }

        String ref =
                pathItem.get$ref();

        if (ref == null ||
                ref.isBlank()) {

            return new ResolvedPathItem(
                    openApi,
                    pathItem,
                    sourceDocument,
                    null,
                    null
            );
        }

        if (ref.startsWith("#/")) {

            PathItem resolved =
                    resolveLocal(
                            openApi,
                            ref
                    );

            return new ResolvedPathItem(
                    openApi,
                    resolved != null
                            ? resolved
                            : pathItem,
                    sourceDocument,
                    ref,
                    ref
            );
        }

        return resolveExternal(
                sourceDocument,
                ref,
                pathItem
        );
    }


    private ResolvedPathItem resolveExternal(
            String sourceDocument,
            String ref,
            PathItem originalPathItem) {

        int separator =
                ref.indexOf('#');

        String documentPart =
                separator >= 0
                        ? ref.substring(
                                0,
                                separator
                        )
                        : ref;

        String fragment =
                separator >= 0
                        ? ref.substring(
                                separator
                        )
                        : null;

        String externalSource =
                sourceDocumentResolver.resolve(
                        sourceDocument,
                        documentPart
                );

        OpenAPI externalOpenApi =
                loader.load(
                        externalSource
                );

        PathItem resolved =
                fragment != null
                        ? resolveLocal(
                                externalOpenApi,
                                fragment
                        )
                        : null;

        return new ResolvedPathItem(
                externalOpenApi,
                resolved != null
                        ? resolved
                        : originalPathItem,
                externalSource,
                ref,
                fragment
        );
    }


    private PathItem resolveLocal(
            OpenAPI openApi,
            String ref) {

        if (openApi == null ||
                openApi.getPaths() == null ||
                ref == null) {

            return null;
        }

        String prefix =
                "#/paths/";

        if (!ref.startsWith(prefix)) {
            return null;
        }

        String encodedPath =
                ref.substring(
                        prefix.length()
                );

        String path =
                decodeJsonPointerToken(
                        encodedPath
                );

        Map<String, PathItem> paths =
                openApi.getPaths();

        return paths.get(
                path
        );
    }


    private String decodeJsonPointerToken(
            String value) {

        if (value == null) {
            return null;
        }

        return value
                .replace("~1", "/")
                .replace("~0", "~");
    }
}