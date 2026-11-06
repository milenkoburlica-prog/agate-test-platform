package at.co.svc.agate.core.engine;

import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.interfaces.TestLogger;
import at.co.svc.agate.core.interfaces.TestStepEngine;

public abstract class AbstractStepEngine implements TestStepEngine {

    @Override
    public final void execute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, 
                              int stepIndex, Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {
        
        //long startTime = System.nanoTime();
        
        // Calling specific logic of each engine
        doExecute(tc, step, context, yamlFile, stepIndex, printExecution, logger, isVerbose);
        
        //long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        
        if (Boolean.TRUE.equals(printExecution)) {
            //logger.info(String.format("    >>> STEP DURATION: %d ms", durationMs));
        }
    }

    // Engines will implement this method instead of the original execute
    protected abstract void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, 
                                     int stepIndex, Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception;
}