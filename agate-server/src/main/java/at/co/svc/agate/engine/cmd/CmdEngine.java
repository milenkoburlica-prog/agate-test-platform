package at.co.svc.agate.engine.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.interfaces.TestLogger;
import at.co.svc.agate.engine.oc.OcCmdResponse;

/**
 * Universal Command Engine for local OS commands. Supports EXEC (execution),
 * ASSERT (validation), and BUFFER (data extraction).
 */
public class CmdEngine extends AbstractStepEngine {

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.CMD;
    }

    @Override
    public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(logger, step);
        }

        String op = (step.getOp() != null) ? step.getOp().toUpperCase() : "EXEC";

        switch (op) {
        case "ASSERT":
            handleAssertion(tc, step, context, stepIndex, printExecution, yamlFile, logger, isVerbose);
            break;
        case "BUFFER":
            handleBuffer(tc, step, context, stepIndex, printExecution, yamlFile, logger, isVerbose);
            break;
        case "EXEC":
            handleExecution(tc, step, context, yamlFile, stepIndex, printExecution, logger, isVerbose);
            break;
        default:
            throw new RuntimeException("Unsupported CMD operation: " + op);
        }
    }

    private void handleExecution(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        String rawCommand = step.getCommand();
        if (rawCommand == null)
            return;

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();
        String resolvedCommand = resolver.resolve(tc, rawCommand, tc.getVariables(), yamlFile, stepIndex, "command");

        if (resolvedCommand != null && step.getParameters() != null) {
            resolvedCommand = resolver.resolve(tc, resolvedCommand, step.getParameters(), yamlFile, stepIndex,
                    "command", step);
        }

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logger.info(String.format("    %s>>> CMD       %s: %s", ConsoleColors.GREEN, ConsoleColors.RESET,
                    resolvedCommand));
        }

        StringBuilder output = new StringBuilder();
        int exitCode = -1;

        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", resolvedCommand);
            pb.redirectErrorStream(true);

            String trimmedCmd = resolvedCommand.trim();

            if (trimmedCmd.length() > 2 && Character.isLetter(trimmedCmd.charAt(0)) && trimmedCmd.charAt(1) == ':') {

                String executablePath = trimmedCmd;
                if (trimmedCmd.startsWith("\"")) {
                    int nextQuote = trimmedCmd.indexOf("\"", 1);
                    if (nextQuote != -1) {
                        executablePath = trimmedCmd.substring(1, nextQuote);
                    }
                } else {
                    int firstSpace = trimmedCmd.indexOf(" ");
                    if (firstSpace != -1) {
                        executablePath = trimmedCmd.substring(0, firstSpace);
                    }
                }

                File executableFile = new File(executablePath);
                File workingDir = executableFile.getParentFile();

                if (workingDir != null && workingDir.exists() && workingDir.isDirectory()) {
                    pb.directory(workingDir);

                    if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                        logger.info(String.format("    %s>>> WORKDIR   %s: %s", ConsoleColors.BLUE, ConsoleColors.RESET,
                                workingDir.getAbsolutePath()));
                    }
                }
            }

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                        logger.info(String.format("    %s<<< OUT       %s: %s", ConsoleColors.GREEN,
                                ConsoleColors.RESET, line));
                    }
                }
            }
            exitCode = process.waitFor();
            OcCmdResponse responseObj = new OcCmdResponse(exitCode, output.toString());

            if (step.getResponse() != null) {
                context.storeBuffer(step.getResponse(), responseObj);
            }

        } catch (Exception e) {
            throw new RuntimeException("Command execution failed: " + e.getMessage());
        }
    }

    private void handleAssertion(TestCase tc, TestStep step, ExecutionContext context, int stepIndex,
            Boolean printExecution, String yamlFile, TestLogger logger, boolean isVerbose) {

        String responseKey = step.getResponse();
        String action = (step.getAction() != null) ? step.getAction().toUpperCase() : "";
        String expected = (step.getExpected() != null) ? step.getExpected() : "0";
        String value = (step.getValue() != null) ? step.getValue() : "";

        Object rawVal = context.getBuffer(responseKey);
        if (rawVal == null) {
            throw new RuntimeException("No response or buffer found for key: " + responseKey);
        }

        OcCmdResponse cachedResponse = (rawVal instanceof OcCmdResponse) ? (OcCmdResponse) rawVal : null;
        String outputToCheck = (rawVal instanceof OcCmdResponse) ? ((OcCmdResponse) rawVal).getOutput()
                : rawVal.toString();

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            if (action.equals("COUNT") || action.equals("EXITCODE")) {
                logger.info(String.format("    %s>>> ASSERT    %s: %s | Expected: [%s]", ConsoleColors.GREEN,
                        ConsoleColors.RESET, action, expected));
            }
        }

        switch (action) {
        case "EXITCODE":
        case "COUNT":
            if (expected == null) {
                throw new RuntimeException("Assertion [" + action + "] requires an 'expected' field.");
            }
            break;
        case "CONTAINS":
        case "NOT_CONTAINS":
        case "EQUALS":
        case "NOT_EQUALS":
            if (value == null || value.isEmpty()) {
                throw new RuntimeException("Assertion [" + action + "] requires a 'value' field.");
            }
            break;
        default:
            throw new RuntimeException("Unknown assertion type: \"" + action + "\"");
        }

        boolean passed = false;
        String actualValue = "";
        switch (action) {
        case "EXITCODE":
            if (cachedResponse == null) {
                throw new RuntimeException("Assertion [EXITCODE] requires a full CMD response, not a buffer string.");
            }
            String actualExit = String.valueOf(cachedResponse.getExitCode());
            passed = actualExit.equals(expected);
            actualValue = "Exit: " + actualExit;
            break;

        case "CONTAINS":
            passed = outputToCheck.contains(value);
            actualValue = passed ? "String \"" + value + "\" found" : "String \"" + value + "\" NOT found";
            break;

        case "NOT_CONTAINS":
            passed = !outputToCheck.contains(value);
            actualValue = passed ? "String \"" + value + "\" absent" : "String \"" + value + "\" present (Error)";
            break;

        case "EQUALS":
            passed = outputToCheck.trim().equals(value.trim());
            actualValue = passed ? "String equals expected"
                    : "String '" + outputToCheck.trim() + "' does NOT equal '" + value + "'";
            break;

        case "NOT_EQUALS":
            passed = !outputToCheck.trim().equals(value.trim());
            actualValue = passed ? "String does not equal expected" : "String matches unexpected value";
            break;

        case "COUNT":
            int actualCount = countOccurrences(outputToCheck, value);
            passed = (actualCount == Integer.parseInt(expected));
            actualValue = "Count = " + actualCount;
            break;

        default:
            throw new RuntimeException("Unknown assertion type: \"" + action + "\"");
        }

        if (!passed) {
            if (action.equals("COUNT") || action.equals("EXITCODE")) {
                throw new RuntimeException(
                        "Assertion " + action + " failed! Expected: " + expected + ", Actual: " + actualValue);
            } else {
                throw new RuntimeException("Assertion " + action + " failed! " + actualValue);
            }
        }
    }

    private void handleBuffer(TestCase tc, TestStep step, ExecutionContext context, int stepIndex,
            Boolean printExecution, String yamlFile, TestLogger logger, boolean isVerbose) {

        String responseKey = step.getResponse();
        String action = (step.getAction() != null) ? step.getAction().toUpperCase() : "TEXT";
        String name = step.getName();

        String rawValue = step.getValue();

        if ((rawValue == null || rawValue.trim().isEmpty()) && (action.equals("LINE") || action.equals("LAST_LINE"))) {
            rawValue = "0";
        }

        int parsedValue = 0;
        if (action.equals("LINE") || action.equals("LAST_LINE")) {
            try {
                parsedValue = Integer.parseInt(rawValue);
            } catch (NumberFormatException e) {
                throw new RuntimeException(
                        "Action [" + action + "] requires a valid numeric value, but got: \"" + rawValue + "\"");
            }
        }

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();
        String resolvedResponseKey = resolver.resolve(tc, responseKey, tc.getVariables(), yamlFile, stepIndex,
                "response");

        OcCmdResponse cachedResponse = context.getResponse(resolvedResponseKey, OcCmdResponse.class);
        if (cachedResponse == null) {
            logger.error(String.format("    %s>>> ERROR     %s: Response [%s] not found!", ConsoleColors.RED,
                    ConsoleColors.RESET, responseKey));
            throw new RuntimeException("No response found for key: " + responseKey);
        }

        String fullOutput = cachedResponse.getOutput().trim();
        String[] lines = fullOutput.split("\\R");

        validateBufferParameters(action, rawValue, lines.length);

        String result = "";

        switch (action) {
        case "COUNT":
            result = String.valueOf(countOccurrences(fullOutput, (rawValue != null ? rawValue : "")));
            break;

        case "FILTER":
            StringBuilder filtered = new StringBuilder();
            String[] filterLines = fullOutput.split("\\R");
            String searchStr = (rawValue != null ? rawValue : "");
            for (String line : filterLines) {
                if (line.contains(searchStr)) {
                    filtered.append(line).append("\n");
                }
            }
            result = filtered.toString().trim();
            break;

        case "LAST_LINE":
            String[] lastLines = fullOutput.split("\\R");
            result = lastLines[lastLines.length - 1 - parsedValue].trim();
            break;

        case "LINE":
            String[] normalLines = fullOutput.split("\\R");
            result = normalLines[parsedValue].trim();
            break;

        case "TEXT":
        default:
            result = fullOutput;
            break;
        }

        context.storeBuffer(name, result);

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            if (action.equals("TEXT")) {
                logger.info(String.format("    %s>>> BUFFER    %s: [%s] | Save Full Output to [%s]",
                        ConsoleColors.GREEN, ConsoleColors.RESET, responseKey, name));
            } else {
                logger.info(String.format("    %s>>> BUFFER    %s: [%s] | %s [%s] to [%s]", ConsoleColors.GREEN,
                        ConsoleColors.RESET, responseKey, action, rawValue, name));
            }

            if (result != null && !result.isEmpty()) {
                String[] resultLines = result.split("\\R");
                for (String line : resultLines) {
                    logger.info(String.format("    %s<<< OUT       %s: %s", ConsoleColors.GREEN, ConsoleColors.RESET,
                            line));
                }
            }
        }
    }

    private void validateBufferParameters(String action, String rawValue, int maxLines) {
        if (!action.equals("LINE") && !action.equals("LAST_LINE")) {
            return;
        }

        if (rawValue == null || rawValue.isEmpty()) {
            throw new RuntimeException(
                    "BUFFER error: Action '" + action + "' requires a numeric 'value', but none was provided.");
        }

        try {
            int parsedValue = Integer.parseInt(rawValue);

            if (parsedValue < 0) {
                throw new RuntimeException(
                        "BUFFER error: Value '" + rawValue + "' for action '" + action + "' must be >= 0.");
            }

            if (parsedValue >= maxLines) {
                throw new RuntimeException("BUFFER error: Value '" + rawValue + "' for action '" + action
                        + "' is out of bounds. Output contains only " + maxLines + " lines.");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "BUFFER error: Value '" + rawValue + "' for action '" + action + "' is not a valid integer.");
        }
    }

    private int countOccurrences(String text, String search) {
        if (text == null || search == null || search.isEmpty())
            return 0;

        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(search, idx)) != -1) {
            count++;
            idx += search.length();
        }
        return count;
    }
}