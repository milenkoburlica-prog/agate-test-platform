package at.co.svc.agate.engine.buffer;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /*
     * Pure reusable placeholder.
     *
     * Examples:
     *
     *   {R[Verify]}
     *   {R[Command]}
     *
     * Intentionally NOT matched:
     *
     *   abc-{R[Verify]}
     *   {R[Verify]}-abc
     *   {B[Test]}
     *
     * Special BUFFER EXEC semantics:
     *
     * If the value consists only of a reusable parameter and that
     * reusable parameter was not provided, the BUFFER assignment is
     * skipped.
     *
     * This reproduces the Tosca behavior for optional reusable
     * parameters:
     *
     *   L_Verify = not-set
     *   L_Verify = {PL[Verify]}
     *
     * If Verify is not provided, Tosca keeps the previous value.
     */
    private static final Pattern PURE_R_PLACEHOLDER =
            Pattern.compile(
                    "^\\s*\\{R\\[([^\\]]+)\\]\\}\\s*$"
            );


    @Override
    public boolean canExecute(
            StepType type) {

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

            PrintDslStepContext.logDslStepContext(
                    logger,
                    step
            );
        }

        String op =
                normalizeUpper(
                        step.getOp(),
                        "EXEC"
                );

        if (!SUPPORTED_OPERATIONS.contains(
                op)) {

            throw AgateStepException.builder(
                            "Unsupported BUFFER operation")
                    .actual(op)
                    .hint(
                            "Supported operations: EXEC, ASSERT")
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
                            isVerbose
                    );

            case "ASSERT" ->
                    handleAssert(
                            tc,
                            step,
                            yamlFile,
                            stepIndex,
                            printExecution,
                            logger,
                            isVerbose
                    );

            default ->
                    throw AgateStepException.builder(
                                    "Unsupported BUFFER operation")
                            .actual(op)
                            .hint(
                                    "Supported operations: EXEC, ASSERT")
                            .build();
        }
    }


    // =========================================================
    // BUFFER EXEC
    // =========================================================

    private void handleExec(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) {

        String rawName =
                step.getName();

        String rawValue =
                step.getValue();

        if (isBlank(
                rawName)) {

            throw AgateStepException.builder(
                            "Required property is missing")
                    .field("name")
                    .hint(
                            "BUFFER EXEC requires a target variable name.")
                    .build();
        }

        YamlPlaceholderResolver resolver =
                new YamlPlaceholderResolver();

        java.util.Map<String, Object> runtimeVariables =
                createRuntimeVariables(
                        tc,
                        context
                );

        String resolvedName;

        String resolvedValue;

        try {

            /*
             * Dynamic buffer names are valid:
             *
             *   name: '{B[L_Verify]}'
             *
             * Example:
             *
             *   L_Verify = TETSIM
             *   name     = {B[L_Verify]}
             *
             * resolves to:
             *
             *   name = TETSIM
             */
            resolvedName =
                    resolveRuntimeValue(
                            resolver,
                            tc,
                            step,
                            rawName,
                            runtimeVariables,
                            yamlFile,
                            stepIndex,
                            "NAME"
                    );

            try {

                resolvedValue =
                        resolveRuntimeValue(
                                resolver,
                                tc,
                                step,
                                rawValue,
                                runtimeVariables,
                                yamlFile,
                                stepIndex,
                                "VALUE"
                        );

            } catch (AgateStepException e) {

                /*
                 * ---------------------------------------------------------
                 * Optional reusable parameter semantics
                 * ---------------------------------------------------------
                 *
                 * Only for:
                 *
                 *   type: BUFFER
                 *   op: EXEC
                 *   value: '{R[Something]}'
                 *
                 * If the reusable parameter does not exist, do NOT overwrite
                 * the target BUFFER.
                 *
                 * Example:
                 *
                 *   - type: BUFFER
                 *     op: EXEC
                 *     name: L_Verify
                 *     value: not-set
                 *
                 *   - type: BUFFER
                 *     op: EXEC
                 *     name: L_Verify
                 *     value: '{R[Verify]}'
                 *
                 * CALL:
                 *
                 *   parameters:
                 *     Option: verify-no
                 *
                 * Verify is not supplied.
                 *
                 * Result:
                 *
                 *   L_Verify remains "not-set".
                 *
                 * IMPORTANT:
                 *
                 * This behavior is intentionally implemented here in the
                 * BufferEngine and NOT in YamlPlaceholderResolver.
                 *
                 * Therefore CMD, SQL, REST, etc. remain strict and still
                 * fail when an R parameter is missing.
                 * ---------------------------------------------------------
                 */
                if (isOptionalMissingReusableValue(
                        rawValue,
                        e)) {

                    String parameterName =
                            extractReusableParameterName(
                                    rawValue
                            );

                    if (Boolean.TRUE.equals(
                            printExecution)
                            && isVerbose) {

                        logger.info(
                                String.format(
                                        "    >>> BUFFER SKIP : [%s] | "
                                                + "Reusable parameter [%s] was not provided "
                                                + "- existing buffer value is kept",
                                        resolvedName,
                                        parameterName
                                )
                        );
                    }

                    return;
                }

                throw e;
            }

        } catch (AgateStepException e) {

            throw e;

        } catch (Exception e) {

            throw AgateStepException.builder(
                            "BUFFER value could not be resolved")
                    .detail(
                            "Name",
                            rawName)
                    .detail(
                            "Value",
                            rawValue)
                    .detail(
                            "Technical error",
                            safeMessage(e))
                    .hint(
                            "Check placeholders and variables used in the BUFFER name/value.")
                    .cause(e)
                    .build();
        }

        if (isBlank(
                resolvedName)) {

            throw AgateStepException.builder(
                            "BUFFER name resolved to an empty value")
                    .detail(
                            "Name expression",
                            rawName)
                    .hint(
                            "Check the variable used by the dynamic BUFFER name.")
                    .build();
        }

        tc.addVariable(
                resolvedName,
                resolvedValue
        );

        context.setVar(
                resolvedName,
                resolvedValue
        );

        if (Boolean.TRUE.equals(
                printExecution)
                && isVerbose) {

            if (!rawName.equals(
                    resolvedName)) {

                logger.info(
                        String.format(
                                "    %s>>> BUFFER RESOLVE%s: Name  [%s] -> [%s]",
                                ConsoleColors.GREEN,
                                ConsoleColors.RESET,
                                rawName,
                                resolvedName
                        )
                );
            }

            if (rawValue != null
                    && !rawValue.equals(
                    resolvedValue)) {

                logger.info(
                        String.format(
                                "    %s>>> BUFFER RESOLVE%s: Value [%s] -> [%s]",
                                ConsoleColors.GREEN,
                                ConsoleColors.RESET,
                                rawValue,
                                resolvedValue
                        )
                );
            }

            logger.info(
                    String.format(
                            "    %s>>> BUFFER     %s: [%s] | Store value [%s]",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET,
                            resolvedName,
                            resolvedValue
                    )
            );
        }
    }


    // =========================================================
    // OPTIONAL R PARAMETER
    // =========================================================

    /**
     * Returns true only when:
     *
     * 1. The BUFFER value is exactly one R placeholder:
     *
     *      {R[Verify]}
     *
     * 2. Resolution failed specifically because that reusable
     *    parameter was not provided.
     *
     * Other errors remain strict.
     */
    private boolean isOptionalMissingReusableValue(
            String rawValue,
            AgateStepException exception) {

        if (rawValue == null
                || exception == null) {

            return false;
        }

        Matcher matcher =
                PURE_R_PLACEHOLDER.matcher(
                        rawValue
                );

        if (!matcher.matches()) {

            return false;
        }

        return "Reusable parameter was not found"
                .equals(
                        exception.getReason()
                );
    }


    /**
     * Extracts:
     *
     *   {R[Verify]}
     *
     * as:
     *
     *   Verify
     */
    private String extractReusableParameterName(
            String rawValue) {

        if (rawValue == null) {

            return "";
        }

        Matcher matcher =
                PURE_R_PLACEHOLDER.matcher(
                        rawValue
                );

        if (!matcher.matches()) {

            return rawValue;
        }

        return matcher.group(1);
    }


    // =========================================================
    // PLACEHOLDER RESOLUTION
    // =========================================================

    private String resolveRuntimeValue(
            YamlPlaceholderResolver resolver,
            TestCase tc,
            TestStep step,
            String rawValue,
            java.util.Map<String, Object> runtimeVariables,
            String yamlFile,
            int stepIndex,
            String field) {

        if (rawValue == null) {

            return null;
        }

        if (!rawValue.contains(
                "{")) {

            return rawValue;
        }

        return resolver.resolve(
                tc,
                rawValue,
                runtimeVariables,
                yamlFile,
                stepIndex,
                field,
                step
        );
    }


    private java.util.Map<String, Object> createRuntimeVariables(
            TestCase tc,
            ExecutionContext context) {

        java.util.Map<String, Object> variables =
                new java.util.HashMap<>();

        if (tc != null
                && tc.getVariables() != null) {

            variables.putAll(
                    tc.getVariables()
            );
        }

        if (context != null
                && context.getVars() != null) {

            variables.putAll(
                    context.getVars()
            );
        }

        if (context != null
                && context.getBufferMap() != null) {

            variables.putAll(
                    context.getBufferMap()
            );
        }

        return variables;
    }


    // =========================================================
    // BUFFER ASSERT
    // =========================================================

    private void handleAssert(
            TestCase tc,
            TestStep step,
            String yamlFile,
            int stepIndex,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) {

        String name =
                step.getName();

        String action =
                normalizeUpper(
                        step.getAction(),
                        ""
                );

        String expectedRaw =
                step.getExpected();

        if (isBlank(
                name)) {

            throw AgateStepException.builder(
                            "Required property is missing")
                    .field("name")
                    .hint(
                            "BUFFER ASSERT requires the name of a buffered variable.")
                    .build();
        }

        if (isBlank(
                action)) {

            throw AgateStepException.builder(
                            "Required property is missing")
                    .field("action")
                    .hint(
                            "BUFFER ASSERT requires an assertion action.")
                    .build();
        }

        if (!SUPPORTED_ASSERT_ACTIONS.contains(
                action)) {

            throw AgateStepException.builder(
                            "Unsupported BUFFER assertion action")
                    .actual(action)
                    .hint(
                            "Supported actions: "
                                    + String.join(
                                    ", ",
                                    SUPPORTED_ASSERT_ACTIONS
                            ))
                    .build();
        }

        if (requiresExpected(
                action)
                && expectedRaw == null) {

            throw AgateStepException.builder(
                            "Required property is missing")
                    .field("expected")
                    .detail(
                            "Action",
                            action)
                    .hint(
                            "This BUFFER assertion action requires an expected value.")
                    .build();
        }

        boolean bufferExists =
                tc.getVariables() != null
                        && tc.getVariables()
                        .containsKey(name);

        if (!bufferExists) {

            throw AgateStepException.builder(
                            "BUFFER variable was not found")
                    .detail(
                            "Name",
                            name)
                    .hint(
                            "Make sure a BUFFER EXEC step or another step creates this variable before the assertion.")
                    .build();
        }

        Object actualObj =
                tc.getVariables()
                        .get(name);

        String actual =
                actualObj != null
                        ? String.valueOf(
                        actualObj)
                        : null;

        String expectedResolved =
                null;

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
                                "VALUE"
                        );

            } catch (AgateStepException e) {

                throw e;

            } catch (Exception e) {

                throw AgateStepException.builder(
                                "BUFFER expected value could not be resolved")
                        .detail(
                                "Name",
                                name)
                        .detail(
                                "Expected",
                                expectedRaw)
                        .detail(
                                "Technical error",
                                safeMessage(e))
                        .hint(
                                "Check placeholders and variables used in the expected value.")
                        .cause(e)
                        .build();
            }
        }

        if (Boolean.TRUE.equals(
                printExecution)
                && isVerbose) {

            if (isUnaryAction(
                    action)) {

                logger.info(
                        String.format(
                                "    %s>>> ASSERT     %s: [%s] | %s",
                                ConsoleColors.GREEN,
                                ConsoleColors.RESET,
                                name,
                                action
                        )
                );

            } else {

                logger.info(
                        String.format(
                                "    %s>>> ASSERT     %s: [%s] | %s \"%s\"",
                                ConsoleColors.GREEN,
                                ConsoleColors.RESET,
                                name,
                                action,
                                expectedResolved
                        )
                );
            }
        }

        boolean success =
                compareStrings(
                        actual,
                        expectedResolved,
                        action
                );

        String actualDisplay =
                actual == null
                        ? "null"
                        : actual;

        if (!success) {

            AgateStepException.Builder failure =
                    AgateStepException.builder(
                                    "BUFFER assertion failed")
                            .detail(
                                    "Name",
                                    name)
                            .detail(
                                    "Action",
                                    action)
                            .expected(
                                    displayExpected(
                                            action,
                                            expectedResolved
                                    ))
                            .actual(
                                    actualDisplay
                            );

            if ("CONTAINS".equals(
                    action)) {

                failure.hint(
                        "Check whether the buffered value contains the expected text."
                );
            }

            throw failure.build();
        }

        if (Boolean.TRUE.equals(
                printExecution)
                && isVerbose) {

            logger.info(
                    String.format(
                            "    %s>>> RESULT     %s: SUCCESS | Value is \"%s\"",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET,
                            actualDisplay
                    )
            );
        }
    }


    // =========================================================
    // ASSERT HELPERS
    // =========================================================

    private boolean compareStrings(
            String actual,
            String expected,
            String action) {

        if (actual == null) {

            return switch (action) {

                case "IS_NULL" ->
                        true;

                case "IS_NOT_NULL" ->
                        false;

                case "IS_EMPTY" ->
                        true;

                case "IS_NOT_EMPTY" ->
                        false;

                case "EQUALS" ->
                        expected == null;

                case "NOT_EQUALS" ->
                        expected != null;

                case "CONTAINS" ->
                        false;

                default ->
                        false;
            };
        }

        return switch (action) {

            case "EQUALS" ->
                    actual.equals(
                            expected
                    );

            case "NOT_EQUALS" ->
                    !actual.equals(
                            expected
                    );

            case "CONTAINS" ->
                    expected != null
                            && actual.contains(
                            expected
                    );

            case "IS_NULL" ->
                    false;

            case "IS_NOT_NULL" ->
                    true;

            case "IS_EMPTY" ->
                    actual.isEmpty();

            case "IS_NOT_EMPTY" ->
                    !actual.isEmpty();

            default ->
                    throw AgateStepException.builder(
                                    "Unsupported BUFFER assertion action")
                            .actual(action)
                            .hint(
                                    "Supported actions: "
                                            + String.join(
                                            ", ",
                                            SUPPORTED_ASSERT_ACTIONS
                                    ))
                            .build();
        };
    }


    private boolean requiresExpected(
            String action) {

        return !isUnaryAction(
                action
        );
    }


    private boolean isUnaryAction(
            String action) {

        return Set.of(
                        "IS_NULL",
                        "IS_NOT_NULL",
                        "IS_EMPTY",
                        "IS_NOT_EMPTY"
                )
                .contains(
                        action
                );
    }


    private String displayExpected(
            String action,
            String expected) {

        return switch (action) {

            case "IS_NULL" ->
                    "NULL";

            case "IS_NOT_NULL" ->
                    "NOT NULL";

            case "IS_EMPTY" ->
                    "EMPTY";

            case "IS_NOT_EMPTY" ->
                    "NOT EMPTY";

            default ->
                    expected;
        };
    }


    // =========================================================
    // GENERAL HELPERS
    // =========================================================

    private static String normalizeUpper(
            String value,
            String fallback) {

        if (value == null
                || value.isBlank()) {

            return fallback;
        }

        return value.trim()
                .toUpperCase();
    }


    private static boolean isBlank(
            String value) {

        return value == null
                || value.isBlank();
    }


    private static String safeMessage(
            Throwable throwable) {

        if (throwable == null) {

            return "Unknown error";
        }

        String message =
                throwable.getMessage();

        if (message == null
                || message.isBlank()) {

            return throwable.getClass()
                    .getSimpleName();
        }

        return message.trim();
    }
}