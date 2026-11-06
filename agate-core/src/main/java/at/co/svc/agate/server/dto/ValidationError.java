package at.co.svc.agate.server.dto;

public class ValidationError {
    public String path;
    public String message;

    public ValidationError(String path, String message) {
        this.path = path;
        this.message = message;
    }
}