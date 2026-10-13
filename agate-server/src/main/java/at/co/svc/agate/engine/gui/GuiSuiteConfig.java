package at.co.svc.agate.engine.gui;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/** Optional suite-level browser configuration. No test-case DSL changes. */
public record GuiSuiteConfig(String browser, boolean headless, boolean reuse,
                             boolean ignoreHttpsErrors, boolean allowLocalNetwork,
                             boolean trace, String executable, String args,
                             boolean lifecycleLog, boolean explicitGuiSection) {
    /** API compatibility with existing eight-argument constructors. */
    public GuiSuiteConfig(String browser, boolean headless, boolean reuse,
                          boolean ignoreHttpsErrors, boolean allowLocalNetwork,
                          boolean trace, String executable, String args) {
        this(browser, headless, reuse, ignoreHttpsErrors, allowLocalNetwork,
                trace, executable, args, Boolean.getBoolean("agate.gui.lifecycleLog"), false);
    }
    public static GuiSuiteConfig fromYaml(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Object data = new Yaml().load(reader);
            if (!(data instanceof Map<?, ?> root)) return defaults();
            Object node = root.get("gui");
            if (node == null) return defaults();
            if (!(node instanceof Map<?, ?> m)) {
                throw new IllegalArgumentException("'gui' must be a mapping in " + file);
            }
            String mode = string(m, "sessionMode", "ISOLATED").toUpperCase(Locale.ROOT);
            if (!"ISOLATED".equals(mode)) {
                throw new IllegalArgumentException("Only gui.sessionMode: ISOLATED is supported (no SHARED mode)");
            }
            String requested = string(m, "browser", defaultBrowser()).toLowerCase(Locale.ROOT);
            if (!java.util.Set.of("chromium", "internal", "chrome", "edge", "msedge").contains(requested)) {
                throw new IllegalArgumentException("Unsupported gui.browser: " + requested);
            }
            return new GuiSuiteConfig(requested,
                    bool(m, "headless", Boolean.parseBoolean(System.getProperty("agate.gui.headless", "false"))),
                    bool(m, "reuse", true),
                    bool(m, "ignoreHttpsErrors", Boolean.getBoolean("agate.gui.ignoreHttpsErrors")),
                    bool(m, "allowLocalNetwork", Boolean.parseBoolean(System.getProperty("agate.gui.allowLocalNetwork", "true"))),
                    bool(m, "trace", Boolean.getBoolean("agate.gui.trace")),
                    string(m, "executable", System.getProperty("agate.gui.executable")),
                    string(m, "args", System.getProperty("agate.gui.args")),
                    bool(m, "lifecycleLog", Boolean.getBoolean("agate.gui.lifecycleLog")),
                    true);
        }
    }
    public static GuiSuiteConfig defaults() {
        return new GuiSuiteConfig(defaultBrowser(),
                Boolean.parseBoolean(System.getProperty("agate.gui.headless", "false")), true,
                Boolean.getBoolean("agate.gui.ignoreHttpsErrors"),
                Boolean.parseBoolean(System.getProperty("agate.gui.allowLocalNetwork", "true")),
                Boolean.getBoolean("agate.gui.trace"),
                System.getProperty("agate.gui.executable"), System.getProperty("agate.gui.args"),
                Boolean.getBoolean("agate.gui.lifecycleLog"), false);
    }
    static String defaultBrowser() {
        String browser = System.getProperty("agate.gui.browser");
        if (browser == null || browser.isBlank()) browser = System.getenv("AGATE_GUI_BROWSER");
        return browser == null || browser.isBlank() ? "chromium" : browser.trim().toLowerCase(Locale.ROOT);
    }
    private static String string(Map<?, ?> map, String key, String fallback) {
        return map.get(key) == null ? fallback : String.valueOf(map.get(key));
    }
    private static boolean bool(Map<?, ?> map, String key, boolean fallback) {
        if (map.get(key) == null) return fallback;
        Object value = map.get(key);
        if (value instanceof Boolean b) return b;
        if ("true".equalsIgnoreCase(value.toString())) return true;
        if ("false".equalsIgnoreCase(value.toString())) return false;
        throw new IllegalArgumentException("gui." + key + " must be true or false");
    }
}
