package at.co.svc.agate.core.dsl.register;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionEvaluator {

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

        String value = clean(m.group(1));

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

        String left = clean(m.group(1));

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

        return s.trim();
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

        String upper = expr.toUpperCase();

        int last = 0;

        for (int i = 0; i < upper.length(); i++) {

            char c = upper.charAt(i);

            if (c == '(') level++;
            if (c == ')') level--;

            if (level == 0) {

                String op = " " + operator + " ";

                if (upper.startsWith(op, i)) {

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