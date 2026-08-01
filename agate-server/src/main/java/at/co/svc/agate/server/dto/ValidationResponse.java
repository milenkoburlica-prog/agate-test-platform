package at.co.svc.agate.server.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidationResponse {
    public boolean isValid;
    public List<ValidationError> errors = new ArrayList<>();
    public List<String> warnings = new ArrayList<>();

    public ValidationResponse(boolean isValid) {
        this.isValid = isValid;
    }

    public void addError(String path, String message) {
        this.errors.add(new ValidationError(path, message));
        this.isValid = false;
    }
}