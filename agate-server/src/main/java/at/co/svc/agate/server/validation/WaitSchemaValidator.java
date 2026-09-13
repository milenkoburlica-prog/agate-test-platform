package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class WaitSchemaValidator
        implements AgateValidator {

    private static final Set<String> WAIT_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "condition",
                    "value"
            );

    /*
     * WAIT runtime currently supports:
     *
     *   1000      -> milliseconds
     *   200ms     -> milliseconds
     *   200 ms    -> milliseconds
     *   2s        -> seconds
     *   2 s       -> seconds
     *   1m        -> minutes
     *   1 m       -> minutes
     *
     * Whitespace between number and unit is allowed because
     * WaitEngine removes whitespace before parsing.
     */
    private static final Pattern DURATION_PATTERN =
            Pattern.compile(
                    "^\\d+\\s*(ms|s|m)?$",
                    Pattern.CASE_INSENSITIVE
            );

    /*
     * Runtime placeholders are resolved only during execution.
     *
     * Example:
     *
     *   value: "{B[timeout]}"
     *
     * Static validation cannot know the resolved value yet.
     * DataFlowValidator validates whether the referenced variable
     * exists before this WAIT step.
     */
    private static final Pattern RUNTIME_VALUE_PATTERN =
            Pattern.compile(
                    "^\\{B\\[[A-Za-z_][A-Za-z0-9_.-]*]}$"
            );

    @Override
    public List<ValidationIssue> validate(
            ValidationContext c) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        for (ValidationUtil.TestCaseRef tc :
                ValidationUtil.testCases(
                        c.root())) {

            for (ValidationUtil.StepRef step :
                    ValidationUtil.steps(
                            tc.node(),
                            tc.index())) {

                JsonNode node =
                        step.node();

                String type =
                        ValidationUtil.type(
                                node);

                if (!"WAIT".equals(type)) {
                    continue;
                }

                validateWait(
                        c,
                        node,
                        issues);
            }
        }

        return issues;
    }

    private void validateWait(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        ValidationUtil.unknownFields(
                c,
                node,
                WAIT_FIELDS,
                "WAIT",
                issues);

        /*
         * value is required.
         */
        JsonNode valueNode =
                node.get("value");

        if (valueNode == null
                || valueNode.isNull()
                || (valueNode.isTextual()
                && valueNode.asText().isBlank())) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V800",
                            "WAIT requires property 'value'.",
                            c.file(),
                            ValidationUtil.lineOfStep(
                                    c,
                                    node)
                    )
            );

            return;
        }

        /*
         * YAML numeric form:
         *
         * value: 1000
         */
        if (valueNode.isIntegralNumber()) {

            long value =
                    valueNode.asLong();

            if (value < 0) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V802",
                                "WAIT property 'value' must not be negative. Actual: '"
                                        + value
                                        + "'.",
                                c.file(),
                                ValidationUtil.lineOfProperty(
                                        c,
                                        node,
                                        "value")
                        )
                );
            }

            return;
        }

        /*
         * Other JSON/YAML number types are not useful here.
         *
         * Example:
         *
         * value: 1.5
         *
         * WaitEngine ultimately uses Long.parseLong(), therefore
         * decimal durations are not supported.
         */
        if (valueNode.isNumber()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V801",
                            "WAIT property 'value' has invalid duration format. "
                                    + "Supported examples: 1000, 200ms, 2s, 1m.",
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    "value")
                    )
            );

            return;
        }

        if (!valueNode.isTextual()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V801",
                            "WAIT property 'value' must be a duration or runtime variable.",
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    "value")
                    )
            );

            return;
        }

        String rawValue =
                valueNode.asText();

        String value =
                rawValue.trim();

        /*
         * Runtime variable:
         *
         * value: "{B[timeout]}"
         *
         * Do not attempt to validate its resulting duration here.
         * DataFlowValidator checks whether timeout exists before use.
         */
        if (RUNTIME_VALUE_PATTERN
                .matcher(value)
                .matches()) {

            return;
        }

        /*
         * Explicit negative values deserve the more precise V802.
         *
         * Examples:
         *
         * -20
         * -20ms
         * -2s
         * -1m
         */
        if (value.matches(
                "^-\\d+\\s*(ms|s|m)?$")) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V802",
                            "WAIT property 'value' must not be negative. Actual: '"
                                    + rawValue
                                    + "'.",
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    "value")
                    )
            );

            return;
        }

        /*
         * Validate exactly the syntax accepted by WaitEngine.
         */
        if (!DURATION_PATTERN
                .matcher(value)
                .matches()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V801",
                            "WAIT property 'value' has invalid duration format. Actual: '"
                                    + rawValue
                                    + "'. Supported examples: 1000, 200ms, 200 ms, 2s, 1m.",
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    "value")
                    )
            );
        }
    }
}