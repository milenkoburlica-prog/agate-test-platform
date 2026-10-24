package at.co.svc.agate.engine.gui;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class GuiElementTargetIdentifierTest {
    @Test void allIdMatchersWorkForEveryControl() {
        for (String kind : GuiElementTarget.KINDS) {
            for (var e : Map.of("id", "=", "idStartsWith", "^=", "idEndsWith", "$=", "idContains", "*=").entrySet()) {
                Map<String,Object> step=Map.of(kind, Map.of(e.getKey(), "probe"));
                var target=GuiElementTarget.fromStep(step);
                assertTrue(target.get("css").toString().contains("[id"+e.getValue()+"\"probe\"]"),kind+" / "+e.getKey());
            }
        }
    }
    @Test void cannotUseTwoLocatorStrategies() {
        assertThrows(IllegalArgumentException.class, () ->
            GuiElementTarget.fromStep(Map.of("textbox", Map.of("id", "a", "idContains", "b"))));
    }
    @Test void visibleButtonTextIsRoleBased() {
        var target=GuiElementTarget.fromStep(Map.of("button",Map.of("text","(W)eiter")));
        assertEquals("button",target.get("role"));
        assertEquals("(W)eiter",target.get("name"));
    }
}
