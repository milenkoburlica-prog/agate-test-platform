package at.co.svc.agate.core.dsl.register;

import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SqlRowCountBreakConditionTest {
    private String resolve(ExecutionContext context, String condition) throws Exception {
        Method method = TestExecutor.class.getDeclaredMethod(
                "resolveSqlRowCountsInCondition", String.class, ExecutionContext.class);
        method.setAccessible(true);
        try {
            return (String) method.invoke(new TestExecutor(null), condition, context);
        } catch (java.lang.reflect.InvocationTargetException exception) {
            if (exception.getCause() instanceof RuntimeException runtime) throw runtime;
            throw exception;
        }
    }

    @Test
    void emptySelectTriggersBreakBeforeSqlBuffer() throws Exception {
        var context = new ExecutionContext(null);
        context.storeBuffer("bvid_res", List.of());
        var expression = resolve(context, "{SQL_ROW_COUNT[bvid_res]} == 0");
        assertEquals("0 == 0", expression);
        assertTrue(new ConditionEvaluator().evaluate(expression));
    }

    @Test
    void existingRowDoesNotTriggerBreak() throws Exception {
        var context = new ExecutionContext(null);
        context.storeBuffer("bvid_res", List.of(Map.of("BVID", "123")));
        assertEquals("1 == 0", resolve(context, "{SQL_ROW_COUNT[bvid_res]} == 0"));
        assertFalse(new ConditionEvaluator().evaluate("1 == 0"));
    }

    @Test
    void missingSelectResponseIsNotTreatedAsEmpty() {
        var context = new ExecutionContext(null);
        assertThrows(IllegalArgumentException.class,
                () -> resolve(context, "{SQL_ROW_COUNT[missing]} == 0"));
    }
}
