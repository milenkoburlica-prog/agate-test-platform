package at.co.svc.agate.engine.gui;

import com.microsoft.playwright.Playwright;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bounded shutdown for the exact launch-owned Playwright driver AND its
 * browser subprocesses. Java/Windows does not automatically kill children
 * when the driver process is forcibly terminated.
 *
 * This is intentionally restricted to the Process read from the exact
 * com.microsoft.playwright.impl.PlaywrightImpl instance; it never enumerates
 * all Edge processes and never touches the user's unrelated browser.
 */
final class GuiPlaywrightShutdown {
    private GuiPlaywrightShutdown() {}

    static void closeLaunchOwnedBrowser(Playwright playwright, String ownerId,
                                        GuiLifecycleMetrics metrics) {
        if (playwright == null) return;
        GuiLifecycleLog.detail(metrics, "[GUI lifecycle] BROWSER_CLOSE_VIA_PLAYWRIGHT owner=" + ownerId);
        close(playwright, ownerId, metrics);
    }

    static void close(Playwright playwright, String ownerId, GuiLifecycleMetrics metrics) {
        if (playwright == null) return;
        final boolean fallbackEnabled = Boolean.parseBoolean(
                System.getProperty("agate.gui.driverForceKill", "true"));
        final long graceMs = graceMillis();
        final Process driver = ownDriverProcess(playwright);
        final List<GuiOwnedProcessTree.Owned> captured = driver == null
                ? List.of() : GuiOwnedProcessTree.capture(driver);
        final AtomicBoolean completed = new AtomicBoolean();
        final AtomicBoolean stopStarted = new AtomicBoolean();
        final AtomicBoolean forcedDriver = new AtomicBoolean();
        Thread watchdog = null;

        if (driver != null && fallbackEnabled) {
            watchdog = new Thread(() -> {
                try {
                    Thread.sleep(graceMs);
                    if (completed.get()) return;
                    stopStarted.set(true);
                    if (completed.get()) return;
                    // Capture immediately before stopping the driver, while
                    // Windows can still walk its Edge/Chromium descendants.
                    List<GuiOwnedProcessTree.Owned> owned = GuiOwnedProcessTree.merge(
                            captured, GuiOwnedProcessTree.capture(driver));
                    GuiOwnedProcessTree.Result result =
                            GuiOwnedProcessTree.stopDescendants(owned, ownerId, 800,
                                    metrics != null && metrics.verbose());
                    if (metrics != null) metrics.forcedChildStops(result.signalled());
                    if (driver.isAlive()) {
                        GuiLifecycleLog.detail(metrics, "[GUI lifecycle] DRIVER_FORCE_STOP owner=" + ownerId
                                + " pid=" + driver.pid() + " graceMs=" + graceMs);
                        forcedDriver.set(true);
                        if (metrics != null) metrics.forcedDriverStop();
                        driver.destroy();
                        if (!driver.waitFor(400, TimeUnit.MILLISECONDS) && driver.isAlive()) {
                            driver.destroyForcibly();
                        }
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException ex) {
                    System.err.println("[GUI lifecycle] OWNED_PROCESS_SHUTDOWN_ERROR owner="
                            + ownerId + " cause=" + ex);
                }
            }, "agate-gui-owned-process-watchdog");
            watchdog.setDaemon(true);
            watchdog.start();
        } else if (driver == null && fallbackEnabled) {
            System.err.println("[GUI lifecycle] DRIVER_PID_UNAVAILABLE owner=" + ownerId
                    + " (automatic process-tree fallback is unavailable)");
        }

        long started = System.nanoTime();
        Throwable primaryFailure = null;
        GuiLifecycleLog.detail(metrics, "[GUI lifecycle] PLAYWRIGHT_CLOSE_BEGIN owner=" + ownerId
                + " capturedChildren=" + captured.size());
        try {
            playwright.close();
            GuiLifecycleLog.detail(metrics, "[GUI lifecycle] PLAYWRIGHT_CLOSE_END owner=" + ownerId
                    + " durationMs=" + GuiLifecycleMetrics.elapsed(started)
                    + " forcedDriver=" + forcedDriver.get());
        } catch (RuntimeException | Error ex) {
            primaryFailure = ex;
            if (metrics != null) metrics.failure();
            System.err.println("[GUI lifecycle] PLAYWRIGHT_CLOSE_ERROR owner=" + ownerId
                    + " durationMs=" + GuiLifecycleMetrics.elapsed(started)
                    + " cause=" + ex);
            throw ex;
        } finally {
            completed.set(true);
            if (watchdog != null) {
                // If fallback has started, let the worker finish killing the
                // already-captured children before checking process survival.
                if (!stopStarted.get()) watchdog.interrupt();
                try { watchdog.join(2500); }
                catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            }
            if (metrics != null) metrics.add("PLAYWRIGHT_CLOSE", GuiLifecycleMetrics.elapsed(started));

            if (driver != null && fallbackEnabled) {
                // Even if Playwright.close() returned normally, an orphaned
                // browser is still a leak. The snapshot survives parent death.
                List<GuiOwnedProcessTree.Owned> owned = GuiOwnedProcessTree.merge(
                        captured, GuiOwnedProcessTree.capture(driver));
                if (!GuiOwnedProcessTree.alivePids(owned).isEmpty()) {
                    GuiOwnedProcessTree.Result last =
                            GuiOwnedProcessTree.stopDescendants(owned, ownerId, 1000,
                                    metrics != null && metrics.verbose());
                    if (metrics != null) metrics.forcedChildStops(last.signalled());
                }
                // A successful close() return is not proof the SDK driver
                // exited: the SDK can log a timeout and return normally.
                if (driver.isAlive()) {
                    GuiLifecycleLog.detail(metrics, "[GUI lifecycle] DRIVER_LATE_FORCE_STOP owner="
                            + ownerId + " pid=" + driver.pid());
                    if (metrics != null) metrics.forcedDriverStop();
                    driver.destroyForcibly();
                    try { driver.waitFor(500, TimeUnit.MILLISECONDS); }
                    catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                }
                List<Long> survivors = GuiOwnedProcessTree.alivePids(owned);
                boolean driverAlive = driver.isAlive();
                GuiLifecycleLog.detail(metrics, "[GUI lifecycle] OWNED_PROCESS_VERIFY owner=" + ownerId
                        + " driverAlive=" + driverAlive
                        + " childrenAlive=" + survivors.size()
                        + " survivors=" + survivors);
                if (driverAlive || !survivors.isEmpty()) {
                    if (metrics != null) {
                        metrics.childProcessLeaks(survivors.size());
                        metrics.failure();
                    }
                    IllegalStateException leak = new IllegalStateException(
                            "AGATE-owned processes survived cleanup, driverAlive="
                            + driverAlive + " childPids=" + survivors);
                    if (primaryFailure != null) primaryFailure.addSuppressed(leak);
                    else throw leak;
                }
            }
        }
    }

    private static long graceMillis() {
        String value = System.getProperty("agate.gui.driverShutdownGraceMs", "3000");
        try { return Math.max(1000, Math.min(20000, Long.parseLong(value))); }
        catch (NumberFormatException ex) { return 3000; }
    }

    private static Process ownDriverProcess(Playwright playwright) {
        if (!"com.microsoft.playwright.impl.PlaywrightImpl".equals(
                playwright.getClass().getName())) return null;
        try {
            Field field = playwright.getClass().getDeclaredField("driverProcess");
            field.setAccessible(true);
            Object result = field.get(playwright);
            return result instanceof Process process ? process : null;
        } catch (ReflectiveOperationException | RuntimeException ex) { return null; }
    }
}
