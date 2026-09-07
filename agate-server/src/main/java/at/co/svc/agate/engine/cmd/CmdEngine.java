package at.co.svc.agate.engine.cmd;

import java.io.File;

import at.co.svc.agate.core.command.CommandExecutionSupport;
import at.co.svc.agate.core.command.CommandResult;
import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.interfaces.TestLogger;

public class CmdEngine extends AbstractStepEngine {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.CMD;
    }

    @Override
    public void doExecute(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) throws Exception {

        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(
                    logger,
                    step);
        }

        String op =
                step.getOp() != null
                        ? step.getOp().toUpperCase()
                        : "EXEC";

        switch (op) {

        case "ASSERT":
            handleAssertion(
                    tc,
                    step,
                    context,
                    stepIndex,
                    printExecution,
                    yamlFile,
                    logger,
                    isVerbose);
            break;

        case "BUFFER":
            handleBuffer(
                    tc,
                    step,
                    context,
                    stepIndex,
                    printExecution,
                    yamlFile,
                    logger,
                    isVerbose);
            break;

        case "EXEC":
            handleExecution(
                    tc,
                    step,
                    context,
                    yamlFile,
                    stepIndex,
                    printExecution,
                    logger,
                    isVerbose);
            break;

        default:
            throw new RuntimeException(
                    "Unsupported CMD operation: " + op);
        }
    }

    private void handleExecution(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) throws Exception {

        String rawCommand = step.getCommand();

        if (rawCommand == null
                || rawCommand.isBlank()) {

            throw new RuntimeException(
                    "CMD EXEC requires 'command'.");
        }

        YamlPlaceholderResolver resolver =
                new YamlPlaceholderResolver();

        String resolvedCommand =
                resolver.resolve(
                        tc,
                        rawCommand,
                        tc.getVariables(),
                        yamlFile,
                        stepIndex,
                        "command");

        if (resolvedCommand != null
                && step.getParameters() != null) {

            resolvedCommand =
                    resolver.resolve(
                            tc,
                            resolvedCommand,
                            step.getParameters(),
                            yamlFile,
                            stepIndex,
                            "command",
                            step);
        }

        String outputFile =
                step.getOutputFile();

        if (outputFile != null) {

            outputFile =
                    resolver.resolve(
                            tc,
                            outputFile,
                            tc.getVariables(),
                            yamlFile,
                            stepIndex,
                            "outputFile");

            if (step.getParameters() != null) {
                outputFile =
                        resolver.resolve(
                                tc,
                                outputFile,
                                step.getParameters(),
                                yamlFile,
                                stepIndex,
                                "outputFile",
                                step);
            }
        }

        File workingDirectory =
                determineWorkingDirectory(
                        resolvedCommand);

        int timeout =
                step.getTimeout() != null
                        ? step.getTimeout()
                        : DEFAULT_TIMEOUT_SECONDS;

        int expectedExitCode =
                step.getExpectedExitCode() != null
                        ? step.getExpectedExitCode()
                        : 0;

        boolean checkExitCode =
                step.getCheckExitCode() == null
                        || step.getCheckExitCode();

        if (timeout <= 0) {
            throw new RuntimeException(
                    "CMD timeout must be greater than 0.");
        }

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logger.info(
                    String.format(
                            "    %s>>> CMD       %s: %s",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET,
                            resolvedCommand));

            if (workingDirectory != null) {

                logger.info(
                        String.format(
                                "    %s>>> WORKDIR   %s: %s",
                                ConsoleColors.BLUE,
                                ConsoleColors.RESET,
                                workingDirectory.getAbsolutePath()));
            }
        }

        CommandResult result =
                CommandExecutionSupport.executeWindowsShell(
                        resolvedCommand,
                        workingDirectory,
                        timeout);

        if (step.getResponse() != null) {
            context.storeBuffer(
                    step.getResponse(),
                    result);
        }

        if (outputFile != null
                && !outputFile.isBlank()) {

            CommandExecutionSupport.writeOutputFile(
                    outputFile,
                    result.getOutput(),
                    workingDirectory);
        }

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logOutput(
                    result.getOutput(),
                    logger);

            String statusColor =
                    result.getExitCode() == expectedExitCode
                            && !result.isTimedOut()
                                    ? ConsoleColors.GREEN
                                    : ConsoleColors.RED;

            logger.info(
                    String.format(
                            "    %s>>> RESULT    %s: Exit=%d, Timeout=%s, Duration=%d ms",
                            statusColor,
                            ConsoleColors.RESET,
                            result.getExitCode(),
                            result.isTimedOut(),
                            result.getDurationMs()));
        }

        if (result.isTimedOut()) {
            throw new RuntimeException(
                    "Command timed out after "
                            + timeout
                            + " seconds.");
        }

        if (checkExitCode
                && result.getExitCode()
                        != expectedExitCode) {

            throw new RuntimeException(
                    "Command execution failed. Expected exit code "
                            + expectedExitCode
                            + " but got "
                            + result.getExitCode());
        }
    }

    private void handleAssertion(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            int stepIndex,
            Boolean printExecution,
            String yamlFile,
            TestLogger logger,
            boolean isVerbose) {

        String responseKey =
                step.getResponse();

        String action =
                step.getAction() != null
                        ? step.getAction().toUpperCase()
                        : "";

        String expected =
                step.getExpected() != null
                        ? step.getExpected()
                        : "0";

        String value =
                step.getValue() != null
                        ? step.getValue()
                        : "";

        Object rawVal =
                context.getBuffer(
                        responseKey);

        if (rawVal == null) {
            throw new RuntimeException(
                    "No response or buffer found for key: "
                            + responseKey);
        }

        CommandResult commandResult =
                rawVal instanceof CommandResult
                        ? (CommandResult) rawVal
                        : null;

        String outputToCheck =
                commandResult != null
                        ? commandResult.getOutput()
                        : rawVal.toString();

        switch (action) {

        case "EXITCODE":
            if (commandResult == null) {
                throw new RuntimeException(
                        "Assertion [EXITCODE] requires a CMD command response.");
            }

            if (commandResult.getExitCode()
                    != Integer.parseInt(expected)) {

                throw new RuntimeException(
                        "Assertion EXITCODE failed! Expected: "
                                + expected
                                + ", Actual: "
                                + commandResult.getExitCode());
            }
            break;

        case "CONTAINS":
            if (!outputToCheck.contains(value)) {
                throw new RuntimeException(
                        "Assertion CONTAINS failed! String \""
                                + value
                                + "\" not found.");
            }
            break;

        case "NOT_CONTAINS":
            if (outputToCheck.contains(value)) {
                throw new RuntimeException(
                        "Assertion NOT_CONTAINS failed! String \""
                                + value
                                + "\" found.");
            }
            break;

        case "EQUALS":
            if (!outputToCheck.trim()
                    .equals(value.trim())) {

                throw new RuntimeException(
                        "Assertion EQUALS failed! Expected: \""
                                + value
                                + "\", Actual: \""
                                + outputToCheck.trim()
                                + "\"");
            }
            break;

        case "NOT_EQUALS":
            if (outputToCheck.trim()
                    .equals(value.trim())) {

                throw new RuntimeException(
                        "Assertion NOT_EQUALS failed.");
            }
            break;

        case "COUNT":
            int actualCount =
                    countOccurrences(
                            outputToCheck,
                            value);

            if (actualCount
                    != Integer.parseInt(expected)) {

                throw new RuntimeException(
                        "Assertion COUNT failed! Expected: "
                                + expected
                                + ", Actual: "
                                + actualCount);
            }
            break;

        default:
            throw new RuntimeException(
                    "Unknown assertion type: \""
                            + action
                            + "\"");
        }

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logger.info(
                    String.format(
                            "    %s>>> RESULT    %s: SUCCESS",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET));
        }
    }

    private void handleBuffer(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            int stepIndex,
            Boolean printExecution,
            String yamlFile,
            TestLogger logger,
            boolean isVerbose) {

        String responseKey =
                step.getResponse();

        String action =
                step.getAction() != null
                        ? step.getAction().toUpperCase()
                        : "TEXT";

        String name =
                step.getName();

        String rawValue =
                step.getValue();

        if ((rawValue == null
                || rawValue.trim().isEmpty())
                && (action.equals("LINE")
                        || action.equals("LAST_LINE"))) {

            rawValue = "0";
        }

        Object raw =
                context.getBuffer(
                        responseKey);

        if (!(raw instanceof CommandResult)) {
            throw new RuntimeException(
                    "No CMD response found for key: "
                            + responseKey);
        }

        CommandResult commandResult =
                (CommandResult) raw;

        String fullOutput =
                commandResult.getOutput() == null
                        ? ""
                        : commandResult.getOutput().trim();

        String result;

        switch (action) {

        case "COUNT":
            result =
                    String.valueOf(
                            countOccurrences(
                                    fullOutput,
                                    rawValue != null
                                            ? rawValue
                                            : ""));
            break;

        case "FILTER":
            result =
                    filterLines(
                            fullOutput,
                            rawValue != null
                                    ? rawValue
                                    : "");
            break;

        case "LAST_LINE":
            result =
                    getLastLine(
                            fullOutput,
                            Integer.parseInt(rawValue));
            break;

        case "LINE":
            result =
                    getLine(
                            fullOutput,
                            Integer.parseInt(rawValue));
            break;

        case "TEXT":
            result = fullOutput;
            break;

        default:
            throw new RuntimeException(
                    "Unsupported CMD BUFFER action: "
                            + action);
        }

        if (name == null
                || name.isBlank()) {

            throw new RuntimeException(
                    "CMD BUFFER requires 'name'.");
        }

        context.storeBuffer(
                name,
                result);

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logger.info(
                    String.format(
                            "    %s>>> RESULT    %s: SUCCESS (Buffered)",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET));
        }
    }

    private File determineWorkingDirectory(
            String command) {

        if (command == null) {
            return null;
        }

        String trimmed =
                command.trim();

        String executablePath = null;

        if (trimmed.startsWith("\"")) {

            int nextQuote =
                    trimmed.indexOf(
                            "\"",
                            1);

            if (nextQuote > 1) {
                executablePath =
                        trimmed.substring(
                                1,
                                nextQuote);
            }

        } else if (trimmed.length() > 2
                && Character.isLetter(
                        trimmed.charAt(0))
                && trimmed.charAt(1) == ':') {

            int firstSpace =
                    trimmed.indexOf(' ');

            executablePath =
                    firstSpace > 0
                            ? trimmed.substring(
                                    0,
                                    firstSpace)
                            : trimmed;
        }

        if (executablePath == null) {
            return null;
        }

        File executableFile =
                new File(
                        executablePath);

        File parent =
                executableFile.getParentFile();

        if (parent != null
                && parent.exists()
                && parent.isDirectory()) {

            return parent;
        }

        return null;
    }

    private void logOutput(
            String output,
            TestLogger logger) {

        if (output == null
                || output.isBlank()) {
            return;
        }

        for (String line :
                output.trim().split("\\R")) {

            logger.info(
                    String.format(
                            "    %s<<< OUT       %s: %s",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET,
                            line));
        }
    }

    private String filterLines(
            String text,
            String search) {

        StringBuilder result =
                new StringBuilder();

        for (String line :
                text.split("\\R")) {

            if (line.contains(search)) {
                result.append(line)
                        .append(System.lineSeparator());
            }
        }

        return result.toString().trim();
    }

    private String getLine(
            String text,
            int index) {

        String[] lines =
                text.split("\\R");

        if (index < 0
                || index >= lines.length) {

            throw new RuntimeException(
                    "LINE index out of bounds: "
                            + index);
        }

        return lines[index].trim();
    }

    private String getLastLine(
            String text,
            int offset) {

        String[] lines =
                text.split("\\R");

        int index =
                lines.length - 1 - offset;

        if (index < 0
                || index >= lines.length) {

            throw new RuntimeException(
                    "LAST_LINE index out of bounds: "
                            + offset);
        }

        return lines[index].trim();
    }

    private int countOccurrences(
            String text,
            String search) {

        if (text == null
                || search == null
                || search.isEmpty()) {

            return 0;
        }

        int count = 0;
        int index = 0;

        while ((index =
                text.indexOf(
                        search,
                        index)) != -1) {

            count++;
            index += search.length();
        }

        return count;
    }
}