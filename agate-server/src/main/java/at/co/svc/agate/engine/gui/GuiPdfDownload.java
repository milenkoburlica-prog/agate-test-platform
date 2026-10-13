package at.co.svc.agate.engine.gui;

import com.microsoft.playwright.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/** Captures original POST response bytes before Chromium's PDF viewer consumes them. */
final class GuiPdfDownload {
    private GuiPdfDownload() {}
    static Path save(Page origin, Locator button, String filename, String urlGlob, double timeout, boolean ignoreHttpsErrors) {
        Path output = Path.of(filename).toAbsolutePath().normalize();
        BrowserContext context = origin.context();
        Set<Page> before = new HashSet<>(context.pages());
        before.add(origin);
        Set<Page> opened = new LinkedHashSet<>();
        AtomicReference<Path> saved = new AtomicReference<>();
        AtomicReference<RuntimeException> error = new AtomicReference<>();
        Consumer<Page> onPage = opened::add;
        Consumer<Route> handler = route -> {
            Request request = route.request();
            if (!"POST".equals(request.method())) { route.resume(); return; }
            try {
                // Capture the navigation owner before fulfilling the PDF response.
                // The PDF viewer may not expose the expected opener relationship.
                try {
                    Page owner = request.frame().page();
                    if (!before.contains(owner)) opened.add(owner);
                } catch (PlaywrightException unavailableFrame) {
                    // onPage and the context snapshot are independent fallbacks.
                }
                // Fetch the intercepted request once, sharing browser cookies and preserving its POST body.
                APIResponse response = context.request().fetch(request, com.microsoft.playwright.options.RequestOptions.create()
                        .setTimeout(timeout).setIgnoreHTTPSErrors(ignoreHttpsErrors));
                try {
                    byte[] bytes = response.body();
                    if (!response.ok()) throw new IllegalStateException("PDF request returned HTTP " + response.status());
                    GuiPdfFile.write(output, bytes);
                    route.fulfill(new Route.FulfillOptions().setResponse(response));
                    saved.set(output);
                } finally { response.dispose(); }
            } catch (Exception failure) {
                error.set(new IllegalStateException("Could not save PDF: " + failure.getMessage(), failure));
                try { route.abort(); } catch (Exception ignored) {}
            }
        };
        context.onPage(onPage);
        context.route(urlGlob, handler);
        try {
            button.click(new Locator.ClickOptions().setTimeout(timeout));
            context.waitForCondition(() -> saved.get() != null || error.get() != null,
                    new BrowserContext.WaitForConditionOptions().setTimeout(timeout));
            if (error.get() != null) throw error.get();
            return saved.get();
        } finally {
            context.unroute(urlGlob, handler);
            context.offPage(onPage);
            // This isolated browser context executes one GUI action at a time.
            // Include pages whose page event was delivered after the response event.
            for (Page page : context.pages()) {
                if (!before.contains(page)) opened.add(page);
            }
            RuntimeException closeFailure = null;
            for (Page popup : opened) {
                if (before.contains(popup) || popup.isClosed()) continue;
                try {
                    popup.close();
                    if (!popup.isClosed()) {
                        throw new IllegalStateException("PDF tab remained open after close()");
                    }
                } catch (RuntimeException failure) {
                    if (closeFailure == null) {
                        closeFailure = new IllegalStateException(
                                "PDF was saved, but a newly opened tab could not be closed", failure);
                    } else closeFailure.addSuppressed(failure);
                }
            }
            if (!origin.isClosed()) origin.bringToFront();
            if (closeFailure != null) {
                if (error.get() != null) error.get().addSuppressed(closeFailure);
                else if (saved.get() != null) throw closeFailure;
            }
        }
    }
}
