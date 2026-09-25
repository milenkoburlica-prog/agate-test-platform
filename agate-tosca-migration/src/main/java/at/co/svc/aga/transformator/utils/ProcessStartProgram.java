package at.co.svc.aga.transformator.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

public class ProcessStartProgram {

    public static void processStartProgram(
            CleanStep step,
            String ident) {

        if (step.getValues() == null || step.getValues().isEmpty()) {
            return;
        }

        Map<String, StepValueDetails> values =
                step.getValuesAsMap();

        String directory =
                translate(findValue(values, "Directory")).trim();

        String executablePath =
                findValue(values, "Path").trim();

        List<String> arguments =
                getArguments(step);

        if (arguments.isEmpty()) {

            String fallback =
                    findValue(values, "Arguments");

            if (!fallback.isBlank()) {
                arguments.add(fallback);
            }
        }

        if (arguments.isEmpty()) {

            MigrationLog.info(
                    "[Skipped] TBox Start Program - "
                            + "no command found in Arguments"
                            + getStepSuffix(step)
            );

            return;
        }

        String lowerPath =
                executablePath.toLowerCase(Locale.ROOT);

        boolean powershell =
                lowerPath.contains("powershell")
                        || lowerPath.contains("pwsh");

        boolean cmd =
                lowerPath.contains("cmd");

        String command;

        if (powershell) {

            command =
                    buildPowerShellCommand(
                            arguments,
                            directory
                    );

        } else if (cmd) {

            command =
                    buildCmdCommand(
                            arguments,
                            directory
                    );

        } else {

            command =
                    buildGenericCommand(
                            executablePath,
                            arguments,
                            directory
                    );
        }

        String translatedCommand =
                translate(command);

        String outputFile =
                translate(
                        firstNonBlank(
                                findValueBySuffix(
                                        step,
                                        "StandardOutputFile"
                                ),
                                findValue(
                                        values,
                                        "StandardOutputFile"
                                )
                        )
                );

        String expectedExitCode =
                firstNonBlank(
                        findValueBySuffix(
                                step,
                                "ExitCode"
                        ),
                        findValue(
                                values,
                                "ExitCode"
                        )
                );

        String timeout =
                firstNonBlank(
                        findValueBySuffix(
                                step,
                                "TimeoutForExit"
                        ),
                        findValue(
                                values,
                                "TimeoutForExit"
                        )
                );

        // -----------------------------------------------------
        // AGATE DSL
        // -----------------------------------------------------

        ToscaToAgaPhase1.writeLine("");

        ToscaToAgaPhase1.writeLine(
                ident
                        + "      # "
                        + getStepComment(
                                powershell,
                                cmd
                        )
                        + ": "
                        + safeStepName(step)
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "      - type: CMD"
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        op: EXEC"
        );

        printCondition(
                step,
                ident
        );

        printCommand(
                ident,
                translatedCommand
        );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        response: cmd_response"
        );

        /*
         * Tosca:
         * WaitForExit.StandardOutputFile
         *
         * AGATE:
         * CMD.outputFile
         */
        if (!outputFile.isBlank()) {

            writeYamlString(
                    ident,
                    "outputFile",
                    outputFile
            );
        }

        if (!timeout.isBlank()) {
            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        timeout: "
                            + timeout.trim()
            );
        }

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        ignoreExitCode: false"
        );

        if (!expectedExitCode.isBlank()
                && !"0".equals(expectedExitCode.trim())) {

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        expectedExitCode: "
                            + expectedExitCode.trim()
            );
        }
    }


    // =========================================================
    // Arguments
    // =========================================================

    private static List<String> getArguments(
            CleanStep step) {

        List<String> result =
                new ArrayList<>();

        if (step.getValues() == null) {
            return result;
        }

        /*
         * Repeated Tosca Arguments.Argument values are legal.
         *
         * Do not use getValuesAsMap() here because duplicate argument
         * entries would be collapsed.
         */
        for (StepValueDetails details :
                step.getValues()) {

            if (details == null
                    || details.getName() == null
                    || details.getValue() == null) {

                continue;
            }

            String name =
                    details.getName();

            String lowerName =
                    name.toLowerCase(Locale.ROOT);

            if (name.equalsIgnoreCase(
                    "Arguments.Argument")
                    || name.equalsIgnoreCase(
                    "Argument")
                    || lowerName.endsWith(
                    ".arguments.argument")) {

                result.add(
                        details.getValue()
                );
            }
        }

        return result;
    }


    // =========================================================
    // CMD
    // =========================================================

    private static String buildCmdCommand(
            List<String> arguments,
            String directory) {

        String command =
                joinArguments(arguments);

        /*
         * Depending on the previous Tosca cleanup stages we can receive:
         *
         *   ["/C", "oc exec ..."]
         *
         * or:
         *
         *   ["/C oc exec ..."]
         *
         * Normalize both.
         */
        command =
                removeCmdPrefix(command);

        /*
         * Tosca sometimes serializes one literal double quote as four quotes,
         * e.g.:
         *
         *   bash -c """"{B[CMD]}""""
         *
         * AGATE/CMD needs:
         *
         *   bash -c "{B[CMD]}"
         */
        command =
                normalizeToscaQuotes(command);

        if (!directory.isBlank()) {

            /*
             * Tosca Directory is a working directory.
             * Never concatenate it directly with the command:
             *
             * WRONG:
             *   C:\TCLogsdel ...
             *
             * Instead preserve the semantics via cmd:
             *
             *   cd /d "C:\TCLogs" && del ...
             */
            command =
                    "cd /d "
                            + quoteCmd(directory)
                            + " && "
                            + command;
        }

        return command.trim();
    }

    private static String removeCmdPrefix(
            String command) {

        String result =
                command == null
                        ? ""
                        : command.trim();

        if (result.equalsIgnoreCase("/C")) {
            return "";
        }

        if (startsWithIgnoreCase(
                result,
                "/C ")) {

            return result
                    .substring(3)
                    .trim();
        }

        return result;
    }


    // =========================================================
    // PowerShell
    // =========================================================

    private static String buildPowerShellCommand(
            List<String> arguments,
            String directory) {

        String raw =
                joinArguments(arguments)
                        .trim();

        /*
         * Tosca cleanup may give us either:
         *
         *   ["-command", "(Get-Content ... | ...)"]
         *
         * or:
         *
         *   ["-command (Get-Content ... | ...)"]
         *
         * or:
         *
         *   ["-ExecutionPolicy Bypass", "-File", "..."]
         *
         * or one combined string:
         *
         *   ["-ExecutionPolicy Bypass -File ..."]
         *
         * Therefore parse the joined raw command line, not only exact
         * list elements.
         */

        String lower =
                raw.toLowerCase(Locale.ROOT);

        String command;

        int commandPos =
                indexOfTokenIgnoreCase(
                        raw,
                        "-command"
                );

        int filePos =
                indexOfTokenIgnoreCase(
                        raw,
                        "-file"
                );

        if (commandPos >= 0) {

            String prefix =
                    raw.substring(
                            0,
                            commandPos
                    ).trim();

            String expression =
                    raw.substring(
                            commandPos
                                    + "-command".length()
                    ).trim();

            StringBuilder result =
                    new StringBuilder(
                            "powershell"
                    );

            /*
             * Preserve switches that occur before -Command.
             */
            if (!prefix.isBlank()) {

                result.append(" ")
                        .append(prefix);
            }

            result.append(" -Command");

            if (!expression.isBlank()) {

                String finalExpression =
                        expression;

                if (!directory.isBlank()) {

                    finalExpression =
                            "Set-Location -LiteralPath "
                                    + quotePowerShellLiteral(
                                            directory
                                    )
                                    + "; "
                                    + expression;
                }

                result.append(" ")
                        .append(
                                quotePowerShellCommand(
                                        finalExpression
                                )
                        );
            }

            command =
                    result.toString();

        } else if (filePos >= 0) {

            /*
             * A PowerShell -File invocation is already a complete
             * PowerShell command line. Do NOT prepend -Command.
             *
             * Example:
             *
             * powershell -ExecutionPolicy Bypass -File script.ps1 arg1
             */
            command =
                    "powershell "
                            + raw;

            if (!directory.isBlank()) {

                /*
                 * Do not rewrite -File invocation semantics just to apply
                 * Directory. Report it for review instead of generating
                 * incorrect PowerShell.
                 */
                MigrationLog.info(
                        "[Review] PowerShell -File step has Tosca Directory='"
                                + directory
                                + "'. Directory was not concatenated into command."
                                + getStepSuffix(null)
                );
            }

        } else {

            /*
             * No explicit -Command or -File.
             * Treat the raw Tosca arguments as the PowerShell expression.
             */
            String expression =
                    raw;

            if (!directory.isBlank()) {

                expression =
                        "Set-Location -LiteralPath "
                                + quotePowerShellLiteral(
                                        directory
                                )
                                + "; "
                                + expression;
            }

            command =
                    "powershell -Command "
                            + quotePowerShellCommand(
                                    expression
                            );
        }

        return command.trim();
    }

    /**
     * Finds a PowerShell switch as an actual token.
     *
     * Examples:
     *   "-command abc" -> 0
     *   "-ExecutionPolicy Bypass -File x.ps1" -> position of -File
     *
     * It intentionally does not match text embedded inside another word.
     */
    private static int indexOfTokenIgnoreCase(
            String text,
            String token) {

        if (text == null
                || token == null) {

            return -1;
        }

        String lowerText =
                text.toLowerCase(
                        Locale.ROOT
                );

        String lowerToken =
                token.toLowerCase(
                        Locale.ROOT
                );

        int from =
                0;

        while (true) {

            int index =
                    lowerText.indexOf(
                            lowerToken,
                            from
                    );

            if (index < 0) {
                return -1;
            }

            boolean leftBoundary =
                    index == 0
                            || Character.isWhitespace(
                            lowerText.charAt(
                                    index - 1
                            )
                    );

            int end =
                    index
                            + lowerToken.length();

            boolean rightBoundary =
                    end >= lowerText.length()
                            || Character.isWhitespace(
                            lowerText.charAt(
                                    end
                            )
                    );

            if (leftBoundary
                    && rightBoundary) {

                return index;
            }

            from =
                    index + 1;
        }
    }


    // =========================================================
    // Generic executable
    // =========================================================

    private static String buildGenericCommand(
            String executablePath,
            List<String> arguments,
            String directory) {

        String executable =
                executablePath == null
                        ? ""
                        : executablePath.trim();

        String args =
                joinArguments(arguments);

        String command =
                executable.isBlank()
                        ? args
                        : quoteCmd(executable)
                        + (args.isBlank()
                        ? ""
                        : " " + args);

        command =
                normalizeToscaQuotes(command);

        if (!directory.isBlank()) {

            command =
                    "cd /d "
                            + quoteCmd(directory)
                            + " && "
                            + command;
        }

        return command.trim();
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
                );

        condition =
                condition.replace(
                        "\"",
                        "'"
                );

        ToscaToAgaPhase1.writeLine(
                ident
                        + "        condition: \""
                        + escapeDoubleQuotedYaml(
                                condition
                        )
                        + "\""
        );
    }

    private static void printCommand(
            String ident,
            String command) {

        if (command == null) {
            command = "";
        }

        /*
         * Use block scalar for commands containing double quotes.
         * This is especially useful for PowerShell -Command.
         */
        if (command.contains("\"")
                || command.contains("\n")
                || command.contains("\r")) {

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        command: |"
            );

            String normalized =
                    command
                            .replace(
                                    "\r\n",
                                    "\n"
                            )
                            .replace(
                                    "\r",
                                    "\n"
                            );

            for (String line :
                    normalized.split(
                            "\n",
                            -1
                    )) {

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "            "
                                + line
                );
            }

        } else {

            writeYamlString(
                    ident,
                    "command",
                    command
            );
        }
    }

    private static void writeYamlString(
            String ident,
            String property,
            String value) {

        String safe =
                value == null
                        ? ""
                        : value;

        /*
         * YAML single quotes keep Windows backslashes literal.
         */
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


    // =========================================================
    // Quoting
    // =========================================================

    private static String quotePowerShellCommand(
            String expression) {

        String normalized =
                expression == null
                        ? ""
                        : expression.trim();

        normalized =
                normalized
                        .replace(
                                "\"\"\"\"",
                                "\""
                        )
                        .replace(
                                "\"\"\"",
                                "\""
                        );

        /*
         * The complete expression must remain one argument of
         * powershell -Command, otherwise cmd.exe can intercept '|', '&', etc.
         */
        normalized =
                normalized.replace(
                        "\"",
                        "\\\""
                );

        return "\""
                + normalized
                + "\"";
    }

    private static String quotePowerShellLiteral(
            String value) {

        String safe =
                value == null
                        ? ""
                        : value;

        return "'"
                + safe.replace(
                        "'",
                        "''"
                )
                + "'";
    }

    private static String quoteCmd(
            String value) {

        String safe =
                value == null
                        ? ""
                        : value.trim();

        if (safe.startsWith("\"")
                && safe.endsWith("\"")) {

            return safe;
        }

        return "\""
                + safe.replace(
                        "\"",
                        "\\\""
                )
                + "\"";
    }


    // =========================================================
    // Value lookup
    // =========================================================

    private static String findValue(
            Map<String, StepValueDetails> values,
            String name) {

        if (values == null) {
            return "";
        }

        for (Map.Entry<String, StepValueDetails> entry :
                values.entrySet()) {

            if (entry.getKey() != null
                    && entry.getKey()
                    .equalsIgnoreCase(
                            name
                    )) {

                StepValueDetails details =
                        entry.getValue();

                return details != null
                        && details.getValue() != null
                        ? details.getValue()
                        : "";
            }
        }

        return "";
    }

    private static String findValueBySuffix(
            CleanStep step,
            String suffix) {

        if (step.getValues() == null) {
            return "";
        }

        String normalizedSuffix =
                suffix.toLowerCase(
                        Locale.ROOT
                );

        String result =
                "";

        for (StepValueDetails details :
                step.getValues()) {

            if (details == null
                    || details.getName() == null) {

                continue;
            }

            String name =
                    details.getName()
                            .toLowerCase(
                                    Locale.ROOT
                            );

            if (name.equals(
                    normalizedSuffix)
                    || name.endsWith(
                    "."
                            + normalizedSuffix)) {

                result =
                        details.getValue() != null
                                ? details.getValue()
                                : "";
            }
        }

        return result;
    }


    // =========================================================
    // Helpers
    // =========================================================

    private static String joinArguments(
            List<String> arguments) {

        if (arguments == null
                || arguments.isEmpty()) {

            return "";
        }

        StringBuilder result =
                new StringBuilder();

        for (String argument :
                arguments) {

            if (argument == null
                    || argument.isBlank()) {

                continue;
            }

            if (result.length() > 0) {
                result.append(" ");
            }

            result.append(
                    argument.trim()
            );
        }

        return result.toString();
    }

    private static String normalizeToscaQuotes(
            String value) {

        if (value == null || value.isEmpty()) {
            return "";
        }

        String result = value;

        /*
         * Tosca TSU exports may represent one intended quote with
         * multiple quote characters. Normalize the patterns observed
         * in Start Program arguments.
         */
        while (result.contains("\"\"\"\"")) {
            result = result.replace("\"\"\"\"", "\"");
        }

        while (result.contains("\"\"\"")) {
            result = result.replace("\"\"\"", "\"");
        }

        return result;
    }


    private static boolean startsWithIgnoreCase(
            String value,
            String prefix) {

        if (value == null
                || prefix == null
                || value.length()
                < prefix.length()) {

            return false;
        }

        return value.regionMatches(
                true,
                0,
                prefix,
                0,
                prefix.length()
        );
    }

    private static String firstNonBlank(
            String... values) {

        for (String value :
                values) {

            if (value != null
                    && !value.isBlank()) {

                return value;
            }
        }

        return "";
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

    private static String escapeDoubleQuotedYaml(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                );
    }

    private static String getStepComment(
            boolean powershell,
            boolean cmd) {

        if (powershell) {
            return "Execute PowerShell Command";
        }

        if (cmd) {
            return "Execute CMD Command";
        }

        return "Execute Command";
    }

    private static String safeStepName(
            CleanStep step) {

        return step != null
                && step.getName() != null
                ? step.getName()
                : "TBox Start Program";
    }

    private static String getStepSuffix(
            CleanStep step) {

        return step != null
                && step.getName() != null
                ? " - " + step.getName()
                : "";
    }
}
