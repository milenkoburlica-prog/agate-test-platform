package at.co.svc.aga.transformator.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProcessGuiStepNullNormalizationTest {
    private static Map<String,Object> gui(String action, String field, Object value) {
        Map<String,Object> step = new LinkedHashMap<>();
        step.put("type", "GUI");
        step.put("op", "EXEC");
        step.put("action", action);
        step.put(field, value);
        return step;
    }

    @Test void fillNullClearsTextbox() {
        for (String literal : new String[]{"{NULL}", "\"{NULL}\""}) {
            Map<String,Object> step = gui("FILL", "value", literal);
            ProcessGuiStep.humanReadable(step);
            assertEquals("", step.get("value"));
        }
    }

    @Test void textGlobRemovesNullWithoutChangingWildcards() {
        Map<String,Object> step = gui("TEXT_GLOB", "expected", "{NULL}*Josef*");
        ProcessGuiStep.humanReadable(step);
        assertEquals("*Josef*", step.get("expected"));
        Map<String,Object> quoted = gui("TEXT_GLOB", "expected", "\"{NULL}\"*Josef*");
        ProcessGuiStep.humanReadable(quoted);
        assertEquals("*Josef*", quoted.get("expected"));
    }

    @Test void unrelatedGuiAndNonGuiActionsAreUnaffected() {
        Map<String,Object> text = gui("TEXT", "expected", "{NULL}");
        ProcessGuiStep.normalizeGuiNullTokens(text);
        assertEquals("{NULL}", text.get("expected"));
        Map<String,Object> notGui = gui("FILL", "value", "{NULL}");
        notGui.put("type", "HTTP");
        ProcessGuiStep.normalizeGuiNullTokens(notGui);
        assertEquals("{NULL}", notGui.get("value"));
    }

    @Test void nestedTextboxIsNormalized() {
        Map<String,Object> step = gui("FILL", "textbox", Map.of("id", "DmpForm:plz", "value", "{NULL}"));
        ProcessGuiStep.normalizeGuiNullTokens(step);
        assertEquals("", ((Map<?,?>)step.get("textbox")).get("value"));
    }
}
