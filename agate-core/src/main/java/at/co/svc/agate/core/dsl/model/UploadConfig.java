package at.co.svc.agate.core.dsl.model; // Prilagodi tačan paket tvom projektu

public class UploadConfig {
    private String method;
    private String path;
    private String sourceFile;

    // Getters and Setters
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
}
