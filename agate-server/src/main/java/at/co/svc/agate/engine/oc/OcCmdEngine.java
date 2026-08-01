package at.co.svc.agate.engine.oc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.env.EnvironmentManager;
import at.co.svc.agate.core.interfaces.TestLogger;

/**
 * Universal Command Engine for local OpenShift OC commands. Supports EXEC
 * (execution), ASSERT (validation), BUFFER (data extraction), GET (get files
 * from OpensShift) and PUT (put files to OpenShift).
 */
public class OcCmdEngine extends AbstractStepEngine {

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.OC;
    }

    @Override
    public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(logger, step);
        }

        String op = (step.getOp() != null) ? step.getOp().toUpperCase() : "EXEC";

        switch (op) {
        case "EXEC":
            handleExecution(tc, step, context, yamlFile, stepIndex, printExecution, logger, isVerbose);
            break;
        case "PUT":
        case "GET":
            handleTransfer(tc, step, context, yamlFile, stepIndex, printExecution, logger, isVerbose);
            break;
        case "ASSERT":
            handleAssertion(step, context, stepIndex, printExecution, logger, isVerbose);
            break;
        case "BUFFER":
            handleBuffer(tc, step, context, stepIndex, printExecution, logger, isVerbose);
            break;
        default:
            throw new RuntimeException("Unsupported OC operation: " + op);
        }
    }

    private void handleExecution(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        String rawCommand = step.getCommand();
        if (rawCommand == null)
            return;

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();

        Map<String, Object> combinedVars = new HashMap<>(
                tc.getVariables() != null ? tc.getVariables() : new HashMap<>());
        if (context != null && context.getVars() != null) {
            combinedVars.putAll(context.getVars());
        }

        String resolvedCommand = resolver.resolve(tc, rawCommand, combinedVars, yamlFile, stepIndex, rawCommand);
        if (resolvedCommand != null && step.getParameters() != null) {
            resolvedCommand = resolver.resolve(tc, resolvedCommand, step.getParameters(), yamlFile, stepIndex,
                    "command", step);
        }

        String pod = step.getPod();
        String namespace = step.getNamespace();
        String namespaceEnv = EnvironmentManager.getEnvValue("env.openShift.namespace");
        namespace = (namespace != null ? namespace : namespaceEnv);
        String ns = resolver.resolve(tc, namespace, combinedVars, yamlFile, stepIndex, "");

        String podresolved = resolver.resolve(tc, pod, step.getParameters(), yamlFile, stepIndex, "command", step);
        podresolved = resolver.resolve(tc, podresolved, tc.getVariables(), yamlFile, stepIndex, "command");

        String activePod = getActivePodName(podresolved, ns);

        String execTemplate = String.format("oc exec %s --namespace %s -- bash -c \"%s\"", activePod, ns,
                resolvedCommand);
        String fullExecCmd = resolver.resolve(tc, execTemplate, combinedVars, yamlFile, stepIndex, resolvedCommand);

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logger.info(String.format("    %s>>> OC EXEC    %s: [%s // %s]: %s", ConsoleColors.GREEN,
                    ConsoleColors.RESET, ns, activePod, resolvedCommand));
        }

        try {
            OcCmdResponse responseObj = runWindowsCommand(fullExecCmd);

            if (isLoginRequired(responseObj.getOutput())) {
                handleLazyLogin(tc, context, resolver, yamlFile, stepIndex, logger,
                        printExecution != null && printExecution);

                activePod = getActivePodName(pod, ns);
                execTemplate = String.format("oc exec %s --namespace %s -- bash -c \"%s\"", activePod, ns,
                        resolvedCommand);
                fullExecCmd = resolver.resolve(tc, execTemplate, combinedVars, yamlFile, stepIndex, resolvedCommand);

                if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                    logger.info(String.format("    %s>>> AUTH        %s: Retrying EXEC with active pod: %s",
                            ConsoleColors.YELLOW, ConsoleColors.RESET, activePod));
                }
                responseObj = runWindowsCommand(fullExecCmd);
            }

            if (step.getResponse() != null) {
                context.storeBuffer(step.getResponse(), responseObj);
            }

            if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                logNormalizedOutput(responseObj.getOutput(), logger, ConsoleColors.GREEN);

                String statusColor = (responseObj.getExitCode() == 0) ? ConsoleColors.GREEN : ConsoleColors.RED;
                logger.info(String.format("    %s>>> RESULT    %s: %s (Exit: %d)", statusColor, ConsoleColors.RESET,
                        (responseObj.getExitCode() == 0 ? "SUCCESS" : "FAILED"), responseObj.getExitCode()));
            }

        } catch (Exception e) {
            throw new RuntimeException("Command execution failed: " + e.getMessage());
        }
    }

    private void handleTransfer(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        if (step.getPod() == null) {
            throw new RuntimeException("Pod Name not defined");
        }

        Map<String, Object> combinedVars = new HashMap<>(
                tc.getVariables() != null ? tc.getVariables() : new HashMap<>());
        if (context != null && context.getVars() != null) {
            combinedVars.putAll(context.getVars());
        }

        String namespace = step.getNamespace();
        String namespaceEnv = EnvironmentManager.getEnvValue("env.openShift.namespace");
        namespace = (namespace != null ? namespace : namespaceEnv);

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();
        String ns = resolver.resolve(tc, namespace, combinedVars, yamlFile, stepIndex, "");
        String op = step.getOp().toUpperCase();

        String resolvedPodBase = resolver.resolve(tc, step.getPod().trim(), combinedVars, yamlFile, stepIndex, "");
        String podName = getActivePodName(resolvedPodBase, ns);

        String from = resolver.resolve(tc, step.getFrom(), combinedVars, yamlFile, stepIndex, "");
        String to = resolver.resolve(tc, step.getTo(), combinedVars, yamlFile, stepIndex, "");

        String fullCmd = op.equals("PUT")
                ? String.format("oc cp \"%s\" %s/%s:\"%s\"", stripDriveLetter(from), ns, podName, to)
                : String.format("oc cp %s/%s:\"%s\" \"%s\"", ns, podName, from, stripDriveLetter(to));

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logger.info(String.format("    %s>>> OC %-7s%s: [%s // %s]: %s TO %s", ConsoleColors.GREEN, op,
                    ConsoleColors.RESET, ns, podName, from, to));
        }

        try {
            OcCmdResponse res = runWindowsCommand(fullCmd);

            if (isLoginRequired(res.getOutput())) {
                handleLazyLogin(tc, context, resolver, yamlFile, stepIndex, logger,
                        printExecution != null && printExecution);

                podName = getActivePodName(resolvedPodBase, ns);
                fullCmd = op.equals("PUT")
                        ? String.format("oc cp \"%s\" %s/%s:\"%s\"", stripDriveLetter(from), ns, podName, to)
                        : String.format("oc cp %s/%s:\"%s\" \"%s\"", ns, podName, from, stripDriveLetter(to));

                if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                    logger.info(String.format("    %s>>> AUTH        %s: Retrying %s with active pod: %s",
                            ConsoleColors.YELLOW, ConsoleColors.RESET, op, podName));
                }
                res = runWindowsCommand(fullCmd);
            }

            if (step.getResponse() != null) {
                context.storeBuffer(step.getResponse(), res);
            }

            if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                logNormalizedOutput(res.getOutput(), logger, ConsoleColors.GREEN);

                String statusColor = (res.getExitCode() == 0) ? ConsoleColors.GREEN : ConsoleColors.RED;
                logger.info(String.format("    %s>>> RESULT    %s: %s (Exit: %d)", statusColor, ConsoleColors.RESET,
                        (res.getExitCode() == 0 ? "SUCCESS" : "FAILED"), res.getExitCode()));
            }
        } catch (Exception e) {
            throw new RuntimeException("OC " + op + " failed: " + e.getMessage());
        }
    }

    private void handleAssertion(TestStep step, ExecutionContext context, int stepIndex, Boolean printExecution,
            TestLogger logger, boolean isVerbose) {

        String responseKey = step.getResponse();
        String action = (step.getAction() != null) ? step.getAction().toUpperCase() : "";
        String expected = (step.getExpected() != null) ? step.getExpected() : "0";
        String value = (step.getValue() != null) ? step.getValue() : "";

        OcCmdResponse cachedResponse = context.getResponse(responseKey, OcCmdResponse.class);

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            if (action.equalsIgnoreCase("EXITCODE")) {
                logger.info(String.format("    %s>>> ASSERT    %s: [%s] %s \"%s\"", ConsoleColors.GREEN,
                        ConsoleColors.RESET, responseKey, action, expected));
            } else if (action.equalsIgnoreCase("COUNT")) {
                logger.info(String.format("    %s>>> ASSERT    %s: [%s] %s \"%s\" (Expected: %s)", ConsoleColors.GREEN,
                        ConsoleColors.RESET, responseKey, action, value, expected));
            } else {
                logger.info(String.format("    %s>>> ASSERT    %s: [%s] %s \"%s\"", ConsoleColors.GREEN,
                        ConsoleColors.RESET, responseKey, action, value));
            }
        }

        if (cachedResponse == null) {
            if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                logger.info(String.format("    %s>>> RESULT    %s: FAILED (No response found for key: %s)",
                        ConsoleColors.RED, ConsoleColors.RESET, responseKey));
            }
            throw new RuntimeException("No response found for key: " + responseKey);
        }

        boolean passed = false;
        String actualValue = "";

        if (!action.equals("EXITCODE") && !action.equals("CONTAINS") && !action.equals("NOT_CONTAINS")
                && !action.equals("EQUALS") && !action.equals("NOT_EQUALS") && !action.equals("COUNT")) {
            String errorMsg = "Unknown assertion type: \"" + action + "\"";
            if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                logger.info(String.format("    %s>>> RESULT    %s: FAILED (%s)", ConsoleColors.RED, ConsoleColors.RESET,
                        errorMsg));
            }
            throw new RuntimeException(errorMsg);
        }

        switch (action) {
        case "EXITCODE":
            String actualExit = String.valueOf(cachedResponse.getExitCode());
            passed = (actualExit.equals(expected));
            actualValue = "Exit: " + actualExit;
            break;
        case "CONTAINS":
            passed = cachedResponse.getOutput().contains(value);
            actualValue = passed ? "String \"" + value + "\" found" : "String \"" + value + "\" NOT found";
            break;
        case "NOT_CONTAINS":
            passed = !cachedResponse.getOutput().contains(value);
            actualValue = passed ? "String \"" + value + "\" absent" : "String \"" + value + "\" present (Error)";
            break;
        case "EQUALS":
            passed = cachedResponse.getOutput().trim().equals(value.trim());
            actualValue = passed ? "Output equals \"" + value + "\""
                    : "Output \"" + cachedResponse.getOutput().trim() + "\" does not equal \"" + value + "\"";
            break;
        case "NOT_EQUALS":
            passed = !cachedResponse.getOutput().trim().equals(value.trim());
            actualValue = passed ? "Output does not equal \"" + value + "\""
                    : "Output equals \"" + value + "\" (Error)";
            break;
        case "COUNT":
            int actualCount = countOccurrences(cachedResponse.getOutput(), value);
            passed = (actualCount == Integer.parseInt(expected));
            actualValue = passed ? "String \"" + value + "\" count = " + actualCount + " (expected: " + expected + ")"
                    : "String \"" + value + "\" count = " + actualCount + " (expected: " + expected + ")";
            break;
        }

        if (passed) {
            if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                logger.info(String.format("    %s>>> RESULT    %s: SUCCESS (%s)", ConsoleColors.GREEN,
                        ConsoleColors.RESET, actualValue));
            }
        } else {
            if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                logger.info(String.format("    %s>>> RESULT    %s: FAILED (%s)", ConsoleColors.RED, ConsoleColors.RESET,
                        actualValue));
            }
            throw new RuntimeException(
                    String.format("Assertion %s failed! Expected: %s, Actual: %s", action, expected, actualValue));
        }
    }

    private void handleBuffer(TestCase tc, TestStep step, ExecutionContext context, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) {

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

        OcCmdResponse res = (OcCmdResponse) context.getBuffer(responseKey);
        if (res == null) {
            throw new RuntimeException("No response found in buffer for key: " + responseKey);
        }

        String fullOutput = res.getOutput().trim();
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
            if ("TEXT".equals(action)) {
                logger.info(String.format("    %s>>> BUFFER    %s: [%s] Store entire output to variable [%s]",
                        ConsoleColors.GREEN, ConsoleColors.RESET, responseKey, name));
            } else {
                logger.info(String.format("    %s>>> BUFFER    %s: [%s] %s \"%s\" to variable [%s]",
                        ConsoleColors.GREEN, ConsoleColors.RESET, responseKey, action, rawValue, name));
            }

            if (result != null && !result.isEmpty()) {
                String[] resultLines = result.split("\\R");
                for (String line : resultLines) {
                    logger.info(String.format("    %s<<< OUT       %s: %s", ConsoleColors.GREEN, ConsoleColors.RESET,
                            line));
                }
            }
            logger.info(String.format("    %s>>> RESULT    %s: SUCCESS (Buffered)", ConsoleColors.GREEN,
                    ConsoleColors.RESET));
        }
    }

    private void handleLazyLogin(TestCase tc, ExecutionContext context, YamlPlaceholderResolver res, String file,
            int idx, TestLogger logger, boolean print) throws Exception {

        Map<String, Object> combinedVars = new HashMap<>(
                tc.getVariables() != null ? tc.getVariables() : new HashMap<>());
        if (context != null && context.getVars() != null) {
            combinedVars.putAll(context.getVars());
        }

        String server = res.resolve(tc, "{E[env.openShift.loginServer]}", combinedVars, file, idx, "");
        String username = res.resolve(tc, "{E[env.openShift.username]}", combinedVars, file, idx, "");
        String password = res.resolve(tc, "{E[env.openShift.password]}", combinedVars, file, idx, "");

        if (print) {
            logger.info(String.format("    %s>>> AUTH       %s: Session expired. Lazy Login Triggered...",
                    ConsoleColors.YELLOW, ConsoleColors.RESET));
            logger.info(String.format("    %s>>> AUTH       %s: Server: %s | User: %s | Pwd: ****",
                    ConsoleColors.YELLOW, ConsoleColors.RESET, server, username));
        }

        String loginCmd = String.format("oc login %s --username=%s --password=%s --insecure-skip-tls-verify", server,
                username, password);
        OcCmdResponse loginRes = runWindowsCommand(loginCmd);

        if (print && loginRes.getOutput() != null) {
            String[] loginLines = loginRes.getOutput().trim().split("\\r?\\n");
            for (String line : loginLines) {
                if (!line.trim().isEmpty()) {
                    logger.info(String.format("    %s<<< OUT       %s: [AUTH] %s", ConsoleColors.YELLOW,
                            ConsoleColors.RESET, line.trim()));
                }
            }
        }

        String targetNamespace = res.resolve(tc, "{E[env.openShift.namespace]}", combinedVars, file, idx, "");
        if (targetNamespace == null || targetNamespace.trim().isEmpty() || targetNamespace.contains("{E[")) {
            targetNamespace = "kvw-int-app";
        }

        if (print) {
            logger.info(String.format("    %s>>> AUTH       %s: Setting active project to: %s", ConsoleColors.YELLOW,
                    ConsoleColors.RESET, targetNamespace));
        }
        runWindowsCommand("oc project " + targetNamespace);
    }

    private void logNormalizedOutput(String output, TestLogger logger, String color) {
        if (output == null || output.trim().isEmpty())
            return;
        String[] lines = output.trim().split("\\R");
        for (String line : lines) {
            logger.info(String.format("    %s<<< OUT       %s: %s", color, ConsoleColors.RESET, line));
        }
    }

    private OcCmdResponse runWindowsCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process p = new ProcessBuilder("cmd.exe", "/c", command).redirectErrorStream(true).start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null)
                    output.append(line).append("\n");
            }
            return new OcCmdResponse(p.waitFor(), output.toString());
        } catch (Exception e) {
            return new OcCmdResponse(1, "SHELL_ERROR: " + e.getMessage());
        }
    }

    private String stripDriveLetter(String path) {
        if (path != null && path.length() > 1 && path.charAt(1) == ':')
            return path.substring(2);
        return path;
    }

    private String getActivePodName(String service, String ns) throws Exception {
        String cmd = String.format(
                "oc get pods -n %s --field-selector=status.phase=Running -o custom-columns=:metadata.name --no-headers",
                ns);
        OcCmdResponse res = runWindowsCommand(cmd);
        String[] lines = res.getOutput().split("\\r?\\n");
        for (String line : lines) {
            if (line.trim().startsWith(service))
                return line.trim();
        }
        return service;
    }

    private boolean isLoginRequired(String out) {
        if (out == null || out.trim().isEmpty()) {
            return false;
        }
        String lowerOut = out.toLowerCase();
        return lowerOut.contains("you must be logged in") || lowerOut.contains("unauthorized")
                || lowerOut.contains("system:anonymous") || lowerOut.contains("log in")
                || lowerOut.contains("forbidden");
    }

    private int countOccurrences(String text, String search) {
        if (text == null || search == null || search.isEmpty())
            return 0;
        int count = 0, idx = 0;
        while ((idx = text.indexOf(search, idx)) != -1) {
            count++;
            idx += search.length();
        }
        return count;
    }

}