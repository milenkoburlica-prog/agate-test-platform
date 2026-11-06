package at.co.svc.agate.engine.wait;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.interfaces.TestLogger;

/**
 * Engine for handling pause/wait steps using StepType.WAIT.
 */
public class WaitEngine extends AbstractStepEngine {

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.WAIT;
    }

    @Override
    public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, 
                        int stepIndex, Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {
        
        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(logger, step);
        }

        
        String actionValue = step.getValue();
        
        if (actionValue != null && actionValue.contains("{")) {
            YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();
            actionValue = resolver.resolve(tc, actionValue, tc.getVariables(), yamlFile, stepIndex, "VALUE", step);
        }
        
        // Safety check: if action is null or empty, default to 0 ms
        if (actionValue == null || actionValue.isEmpty()) {
            actionValue = "0";
        }

        long millis = 0;
        String input = actionValue.toLowerCase().trim().replaceAll("\\s+", "");

        try {
            if (input.endsWith("ms")) {
                millis = Long.parseLong(input.replace("ms", ""));
            } else if (input.endsWith("s")) {
                millis = Long.parseLong(input.replace("s", "")) * 1000;
            } else if (input.endsWith("m")) {
                millis = Long.parseLong(input.replace("m", "")) * 60 * 1000;
            } else if (input.matches("\\d+")) {
                millis = Long.parseLong(input);
            } else {
                throw new IllegalArgumentException("Unknown time format: " + actionValue);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid number format in WAIT step: " + actionValue);
        }
        
        
        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logger.info(String.format("    %s>>> DURATION%s  : %d ms", ConsoleColors.GREEN, ConsoleColors.RESET, millis));
        }
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Wait interrupted: " + e.getMessage());
            }
        }
        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            //logger.info(String.format("    %s>>> DONE%s", ConsoleColors.GREEN, ConsoleColors.RESET));
            
        }
    }
}