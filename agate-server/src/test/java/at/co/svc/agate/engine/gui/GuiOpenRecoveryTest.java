package at.co.svc.agate.engine.gui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class GuiOpenRecoveryTest {
    @Test void recognizesClosedBrowserMessages() {
        assertTrue(GuiEngine.isUnexpectedBrowserClose(new com.microsoft.playwright.PlaywrightException(
                "TargetClosedError: Target page, context or browser has been closed")));
        assertFalse(GuiEngine.isUnexpectedBrowserClose(new com.microsoft.playwright.PlaywrightException(
                "Timeout 30000ms exceeded")));
    }
}
