package at.co.svc.agate.openapi.phase1.model;

public class AgateExpectedResponseModel {

    private String requestedStatusCode;

    private String resolvedStatusCode;

    private boolean defaultResponse;

    private String description;

    private String sourceRef;

    private AgateExpectedResponseContentModel content;


    public String getRequestedStatusCode() {
        return requestedStatusCode;
    }

    public void setRequestedStatusCode(
            String requestedStatusCode) {

        this.requestedStatusCode = requestedStatusCode;
    }


    public String getResolvedStatusCode() {
        return resolvedStatusCode;
    }

    public void setResolvedStatusCode(
            String resolvedStatusCode) {

        this.resolvedStatusCode = resolvedStatusCode;
    }


    public boolean isDefaultResponse() {
        return defaultResponse;
    }

    public void setDefaultResponse(
            boolean defaultResponse) {

        this.defaultResponse = defaultResponse;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }


    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(
            String sourceRef) {

        this.sourceRef = sourceRef;
    }


    public AgateExpectedResponseContentModel getContent() {
        return content;
    }

    public void setContent(
            AgateExpectedResponseContentModel content) {

        this.content = content;
    }
}