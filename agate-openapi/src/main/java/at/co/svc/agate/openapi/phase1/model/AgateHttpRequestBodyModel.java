package at.co.svc.agate.openapi.phase1.model;

public class AgateHttpRequestBodyModel {

    private String mediaType;

    private Object value;


    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(
            String mediaType) {

        this.mediaType = mediaType;
    }


    public Object getValue() {
        return value;
    }

    public void setValue(
            Object value) {

        this.value = value;
    }
}