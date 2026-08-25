package at.co.svc.agate.openapi.resolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

public class ResolvedSchema {

    private final OpenAPI openApi;
    private final Schema<?> schema;
    private final String sourceDocument;
    private final String sourceRef;
    private final String resolvedRef;


    public ResolvedSchema(
            OpenAPI openApi,
            Schema<?> schema,
            String sourceDocument,
            String sourceRef,
            String resolvedRef) {

        this.openApi = openApi;
        this.schema = schema;
        this.sourceDocument = sourceDocument;
        this.sourceRef = sourceRef;
        this.resolvedRef = resolvedRef;
    }


    public OpenAPI getOpenApi() {
        return openApi;
    }

    public Schema<?> getSchema() {
        return schema;
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