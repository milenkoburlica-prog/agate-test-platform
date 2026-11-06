package at.co.svc.agate.core.dsl.register;

import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class GuiNullSemanticsTest {
    private static Map<String,Object> step(String type, String action, String field, Object value) {
        Map<String,Object> s = new LinkedHashMap<>();
        s.put("type", type); s.put("action", action); s.put(field, value); return s;
    }
    @Test void fillsBecomeEmptyButStepIsRetained() {
        for (String input : List.of("{NULL}", "\"{NULL}\"")) {
            Map<String,Object> s = step("GUI", "FILL", "value", input);
            GuiNullSemantics.normalizeStep(s);
            assertEquals("", s.get("value"));
        }
    }
    @Test void textGlobStripsOnlyNullMarker() {
        Map<String,Object> s = step("GUI", "TEXT_GLOB", "expected", "{NULL}*Josef*");
        GuiNullSemantics.normalizeStep(s);
        assertEquals("*Josef*", s.get("expected"));
    }
    @Test void nestedLegacyAssertionAndCompactTextbox() {
        Map<String,Object> assertion = new LinkedHashMap<>(Map.of("type","TEXT_GLOB", "expected", "{NULL}*Josef*"));
        Map<String,Object> a = step("GUI", "ASSERT", "assertion", assertion);
        Map<String,Object> textbox = new LinkedHashMap<>(Map.of("id", "name", "value", "{NULL}"));
        Map<String,Object> b = step("GUI", "FILL", "textbox", textbox);
        GuiNullSemantics.normalizeTree(Map.of("steps",List.of(a,b)));
        assertEquals("*Josef*", assertion.get("expected"));
        assertEquals("", textbox.get("value"));
    }
    @Test void nonGuiAndNormalTextAreUntouched() {
        Map<String,Object> rest = step("REST", "FILL", "value", "{NULL}");
        Map<String,Object> normal = step("GUI", "FILL", "value", "Joe {NULL}");
        GuiNullSemantics.normalizeStep(rest); GuiNullSemantics.normalizeStep(normal);
        assertEquals("{NULL}", rest.get("value"));
        assertEquals("Joe {NULL}", normal.get("value"));
    }
}
