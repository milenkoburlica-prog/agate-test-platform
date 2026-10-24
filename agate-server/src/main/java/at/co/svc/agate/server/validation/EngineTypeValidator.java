package at.co.svc.agate.server.validation;

import at.co.svc.agate.core.dsl.model.StepType;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EngineTypeValidator
        implements AgateValidator {

    @Override
    public List<ValidationIssue> validate(
            ValidationContext c) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        for (ValidationUtil.TestCaseRef tc : ValidationUtil.testCases(c.root())) {
            validateNested(c, tc.node().get("steps"), "testCases[" + tc.index() + "].steps", issues, false);
        }

        return issues;
    }

    private void validateNested(ValidationContext c, JsonNode steps, String prefix,
                                List<ValidationIssue> issues, boolean inLoop) {
        if (steps == null || !steps.isArray()) return;
        for (int i = 0; i < steps.size(); i++) {
            JsonNode node = steps.get(i);
            if (!node.isObject()) continue;
            String path = prefix + "[" + i + "]";
            String rawType = ValidationUtil.text(node, "type");
            if (rawType == null || rawType.isBlank()) {
                issues.add(ValidationIssue.error("AGATE-V120",
                        path + " requires property 'type'.", c.file(), lineOfStep(c,node)));
            } else if (!isKnownStepType(rawType)) {
                issues.add(ValidationIssue.error("AGATE-V121",
                        "Unknown or unsupported engine type '" + rawType + "' at " + path + ".",
                        c.file(), c.source().lineOf("type: " + rawType)));
            }
            if ("BREAK".equalsIgnoreCase(rawType)) {
                if (!inLoop) issues.add(ValidationIssue.error("AGATE-V128",
                        "BREAK is only allowed inside LOOP.steps", c.file(), lineOfStep(c, node)));
                String condition = ValidationUtil.text(node, "condition");
                if (condition == null || condition.isBlank()) issues.add(ValidationIssue.error("AGATE-V129",
                        "BREAK requires condition", c.file(), lineOfStep(c, node)));
            }
            validateNested(c, node.get("steps"), path + ".steps", issues,
                    inLoop || "LOOP".equalsIgnoreCase(rawType));
        }
    }

    private boolean isKnownStepType(
            String rawType) {

        try {

            StepType.valueOf(
                    rawType.trim()
                            .toUpperCase(
                                    Locale.ROOT));

            return true;

        } catch (IllegalArgumentException e) {

            return false;
        }
    }

    private int lineOfStep(
            ValidationContext c,
            JsonNode node) {

        String id =
                ValidationUtil.text(
                        node,
                        "id");

        if (id != null
                && !id.isBlank()) {

            return c.source()
                    .lineOf(
                            "id: "
                                    + id);
        }

        String type =
                ValidationUtil.text(
                        node,
                        "type");

        if (type != null
                && !type.isBlank()) {

            return c.source()
                    .lineOf(
                            "type: "
                                    + type);
        }

        return 1;
    }
}