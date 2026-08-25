package at.co.svc.agate.openapi.resolver;

import at.co.svc.agate.openapi.loader.OpenApiLoader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

public class ParameterResolver {

    private final OpenApiLoader loader =
            new OpenApiLoader();

    private final SourceDocumentResolver sourceDocumentResolver =
            new SourceDocumentResolver();


    public ResolvedParameter resolve(
            OpenAPI openApi,
            String sourceDocument,
            Parameter parameter) {

        if (parameter == null) {
            return null;
        }

        String ref =
                parameter.get$ref();

        if (ref == null ||
                ref.isBlank()) {

            return new ResolvedParameter(
                    openApi,
                    parameter,
                    sourceDocument,
                    null,
                    null
            );
        }

        if (ref.startsWith("#/")) {

            Parameter resolved =
                    resolveLocal(
                            openApi,
                            ref
                    );

            return new ResolvedParameter(
                    openApi,
                    resolved != null
                            ? resolved
                            : parameter,
                    sourceDocument,
                    ref,
                    ref
            );
        }

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

        Parameter resolved =
                fragment != null
                        ? resolveLocal(
                                externalOpenApi,
                                fragment
                        )
                        : null;

        return new ResolvedParameter(
                externalOpenApi,
                resolved != null
                        ? resolved
                        : parameter,
                externalSource,
                ref,
                fragment
        );
    }


    private Parameter resolveLocal(
            OpenAPI openApi,
            String ref) {

        if (openApi == null ||
                openApi.getComponents() == null ||
                openApi
                        .getComponents()
                        .getParameters() == null) {

            return null;
        }

        String prefix =
                "#/components/parameters/";

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
                .getParameters()
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