package at.co.svc.aga.transformator.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProcessGuiStepBrowserTabsCloseTest {
    private static Map<String,Object> closeStep() {
        Map<String,Object> step = new LinkedHashMap<>();
        step.put("id", "01KZE1VKTW3SWB612R4REDT5S7_close");
        step.put("type", "GUI");
        step.put("op", "EXEC");
        step.put("action", "CLOSE_BROWSER");
        return step;
    }

    @Test
    void browserTabsReusableUsesDesktopWindowClose() {
        Map<String,Object> step = closeStep();
        ProcessGuiStep.adaptBrowserTabsClose(step, "data/dmp_gui/reusable/browser_tabs_schließen.yaml");
        assertEquals("WINDOW", step.get("type"));
        assertEquals("EXEC", step.get("op"));
        assertEquals("CLOSE", step.get("action"));
        assertEquals("e-card*", step.get("caption"));
        assertEquals("01KZE1VKTW3SWB612R4REDT5S7_close", step.get("id"));
    }

    @Test
    void otherReusableRemainsPlaywrightClose() {
        Map<String,Object> step = closeStep();
        ProcessGuiStep.adaptBrowserTabsClose(step, "data/dmp_gui/reusable/logout.yaml");
        assertEquals("GUI", step.get("type"));
        assertEquals("CLOSE_BROWSER", step.get("action"));
        assertFalse(step.containsKey("caption"));
    }

    @Test
    void similarlyNamedFileDoesNotMatch() {
        Map<String,Object> step = closeStep();
        ProcessGuiStep.adaptBrowserTabsClose(step, "data/reusable/copy_browser_tabs_schließen.yaml");
        assertEquals("CLOSE_BROWSER", step.get("action"));
    }

    @Test
    void noTransformationForUnrelatedAction() {
        Map<String,Object> step = closeStep();
        step.put("action", "CLICK");
        ProcessGuiStep.adaptBrowserTabsClose(step, "browser_tabs_schließen.yaml");
        assertEquals("CLICK", step.get("action"));
        assertFalse(step.containsKey("caption"));
    }

    @Test
    void windowsStylePathIsRecognized() {
        Map<String,Object> step = closeStep();
        ProcessGuiStep.adaptBrowserTabsClose(step, "C:\\data\\reusable\\browser_tabs_schließen.yaml");
        assertEquals("WINDOW", step.get("type"));
    }
}
