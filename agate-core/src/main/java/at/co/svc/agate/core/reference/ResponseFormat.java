package at.co.svc.agate.core.reference;

public enum ResponseFormat {

    XML("xml"),
    JSON("json");

    private final String fileExtension;

    ResponseFormat(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}