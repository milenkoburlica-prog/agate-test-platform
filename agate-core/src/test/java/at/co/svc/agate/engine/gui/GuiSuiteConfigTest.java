package at.co.svc.agate.engine.gui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GuiSuiteConfigTest {
    @Test void noGuiSectionKeepsDefaultIsolatedReuse() throws Exception {
        Path yaml = Files.createTempFile("agate-gui-suite", ".yaml");
        try {
            Files.writeString(yaml, "testCases: []\n");
            GuiSuiteConfig config = GuiSuiteConfig.fromYaml(yaml);
            assertTrue(config.reuse());
            assertEquals("chromium", config.browser());
        } finally { Files.deleteIfExists(yaml); }
    }
    @Test void explicitSuiteConfigIsRead() throws Exception {
        Path yaml = Files.createTempFile("agate-gui-suite", ".yaml");
        try {
            Files.writeString(yaml, "gui:\n  browser: edge\n  headless: true\n  reuse: true\n  sessionMode: ISOLATED\ntestCases: []\n");
            GuiSuiteConfig config = GuiSuiteConfig.fromYaml(yaml);
            assertEquals("edge", config.browser());
            assertTrue(config.headless());
            assertTrue(config.reuse());
        } finally { Files.deleteIfExists(yaml); }
    }
    @Test void sharedModeIsRejected() throws Exception {
        Path yaml = Files.createTempFile("agate-gui-suite", ".yaml");
        try {
            Files.writeString(yaml, "gui:\n  sessionMode: SHARED\ntestCases: []\n");
            assertThrows(IllegalArgumentException.class, () -> GuiSuiteConfig.fromYaml(yaml));
        } finally { Files.deleteIfExists(yaml); }
    }
}
