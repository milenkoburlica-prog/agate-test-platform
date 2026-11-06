package at.co.svc.agate.engine.gui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

/** Owns browser resources for one invocation of a YAML suite. */
public final class GuiRunScope implements AutoCloseable {
    private final String id = UUID.randomUUID().toString();
    private final GuiSuiteConfig config;
    private final GuiLifecycleMetrics metrics;
    private boolean configurationPrinted;
    private final GuiBrowserHost host;
    private volatile boolean closed;
    private GuiRunScope(GuiSuiteConfig config) {
        this.config = config;
        this.metrics = new GuiLifecycleMetrics(config.lifecycleLog());
        this.host = config.reuse() ? new GuiBrowserHost(config, metrics) : null;
        if (config.explicitGuiSection()) printConfiguration();
    }
    public static GuiRunScope fromYaml(String yamlFile) throws IOException {
        return new GuiRunScope(GuiSuiteConfig.fromYaml(Path.of(yamlFile)));
    }
    public String id() { return id; }
    synchronized GuiSession newSession() {
        printConfiguration();
        if (closed) throw new IllegalStateException("GUI run is already closed: " + id);
        return new GuiSession(host, config, metrics);
    }
    private void printConfiguration() {
        if (configurationPrinted) return;
        configurationPrinted = true;
        System.out.println("[GUI] CONFIG browser=" + config.browser()
                + " headless=" + config.headless()
                + " reuse=" + config.reuse());
    }
    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        // Ensure contexts are disposed before their owner Playwright is closed.
        try {
            GuiSessionManager.closeScope(this);
        } finally {
            try { if (host != null) host.close(); }
            finally { metrics.printSummary(id, config.reuse()); }
        }
    }
}
