package at.co.svc.agate.engine.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class for storing REST response data in the execution context.
 */
public class RestResponse {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headersMap;
    private final String method;
    private final String url;

    public RestResponse(int statusCode, String body, Map<String, List<String>> headers, String method, String url) {
        this.statusCode = statusCode;
        this.body = body;
        this.method = method;
        this.url = url;

        this.headersMap = new HashMap<>();
        if (headers != null) {
            headers.forEach((k, v) -> {
                if (v != null && !v.isEmpty()) {
                    this.headersMap.put(k, v.get(0));
                }
            });
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeadersMap() {
        return headersMap;
    }

    public String getHeader(String name) {
        return headersMap.get(name);
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }
}