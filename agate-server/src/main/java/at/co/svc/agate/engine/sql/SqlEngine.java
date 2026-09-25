package at.co.svc.agate.engine.sql;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.co.svc.agate.core.dsl.model.Constraint;
import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.error.AgateStepException;
import at.co.svc.agate.core.interfaces.TestLogger;

/**
 * SQL Engine with tester-friendly AGATE error handling.
 */
public class SqlEngine extends AbstractStepEngine {

    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd",
            "dd.MM.yyyy",
            "MM/dd/yyyy",
            "yyyy/MM/dd",
            "dd-MM-yyyy"
    };

    private static final Set<String> SUPPORTED_OPERATIONS =
            Set.of("EXEC", "ASSERT", "BUFFER");

    private static final Set<String> SUPPORTED_ASSERT_ACTIONS =
            Set.of(
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
                    "GREATER_THAN_OR_EQUAL",
                    "LESS_THAN",
                    "LESS_THAN_OR_EQUAL",
                    "BETWEEN",
                    "DATE_EQUALS"
            );



    private static final Set<String> SUPPORTED_ASSERT_SOURCES =
            Set.of(
                    "ROW_COUNT"
            );

    private static final Set<String> SUPPORTED_ROW_COUNT_ACTIONS =
            Set.of(
                    "EQUALS",
                    "NOT_EQUALS",
                    "GREATER_THAN",
                    "GREATER_THAN_OR_EQUAL",
                    "LESS_THAN",
                    "LESS_THAN_OR_EQUAL"
            );

    private static final Set<String> SUPPORTED_CONSTRAINT_ACTIONS =
            Set.of(
                    "EQUALS",
                    "NOT_EQUALS",
                    "CONTAINS",
                    "IS_NULL",
                    "IS_NOT_NULL"
            );

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getTableFromContext(
            ExecutionContext context,
            String key) {

        if (isBlank(key)) {
            throw AgateStepException.builder("Required property is missing")
                    .field("response")
                    .hint("Reference the response created by a previous SQL EXEC step.")
                    .build();
        }

        Object data = context.getResponse(key, Object.class);

        if (data == null) {
            throw AgateStepException.builder("SQL response was not found")
                    .detail("Response", key)
                    .hint("Make sure a SQL EXEC step runs before this step and uses the same response name.")
                    .build();
        }

        if (data instanceof List<?>) {
            return (List<Map<String, Object>>) data;
        }

        throw AgateStepException.builder("SQL response has an invalid type")
                .detail("Response", key)
                .expected("SQL result table")
                .actual(data.getClass().getSimpleName())
                .hint("Reference a response produced by a SQL EXEC step.")
                .build();
    }

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.SQL;
    }

    @Override
    public void doExecute(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) throws Exception {

        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(logger, step);
        }

        String op = normalizeUpper(step.getOp(), "EXEC");

        if (!SUPPORTED_OPERATIONS.contains(op)) {
            throw AgateStepException.builder("Unsupported SQL operation")
                    .actual(op)
                    .hint("Supported operations: EXEC, ASSERT, BUFFER")
                    .build();
        }

        try {
            switch (op) {
                case "EXEC" ->
                        handleExecution(
                                tc,
                                step,
                                context,
                                yamlFile,
                                stepIndex,
                                printExecution,
                                logger,
                                isVerbose);

                case "ASSERT" ->
                        handleAssertion(
                                step,
                                context,
                                stepIndex,
                                printExecution,
                                logger,
                                isVerbose);

                case "BUFFER" ->
                        handleBuffer(
                                tc,
                                step,
                                context,
                                stepIndex,
                                printExecution,
                                logger,
                                isVerbose);

                default ->
                        throw AgateStepException.builder("Unsupported SQL operation")
                                .actual(op)
                                .hint("Supported operations: EXEC, ASSERT, BUFFER")
                                .build();
            }

        } catch (AgateStepException e) {
            logger.info(String.format(
                    "    %s>>> %s STEP FAILED | ERROR: %s%s",
                    ConsoleColors.RED,
                    step.getType(),
                    e.getMessage(),
                    ConsoleColors.RESET));
            throw e;

        } catch (Exception e) {
            logger.info(String.format(
                    "    %s>>> %s STEP FAILED | ERROR: %s%s",
                    ConsoleColors.RED,
                    step.getType(),
                    safeMessage(e),
                    ConsoleColors.RESET));
            throw e;
        }
    }

    private void handleExecution(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) throws Exception {

        if (isBlank(step.getCommand())) {
            throw AgateStepException.builder("Required property is missing")
                    .field("command")
                    .hint("SQL EXEC requires a SQL statement in 'command'.")
                    .build();
        }

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();

        String sqlCmd = step.getCommand();
        String resolvedSql;

        try {
            resolvedSql = resolver.resolve(
                    tc,
                    sqlCmd,
                    step.getParameters(),
                    yamlFile,
                    stepIndex,
                    step.getCommand(),
                    step);

            resolvedSql = resolver.resolve(
                    tc,
                    resolvedSql,
                    tc.getVariables(),
                    yamlFile,
                    stepIndex,
                    step.getCommand());

        } catch (AgateStepException e) {
            throw e;

        } catch (Exception e) {
            throw AgateStepException.builder("SQL command could not be resolved")
                    .detail("Command", abbreviate(sqlCmd, 300))
                    .detail("Technical error", safeMessage(e))
                    .hint("Check placeholders and variables used by the SQL command.")
                    .cause(e)
                    .build();
        }

        if (isBlank(resolvedSql)) {
            throw AgateStepException.builder("SQL command is empty after placeholder resolution")
                    .field("command")
                    .hint("Check the SQL command and all placeholders used by it.")
                    .build();
        }

        resolvedSql = resolvedSql.replace("SYSDATE{NULL}", "SYSDATE").trim();

        if (resolvedSql.endsWith(";")) {
            resolvedSql = resolvedSql.substring(0, resolvedSql.length() - 1).trim();
        }

        String sqlUpper = resolvedSql.toUpperCase();

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose
                && !isBlank(step.getDatasource())) {

            logger.info(ConsoleColors.GREEN
                    + "    >>> DATASOURCE : "
                    + step.getDatasource().trim()
                    + ConsoleColors.RESET);
        }

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            if (resolvedSql.contains("\n") || resolvedSql.contains("\r")) {
                logger.info(ConsoleColors.GREEN
                        + "    >>> SQL EXEC:"
                        + ConsoleColors.RESET);

                String[] sqlLines = resolvedSql.split("\\R");

                for (String line : sqlLines) {
                    if (!line.trim().isEmpty()) {
                        logger.info(ConsoleColors.GREEN
                                + "    >>>          "
                                + line.trim()
                                + ConsoleColors.RESET);
                    }
                }

            } else {
                logger.info(ConsoleColors.GREEN
                        + "    >>> SQL EXEC: "
                        + resolvedSql
                        + ConsoleColors.RESET);
            }
        }

        try {
            if (sqlUpper.startsWith("SELECT")) {

                List<Map<String, Object>> resultTable =
                        DatabaseManager.select(resolvedSql, step.getDatasource());

                if (step.getConstraints() != null
                        && !step.getConstraints().isEmpty()) {

                    resultTable =
                            filterTable(
                                    resultTable,
                                    step.getConstraints());

                    if (Boolean.TRUE.equals(printExecution)
                            && isVerbose) {

                        logger.info(ConsoleColors.GREEN
                                + "    >>> FILTERED: Constraints applied, rows remaining: "
                                + resultTable.size()
                                + ConsoleColors.RESET);
                    }
                }

                if (!isBlank(step.getResponse())) {
                    context.storeBuffer(
                            step.getResponse(),
                            resultTable);
                }

                if (Boolean.TRUE.equals(printExecution)
                        && isVerbose) {

                    SqlTablePrinter.print(
                            resultTable,
                            logger);

                    logger.info(ConsoleColors.GREEN
                            + "    >>> Affected Rows: "
                            + resultTable.size()
                            + ConsoleColors.RESET);
                }

            } else {

                int rowsAffected =
                        DatabaseManager.update(resolvedSql, step.getDatasource());

                if (Boolean.TRUE.equals(printExecution)
                        && isVerbose) {

                    logger.info(ConsoleColors.GREEN
                            + "    >>> Affected Rows: "
                            + rowsAffected
                            + ConsoleColors.RESET);
                }
            }

        } catch (AgateStepException e) {
            throw e;

        } catch (Exception e) {
            throw AgateStepException.builder("SQL execution failed")
                    .detail("Database error", safeMessage(e))
                    .detail("Command", abbreviate(resolvedSql, 500))
                    .hint("Check the SQL syntax, database objects, permissions and environment configuration.")
                    .cause(e)
                    .build();
        }
    }

    private void handleAssertion(
            TestStep step,
            ExecutionContext context,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) {

        String responseKey = step.getResponse();
        String source = normalizeUpper(step.getSource(), "");
        String action = normalizeUpper(step.getAction(), "");
        String expectedRaw = step.getExpected();
        String column = step.getColumn();

        if (isBlank(action)) {
            throw AgateStepException.builder("Required property is missing")
                    .field("action")
                    .hint("SQL ASSERT requires an assertion action.")
                    .build();
        }

        if (!SUPPORTED_ASSERT_ACTIONS.contains(action)) {
            throw AgateStepException.builder("Unsupported SQL assertion action")
                    .actual(action)
                    .hint("Supported actions: "
                            + String.join(", ", SUPPORTED_ASSERT_ACTIONS))
                    .build();
        }

        if (!isBlank(source)
                && !SUPPORTED_ASSERT_SOURCES.contains(source)) {

            throw AgateStepException.builder("Unsupported SQL assertion source")
                    .actual(source)
                    .hint("Supported SQL ASSERT sources: "
                            + String.join(", ", SUPPORTED_ASSERT_SOURCES))
                    .build();
        }

        Integer row = parseRow(step.getRow());

        List<Map<String, Object>> table =
                getTableFromContext(
                        context,
                        responseKey);

        String actualValue;
        boolean passed;

        if ("ROW_COUNT".equals(source)) {

            if (!SUPPORTED_ROW_COUNT_ACTIONS.contains(action)) {
                throw AgateStepException.builder("Unsupported ROW_COUNT assertion action")
                        .actual(action)
                        .detail("Source", source)
                        .hint("Supported ROW_COUNT actions: "
                                + String.join(", ", SUPPORTED_ROW_COUNT_ACTIONS))
                        .build();
            }

            requireExpected(expectedRaw, action);
            validateRowCountExpected(expectedRaw);

            actualValue =
                    String.valueOf(table.size());

            validateExpectedForAction(
                    action,
                    actualValue,
                    expectedRaw);

            passed =
                    evaluateAdvancedComparison(
                            action,
                            actualValue,
                            expectedRaw);

        } else if ("ALL_MATCH".equals(action)
                || "ANY_MATCH".equals(action)) {

            requireColumn(column, action);
            requireExpected(expectedRaw, action);

            final String[] out =
                    new String[1];

            passed =
                    handleCollectionAssertion(
                            table,
                            column,
                            expectedRaw,
                            action,
                            s -> out[0] = s);

            actualValue =
                    out[0];

        } else {

            requireColumn(column, action);

            actualValue =
                    extractCellValueOrThrow(
                            table,
                            row,
                            column);

            String expected =
                    resolveExpectedValue(
                            expectedRaw);

            if (requiresExpected(action)) {
                requireExpected(expectedRaw, action);
            }

            validateExpectedForAction(
                    action,
                    actualValue,
                    expected);

            passed =
                    evaluateAdvancedComparison(
                            action,
                            actualValue,
                            expected);
        }

        if (!passed) {

            AgateStepException.Builder failure =
                    AgateStepException.builder("SQL assertion failed")
                            .expected(displayExpected(action, expectedRaw))
                            .actual(actualValue)
                            .detail("Action", action)
                            .detail("Response", responseKey);

            if (!isBlank(source)) {
                failure.detail("Source", source);
            }

            if (!"ROW_COUNT".equals(source)) {

                if (row != null
                        && !"ALL_MATCH".equals(action)
                        && !"ANY_MATCH".equals(action)) {

                    failure.detail("Row", row);
                }

                if (!isBlank(column)) {
                    failure.detail("Column", column);
                }
            }

            throw failure.build();
        }

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            String msg;

            if ("ROW_COUNT".equals(source)) {

                msg = String.format(
                        "ROW_COUNT -> Actual: %s %s Expected: %s",
                        actualValue,
                        action,
                        expectedRaw);

            } else {

                String columnInfo =
                        column;

                if (column != null
                        && column.trim().matches("\\d+")
                        && !table.isEmpty()) {

                    int idx =
                            Integer.parseInt(column.trim());

                    Object[] keys =
                            table.get(0)
                                    .keySet()
                                    .toArray();

                    if (idx >= 0
                            && idx < keys.length) {

                        columnInfo =
                                String.format(
                                        "%s (%s)",
                                        column.trim(),
                                        keys[idx]);
                    }
                }

                switch (action) {

                    case "ALL_MATCH",
                         "ANY_MATCH" ->
                            msg = String.format(
                                    "[Col:%s] -> %s | %s (Expected: \"%s\")",
                                    columnInfo,
                                    action,
                                    actualValue,
                                    expectedRaw);

                    case "IS_NULL",
                         "IS_NOT_NULL",
                         "IS_EMPTY",
                         "IS_NOT_EMPTY" ->
                            msg = String.format(
                                    "[Row:%d, Col:%s] -> Actual: \"%s\" [%s]",
                                    row,
                                    columnInfo,
                                    actualValue,
                                    action);

                    default ->
                            msg = String.format(
                                    "[Row:%d, Col:%s] -> Actual: \"%s\" %s Expected: \"%s\"",
                                    row,
                                    columnInfo,
                                    actualValue,
                                    action,
                                    expectedRaw);
                }
            }

            logger.info(
                    ConsoleColors.GREEN
                            + "    >>> ASSERT      : "
                            + msg
                            + ConsoleColors.RESET);
        }
    }

    private boolean handleCollectionAssertion(
            List<Map<String, Object>> table,
            String colName,
            String expected,
            String type,
            java.util.function.Consumer<String> actualOut) {

        if (table.isEmpty()) {
            actualOut.accept("Matches: 0/0");
            return false;
        }

        ensureColumnExists(
                table,
                colName);

        int matches = 0;

        for (int i = 0;
             i < table.size();
             i++) {

            String cellVal =
                    extractCellValueOrThrow(
                            table,
                            i,
                            colName);

            if (cellVal != null
                    && cellVal.equals(expected)) {
                matches++;
            }
        }

        actualOut.accept(
                "Matches: "
                        + matches
                        + "/"
                        + table.size());

        return "ALL_MATCH".equals(type)
                ? matches == table.size()
                : matches > 0;
    }

    private void handleBuffer(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) {

        String responseKey =
                step.getResponse();

        if (isBlank(responseKey)) {
            throw AgateStepException.builder("Required property is missing")
                    .field("response")
                    .hint("SQL BUFFER requires the response name of a previous SQL EXEC step.")
                    .build();
        }

        String column =
                step.getColumn();

        if (isBlank(column)) {
            throw AgateStepException.builder("Required property is missing")
                    .field("column")
                    .hint("SQL BUFFER requires a column name or zero-based column index.")
                    .build();
        }

        String name =
                step.getName();

        if (isBlank(name)) {
            throw AgateStepException.builder("Required property is missing")
                    .field("name")
                    .hint("SQL BUFFER requires a target buffer name.")
                    .build();
        }

        Integer row =
                parseRow(step.getRow());

        List<Map<String, Object>> table =
                getTableFromContext(
                        context,
                        responseKey);

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logger.info(
                    ConsoleColors.GREEN
                            + String.format(
                                    "    >>> BUFFER      : [Row:%d, Col:%s] -> [%s]",
                                    row,
                                    column,
                                    name)
                            + ConsoleColors.RESET);
        }

        String result =
                extractCellValueOrThrow(
                        table,
                        row,
                        column);

        tc.addVariable(
                name,
                result);

        context.storeBuffer(
                name,
                result);

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logger.info(
                    ConsoleColors.GREEN
                            + "    <<< OUT         : "
                            + result
                            + ConsoleColors.RESET);
        }
    }

    private boolean evaluateAdvancedComparison(
            String type,
            String actual,
            String expected) {

        return switch (type) {

            case "IS_NULL" ->
                    "null".equals(actual);

            case "IS_NOT_NULL" ->
                    !"null".equals(actual);

            case "IS_EMPTY" ->
                    actual == null
                            || "null".equals(actual)
                            || actual.trim().isEmpty();

            case "IS_NOT_EMPTY" ->
                    !(actual == null
                            || "null".equals(actual)
                            || actual.trim().isEmpty());

            case "EQUALS" ->
                    actual.equals(expected);

            case "NOT_EQUALS" ->
                    !actual.equals(expected);

            case "CONTAINS" ->
                    actual.contains(expected);

            case "GREATER_THAN" ->
                    Double.parseDouble(actual)
                            > Double.parseDouble(expected);

            case "LESS_THAN" ->
                    Double.parseDouble(actual)
                            < Double.parseDouble(expected);

            case "GREATER_THAN_OR_EQUAL" ->
                    Double.parseDouble(actual)
                            >= Double.parseDouble(expected);

            case "LESS_THAN_OR_EQUAL" ->
                    Double.parseDouble(actual)
                            <= Double.parseDouble(expected);

            case "BETWEEN" ->
                    checkBetween(
                            actual,
                            expected);

            case "DATE_EQUALS" ->
                    compareDatesSmart(
                            actual,
                            expected);

            default ->
                    throw AgateStepException.builder("Unsupported SQL assertion action")
                            .actual(type)
                            .hint("Supported actions: "
                                    + String.join(", ", SUPPORTED_ASSERT_ACTIONS))
                            .build();
        };
    }

    private void validateExpectedForAction(
            String action,
            String actual,
            String expected) {

        if (Set.of(
                "GREATER_THAN",
                "LESS_THAN",
                "GREATER_THAN_OR_EQUAL",
                "LESS_THAN_OR_EQUAL")
                .contains(action)) {

            if (!isNumeric(expected)) {
                throw AgateStepException.builder("Invalid numeric expected value")
                        .expected("A numeric value")
                        .actual(expected)
                        .detail("Action", action)
                        .hint("Use a numeric value, for example: 5 or 10.5")
                        .build();
            }

            if (!isNumeric(actual)) {
                throw AgateStepException.builder("SQL value is not numeric")
                        .expected("A numeric SQL value")
                        .actual(actual)
                        .detail("Action", action)
                        .hint("Use a numeric column with this assertion action.")
                        .build();
            }
        }

        if ("BETWEEN".equals(action)) {
            validateBetweenExpression(
                    expected);

            if (!isNumeric(actual)) {
                throw AgateStepException.builder("SQL value is not numeric")
                        .expected("A numeric SQL value")
                        .actual(actual)
                        .detail("Action", action)
                        .hint("BETWEEN can only be used with numeric values.")
                        .build();
            }
        }

        if ("DATE_EQUALS".equals(action)) {
            validateDateExpected(
                    expected);
        }

    }

    private void validateRowCountExpected(
            String expected) {

        if (!isInteger(expected)) {

            throw AgateStepException.builder("Invalid ROW_COUNT expected value")
                    .expected("A non-negative whole number")
                    .actual(expected)
                    .hint("Use a row count such as 0, 1, 2, ...")
                    .build();
        }

        int value =
                Integer.parseInt(
                        expected.trim());

        if (value < 0) {

            throw AgateStepException.builder("Invalid ROW_COUNT expected value")
                    .expected("A non-negative whole number")
                    .actual(expected)
                    .hint("ROW_COUNT cannot be compared with a negative expected row count.")
                    .build();
        }
    }

    private void validateBetweenExpression(
            String expected) {

        if (isBlank(expected)) {
            throw AgateStepException.builder("Invalid BETWEEN expression")
                    .actual(expected)
                    .hint("Use format <min>..<max>, for example: 100..200")
                    .build();
        }

        String[] bounds =
                expected.split("\\.\\.", -1);

        if (bounds.length != 2
                || !isNumeric(bounds[0])
                || !isNumeric(bounds[1])) {

            throw AgateStepException.builder("Invalid BETWEEN expression")
                    .actual(expected)
                    .hint("Use format <min>..<max>, for example: 100..200")
                    .build();
        }

        double min =
                Double.parseDouble(bounds[0]);

        double max =
                Double.parseDouble(bounds[1]);

        if (min > max) {
            throw AgateStepException.builder("Invalid BETWEEN expression")
                    .actual(expected)
                    .hint("The lower bound must not be greater than the upper bound.")
                    .build();
        }
    }

    private boolean checkBetween(
            String actual,
            String expected) {

        String[] bounds =
                expected.split("\\.\\.");

        double val =
                Double.parseDouble(actual);

        double min =
                Double.parseDouble(bounds[0]);

        double max =
                Double.parseDouble(bounds[1]);

        return val >= min
                && val <= max;
    }

    private void validateDateExpected(
            String expected) {

        if (isBlank(expected)) {
            throw AgateStepException.builder("Invalid DATE_EQUALS expected value")
                    .actual(expected)
                    .hint("Use TODAY, YESTERDAY or a supported date format.")
                    .build();
        }

        if ("TODAY".equalsIgnoreCase(expected)
                || "YESTERDAY".equalsIgnoreCase(expected)) {
            return;
        }

        try {
            parseDate(expected);

        } catch (ParseException e) {
            throw AgateStepException.builder("Invalid DATE_EQUALS expected value")
                    .actual(expected)
                    .hint("Use TODAY, YESTERDAY or a date such as 2026-09-11 or 11.09.2026.")
                    .cause(e)
                    .build();
        }
    }

    private boolean compareDatesSmart(
            String actual,
            String expected) {

        try {
            Date actualDate =
                    parseDate(actual.split(" ")[0]);

            Calendar calTarget =
                    Calendar.getInstance();

            if ("YESTERDAY".equalsIgnoreCase(expected)) {
                calTarget.add(
                        Calendar.DATE,
                        -1);

            } else if (!"TODAY".equalsIgnoreCase(expected)) {
                calTarget.setTime(
                        parseDate(expected));
            }

            Calendar calActual =
                    Calendar.getInstance();

            calActual.setTime(
                    actualDate);

            return calActual.get(Calendar.YEAR)
                    == calTarget.get(Calendar.YEAR)
                    && calActual.get(Calendar.DAY_OF_YEAR)
                    == calTarget.get(Calendar.DAY_OF_YEAR);

        } catch (ParseException e) {
            throw AgateStepException.builder("SQL date value could not be parsed")
                    .actual(actual)
                    .hint("Check that the SQL value is a supported date format.")
                    .cause(e)
                    .build();
        }
    }

    private Date parseDate(
            String dateStr) throws ParseException {

        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf =
                        new SimpleDateFormat(format);

                sdf.setLenient(false);

                return sdf.parse(dateStr);

            } catch (ParseException ignored) {
                // try next format
            }
        }

        throw new ParseException(
                "Format error: " + dateStr,
                0);
    }

    private String extractCellValueOrThrow(
            List<Map<String, Object>> table,
            Integer row,
            String column) {

        if (table == null) {
            throw AgateStepException.builder("SQL result table is missing")
                    .hint("Reference a valid response produced by SQL EXEC.")
                    .build();
        }

        if (table.isEmpty()) {
            throw AgateStepException.builder("SQL result contains no rows")
                    .detail("Row", row)
                    .detail("Column", column)
                    .hint("Check the SQL query or constraints before accessing a cell.")
                    .build();
        }

        if (row == null
                || row < 0
                || row >= table.size()) {

            throw AgateStepException.builder("SQL row index is out of range")
                    .detail("Row", row)
                    .detail("Rows available", table.size())
                    .hint("SQL row indexes are zero-based.")
                    .build();
        }

        if (isBlank(column)) {
            throw AgateStepException.builder("Required property is missing")
                    .field("column")
                    .hint("Specify a column name or zero-based column index.")
                    .build();
        }

        Map<String, Object> rowData =
                table.get(row);

        String colTrimmed =
                column.trim();

        if (colTrimmed.matches("\\d+")) {

            int index =
                    Integer.parseInt(colTrimmed);

            Object[] values =
                    rowData.values().toArray();

            if (index < 0
                    || index >= values.length) {

                throw AgateStepException.builder("SQL column index is out of range")
                        .detail("Column", index)
                        .detail("Columns available", values.length)
                        .hint("SQL column indexes are zero-based.")
                        .build();
            }

            Object value =
                    values[index];

            return value != null
                    ? value.toString()
                    : "null";
        }

        for (Map.Entry<String, Object> entry
                : rowData.entrySet()) {

            if (entry.getKey()
                    .equalsIgnoreCase(colTrimmed)) {

                Object value =
                        entry.getValue();

                return value != null
                        ? value.toString()
                        : "null";
            }
        }

        throw AgateStepException.builder("SQL column was not found")
                .detail("Column", colTrimmed)
                .detail("Available columns", String.join(", ", rowData.keySet()))
                .hint("Check the column name or alias returned by the SQL statement.")
                .build();
    }

    private void ensureColumnExists(
            List<Map<String, Object>> table,
            String column) {

        if (table == null
                || table.isEmpty()) {
            return;
        }

        if (isBlank(column)) {
            throw AgateStepException.builder("Required property is missing")
                    .field("column")
                    .build();
        }

        String colTrimmed =
                column.trim();

        if (colTrimmed.matches("\\d+")) {

            int index =
                    Integer.parseInt(colTrimmed);

            int columnCount =
                    table.get(0)
                            .size();

            if (index < 0
                    || index >= columnCount) {

                throw AgateStepException.builder("SQL column index is out of range")
                        .detail("Column", index)
                        .detail("Columns available", columnCount)
                        .hint("SQL column indexes are zero-based.")
                        .build();
            }

            return;
        }

        for (String key
                : table.get(0).keySet()) {

            if (key.equalsIgnoreCase(colTrimmed)) {
                return;
            }
        }

        throw AgateStepException.builder("SQL column was not found")
                .detail("Column", colTrimmed)
                .detail("Available columns",
                        String.join(
                                ", ",
                                table.get(0).keySet()))
                .hint("Check the column name or alias returned by the SQL statement.")
                .build();
    }

    private List<Map<String, Object>> filterTable(
            List<Map<String, Object>> table,
            List<Constraint> constraints) {

        if (constraints == null
                || constraints.isEmpty()) {
            return table;
        }

        if (table == null
                || table.isEmpty()) {
            return table;
        }

        validateConstraints(
                table,
                constraints);

        List<Map<String, Object>> filtered =
                new ArrayList<>();

        for (Map<String, Object> row : table) {

            boolean rowMatchesAllConstraints =
                    true;

            for (Constraint constraint : constraints) {

                String targetCol =
                        constraint.getColumn()
                                .trim();

                String action =
                        constraint.getAction()
                                .trim()
                                .toUpperCase();

                String expected =
                        constraint.getExpected();

                String actualKey =
                        findColumnKey(
                                row,
                                targetCol);

                Object valObj =
                        row.get(actualKey);

                String actual =
                        valObj == null
                                ? "null"
                                : valObj.toString().trim();

                boolean match =
                        switch (action) {

                            case "EQUALS" ->
                                    actual.equalsIgnoreCase(expected);

                            case "NOT_EQUALS" ->
                                    !actual.equalsIgnoreCase(expected);

                            case "CONTAINS" ->
                                    expected != null
                                            && actual.toLowerCase()
                                            .contains(
                                                    expected.toLowerCase());

                            case "IS_NULL" ->
                                    "null".equals(actual);

                            case "IS_NOT_NULL" ->
                                    !"null".equals(actual);

                            default ->
                                    throw AgateStepException.builder("Unsupported SQL constraint action")
                                            .actual(action)
                                            .hint("Supported constraint actions: "
                                                    + String.join(", ", SUPPORTED_CONSTRAINT_ACTIONS))
                                            .build();
                        };

                if (!match) {
                    rowMatchesAllConstraints =
                            false;
                    break;
                }
            }

            if (rowMatchesAllConstraints) {
                filtered.add(row);
            }
        }

        return filtered;
    }

    private void validateConstraints(
            List<Map<String, Object>> table,
            List<Constraint> constraints) {

        Map<String, Object> sampleRow =
                table.get(0);

        for (Constraint constraint : constraints) {

            if (constraint == null) {
                throw AgateStepException.builder("Invalid SQL constraint")
                        .hint("Constraint definition must not be empty.")
                        .build();
            }

            String column =
                    constraint.getColumn();

            String action =
                    constraint.getAction();

            if (isBlank(column)) {
                throw AgateStepException.builder("Required property is missing")
                        .field("constraints.column")
                        .hint("Each SQL constraint requires a column.")
                        .build();
            }

            if (isBlank(action)) {
                throw AgateStepException.builder("Required property is missing")
                        .field("constraints.action")
                        .hint("Each SQL constraint requires an action.")
                        .build();
            }

            String normalizedAction =
                    action.trim().toUpperCase();

            if (!SUPPORTED_CONSTRAINT_ACTIONS.contains(
                    normalizedAction)) {

                throw AgateStepException.builder("Unsupported SQL constraint action")
                        .actual(normalizedAction)
                        .detail("Column", column)
                        .hint("Supported constraint actions: "
                                + String.join(", ", SUPPORTED_CONSTRAINT_ACTIONS))
                        .build();
            }

            if (findColumnKey(
                    sampleRow,
                    column) == null) {

                throw AgateStepException.builder("SQL constraint column was not found")
                        .detail("Column", column)
                        .detail("Available columns",
                                String.join(
                                        ", ",
                                        sampleRow.keySet()))
                        .hint("Check the column name or alias returned by the SQL statement.")
                        .build();
            }

            if (Set.of(
                    "EQUALS",
                    "NOT_EQUALS",
                    "CONTAINS")
                    .contains(normalizedAction)
                    && constraint.getExpected() == null) {

                throw AgateStepException.builder("Required property is missing")
                        .field("constraints.expected")
                        .detail("Column", column)
                        .detail("Action", normalizedAction)
                        .hint("This constraint action requires an expected value.")
                        .build();
            }
        }
    }

    private String findColumnKey(
            Map<String, Object> row,
            String requestedColumn) {

        if (row == null
                || isBlank(requestedColumn)) {
            return null;
        }

        for (String key : row.keySet()) {
            if (key.equalsIgnoreCase(
                    requestedColumn.trim())) {
                return key;
            }
        }

        return null;
    }

    private Integer parseRow(
            String rowValue) {

        if (isBlank(rowValue)) {
            return 0;
        }

        try {
            int row =
                    Integer.parseInt(
                            rowValue.trim());

            if (row < 0) {
                throw AgateStepException.builder("Invalid SQL row index")
                        .actual(row)
                        .hint("SQL row indexes must be zero or greater.")
                        .build();
            }

            return row;

        } catch (NumberFormatException e) {

            throw AgateStepException.builder("Invalid SQL row index")
                    .expected("A whole number")
                    .actual(rowValue)
                    .hint("SQL row indexes are zero-based, for example: 0, 1, 2.")
                    .cause(e)
                    .build();
        }
    }

    private String resolveExpectedValue(
            String expectedRaw) {

        if (expectedRaw == null) {
            return null;
        }

        if (expectedRaw.startsWith("NUMBER:")) {
            return expectedRaw.substring(
                    "NUMBER:".length());
        }

        if (expectedRaw.startsWith("DATE:")) {

            String expr =
                    expectedRaw.substring(
                                    "DATE:".length())
                            .toUpperCase();

            Calendar cal =
                    Calendar.getInstance();

            if (expr.startsWith("SYSDATE")) {

                try {
                    if (expr.contains("-")) {

                        int days =
                                Integer.parseInt(
                                        expr.split("-")[1]);

                        cal.add(
                                Calendar.DATE,
                                -days);

                    } else if (expr.contains("+")) {

                        int days =
                                Integer.parseInt(
                                        expr.split("\\+")[1]);

                        cal.add(
                                Calendar.DATE,
                                days);
                    }

                } catch (Exception e) {

                    throw AgateStepException.builder("Invalid DATE expression")
                            .actual(expectedRaw)
                            .hint("Use DATE:SYSDATE, DATE:SYSDATE-1 or DATE:SYSDATE+1.")
                            .cause(e)
                            .build();
                }
            }

            return new SimpleDateFormat(
                    "yyyy-MM-dd")
                    .format(
                            cal.getTime());
        }

        return expectedRaw;
    }

    private boolean requiresExpected(
            String action) {

        return !Set.of(
                "IS_NULL",
                "IS_NOT_NULL",
                "IS_EMPTY",
                "IS_NOT_EMPTY")
                .contains(action);
    }

    private void requireExpected(
            String expected,
            String action) {

        if (expected == null) {

            throw AgateStepException.builder("Required property is missing")
                    .field("expected")
                    .detail("Action", action)
                    .hint("This SQL assertion action requires an expected value.")
                    .build();
        }
    }

    private void requireColumn(
            String column,
            String action) {

        if (isBlank(column)) {

            throw AgateStepException.builder("Required property is missing")
                    .field("column")
                    .detail("Action", action)
                    .hint("This SQL assertion action requires a column.")
                    .build();
        }
    }

    private String displayExpected(
            String action,
            String expectedRaw) {

        if ("IS_NULL".equals(action)) {
            return "NULL";
        }

        if ("IS_NOT_NULL".equals(action)) {
            return "NOT NULL";
        }

        if ("IS_EMPTY".equals(action)) {
            return "EMPTY";
        }

        if ("IS_NOT_EMPTY".equals(action)) {
            return "NOT EMPTY";
        }

        return expectedRaw;
    }

    private boolean isNumeric(
            String value) {

        if (isBlank(value)) {
            return false;
        }

        try {
            Double.parseDouble(
                    value.trim());

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isInteger(
            String value) {

        if (isBlank(value)) {
            return false;
        }

        try {
            Integer.parseInt(
                    value.trim());

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String normalizeUpper(
            String value,
            String fallback) {

        if (value == null
                || value.isBlank()) {
            return fallback;
        }

        return value.trim()
                .toUpperCase();
    }

    private static boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }

    private static String safeMessage(
            Throwable throwable) {

        if (throwable == null) {
            return "Unknown error";
        }

        String message =
                throwable.getMessage();

        if (message == null
                || message.isBlank()) {
            return throwable.getClass()
                    .getSimpleName();
        }

        return message.trim();
    }

    private static String abbreviate(
            String value,
            int maxLength) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.replaceAll("\\s+", " ")
                        .trim();

        if (normalized.length()
                <= maxLength) {
            return normalized;
        }

        return normalized.substring(
                0,
                Math.max(
                        0,
                        maxLength - 3))
                + "...";
    }
}
