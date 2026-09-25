package at.co.svc.aga.transformator.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

/**
 * Migrates executable uses of Tosca "TBox Evaluation Tool".
 *
 * IF-condition usages are handled earlier by:
 *
 *   at.co.svc.tosca.testcases.ProcessTBoxEvaluationTool
 *   + IfPropagationEngine
 *
 * This class is only for Evaluation Tool steps that remain as real
 * executable steps inside THEN/ELSE branches.
 */
public final class ProcessTBoxEvaluationRuntime {

    private static final Pattern BUFFER_PLACEHOLDER =
            Pattern.compile("^\\{B\\[([^]]+)]}$");

    private ProcessTBoxEvaluationRuntime() {
    }

    /**
     * @return true if the step was migrated to AGATE YAML,
     *         false if the expression is unsupported.
     */
    public static boolean processEvaluation(
            CleanStep step,
            String ident) {

        if (step == null
                || step.getValues() == null
                || step.getValues().isEmpty()) {

            MigrationLog.info(
                    "[Review] TBox Evaluation Tool has no values"
                            + stepSuffix(step)
            );

            return false;
        }

        String expression =
                findExpression(step);

        if (expression == null
                || expression.isBlank()) {

            MigrationLog.info(
                    "[Review] TBox Evaluation Tool expression not found"
                            + stepSuffix(step)
            );

            return false;
        }

        ParsedEquality equality =
                parseEquality(expression);

        if (equality == null) {

            MigrationLog.info(
                    "[Review] Unsupported executable TBox Evaluation Tool expression: "
                            + expression
                            + stepSuffix(step)
            );

            return false;
        }

        AssertionMapping mapping =
                mapToBufferAssertion(
                        equality.left(),
                        equality.right()
                );

        if (mapping == null) {

            MigrationLog.info(
                    "[Review] TBox Evaluation Tool expression cannot be mapped "
                            + "safely to BUFFER ASSERT: "
                            + expression
                            + stepSuffix(step)
            );

            return false;
        }

        ToscaToAgaPhase1.writeLine("");

        ToscaToAgaPhase1.writeLine(
                ident
                        + "      # Evaluate: "
                        + safeStepName(step)
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "      - type: BUFFER"
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        op: ASSERT"
        );

        printCondition(
                step,
                ident
        );

        writeYamlString(
                ident,
                "name",
                mapping.bufferName()
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        action: EQUALS"
        );

        writeYamlString(
                ident,
                "expected",
                translate(
                        mapping.expected()
                )
        );

        return true;
    }


    // =========================================================
    // Expression extraction
    // =========================================================

    private static String findExpression(
            CleanStep step) {

        for (StepValueDetails value :
                step.getValues()) {

            if (value == null
                    || value.getValue() == null
                    || value.getValue().isBlank()) {

                continue;
            }

            String candidate =
                    value.getValue().trim();

            if (candidate.contains("==")) {
                return candidate;
            }
        }

        return null;
    }


    // =========================================================
    // Equality parsing
    // =========================================================

    private static ParsedEquality parseEquality(
            String expression) {

        int pos =
                findEqualityOperator(
                        expression
                );

        if (pos < 0) {
            return null;
        }

        String left =
                expression.substring(
                        0,
                        pos
                ).trim();

        String right =
                expression.substring(
                        pos + 2
                ).trim();

        left =
                stripOuterParentheses(
                        left
                );

        right =
                stripOuterParentheses(
                        right
                );

        left =
                stripMatchingQuotes(
                        left
                );

        right =
                stripMatchingQuotes(
                        right
                );

        if (left.isBlank()
                || right.isBlank()) {

            return null;
        }

        return new ParsedEquality(
                left,
                right
        );
    }


    private static int findEqualityOperator(
            String expression) {

        boolean inSingleQuote =
                false;

        boolean inDoubleQuote =
                false;

        int depth =
                0;

        for (int i = 0;
             i < expression.length() - 1;
             i++) {

            char c =
                    expression.charAt(i);

            if (c == '\''
                    && !inDoubleQuote) {

                inSingleQuote =
                        !inSingleQuote;

                continue;
            }

            if (c == '"'
                    && !inSingleQuote) {

                inDoubleQuote =
                        !inDoubleQuote;

                continue;
            }

            if (inSingleQuote
                    || inDoubleQuote) {

                continue;
            }

            if (c == '(') {

                depth++;

                continue;
            }

            if (c == ')') {

                depth =
                        Math.max(
                                0,
                                depth - 1
                        );

                continue;
            }

            if (depth == 0
                    && c == '='
                    && expression.charAt(i + 1) == '=') {

                return i;
            }
        }

        /*
         * Typical Tosca form:
         *
         *   '{B[L_Verify]}' == '{B[SSHResponse]}'
         *
         * The operator remains outside the quoted operands.
         * Fallback protects against unusual parenthesis wrapping.
         */
        return expression.indexOf("==");
    }


    // =========================================================
    // AGATE mapping
    // =========================================================

    private static AssertionMapping mapToBufferAssertion(
            String left,
            String right) {

        String leftBuffer =
                extractBufferName(left);

        String rightBuffer =
                extractBufferName(right);

        /*
         * Current SSH reusable:
         *
         *   '{B[L_Verify]}' == '{B[SSHResponse]}'
         *
         * right side = actual runtime response
         * left side  = expected value
         */
        if (leftBuffer != null
                && rightBuffer != null) {

            return new AssertionMapping(
                    rightBuffer,
                    left
            );
        }

        if (leftBuffer != null) {

            return new AssertionMapping(
                    leftBuffer,
                    right
            );
        }

        if (rightBuffer != null) {

            return new AssertionMapping(
                    rightBuffer,
                    left
            );
        }

        return null;
    }


    private static String extractBufferName(
            String value) {

        if (value == null) {
            return null;
        }

        Matcher matcher =
                BUFFER_PLACEHOLDER.matcher(
                        value.trim()
                );

        if (!matcher.matches()) {
            return null;
        }

        return matcher.group(1);
    }


    // =========================================================
    // YAML
    // =========================================================

    private static void printCondition(
            CleanStep step,
            String ident) {

        if (step.getCondition() == null
                || step.getCondition().isBlank()) {

            return;
        }

        String condition =
                translate(
                        step.getCondition()
                ).replace(
                        "\"",
                        "'"
                );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        condition: \""
                        + condition
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        + "\""
        );
    }


    private static void writeYamlString(
            String ident,
            String property,
            String value) {

        String safe =
                value == null
                        ? ""
                        : value;

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        "
                        + property
                        + ": '"
                        + safe.replace(
                        "'",
                        "''"
                )
                        + "'"
        );
    }


    private static String translate(
            String value) {

        if (value == null) {
            return "";
        }

        return ToscaValueTranslator
                .translateToscaValues(
                        value
                );
    }


    // =========================================================
    // Helpers
    // =========================================================

    private static String stripMatchingQuotes(
            String value) {

        String result =
                value == null
                        ? ""
                        : value.trim();

        if (result.length() >= 2) {

            char first =
                    result.charAt(0);

            char last =
                    result.charAt(
                            result.length() - 1
                    );

            if ((first == '\''
                    && last == '\'')
                    || (first == '"'
                    && last == '"')) {

                return result.substring(
                        1,
                        result.length() - 1
                ).trim();
            }
        }

        return result;
    }


    private static String stripOuterParentheses(
            String value) {

        String result =
                value == null
                        ? ""
                        : value.trim();

        while (isFullyWrapped(result)) {

            result =
                    result.substring(
                            1,
                            result.length() - 1
                    ).trim();
        }

        return result;
    }


    private static boolean isFullyWrapped(
            String value) {

        if (value == null
                || value.length() < 2
                || value.charAt(0) != '('
                || value.charAt(value.length() - 1) != ')') {

            return false;
        }

        boolean inSingleQuote =
                false;

        boolean inDoubleQuote =
                false;

        int depth =
                0;

        for (int i = 0;
             i < value.length();
             i++) {

            char c =
                    value.charAt(i);

            if (c == '\''
                    && !inDoubleQuote) {

                inSingleQuote =
                        !inSingleQuote;

                continue;
            }

            if (c == '"'
                    && !inSingleQuote) {

                inDoubleQuote =
                        !inDoubleQuote;

                continue;
            }

            if (inSingleQuote
                    || inDoubleQuote) {

                continue;
            }

            if (c == '(') {

                depth++;

            } else if (c == ')') {

                depth--;

                if (depth == 0
                        && i < value.length() - 1) {

                    return false;
                }
            }
        }

        return depth == 0;
    }


    private static String safeStepName(
            CleanStep step) {

        return step != null
                && step.getName() != null
                && !step.getName().isBlank()
                ? step.getName()
                : "TBox Evaluation Tool";
    }


    private static String stepSuffix(
            CleanStep step) {

        return step != null
                && step.getName() != null
                ? " - " + step.getName()
                : "";
    }


    private record ParsedEquality(
            String left,
            String right) {
    }


    private record AssertionMapping(
            String bufferName,
            String expected) {
    }
}
