package at.co.svc.agate.openapi.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgateEndpoint {

    private String path;
    private String method;
    private String operationId;
    private String summary;
    private String description;

    private boolean deprecated;

    private List<String> tags =
            new ArrayList<>();

    private List<Map<String, List<String>>> security =
            new ArrayList<>();

    private List<AgateParameter> parameters =
            new ArrayList<>();

    private AgateRequestBody requestBody;

    private List<AgateResponse> responses =
            new ArrayList<>();


    public String getIdentity() {

        return String.valueOf(method)
                + ":"
                + String.valueOf(path);
    }

    public String getPath() {
        return path;
    }

    public void setPath(
            String path) {

        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(
            String method) {

        this.method = method;
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

    public void addTag(
            String tag) {

        if (tag != null) {
            tags.add(tag);
        }
    }

    public List<Map<String, List<String>>> getSecurity() {
        return security;
    }

    public void setSecurity(
            List<Map<String, List<String>>> security) {

        this.security =
                security != null
                        ? security
                        : new ArrayList<>();
    }

    public void addSecurity(
            Map<String, List<String>> securityRequirement) {

        if (securityRequirement != null) {

            security.add(
                    new LinkedHashMap<>(
                            securityRequirement
                    )
            );
        }
    }

    public List<AgateParameter> getParameters() {
        return parameters;
    }

    public void setParameters(
            List<AgateParameter> parameters) {

        this.parameters =
                parameters != null
                        ? parameters
                        : new ArrayList<>();
    }

    public void addParameter(
            AgateParameter parameter) {

        if (parameter != null) {
            parameters.add(parameter);
        }
    }

    public AgateRequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(
            AgateRequestBody requestBody) {

        this.requestBody = requestBody;
    }

    public List<AgateResponse> getResponses() {
        return responses;
    }

    public void setResponses(
            List<AgateResponse> responses) {

        this.responses =
                responses != null
                        ? responses
                        : new ArrayList<>();
    }

    public void addResponse(
            AgateResponse response) {

        if (response != null) {
            responses.add(response);
        }
    }
}