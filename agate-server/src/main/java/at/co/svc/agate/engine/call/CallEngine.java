package at.co.svc.agate.engine.call;

import java.util.Map;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.interfaces.TestLogger;

public class CallEngine extends AbstractStepEngine {

    private final StepExecutor executor;

    @FunctionalInterface
    public interface StepExecutor {
        void execute(
                TestCase tc,
                TestStep step,
                ExecutionContext context,
                String yamlFile,
                int idx,
                boolean verbose) throws Exception;
    }

    public CallEngine(StepExecutor executor) {
        this.executor = executor;
    }

    @Override
    public boolean canExecute(StepType type) {
        return type == StepType.CALL;
    }

    @Override
    public void doExecute(
            TestCase tc,
            TestStep step,
            ExecutionContext context,
            String yamlFile,
            int currentStepNumber,
            Boolean printExecution,
            TestLogger logger,
            boolean isVerbose) throws Exception {

        try {

            /*
             * Keep the original CALL DSL output.
             *
             * Important:
             * Do not hide this behind isVerbose here,
             * because CALL DSL was previously always printed
             * and the central execution flow already controls
             * whether the step should be shown.
             */
            PrintDslStepContext.logDslStepContext(
                    logger,
                    step
            );

            /*
             * Print resolved CALL parameters.
             *
             * This is logging only.
             * step.getParameters() is NOT modified.
             */
            printResolvedCallParameters(
                    tc,
                    step,
                    yamlFile,
                    currentStepNumber,
                    logger
            );

            String bInstance =
                    System.getProperty("INSTANCE");

            String bOrdid =
                    tc.getVariables() != null
                            && tc.getVariables().get("B_OrdinationsId") != null
                            ? tc.getVariables()
                                    .get("B_OrdinationsId")
                                    .toString()
                            : "";

            String bVpNummer =
                    tc.getVariables() != null
                            && tc.getVariables().get("B_Karte") != null
                            ? tc.getVariables()
                                    .get("B_Karte")
                                    .toString()
                            : "";

            Object slot =
                    step.getParameters() != null
                            ? step.getParameters().get("cardSlot")
                            : null;

            String bCardSlot =
                    slot != null
                            ? slot.toString()
                            : "baseContact";

            String existingDialogId = null;

            /*
             * Special handling for dialog setup reusable.
             */
            if (step.getCommand() != null
                    && step.getCommand()
                            .startsWith("reusable.ru_dialog_aufbau")) {

                existingDialogId =
                        DialogManager.getInstance()
                                .getDialogId(
                                        bInstance,
                                        bOrdid,
                                        bVpNummer,
                                        bCardSlot
                                );

                tc.addVariable(
                        "token",
                        "{NULL}"
                );

                tc.addVariable(
                        "dialogId",
                        existingDialogId
                );
            }

            /*
             * Execute reusable child steps.
             */
            if (step.getSubSteps() != null
                    && !step.getSubSteps().isEmpty()) {

                int subIndex = 1;

                for (TestStep subStep : step.getSubSteps()) {

                    executor.execute(
                            tc,
                            subStep,
                            context,
                            yamlFile,
                            subIndex,
                            isVerbose
                    );

                    subIndex++;
                }

                /*
                 * Persist dialog id after reusable execution.
                 */
                if (step.getCommand() != null
                        && step.getCommand()
                                .startsWith("reusable.ru_dialog_aufbau")) {

                    DialogManager.getInstance()
                            .saveDialogId(
                                    bInstance,
                                    bOrdid,
                                    bVpNummer,
                                    bCardSlot,
                                    existingDialogId
                            );
                }

            } else if (existingDialogId != null) {

                /*
                 * Existing dialog found and reusable has no child
                 * steps that need to be executed.
                 */
                tc.setVariables(
                        Map.of(
                                "dialogId",
                                existingDialogId
                        )
                );

                tc.addVariable(
                        "token",
                        "{NULL}"
                );

                context.setVar(
                        "token",
                        "{NULL}"
                );
            }

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Prints CALL command and resolved parameter values.
     *
     * Example:
     *
     *     >>> CALL       : reusable.ru_dialog_aufbau_mit_ordid
     *     >>> PARAM      : ORD_ID           = 15682076
     *     >>> PARAM      : vpNummer         = 645031
     *     >>> PARAM      : cardSlot         = baseContact
     *
     * Important:
     *
     * Resolution here is only for display.
     * The original step parameters remain unchanged.
     */
    private void printResolvedCallParameters(
            TestCase tc,
            TestStep step,
            String yamlFile,
            int stepIndex,
            TestLogger logger) {

        /*
         * Always print the CALL command.
         */
        String command =
                step.getCommand() != null
                        ? step.getCommand()
                        : "<not defined>";

        logger.info(
                String.format(
                        "    %s>>> CALL%s       : %s",
                        ConsoleColors.GREEN,
                        ConsoleColors.RESET,
                        command
                )
        );

        /*
         * CALL without parameters is perfectly valid.
         */
        if (step.getParameters() == null
                || step.getParameters().isEmpty()) {

            return;
        }

        YamlPlaceholderResolver resolver =
                new YamlPlaceholderResolver();

        for (Map.Entry<String, Object> entry :
                step.getParameters().entrySet()) {

            String parameterName =
                    entry.getKey();

            Object rawValue =
                    entry.getValue();

            String resolvedValue =
                    rawValue == null
                            ? "null"
                            : rawValue.toString();

            try {

                /*
                 * Resolve runtime placeholders only for logging.
                 *
                 * Example:
                 *
                 *   {B[B_OrdinationsId]}
                 *
                 * becomes:
                 *
                 *   15682076
                 */
                resolvedValue =
                        resolver.resolve(
                                tc,
                                resolvedValue,
                                tc.getVariables(),
                                yamlFile,
                                stepIndex,
                                "call-param-" + parameterName,
                                step
                        );

            } catch (Exception ignored) {

                /*
                 * Logging must never break test execution.
                 *
                 * If a value cannot be resolved at this point,
                 * the original value is displayed.
                 */
            }

            logger.info(
                    String.format(
                            "    %s>>> PARAM%s      : %-16s = %s",
                            ConsoleColors.GREEN,
                            ConsoleColors.RESET,
                            parameterName,
                            resolvedValue
                    )
            );
        }
    }
}