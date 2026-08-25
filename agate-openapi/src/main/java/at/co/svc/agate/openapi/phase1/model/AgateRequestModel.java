package at.co.svc.agate.openapi.phase1.model;

import java.util.ArrayList;
import java.util.List;

public class AgateRequestModel {

    private String method;
    private String path;

    private List<AgateRequestParameterModel> pathParameters =
            new ArrayList<>();

    private List<AgateRequestParameterModel> queryParameters =
            new ArrayList<>();

    private List<AgateRequestParameterModel> headerParameters =
            new ArrayList<>();

    private List<AgateRequestParameterModel> cookieParameters =
            new ArrayList<>();

    private AgateRequestBodyModel body;


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

    public List<AgateRequestParameterModel> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(
            List<AgateRequestParameterModel> pathParameters) {

        this.pathParameters =
                pathParameters != null
                        ? pathParameters
                        : new ArrayList<>();
    }

    public List<AgateRequestParameterModel> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(
            List<AgateRequestParameterModel> queryParameters) {

        this.queryParameters =
                queryParameters != null
                        ? queryParameters
                        : new ArrayList<>();
    }

    public List<AgateRequestParameterModel> getHeaderParameters() {
        return headerParameters;
    }

    public void setHeaderParameters(
            List<AgateRequestParameterModel> headerParameters) {

        this.headerParameters =
                headerParameters != null
                        ? headerParameters
                        : new ArrayList<>();
    }

    public List<AgateRequestParameterModel> getCookieParameters() {
        return cookieParameters;
    }

    public void setCookieParameters(
            List<AgateRequestParameterModel> cookieParameters) {

        this.cookieParameters =
                cookieParameters != null
                        ? cookieParameters
                        : new ArrayList<>();
    }

    public AgateRequestBodyModel getBody() {
        return body;
    }

    public void setBody(
            AgateRequestBodyModel body) {

        this.body = body;
    }

    public void addPathParameter(
            AgateRequestParameterModel parameter) {

        if (parameter != null) {
            pathParameters.add(parameter);
        }
    }

    public void addQueryParameter(
            AgateRequestParameterModel parameter) {

        if (parameter != null) {
            queryParameters.add(parameter);
        }
    }

    public void addHeaderParameter(
            AgateRequestParameterModel parameter) {

        if (parameter != null) {
            headerParameters.add(parameter);
        }
    }

    public void addCookieParameter(
            AgateRequestParameterModel parameter) {

        if (parameter != null) {
            cookieParameters.add(parameter);
        }
    }
}