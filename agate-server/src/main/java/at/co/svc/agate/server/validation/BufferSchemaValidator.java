package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BufferSchemaValidator
        implements AgateValidator {

    private static final Set<String> BUFFER_EXEC =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "name",
                    "value"
            );

    private static final Set<String> BUFFER_ASSERT =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "name",
                    "action",
                    "expected"
            );

    private static final Set<String> ASSERT_ACTIONS =
            Set.of(
                    "EQUALS",
                    "NOT_EQUALS",
                    "CONTAINS",
                    "IS_NULL",
                    "IS_NOT_NULL",
                    "IS_EMPTY",
                    "IS_NOT_EMPTY"
            );

    private static final Set<String> ACTIONS_REQUIRING_EXPECTED =
            Set.of(
                    "EQUALS",
                    "NOT_EQUALS",
                    "CONTAINS"
            );

    @Override
    public List<ValidationIssue> validate(
            ValidationContext c) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        for (ValidationUtil.TestCaseRef tc :
                ValidationUtil.testCases(c.root())) {

            for (ValidationUtil.StepRef step :
                    ValidationUtil.steps(
                            tc.node(),
                            tc.index())) {

                JsonNode node =
                        step.node();

                String rawType =
                        ValidationUtil.text(
                                node,
                                "type");

                if (rawType == null
                        || !"BUFFER".equalsIgnoreCase(rawType)) {

                    continue;
                }

                String op =
                        ValidationUtil.op(node);

                if (!Set.of(
                        "EXEC",
                        "ASSERT")
                        .contains(op)) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V500",
                                    "BUFFER has unsupported op '"
                                            + op
                                            + "'. Supported: EXEC, ASSERT.",
                                    c.file(),
                                    ValidationUtil.lineOfStep(
                                            c,
                                            node)
                            )
                    );

                    continue;
                }

                if ("EXEC".equals(op)) {

                    validateExec(
                            c,
                            node,
                            issues);

                } else {

                    validateAssert(
                            c,
                            node,
                            issues);
                }
            }
        }

        return issues;
    }

    private void validateExec(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        ValidationUtil.unknownFields(
                c,
                node,
                BUFFER_EXEC,
                "BUFFER/EXEC",
                issues);

        ValidationUtil.require(
                c,
                node,
                "name",
                "AGATE-V501",
                "BUFFER/EXEC",
                issues);

        ValidationUtil.require(
                c,
                node,
                "value",
                "AGATE-V502",
                "BUFFER/EXEC",
                issues);
    }

    private void validateAssert(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        /*
         * IMPORTANT:
         *
         * "response" is intentionally NOT allowed here.
         *
         * BUFFER/ASSERT works on the variable referenced by "name".
         *
         * Example:
         *
         * type: BUFFER
         * op: ASSERT
         * name: title
         * action: EQUALS
         * expected: "abc"
         *
         * A response property belongs to REST/SOAP ASSERT/BUFFER,
         * not to the native BUFFER Engine ASSERT operation.
         */
        ValidationUtil.unknownFields(
                c,
                node,
                BUFFER_ASSERT,
                "BUFFER/ASSERT",
                issues);

        ValidationUtil.require(
                c,
                node,
                "name",
                "AGATE-V510",
                "BUFFER/ASSERT",
                issues);

        ValidationUtil.require(
                c,
                node,
                "action",
                "AGATE-V511",
                "BUFFER/ASSERT",
                issues);

        String rawAction =
                ValidationUtil.text(
                        node,
                        "action");

        if (rawAction == null
                || rawAction.isBlank()) {

            return;
        }

        String action =
                rawAction.toUpperCase(
                        Locale.ROOT);

        if (!ASSERT_ACTIONS.contains(action)) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V512",
                            "BUFFER/ASSERT has invalid action '"
                                    + rawAction
                                    + "'. Supported: "
                                    + ASSERT_ACTIONS,
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    "action")
                    )
            );

            return;
        }

        /*
         * expected is mandatory only for actions
         * which compare against a concrete value.
         */
        if (ACTIONS_REQUIRING_EXPECTED.contains(action)
                && !node.hasNonNull("expected")) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V513",
                            "BUFFER/ASSERT action "
                                    + action
                                    + " requires property 'expected'.",
                            c.file(),
                            ValidationUtil.lineOfStep(
                                    c,
                                    node)
                    )
            );
        }

        /*
         * IS_* assertions do not use expected.
         *
         * This is only a warning because the assertion
         * itself is still executable.
         */
        if (!ACTIONS_REQUIRING_EXPECTED.contains(action)
                && node.has("expected")) {

            issues.add(
                    ValidationIssue.warning(
                            "AGATE-V514",
                            "BUFFER/ASSERT action "
                                    + action
                                    + " does not require property 'expected'.",
                            c.file(),
                            ValidationUtil.lineOfProperty(
                                    c,
                                    node,
                                    "action")
                    )
            );
        }
    }
}