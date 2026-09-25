package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SqlSchemaValidator
        implements AgateValidator {

    private static final Set<String> SQL_OPS =
            Set.of(
                    "EXEC",
                    "ASSERT",
                    "BUFFER"
            );

    private static final Set<String> SQL_EXEC_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "command",
                    "datasource",
                    "response",
                    "constraints"
            );

    private static final Set<String> SQL_ASSERT_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "response",
                    "action",
                    "row",
                    "column",
                    "expected"
            );

    private static final Set<String> SQL_BUFFER_FIELDS =
            Set.of(
                    "id",
                    "type",
                    "op",
                    "condition",
                    "response",
                    "row",
                    "column",
                    "name"
            );

    private static final Set<String> SQL_ASSERT_ACTIONS =
            Set.of(
                    "ROW_COUNT",
                    "ALL_MATCH",
                    "ANY_MATCH",
                    "IS_NULL",
                    "IS_NOT_NULL",
                    "IS_EMPTY",
                    "IS_NOT_EMPTY",
                    "EQUALS",
                    "NOT_EQUALS",
                    "CONTAINS",
                    "GREATER_THAN",
                    "LESS_THAN",
                    "GREATER_THAN_OR_EQUAL",
                    "LESS_THAN_OR_EQUAL",
                    "BETWEEN",
                    "DATE_EQUALS"
            );

    private static final Set<String> ACTIONS_WITHOUT_EXPECTED =
            Set.of(
                    "IS_NULL",
                    "IS_NOT_NULL",
                    "IS_EMPTY",
                    "IS_NOT_EMPTY"
            );

    private static final Set<String> ACTIONS_WITHOUT_COLUMN =
            Set.of(
                    "ROW_COUNT"
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
                        || !"SQL".equalsIgnoreCase(rawType)) {

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
                                    "AGATE-V900",
                                    "SQL step requires property 'op'. "
                                            + "Allowed values: EXEC, ASSERT, BUFFER.",
                                    c.file(),
                                    lineOfStep(
                                            c,
                                            node)
                            )
                    );

                    continue;
                }

                String op =
                        rawOp.trim()
                                .toUpperCase(Locale.ROOT);

                if (!SQL_OPS.contains(op)) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V901",
                                    "SQL has invalid op '"
                                            + rawOp
                                            + "'. Allowed values: EXEC, ASSERT, BUFFER.",
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
                SQL_EXEC_FIELDS,
                "SQL/EXEC",
                issues);

        ValidationUtil.require(
                c,
                node,
                "command",
                "AGATE-V902",
                "SQL/EXEC",
                issues);

        JsonNode datasourceNode =
                node.get("datasource");

        if (datasourceNode != null
                && !datasourceNode.isNull()) {

            String datasource =
                    datasourceNode.asText();

            if (datasource == null
                    || datasource.isBlank()) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V909",
                                "SQL/EXEC property 'datasource' must not be blank when specified.",
                                c.file(),
                                c.source()
                                        .lineOf("datasource:")
                        )
                );
            }
        }

        String command =
                ValidationUtil.text(
                        node,
                        "command");

        if (command != null
                && isSelectCommand(command)
                && isMissing(node, "response")) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V903",
                            "SQL/EXEC SELECT requires property 'response'.",
                            c.file(),
                            lineOfStep(
                                    c,
                                    node)
                    )
            );
        }

        validateConstraints(
                c,
                node,
                issues);
    }

    private void validateAssert(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        ValidationUtil.unknownFields(
                c,
                node,
                SQL_ASSERT_FIELDS,
                "SQL/ASSERT",
                issues);

        ValidationUtil.require(
                c,
                node,
                "response",
                "AGATE-V910",
                "SQL/ASSERT",
                issues);

        ValidationUtil.require(
                c,
                node,
                "action",
                "AGATE-V911",
                "SQL/ASSERT",
                issues);

        String rawAction =
                ValidationUtil.text(
                        node,
                        "action");

        if (rawAction == null
                || rawAction.isBlank()) {

            validateRow(
                    c,
                    node,
                    issues);

            return;
        }

        String action =
                rawAction.trim()
                        .toUpperCase(Locale.ROOT);

        if (!SQL_ASSERT_ACTIONS.contains(action)) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V912",
                            "SQL/ASSERT has invalid action '"
                                    + rawAction
                                    + "'. Allowed values: "
                                    + String.join(
                                            ", ",
                                            SQL_ASSERT_ACTIONS)
                                    + ".",
                            c.file(),
                            c.source()
                                    .lineOf(
                                            "action: "
                                                    + rawAction)
                    )
            );

            validateRow(
                    c,
                    node,
                    issues);

            return;
        }

        /*
         * column is required for every ASSERT action
         * except ROW_COUNT.
         */
        if (!ACTIONS_WITHOUT_COLUMN.contains(action)
                && isMissing(node, "column")) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V913",
                            "SQL/ASSERT "
                                    + action
                                    + " requires property 'column'.",
                            c.file(),
                            lineOfAction(
                                    c,
                                    rawAction)
                    )
            );
        }

        /*
         * expected is required except for unary
         * NULL / EMPTY assertions.
         */
        if (!ACTIONS_WITHOUT_EXPECTED.contains(action)
                && isMissing(node, "expected")) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V914",
                            "SQL/ASSERT "
                                    + action
                                    + " requires property 'expected'.",
                            c.file(),
                            lineOfAction(
                                    c,
                                    rawAction)
                    )
            );
        }

        validateRow(
                c,
                node,
                issues);
    }

    private void validateBuffer(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        /*
         * action intentionally not allowed here.
         *
         * SQL/BUFFER extracts one cell and does not use action.
         */
        ValidationUtil.unknownFields(
                c,
                node,
                SQL_BUFFER_FIELDS,
                "SQL/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "response",
                "AGATE-V920",
                "SQL/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "column",
                "AGATE-V921",
                "SQL/BUFFER",
                issues);

        ValidationUtil.require(
                c,
                node,
                "name",
                "AGATE-V922",
                "SQL/BUFFER",
                issues);

        validateRow(
                c,
                node,
                issues);
    }

    private void validateRow(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        JsonNode row =
                node.get("row");

        if (row == null
                || row.isNull()) {

            return;
        }

        if (row.isIntegralNumber()) {

            if (row.asLong() < 0) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V915",
                                "SQL property 'row' must not be negative. "
                                        + "Actual: "
                                        + row.asLong()
                                        + ".",
                                c.file(),
                                c.source()
                                        .lineOf(
                                                "row: "
                                                        + row.asText())
                        )
                );
            }

            return;
        }

        /*
         * Accept textual integer values as well,
         * because the DSL/examples allow flexible YAML scalar usage.
         */
        if (row.isTextual()) {

            String value =
                    row.asText()
                            .trim();

            try {

                long parsed =
                        Long.parseLong(value);

                if (parsed < 0) {

                    issues.add(
                            ValidationIssue.error(
                                    "AGATE-V915",
                                    "SQL property 'row' must not be negative. "
                                            + "Actual: "
                                            + parsed
                                            + ".",
                                    c.file(),
                                    c.source()
                                            .lineOf(
                                                    "row:")
                            )
                    );
                }

                return;

            } catch (NumberFormatException ignored) {
                // handled below
            }
        }

        issues.add(
                ValidationIssue.error(
                        "AGATE-V916",
                        "SQL property 'row' must be a non-negative integer. "
                                + "Actual: '"
                                + row.asText()
                                + "'.",
                        c.file(),
                        c.source()
                                .lineOf("row:")
                )
        );
    }

    private void validateConstraints(
            ValidationContext c,
            JsonNode node,
            List<ValidationIssue> issues) {

        JsonNode constraints =
                node.get("constraints");

        if (constraints == null
                || constraints.isNull()) {

            return;
        }

        if (!constraints.isArray()) {

            issues.add(
                    ValidationIssue.error(
                            "AGATE-V904",
                            "SQL/EXEC property 'constraints' must be a list.",
                            c.file(),
                            c.source()
                                    .lineOf("constraints:")
                    )
            );

            return;
        }

        int index = 0;

        for (JsonNode constraint :
                constraints) {

            index++;

            if (!constraint.isObject()) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V905",
                                "SQL/EXEC constraint #"
                                        + index
                                        + " must be an object.",
                                c.file(),
                                c.source()
                                        .lineOf("constraints:")
                        )
                );

                continue;
            }

            if (isMissing(
                    constraint,
                    "column")) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V906",
                                "SQL/EXEC constraint #"
                                        + index
                                        + " requires property 'column'.",
                                c.file(),
                                c.source()
                                        .lineOf("constraints:")
                        )
                );
            }

            if (isMissing(
                    constraint,
                    "action")) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V907",
                                "SQL/EXEC constraint #"
                                        + index
                                        + " requires property 'action'.",
                                c.file(),
                                c.source()
                                        .lineOf("constraints:")
                        )
                );

                continue;
            }

            String action =
                    ValidationUtil.text(
                            constraint,
                            "action");

            if (action == null
                    || action.isBlank()) {

                continue;
            }

            String normalized =
                    action.trim()
                            .toUpperCase(Locale.ROOT);

            /*
             * Documentation explicitly says expected is not
             * required for IS_NULL / IS_NOT_NULL.
             *
             * For all other constraint actions we require expected.
             */
            if (!"IS_NULL".equals(normalized)
                    && !"IS_NOT_NULL".equals(normalized)
                    && isMissing(
                            constraint,
                            "expected")) {

                issues.add(
                        ValidationIssue.error(
                                "AGATE-V908",
                                "SQL/EXEC constraint #"
                                        + index
                                        + " with action '"
                                        + action
                                        + "' requires property 'expected'.",
                                c.file(),
                                c.source()
                                        .lineOf(
                                                "action: "
                                                        + action)
                        )
                );
            }

            /*
             * Deliberately no strict constraint-action whitelist yet.
             *
             * Documentation references the ASSERT actions but does
             * not define a separate exhaustive constraint action list.
             * Better not reject a valid runtime action without
             * confirming the implementation.
             */
        }
    }

    private boolean isSelectCommand(
            String command) {

        if (command == null) {
            return false;
        }

        String normalized =
                command.stripLeading()
                        .toUpperCase(Locale.ROOT);

        return normalized.startsWith("SELECT ")
                || normalized.startsWith("SELECT\n")
                || normalized.startsWith("WITH ");
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
                .lineOf("type: SQL");
    }

    private int lineOfStep(
            ValidationContext c,
            JsonNode node) {

        String command =
                ValidationUtil.text(
                        node,
                        "command");

        if (command != null
                && !command.isBlank()) {

            String firstLine =
                    command.lines()
                            .findFirst()
                            .orElse(command)
                            .trim();

            if (!firstLine.isBlank()) {

                int line =
                        c.source()
                                .lineOf(firstLine);

                if (line > 0) {
                    return line;
                }
            }
        }

        return c.source()
                .lineOf("type: SQL");
    }
}