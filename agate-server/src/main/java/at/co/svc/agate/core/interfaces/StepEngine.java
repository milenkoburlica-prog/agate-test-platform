package at.co.svc.agate.core.interfaces;

import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;

/**
 * StepEngine defines a component capable of executing a DSL step.
 *
 * Each engine handles a specific StepType (REST, SQL, CMD, etc).
 */
public interface StepEngine {

    void execute(TestStep step, ExecutionContext context) throws Exception;

}