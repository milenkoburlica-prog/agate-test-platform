package at.co.svc.agate.openapi.phase1.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgateHttpRequestModel {

    private String method;

    private String path;

    private Map<String, List<String>> queryParameters =
            new LinkedHashMap<>();

    private Map<String, List<String>> headers =
            new LinkedHashMap<>();

    private Map<String, List<String>> cookies =
            new LinkedHashMap<>();

    private AgateHttpRequestBodyModel body;


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


    public Map<String, List<String>> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(
            Map<String, List<String>> queryParameters) {

        this.queryParameters =
                queryParameters != null
                        ? queryParameters
                        : new LinkedHashMap<>();
    }


    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(
            Map<String, List<String>> headers) {

        this.headers =
                headers != null
                        ? headers
                        : new LinkedHashMap<>();
    }


    public Map<String, List<String>> getCookies() {
        return cookies;
    }

    public void setCookies(
            Map<String, List<String>> cookies) {

        this.cookies =
                cookies != null
                        ? cookies
                        : new LinkedHashMap<>();
    }


    public AgateHttpRequestBodyModel getBody() {
        return body;
    }

    public void setBody(
            AgateHttpRequestBodyModel body) {

        this.body = body;
    }


    public void addQueryParameter(
            String name,
            List<String> values) {

        queryParameters.put(
                name,
                values != null
                        ? new ArrayList<>(values)
                        : new ArrayList<>()
        );
    }


    public void addHeader(
            String name,
            List<String> values) {

        headers.put(
                name,
                values != null
                        ? new ArrayList<>(values)
                        : new ArrayList<>()
        );
    }


    public void addCookie(
            String name,
            List<String> values) {

        cookies.put(
                name,
                values != null
                        ? new ArrayList<>(values)
                        : new ArrayList<>()
        );
    }
}