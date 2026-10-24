package at.co.svc.aga.transformator.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProcessGuiStepHelpAndSelectTest {
    @Test
    void onlineHelpTavCloseTargetsPopupNotBrowser() {
        Map<String,Object> step = new LinkedHashMap<>();
        step.put("type", "GUI"); step.put("op", "EXEC");
        step.put("action", "CLOSE_BROWSER");
        step.put("condition", "(NOT ('{E[env.B_TestkonfigBrowser]}' == 'InternetExplorer')) AND (NOT ('{B[ONLINE_HILFE_APPL]}' == 'TSV'))");
        ProcessGuiStep.adaptKnownHelpTabClose(step, "data/dmp_gui/reusable/online_hilfe_aufrufen_de.yaml");
        assertEquals("CLOSE_TAB", step.get("action"));
        assertEquals("*chipkarte.at*", step.get("urlGlob"));
    }
    @Test
    void unrelatedCloseBrowserRemainsIntact() {
        Map<String,Object> step = new LinkedHashMap<>();
        step.put("type", "GUI"); step.put("action", "CLOSE_BROWSER");
        step.put("condition", "NOT ('{B[ONLINE_HILFE_APPL]}' == 'TSV')");
        ProcessGuiStep.adaptKnownHelpTabClose(step, "data/reusable/logout.yaml");
        assertEquals("CLOSE_BROWSER", step.get("action"));
    }
    @Test
    void exactDropdownIdKeepsLabelInsideDropdown() {
        Map<String,Object> step = new LinkedHashMap<>();
        step.put("type", "GUI"); step.put("op", "EXEC"); step.put("action", "SELECT");
        step.put("target", Map.of("css", "select[id=\"DmpForm:dmpProgramm\"]"));
        step.put("value", Map.of("label", "Diabetes mellitus Typ 2"));
        ProcessGuiStep.humanReadable(step);
        assertFalse(step.containsKey("target"));
        assertFalse(step.containsKey("value"));
        assertEquals(Map.of("id", "DmpForm:dmpProgramm", "value", "Diabetes mellitus Typ 2"), step.get("dropdown"));
    }
}
