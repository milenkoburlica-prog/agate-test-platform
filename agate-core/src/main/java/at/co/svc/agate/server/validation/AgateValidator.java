package at.co.svc.agate.server.validation;

import java.util.List;

public interface AgateValidator {
    List<ValidationIssue> validate(ValidationContext context);
}
