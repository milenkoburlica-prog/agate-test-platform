package at.co.svc.agate.engine.gui;

/** Optional diagnostics. No changes to the browser lifecycle itself. */
final class GuiLifecycleLog {
    private GuiLifecycleLog() {}

    static void detail(GuiLifecycleMetrics metrics, String message) {
        if (metrics == null ? Boolean.getBoolean("agate.gui.lifecycleLog") : metrics.verbose()) {
            System.out.println(message);
        }
    }

    static void detail(boolean enabled, String message) {
        if (enabled) System.out.println(message);
    }

    static void warning(String message) {
        System.err.println(message);
    }
}
