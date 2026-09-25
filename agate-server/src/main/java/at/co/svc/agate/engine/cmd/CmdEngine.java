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
import at.co.svc.agate.core.error.AgateStepException;
import at.co.svc.agate.core.interfaces.TestLogger;

public class CmdEngine extends AbstractStepEngine {

    private static final int DEFAULT_TIMEOUT_MS = 30_000;

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
            throw AgateStepException.builder("Unsupported CMD operation")
                    .actual(op)
                    .hint("Supported operations: EXEC, ASSERT, BUFFER")
                    .build();
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

            throw AgateStepException.builder("Required property is missing")
                    .field("command")
                    .hint("CMD EXEC requires a command to execute.")
                    .build();
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
                        "command",
                        step);

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
                        : DEFAULT_TIMEOUT_MS;

        int expectedExitCode =
                step.getExpectedExitCode() != null
                        ? step.getExpectedExitCode()
                        : 0;

        boolean ignoreExitCode =
                step.getIgnoreExitCode() != null
                        ? step.getIgnoreExitCode()
                        : step.getCheckExitCode() != null
                                ? !step.getCheckExitCode()
                                : false;

        if (timeout <= 0) {
            throw AgateStepException.builder("Invalid CMD timeout")
                    .expected("greater than 0 milliseconds")
                    .actual(timeout)
                    .field("timeout")
                    .build();
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
                    !result.isTimedOut()
                            && (ignoreExitCode
                                    || result.getExitCode() == expectedExitCode)
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
            throw AgateStepException.builder("Command timed out")
                    .expected("completion within " + timeout + " ms")
                    .actual("timeout")
                    .command(resolvedCommand)
                    .hint("Check the command or increase timeout if the longer runtime is expected.")
                    .build();
        }

        if (!ignoreExitCode
                && result.getExitCode()
                        != expectedExitCode) {

            String expectedDisplay =
                    step.getExpectedExitCode() == null
                            ? expectedExitCode + " (default)"
                            : String.valueOf(expectedExitCode);

            throw AgateStepException.builder("Unexpected exit code")
                    .expected(expectedDisplay)
                    .actual(result.getExitCode())
                    .command(resolvedCommand)
                    .hint("Set ignoreExitCode: true to accept any exit code.")
                    .build();
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
                        ? step.getAction().trim().toUpperCase()
                        : "";

        String expected =
                step.getExpected() != null
                        ? step.getExpected()
                        : "0";

        String value =
                step.getValue() != null
                        ? step.getValue()
                        : "";

        if (responseKey == null || responseKey.isBlank()) {
            throw AgateStepException.builder("Required property is missing")
                    .field("response")
                    .hint("CMD ASSERT must reference a response created by a previous CMD EXEC step.")
                    .build();
        }

        if (action.isBlank()) {
            throw AgateStepException.builder("Required property is missing")
                    .field("action")
                    .hint("Supported CMD ASSERT actions: EXITCODE, CONTAINS, NOT_CONTAINS, EQUALS, NOT_EQUALS, COUNT")
                    .build();
        }

        Object rawVal =
                context.getBuffer(
                        responseKey);

        if (rawVal == null) {
            throw AgateStepException.builder("CMD response was not found")
                    .actual(responseKey)
                    .field("response")
                    .hint("Check that a previous CMD EXEC step stores response: " + responseKey)
                    .build();
        }

        CommandResult commandResult =
                rawVal instanceof CommandResult
                        ? (CommandResult) rawVal
                        : null;

        String outputToCheck =
                commandResult != null
                        ? (commandResult.getOutput() != null ? commandResult.getOutput() : "")
                        : rawVal.toString();

        switch (action) {

        case "EXITCODE":
            if (commandResult == null) {
                throw AgateStepException.builder("EXITCODE assertion requires a CMD command response")
                        .actual(responseKey)
                        .field("response")
                        .hint("Reference the response created by the preceding CMD EXEC step.")
                        .build();
            }

            final int expectedExitCode;
            try {
                expectedExitCode = Integer.parseInt(expected);
            } catch (NumberFormatException e) {
                throw AgateStepException.builder("Invalid EXITCODE assertion value")
                        .expected("integer exit code")
                        .actual(expected)
                        .field("expected")
                        .hint("Use an integer value such as expected: 0")
                        .cause(e)
                        .build();
            }

            if (commandResult.getExitCode() != expectedExitCode) {
                throw AgateStepException.builder("EXITCODE assertion failed")
                        .expected(expectedExitCode)
                        .actual(commandResult.getExitCode())
                        .build();
            }
            break;

        case "CONTAINS":
            if (!outputToCheck.contains(value)) {
                throw AgateStepException.builder("CONTAINS assertion failed")
                        .expected(value)
                        .actual(outputToCheck.trim())
                        .build();
            }
            break;

        case "NOT_CONTAINS":
            if (outputToCheck.contains(value)) {
                throw AgateStepException.builder("NOT_CONTAINS assertion failed")
                        .expected("output must not contain: " + value)
                        .actual(outputToCheck.trim())
                        .build();
            }
            break;

        case "EQUALS":
            if (!outputToCheck.trim().equals(value.trim())) {
                throw AgateStepException.builder("EQUALS assertion failed")
                        .expected(value.trim())
                        .actual(outputToCheck.trim())
                        .build();
            }
            break;

        case "NOT_EQUALS":
            if (outputToCheck.trim().equals(value.trim())) {
                throw AgateStepException.builder("NOT_EQUALS assertion failed")
                        .expected("value different from: " + value.trim())
                        .actual(outputToCheck.trim())
                        .build();
            }
            break;

        case "COUNT":
            final int expectedCount;
            try {
                expectedCount = Integer.parseInt(expected);
            } catch (NumberFormatException e) {
                throw AgateStepException.builder("Invalid COUNT assertion value")
                        .expected("integer count")
                        .actual(expected)
                        .field("expected")
                        .hint("Use an integer value such as expected: 1")
                        .cause(e)
                        .build();
            }

            int actualCount =
                    countOccurrences(
                            outputToCheck,
                            value);

            if (actualCount != expectedCount) {
                throw AgateStepException.builder("COUNT assertion failed")
                        .expected(expectedCount)
                        .actual(actualCount)
                        .detail("Value", value)
                        .build();
            }
            break;

        default:
            throw AgateStepException.builder("Unsupported CMD ASSERT action")
                    .actual(action)
                    .field("action")
                    .hint("Supported actions: EXITCODE, CONTAINS, NOT_CONTAINS, EQUALS, NOT_EQUALS, COUNT")
                    .build();
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