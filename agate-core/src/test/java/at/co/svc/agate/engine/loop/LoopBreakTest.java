package at.co.svc.agate.engine.loop;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class LoopBreakTest {
    private static TestStep child(StepType type, String condition) {
        TestStep step = new TestStep();
        step.setType(type);
        step.setCondition(condition);
        return step;
    }
    private static TestStep loop(TestStep... children) {
        TestStep step = child(StepType.LOOP, "true");
        step.setLoopMode("DO_WHILE");
        step.setMaxIterations(3);
        step.setTimeoutMs(10000L);
        step.setSubSteps(List.of(children));
        return step;
    }
    @Test void breakTrueSkipsFollowingStepsAndStopsLoop() throws Exception {
        AtomicInteger executed = new AtomicInteger();
        LoopEngine engine = new LoopEngine((tc,s,ctx,y,n,v) -> executed.incrementAndGet(),
            (tc,s,ctx,y,n,v) -> true);
        engine.doExecute(new TestCase(), loop(child(StepType.BREAK,"true"),child(StepType.WAIT,null)),
            new ExecutionContext(null),"test.yaml",1,false,null,false);
        assertEquals(0,executed.get());
    }
    @Test void breakFalseContinuesToNextChild() throws Exception {
        AtomicInteger executed = new AtomicInteger();
        AtomicInteger checks = new AtomicInteger();
        LoopEngine engine = new LoopEngine((tc,s,ctx,y,n,v) -> executed.incrementAndGet(),
            (tc,s,ctx,y,n,v) -> s.getType()==StepType.BREAK ? false : checks.incrementAndGet()<1);
        engine.doExecute(new TestCase(), loop(child(StepType.BREAK,"false"), child(StepType.WAIT,null)),
            new ExecutionContext(null),"test.yaml",1,false,null,false);
        assertEquals(1,executed.get());
    }
    @Test void breakConditionUsesExistingEvaluatorAndNeverExecutesAsStep() throws Exception {
        AtomicInteger checks = new AtomicInteger();
        LoopEngine engine = new LoopEngine((tc,s,ctx,y,n,v) -> fail("BREAK must not dispatch"),
            (tc,s,ctx,y,n,v) -> { checks.incrementAndGet(); return true; });
        engine.doExecute(new TestCase(),loop(child(StepType.BREAK,"{SQL_ROW_COUNT[r]} == 0")),
            new ExecutionContext(null),"test.yaml",1,false,null,false);
        assertEquals(1,checks.get());
    }
}
