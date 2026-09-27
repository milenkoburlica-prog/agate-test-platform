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
                boolean verbose)
                throws Exception;
    }

    public CallEngine(
            StepExecutor executor) {

        this.executor =
                executor;
    }

    @Override
    public boolean canExecute(
            StepType type) {

        return type
                == StepType.CALL;
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
            boolean isVerbose)
            throws Exception {

        try {

            /*
             * =====================================================
             * CURRENT CALL VISIBILITY
             * =====================================================
             *
             * isVerbose here means:
             *
             * true:
             *   this CALL itself is visible
             *
             * false:
             *   this CALL is an internal reusable step and should
             *   remain silent
             *
             * Top-level CALLs arrive here with true.
             *
             * Nested CALLs arrive with true only when their parent
             * reusable has verbose=true.
             * =====================================================
             */
            if (isVerbose) {

                PrintDslStepContext
                        .logDslStepContext(
                                logger,
                                step);

                printResolvedCallParameters(
                        tc,
                        step,
                        yamlFile,
                        currentStepNumber,
                        logger
                );
            }

            /*
             * =====================================================
             * CHILD VERBOSE
             * =====================================================
             *
             * Child reusable steps are silent by default.
             *
             * Only:
             *
             * parameters:
             *   verbose: true
             *
             * enables detailed child-step logging.
             *
             * IMPORTANT:
             *
             * The current CALL remains visible independently from
             * this setting.
             * =====================================================
             */
            boolean childVerbose =
                    false;

            if (isVerbose
                    && step.getParameters()
                    != null
                    && step.getParameters()
                    .containsKey(
                            "verbose")) {

                Object verboseValue =
                        step.getParameters()
                                .get(
                                        "verbose");

                if (verboseValue != null) {

                    childVerbose =
                            Boolean.parseBoolean(
                                    verboseValue
                                            .toString());
                }
            }

            String bInstance =
                    System.getProperty(
                            "INSTANCE");

            String bOrdid =
                    tc.getVariables() != null
                            && tc.getVariables()
                            .get(
                                    "B_OrdinationsId")
                            != null

                            ? tc.getVariables()
                            .get(
                                    "B_OrdinationsId")
                            .toString()

                            : "";

            String bVpNummer =
                    tc.getVariables() != null
                            && tc.getVariables()
                            .get(
                                    "B_Karte")
                            != null

                            ? tc.getVariables()
                            .get(
                                    "B_Karte")
                            .toString()

                            : "";

            Object slot =
                    step.getParameters()
                    != null

                            ? step.getParameters()
                            .get(
                                    "cardSlot")

                            : null;

            String bCardSlot =
                    slot != null
                            ? slot.toString()
                            : "baseContact";

            String existingDialogId =
                    null;

            /*
             * =====================================================
             * SPECIAL DIALOG SETUP HANDLING
             * =====================================================
             */
            if (step.getCommand() != null
                    && step.getCommand()
                    .startsWith(
                            "reusable.ru_dialog_aufbau")) {

                existingDialogId =
                        DialogManager
                                .getInstance()
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
             * =====================================================
             * EXECUTE REUSABLE CHILD STEPS
             * =====================================================
             */
            if (step.getSubSteps() != null
                    && !step.getSubSteps()
                    .isEmpty()) {

                int subIndex =
                        1;

                for (TestStep subStep :
                        step.getSubSteps()) {

                    /*
                     * Child steps are visible only when this CALL
                     * explicitly has:
                     *
                     * parameters:
                     *   verbose: true
                     */
                    executor.execute(
                            tc,
                            subStep,
                            context,
                            yamlFile,
                            subIndex,
                            childVerbose
                    );

                    subIndex++;
                }

                /*
                 * Persist dialog id after reusable execution.
                 */
                if (step.getCommand() != null
                        && step.getCommand()
                        .startsWith(
                                "reusable.ru_dialog_aufbau")) {

                    DialogManager
                            .getInstance()
                            .saveDialogId(
                                    bInstance,
                                    bOrdid,
                                    bVpNummer,
                                    bCardSlot,
                                    existingDialogId
                            );
                }

            } else if (existingDialogId
                    != null) {

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
     * Resolution here is only for display.
     *
     * The original step parameters remain unchanged.
     */
    private void printResolvedCallParameters(
            TestCase tc,
            TestStep step,
            String yamlFile,
            int stepIndex,
            TestLogger logger) {

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
         * CALL without parameters is valid.
         */
        if (step.getParameters() == null
                || step.getParameters()
                .isEmpty()) {

            return;
        }

        YamlPlaceholderResolver resolver =
                new YamlPlaceholderResolver();

        for (Map.Entry<String, Object> entry :
                step.getParameters()
                        .entrySet()) {

            String parameterName =
                    entry.getKey();

            /*
             * verbose is a CALL control parameter.
             *
             * It is not a reusable business parameter and therefore
             * does not need to be printed as:
             *
             * >>> PARAM : verbose = true
             */
            if ("verbose".equalsIgnoreCase(
                    parameterName)) {

                continue;
            }

            Object rawValue =
                    entry.getValue();

            String resolvedValue =
                    rawValue == null

                            ? "null"

                            : rawValue
                            .toString();

            try {

                resolvedValue =
                        resolver.resolve(
                                tc,
                                resolvedValue,
                                tc.getVariables(),
                                yamlFile,
                                stepIndex,
                                "call-param-"
                                        + parameterName,
                                step
                        );

            } catch (Exception ignored) {

                /*
                 * Logging must never break execution.
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