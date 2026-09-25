package at.co.svc.aga.transformator.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

public class ProcessJsonAssert {

    public static String filePath = null;

    public static void processJsonOpenFile(CleanStep step) {

        Map<String, StepValueDetails> values =
                step.getValuesAsMap();

        if (values == null) {
            return;
        }

        for (Map.Entry<String, StepValueDetails> entry
                : values.entrySet()) {

            StepValueDetails details =
                    entry.getValue();

            if (details != null
                    && details.getToscaPath() != null
                    && !details.getToscaPath().isEmpty()
                    && "Filepath".equalsIgnoreCase(
                            details.getToscaPath())) {

                filePath =
                        details.getValue();
            }
        }
    }

    public static void processJsonAssert(CleanStep step) {

        Map<String, StepValueDetails> values =
                step.getValuesAsMap();

        if (values == null) {
            return;
        }

        /*
         * 1. Filter and sort Verify elements so that they follow
         * a natural order through the JSON tree.
         */
        List<StepValueDetails> verifyElements =
                new ArrayList<>();

        for (Map.Entry<String, StepValueDetails> entry
                : values.entrySet()) {

            StepValueDetails details =
                    entry.getValue();

            if (details != null
                    && "Verify".equalsIgnoreCase(
                            details.getActionMode())
                    && details.getToscaPath() != null
                    && !details.getToscaPath().isEmpty()) {

                verifyElements.add(details);
            }
        }

        verifyElements.sort(
                Comparator.comparing(
                        StepValueDetails::getToscaPath
                )
        );

        /*
         * If there are no Verify elements, skip this step.
         *
         * This is a debug message and should not appear
         * in normal logging.
         */
        if (verifyElements.isEmpty()) {

            MigrationLog.debug(
                    "JSON Assert skipped - no Verify elements: "
                            + step.getName()
            );

            return;
        }

        // -----------------------------------------------------
        // AGATE DSL
        // -----------------------------------------------------

        ToscaToAgaPhase1.writeLine(
                "\n      # Assert JSON Response: "
                        + step.getName()
        );

        ToscaToAgaPhase1.writeLine(
                "      - type: JSON"
        );

        ToscaToAgaPhase1.writeLine(
                "        op: ASSERT"
        );

        String cleanCondition = "";

        if (step.getCondition() != null
                && !step.getCondition().trim().isEmpty()) {

            String condition =
                    step.getCondition();

            cleanCondition =
                    ToscaValueTranslator
                            .translateToscaValues(
                                    condition
                            );

            String formattedCondition =
                    cleanCondition.replace(
                            "\"",
                            "'"
                    );

            ToscaToAgaPhase1.writeLine(
                    "        condition: \""
                            + formattedCondition
                            + "\""
            );
        }

        /*
         * The file attribute is optional.
         *
         * Important: check for null before calling trim().
         */
        if (ProcessJsonAssert.filePath != null
                && !ProcessJsonAssert.filePath
                        .trim()
                        .isEmpty()) {

            String filename =
                    ProcessJsonAssert.filePath.trim();

            // Replace forward slashes with backslashes
            filename =
                    filename.replace(
                            "/",
                            "\\"
                    );

            /*
             * Escape backslashes for YAML output.
             */
            filename =
                    filename.replaceAll(
                            "\\\\",
                            "\\\\\\\\"
                    );

            ToscaToAgaPhase1.writeLine(
                    "        file: \""
                            + filename
                            + "\""
            );
        }

        // =====================================================
        // SPECIAL CASE
        // =====================================================

        String targetCondition1 =
                "('true' == 'true') AND "
                        + "('{B[L_Buffer_Selector]}' == 'V1') AND "
                        + "(NOT (('{B[Suchkriterium2]}' == 'BUFFER_NOT_SET')))";

        if (cleanCondition.trim()
                .equals(targetCondition1)) {

            ToscaToAgaPhase1.writeLine(
                    "        parameters:"
            );

            ToscaToAgaPhase1.writeLine(
                    "          path1: \"$.logs[0][1]\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          value1: \"INFO\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          path2: \"$.logs[0][2]\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          value2: \"{R[LogTyp]}\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          path4: \"$.logs[0][9]\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          value4: \"{R[Suchkriterium2]}\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          path5: \"$.logs[0][12]\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          value5: \"{R[MeldungTyp]}\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          path6: \"$.logs[0][13]\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          value6: \"{R[MeldungCode]}\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          path7: \"$.logs[0][14]\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          value7: \"{R[MeldungText]}\""
            );

            return;
        }

        // =====================================================
        // STANDARD ASSERT PARAMETERS
        // =====================================================

        ToscaToAgaPhase1.writeLine(
                "        parameters:"
        );

        int assertCounter = 1;

        for (StepValueDetails details
                : verifyElements) {

            String agatePath =
                    convertToscaToAgate(
                            details.getToscaPath()
                    );

            String agateValue =
                    transformValuePrefix(
                            details.getValue()
                    );

            ToscaToAgaPhase1.writeLine(
                    "          path"
                            + assertCounter
                            + ": \""
                            + agatePath
                            + "\""
            );

            ToscaToAgaPhase1.writeLine(
                    "          value"
                            + assertCounter
                            + ": \""
                            + agateValue
                            + "\""
            );

            assertCounter++;
        }
    }

    /**
     * Converts Tosca path syntax into Agate JSONPath syntax.
     */
    private static String convertToscaToAgate(
            String toscaPath) {

        String path =
                toscaPath.replace(
                        "RootObject",
                        "$"
                );

        Pattern itemPattern =
                Pattern.compile(
                        "\\.item#(\\d+)"
                );

        Matcher itemMatcher =
                itemPattern.matcher(path);

        StringBuilder sb =
                new StringBuilder();

        while (itemMatcher.find()) {

            int toscaIndex =
                    Integer.parseInt(
                            itemMatcher.group(1)
                    );

            int jsonIndex =
                    toscaIndex - 1;

            itemMatcher.appendReplacement(
                    sb,
                    "[" + jsonIndex + "]"
            );
        }

        itemMatcher.appendTail(sb);

        path = sb.toString();

        /*
         * Convert:
         *
         * .mdc.event.code
         *
         * into:
         *
         * .mdc['event.code']
         */
        if (path.contains(".mdc.")) {

            int mdcIndex =
                    path.indexOf(".mdc.");

            String prefix =
                    path.substring(
                            0,
                            mdcIndex + 4
                    );

            String suffix =
                    path.substring(
                            mdcIndex + 5
                    );

            if (suffix.contains(".")) {

                path =
                        prefix
                                + "['"
                                + suffix
                                + "']";
            }
        }

        return path;
    }

    /**
     * Converts Tosca buffer syntax:
     *
     * {PL[Name]}
     *
     * into Agate syntax:
     *
     * {R[Name]}
     */
    private static String transformValuePrefix(
            String value) {

        if (value == null) {
            return "";
        }

        return value.replaceAll(
                "(?i)\\{PL\\[",
                "{R["
        );
    }
}