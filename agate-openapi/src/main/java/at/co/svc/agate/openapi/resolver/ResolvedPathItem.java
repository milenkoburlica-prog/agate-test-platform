package at.co.svc.agate.openapi.resolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;

public class ResolvedPathItem {

    private final OpenAPI openApi;

    private final PathItem pathItem;

    private final String sourceDocument;

    private final String sourceRef;

    private final String resolvedRef;


    public ResolvedPathItem(
            OpenAPI openApi,
            PathItem pathItem,
            String sourceDocument,
            String sourceRef,
            String resolvedRef) {

        this.openApi =
                openApi;

        this.pathItem =
                pathItem;

        this.sourceDocument =
                sourceDocument;

        this.sourceRef =
                sourceRef;

        this.resolvedRef =
                resolvedRef;
    }


    public OpenAPI getOpenApi() {

        return openApi;
    }


    public PathItem getPathItem() {

        return pathItem;
    }


    public String getSourceDocument() {

        return sourceDocument;
    }


    public String getSourceRef() {

        return sourceRef;
    }


    public String getResolvedRef() {

        return resolvedRef;
    }
}