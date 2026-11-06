package at.co.svc.agate.core.interfaces;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;

/**
 * Common interface for all step execution engines.
 */
public interface TestStepEngine {
    
    /**
     * Checks if this engine can handle the given step type.
     */
    boolean canExecute(StepType type);

    /**
     * Executes the logic for a specific test step.
     */
    void execute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, 
                 int stepIndex, Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception;
}