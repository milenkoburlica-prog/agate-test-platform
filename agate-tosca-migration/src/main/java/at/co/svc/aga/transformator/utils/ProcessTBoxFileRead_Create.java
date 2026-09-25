package at.co.svc.aga.transformator.utils;

import java.util.Map;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

public class ProcessTBoxFileRead_Create {

    private static int responseCounter = 0;

    public static void processReadCreate(
            CleanStep step,
            String ident) {

        Map<String, StepValueDetails> values =
                step.getValuesAsMap();

        if (values == null || values.isEmpty()) {
            return;
        }

        StepValueDetails directoryDetails =
                getDetailsIgnoreCase(values, "Directory");

        StepValueDetails fileDetails =
                getDetailsIgnoreCase(values, "File");

        StepValueDetails textDetails =
                getDetailsIgnoreCase(values, "Text");

        if (textDetails == null) {

            MigrationLog.info(
                    "[Skipped] TBox Read/Create File - "
                            + "no Text attribute found"
                            + getStepSuffix(step)
            );

            return;
        }

        String directory =
                translate(getValue(directoryDetails));

        String file =
                translate(getValue(fileDetails));

        String text =
                translate(getValue(textDetails));

        String actionMode =
                textDetails.getActionMode() != null
                        ? textDetails.getActionMode()
                        : "";

        String path =
                buildPath(directory, file);

        if ("Input".equalsIgnoreCase(actionMode)
                || "Insert".equalsIgnoreCase(actionMode)) {

            processWrite(
                    step,
                    values,
                    ident,
                    path,
                    text
            );

        } else if ("Buffer".equalsIgnoreCase(actionMode)) {

            processBuffer(
                    step,
                    values,
                    ident,
                    path,
                    text
            );

        } else if ("Verify".equalsIgnoreCase(actionMode)) {

            processVerify(
                    step,
                    values,
                    ident,
                    path,
                    text
            );

        } else {

            MigrationLog.info(
                    "[Skipped] TBox Read/Create File - "
                            + "unsupported Text ActionMode '"
                            + actionMode
                            + "'"
                            + getStepSuffix(step)
            );
        }
    }

    // =========================================================
    // WRITE
    // =========================================================

    private static void processWrite(
            CleanStep step,
            Map<String, StepValueDetails> values,
            String ident,
            String path,
            String text) {

        ToscaToAgaPhase1.writeLine("");
        ToscaToAgaPhase1.writeLine(
                ident
                        + "      # Create/Write File: "
                        + safeStepName(step)
        );

        ToscaToAgaPhase1.writeLine(
                ident + "      - type: FILE");

        ToscaToAgaPhase1.writeLine(
                ident + "        op: EXEC");

        ToscaToAgaPhase1.writeLine(
                ident + "        action: WRITE");

        printCondition(step, ident);
        printYamlString(ident, "path", path);
        printYamlText(ident, "text", text);
        printOptionalEncoding(values, ident);
        printOptionalOverwrite(values, ident);
    }

    // =========================================================
    // BUFFER
    // =========================================================

    private static void processBuffer(
            CleanStep step,
            Map<String, StepValueDetails> values,
            String ident,
            String path,
            String bufferName) {

        String response =
                nextResponseName();

        // READ
        ToscaToAgaPhase1.writeLine("");
        ToscaToAgaPhase1.writeLine(
                ident
                        + "      # Read File: "
                        + safeStepName(step)
        );

        ToscaToAgaPhase1.writeLine(
                ident + "      - type: FILE");

        ToscaToAgaPhase1.writeLine(
                ident + "        op: EXEC");

        ToscaToAgaPhase1.writeLine(
                ident + "        action: READ");

        printCondition(step, ident);
        printYamlString(ident, "path", path);

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        response: "
                        + response
        );

        printOptionalEncoding(values, ident);

        // BUFFER
        ToscaToAgaPhase1.writeLine("");
        ToscaToAgaPhase1.writeLine(
                ident + "      - type: FILE");

        ToscaToAgaPhase1.writeLine(
                ident + "        op: BUFFER");

        /*
         * The second half belongs to the same Tosca step and therefore
         * must have the same condition. Otherwise a skipped READ could be
         * followed by an unconditional BUFFER on a missing response.
         */
        printCondition(step, ident);

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        response: "
                        + response
        );

        ToscaToAgaPhase1.writeLine(
                ident + "        action: TEXT");

        /*
         * Text ActionMode=Buffer means the Text value is the buffer name.
         */
        printYamlString(
                ident,
                "name",
                bufferName
        );
    }

    // =========================================================
    // VERIFY
    // =========================================================

    private static void processVerify(
            CleanStep step,
            Map<String, StepValueDetails> values,
            String ident,
            String path,
            String expectedText) {

        String response =
                nextResponseName();

        // READ
        ToscaToAgaPhase1.writeLine("");
        ToscaToAgaPhase1.writeLine(
                ident
                        + "      # Read File for verification: "
                        + safeStepName(step)
        );

        ToscaToAgaPhase1.writeLine(
                ident + "      - type: FILE");

        ToscaToAgaPhase1.writeLine(
                ident + "        op: EXEC");

        ToscaToAgaPhase1.writeLine(
                ident + "        action: READ");

        printCondition(step, ident);
        printYamlString(ident, "path", path);

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        response: "
                        + response
        );

        printOptionalEncoding(values, ident);

        // ASSERT
        ToscaToAgaPhase1.writeLine("");
        ToscaToAgaPhase1.writeLine(
                ident + "      - type: FILE");

        ToscaToAgaPhase1.writeLine(
                ident + "        op: ASSERT");

        printCondition(step, ident);

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        response: "
                        + response
        );

        ToscaToAgaPhase1.writeLine(
                ident + "        action: EQUALS");

        printYamlText(
                ident,
                "value",
                expectedText
        );
    }

    // =========================================================
    // Optional Tosca parameters
    // =========================================================

    private static void printOptionalEncoding(
            Map<String, StepValueDetails> values,
            String ident) {

        String encoding =
                getMapValueIgnoreCase(
                        values,
                        "Encoding"
                );

        if (encoding == null || encoding.isBlank()) {
            return;
        }

        printYamlString(
                ident,
                "encoding",
                encoding
        );
    }

    private static void printOptionalOverwrite(
            Map<String, StepValueDetails> values,
            String ident) {

        String overwrite =
                getMapValueIgnoreCase(
                        values,
                        "Overwrite"
                );

        if (overwrite == null || overwrite.isBlank()) {
            return;
        }

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        overwrite: "
                        + Boolean.parseBoolean(overwrite)
        );
    }

    // =========================================================
    // Condition
    // =========================================================

    private static void printCondition(
            CleanStep step,
            String ident) {

        if (step.getCondition() == null
                || step.getCondition().isBlank()) {
            return;
        }

        String translatedCondition =
                ToscaValueTranslator
                        .translateToscaValues(
                                step.getCondition());

        translatedCondition =
                translatedCondition.replace(
                        "\"",
                        "'"
                );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        condition: \""
                        + translatedCondition
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                        + "\""
        );
    }

    // =========================================================
    // Path
    // =========================================================

    private static String buildPath(
            String directory,
            String file) {

        String dir =
                directory == null ? "" : directory.trim();

        String f =
                file == null ? "" : file.trim();

        if (dir.isEmpty()) {
            return f;
        }

        if (f.isEmpty()) {
            return dir;
        }

        /*
         * Tosca sometimes stores the complete path already
         * in the File field.
         *
         * Example:
         *
         * Directory = {B[BLogDir]}
         * File      = {B[BLogDir]}\{B[BLogFile]}_dest_ohne_CRLF
         *
         * In this case Directory must not be prepended again.
         */
        if (f.startsWith(dir)) {
            return f;
        }

        if (dir.endsWith("/")
                || dir.endsWith("\\")) {

            return dir + f;
        }

        /*
         * Preserve Windows-looking paths as Windows paths.
         */
        if (dir.contains("\\")
                || dir.matches("^[A-Za-z]:.*")) {

            return dir + "\\" + f;
        }

        return dir + "/" + f;
    }
    
    
    // =========================================================
    // YAML helpers
    // =========================================================

    private static void printYamlString(
            String ident,
            String property,
            String value) {

        String v =
                value == null ? "" : value;

        /*
         * Prefer YAML single quotes.
         * This prevents Windows paths containing \r, \n, \t, \T, ...
         * from being interpreted as YAML escapes.
         */
        ToscaToAgaPhase1.writeLine(
                ident
                        + "        "
                        + property
                        + ": '"
                        + v.replace("'", "''")
                        + "'"
        );
    }

    private static void printYamlText(
            String ident,
            String property,
            String value) {

        String v =
                value == null ? "" : value;

        if (v.contains("\n")
                || v.contains("\r")) {

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        "
                            + property
                            + ": |"
            );

            String normalized =
                    v.replace("\r\n", "\n")
                            .replace("\r", "\n");

            for (String line :
                    normalized.split("\n", -1)) {

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "          "
                                + line
                );
            }

        } else {

            printYamlString(
                    ident,
                    property,
                    v
            );
        }
    }

    // =========================================================
    // Tosca value helpers
    // =========================================================

    private static StepValueDetails getDetailsIgnoreCase(
            Map<String, StepValueDetails> values,
            String key) {

        for (Map.Entry<String, StepValueDetails> entry :
                values.entrySet()) {

            if (entry.getKey() != null
                    && entry.getKey().equalsIgnoreCase(key)) {

                return entry.getValue();
            }
        }

        return null;
    }

    private static String getMapValueIgnoreCase(
            Map<String, StepValueDetails> values,
            String key) {

        return getValue(
                getDetailsIgnoreCase(
                        values,
                        key
                )
        );
    }

    private static String getValue(
            StepValueDetails details) {

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

    // =========================================================
    // Response
    // =========================================================

    private static synchronized String nextResponseName() {

        responseCounter++;

        return "file_content_"
                + responseCounter;
    }

    private static String safeStepName(
            CleanStep step) {

        return step.getName() != null
                ? step.getName()
                : "TBox Read/Create File";
    }

    private static String getStepSuffix(
            CleanStep step) {

        return step.getName() != null
                ? " - " + step.getName()
                : "";
    }
}
