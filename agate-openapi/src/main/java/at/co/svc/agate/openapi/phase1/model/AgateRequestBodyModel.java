package at.co.svc.agate.openapi.phase1.model;

import java.util.ArrayList;
import java.util.List;

public class AgateRequestBodyModel {

    private boolean required;

    private String description;

    private String sourceRef;

    private List<AgateRequestContentModel> contents =
            new ArrayList<>();


    public boolean isRequired() {
        return required;
    }

    public void setRequired(
            boolean required) {

        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(
            String sourceRef) {

        this.sourceRef = sourceRef;
    }

    public List<AgateRequestContentModel> getContents() {
        return contents;
    }

    public void setContents(
            List<AgateRequestContentModel> contents) {

        this.contents =
                contents != null
                        ? contents
                        : new ArrayList<>();
    }

    public void addContent(
            AgateRequestContentModel content) {

        if (content != null) {
            contents.add(content);
        }
    }
}