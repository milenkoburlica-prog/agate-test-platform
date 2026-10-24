package at.co.svc.agate.core.dsl.register;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SqlCellBreakConditionTest {
    private boolean evaluate(ExecutionContext context, StepType type, String condition) {
        TestStep step = new TestStep();
        step.setType(type);
        step.setCondition(condition);
        return new TestExecutor(null).evaluateLoopCondition(
                new TestCase(), step, context, "sql-condition.yaml", 1, false);
    }

    @Test
    void breakOnCountValueNotNumberOfRows() {
        ExecutionContext context = new ExecutionContext(null);
        String condition = "{SQL[patient_count_res][0][0]} == 1";
        context.storeBuffer("patient_count_res", List.of(Map.of("COUNT(*)", 0)));
        assertFalse(evaluate(context, StepType.BREAK, condition));
        context.storeBuffer("patient_count_res", List.of(Map.of("COUNT(*)", 1)));
        assertTrue(evaluate(context, StepType.BREAK, condition));
        // SELECT COUNT(*) returns one row in both cases.
        assertTrue(evaluate(context, StepType.BREAK, "{SQL_ROW_COUNT[patient_count_res]} == 1"));
    }

    @Test
    void loopConditionCanUseNamedColumn() {
        ExecutionContext context = new ExecutionContext(null);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("PATIENT_COUNT", 1);
        context.storeBuffer("patient_count_res", List.of(row));
        assertTrue(evaluate(context, StepType.LOOP, "{SQL[patient_count_res][0][patient_count]} == 1"));
    }

    @Test
    void invalidSqlCellFailsExplicitly() {
        ExecutionContext context = new ExecutionContext(null);
        assertThrows(IllegalArgumentException.class,
                () -> evaluate(context, StepType.BREAK, "{SQL[missing][0][0]} == 1"));
        context.storeBuffer("patient_count_res", List.of());
        assertThrows(IllegalArgumentException.class,
                () -> evaluate(context, StepType.BREAK, "{SQL[patient_count_res][0][0]} == 1"));
    }

    @Test
    void bufferConditionStillWorks() {
        ExecutionContext context = new ExecutionContext(null);
        context.storeBuffer("patientCount", "1");
        assertTrue(evaluate(context, StepType.BREAK, "{B[patientCount]} == 1"));
    }
}
