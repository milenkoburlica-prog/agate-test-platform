package at.co.svc.agate.openapi.resolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class ResolvedResponse {

    private final OpenAPI openApi;
    private final ApiResponse response;
    private final String sourceDocument;
    private final String sourceRef;
    private final String resolvedRef;


    public ResolvedResponse(
            OpenAPI openApi,
            ApiResponse response,
            String sourceDocument,
            String sourceRef,
            String resolvedRef) {

        this.openApi = openApi;
        this.response = response;
        this.sourceDocument = sourceDocument;
        this.sourceRef = sourceRef;
        this.resolvedRef = resolvedRef;
    }


    public OpenAPI getOpenApi() {
        return openApi;
    }

    public ApiResponse getResponse() {
        return response;
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