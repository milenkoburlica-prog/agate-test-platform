package at.co.svc.agate.openapi.resolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.RequestBody;

public class ResolvedRequestBody {

    private final OpenAPI openApi;
    private final RequestBody requestBody;
    private final String sourceDocument;
    private final String sourceRef;
    private final String resolvedRef;


    public ResolvedRequestBody(
            OpenAPI openApi,
            RequestBody requestBody,
            String sourceDocument,
            String sourceRef,
            String resolvedRef) {

        this.openApi = openApi;
        this.requestBody = requestBody;
        this.sourceDocument = sourceDocument;
        this.sourceRef = sourceRef;
        this.resolvedRef = resolvedRef;
    }


    public OpenAPI getOpenApi() {
        return openApi;
    }

    public RequestBody getRequestBody() {
        return requestBody;
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