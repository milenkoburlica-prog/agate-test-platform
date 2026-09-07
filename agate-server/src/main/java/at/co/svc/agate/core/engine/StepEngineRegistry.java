package at.co.svc.agate.core.engine;

import java.util.ArrayList;
import java.util.List;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.interfaces.TestStepEngine;
import at.co.svc.agate.engine.call.CallEngine;
import at.co.svc.agate.engine.cmd.CmdEngine;
import at.co.svc.agate.engine.file.FileEngine;
import at.co.svc.agate.engine.gui.GuiEngine;
import at.co.svc.agate.engine.json.JsonEngine;
import at.co.svc.agate.engine.oc.OcCmdEngine;
import at.co.svc.agate.engine.rest.RestEngine;
import at.co.svc.agate.engine.sql.SqlEngine;
import at.co.svc.agate.engine.wait.WaitEngine;

public class StepEngineRegistry {

    private final List<TestStepEngine> engines =
            new ArrayList<>();

    public StepEngineRegistry(
            CallEngine.StepExecutor executor) {

        engines.add(new SqlEngine());

        engines.add(new CmdEngine());
        engines.add(new OcCmdEngine());
        engines.add(new FileEngine());

        engines.add(new GuiEngine());

        engines.add(new RestEngine());
        engines.add(new WaitEngine());
        engines.add(new JsonEngine());

        engines.add(
                new CallEngine(
                        executor));
    }

    public void register(
            TestStepEngine engine) {

        engines.add(engine);
    }

    public TestStepEngine getEngine(
            TestStep step) {

        StepType type =
                step.getType();

        for (TestStepEngine engine :
                engines) {

            if (!engine.canExecute(type)) {
                continue;
            }

            if (type == StepType.REST
                    && (step.getName() == null
                            || step.getResponse() == null)) {

                continue;
            }

            return engine;
        }

        throw new RuntimeException(
                "No engine found capable of executing step type: "
                        + type
                        + " with action: "
                        + step.getAction());
    }
}