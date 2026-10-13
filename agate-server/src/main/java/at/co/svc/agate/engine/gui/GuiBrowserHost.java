package at.co.svc.agate.engine.gui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** A single Playwright owner thread and browser per suite, never shared across suites. */
final class GuiBrowserHost implements AutoCloseable {
    private final GuiSuiteConfig config;
    private final GuiLifecycleMetrics metrics;
    private final ExecutorService worker;
    private Playwright playwright;
    private Browser browser;
    private String label;
    private String browserInstanceId;
    private boolean closed;

    GuiBrowserHost(GuiSuiteConfig config) { this(config, new GuiLifecycleMetrics(config.lifecycleLog())); }
    GuiBrowserHost(GuiSuiteConfig config, GuiLifecycleMetrics metrics) {
        this.config = config;
        this.metrics = metrics;
        this.worker = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "agate-suite-browser");
            thread.setDaemon(true);
            return thread;
        });
    }
    GuiSuiteConfig config() { return config; }
    GuiLifecycleMetrics metrics() { return metrics; }
    <T> T call(Callable<T> action) throws Exception {
        Future<T> result;
        synchronized (this) {
            if (closed) throw new IllegalStateException("AGATE GUI suite has ended");
            result = worker.submit(action);
        }
        try { return result.get(); }
        catch (ExecutionException ex) {
            if (ex.getCause() instanceof Exception e) throw e;
            if (ex.getCause() instanceof Error e) throw e;
            throw new IllegalStateException(ex.getCause());
        } catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw ex; }
    }
    /** Called on this host's single Playwright owner worker. */
    Browser browser() {
        if (browser != null && browser.isConnected()) {
            GuiLifecycleLog.detail(metrics, "[GUI lifecycle] BROWSER_REUSE id=" + browserInstanceId);
            return browser;
        }
        if (playwright != null) {
            GuiPlaywrightShutdown.closeLaunchOwnedBrowser(playwright, browserInstanceId, metrics);
            playwright = null;
        }
        long started = System.nanoTime();
        playwright = Playwright.create();
        metrics.add("PLAYWRIGHT_CREATE", GuiLifecycleMetrics.elapsed(started));
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(config.headless());
        label = GuiBrowserChoice.configure(options, config.browser(), config.executable());
        if (config.args() != null && !config.args().isBlank()) {
            options.setArgs(Arrays.asList(config.args().split("\\s+")));
        }
        try {
            started = System.nanoTime();
            browser = playwright.chromium().launch(options);
            metrics.add("BROWSER_LAUNCH", GuiLifecycleMetrics.elapsed(started));
            browserInstanceId = UUID.randomUUID().toString().substring(0, 8);
            GuiLifecycleLog.detail(metrics, "[GUI lifecycle] BROWSER_LAUNCH id=" + browserInstanceId
                    + " durationMs=" + GuiLifecycleMetrics.elapsed(started));
            return browser;
        } catch (RuntimeException ex) {
            try { GuiPlaywrightShutdown.closeLaunchOwnedBrowser(playwright, browserInstanceId, metrics); }
            catch (RuntimeException cleanup) { ex.addSuppressed(cleanup); }
            playwright = null;
            throw ex;
        }
    }
    String label() { return label == null ? config.browser() : label; }
    @Override public void close() {
        Future<?> finished;
        synchronized (this) {
            if (closed) return;
            closed = true;
            finished = worker.submit(this::disposeOnOwnerThread);
            worker.shutdown();
        }
        // The SDK has a separate 30-second internal driver wait; our exact-PID
        // watchdog normally terminates a stuck driver earlier. Never silently
        // claim cleanup success if the owner thread is still blocked.
        try { finished.get(40, TimeUnit.SECONDS); }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            worker.shutdownNow();
            metrics.failure();
            throw new IllegalStateException("GUI browser cleanup interrupted", ex);
        }
        catch (ExecutionException ex) {
            worker.shutdownNow();
            throw new IllegalStateException("GUI browser cleanup failed", ex.getCause());
        }
        catch (TimeoutException ex) {
            worker.shutdownNow();
            metrics.failure();
            System.err.println("[GUI lifecycle] CLEANUP_FAILED id=" + browserInstanceId
                    + " phase=HOST_CLOSE elapsedMs>=40000");
            throw new IllegalStateException("GUI browser cleanup exceeded 40 seconds", ex);
        }
    }
    private void disposeOnOwnerThread() {
        // All GuiSession instances close their own BrowserContexts first.
        // Browser.close() is deliberately NOT called here: on Windows Edge
        // it was hanging for 40s even though all contexts were already closed.
        // Playwright.close() is documented to close its own launched browsers.
        try {
            if (playwright != null) {
                GuiPlaywrightShutdown.closeLaunchOwnedBrowser(
                        playwright, browserInstanceId, metrics);
            }
        } finally {
            browser = null;
            playwright = null;
        }
    }
}
