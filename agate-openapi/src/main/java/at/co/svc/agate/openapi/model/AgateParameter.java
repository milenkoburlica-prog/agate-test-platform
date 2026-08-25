package at.co.svc.agate.openapi.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class AgateParameter {

    private String name;
    private String location;
    private boolean required;
    private String description;
    private String sourceRef;

    private String style;
    private Boolean explode;
    private Boolean allowEmptyValue;
    private Boolean allowReserved;

    private Object example;

    private Map<String, Object> examples =
            new LinkedHashMap<>();

    private AgateSchema schema;


    public String getName() {
        return name;
    }

    public void setName(
            String name) {

        this.name = name;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(
            String location) {

        this.location = location;
    }


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


    public String getStyle() {
        return style;
    }

    public void setStyle(
            String style) {

        this.style = style;
    }


    public Boolean getExplode() {
        return explode;
    }

    public void setExplode(
            Boolean explode) {

        this.explode = explode;
    }


    public Boolean getAllowEmptyValue() {
        return allowEmptyValue;
    }

    public void setAllowEmptyValue(
            Boolean allowEmptyValue) {

        this.allowEmptyValue = allowEmptyValue;
    }


    public Boolean getAllowReserved() {
        return allowReserved;
    }

    public void setAllowReserved(
            Boolean allowReserved) {

        this.allowReserved = allowReserved;
    }


    public Object getExample() {
        return example;
    }

    public void setExample(
            Object example) {

        this.example = example;
    }


    public Map<String, Object> getExamples() {
        return examples;
    }

    public void setExamples(
            Map<String, Object> examples) {

        this.examples =
                examples != null
                        ? examples
                        : new LinkedHashMap<>();
    }


    public AgateSchema getSchema() {
        return schema;
    }

    public void setSchema(
            AgateSchema schema) {

        this.schema = schema;
    }
}