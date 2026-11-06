package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class LoopSchemaValidator
        implements AgateValidator {

    private static final Set<String> LOOP_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "mode",
                    "condition",
                    "maxIterations",
                    "timeoutMs",
                    "steps"
            );

    private static final Set<String> LOOP_MODES =
            Set.of(
                    "WHILE",
                    "DO_WHILE"
            );

    @Override
    public List<ValidationIssue> validate(
            ValidationContext c) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        for (ValidationUtil.TestCaseRef tc :
                ValidationUtil.testCases(
                        c.root())) {

            validateSteps(
                    c,
                    tc.node().get("steps"),
                    issues);
        }

        return issues;
    }

    private void validateSteps(
            ValidationContext c,
            JsonNode steps,
            List<ValidationIssue> issues) {

        if (steps == null
                || !steps.isArray()) {

            return;
        }

        for (JsonNode node :
                steps) {

            if (node == null
                    || !node.isObject()) {

                continue;
            }

            String rawType =
                    ValidationUtil.text(
                            node,
                            "type");

            if ("LOOP".equalsIgnoreCase(
                    rawType)) {

                validateLoop(
                        c,
                        node,
                        issues);
            }

            JsonNode nestedSteps =
                    node.get("steps");

            if (nestedSteps != null
                    && nestedSteps.isArray()) {

                validateSteps(
                        c,
                        nestedSteps,
                        issues);
            }
        }
    }

    private void validateLoop(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        ValidationUtil.unknownFields(
                c,
                node,
                LOOP_FIELDS,
                "LOOP",
                issues);

        ValidationUtil.require(
                c,
                node,
                "mode",
                "AGATE-V1100",
                "LOOP",
                issues);

        ValidationUtil.require(
                c,
                node,
                "condition",
                "AGATE-V1101",
                "LOOP",
                issues);

        ValidationUtil.require(
                c,
                node,
                "maxIterations",
                "AGATE-V1102",
                "LOOP",
                issues);

        ValidationUtil.require(
                c,
                node,
                "timeoutMs",
                "AGATE-V1103",
                "LOOP",
                issues);

        ValidationUtil.require(
                c,
                node,
                "steps",
                "AGATE-V1104",
                "LOOP",
                issues);

        String rawMode =
                ValidationUtil.text(
                        node,
                        "mode");

        if (rawMode != null
                && !rawMode.isBlank()) {

            String mode =
                    rawMode.trim()
                            .toUpperCase(
                                    Locale.ROOT);

            if (!LOOP_MODES.contains(
                    mode)) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V1105",
                                "LOOP has invalid mode '"
                                        + rawMode
                                        + "'. Allowed values: WHILE, DO_WHILE.",
                                c.file(),
                                ValidationUtil.lineOfProperty(
                                        c,
                                        node,
                                        "mode")
                        )
                );
            }
        }

        validatePositiveInteger(
                c,
                node,
                "maxIterations",
                "AGATE-V1106",
                issues);

        validatePositiveLong(
                c,
                node,
                "timeoutMs",
                "AGATE-V1107",
                issues);

        JsonNode nestedSteps =
                node.get("steps");

        if (nestedSteps != null
                && nestedSteps.isArray()
                && nestedSteps.isEmpty()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V1108",
                            "LOOP property 'steps' must contain at least one child step.",
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    "steps")
                    )
            );
        }
    }

    private void validatePositiveInteger(
            ValidationContext c,
            JsonNode node,
            String field,
            String code,
            List<ValidationIssue> issues) {

        JsonNode value =
                node.get(field);

        if (value == null
                || value.isNull()) {

            return;
        }

        Integer parsed =
                parseInteger(
                        value);

        if (parsed == null
                || parsed <= 0) {

            issues.add(
                    ValidationIssue.error(
                            code,
                            "LOOP property '"
                                    + field
                                    + "' must be an integer greater than 0.",
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    field)
                    )
            );
        }
    }

    private void validatePositiveLong(
            ValidationContext c,
            JsonNode node,
            String field,
            String code,
            List<ValidationIssue> issues) {

        JsonNode value =
                node.get(field);

        if (value == null
                || value.isNull()) {

            return;
        }

        Long parsed =
                parseLong(
                        value);

        if (parsed == null
                || parsed <= 0) {

            issues.add(
                    ValidationIssue.error(
                            code,
                            "LOOP property '"
                                    + field
                                    + "' must be a number greater than 0.",
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    field)
                    )
            );
        }
    }

    private Integer parseInteger(
            JsonNode node) {

        try {

            if (node.isIntegralNumber()) {
                return node.intValue();
            }

            if (node.isTextual()) {
                return Integer.valueOf(
                        node.asText()
                                .trim());
            }

        } catch (NumberFormatException e) {
            return null;
        }

        return null;
    }

    private Long parseLong(
            JsonNode node) {

        try {

            if (node.isIntegralNumber()) {
                return node.longValue();
            }

            if (node.isTextual()) {
                return Long.valueOf(
                        node.asText()
                                .trim());
            }

        } catch (NumberFormatException e) {
            return null;
        }

        return null;
    }
}
