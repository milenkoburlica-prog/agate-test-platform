package at.co.svc.agate.engine.call;

import java.util.Map;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.interfaces.TestLogger;

public class CallEngine extends AbstractStepEngine {

    private final StepExecutor executor;

    @FunctionalInterface
    public interface StepExecutor {
        void execute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int idx, boolean verbose) throws Exception;
    }

    public CallEngine(StepExecutor executor) {
        this.executor = executor;
    }

    @Override
    public boolean canExecute(StepType type) {
        return type == StepType.CALL;
    }

    @Override
    public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int currentStepNumber, Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        try {
            PrintDslStepContext.logDslStepContext(logger, step);

            String bInstance = System.getProperty("INSTANCE");
            String bOrdid = (tc.getVariables() != null && tc.getVariables().get("B_OrdinationsId") != null) ? tc.getVariables().get("B_OrdinationsId").toString() : "";
            String bVpNummer = (tc.getVariables() != null && tc.getVariables().get("B_Karte") != null) ? tc.getVariables().get("B_Karte").toString() : "";
            Object slot = step.getParameters().get("cardSlot");
            String bCardSlot = (slot != null) ? slot.toString() : "baseContact";

            String existingDialogId = null;
            if ((step.getCommand() != null) && (step.getCommand().startsWith("reusable.ru_dialog_aufbau"))) {
                existingDialogId = DialogManager.getInstance().getDialogId(bInstance, bOrdid, bVpNummer, bCardSlot);
                tc.addVariable("token", "{NULL}");
                tc.addVariable("dialogId", existingDialogId);
            }

            if (step.getSubSteps() != null && !step.getSubSteps().isEmpty()) {
                int subIndex = 1;
                for (TestStep subStep : step.getSubSteps()) {
                    executor.execute(tc, subStep, context, yamlFile, subIndex, isVerbose);
                    subIndex++;
                }

                if ((step.getCommand() != null) && (step.getCommand().startsWith("reusable.ru_dialog_aufbau"))) {
                    DialogManager.getInstance().saveDialogId(bInstance, bOrdid, bVpNummer, bCardSlot, existingDialogId);
                }
            } else if (existingDialogId != null) {
                tc.setVariables(Map.of("dialogId", existingDialogId));
                tc.addVariable("token", "{NULL}");
                context.setVar("token", "{NULL}");
            }

        } catch (Exception e) {
            throw e;
        }
    }
}