package at.co.svc.agate.openapi.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class AgateContent {

    private String mediaType;

    private Object example;

    private Map<String, Object> examples =
            new LinkedHashMap<>();

    private AgateSchema schema;


    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(
            String mediaType) {

        this.mediaType = mediaType;
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