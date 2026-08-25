package at.co.svc.agate.openapi.phase1.model;

import java.util.ArrayList;
import java.util.List;

public class AgateOperationModel {

    private String identity;
    private String method;
    private String path;
    private String operationId;
    private String summary;
    private String description;

    private boolean deprecated;

    private List<String> tags =
            new ArrayList<>();

    private AgateRequestModel request;

    private List<AgateResponseModel> responses =
            new ArrayList<>();


    public String getIdentity() {
        return identity;
    }

    public void setIdentity(
            String identity) {

        this.identity = identity;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(
            String method) {

        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(
            String path) {

        this.path = path;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(
            String operationId) {

        this.operationId = operationId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(
            String summary) {

        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(
            boolean deprecated) {

        this.deprecated = deprecated;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(
            List<String> tags) {

        this.tags =
                tags != null
                        ? new ArrayList<>(tags)
                        : new ArrayList<>();
    }

    public AgateRequestModel getRequest() {
        return request;
    }

    public void setRequest(
            AgateRequestModel request) {

        this.request = request;
    }

    public List<AgateResponseModel> getResponses() {
        return responses;
    }

    public void setResponses(
            List<AgateResponseModel> responses) {

        this.responses =
                responses != null
                        ? responses
                        : new ArrayList<>();
    }

    public void addResponse(
            AgateResponseModel response) {

        if (response != null) {
            responses.add(response);
        }
    }
}