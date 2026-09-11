package at.co.svc.agate.engine.buffer;

import java.util.Set;

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

/**
 * Buffer Engine for local variables and simple text assertions.
 */
public class BufferEngine extends AbstractStepEngine {

    private static final Set<String> SUPPORTED_OPERATIONS =
            Set.of("EXEC", "ASSERT");

    private static final Set<String> SUPPORTED_ASSERT_ACTIONS =
            Set.of(
                    "EQUALS",
                    "NOT_EQUALS",
                    "CONTAINS",
                    "IS_NULL",
                    "IS_NOT_NULL",
                    "IS_EMPTY",
                    "IS_NOT_EMPTY"
            );

    @Override
    public boolean canExecute(StepType type) {
        return type == StepType.BUFFER;
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
            PrintDslStepContext.logDslStepContext(logger, step);
        }

        String op = normalizeUpper(step.getOp(), "EXEC");

        if (!SUPPORTED_OPERATIONS.contains(op)) {
            throw AgateStepException.builder("Unsupported BUFFER operation")
                    .actual(op)
                    .hint("Supported operations: EXEC, ASSERT")
                    .build();
        }

        switch (op) {
            case "EXEC" ->
                    handleExec(
                            tc,
                            step,
                            context,
                            yamlFile,
                            stepIndex,
                            printExecution,
                            logger,
                            isVerbose);

            case "ASSERT" ->
                    handleAssert(
                            tc,
                            step,
                            yamlFile,
                            stepIndex,
                            printExecution,
                            logger,
                            isVerbose);

            default ->
                    throw AgateStepException.builder("Unsupported BUFFER operation")
                            .actual(op)
                            .hint("Supported operations: EXEC, ASSERT")
                            .build();
        }
    }

    private void handleExec(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) {

        String name = step.getName();
        String value = step.getValue();

        if (isBlank(name)) {
            throw AgateStepException.builder("Required property is missing")
                    .field("name")
                    .hint("BUFFER EXEC requires a target variable name.")
                    .build();
        }

        String resolvedValue = value;

        if (value != null && value.contains("{")) {
            try {
                YamlPlaceholderResolver resolver =
                        new YamlPlaceholderResolver();

                resolvedValue =
                        resolver.resolve(
                                tc,
                                value,
                                step.getParameters(),
                                yamlFile,
                                stepIndex,
                                "VALUE",
                                step);

                resolvedValue =
                        resolver.resolve(
                                tc,
                                resolvedValue,
                                tc.getVariables(),
                                yamlFile,
                                stepIndex,
                                "VALUE",
                                step);

            } catch (AgateStepException e) {
                throw e;

            } catch (Exception e) {
                throw AgateStepException.builder("BUFFER value could not be resolved")
                        .detail("Name", name)
                        .detail("Value", value)
                        .detail("Technical error", safeMessage(e))
                        .hint("Check placeholders and variables used in the BUFFER value.")
                        .cause(e)
                        .build();
            }
        }

        tc.addVariable(name, resolvedValue);
        context.setVar(name, resolvedValue);

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logger.info(String.format(
                    "    %s>>> BUFFER     %s: [%s] | Store value [%s]",
                    ConsoleColors.GREEN,
                    ConsoleColors.RESET,
                    name,
                    resolvedValue));
        }
    }

    private void handleAssert(
            TestCase tc,
            TestStep step,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) {

        String name = step.getName();
        String action = normalizeUpper(step.getAction(), "");
        String expectedRaw = step.getExpected();

        if (isBlank(name)) {
            throw AgateStepException.builder("Required property is missing")
                    .field("name")
                    .hint("BUFFER ASSERT requires the name of a buffered variable.")
                    .build();
        }

        if (isBlank(action)) {
            throw AgateStepException.builder("Required property is missing")
                    .field("action")
                    .hint("BUFFER ASSERT requires an assertion action.")
                    .build();
        }

        if (!SUPPORTED_ASSERT_ACTIONS.contains(action)) {
            throw AgateStepException.builder("Unsupported BUFFER assertion action")
                    .actual(action)
                    .hint("Supported actions: "
                            + String.join(", ", SUPPORTED_ASSERT_ACTIONS))
                    .build();
        }

        if (requiresExpected(action) && expectedRaw == null) {
            throw AgateStepException.builder("Required property is missing")
                    .field("expected")
                    .detail("Action", action)
                    .hint("This BUFFER assertion action requires an expected value.")
                    .build();
        }

        boolean bufferExists =
                tc.getVariables() != null
                        && tc.getVariables().containsKey(name);

        if (!bufferExists) {
            throw AgateStepException.builder("BUFFER variable was not found")
                    .detail("Name", name)
                    .hint("Make sure a BUFFER EXEC step or another step creates this variable before the assertion.")
                    .build();
        }

        Object actualObj =
                tc.getVariables().get(name);

        String actual =
                actualObj != null
                        ? String.valueOf(actualObj)
                        : null;

        String expectedResolved = null;

        if (expectedRaw != null) {
            try {
                YamlPlaceholderResolver resolver =
                        new YamlPlaceholderResolver();

                expectedResolved =
                        resolver.resolve(
                                tc,
                                expectedRaw,
                                tc.getVariables(),
                                yamlFile,
                                stepIndex,
                                "VALUE");

            } catch (AgateStepException e) {
                throw e;

            } catch (Exception e) {
                throw AgateStepException.builder("BUFFER expected value could not be resolved")
                        .detail("Name", name)
                        .detail("Expected", expectedRaw)
                        .detail("Technical error", safeMessage(e))
                        .hint("Check placeholders and variables used in the expected value.")
                        .cause(e)
                        .build();
            }
        }

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            if (isUnaryAction(action)) {
                logger.info(String.format(
                        "    %s>>> ASSERT     %s: [%s] | %s",
                        ConsoleColors.GREEN,
                        ConsoleColors.RESET,
                        name,
                        action));
            } else {
                logger.info(String.format(
                        "    %s>>> ASSERT     %s: [%s] | %s \"%s\"",
                        ConsoleColors.GREEN,
                        ConsoleColors.RESET,
                        name,
                        action,
                        expectedResolved));
            }
        }

        boolean success =
                compareStrings(
                        actual,
                        expectedResolved,
                        action);

        String actualDisplay =
                actual == null
                        ? "null"
                        : actual;

        if (!success) {
            AgateStepException.Builder failure =
                    AgateStepException.builder("BUFFER assertion failed")
                            .detail("Name", name)
                            .detail("Action", action)
                            .expected(displayExpected(action, expectedResolved))
                            .actual(actualDisplay);

            if ("CONTAINS".equals(action)) {
                failure.hint("Check whether the buffered value contains the expected text.");
            }

            throw failure.build();
        }

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logger.info(String.format(
                    "    %s>>> RESULT     %s: SUCCESS | Value is \"%s\"",
                    ConsoleColors.GREEN,
                    ConsoleColors.RESET,
                    actualDisplay));
        }
    }

    private boolean compareStrings(
            String actual,
            String expected,
            String action) {

        if (actual == null) {
            return switch (action) {
                case "IS_NULL" -> true;
                case "IS_NOT_NULL" -> false;
                case "IS_EMPTY" -> true;
                case "IS_NOT_EMPTY" -> false;
                case "EQUALS" -> expected == null;
                case "NOT_EQUALS" -> expected != null;
                case "CONTAINS" -> false;
                default -> false;
            };
        }

        return switch (action) {
            case "EQUALS" ->
                    actual.equals(expected);

            case "NOT_EQUALS" ->
                    !actual.equals(expected);

            case "CONTAINS" ->
                    expected != null
                            && actual.contains(expected);

            case "IS_NULL" ->
                    false;

            case "IS_NOT_NULL" ->
                    true;

            case "IS_EMPTY" ->
                    actual.isEmpty();

            case "IS_NOT_EMPTY" ->
                    !actual.isEmpty();

            default ->
                    throw AgateStepException.builder("Unsupported BUFFER assertion action")
                            .actual(action)
                            .hint("Supported actions: "
                                    + String.join(", ", SUPPORTED_ASSERT_ACTIONS))
                            .build();
        };
    }

    private boolean requiresExpected(String action) {
        return !isUnaryAction(action);
    }

    private boolean isUnaryAction(String action) {
        return Set.of(
                "IS_NULL",
                "IS_NOT_NULL",
                "IS_EMPTY",
                "IS_NOT_EMPTY")
                .contains(action);
    }

    private String displayExpected(
            String action,
            String expected) {

        return switch (action) {
            case "IS_NULL" -> "NULL";
            case "IS_NOT_NULL" -> "NOT NULL";
            case "IS_EMPTY" -> "EMPTY";
            case "IS_NOT_EMPTY" -> "NOT EMPTY";
            default -> expected;
        };
    }

    private static String normalizeUpper(
            String value,
            String fallback) {

        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim().toUpperCase();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String safeMessage(Throwable throwable) {
        if (throwable == null) {
            return "Unknown error";
        }

        String message = throwable.getMessage();

        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }

        return message.trim();
    }
}
