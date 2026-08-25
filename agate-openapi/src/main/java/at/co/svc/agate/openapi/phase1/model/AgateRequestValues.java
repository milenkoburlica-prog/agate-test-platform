package at.co.svc.agate.openapi.phase1.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class AgateRequestValues {

    private Map<String, Object> path =
            new LinkedHashMap<>();

    private Map<String, Object> query =
            new LinkedHashMap<>();

    private Map<String, Object> headers =
            new LinkedHashMap<>();

    private Map<String, Object> cookies =
            new LinkedHashMap<>();

    private String bodyMediaType;

    private Object body;


    public Map<String, Object> getPath() {
        return path;
    }

    public void setPath(
            Map<String, Object> path) {

        this.path =
                path != null
                        ? path
                        : new LinkedHashMap<>();
    }


    public Map<String, Object> getQuery() {
        return query;
    }

    public void setQuery(
            Map<String, Object> query) {

        this.query =
                query != null
                        ? query
                        : new LinkedHashMap<>();
    }


    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(
            Map<String, Object> headers) {

        this.headers =
                headers != null
                        ? headers
                        : new LinkedHashMap<>();
    }


    public Map<String, Object> getCookies() {
        return cookies;
    }

    public void setCookies(
            Map<String, Object> cookies) {

        this.cookies =
                cookies != null
                        ? cookies
                        : new LinkedHashMap<>();
    }


    public String getBodyMediaType() {
        return bodyMediaType;
    }

    public void setBodyMediaType(
            String bodyMediaType) {

        this.bodyMediaType = bodyMediaType;
    }


    public Object getBody() {
        return body;
    }

    public void setBody(
            Object body) {

        this.body = body;
    }


    public void putPath(
            String name,
            Object value) {

        path.put(
                name,
                value
        );
    }


    public void putQuery(
            String name,
            Object value) {

        query.put(
                name,
                value
        );
    }


    public void putHeader(
            String name,
            Object value) {

        headers.put(
                name,
                value
        );
    }


    public void putCookie(
            String name,
            Object value) {

        cookies.put(
                name,
                value
        );
    }
}