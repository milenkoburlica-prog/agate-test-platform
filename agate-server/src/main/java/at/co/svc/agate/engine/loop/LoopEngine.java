package at.co.svc.agate.engine.loop;

import java.util.List;
import java.util.Locale;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.error.AgateStepException;
import at.co.svc.agate.core.interfaces.TestLogger;

public class LoopEngine extends AbstractStepEngine {

    private static final String MODE_WHILE = "WHILE";
    private static final String MODE_DO_WHILE = "DO_WHILE";

    @FunctionalInterface
    public interface StepExecutor {
        void execute(
                TestCase testCase,
                TestStep step,
                ExecutionContext context,
                String yamlFile,
                int stepNumber,
                boolean isVerbose) throws Exception;
    }

    @FunctionalInterface
    public interface LoopConditionEvaluator {
        boolean evaluate(
                TestCase testCase,
                TestStep step,
                ExecutionContext context,
                String yamlFile,
                int stepIndex,
                boolean isVerbose);
    }

    private final StepExecutor stepExecutor;
    private final LoopConditionEvaluator conditionEvaluator;

    public LoopEngine(
            StepExecutor stepExecutor,
            LoopConditionEvaluator conditionEvaluator) {

        this.stepExecutor = stepExecutor;
        this.conditionEvaluator = conditionEvaluator;
    }

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.LOOP;
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

        String mode =
                normalizeMode(
                        step.getLoopMode());

        int maxIterations =
                requiredMaxIterations(
                        step);

        long timeoutMs =
                requiredTimeoutMs(
                        step);

        List<TestStep> subSteps =
                step.getSubSteps();

        if (subSteps == null
                || subSteps.isEmpty()) {

            throw AgateStepException.builder(
                    "LOOP has no child steps")
                    .hint("Add at least one step below LOOP.steps.")
                    .build();
        }

        if (step.getCondition() == null
                || step.getCondition().isBlank()) {

            throw AgateStepException.builder(
                    "LOOP condition is missing")
                    .hint("Define a condition which decides whether the loop continues.")
                    .build();
        }

        long startedAt =
                System.currentTimeMillis();

        int iterations =
                0;

        if (isVerbose) {
            logger.info(
                    String.format(
                            "    %s>>> LOOP%s      : mode=%s | maxIterations=%d | timeoutMs=%d",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET,
                            mode,
                            maxIterations,
                            timeoutMs));
        }

        if (MODE_WHILE.equals(mode)) {

            while (evaluateCondition(
                    tc,
                    step,
                    context,
                    yamlFile,
                    stepIndex,
                    logger,
                    isVerbose)) {

                ensureCanStartNextIteration(
                        iterations,
                        maxIterations,
                        startedAt,
                        timeoutMs);

                iterations++;

                logIteration(
                        logger,
                        isVerbose,
                        iterations,
                        maxIterations);

                executeChildren(
                        tc,
                        step,
                        context,
                        yamlFile,
                        isVerbose,
                        startedAt,
                        timeoutMs);
            }

        } else {

            do {

                ensureCanStartNextIteration(
                        iterations,
                        maxIterations,
                        startedAt,
                        timeoutMs);

                iterations++;

                logIteration(
                        logger,
                        isVerbose,
                        iterations,
                        maxIterations);

                executeChildren(
                        tc,
                        step,
                        context,
                        yamlFile,
                        isVerbose,
                        startedAt,
                        timeoutMs);

            } while (evaluateCondition(
                    tc,
                    step,
                    context,
                    yamlFile,
                    stepIndex,
                    logger,
                    isVerbose));
        }

        if (isVerbose) {

            long duration =
                    System.currentTimeMillis()
                            - startedAt;

            logger.info(
                    String.format(
                            "    %s>>> LOOP END%s  : iterations=%d | duration=%d ms",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET,
                            iterations,
                            duration));
        }
    }

    private void executeChildren(
            TestCase tc,
            TestStep loopStep,
            ExecutionContext context,
            String yamlFile,
            boolean isVerbose,
            long startedAt,
            long timeoutMs) throws Exception {

        int childStepNumber =
                1;

        for (TestStep child :
                loopStep.getSubSteps()) {

            stepExecutor.execute(
                    tc,
                    child,
                    context,
                    yamlFile,
                    childStepNumber,
                    isVerbose);

            ensureWithinTimeout(
                    startedAt,
                    timeoutMs);

            childStepNumber++;
        }
    }

    private boolean evaluateCondition(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int stepIndex,
            TestLogger logger,
            boolean isVerbose) {

        boolean result =
                conditionEvaluator.evaluate(
                        tc,
                        step,
                        context,
                        yamlFile,
                        stepIndex,
                        false);

        if (isVerbose) {
            logger.info(
                    String.format(
                            "    >>> LOOP COND : %s -> %s",
                            step.getCondition(),
                            result));
        }

        return result;
    }

    private static String normalizeMode(
            String mode) {

        if (mode == null
                || mode.isBlank()) {

            throw AgateStepException.builder(
                    "LOOP mode is missing")
                    .hint("Supported LOOP modes: WHILE, DO_WHILE.")
                    .build();
        }

        String normalized =
                mode.trim()
                        .toUpperCase(
                                Locale.ROOT);

        if (!MODE_WHILE.equals(normalized)
                && !MODE_DO_WHILE.equals(normalized)) {

            throw AgateStepException.builder(
                    "Unsupported LOOP mode")
                    .actual(mode)
                    .hint("Supported LOOP modes: WHILE, DO_WHILE.")
                    .build();
        }

        return normalized;
    }

    private static int requiredMaxIterations(
            TestStep step) {

        Integer maxIterations =
                step.getMaxIterations();

        if (maxIterations == null
                || maxIterations <= 0) {

            throw AgateStepException.builder(
                    "Invalid LOOP maxIterations")
                    .actual(String.valueOf(maxIterations))
                    .hint("maxIterations must be greater than 0.")
                    .build();
        }

        return maxIterations;
    }

    private static long requiredTimeoutMs(
            TestStep step) {

        Long timeoutMs =
                step.getTimeoutMs();

        if (timeoutMs == null
                || timeoutMs <= 0) {

            throw AgateStepException.builder(
                    "Invalid LOOP timeoutMs")
                    .actual(String.valueOf(timeoutMs))
                    .hint("timeoutMs must be greater than 0.")
                    .build();
        }

        return timeoutMs;
    }

    private static void ensureCanStartNextIteration(
            int completedIterations,
            int maxIterations,
            long startedAt,
            long timeoutMs) {

        if (completedIterations >= maxIterations) {

            throw AgateStepException.builder(
                    "LOOP maximum iterations exceeded")
                    .expected("Less than or equal to "
                            + maxIterations
                            + " iterations")
                    .actual(String.valueOf(completedIterations))
                    .hint("Increase maxIterations or verify that the loop condition can become false.")
                    .build();
        }

        ensureWithinTimeout(
                startedAt,
                timeoutMs);
    }

    private static void ensureWithinTimeout(
            long startedAt,
            long timeoutMs) {

        long elapsed =
                System.currentTimeMillis()
                        - startedAt;

        if (elapsed >= timeoutMs) {

            throw AgateStepException.builder(
                    "LOOP timeout exceeded")
                    .expected("Less than "
                            + timeoutMs
                            + " ms")
                    .actual(elapsed
                            + " ms")
                    .hint("Increase timeoutMs or verify that the loop condition can become false.")
                    .build();
        }
    }

    private static void logIteration(
            TestLogger logger,
            boolean isVerbose,
            int iteration,
            int maxIterations) {

        if (!isVerbose) {
            return;
        }

        logger.info(
                String.format(
                        "    >>> LOOP ITER : %d/%d",
                        iteration,
                        maxIterations));
    }
}
