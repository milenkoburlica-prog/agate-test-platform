package at.co.svc.aga.transformator.utils;

import java.util.Map;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

public class ProcessStartProgram {

    public static void processStartProgram(
            CleanStep step,
            String ident) {

        Map<String, StepValueDetails> values =
                step.getValuesAsMap();

        if (values == null) {
            return;
        }

        String directory =
                MigationPhase2Utils
                        .getMapValueIgnoreCase(
                                values,
                                "Directory"
                        )
                        .toLowerCase();

        String path =
                MigationPhase2Utils
                        .getMapValueIgnoreCase(
                                values,
                                "Path"
                        )
                        .toLowerCase();

        String rawCommand =
                MigationPhase2Utils
                        .getMapValueIgnoreCase(
                                values,
                                "Arguments.Argument"
                        );

        if (rawCommand.isEmpty()) {

            rawCommand =
                    MigationPhase2Utils
                            .getMapValueIgnoreCase(
                                    values,
                                    "Arguments"
                            );
        }

        /*
         * No command was found, so this step cannot be migrated.
         *
         * This is relevant enough to remain visible
         * at the normal logging level.
         */
        if (rawCommand.isEmpty()) {

            MigrationLog.info(
                    "[Skipped] TBox Start Program - "
                            + "no command found in Arguments"
                            + (step.getName() != null
                                    ? " - " + step.getName()
                                    : "")
            );

            return;
        }

        // -----------------------------------------------------
        // Determine command engine
        // -----------------------------------------------------

        String finalCommand =
                rawCommand;

        String stepComment =
                "Execute Command";

        if (path.contains("powershell")
                || path.contains("pwsh")) {

            finalCommand =
                    "powershell -Command "
                            + rawCommand;

            stepComment =
                    "Execute PowerShell Command";

        } else if (path.contains("cmd")) {

            stepComment =
                    "Execute CMD Command";
        }

        // -----------------------------------------------------
        // AGATE DSL
        // -----------------------------------------------------

        ToscaToAgaPhase1.writeLine("\n");

        ToscaToAgaPhase1.writeLine(
                ident
                        + "      # "
                        + stepComment
                        + ": "
                        + step.getName()
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "      - type: CMD"
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        op: EXEC"
        );

        if (step.getCondition() != null
                && !step.getCondition().isEmpty()) {

            String condition =
                    step.getCondition();

            String cleanCondition =
                    SVCToscaTranslator
                            .translateToscaValues(
                                    condition
                            );

            String formattedCondition =
                    cleanCondition.replace(
                            "\"",
                            "'"
                    );

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        condition: \""
                            + formattedCondition
                            + "\""
            );
        }

        String cmd =
                finalCommand.replace(
                        "\\",
                        "\\\\"
                );

        /*
         * Remove /C from the beginning of CMD commands.
         */
        if (cmd.toUpperCase().startsWith("/C")) {

            cmd =
                    cmd.substring(2).trim();
        }

        /*
         * Prefix the command with the configured directory
         * if it is not already present.
         */
        if (!cmd.startsWith(directory)) {

            cmd =
                    directory
                            + cmd;
        }

        String translatedCommand =
                SVCToscaTranslator
                        .translateToscaValues(
                                cmd
                        );

        /*
         * Use YAML block syntax when the command contains quotes.
         */
        if (translatedCommand.contains("\"")) {

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        command: |"
            );

            translatedCommand =
                    translatedCommand.replace(
                            "\"\"\"\"",
                            "\""
                    );

            translatedCommand =
                    translatedCommand.replace(
                            "\"\"\"",
                            "\""
                    );

            String formattedCmd =
                    translatedCommand.replace(
                            "   ",
                            " "
                    );

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "            "
                            + formattedCmd
            );

        } else {

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        command: \""
                            + translatedCommand
                            + "\""
            );
        }

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        response: cmd_response"
        );
    }
}