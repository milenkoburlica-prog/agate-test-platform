package at.co.svc.agate.server.validation;

import java.nio.file.Path;

public record ValidationIssue(
        ValidationSeverity severity,
        String code,
        String message,
        Path file,
        Integer line,
        Integer column
) {
    public static ValidationIssue error(String code, String message, Path file, Integer line) {
        return new ValidationIssue(ValidationSeverity.ERROR, code, message, file, line, null);
    }

    public static ValidationIssue warning(String code, String message, Path file, Integer line) {
        return new ValidationIssue(ValidationSeverity.WARNING, code, message, file, line, null);
    }

    public static ValidationIssue info(String code, String message, Path file, Integer line) {
        return new ValidationIssue(ValidationSeverity.INFO, code, message, file, line, null);
    }
}
