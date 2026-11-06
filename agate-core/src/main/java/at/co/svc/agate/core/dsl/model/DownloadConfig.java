package at.co.svc.agate.core.dsl.model;

/**
 * Configuration model for automated file extraction from SOAP Responses.
 */
public class DownloadConfig {
    private String method = "INLINE"; // Default fallback method
    private String path;
    private String targetPath;

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

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }
}