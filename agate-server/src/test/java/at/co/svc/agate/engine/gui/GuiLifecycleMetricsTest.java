package at.co.svc.agate.engine.gui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class GuiLifecycleMetricsTest {
    @Test void tracksCleanupFailuresAndKeepsMetricsLocal() {
        GuiLifecycleMetrics first = new GuiLifecycleMetrics();
        GuiLifecycleMetrics second = new GuiLifecycleMetrics();
        first.add("PLAYWRIGHT_CREATE", 120);
        first.add("BROWSER_LAUNCH", 300);
        first.add("CONTEXT_CREATE", 10);
        first.add("CONTEXT_CLOSE", 5);
        first.add("PLAYWRIGHT_CLOSE", 3);
        first.failure();
        first.forcedDriverStop();
        assertEquals(1, first.failures());
        assertEquals(0, second.failures(), "suite metrics must not leak to other runs");
        assertDoesNotThrow(() -> first.printSummary("test-suite", true));
    }
}
