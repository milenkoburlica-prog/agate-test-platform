package at.co.svc.agate.core.dsl.register;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionEvaluator {

    private Map<String, Object> values = Collections.emptyMap();
    private boolean resolveNames;

    public boolean evaluate(String expression, Map<String, Object> runtimeValues) {
        ConditionEvaluator evaluator = new ConditionEvaluator();
        evaluator.values = runtimeValues == null ? Collections.emptyMap() : runtimeValues;
        evaluator.resolveNames = true;
        return evaluator.evaluate(expression);
    }

    public boolean evaluate(String expression) {

        if (expression == null || expression.isBlank()) {
            return false;
        }

        expression = normalize(expression);

        return eval(expression.trim());
    }

    // =========================================================
    // MAIN RECURSIVE EVAL
    // =========================================================

    private boolean eval(String expr) {

        expr = removeOuterBrackets(expr.trim());

        // OR
        List<String> orParts = splitTopLevel(expr, "OR");

        if (orParts.size() > 1) {

            for (String p : orParts) {

                if (eval(p)) {
                    return true;
                }
            }

            return false;
        }

        // AND
        List<String> andParts = splitTopLevel(expr, "AND");

        if (andParts.size() > 1) {

            for (String p : andParts) {

                if (!eval(p)) {
                    return false;
                }
            }

            return true;
        }

        // NOT
        if (expr.toUpperCase().startsWith("NOT ")) {

            String inner = expr.substring(4).trim();

            return !eval(inner);
        }

        // Unary
        Boolean unary = tryUnary(expr);

        if (unary != null) {
            return unary;
        }

        // Numeric comparisons are needed for SQL-driven LOOP/BREAK conditions.
        Boolean numeric = tryNumericComparison(expr);
        if (numeric != null) return numeric;

        // Binary
        Boolean binary = tryBinary(expr);

        if (binary != null) {
            return binary;
        }

        return false;
    }

    // =========================================================
    // UNARY
    // =========================================================

    private Boolean tryUnary(String expr) {

        Pattern p = Pattern.compile(
                "(.+)\\s+(IS_NULL|IS_NOT_NULL|IS_EMPTY|IS_NOT_EMPTY)",
                Pattern.CASE_INSENSITIVE);

        Matcher m = p.matcher(expr);

        if (!m.matches()) {
            return null;
        }

        String value = operand(m.group(1));

        String op = m.group(2).toUpperCase();

        switch (op) {

            case "IS_NULL":
                return value == null;

            case "IS_NOT_NULL":
                return value != null;

            case "IS_EMPTY":
                return value == null || value.trim().isEmpty();

            case "IS_NOT_EMPTY":
                return value != null && !value.trim().isEmpty();
        }

        return false;
    }

    private Boolean tryNumericComparison(String expr) {
        Matcher match = Pattern.compile("^(.+?)\\s*(>=|<=|>|<)\\s*(.+)$").matcher(expr);
        if (!match.matches()) return null;
        try {
            java.math.BigDecimal left = new java.math.BigDecimal(operand(match.group(1)).trim());
            java.math.BigDecimal right = new java.math.BigDecimal(operand(match.group(3)).trim());
            int compare = left.compareTo(right);
            return switch (match.group(2)) {
                case ">" -> compare > 0;
                case "<" -> compare < 0;
                case ">=" -> compare >= 0;
                case "<=" -> compare <= 0;
                default -> false;
            };
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("Numeric condition requires numeric operands: " + expr, ex);
        }
    }

    // =========================================================
    // BINARY
    // =========================================================

    private Boolean tryBinary(String expr) {

        Pattern p = Pattern.compile(
                "(.+?)\\s*(==|=|!=|<>|EQUALS|NOT_EQUALS|CONTAINS)\\s*(.+)",
                Pattern.CASE_INSENSITIVE);

        Matcher m = p.matcher(expr);

        if (!m.matches()) {
            return null;
        }

        String left = operand(m.group(1));

        String op = m.group(2).toUpperCase();

        String right = clean(m.group(3));

        switch (op) {

            case "==":
            case "=":
            case "EQUALS":
                return Objects.equals(left, right);

            case "!=":
            case "<>":
            case "NOT_EQUALS":
                return !Objects.equals(left, right);

            case "CONTAINS":
                return left != null && left.contains(right);
        }

        return false;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private String operand(String raw) {
        String token = removeOuterBrackets(raw.trim());
        boolean quoted = (token.startsWith("'") && token.endsWith("'"))
                || (token.startsWith("\"") && token.endsWith("\""));
        if (resolveNames && !quoted && token.matches("[\\p{L}_][\\p{L}\\p{N}_.]*")
                && !Set.of("NULL", "TRUE", "FALSE").contains(token.toUpperCase(Locale.ROOT))) {
            Object value = values.get(token);
            return value == null ? null : clean(value.toString());
        }
        return clean(token);
    }

    private String normalize(String s) {

        return s
                .replace("&&", " AND ")
                .replace("||", " OR ");
    }

    private String clean(String s) {

        s = removeOuterBrackets(s.trim());

        if (s.startsWith("'") && s.endsWith("'")) {
            s = s.substring(1, s.length() - 1);
        }

        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }

        s = s.trim();
        return s.equalsIgnoreCase("NULL") || s.equals("{NULL}") ? null : s;
    }

    private String removeOuterBrackets(String s) {

        while (s.startsWith("(") && s.endsWith(")")) {

            int level = 0;

            boolean valid = true;

            for (int i = 0; i < s.length(); i++) {

                char c = s.charAt(i);

                if (c == '(') level++;
                if (c == ')') level--;

                if (level == 0 && i < s.length() - 1) {
                    valid = false;
                    break;
                }
            }

            if (!valid) {
                break;
            }

            s = s.substring(1, s.length() - 1).trim();
        }

        return s;
    }

    private List<String> splitTopLevel(String expr, String operator) {

        List<String> parts = new ArrayList<>();

        int level = 0;

        // Do not uppercase the whole expression: Unicode case conversion may change
        // its length (e.g. Straße -> STRASSE) and corrupt character offsets.

        int last = 0;

        for (int i = 0; i < expr.length(); i++) {

            char c = expr.charAt(i);

            if (c == '(') level++;
            if (c == ')') level--;

            if (level == 0) {

                String op = " " + operator + " ";

                if (expr.regionMatches(true, i, op, 0, op.length())) {

                    parts.add(expr.substring(last, i).trim());

                    last = i + op.length();
                }
            }
        }

        if (last == 0) {
            parts.add(expr);
        } else {
            parts.add(expr.substring(last).trim());
        }

        return parts;
    }
}