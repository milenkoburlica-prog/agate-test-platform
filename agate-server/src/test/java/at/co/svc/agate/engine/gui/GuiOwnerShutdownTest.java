package at.co.svc.agate.engine.gui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** No native browser is launched: verifies cleanup routing and thread ownership. */
class GuiOwnerShutdownTest {
    private static <T> T proxy(Class<T> type, AtomicInteger closes) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (ignored, method, args) -> {
                    if (method.getName().equals("close")) {
                        closes.incrementAndGet();
                        return null;
                    }
                    if (method.getName().equals("toString")) return "fake-" + type.getSimpleName();
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class) return 0;
                    return null;
                }));
    }

    private static void inject(Object object, String name, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test void reusedBrowserClosesOnlyViaPlaywrightOwner() throws Exception {
        AtomicInteger browserCloses = new AtomicInteger();
        AtomicInteger playwrightCloses = new AtomicInteger();
        GuiSuiteConfig config = new GuiSuiteConfig("chromium", true, true, false,
                false, false, null, null);
        GuiBrowserHost host = new GuiBrowserHost(config);
        inject(host, "browser", proxy(Browser.class, browserCloses));
        inject(host, "playwright", proxy(Playwright.class, playwrightCloses));
        inject(host, "browserInstanceId", "unit-test");
        host.close();
        assertEquals(0, browserCloses.get(), "Browser.close() must not block the owner");
        assertEquals(1, playwrightCloses.get(), "Owner must close Playwright exactly once");
        host.close();
        assertEquals(1, playwrightCloses.get(), "Close must be idempotent");
    }

    @Test void dedicatedBrowserClosesOnlyViaPlaywrightOwner() throws Exception {
        AtomicInteger browserCloses = new AtomicInteger();
        AtomicInteger playwrightCloses = new AtomicInteger();
        GuiSuiteConfig config = new GuiSuiteConfig("chromium", true, false, false,
                false, false, null, null);
        GuiSession session = new GuiSession(null, config);
        inject(session, "browser", proxy(Browser.class, browserCloses));
        inject(session, "playwright", proxy(Playwright.class, playwrightCloses));
        session.close();
        assertEquals(0, browserCloses.get());
        assertEquals(1, playwrightCloses.get());
        session.close();
        assertEquals(1, playwrightCloses.get());
    }
}
