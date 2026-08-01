package at.co.svc.agate.engine.rest;

import java.util.List;

public class RestResult {
    private String statusCode;
    private List<String> headers;
    private String body;
    
    public String getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
    public List<String> getHeaders() {
        return headers;
    }
    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public RestResult() {
        super();
    }
    public RestResult(String statusCode, List<String> headers, String body) {
        super();
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

}