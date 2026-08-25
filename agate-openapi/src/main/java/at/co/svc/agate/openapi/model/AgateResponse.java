package at.co.svc.agate.openapi.model;

import java.util.ArrayList;
import java.util.List;

public class AgateResponse {

    private String statusCode;
    private String description;
    private String sourceRef;

    private List<AgateContent> contents =
            new ArrayList<>();


    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public List<AgateContent> getContents() {
        return contents;
    }

    public void setContents(
            List<AgateContent> contents) {

        this.contents =
                contents != null
                        ? contents
                        : new ArrayList<>();
    }

    public void addContent(
            AgateContent content) {

        if (content != null) {
            contents.add(content);
        }
    }
}