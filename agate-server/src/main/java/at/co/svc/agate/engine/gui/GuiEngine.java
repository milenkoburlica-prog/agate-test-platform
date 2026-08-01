package at.co.svc.agate.engine.gui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitForSelectorState;

import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.register.PrintDslStepContext;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.interfaces.TestLogger;

/**
 * Unified GUI Engine based on Playwright. Handles CALL (actions), ASSERT (UI
 * validation), and BUFFER (data extraction).
 */
public class GuiEngine extends AbstractStepEngine {

    private static final Map<String, GuiSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public boolean canExecute(StepType stepType) {
        return stepType == StepType.GUI;
    }

    @Override
    public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile, int stepIndex,
            Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {

        if (isVerbose) {
            PrintDslStepContext.logDslStepContext(logger, step);
        }

        String op = (step.getOp() != null) ? step.getOp().toUpperCase() : "EXEC";
        String sessionId = tc.getVariables().getOrDefault("SESSION_ID", "DEFAULT").toString();

        GuiSession session = getOrCreateSession(sessionId);
        Page page = session.getPage();

        try {
            switch (op) {
            case "ASSERT":
                handleAssertion(step, page, stepIndex, printExecution, logger, isVerbose);
                break;
            case "BUFFER":
                handleBuffer(tc, step, page, stepIndex, printExecution, logger, isVerbose);
                break;
            case "EXEC":
            default:
                handleCall(sessionId, step, page, stepIndex, printExecution, logger, isVerbose);
                break;
            }
        } catch (Exception e) {
            if (Boolean.TRUE.equals(printExecution)) {
                logger.info(String.format("      FAIL            : %s", ConsoleColors.RED, ConsoleColors.RESET,
                        e.getMessage()));
            }
            throw new RuntimeException("GUI Step Failed: " + e.getMessage());
        }
    }

    private void handleCall(String sessionId, TestStep step, Page page, int stepIndex, Boolean printExecution,
            TestLogger logger, boolean isVerbose) {

        String action = step.getAction().toUpperCase();
        String selector = step.getSelector();
        String value = step.getValue();

        if (Boolean.TRUE.equals(printExecution)) {
            if (action.equalsIgnoreCase("OPEN")) {
                logger.info("...");
                logger.info(String.format("[%03d] %s%-15s%s → %s (%s)", stepIndex, ConsoleColors.YELLOW, "GUI EXEC",
                        ConsoleColors.RESET, action, value != null ? value : ""));
            } else {
                logger.info("...");
                logger.info(String.format("[%03d] %s%-15s%s → %s (%s)", stepIndex, ConsoleColors.YELLOW, "GUI EXEC",
                        ConsoleColors.RESET, action, selector != null ? selector : ""));
            }
        }

        switch (action) {

        case "OPEN":
            page.navigate(value.trim());
            break;

        case "INPUT":
            if (selector == null || value == null) {
                throw new RuntimeException("INPUT requires selector and value");
            }
            page.fill(selector.trim(), value);
            break;

        case "CLICK":
            if (selector == null) {
                throw new RuntimeException("CLICK requires selector");
            }

            String safeSelector = normalizeSelector(selector);

            page.waitForSelector(safeSelector,
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));

            page.locator(safeSelector).first().click();

            break;
        case "CLOSE":
            cleanup(sessionId, logger);
            break;

        default:
            throw new RuntimeException("Unsupported GUI action: " + action);
        }
    }

    private String normalizeSelector(String selector) {
        if (selector == null)
            return null;

        String s = selector.trim();

        if (s.endsWith("]") && !s.contains("[")) {
            s = s.substring(0, s.length() - 1);
        }

        if (s.startsWith("data-test-id=")) {
            s = "[" + s + "]";
        }

        if (s.startsWith("name=")) {
            s = "[" + s + "]";
        }

        return s;
    }

    private void handleAssertion(TestStep step, Page page, int stepIndex, Boolean printExecution, TestLogger logger, boolean isVerbose) {

        String action = step.getAction().toUpperCase();
        String selector = step.getSelector();
        String expected = step.getExpected();

        String safeSelector = normalizeSelector(selector);

        boolean passed = false;
        String detail = action;

        switch (action) {

        case "VISIBLE":
            page.waitForSelector(safeSelector,
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(35000));
            passed = true;
            break;

        case "TEXT":
            String actual = page.innerText(safeSelector);
            passed = actual.equals(expected);
            detail = String.format("Text check on '%s' (Expected: '%s', Actual: '%s')", safeSelector, expected, actual);
            break;

        default:
            throw new RuntimeException("Unsupported ASSERT action: " + action);
        }

        if (Boolean.TRUE.equals(printExecution)) {
            logger.info("...");
            String color = passed ? ConsoleColors.GREEN : ConsoleColors.RED;
            logger.info(String.format("[%03d] %s%-15s%s → %s %s", stepIndex, ConsoleColors.YELLOW, "GUI ASSERT",
                    ConsoleColors.RESET, detail, color + (passed ? "✔" : "✖") + ConsoleColors.RESET));
        }

        if (!passed) {
            throw new RuntimeException("GUI Assertion failed: " + detail);
        }
    }

    private void handleBuffer(TestCase tc, TestStep step, Page page, int stepIndex, Boolean printExecution,
            TestLogger logger, boolean isVerbose) {
        String selector = step.getAction();
        String varName = step.getResponse();

        String value = page.innerText(selector.trim());
        tc.addVariable(varName, value);

        if (Boolean.TRUE.equals(printExecution)) {
            logger.info("...");
            logger.info(String.format("[%03d] %s%-15s%s → %s", stepIndex, ConsoleColors.YELLOW, "GUI BUFFER",
                    ConsoleColors.RESET, varName));
            logger.info(String.format("      <<< %s%s", ConsoleColors.CYAN, value, ConsoleColors.RESET));
        }
    }

    private GuiSession getOrCreateSession(String sessionId) {
        return SESSIONS.computeIfAbsent(sessionId, id -> {
            GuiSession guiSession = new GuiSession();
            guiSession.setPlaywright(Playwright.create());
            guiSession.setBrowser(guiSession.getPlaywright()
                                        .chromium()
                                        .launch(new BrowserType.LaunchOptions()
                                        .setHeadless(false)
                                        .setArgs(java.util.List.of("--start-maximized"))
                                        .setSlowMo(100)));

            BrowserContext context = guiSession.getBrowser()
                    .newContext(new Browser.NewContextOptions().setViewportSize(null));
            guiSession.setPage(context.newPage());
            guiSession.getPage().setDefaultTimeout(10000);
            return guiSession;
        });
    }

    public static void cleanup(String sessionId, TestLogger logger) {
        GuiSession session = SESSIONS.remove(sessionId);
        if (session != null) {
            try {
                if (session.getPage() != null) {
                    session.getPage().close();
                }

                if (session.getBrowser() != null) {
                    session.getBrowser().close();
                }

                Thread.sleep(1000);

                if (session.getPlaywright() != null) {
                    session.getPlaywright().close();
                }

                if (logger != null)
                    logger.info("      [INFO] Playwright session '" + sessionId + "' cleaned up.");
            } catch (Exception e) {
                if (logger != null)
                    logger.info("      [DEBUG] Quiet cleanup: " + e.getMessage());
            }
        }
    }

    public static void cleanupAll(TestLogger logger) {
        SESSIONS.keySet().forEach(id -> cleanup(id, logger));
    }
}