package at.co.svc.agate.openapi.resolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

public class ResolvedParameter {

    private final OpenAPI openApi;
    private final Parameter parameter;
    private final String sourceDocument;
    private final String sourceRef;
    private final String resolvedRef;


    public ResolvedParameter(
            OpenAPI openApi,
            Parameter parameter,
            String sourceDocument,
            String sourceRef,
            String resolvedRef) {

        this.openApi = openApi;
        this.parameter = parameter;
        this.sourceDocument = sourceDocument;
        this.sourceRef = sourceRef;
        this.resolvedRef = resolvedRef;
    }


    public OpenAPI getOpenApi() {
        return openApi;
    }

    public Parameter getParameter() {
        return parameter;
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