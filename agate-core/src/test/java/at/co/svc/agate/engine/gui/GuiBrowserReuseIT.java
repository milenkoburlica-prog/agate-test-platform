package at.co.svc.agate.engine.gui;

import static org.junit.jupiter.api.Assertions.*;
import com.microsoft.playwright.Browser;
import org.junit.jupiter.api.Test;

/** Optional integration test: requires Playwright Chromium to be installed. */
class GuiBrowserReuseIT {
    @Test void reusesBrowserButNotCookies() throws Exception {
        GuiSuiteConfig settings = new GuiSuiteConfig("chromium", true, true, false, false, false, null, null);
        try (GuiBrowserHost host = new GuiBrowserHost(settings)) {
            Browser first = host.call(host::browser);
            GuiSession a = new GuiSession(host, settings);
            try {
                a.call(() -> {
                    a.page(null).context().addCookies(java.util.List.of(
                        new com.microsoft.playwright.options.Cookie("agate-test", "first").setUrl("https://example.com/")));
                    return null;
                });
            } finally { a.close(); }
            GuiSession b = new GuiSession(host, settings);
            try {
                assertSame(first, host.call(host::browser));
                assertEquals(0, b.call(() -> {
                    return b.page(null).context().cookies().size();
                }));
            } finally { b.close(); }
        }
    }

    @Test void bindingsAreScopedToTheSuiteAndContextsAreIsolated() throws Exception {
        java.nio.file.Path yaml = java.nio.file.Files.createTempFile("agate-gui-reuse-", ".yaml");
        try {
            java.nio.file.Files.writeString(yaml, "gui:\n  browser: chromium\n  headless: true\n  reuse: true\n  sessionMode: ISOLATED\n");
            try (GuiRunScope scope = GuiRunScope.fromYaml(yaml.toString())) {
                at.co.svc.agate.core.dsl.runtime.ExecutionContext firstContext =
                    new at.co.svc.agate.core.dsl.runtime.ExecutionContext(msg -> {});
                at.co.svc.agate.core.dsl.runtime.ExecutionContext secondContext =
                    new at.co.svc.agate.core.dsl.runtime.ExecutionContext(msg -> {});
                GuiSessionManager.bind(firstContext, scope);
                GuiSession first = GuiSessionManager.get(firstContext, "DEFAULT");
                Browser one = first.call(() -> first.page(null).context().browser());
                first.call(() -> {
                    first.page(null).context().addCookies(java.util.List.of(
                        new com.microsoft.playwright.options.Cookie("agate-test", "first")
                            .setUrl("https://example.com/")));
                    return null;
                });
                GuiSessionManager.close(firstContext, null);
                GuiSessionManager.unbind(firstContext);
                GuiSessionManager.bind(secondContext, scope);
                GuiSession second = GuiSessionManager.get(secondContext, "DEFAULT");
                Browser two = second.call(() -> second.page(null).context().browser());
                assertSame(one, two, "The second test must reuse the original browser");
                int cookieCount = second.call(() -> second.page(null).context().cookies().size());
                assertEquals(0, cookieCount,
                    "The second test must not receive the first test's cookies");
                GuiSessionManager.close(secondContext, null);
                GuiSessionManager.unbind(secondContext);
            }
        } finally {
            java.nio.file.Files.deleteIfExists(yaml);
        }
    }
}
