package at.co.svc.tosca.testcases;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Locale;

/**
 * Extracts the predicate represented by a Tosca "TBox Evaluation Tool"
 * step when that step is used inside an IF condition folder.
 *
 * Important:
 * This class does NOT generate AGATE YAML.
 *
 * Its result is consumed by IfPropagationEngine:
 *
 *   Evaluation Tool expression
 *          -> IF predicate
 *          -> THEN  = predicate
 *          -> ELSE  = NOT(predicate)
 *
 * After successful extraction, IfPropagationEngine may remove the
 * original condition-step from the flattened output.
 */
public final class ProcessTBoxEvaluationTool {

    private static final String MODULE_NAME =
            "TBox Evaluation Tool";

    /*
     * Tosca numeric ActionMode observed for Evaluation Tool expressions.
     * We do not rely exclusively on this value because exports may already
     * contain translated action-mode names.
     */
    private static final String EVALUATION_ACTION_MODE =
            "69";

    private ProcessTBoxEvaluationTool() {
    }


    // =========================================================
    // DETECTION
    // =========================================================

    public static boolean isEvaluationTool(
            JsonNode stepNode) {

        if (stepNode == null
                || !"XTestStep".equals(
                stepNode.path("ObjectClass")
                        .asText())) {

            return false;
        }

        String stepName =
                stepNode.path("Attributes")
                        .path("Name")
                        .asText("");

        if (MODULE_NAME.equalsIgnoreCase(
                stepName.trim())) {

            return true;
        }

        /*
         * Fallback for already transformed/intermediate JSON where
         * the visible step name may differ but the Evaluation Tool
         * ActionMode is still preserved.
         */
        for (JsonNode parameter :
                stepNode.path("Parameters")) {

            String mode =
                    parameter.path("ActionMode")
                            .asText("")
                            .trim();

            if (EVALUATION_ACTION_MODE.equals(
                    mode)) {

                return true;
            }
        }

        return false;
    }


    // =========================================================
    // CONDITION EXTRACTION
    // =========================================================

    public static String extractCondition(
            JsonNode stepNode) {

        if (!isEvaluationTool(
                stepNode)) {

            return null;
        }

        for (JsonNode parameter :
                stepNode.path("Parameters")) {

            String value =
                    parameter.path("Value")
                            .asText("")
                            .trim();

            if (value.isEmpty()) {
                continue;
            }

            if (looksLikeBooleanExpression(
                    value)) {

                return normalizeExpression(
                        value
                );
            }
        }

        return null;
    }


    // =========================================================
    // EXPRESSION SUPPORT
    // =========================================================

    private static boolean looksLikeBooleanExpression(
            String value) {

        String v =
                value == null
                        ? ""
                        : value.trim();

        if (v.isEmpty()) {
            return false;
        }

        String upper =
                v.toUpperCase(
                        Locale.ROOT
                );

        return v.contains("==")
                || v.contains("!=")
                || v.contains(">=")
                || v.contains("<=")
                || containsStandaloneGreaterThan(v)
                || containsStandaloneLessThan(v)
                || v.contains("&&")
                || v.contains("||")
                || upper.contains(" AND ")
                || upper.contains(" OR ")
                || upper.startsWith("NOT ");
    }


    private static String normalizeExpression(
            String value) {

        if (value == null) {
            return "";
        }

        return value.trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }


    private static boolean containsStandaloneGreaterThan(
            String value) {

        for (int i = 0;
             i < value.length();
             i++) {

            if (value.charAt(i) == '>') {

                if (i == 0
                        || value.charAt(i - 1) != '=') {

                    return true;
                }
            }
        }

        return false;
    }


    private static boolean containsStandaloneLessThan(
            String value) {

        for (int i = 0;
             i < value.length();
             i++) {

            if (value.charAt(i) == '<') {

                if (i == 0
                        || value.charAt(i - 1) != '=') {

                    return true;
                }
            }
        }

        return false;
    }
}
