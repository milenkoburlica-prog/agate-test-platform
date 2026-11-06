package at.co.svc.agate.engine.gui;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GuiLifecycleLoggingTest {
    @Test void suiteConfigDefaultIsQuietAndIsolatedWithoutExplicitMode() throws Exception {
        Path path = Files.createTempFile("agate-lifecycle", ".yaml");
        try {
            Files.writeString(path, "gui:\n  browser: chrome\n  headless: true\n  reuse: false\ntestCases: []\n");
            GuiSuiteConfig config = GuiSuiteConfig.fromYaml(path);
            assertEquals(Boolean.getBoolean("agate.gui.lifecycleLog"), config.lifecycleLog());
            assertTrue(config.explicitGuiSection());
            assertEquals("chrome", config.browser());
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            PrintStream original = System.out;
            try {
                System.setOut(new PrintStream(result));
                try (GuiRunScope ignored = GuiRunScope.fromYaml(path.toString())) { }
            } finally { System.setOut(original); }
            String log = result.toString();
            assertTrue(log.contains("[GUI] CONFIG browser=chrome headless=true reuse=false"));
            assertFalse(log.contains("sessionMode"));
            assertFalse(log.contains("[GUI metrics]"));
            assertFalse(log.contains("[GUI lifecycle]"));
        } finally { Files.deleteIfExists(path); }
    }
    @Test void explicitVerboseModeIsPerSuite() throws Exception {
        Path path = Files.createTempFile("agate-lifecycle", ".yaml");
        try {
            Files.writeString(path, "gui:\n  browser: edge\n  lifecycleLog: true\ntestCases: []\n");
            GuiSuiteConfig config = GuiSuiteConfig.fromYaml(path);
            assertTrue(config.lifecycleLog());
            assertTrue(new GuiLifecycleMetrics(config.lifecycleLog()).verbose());
        } finally { Files.deleteIfExists(path); }
    }
}
