package at.co.svc.aga.transformator.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ProcessAPILogicConditionalYamlTest {
    private String invokeEscape(String value) {
        try {
            var method = ProcessAPILogic.class.getDeclaredMethod("escapeYamlDoubleQuoted", String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void escapingConditionPreservesExpressionSemantics() {
        String condition = "({XL[Antwort.EingabeStatus]} != \"\") AND (Antwort.EingabeStatus != \"\")";
        String escaped = invokeEscape(condition);
        assertEquals(condition, escaped.replace("\\\"", "\""));
    }
}
