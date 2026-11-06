package at.co.svc.agate.core.dsl.register;

import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class TestVariableInitializerTest {
    @Test
    void preservesAllTestVariablesLiterallyWithoutResolvingBuffersOrDates() {
        TestCase testCase = new TestCase();
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("DialogID", "{B[dialogId]}");
        variables.put("DmpCode", "{NULL}");
        variables.put("searchAnfragenBasisdaten.EinschreibungSeit", "{DATE[][][]}");
        variables.put("Empty", "{EMPTY}");
        variables.put("XL", "{XL[test]}");
        testCase.setVariables(variables);
        ExecutionContext context = new ExecutionContext(null);

        assertDoesNotThrow(() -> TestVariableInitializer.copyLiteralValues(testCase, context));
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (TestVariableInitializer.containsDeferredRuntimePlaceholder(String.valueOf(entry.getValue()))) {
                assertNull(context.getVar(entry.getKey()));
            } else {
                assertEquals(entry.getValue(), context.getVar(entry.getKey()));
            }
            assertEquals(entry.getValue(), testCase.getVariables().get(entry.getKey()));
        }
    }

    @Test
    void skipsNullsWithoutModifyingTestCaseMap() {
        TestCase testCase = new TestCase();
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("unset", null);
        variables.put("numeric", 42);
        testCase.setVariables(variables);
        ExecutionContext context = new ExecutionContext(null);
        TestVariableInitializer.copyLiteralValues(testCase, context);
        assertNull(context.getVar("unset"));
        assertEquals("42", context.getVar("numeric"));
        assertEquals(42, testCase.getVariables().get("numeric"));
    }
    @Test
    void deferredDialogIdDoesNotShadowRuntimeDialogId() {
        TestCase tc = new TestCase();
        tc.setVariables(new LinkedHashMap<>(Map.of("dialogId", "{B[dialogId]}", "VPNR", "645031")));
        ExecutionContext ctx = new ExecutionContext(null);
        TestVariableInitializer.copyLiteralValues(tc, ctx);
        assertEquals("{B[dialogId]}", tc.getVariables().get("dialogId"));
        assertNull(ctx.getVar("dialogId"));
        assertEquals("645031", ctx.getVar("VPNR"));
        ctx.setVar("dialogId", "resolved-runtime-dialog-id");
        assertEquals("resolved-runtime-dialog-id", ctx.getVar("dialogId"));
    }
}
