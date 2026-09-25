package at.co.svc.aga.transformator.utils;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

public class ProcessBuffer {

    public static void processBuffer(CleanStep step, String ident) {

        if (step.getValues() == null || step.getValues().isEmpty()) {
            return;
        }

        /*
         * IMPORTANT:
         * Do NOT iterate over getValuesAsMap().
         *
         * Tosca may contain duplicate ExplicitName entries and their order
         * matters. Example:
         *
         *   L_Verify = not-set
         *   L_Verify = {PL[Verify]}
         *
         * Iterating the raw list preserves both assignments and Tosca order.
         */
        for (StepValueDetails details : step.getValues()) {

            if (details == null || details.getName() == null) {
                continue;
            }

            String name = details.getName();
            String mode = safe(details.getActionMode());
            String rawValue = safe(details.getValue());

            String translatedValue =
                    translateValue(rawValue);

            String condition =
                    translateCondition(step.getCondition());

            if ("Input".equalsIgnoreCase(mode)
                    || "Insert".equalsIgnoreCase(mode)) {

                printYamlStep(
                        "BUFFER",
                        "EXEC",
                        name,
                        translatedValue,
                        null,
                        condition,
                        ident
                );

            } else if ("Verify".equalsIgnoreCase(mode)) {

                printYamlStep(
                        "BUFFER",
                        "ASSERT",
                        name,
                        null,
                        translatedValue,
                        condition,
                        ident
                );
            }
        }
    }

    private static void printYamlStep(
            String type,
            String op,
            String name,
            String value,
            String expected,
            String condition,
            String ident) {

        ToscaToAgaPhase1.writeLine("");
        ToscaToAgaPhase1.writeLine(
                ident + "      - type: " + type
        );
        ToscaToAgaPhase1.writeLine(
                ident + "        op: " + op
        );

        if (condition != null && !condition.isBlank()) {

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        condition: "
                            + formatYamlCondition(condition)
            );
        }

        writeYamlScalar(
                ident,
                "name",
                name
        );

        if (value != null) {

            writeYamlScalar(
                    ident,
                    "value",
                    value
            );
        }

        if (expected != null) {

            ToscaToAgaPhase1.writeLine(
                    ident + "        action: EQUALS"
            );

            writeYamlScalar(
                    ident,
                    "expected",
                    expected
            );
        }
    }

    public static void processInlineBuffer(
            CleanStep step,
            String ident) {

        if (step.getValues() == null
                || step.getValues().isEmpty()) {
            return;
        }

        for (StepValueDetails details : step.getValues()) {

            if (details == null
                    || details.getName() == null) {
                continue;
            }

            String value =
                    translateValue(
                            safe(details.getValue())
                    );

            String condition =
                    translateCondition(
                            step.getCondition()
                    );

            ToscaToAgaPhase1.writeLine("");

            ToscaToAgaPhase1.writeLine(
                    ident + "      - type: BUFFER"
            );

            ToscaToAgaPhase1.writeLine(
                    ident + "        op: EXEC"
            );

            writeYamlScalar(
                    ident,
                    "name",
                    details.getName()
            );

            if (condition != null
                    && !condition.isBlank()) {

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "        condition: "
                                + formatYamlCondition(condition)
                );
            }

            writeYamlScalar(
                    ident,
                    "value",
                    value
            );
        }
    }

    private static String translateCondition(
            String condition) {

        if (condition == null
                || condition.isBlank()) {
            return "";
        }

        String translated =
                ToscaValueTranslator
                        .translateToscaValues(
                                condition
                        );

        return ToscaDateFormatTranslator
                .translate(
                        translated
                );
    }

    private static String translateValue(
            String value) {

        String translated =
                ToscaValueTranslator
                        .translateToscaValues(
                                safe(value)
                        );

        /*
         * DATE/DATETIME normalization is deliberately enforced here too.
         *
         * This makes BUFFER migration deterministic even if another caller
         * uses an older SVCToscaTranslator implementation.
         */
        return ToscaDateFormatTranslator
                .translate(
                        translated
                );
    }

    private static void writeYamlScalar(
            String ident,
            String property,
            String value) {

        String v =
                safe(value);

        if (v.contains("\n")
                || v.contains("\r")) {

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        "
                            + property
                            + ": |"
            );

            String normalized =
                    v.replace(
                                    "\r\n",
                                    "\n"
                            )
                            .replace(
                                    "\r",
                                    "\n"
                            );

            for (String line
                    : normalized.split("\n", -1)) {

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "          "
                                + line
                );
            }

            return;
        }

        /*
         * YAML single quotes:
         *
         * Java format
         *   yyyy-MM-dd'T'HH:mm:ss
         *
         * is emitted as
         *   '...yyyy-MM-dd''T''HH:mm:ss...'
         *
         * and YAML reads that back as a single quoted T.
         */
        ToscaToAgaPhase1.writeLine(
                ident
                        + "        "
                        + property
                        + ": '"
                        + v.replace(
                                "'",
                                "''"
                        )
                        + "'"
        );
    }

    /**
     * Formats a condition as a readable YAML scalar.
     *
     * Examples:
     *
     *   BetreuungsStatus == "Aus"
     *
     * becomes:
     *
     *   'BetreuungsStatus == "Aus"'
     *
     *
     * A condition containing single quotes:
     *
     *   'searchAnfragenBasisdaten.SvNummer' != NULL
     *
     * becomes:
     *
     *   "'searchAnfragenBasisdaten.SvNummer' != NULL"
     *
     *
     * If both single and double quotes occur, YAML double quotes are used
     * and the embedded double quotes are escaped.
     */
    private static String formatYamlCondition(
            String condition) {

        String value =
                safe(condition);

        /*
         * Condition contains double quotes but no single quotes.
         *
         * Use single quotes around the YAML scalar so that embedded
         * double quotes remain readable and require no escaping.
         */
        if (value.contains("\"")
                && !value.contains("'")) {

            return "'"
                    + value
                    + "'";
        }

        /*
         * Otherwise use YAML double quotes.
         *
         * This is also suitable for expressions that already contain
         * single quotes.
         */
        return "\""
                + value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                )
                + "\"";
    }

    private static String safe(
            String value) {

        return value == null
                ? ""
                : value;
    }
}