package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class OcSchemaValidator
        implements AgateValidator {

    private static final Set<String> OC_OPS =
            Set.of(
                    "EXEC",
                    "PUT",
                    "GET",
                    "ASSERT",
                    "BUFFER"
            );

    private static final Set<String> OC_EXEC_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "pod",
                    "namespace",
                    "command",
                    "response",
                    "expectedExitCode",
                    "checkExitCode",
                    "timeout",
                    "outputFile"
            );

    private static final Set<String> OC_TRANSFER_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "pod",
                    "namespace",
                    "from",
                    "to",
                    "response",
                    "expectedExitCode",
                    "checkExitCode",
                    "timeout"
            );

    private static final Set<String> OC_ASSERT_FIELDS =
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

    private static final Set<String> OC_BUFFER_FIELDS =
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

    private static final Set<String> OC_ASSERT_ACTIONS =
            Set.of(
                    "EXITCODE",
                    "CONTAINS",
                    "NOT_CONTAINS",
                    "EQUALS",
                    "NOT_EQUALS",
                    "COUNT"
            );

    private static final String OC_ASSERT_ACTIONS_TEXT =
            "EXITCODE, CONTAINS, NOT_CONTAINS, EQUALS, NOT_EQUALS, COUNT";

    private static final Set<String> OC_BUFFER_ACTIONS =
            Set.of(
                    "TEXT",
                    "FILTER",
                    "LINE",
                    "LAST_LINE",
                    "COUNT"
            );

    private static final String OC_BUFFER_ACTIONS_TEXT =
            "TEXT, FILTER, LINE, LAST_LINE, COUNT";

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
                        || !"OC".equalsIgnoreCase(rawType)) {

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
                                    "AGATE-V1000",
                                    "OC step requires property 'op'. "
                                            + "Allowed values: EXEC, PUT, GET, ASSERT, BUFFER.",
                                    c.file(),
                                    lineOfStep(c, node)
                            )
                    );

                    continue;
                }

                String op =
                        rawOp.trim()
                                .toUpperCase(Locale.ROOT);

                if (!OC_OPS.contains(op)) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V1001",
                                    "OC has invalid op '"
                                            + rawOp
                                            + "'. Allowed values: "
                                            + "EXEC, PUT, GET, ASSERT, BUFFER.",
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

                    case "PUT" ->
                            validateTransfer(
                                    c,
                                    node,
                                    issues,
                                    "PUT");

                    case "GET" ->
                            validateTransfer(
                                    c,
                                    node,
                                    issues,
                                    "GET");

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
                        // unreachable
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
                OC_EXEC_FIELDS,
                "OC/EXEC",
                issues);

        ValidationUtil.require(
                c,
                node,
                "pod",
                "AGATE-V1002",
                "OC/EXEC",
                issues);

        ValidationUtil.require(
                c,
                node,
                "command",
                "AGATE-V1003",
                "OC/EXEC",
                issues);

        validateExecutionOptions(
                c,
                node,
                issues,
                "OC/EXEC");
    }

    private void validateTransfer(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues,
            String op) {

        ValidationUtil.unknownFields(
                c,
                node,
                OC_TRANSFER_FIELDS,
                "OC/" + op,
                issues);

        ValidationUtil.require(
                c,
                node,
                "pod",
                "AGATE-V1010",
                "OC/" + op,
                issues);

        ValidationUtil.require(
                c,
                node,
                "from",
                "AGATE-V1011",
                "OC/" + op,
                issues);

        ValidationUtil.require(
                c,
                node,
                "to",
                "AGATE-V1012",
                "OC/" + op,
                issues);

        validateExecutionOptions(
                c,
                node,
                issues,
                "OC/" + op);
    }

    private void validateExecutionOptions(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues,
            String contextName) {

        /*
         * expectedExitCode
         */
        JsonNode expectedExitCode =
                node.get("expectedExitCode");

        if (expectedExitCode != null
                && !expectedExitCode.isNull()
                && !expectedExitCode.isIntegralNumber()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V1020",
                            contextName
                                    + " property 'expectedExitCode' must be an integer. "
                                    + "Actual: '"
                                    + expectedExitCode.asText()
                                    + "'.",
                            c.file(),
                            c.source()
                                    .lineOf("expectedExitCode:")
                    )
            );
        }

        /*
         * checkExitCode
         */
        JsonNode checkExitCode =
                node.get("checkExitCode");

        if (checkExitCode != null
                && !checkExitCode.isNull()
                && !checkExitCode.isBoolean()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V1021",
                            contextName
                                    + " property 'checkExitCode' must be boolean. "
                                    + "Actual: '"
                                    + checkExitCode.asText()
                                    + "'. Allowed values: true | false.",
                            c.file(),
                            c.source()
                                    .lineOf("checkExitCode:")
                    )
            );
        }

        /*
         * timeout
         */
        JsonNode timeout =
                node.get("timeout");

        if (timeout != null
                && !timeout.isNull()) {

            if (!timeout.isIntegralNumber()) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V1022",
                                contextName
                                        + " property 'timeout' must be an integer greater than 0. "
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
                                "AGATE-V1023",
                                contextName
                                        + " property 'timeout' must be greater than 0. "
                                        + "Actual: "
                                        + timeout.asLong()
                                        + ".",
                                c.file(),
                                c.source()
                                        .lineOf(
                                                "timeout: "
                                                        + timeout.asText())
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
                OC_ASSERT_FIELDS,
                "OC/ASSERT",
                issues);

        ValidationUtil.require(
                c,
                node,
                "response",
                "AGATE-V1030",
                "OC/ASSERT",
                issues);

        ValidationUtil.require(
                c,
                node,
                "action",
                "AGATE-V1031",
                "OC/ASSERT",
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

        if (!OC_ASSERT_ACTIONS.contains(action)) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V1032",
                            "OC/ASSERT has invalid action '"
                                    + rawAction
                                    + "'. Allowed values: "
                                    + OC_ASSERT_ACTIONS_TEXT
                                    + ".",
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

                if (isMissing(
                        node,
                        "expected")) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V1033",
                                    "OC/ASSERT EXITCODE requires property 'expected'.",
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

                if (isMissing(
                        node,
                        "value")) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V1034",
                                    "OC/ASSERT "
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

                if (isMissing(
                        node,
                        "value")) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V1034",
                                    "OC/ASSERT COUNT requires property 'value'.",
                                    c.file(),
                                    lineOfAction(
                                            c,
                                            rawAction)
                            )
                    );
                }

                if (isMissing(
                        node,
                        "expected")) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V1035",
                                    "OC/ASSERT COUNT requires property 'expected'.",
                                    c.file(),
                                    lineOfAction(
                                            c,
                                            rawAction)
                            )
                    );
                }
            }

            default -> {
                // already validated
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
                OC_BUFFER_FIELDS,
                "OC/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "response",
                "AGATE-V1040",
                "OC/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "action",
                "AGATE-V1041",
                "OC/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "name",
                "AGATE-V1042",
                "OC/BUFFER",
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

        if (!OC_BUFFER_ACTIONS.contains(action)) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V1043",
                            "OC/BUFFER has invalid action '"
                                    + rawAction
                                    + "'. Allowed values: "
                                    + OC_BUFFER_ACTIONS_TEXT
                                    + ".",
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

                if (isMissing(
                        node,
                        "value")) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V1044",
                                    "OC/BUFFER "
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
                 * TEXT does not use value.
                 *
                 * LINE and LAST_LINE:
                 * value optional, default 0.
                 */
            }

            default -> {
                // already validated
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

            return value.asText()
                    .isBlank();
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
                .lineOf("type: OC");
    }

    private int lineOfStep(
            ValidationContext c,
            JsonNode node) {

        String pod =
                ValidationUtil.text(
                        node,
                        "pod");

        if (pod != null
                && !pod.isBlank()) {

            int line =
                    c.source()
                            .lineOf(
                                    "pod: "
                                            + pod);

            if (line > 0) {
                return line;
            }
        }

        String command =
                ValidationUtil.text(
                        node,
                        "command");

        if (command != null
                && !command.isBlank()) {

            int line =
                    c.source()
                            .lineOf(
                                    "command: "
                                            + command);

            if (line > 0) {
                return line;
            }
        }

        return c.source()
                .lineOf("type: OC");
    }
}