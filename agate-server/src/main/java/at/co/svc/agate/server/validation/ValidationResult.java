package at.co.svc.agate.server.validation;

import java.util.Comparator;
import java.util.List;

public record ValidationResult(List<ValidationIssue> issues) {

    public ValidationResult {
        issues = List.copyOf(issues);
    }

    public boolean valid() {
        return issues.stream().noneMatch(i -> i.severity() == ValidationSeverity.ERROR);
    }

    public long errors() {
        return count(ValidationSeverity.ERROR);
    }

    public long warnings() {
        return count(ValidationSeverity.WARNING);
    }

    public long infos() {
        return count(ValidationSeverity.INFO);
    }

    private long count(ValidationSeverity severity) {
        return issues.stream().filter(i -> i.severity() == severity).count();
    }

    public List<ValidationIssue> sortedIssues() {
        return issues.stream()
                .sorted(Comparator
                        .comparing((ValidationIssue i) -> i.line() == null ? Integer.MAX_VALUE : i.line())
                        .thenComparing(ValidationIssue::code))
                .toList();
    }
}
