package at.co.svc.agate.engine.sql;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import at.co.svc.agate.core.dsl.model.Constraint;
import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.interfaces.TestLogger;

/**
 * Enhanced SQL Engine aligned with standard framework output format.
 */
public class SqlEngine extends AbstractStepEngine {

    private static final String[] DATE_FORMATS = { "yyyy-MM-dd", "dd.MM.yyyy", "MM/dd/yyyy", "yyyy/MM/dd",
            "dd-MM-yyyy" };

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getTableFromContext(ExecutionContext context, String key) {
        Object data = context.getResponse(key, Object.class);
        if (data == null) return null;
        if (data instanceof List) {
            return (List<Map<String, Object>>) data;
        }
        throw new RuntimeException("Buffer '" + key + "' does not contain a valid SQL result table (Found: " + data.getClass().getSimpleName() + ")");
    }

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.SQL;
    }

    @Override
    public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(logger, step);
        }

        String op = (step.getOp() != null) ? step.getOp().toUpperCase() : "EXEC";
        
        try {
            switch (op) {
                case "ASSERT" -> handleAssertion(step, context, stepIndex, printExecution, logger, isVerbose);
                case "BUFFER" -> handleBuffer(tc, step, context, stepIndex, printExecution, logger, isVerbose);
                default        -> handleExecution(tc, step, context, yamlFile, stepIndex, printExecution, logger, isVerbose);
            }

        } catch (Exception e) {
            logger.info(String.format("    %s>>> %s STEP FAILED | ERROR: %s%s", ConsoleColors.RED, step.getType(), e.getMessage(), ConsoleColors.RESET));
            throw e;
        }
    }
    
    private void handleExecution(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {
        
        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();
        String sqlCmd = step.getCommand();
        String resolvedSql = resolver.resolve(tc, sqlCmd, step.getParameters(), yamlFile, stepIndex, step.getCommand(), step);
        resolvedSql = resolver.resolve(tc, resolvedSql, tc.getVariables(), yamlFile, stepIndex, step.getCommand());
        resolvedSql = resolvedSql.replace("SYSDATE{NULL}", "SYSDATE");
        String sqlUpper = resolvedSql.trim().toUpperCase();
        
        resolvedSql = resolvedSql.trim();

        if (resolvedSql.endsWith(";")) {
            resolvedSql = resolvedSql.substring(0, resolvedSql.length() - 1);
        }
        
        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            if (resolvedSql.contains("\n") || resolvedSql.contains("\r")) {
                logger.info(ConsoleColors.GREEN + "    >>> SQL EXEC:" + ConsoleColors.RESET);
                String[] sqlLines = resolvedSql.split("\\R");
                for (String line : sqlLines) {
                    if (!line.trim().isEmpty()) {
                        logger.info(ConsoleColors.GREEN + "    >>>          " + line.trim() + ConsoleColors.RESET);
                    }
                }
            } else {
                logger.info(ConsoleColors.GREEN + "    >>> SQL EXEC: " + resolvedSql + ConsoleColors.RESET);
            }
        }
        
        try {
            if (sqlUpper.startsWith("SELECT")) {
                List<Map<String, Object>> resultTable = DatabaseManager.select(resolvedSql);
                
                if (step.getConstraints() != null && !step.getConstraints().isEmpty()) {
                    resultTable = filterTable(resultTable, step.getConstraints());
                    if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                        logger.info(ConsoleColors.GREEN + "    >>> FILTERED: Constraints applied, rows remaining: " + resultTable.size() + ConsoleColors.RESET);
                    }
                }
                
                if (step.getResponse() != null)
                    context.storeBuffer(step.getResponse(), resultTable);

                if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                    SqlTablePrinter.print(resultTable, logger);
                    logger.info(ConsoleColors.GREEN + "    >>> Affected Rows: " + resultTable.size() + ConsoleColors.RESET);
                }
            } else {
                int rowsAffected = DatabaseManager.update(resolvedSql);

                if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                    logger.info(ConsoleColors.GREEN + "    >>> Affected Rows: " + rowsAffected + ConsoleColors.RESET);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("SQL Execution Failed: " + e.getMessage());
        }
    }

    private void handleAssertion(TestStep step, ExecutionContext context, int stepIndex, Boolean printExecution,
            TestLogger logger, boolean isVerbose) throws Exception {
        
        String responseKey = step.getResponse();
        String action = (step.getAction() != null) ? step.getAction().toUpperCase() : "";
        String expectedRaw = step.getExpected();
        String column = step.getColumn();
        
        Integer row = 0;
        try {
            row = (step.getRow() != null) ? Integer.parseInt(step.getRow()) : 0;
        } catch (NumberFormatException e) {
            throw new RuntimeException("SQL Assertion Failed: Invalid row format '" + step.getRow() + "'");
        }

        List<Map<String, Object>> table = getTableFromContext(context, responseKey);
        if (table == null) {
            throw new RuntimeException("No SQL response found for: " + responseKey);
        }

        boolean passed;
        String actualValue;

        if ("ROW_COUNT".equals(action)) {
            actualValue = String.valueOf(table.size());
            passed = evaluateAdvancedComparison("ROW_COUNT", actualValue, expectedRaw);
        } else if ("ALL_MATCH".equals(action) || "ANY_MATCH".equals(action)) {
            final String[] out = new String[1];
            passed = handleCollectionAssertion(table, row, column, expectedRaw, action, s -> out[0] = s);
            actualValue = out[0]; 
        } else {
            if (column == null) throw new RuntimeException("Column must be defined for action: " + action);
            actualValue = extractCellValue(table, row, column);
            String expected = expectedRaw;

            if (expectedRaw != null) {
                if (expectedRaw.startsWith("NUMBER:")) {
                    expected = expectedRaw.substring("NUMBER:".length());
                } else if (expectedRaw.startsWith("DATE:")) {
                    String expr = expectedRaw.substring("DATE:".length()).toUpperCase();
                    Calendar cal = Calendar.getInstance();
                    if (expr.startsWith("SYSDATE")) {
                        if (expr.contains("-")) {
                            int days = Integer.parseInt(expr.split("-")[1]);
                            cal.add(Calendar.DATE, -days);
                        } else if (expr.contains("+")) {
                            int days = Integer.parseInt(expr.split("\\+")[1]);
                            cal.add(Calendar.DATE, days);
                        }
                    }
                    expected = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
                }
            }
            
            passed = evaluateAdvancedComparison(action, actualValue, expected);
        }

        if (!passed) {
            throw new RuntimeException(String.format("SQL Assertion [%s] failed! Expected: %s, Actual: %s", 
                                                    action, expectedRaw, actualValue));
        }

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            String msg;
            
            String columnInfo = column;
            if (column != null && column.trim().matches("\\d+") && !table.isEmpty()) {
                int idx = Integer.parseInt(column.trim());
                Object[] keys = table.get(0).keySet().toArray();
                if (idx >= 0 && idx < keys.length) {
                    columnInfo = String.format("%s (%s)", column.trim(), keys[idx]);
                }
            }

            switch (action) {
                case "ROW_COUNT":
                    msg = String.format("ROW_COUNT -> Actual: %s %s Expected: %s", actualValue, action, expectedRaw);
                    break;
                case "ALL_MATCH":
                case "ANY_MATCH":
                    msg = String.format("[Col:%s] -> %s | %s (Expected: \"%s\")", columnInfo, action, actualValue, expectedRaw);
                    break;
                case "IS_NULL":
                case "IS_NOT_NULL":
                case "IS_EMPTY":
                case "IS_NOT_EMPTY":
                    msg = String.format("[Row:%d, Col:%s] -> Actual: \"%s\" [%s]", 
                            row, columnInfo, actualValue, action);
                    break;
                default:
                    msg = String.format("[Row:%d, Col:%s] -> Actual: \"%s\" %s Expected: \"%s\"", 
                            row, columnInfo, actualValue, action, expectedRaw);
                    break;
            }
            
            logger.info(ConsoleColors.GREEN + "    >>> ASSERT      : " + msg + ConsoleColors.RESET);
        }
    }    
    
    private boolean handleCollectionAssertion(List<Map<String, Object>> table, Integer row, String colName,
            String expected, String type, java.util.function.Consumer<String> actualOut) {
        if (table.isEmpty()) {
            actualOut.accept("Empty Table");
            return false;
        }

        int matches = 0;
        for (int i = 0; i < table.size(); i++) {
            String cellVal = extractCellValue(table, i, colName);
            if (cellVal != null && cellVal.equals(expected))
                matches++;
        }

        actualOut.accept("Matches: " + matches + "/" + table.size());
        return type.equals("ALL_MATCH") ? (matches == table.size()) : (matches > 0);
    }

    private void handleBuffer(TestCase tc, TestStep step, ExecutionContext context, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) {

        String responseKey = step.getResponse();
        List<Map<String, Object>> table = getTableFromContext(context, responseKey);

        if (table == null) {
            throw new RuntimeException("SQL Buffer Failed: No data found for key: " + responseKey);
        }

        String column = step.getColumn();
        Integer row = (step.getRow() != null) ? Integer.parseInt(step.getRow()) : 0;
        String name = step.getName();

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logger.info(ConsoleColors.GREEN + String.format("    >>> BUFFER      : [Row:%d, Col:%s] -> [%s]", 
                        row, column, name) + ConsoleColors.RESET);
        }

        String result = extractCellValue(table, row, column);
        
        if (result.startsWith("ERR:")) {
            throw new RuntimeException("SQL BUFFER FAIL: " + result);
        }

        tc.addVariable(name, result);
        context.storeBuffer(name, result);
        
        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logger.info(ConsoleColors.GREEN + "    <<< OUT         : " + result + ConsoleColors.RESET);
        }
    }
    
    private boolean evaluateAdvancedComparison(String type, String actual, String expected) {
        if (actual.startsWith("ERR:"))
            return false;
        return switch (type) {
        case "IS_NULL" -> actual.equals("null");
        case "IS_NOT_NULL" -> !actual.equals("null");
        case "ROW_COUNT" -> actual.equals(expected);
        case "EQUALS" -> actual.equals(expected);
        case "GREATER_THAN" -> {
            try {
                yield Double.parseDouble(actual) > Double.parseDouble(expected);
            } catch (Exception e) {
                yield false;
            }
        }
        case "LESS_THAN" -> {
            try {
                yield Double.parseDouble(actual) < Double.parseDouble(expected);
            } catch (Exception e) {
                yield false;
            }
        }
        case "GREATER_THAN_OR_EQUAL" -> {
            try {
                yield Double.parseDouble(actual) >= Double.parseDouble(expected);
            } catch (Exception e) {
                yield false;
            }
        }
        case "LESS_THAN_OR_EQUAL" -> {
            try {
                yield Double.parseDouble(actual) <= Double.parseDouble(expected);
            } catch (Exception e) {
                yield false;
            }
        }
        case "IS_EMPTY" -> actual == null || actual.equals("null") || actual.trim().isEmpty();
        case "IS_NOT_EMPTY" -> !(actual == null || actual.equals("null") || actual.trim().isEmpty());
        case "NOT_EQUALS" -> !actual.equals(expected);
        case "CONTAINS" -> actual.contains(expected);
        case "BETWEEN" -> checkBetween(actual, expected);
        case "DATE_EQUALS" -> compareDatesSmart(actual, expected);
        default -> false;
        };
    }

    private boolean checkBetween(String actual, String expected) {
        try {
            String[] bounds = expected.split("\\.\\.");
            double val = Double.parseDouble(actual);
            return val >= Double.parseDouble(bounds[0]) && val <= Double.parseDouble(bounds[1]);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean compareDatesSmart(String actual, String expected) {
        try {
            Date actualDate = parseDate(actual.split(" ")[0]);
            Calendar calTarget = Calendar.getInstance();
            if (expected.equalsIgnoreCase("YESTERDAY"))
                calTarget.add(Calendar.DATE, -1);
            else if (!expected.equalsIgnoreCase("TODAY"))
                calTarget.setTime(parseDate(expected));

            Calendar calActual = Calendar.getInstance();
            calActual.setTime(actualDate);
            return calActual.get(Calendar.YEAR) == calTarget.get(Calendar.YEAR)
                    && calActual.get(Calendar.DAY_OF_YEAR) == calTarget.get(Calendar.DAY_OF_YEAR);
        } catch (Exception e) {
            return false;
        }
    }

    private Date parseDate(String dateStr) throws ParseException {
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return sdf.parse(dateStr);
            } catch (ParseException ignored) {
            }
        }
        throw new ParseException("Format error: " + dateStr, 0);
    }

    private String extractCellValue(List<Map<String, Object>> table, Integer row, String column) {
        if (table == null || table.isEmpty()) {
            return "ERR:EMPTY";
        }
        if (row == null || row < 0 || row >= table.size()) {
            return "ERR:BOUNDS";
        }
        if (column == null || column.trim().isEmpty()) {
            return "ERR:INVALID_COLUMN";
        }

        Map<String, Object> rowData = table.get(row);
        String colTrimmed = column.trim();

        if (colTrimmed.matches("\\d+")) {
            int index = Integer.parseInt(colTrimmed);
            Object[] values = rowData.values().toArray();

            if (index >= 0 && index < values.length) {
                Object value = values[index];
                return value != null ? value.toString() : "null";
            }
            return "ERR:INDEX_OUT_OF_RANGE";
        }

        for (Map.Entry<String, Object> entry : rowData.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(colTrimmed)) {
                Object value = entry.getValue();
                return value != null ? value.toString() : "null";
            }
        }
        return "ERR:NOT_FOUND";
    }
    
    private List<Map<String, Object>> filterTable(List<Map<String, Object>> table, List<Constraint> constraints) {
        if (constraints == null || constraints.isEmpty()) return table;
        
        List<Map<String, Object>> filtered = new ArrayList<>();
        
        for (Map<String, Object> row : table) {
            boolean rowMatchesAllConstraints = true;
            
            for (Constraint constraint : constraints) {
                String targetCol = constraint.getColumn().trim();
                String action = constraint.getAction().toUpperCase();
                String expected = constraint.getExpected();
                
                String actualKey = null;
                for (String key : row.keySet()) {
                    if (key.equalsIgnoreCase(targetCol)) {
                        actualKey = key;
                        break;
                    }
                }
                
                if (actualKey == null) {
                    rowMatchesAllConstraints = false;
                    break;
                }
                
                Object valObj = row.get(actualKey);
                String actual = (valObj == null) ? "null" : valObj.toString().trim();
                
                boolean match = false;
                switch (action) {
                    case "EQUALS":       match = actual.equalsIgnoreCase(expected); break;
                    case "NOT_EQUALS":   match = !actual.equalsIgnoreCase(expected); break;
                    case "CONTAINS":     match = actual.toLowerCase().contains(expected.toLowerCase()); break;
                    case "IS_NULL":      match = actual.equals("null"); break;
                    case "IS_NOT_NULL":  match = !actual.equals("null"); break;
                    default:             match = true;
                }
                
                if (!match) {
                    rowMatchesAllConstraints = false;
                    break;
                }
            }
            
            if (rowMatchesAllConstraints) {
                filtered.add(row);
            }
        }
        
        return filtered;
    }
}