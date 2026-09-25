package at.co.svc.aga.transformator.utils;

import java.util.Map;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

public class ProcessFileStep {

    public static void processCopyFile(
            CleanStep step,
            String ident) {

        if (step == null
                || step.getValues() == null
                || step.getValues().isEmpty()) {
            return;
        }

        Map<String, StepValueDetails> values =
                step.getValuesAsMap();

        String source =
                translate(
                        findValue(
                                values,
                                "Source"
                        )
                );

        String targetDirectory =
                translate(
                        findValue(
                                values,
                                "Target Directory"
                        )
                );

        String targetFilename =
                translate(
                        findValue(
                                values,
                                "Target Filename"
                        )
                );

        String overwriteRaw =
                findValue(
                        values,
                        "Overwrite"
                );

        boolean overwrite =
                "true".equalsIgnoreCase(
                        overwriteRaw.trim()
                );

        if (source.isBlank()) {
            MigrationLog.info(
                    "[Skipped] TBox Copy File - Source is missing"
                            + getStepSuffix(step)
            );
            return;
        }

        if (targetDirectory.isBlank()) {
            MigrationLog.info(
                    "[Skipped] TBox Copy File - Target Directory is missing"
                            + getStepSuffix(step)
            );
            return;
        }

        if (targetFilename.isBlank()) {
            MigrationLog.info(
                    "[Skipped] TBox Copy File - Target Filename is missing"
                            + getStepSuffix(step)
            );
            return;
        }

        String target =
                buildTargetPath(
                        targetDirectory,
                        targetFilename
                );

        ToscaToAgaPhase1.writeLine("");

        ToscaToAgaPhase1.writeLine(
                ident
                        + "      # "
                        + safeStepName(step)
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "      - type: FILE"
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        op: EXEC"
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        action: COPY"
        );

        if (step.getCondition() != null
                && !step.getCondition().isBlank()) {

            String condition =
                    translate(
                            step.getCondition()
                    );

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        condition: \""
                            + escapeDoubleQuoted(condition)
                            + "\""
            );
        }

        writeYamlScalar(
                ident,
                "source",
                source
        );

        writeYamlScalar(
                ident,
                "target",
                target
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        overwrite: "
                        + overwrite
        );
    }


    private static String buildTargetPath(
            String directory,
            String filename) {

        if (directory.endsWith("\\")
                || directory.endsWith("/")) {

            return directory + filename;
        }

        return directory
                + "\\"
                + filename;
    }


    private static String findValue(
            Map<String, StepValueDetails> values,
            String name) {

        if (values == null
                || name == null) {
            return "";
        }

        StepValueDetails details =
                values.get(name);

        if (details == null
                || details.getValue() == null) {
            return "";
        }

        return details.getValue();
    }


    private static String translate(
            String value) {

        if (value == null) {
            return "";
        }

        return ToscaValueTranslator
                .translateToscaValues(value);
    }


    private static void writeYamlScalar(
            String ident,
            String key,
            String value) {

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        "
                        + key
                        + ": \""
                        + escapeDoubleQuoted(value)
                        + "\""
        );
    }


    private static String escapeDoubleQuoted(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


    private static String safeStepName(
            CleanStep step) {

        if (step == null
                || step.getName() == null
                || step.getName().isBlank()) {
            return "TBox Copy File";
        }

        return step.getName();
    }


    private static String getStepSuffix(
            CleanStep step) {

        if (step == null
                || step.getName() == null
                || step.getName().isBlank()) {
            return "";
        }

        return " | step=" + step.getName();
    }
}