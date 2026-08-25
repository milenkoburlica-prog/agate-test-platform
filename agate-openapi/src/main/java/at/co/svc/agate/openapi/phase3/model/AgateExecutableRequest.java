package at.co.svc.agate.openapi.phase3.model;

import java.util.LinkedHashMap;
import java.util.Map;


public class AgateExecutableRequest {


    private String method;

    private String pathTemplate;

    private String resolvedPath;


    private Map<String, Object> pathParameters =
            new LinkedHashMap<>();


    private Map<String, Object> queryParameters =
            new LinkedHashMap<>();


    private Map<String, Object> headers =
            new LinkedHashMap<>();


    private Map<String, Object> cookies =
            new LinkedHashMap<>();


    private String bodyMediaType;

    private Object body;




    public String getMethod() {

        return method;
    }


    public void setMethod(
            String method) {

        this.method = method;
    }




    public String getPathTemplate() {

        return pathTemplate;
    }


    public void setPathTemplate(
            String pathTemplate) {

        this.pathTemplate = pathTemplate;
    }




    public String getResolvedPath() {

        return resolvedPath;
    }


    public void setResolvedPath(
            String resolvedPath) {

        this.resolvedPath =
                resolvedPath;
    }




    public Map<String, Object> getPathParameters() {

        return pathParameters;
    }


    public void setPathParameters(
            Map<String, Object> pathParameters) {

        this.pathParameters =
                pathParameters != null
                        ? new LinkedHashMap<>(
                                pathParameters
                        )
                        : new LinkedHashMap<>();
    }




    public Map<String, Object> getQueryParameters() {

        return queryParameters;
    }


    public void setQueryParameters(
            Map<String, Object> queryParameters) {

        this.queryParameters =
                queryParameters != null
                        ? new LinkedHashMap<>(
                                queryParameters
                        )
                        : new LinkedHashMap<>();
    }




    public Map<String, Object> getHeaders() {

        return headers;
    }


    public void setHeaders(
            Map<String, Object> headers) {

        this.headers =
                headers != null
                        ? new LinkedHashMap<>(
                                headers
                        )
                        : new LinkedHashMap<>();
    }




    public Map<String, Object> getCookies() {

        return cookies;
    }


    public void setCookies(
            Map<String, Object> cookies) {

        this.cookies =
                cookies != null
                        ? new LinkedHashMap<>(
                                cookies
                        )
                        : new LinkedHashMap<>();
    }




    public String getBodyMediaType() {

        return bodyMediaType;
    }


    public void setBodyMediaType(
            String bodyMediaType) {

        this.bodyMediaType =
                bodyMediaType;
    }




    public Object getBody() {

        return body;
    }


    public void setBody(
            Object body) {

        this.body = body;
    }
}