package at.co.svc.agate.openapi.resolver;

import at.co.svc.agate.openapi.loader.OpenApiLoader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

public class SchemaResolver {

    private final OpenApiLoader loader =
            new OpenApiLoader();

    private final SourceDocumentResolver sourceDocumentResolver =
            new SourceDocumentResolver();


    public ResolvedSchema resolve(
            OpenAPI openApi,
            String sourceDocument,
            Schema<?> schema) {

        if (schema == null) {
            return null;
        }

        String ref =
                schema.get$ref();

        if (ref == null ||
                ref.isBlank()) {

            return new ResolvedSchema(
                    openApi,
                    schema,
                    sourceDocument,
                    null,
                    null
            );
        }

        if (ref.startsWith("#/")) {

            Schema<?> resolved =
                    resolveLocal(
                            openApi,
                            ref
                    );

            if (resolved == null) {

                return new ResolvedSchema(
                        openApi,
                        schema,
                        sourceDocument,
                        ref,
                        null
                );
            }

            return new ResolvedSchema(
                    openApi,
                    resolved,
                    sourceDocument,
                    ref,
                    ref
            );
        }

        return resolveExternal(
                sourceDocument,
                ref,
                schema
        );
    }


    private ResolvedSchema resolveExternal(
            String sourceDocument,
            String ref,
            Schema<?> originalSchema) {

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
                resolveDocumentPath(
                        sourceDocument,
                        documentPart
                );

        OpenAPI externalOpenApi =
                loader.load(
                        externalSource
                );

        if (fragment == null ||
                fragment.isBlank()) {

            return new ResolvedSchema(
                    externalOpenApi,
                    originalSchema,
                    externalSource,
                    ref,
                    null
            );
        }

        Schema<?> resolvedSchema =
                resolveLocal(
                        externalOpenApi,
                        fragment
                );

        if (resolvedSchema == null) {

            return new ResolvedSchema(
                    externalOpenApi,
                    originalSchema,
                    externalSource,
                    ref,
                    null
            );
        }

        return new ResolvedSchema(
                externalOpenApi,
                resolvedSchema,
                externalSource,
                ref,
                fragment
        );
    }


    private Schema<?> resolveLocal(
            OpenAPI openApi,
            String ref) {

        if (openApi == null ||
                openApi.getComponents() == null ||
                openApi
                        .getComponents()
                        .getSchemas() == null) {

            return null;
        }

        String prefix =
                "#/components/schemas/";

        if (!ref.startsWith(prefix)) {
            return null;
        }

        String name =
                ref.substring(
                        prefix.length()
                );

        if (name.isBlank()) {
            return null;
        }

        return openApi
                .getComponents()
                .getSchemas()
                .get(name);
    }


    private String resolveDocumentPath(
            String sourceDocument,
            String referencedDocument) {

        return sourceDocumentResolver.resolve(
                sourceDocument,
                referencedDocument
        );
    }
}