package at.co.svc.agate.engine.oc;

import java.util.HashMap;
import java.util.Map;

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
import at.co.svc.agate.core.env.EnvironmentManager;
import at.co.svc.agate.core.interfaces.TestLogger;

public class OcCmdEngine extends AbstractStepEngine {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.OC;
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

        case "PUT":
        case "GET":
            handleTransfer(
                    tc,
                    step,
                    context,
                    yamlFile,
                    stepIndex,
                    printExecution,
                    logger,
                    isVerbose);
            break;

        case "ASSERT":
            handleAssertion(
                    step,
                    context,
                    printExecution,
                    logger,
                    isVerbose);
            break;

        case "BUFFER":
            handleBuffer(
                    step,
                    context,
                    printExecution,
                    logger,
                    isVerbose);
            break;

        default:
            throw new RuntimeException(
                    "Unsupported OC operation: " + op);
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

        if (step.getCommand() == null
                || step.getCommand().isBlank()) {
            throw new RuntimeException(
                    "OC EXEC requires 'command'.");
        }

        if (step.getPod() == null
                || step.getPod().isBlank()) {
            throw new RuntimeException(
                    "OC EXEC requires 'pod'.");
        }

        YamlPlaceholderResolver resolver =
                new YamlPlaceholderResolver();

        Map<String, Object> combinedVars =
                new HashMap<>();

        if (tc.getVariables() != null) {
            combinedVars.putAll(
                    tc.getVariables());
        }

        if (context != null
                && context.getVars() != null) {
            combinedVars.putAll(
                    context.getVars());
        }

        String resolvedCommand =
                resolver.resolve(
                        tc,
                        step.getCommand(),
                        combinedVars,
                        yamlFile,
                        stepIndex,
                        "command");

        if (step.getParameters() != null) {
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

        String namespace =
                step.getNamespace();

        if (namespace == null
                || namespace.isBlank()) {
            namespace =
                    EnvironmentManager.getEnvValue(
                            "env.openShift.namespace");
        }

        String resolvedNamespace =
                resolver.resolve(
                        tc,
                        namespace,
                        combinedVars,
                        yamlFile,
                        stepIndex,
                        "namespace");

        String resolvedPod =
                resolver.resolve(
                        tc,
                        step.getPod(),
                        combinedVars,
                        yamlFile,
                        stepIndex,
                        "pod");

        if (step.getParameters() != null) {
            resolvedPod =
                    resolver.resolve(
                            tc,
                            resolvedPod,
                            step.getParameters(),
                            yamlFile,
                            stepIndex,
                            "pod",
                            step);
        }

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

        String activePod =
                getActivePodName(
                        resolvedPod,
                        resolvedNamespace,
                        timeout);

        String command =
                String.format(
                        "oc exec %s --namespace %s -- bash -c \"%s\"",
                        activePod,
                        resolvedNamespace,
                        escapeForWindowsCommand(
                                resolvedCommand));

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logger.info(
                    String.format(
                            "    %s>>> OC EXEC   %s: [%s // %s]: %s",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET,
                            resolvedNamespace,
                            activePod,
                            resolvedCommand));
        }

        CommandResult result =
                runCommand(
                        command,
                        timeout);

        if (isLoginRequired(
                result.getOutput())) {

            handleLazyLogin(
                    tc,
                    context,
                    resolver,
                    yamlFile,
                    stepIndex,
                    logger,
                    Boolean.TRUE.equals(
                            printExecution),
                    timeout);

            activePod =
                    getActivePodName(
                            resolvedPod,
                            resolvedNamespace,
                            timeout);

            command =
                    String.format(
                            "oc exec %s --namespace %s -- bash -c \"%s\"",
                            activePod,
                            resolvedNamespace,
                            escapeForWindowsCommand(
                                    resolvedCommand));

            result =
                    runCommand(
                            command,
                            timeout);
        }

        if (step.getResponse() != null) {
            context.storeBuffer(
                    step.getResponse(),
                    result);
        }

        if (step.getOutputFile() != null
                && !step.getOutputFile().isBlank()) {

            String resolvedOutputFile =
                    resolver.resolve(
                            tc,
                            step.getOutputFile(),
                            combinedVars,
                            yamlFile,
                            stepIndex,
                            "outputFile");

            CommandExecutionSupport.writeOutputFile(
                    resolvedOutputFile,
                    result.getOutput(),
                    null);
        }

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logNormalizedOutput(
                    result.getOutput(),
                    logger,
                    ConsoleColors.GREEN);

            logger.info(
                    String.format(
                            "    %s>>> RESULT    %s: Exit=%d, Timeout=%s, Duration=%d ms",
                            result.getExitCode() == expectedExitCode
                                            && !result.isTimedOut()
                                    ? ConsoleColors.GREEN
                                    : ConsoleColors.RED,
                            ConsoleColors.RESET,
                            result.getExitCode(),
                            result.isTimedOut(),
                            result.getDurationMs()));
        }

        if (result.isTimedOut()) {
            throw new RuntimeException(
                    "OC command timed out after "
                            + timeout
                            + " seconds.");
        }

        if (checkExitCode
                && result.getExitCode()
                        != expectedExitCode) {

            throw new RuntimeException(
                    "OC command failed. Expected exit code "
                            + expectedExitCode
                            + " but got "
                            + result.getExitCode());
        }
    }

    private void handleTransfer(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) throws Exception {

        String op =
                step.getOp().toUpperCase();

        if (step.getPod() == null
                || step.getPod().isBlank()) {
            throw new RuntimeException(
                    "OC " + op + " requires 'pod'.");
        }

        if (step.getFrom() == null
                || step.getFrom().isBlank()) {
            throw new RuntimeException(
                    "OC " + op + " requires 'from'.");
        }

        if (step.getTo() == null
                || step.getTo().isBlank()) {
            throw new RuntimeException(
                    "OC " + op + " requires 'to'.");
        }

        Map<String, Object> combinedVars =
                new HashMap<>();

        if (tc.getVariables() != null) {
            combinedVars.putAll(
                    tc.getVariables());
        }

        if (context != null
                && context.getVars() != null) {
            combinedVars.putAll(
                    context.getVars());
        }

        String namespace =
                step.getNamespace();

        if (namespace == null
                || namespace.isBlank()) {
            namespace =
                    EnvironmentManager.getEnvValue(
                            "env.openShift.namespace");
        }

        YamlPlaceholderResolver resolver =
                new YamlPlaceholderResolver();

        String resolvedNamespace =
                resolver.resolve(
                        tc,
                        namespace,
                        combinedVars,
                        yamlFile,
                        stepIndex,
                        "namespace");

        String resolvedPodBase =
                resolver.resolve(
                        tc,
                        step.getPod().trim(),
                        combinedVars,
                        yamlFile,
                        stepIndex,
                        "pod");

        if (step.getParameters() != null) {
            resolvedPodBase =
                    resolver.resolve(
                            tc,
                            resolvedPodBase,
                            step.getParameters(),
                            yamlFile,
                            stepIndex,
                            "pod",
                            step);
        }

        String from =
                resolver.resolve(
                        tc,
                        step.getFrom(),
                        combinedVars,
                        yamlFile,
                        stepIndex,
                        "from");

        if (step.getParameters() != null) {
            from =
                    resolver.resolve(
                            tc,
                            from,
                            step.getParameters(),
                            yamlFile,
                            stepIndex,
                            "from",
                            step);
        }

        String to =
                resolver.resolve(
                        tc,
                        step.getTo(),
                        combinedVars,
                        yamlFile,
                        stepIndex,
                        "to");

        if (step.getParameters() != null) {
            to =
                    resolver.resolve(
                            tc,
                            to,
                            step.getParameters(),
                            yamlFile,
                            stepIndex,
                            "to",
                            step);
        }

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

        String podName =
                getActivePodName(
                        resolvedPodBase,
                        resolvedNamespace,
                        timeout);

        String fullCommand =
                createTransferCommand(
                        op,
                        resolvedNamespace,
                        podName,
                        from,
                        to);

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logger.info(
                    String.format(
                            "    %s>>> OC %-7s%s: [%s // %s]: %s -> %s",
                            ConsoleColors.GREEN,
                            op,
                            ConsoleColors.RESET,
                            resolvedNamespace,
                            podName,
                            from,
                            to));
        }

        CommandResult result =
                runCommand(
                        fullCommand,
                        timeout);

        if (isLoginRequired(
                result.getOutput())) {

            handleLazyLogin(
                    tc,
                    context,
                    resolver,
                    yamlFile,
                    stepIndex,
                    logger,
                    Boolean.TRUE.equals(
                            printExecution),
                    timeout);

            podName =
                    getActivePodName(
                            resolvedPodBase,
                            resolvedNamespace,
                            timeout);

            fullCommand =
                    createTransferCommand(
                            op,
                            resolvedNamespace,
                            podName,
                            from,
                            to);

            result =
                    runCommand(
                            fullCommand,
                            timeout);
        }

        if (step.getResponse() != null) {
            context.storeBuffer(
                    step.getResponse(),
                    result);
        }

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            String resultColor =
                    result.getExitCode() == expectedExitCode
                                    && !result.isTimedOut()
                            ? ConsoleColors.GREEN
                            : ConsoleColors.RED;

            logNormalizedOutput(
                    result.getOutput(),
                    logger,
                    resultColor);

            logger.info(
                    String.format(
                            "    %s>>> RESULT    %s: Exit=%d, Timeout=%s, Duration=%d ms",
                            resultColor,
                            ConsoleColors.RESET,
                            result.getExitCode(),
                            result.isTimedOut(),
                            result.getDurationMs()));
        }

        if (result.isTimedOut()) {
            throw new RuntimeException(
                    "OC "
                            + op
                            + " timed out after "
                            + timeout
                            + " seconds.");
        }

        if (checkExitCode
                && result.getExitCode()
                        != expectedExitCode) {

            throw new RuntimeException(
                    "OC "
                            + op
                            + " failed. Expected exit code "
                            + expectedExitCode
                            + " but got "
                            + result.getExitCode());
        }
    }

    private String createTransferCommand(
            String op,
            String namespace,
            String podName,
            String from,
            String to) {

        if ("PUT".equals(op)) {

            String localFrom =
                    normalizeWindowsPathForOcCp(from);

            return String.format(
                    "oc cp \"%s\" %s/%s:\"%s\"",
                    localFrom,
                    namespace,
                    podName,
                    to);
        }

        String localTo =
                normalizeWindowsPathForOcCp(to);

        return String.format(
                "oc cp %s/%s:\"%s\" \"%s\"",
                namespace,
                podName,
                from,
                localTo);
    }
    
    private String normalizeWindowsPathForOcCp(
            String path) {

        if (path == null || path.isBlank()) {
            return path;
        }

        if (path.matches("^[A-Za-z]:\\\\.*")) {
            return path.substring(2);
        }

        return path;
    }
    private void handleAssertion(
            TestStep step,
            ExecutionContext context,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) {

        String action =
                step.getAction() != null
                        ? step.getAction().toUpperCase()
                        : "";

        Object raw =
                context.getBuffer(
                        step.getResponse());

        if (raw == null) {
            throw new RuntimeException(
                    "No response found for key: "
                            + step.getResponse());
        }

        CommandResult result =
                raw instanceof CommandResult
                        ? (CommandResult) raw
                        : null;

        String output =
                result != null
                        ? result.getOutput()
                        : raw.toString();

        String expected =
                step.getExpected() != null
                        ? step.getExpected()
                        : "0";

        String value =
                step.getValue() != null
                        ? step.getValue()
                        : "";

        switch (action) {

        case "EXITCODE":

            if (result == null) {
                throw new RuntimeException(
                        "EXITCODE requires an OC command response.");
            }

            if (result.getExitCode()
                    != Integer.parseInt(expected)) {

                throw new RuntimeException(
                        "Assertion EXITCODE failed. Expected "
                                + expected
                                + " but got "
                                + result.getExitCode());
            }

            break;

        case "CONTAINS":

            if (!output.contains(value)) {
                throw new RuntimeException(
                        "Assertion CONTAINS failed.");
            }

            break;

        case "NOT_CONTAINS":

            if (output.contains(value)) {
                throw new RuntimeException(
                        "Assertion NOT_CONTAINS failed.");
            }

            break;

        case "EQUALS":

            if (!output.trim()
                    .equals(value.trim())) {

                throw new RuntimeException(
                        "Assertion EQUALS failed.");
            }

            break;

        case "NOT_EQUALS":

            if (output.trim()
                    .equals(value.trim())) {

                throw new RuntimeException(
                        "Assertion NOT_EQUALS failed.");
            }

            break;

        case "COUNT":

            if (countOccurrences(
                    output,
                    value)
                    != Integer.parseInt(expected)) {

                throw new RuntimeException(
                        "Assertion COUNT failed.");
            }

            break;

        default:
            throw new RuntimeException(
                    "Unknown assertion type: "
                            + action);
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
            TestStep step,
            ExecutionContext context,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) {

        Object raw =
                context.getBuffer(
                        step.getResponse());

        if (!(raw instanceof CommandResult)) {
            throw new RuntimeException(
                    "No OC response found for key: "
                            + step.getResponse());
        }

        CommandResult commandResult =
                (CommandResult) raw;

        String output =
                commandResult.getOutput() == null
                        ? ""
                        : commandResult.getOutput().trim();

        String action =
                step.getAction() != null
                        ? step.getAction().toUpperCase()
                        : "TEXT";

        String value =
                step.getValue();

        if ((value == null
                || value.isBlank())
                && ("LINE".equals(action)
                        || "LAST_LINE".equals(action))) {

            value = "0";
        }

        String buffered;

        switch (action) {

        case "TEXT":
            buffered = output;
            break;

        case "COUNT":
            buffered =
                    String.valueOf(
                            countOccurrences(
                                    output,
                                    value != null
                                            ? value
                                            : ""));
            break;

        case "FILTER":
            buffered =
                    filterLines(
                            output,
                            value != null
                                    ? value
                                    : "");
            break;

        case "LINE":
            buffered =
                    getLine(
                            output,
                            Integer.parseInt(value));
            break;

        case "LAST_LINE":
            buffered =
                    getLastLine(
                            output,
                            Integer.parseInt(value));
            break;

        default:
            throw new RuntimeException(
                    "Unsupported OC BUFFER action: "
                            + action);
        }

        context.storeBuffer(
                step.getName(),
                buffered);

        if (Boolean.TRUE.equals(printExecution)
                && isVerbose) {

            logger.info(
                    String.format(
                            "    %s>>> RESULT    %s: SUCCESS (Buffered)",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET));
        }
    }

    private CommandResult runCommand(
            String command,
            int timeout) throws Exception {

        return CommandExecutionSupport.executeWindowsShell(
                command,
                null,
                timeout);
    }

    private String getActivePodName(
            String service,
            String namespace,
            int timeout) throws Exception {

        String command =
                String.format(
                        "oc get pods -n %s --field-selector=status.phase=Running -o custom-columns=:metadata.name --no-headers",
                        namespace);

        CommandResult result =
                runCommand(
                        command,
                        timeout);

        for (String line :
                result.getOutput()
                        .split("\\R")) {

            if (line.trim()
                    .startsWith(service)) {
                return line.trim();
            }
        }

        return service;
    }

    private void handleLazyLogin(
            TestCase tc,
            ExecutionContext context,
            YamlPlaceholderResolver resolver,
            String yamlFile,
            int stepIndex,
            TestLogger logger,
            boolean print,
            int timeout) throws Exception {

        Map<String, Object> combinedVars =
                new HashMap<>();

        if (tc.getVariables() != null) {
            combinedVars.putAll(
                    tc.getVariables());
        }

        if (context != null
                && context.getVars() != null) {
            combinedVars.putAll(
                    context.getVars());
        }

        String server =
                resolver.resolve(
                        tc,
                        "{E[env.openShift.loginServer]}",
                        combinedVars,
                        yamlFile,
                        stepIndex,
                        "loginServer");

        String username =
                resolver.resolve(
                        tc,
                        "{E[env.openShift.username]}",
                        combinedVars,
                        yamlFile,
                        stepIndex,
                        "username");

        String password =
                resolver.resolve(
                        tc,
                        "{E[env.openShift.password]}",
                        combinedVars,
                        yamlFile,
                        stepIndex,
                        "password");

        String loginCommand =
                String.format(
                        "oc login %s --username=%s --password=%s --insecure-skip-tls-verify",
                        server,
                        username,
                        password);

        runCommand(
                loginCommand,
                timeout);

        String namespace =
                resolver.resolve(
                        tc,
                        "{E[env.openShift.namespace]}",
                        combinedVars,
                        yamlFile,
                        stepIndex,
                        "namespace");

        if (namespace != null
                && !namespace.isBlank()
                && !namespace.contains("{E[")) {

            runCommand(
                    "oc project "
                            + namespace,
                    timeout);
        }

        if (print) {
            logger.info(
                    String.format(
                            "    %s>>> AUTH      %s: Login completed",
                            ConsoleColors.YELLOW,
                            ConsoleColors.RESET));
        }
    }

    private boolean isLoginRequired(
            String output) {

        if (output == null
                || output.isBlank()) {
            return false;
        }

        String lower =
                output.toLowerCase();

        return lower.contains(
                        "you must be logged in")
                || lower.contains(
                        "unauthorized")
                || lower.contains(
                        "system:anonymous")
                || lower.contains(
                        "forbidden");
    }

    private String escapeForWindowsCommand(
            String value) {

        return value.replace(
                "\"",
                "\\\"");
    }

    private void logNormalizedOutput(
            String output,
            TestLogger logger,
            String color) {

        if (output == null
                || output.isBlank()) {
            return;
        }

        for (String line :
                output.trim().split("\\R")) {

            logger.info(
                    String.format(
                            "    %s<<< OUT       %s: %s",
                            color,
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