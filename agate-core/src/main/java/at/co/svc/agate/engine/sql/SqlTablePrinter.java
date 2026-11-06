package at.co.svc.agate.engine.sql;

import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.interfaces.TestLogger;

import java.util.*;

/**
 * Utility class to format and print SQL result sets as ASCII tables.
 * Each output line is prefixed with '<<<' to denote system response.
 */
public class SqlTablePrinter {

    private static final int MAX_COLUMN_WIDTH = 50;
    private static final int MAX_ROWS_TO_PRINT = 10;
    private static final String INDENT = "    "; // 4 spaces for alignment
    private static final String OUT_PREFIX = ConsoleColors.GREEN + "<<< " + ConsoleColors.RESET;

    /**
     * Prints the table using a custom TestLogger.
     */
    public static void print(List<Map<String, Object>> data, TestLogger logger) {
        print(data, logger, "H"); 
    }

    public static void print(List<Map<String, Object>> data, TestLogger logger, String layout) {
        if (data == null || data.isEmpty()) {
            logger.info(INDENT + OUT_PREFIX + "[EMPTY RESULT SET]");
            return;
        }

        StringBuilder sb = new StringBuilder();
        int totalRows = data.size();
        List<Map<String, Object>> rowsToPrint = data.subList(0, Math.min(totalRows, MAX_ROWS_TO_PRINT));

        if ("V".equalsIgnoreCase(layout)) {
            appendVertical(sb, rowsToPrint);
        } else {
            appendHorizontal(sb, rowsToPrint);
        }

        if (totalRows > MAX_ROWS_TO_PRINT) {
            sb.append(String.format("%s%s[!] Shown only first %d of %d rows.%n", INDENT, OUT_PREFIX, MAX_ROWS_TO_PRINT, totalRows));
        } else {
            sb.append(String.format("%s%s[Total Rows: %d]%n", INDENT, OUT_PREFIX, totalRows));
        }

        logger.info(sb.toString());
    }
    
    /**
     * Prints the table directly to System.out (legacy support).
     */
    public static void print(List<Map<String, Object>> data) {
        System.out.print(asText(data));
    }

    /**
     * Returns the table as a String with proper indentation and response prefixes.
     */
    public static String asText(List<Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder();
        
        if (data == null || data.isEmpty()) {
            return INDENT + OUT_PREFIX + "[EMPTY RESULT SET]\n";
        }

        int totalRows = data.size();
        List<Map<String, Object>> rowsToPrint = data.subList(0, Math.min(totalRows, MAX_ROWS_TO_PRINT));

        if (data.get(0).size() > 5) {
            appendVertical(sb, rowsToPrint);
        } else {
            appendHorizontal(sb, rowsToPrint);
        }

        if (totalRows > MAX_ROWS_TO_PRINT) {
            sb.append(String.format("%s%s[!] Shown only first %d of %d rows.%n", INDENT, OUT_PREFIX, MAX_ROWS_TO_PRINT, totalRows));
        } else {
            sb.append(String.format("%s%s[Total Rows: %d]%n", INDENT, OUT_PREFIX, totalRows));
        }

        return sb.toString();
    }

    /**
     * Renders data in a standard grid-like table format.
     */
    private static void appendHorizontal(StringBuilder sb, List<Map<String, Object>> data) {
        Set<String> columnNames = data.get(0).keySet();
        Map<String, Integer> columnWidths = calculateWidths(columnNames, data);
        String separator = createSeparator(columnNames, columnWidths);

        sb.append(INDENT).append(OUT_PREFIX).append(separator).append("\n");
        sb.append(INDENT).append(OUT_PREFIX).append("| ");
        for (String col : columnNames) {
            sb.append(String.format("%-" + columnWidths.get(col) + "s | ", truncate(col, columnWidths.get(col))));
        }
        sb.append("\n").append(INDENT).append(OUT_PREFIX).append(separator).append("\n");

        for (Map<String, Object> row : data) {
            sb.append(INDENT).append(OUT_PREFIX).append("| ");
            for (String col : columnNames) {
                String val = formatValue(row.get(col));
                sb.append(String.format("%-" + columnWidths.get(col) + "s | ", truncate(val, columnWidths.get(col))));
            }
            sb.append("\n");
        }
        sb.append(INDENT).append(OUT_PREFIX).append(separator).append("\n");
    }

    /**
     * Renders data in a key: value list format (useful for wide tables).
     */
    private static void appendVertical(StringBuilder sb, List<Map<String, Object>> data) {
        int rowNum = 1;
        for (Map<String, Object> row : data) {
            sb.append(String.format("%s%s--- Row %d ---%n", INDENT, OUT_PREFIX, rowNum++));
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                sb.append(String.format("%s%s%-15s : %s%n", INDENT, OUT_PREFIX, entry.getKey(), formatValue(entry.getValue())));
            }
        }
    }

    /**
     * Determines optimal column widths based on headers and cell content.
     */
    private static Map<String, Integer> calculateWidths(Set<String> columnNames, List<Map<String, Object>> data) {
        Map<String, Integer> columnWidths = new HashMap<>();
        for (String col : columnNames) {
            int maxWidth = col.length();
            for (Map<String, Object> row : data) {
                String val = formatValue(row.get(col));
                if (val.length() > maxWidth) maxWidth = val.length();
            }
            columnWidths.put(col, Math.min(maxWidth, MAX_COLUMN_WIDTH));
        }
        return columnWidths;
    }

    /**
     * Creates the table border string (e.g., +-----+-------+).
     */
    private static String createSeparator(Set<String> columnNames, Map<String, Integer> widths) {
        StringBuilder sep = new StringBuilder("+");
        for (String col : columnNames) {
            sep.append("-".repeat(widths.get(col) + 2)).append("+");
        }
        return sep.toString();
    }

    /**
     * Handles null values and removes line breaks for clean console display.
     */
    private static String formatValue(Object v) {
        if (v == null) return "null";
        return v.toString().replace("\n", " ").replace("\r", " ");
    }

    /**
     * Truncates text with '...' if it exceeds maximum column width.
     */
    private static String truncate(String text, int width) {
        if (text.length() <= width) return text;
        return text.substring(0, Math.max(0, width - 3)) + "...";
    }
}