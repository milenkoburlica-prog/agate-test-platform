package at.co.svc.agate.core.dsl.model;

import java.util.Map;

/**
 * POJO representing the structure of metadata.json for REST calls.
 */
public class RestMetadata {
    private String method;
    private String url;
    private Map<String, String> headers;

    // Getters and Setters
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
}