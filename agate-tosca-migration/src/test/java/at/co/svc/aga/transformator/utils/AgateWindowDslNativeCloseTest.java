package at.co.svc.aga.transformator.utils;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AgateWindowDslNativeCloseTest {
    private static Map<String,Object> close() {
        Map<String,Object> step = new LinkedHashMap<>();
        step.put("id", "close-1");
        step.put("type", "GUI");
        step.put("op", "EXEC");
        step.put("action", "CLOSE_BROWSER");
        return step;
    }
    private static Map<String,Object> predicate(String caption) {
        Map<String,Object> p = new LinkedHashMap<>();
        p.put("id", "predicate-1");
        p.put("type", "WINDOW");
        p.put("op", "EXISTS");
        p.put("window", Map.of("titleGlob", caption));
        p.put("expected", true);
        return p;
    }
    private static List<Map<String,Object>> control(String caption) {
        Map<String,Object> control = new LinkedHashMap<>();
        control.put("id", "native-loop");
        control.put("op", "DO_WHILE");
        control.put("predicate", Map.of("all", List.of(predicate(caption))));
        control.put("steps", List.of(close()));
        control.put("maxIterations", 10);
        return AgateWindowDsl.normalize(List.of(control));
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> convertedClose(String caption) {
        Map<String,Object> loop = control(caption).stream()
                .filter(s -> "LOOP".equals(s.get("type"))).findFirst().orElseThrow();
        return ((List<Map<String,Object>>)loop.get("steps")).stream()
                .filter(s -> "close-1".equals(s.get("id"))).findFirst().orElseThrow();
    }
    @Test void convertsEcardClose() {
        Map<String,Object> c=convertedClose("e-card*");
        assertEquals("WINDOW",c.get("type"));
        assertEquals("EXEC",c.get("op"));
        assertEquals("CLOSE",c.get("action"));
        assertEquals("e-card*",c.get("caption"));
    }
    @Test void preservesServicesCaption() {
        assertEquals("services-",convertedClose("services-").get("caption"));
    }
    @Test void standaloneCloseBrowserRemainsGui() {
        assertEquals("CLOSE_BROWSER",AgateWindowDsl.normalize(List.of(close())).get(0).get("action"));
    }
}
