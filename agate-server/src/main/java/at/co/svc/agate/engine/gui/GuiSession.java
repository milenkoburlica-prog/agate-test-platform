package at.co.svc.agate.engine.gui;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import com.microsoft.playwright.*;
import at.co.svc.agate.core.interfaces.TestLogger;

/** All Playwright calls, including disposal, run on one owned worker thread. */
public final class GuiSession implements AutoCloseable {
    private final GuiBrowserHost host;
    private final GuiSuiteConfig settings;
    private final GuiLifecycleMetrics metrics;
    private final String contextId = UUID.randomUUID().toString().substring(0, 8);
    private final ExecutorService worker;
    public GuiSession() { this(null, GuiSuiteConfig.defaults()); }
    GuiSession(GuiBrowserHost host, GuiSuiteConfig settings) {
        this(host, settings, host == null ? null : host.metrics());
    }
    GuiSession(GuiBrowserHost host, GuiSuiteConfig settings, GuiLifecycleMetrics metrics) {
        this.host = host;
        this.settings = settings;
        this.metrics = metrics;
        this.worker = host == null ? Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "agate-gui"); t.setDaemon(true); return t;
        }) : null;
    }
    private Playwright playwright;
    private Browser browser;
    private String browserLabel = "Chromium";
    private BrowserContext context;
    private Page page;
    private final Map<String,Page> namedTabs = new LinkedHashMap<>();
    public Page openTab(String name, String url, int timeout, TestLogger logger) throws Exception {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("OPEN_TAB requires tab");
        Page existing=namedTabs.get(name);
        if(existing!=null&&!existing.isClosed())throw new IllegalArgumentException("Tab already exists: "+name);
        Page initial=page(logger);
        Page created=namedTabs.isEmpty()&&context.pages().size()==1&&initial.url().equals("about:blank")?initial:context.newPage();
        namedTabs.put(name,created);page=created;
        created.setDefaultTimeout(timeout);created.setDefaultNavigationTimeout(timeout);created.navigate(url);
        return created;
    }
    /** caption matches the actual tab title exactly; page remains descriptive. */
    public Page selectCaption(String caption, int timeout) {
        if (caption == null || caption.isBlank()) throw new IllegalArgumentException("caption must not be blank");
        if (context == null) throw new IllegalStateException("No browser open for caption: " + caption);
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
        do {
            List<Page> matches = context.pages().stream().filter(p -> !p.isClosed() && caption.equals(p.title())).toList();
            if (matches.size() > 1) throw new IllegalStateException("Ambiguous tab caption: " + caption);
            if (matches.size() == 1) { page = matches.get(0); return page; }
            try { Thread.sleep(100); } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); throw new IllegalStateException("Caption lookup interrupted", e);
            }
        } while (System.nanoTime() < deadline);
        throw new IllegalStateException("No tab with caption: " + caption);
    }

    public List<GuiTabResult.TabInfo> findTabs(String name,String urlGlob,String titleGlob) {
        List<GuiTabResult.TabInfo> result=new ArrayList<>();
        for(var entry:namedTabs.entrySet()) {
            Page tab=entry.getValue();if(tab.isClosed())continue;
            if(name!=null&&!name.equals(entry.getKey()))continue;
            String url=tab.url(),title=tab.title();
            if(urlGlob!=null&&!GuiBrowserActions.matches(urlGlob,url))continue;
            if(titleGlob!=null&&!GuiBrowserActions.matches(titleGlob,title))continue;
            result.add(new GuiTabResult.TabInfo(entry.getKey(),url,title));
        }
        return result;
    }
    public boolean closeTab(String name) {
        if(name==null||name.isBlank())throw new IllegalArgumentException("CLOSE_TAB requires tab");
        Page target=namedTabs.remove(name);if(target==null||target.isClosed())return false;
        target.close();
        if(page==target)page=context.pages().stream().filter(p->!p.isClosed()).findFirst().orElse(null);
        return true;
    }

    private Path directory;
    private boolean tracing;
    private boolean closed;
    private final Set<String> secrets = ConcurrentHashMap.newKeySet();
    public void rememberSecret(String value) { if (value != null && !value.isEmpty()) secrets.add(value); }
    public String redact(String text) {
        return GuiSecretRedactor.redact(text, secrets);
    }
    public String browserDescription() {
        return browserLabel + " " + browser.version();
    }
    public <T> T call(Callable<T> action) throws Exception {
        Future<T> future;
        synchronized (this) {
            if (closed) throw new IllegalStateException("GUI session is closed");
            if (host != null) return host.call(action);
            future = worker.submit(action);
        }
        try { return future.get(); }
        catch (ExecutionException e) {
            if (e.getCause() instanceof Exception ex) throw ex;
            if (e.getCause() instanceof Error error) throw error;
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw e; }
    }
    public Page page(TestLogger logger) throws Exception {
        if (page != null && !page.isClosed()) return page;
        if (context != null) { page=context.newPage();page.onDialog(Dialog::dismiss);return page; }
        try {
            directory = Path.of(System.getProperty("agate.gui.artifacts", "output/gui"))
                    .resolve(UUID.randomUUID().toString()).toAbsolutePath();
            Files.createDirectories(directory);
            if (host != null) {
                browser = host.browser();
                browserLabel = host.label();
            } else {
                long driverStart = System.nanoTime();
                playwright = Playwright.create();
                if (metrics != null) metrics.add("PLAYWRIGHT_CREATE", GuiLifecycleMetrics.elapsed(driverStart));
                BrowserType.LaunchOptions launch = new BrowserType.LaunchOptions().setHeadless(settings.headless());
                browserLabel = GuiBrowserChoice.configure(launch, settings.browser(), settings.executable());
                if (settings.args() != null && !settings.args().isBlank())
                    launch.setArgs(Arrays.asList(settings.args().split("\\s+")));
                long launchStart = System.nanoTime();
                browser = playwright.chromium().launch(launch);
                if (metrics != null) metrics.add("BROWSER_LAUNCH", GuiLifecycleMetrics.elapsed(launchStart));
                GuiLifecycleLog.detail(metrics, "[GUI lifecycle] BROWSER_LAUNCH id=dedicated-" + contextId
                        + " durationMs=" + GuiLifecycleMetrics.elapsed(launchStart));
            }
            if (logger != null) logger.info("[GUI browser] " + browserDescription());
            long contextStart = System.nanoTime();
            context = browser.newContext(new Browser.NewContextOptions().setAcceptDownloads(true)
                    .setIgnoreHTTPSErrors(settings.ignoreHttpsErrors()));
            if (metrics != null) metrics.add("CONTEXT_CREATE", GuiLifecycleMetrics.elapsed(contextStart));
            GuiLifecycleLog.detail(metrics, "[GUI lifecycle] CONTEXT_CREATE id=" + contextId
                    + " durationMs=" + GuiLifecycleMetrics.elapsed(contextStart));
            if (settings.allowLocalNetwork()) {
                context.grantPermissions(List.of("local-network-access"));
            }
            if (settings.trace()) {
                context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));
                tracing = true;
            }
            page = context.newPage();
            // JS dialogs are distinct from Chromium permission prompts.
            page.onDialog(Dialog::dismiss);
            if (logger != null && Boolean.getBoolean("agate.gui.networkLogs")) {
                page.onResponse(response -> logger.info("[GUI network] " + response.status() + " "
                        + response.request().method() + " " + safeUrl(response.url())));
            }
            return page;
        } catch (Exception e) { dispose(); throw e; }
    }
    public Path directory() { return directory; }
    public Path failureScreenshot(int index) {
        if (page == null || page.isClosed()) return null;
        try {
            Path path = directory.resolve(String.format("step-%03d-failed.png", index));
            page.screenshot(new Page.ScreenshotOptions().setPath(path).setTimeout(5000));
            return path;
        } catch (Exception ignored) { return null; }
    }
    public String currentUrl() { return page == null ? "about:blank" : safeUrl(page.url()); }
    public static String safeUrl(String url) {
        if (url == null) return "";
        return url.replaceAll("(?<=//)[^/@]+@", "").replaceAll(";[^/?#]*", "").replaceAll("[?#].*$", "");
    }
    private void dispose() {
        Throwable failure = null;
        if (tracing && context != null) {
            try { context.tracing().stop(new Tracing.StopOptions().setPath(directory.resolve("trace.zip"))); }
            catch (RuntimeException ex) {
                System.err.println("[GUI lifecycle] TRACE_CLOSE_ERROR context=" + contextId + " cause=" + ex);
                // Trace is diagnostic evidence, not a failure of browser isolation.
            } finally { tracing = false; }
        }
        if (context != null) {
            long started = System.nanoTime();
            GuiLifecycleLog.detail(metrics, "[GUI lifecycle] CONTEXT_CLOSE_BEGIN id=" + contextId);
            try {
                context.close();
                GuiLifecycleLog.detail(metrics, "[GUI lifecycle] CONTEXT_CLOSE id=" + contextId
                        + " durationMs=" + GuiLifecycleMetrics.elapsed(started));
            } catch (RuntimeException | Error ex) {
                if (metrics != null) metrics.failure();
                failure = ex;
                System.err.println("[GUI lifecycle] CONTEXT_CLOSE_ERROR id=" + contextId
                        + " durationMs=" + GuiLifecycleMetrics.elapsed(started) + " cause=" + ex);
            } finally {
                if (metrics != null) metrics.add("CONTEXT_CLOSE", GuiLifecycleMetrics.elapsed(started));
                context = null;
                page = null;
                namedTabs.clear();
            }
        } else { page = null; namedTabs.clear(); }
        if (host == null && playwright != null) {
            // Dedicated (reuse=false): close the launch-owned browser through
            // its Playwright owner, not Browser.close(), which blocked on Edge.
            // The BrowserContext has already been closed above.
            try {
                GuiPlaywrightShutdown.closeLaunchOwnedBrowser(
                        playwright, "dedicated-" + contextId, metrics);
            } catch (RuntimeException | Error ex) {
                if (failure == null) failure = ex;
                else failure.addSuppressed(ex);
            } finally {
                playwright = null;
                browser = null;
            }
        } else {
            // In reuse mode the GuiBrowserHost remains owned by the suite;
            // disposing a test must only release its BrowserContext.
            browser = null;
        }
        if (failure != null) {
            if (failure instanceof Error error) throw error;
            throw new IllegalStateException("GUI context cleanup failed: " + contextId, failure);
        }
    }
    @Override public void close() {
        Future<?> future;
        synchronized (this) {
            if (closed) return;
            closed = true;
            if (host != null) {
                try { host.call(() -> { dispose(); return null; }); }
                catch (Exception ex) { throw new IllegalStateException("Failed to release GUI context", ex); }
                return;
            }
            future = worker.submit(this::dispose);
            worker.shutdown();
        }
        try { future.get(40, TimeUnit.SECONDS); }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            worker.shutdownNow();
            if (metrics != null) metrics.failure();
            throw new IllegalStateException("GUI cleanup interrupted", e);
        }
        catch (ExecutionException e) {
            worker.shutdownNow();
            throw new IllegalStateException("GUI cleanup failed", e.getCause());
        }
        catch (TimeoutException e) {
            worker.shutdownNow();
            if (metrics != null) metrics.failure();
            System.err.println("[GUI lifecycle] CLEANUP_FAILED id=" + contextId
                    + " phase=SESSION_CLOSE elapsedMs>=40000");
            throw new IllegalStateException("GUI cleanup timed out after 40 seconds", e);
        }
    }

}
