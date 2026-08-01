package at.co.svc.agate.core.engine;

import java.util.ArrayList;
import java.util.List;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.interfaces.TestStepEngine;
import at.co.svc.agate.engine.call.CallEngine;
import at.co.svc.agate.engine.cmd.CmdEngine;
import at.co.svc.agate.engine.gui.GuiEngine;
import at.co.svc.agate.engine.json.JsonEngine;
import at.co.svc.agate.engine.oc.OcCmdEngine;
import at.co.svc.agate.engine.rest.RestEngine;
import at.co.svc.agate.engine.sql.SqlEngine;
import at.co.svc.agate.engine.wait.WaitEngine;

/**
 * Registry that manages all available test step engines.
 */
public class StepEngineRegistry {

    private final List<TestStepEngine> engines = new ArrayList<>();

    // Constructor now accepts a reference to the execution mechanism (StepExecutor)
    public StepEngineRegistry(CallEngine.StepExecutor executor) {
        // SQL Group
        engines.add(new SqlEngine());

        // CMD / OpenShift Group
        engines.add(new CmdEngine());
        engines.add(new OcCmdEngine());

        // GUI Group
        engines.add(new GuiEngine());

        // Communication & Utility
        engines.add(new RestEngine());
        engines.add(new WaitEngine());
        engines.add(new JsonEngine());

        // Passing executor through lambda into CallEngine
        engines.add(new CallEngine(executor));
    }

    /**
     * Registers a new engine into the system.
     */
    public void register(TestStepEngine engine) {
        engines.add(engine);
    }

    /**
     * Returns the first engine that claims it can execute the given step.
     */
    public TestStepEngine getEngine(TestStep step) {
        StepType type = step.getType();
        
        for (TestStepEngine engine : engines) {
            if (engine.canExecute(type)) {
                // Special check for Buffer/Rest engines: 
                // They often use the same type, but require specific fields
                // Note: If an engine does not use name/response, 
                // you might need to extend this if condition.
                if ((step.getName() == null || step.getResponse() == null) && type == StepType.REST) {
                    continue; 
                }
                return engine;
            }
        }

        throw new RuntimeException("No engine found capable of executing step type: " + type 
            + " with action: " + step.getAction());
    }
}