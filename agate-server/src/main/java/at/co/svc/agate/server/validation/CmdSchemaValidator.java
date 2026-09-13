package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CmdSchemaValidator
        implements AgateValidator {

    private static final Set<String> CMD_EXEC_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "command",
                    "response",
                    "expectedExitCode",
                    "checkExitCode",
                    "timeout",
                    "outputFile"
            );

    private static final Set<String> CMD_ASSERT_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "response",
                    "action",
                    "value",
                    "expected"
            );

    private static final Set<String> CMD_BUFFER_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "response",
                    "action",
                    "value",
                    "name"
            );

    private static final Set<String> CMD_OPS =
            Set.of(
                    "EXEC",
                    "BUFFER",
                    "ASSERT"
            );

    private static final Set<String> CMD_ASSERT_ACTIONS =
            Set.of(
                    "EXITCODE",
                    "CONTAINS",
                    "NOT_CONTAINS",
                    "EQUALS",
                    "NOT_EQUALS",
                    "COUNT"
            );

    private static final Set<String> CMD_BUFFER_ACTIONS =
            Set.of(
                    "TEXT",
                    "FILTER",
                    "LINE",
                    "LAST_LINE",
                    "COUNT"
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
                        || !"CMD".equalsIgnoreCase(rawType)) {

                    continue;
                }

                String rawOp =
                        ValidationUtil.text(
                                node,
                                "op");

                if (rawOp == null
                        || rawOp.isBlank()) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V700",
                                    "CMD step requires property 'op'. "
                                            + "Allowed values: EXEC, BUFFER, ASSERT.",
                                    c.file(),
                                    lineOfStep(c, node)
                            )
                    );

                    continue;
                }

                String op =
                        rawOp.trim()
                                .toUpperCase(Locale.ROOT);

                if (!CMD_OPS.contains(op)) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V701",
                                    "CMD has invalid op '"
                                            + rawOp
                                            + "'. Allowed values: EXEC, BUFFER, ASSERT.",
                                    c.file(),
                                    c.source()
                                            .lineOf(
                                                    "op: "
                                                            + rawOp)
                            )
                    );

                    continue;
                }

                switch (op) {

                    case "EXEC" ->
                            validateExec(
                                    c,
                                    node,
                                    issues);

                    case "ASSERT" ->
                            validateAssert(
                                    c,
                                    node,
                                    issues);

                    case "BUFFER" ->
                            validateBuffer(
                                    c,
                                    node,
                                    issues);

                    default -> {
                        // Cannot normally be reached.
                    }
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
                CMD_EXEC_FIELDS,
                "CMD/EXEC",
                issues);

        ValidationUtil.require(
                c,
                node,
                "command",
                "AGATE-V702",
                "CMD/EXEC",
                issues);

        JsonNode expectedExitCode =
                node.get("expectedExitCode");

        if (expectedExitCode != null
                && !expectedExitCode.isNull()
                && !expectedExitCode.isIntegralNumber()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V703",
                            "CMD/EXEC property 'expectedExitCode' must be an integer. "
                                    + "Actual: '"
                                    + expectedExitCode.asText()
                                    + "'.",
                            c.file(),
                            c.source()
                                    .lineOf("expectedExitCode:")
                    )
            );
        }

        JsonNode checkExitCode =
                node.get("checkExitCode");

        if (checkExitCode != null
                && !checkExitCode.isNull()
                && !checkExitCode.isBoolean()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V704",
                            "CMD/EXEC property 'checkExitCode' must be boolean. "
                                    + "Actual: '"
                                    + checkExitCode.asText()
                                    + "'. Allowed values: true | false.",
                            c.file(),
                            c.source()
                                    .lineOf("checkExitCode:")
                    )
            );
        }

        JsonNode timeout =
                node.get("timeout");

        if (timeout != null
                && !timeout.isNull()) {

            if (!timeout.isIntegralNumber()) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V705",
                                "CMD/EXEC property 'timeout' must be an integer greater than 0. "
                                        + "Actual: '"
                                        + timeout.asText()
                                        + "'.",
                                c.file(),
                                c.source()
                                        .lineOf("timeout:")
                        )
                );

            } else if (timeout.asLong() <= 0) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V706",
                                "CMD/EXEC property 'timeout' must be greater than 0. "
                                        + "Actual: "
                                        + timeout.asLong()
                                        + ".",
                                c.file(),
                                c.source()
                                        .lineOf("timeout:")
                        )
                );
            }
        }
    }

    private void validateAssert(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        ValidationUtil.unknownFields(
                c,
                node,
                CMD_ASSERT_FIELDS,
                "CMD/ASSERT",
                issues);

        ValidationUtil.require(
                c,
                node,
                "response",
                "AGATE-V710",
                "CMD/ASSERT",
                issues);

        ValidationUtil.require(
                c,
                node,
                "action",
                "AGATE-V711",
                "CMD/ASSERT",
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
                rawAction.trim()
                        .toUpperCase(Locale.ROOT);

        if (!CMD_ASSERT_ACTIONS.contains(action)) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V712",
                            "CMD/ASSERT has invalid action '"
                                    + rawAction
                                    + "'. Allowed values: EXITCODE, CONTAINS, "
                                    + "NOT_CONTAINS, EQUALS, NOT_EQUALS, COUNT.",
                            c.file(),
                            c.source()
                                    .lineOf(
                                            "action: "
                                                    + rawAction)
                    )
            );

            return;
        }

        switch (action) {

            case "EXITCODE" -> {

                if (isMissing(node, "expected")) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V713",
                                    "CMD/ASSERT EXITCODE requires property 'expected'.",
                                    c.file(),
                                    lineOfAction(
                                            c,
                                            rawAction)
                            )
                    );
                }
            }

            case "CONTAINS",
                 "NOT_CONTAINS",
                 "EQUALS",
                 "NOT_EQUALS" -> {

                if (isMissing(node, "value")) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V714",
                                    "CMD/ASSERT "
                                            + action
                                            + " requires property 'value'.",
                                    c.file(),
                                    lineOfAction(
                                            c,
                                            rawAction)
                            )
                    );
                }
            }

            case "COUNT" -> {

                if (isMissing(node, "value")) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V714",
                                    "CMD/ASSERT COUNT requires property 'value'.",
                                    c.file(),
                                    lineOfAction(
                                            c,
                                            rawAction)
                            )
                    );
                }

                if (isMissing(node, "expected")) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V715",
                                    "CMD/ASSERT COUNT requires property 'expected'.",
                                    c.file(),
                                    lineOfAction(
                                            c,
                                            rawAction)
                            )
                    );
                }
            }

            default -> {
                // Already validated above.
            }
        }
    }

    private void validateBuffer(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        ValidationUtil.unknownFields(
                c,
                node,
                CMD_BUFFER_FIELDS,
                "CMD/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "response",
                "AGATE-V720",
                "CMD/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "action",
                "AGATE-V721",
                "CMD/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "name",
                "AGATE-V723",
                "CMD/BUFFER",
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
                rawAction.trim()
                        .toUpperCase(Locale.ROOT);

        if (!CMD_BUFFER_ACTIONS.contains(action)) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V722",
                            "CMD/BUFFER has invalid action '"
                                    + rawAction
                                    + "'. Allowed values: TEXT, FILTER, LINE, "
                                    + "LAST_LINE, COUNT.",
                            c.file(),
                            c.source()
                                    .lineOf(
                                            "action: "
                                                    + rawAction)
                    )
            );

            return;
        }

        switch (action) {

            case "FILTER",
                 "COUNT" -> {

                if (isMissing(node, "value")) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V724",
                                    "CMD/BUFFER "
                                            + action
                                            + " requires property 'value'.",
                                    c.file(),
                                    lineOfAction(
                                            c,
                                            rawAction)
                            )
                    );
                }
            }

            case "TEXT",
                 "LINE",
                 "LAST_LINE" -> {
                /*
                 * TEXT does not require value.
                 *
                 * LINE and LAST_LINE may omit value.
                 * Runtime default is 0.
                 */
            }

            default -> {
                // Already validated above.
            }
        }
    }

    private boolean isMissing(
            JsonNode node,
            String field) {

        if (node == null
                || field == null) {

            return true;
        }

        JsonNode value =
                node.get(field);

        if (value == null
                || value.isNull()) {

            return true;
        }

        if (value.isTextual()) {
            return value.asText().isBlank();
        }

        return false;
    }

    private int lineOfAction(
            ValidationContext c,
            String action) {

        if (action != null
                && !action.isBlank()) {

            return c.source()
                    .lineOf(
                            "action: "
                                    + action);
        }

        return c.source()
                .lineOf("type: CMD");
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

        String command =
                ValidationUtil.text(
                        node,
                        "command");

        if (command != null
                && !command.isBlank()) {

            return c.source()
                    .lineOf(
                            "command: "
                                    + command);
        }

        return c.source()
                .lineOf("type: CMD");
    }
}