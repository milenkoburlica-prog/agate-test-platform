package at.co.svc.agate.openapi.phase3.dsl.model;

import java.util.LinkedHashMap;
import java.util.Map;


public class AgateRestDslRequest {


    private String method;

    private String path;


    private Map<String, Object> query =
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




    public String getPath() {

        return path;
    }


    public void setPath(
            String path) {

        this.path = path;
    }




    public Map<String, Object> getQuery() {

        return query;
    }


    public void setQuery(
            Map<String, Object> query) {

        this.query =
                query != null
                        ? new LinkedHashMap<>(
                                query
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