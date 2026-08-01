package at.co.svc.agate.engine.buffer;

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
 * Buffer Engine for local variables and simple text assertions.
 */
public class BufferEngine extends AbstractStepEngine {

    @Override
    public boolean canExecute(StepType type) {
        return type == StepType.BUFFER;
    }

    @Override
    public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        String op = step.getOp() != null ? step.getOp().toUpperCase() : "EXEC";
        String name = step.getName();
        String action = step.getAction();
        String value = step.getValue();

        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(logger, step);
        }
        
        switch (op) {
            case "EXEC":
                handleExec(tc, step, context, name, value, printExecution, logger, isVerbose);
                break;
            case "ASSERT":
                handleAssert(tc, step, yamlFile, name, action, step.getExpected(), printExecution, logger, isVerbose);
                break;
            default:
                throw new RuntimeException("Unsupported operation for BufferEngine: " + op);
        }
    }
    
    private void handleExec(TestCase tc, TestStep step, ExecutionContext context, String name, String value,
            Boolean printExecution, TestLogger logger, boolean isVerbose) {

        if (name == null || name.isEmpty()) {
            throw new RuntimeException("Buffer 'name' must be provided for EXEC operation.");
        }

        String resolvedValue = value;
        if (value != null && value.contains("{")) {
            YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();
            resolvedValue = resolver.resolve(tc, value, step.getParameters(), "", 0, "VALUE", step);
            resolvedValue = resolver.resolve(tc, resolvedValue, tc.getVariables(), "", 0, "VALUE", step);
        }
        
        tc.addVariable(name, resolvedValue);
        context.setVar(name, resolvedValue);
        
        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            logger.info(String.format("    %s>>> BUFFER     %s: [%s] | Store value [%s]", 
                    ConsoleColors.GREEN, ConsoleColors.RESET, name, resolvedValue));
        }
        
    }

    private void handleAssert(TestCase tc, TestStep step, String yamlFile, String name, String action, Object expected,
            Boolean printExecution, TestLogger logger, boolean isVerbose) {

        YamlPlaceholderResolver resolver = new YamlPlaceholderResolver();

        Object actualObj = tc.getVariables().get(name);
        String actual = (actualObj != null) ? String.valueOf(actualObj) : null;
        int stepIndex = 0;
        
        String expectedResolved = "";
        if (expected != null) {
            expectedResolved = resolver.resolve(tc, expected.toString(), tc.getVariables(), yamlFile, stepIndex, "VALUE");
        }

        String cleanAction = (action != null) ? action.toUpperCase() : "";

        if (Boolean.TRUE.equals(printExecution) && isVerbose) {
            if (cleanAction.contains("NULL") || cleanAction.contains("EMPTY")) {
                logger.info(String.format("    %s>>> ASSERT     %s: [%s] | %s", 
                            ConsoleColors.GREEN, ConsoleColors.RESET, name, cleanAction));
            } else {
                logger.info(String.format("    %s>>> ASSERT     %s: [%s] | %s \"%s\"", 
                            ConsoleColors.GREEN, ConsoleColors.RESET, name, cleanAction, expectedResolved));
            }
        }
        
        if (!cleanAction.equals("EQUALS") && !cleanAction.equals("NOT_EQUALS") && !cleanAction.equals("CONTAINS") 
                && !cleanAction.equals("IS_NULL") && !cleanAction.equals("IS_NOT_NULL") 
                && !cleanAction.equals("IS_EMPTY") && !cleanAction.equals("IS_NOT_EMPTY")) {
            
            String errorMsg = "Unknown assertion type: \"" + cleanAction + "\"";
            throw new RuntimeException(errorMsg);
        }
        
        try {
            boolean success = compareStrings(actual, expectedResolved, cleanAction);
            String actualValueDisplay = (actual == null) ? "null" : actual;

            if (success) {
                if (Boolean.TRUE.equals(printExecution) && isVerbose) {
                    logger.info(String.format("    %s>>> RESULT     %s: SUCCESS | Value is \"%s\"", ConsoleColors.GREEN, ConsoleColors.RESET, actualValueDisplay));
                }
                
            } else {
                String msg = String.format("Assertion %s failed for buffer [%s] | Expected: [%s], Actual: [%s]", 
                           cleanAction, name, expectedResolved, actualValueDisplay);
                
                throw new RuntimeException(msg);
            }
            
        } catch (Exception e) {
            if (Boolean.TRUE.equals(printExecution)) {
                logger.info(String.format("    %s>>> RESULT     %s: FAILED | %s", 
                            ConsoleColors.RED, ConsoleColors.RESET, e.getMessage()));
            }
            throw e;
        }
        
    }

    private boolean compareStrings(String actual, String exp, String action) {
        if (actual == null) {
            if ("IS_NULL".equals(action)) return true;
            if ("IS_NOT_NULL".equals(action)) return false;
            if ("IS_EMPTY".equals(action)) return true;
            return false;
        }

        switch (action) {
            case "EQUALS":
                return actual.equals(exp);
            case "NOT_EQUALS":
                return !actual.equals(exp);
            case "CONTAINS":
                return exp != null && actual.contains(exp);
            case "IS_NULL":
                return false;
            case "IS_NOT_NULL":
                return true;
            case "IS_EMPTY":
                return actual.isEmpty();
            case "IS_NOT_EMPTY":
                return !actual.isEmpty();
            default:
                return false;
        }
    }
}