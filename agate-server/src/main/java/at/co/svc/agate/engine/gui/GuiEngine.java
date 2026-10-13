package at.co.svc.agate.engine.gui;

import java.nio.file.*;
import java.util.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.*;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import at.co.svc.agate.core.dsl.model.*;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.interfaces.TestLogger;

/** Studio GUI DSL and legacy EXEC/action steps on the server's execution pipeline. */
public class GuiEngine extends AbstractStepEngine {
    @Override public boolean canExecute(StepType type) { return type == StepType.GUI; }
    @Override public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String yamlFile,
                                   int stepIndex, Boolean printExecution, TestLogger logger, boolean isVerbose) throws Exception {
        String sourceFile = step.getSourceFile() == null || step.getSourceFile().isBlank()
                ? yamlFile : step.getSourceFile();
        GuiStepConfig config = GuiStepConfig.resolve(tc, step, context, yamlFile, stepIndex);
        String sessionName = Objects.toString(tc.getVariables().get("SESSION_ID"), "DEFAULT");
        String id = step.getId() == null ? "step_" + stepIndex : step.getId();
        GuiSession session = GuiSessionManager.get(context, sessionName);
        GuiStepLog log = new GuiStepLog(logger, Boolean.TRUE.equals(printExecution), session, config, step);
        log.begin(stepIndex, id, isVerbose);
        if (config.op().equals("CLOSE")) {
            GuiSessionManager.close(context, sessionName);
            log.field("Result", "Session closed");
            return;
        }
        if (Set.of("OPEN_TAB","FIND_TAB","CLOSE_TAB","BUFFER_TAB_EXISTS").contains(config.op())) {
            session.call(() -> { GuiBrowserActions.execute(tc,context,session,config,Boolean.TRUE.equals(printExecution)?logger:message -> {}); return null; });
            return;
        }
        session.call(() -> {
            Page page = null;
            try {
                page = config.text("caption") == null ? session.page(logger)
                        : session.selectCaption(config.required("caption"), config.timeout());
                page.setDefaultTimeout(config.timeout());
                page.setDefaultNavigationTimeout(config.timeout());
                String before = session.currentUrl();
                if (config.op().equals("OPEN")) log.browser(session.browserDescription());
                execute(tc, context, session, page, config, log);
                log.navigation(before, session.currentUrl());
                log.success();
            } catch (Exception | AssertionError failure) {
                Path screenshot = session.failureScreenshot(stepIndex);
                String reason = log.failure(failure);
                String detail = "Error in YAML file: " + sourceFile
                        + "\n      Source step: " + step.getSourceStepIndex()
                        + "\n      GUI step '" + id + "' (" + config.op() + ") failed at " + session.currentUrl()
                        + "\n      Target  : " + session.redact(config.target().toString())
                        + "\n      Cause   : " + reason
                        + "\n      Hint    : " + session.redact(failureHint(page, config))
                        + (screenshot == null ? "" : "\n      Evidence: " + screenshot);
                // TestExecutor prints the failure once; avoid a duplicate ERROR block.
                throw new GuiStepException(detail, failure);
            }
            return null;
        });
        // TestExecutor already prints status and duration once for every step.
    }
    private void execute(TestCase tc, ExecutionContext context, GuiSession session, Page page, GuiStepConfig c, GuiStepLog log) {
        Locator target = c.target().isEmpty() ? null : GuiLocatorResolver.resolve(page, c.target());
        switch (c.op()) {
            case "OPEN" -> page.navigate(c.text("url") != null ? c.required("url") : c.required("value"),
                    new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            case "BACK" -> page.goBack(new Page.GoBackOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            case "CLICK" -> requireTarget(target,c).click();
            case "FILL" -> {
                Locator input = requireTarget(target,c);
                log.prepareInput(input);
                input.fill(c.required("value"));
            }
            case "SELECT" -> {
                select(requireTarget(target,c), c.get("value"), c.timeout());
                log.selectionResult(target);
            }
            case "CHECK" -> requireTarget(target,c).check();
            case "UNCHECK" -> requireTarget(target,c).uncheck();
            case "HOVER" -> requireTarget(target,c).hover();
            case "PRESS" -> requireTarget(target,c).press(c.required("value"));
            case "UPLOAD" -> requireTarget(target,c).setInputFiles(paths(c.get("value")));
            case "DOWNLOAD_PDF" -> {
                Path output = GuiPdfDownload.save(page, requireTarget(target,c), c.required("outputFile"),
                        c.required("responseUrlGlob"), c.timeout(),
                        c.get("ignoreHttpsErrors") == null ? Boolean.getBoolean("agate.gui.ignoreHttpsErrors")
                                : Boolean.parseBoolean(c.get("ignoreHttpsErrors").toString()));
                store(tc, context, Objects.toString(c.get("buffer"),"download"), output.toString());
                log.artifact(output);
            }
            case "DOWNLOAD" -> {
                Download download = page.waitForDownload(new Page.WaitForDownloadOptions().setTimeout(c.timeout()),
                        () -> requireTarget(target,c).click());
                Path output = outputPath(session, c.text("value") != null ? c.text("value") : download.suggestedFilename());
                download.saveAs(output);
                String buffer = c.text("buffer") == null ? "download" : c.text("buffer");
                store(tc,context,buffer,output.toString());log.field("Buffer",buffer);log.artifact(output);
            }
            case "WAIT" -> {
                if (target != null) target.waitFor(new Locator.WaitForOptions().setTimeout(c.timeout()).setState(
                        WaitForSelectorState.valueOf(Objects.toString(c.get("state"),"visible").toUpperCase(Locale.ROOT))));
                else {
                    double milliseconds = c.get("value") == null ? 1000 : Double.parseDouble(c.required("value"));
                    if (milliseconds < 0 || !Double.isFinite(milliseconds)) throw new IllegalArgumentException("WAIT requires nonnegative milliseconds");
                    page.waitForTimeout(milliseconds);
                }
            }
            case "SCREENSHOT" -> {
                Path output = outputPath(session, Objects.toString(c.get("value"),"screenshot.png"));
                page.screenshot(new Page.ScreenshotOptions().setFullPage(true).setPath(output));log.artifact(output);
            }
            case "EXTRACT" -> {
                Locator locator = requireTarget(target,c);
                String value = c.text("attribute") != null ? locator.getAttribute(c.text("attribute"))
                        : "value".equals(c.text("source")) ? locator.inputValue() : locator.innerText();
                if (value == null) throw new IllegalArgumentException("EXTRACT attribute is missing");
                String buffer = c.text("buffer") != null ? c.text("buffer")
                        : c.text("response") != null ? c.text("response") : "extracted";
                store(tc, context, buffer, value);
                log.extracted(buffer,value,locator);
            }
            case "ASSERT" -> {
                Locator locator = requireTarget(target,c);
                log.prepareAssertion(locator);
                assertion(locator,c);log.assertionResult(locator);
            }
            case "ASSERT_PAGE_STRUCTURE" -> {
                Map<String,Object> baseline = GuiStepConfig.map(tc.getGuiPages().get(c.required("target")));
                Object expected = baseline.get("aria");
                if (expected == null) throw new IllegalArgumentException("ASSERT_PAGE_STRUCTURE requires pages.<target>.aria");
                if (!page.locator("body").ariaSnapshot().trim().equals(expected.toString().trim()))
                    throw new AssertionError("ARIA structure differs from baseline");
            }
            default -> throw new IllegalArgumentException("Unsupported GUI operation: " + c.op());
        }
    }
    private static String failureHint(Page page, GuiStepConfig config) {
        Object css=config.target().get("css");
        if(page != null && css instanceof String selector && selector.startsWith("input[value=")) {
            // Read only button labels; never read password/text input values.
            try {
                Object labels=page.locator("input[type=submit],input[type=button],input[type=reset],button")
                        .evaluateAll("els => els.filter(e => e.getClientRects().length).slice(0,20).map(e => e.tagName === 'INPUT' ? e.value : e.innerText)");
                if(labels instanceof List<?> list) {
                    List<String> visible=list.stream().map(v->Objects.toString(v,"").replace(" ","·")).toList();
                    return "CSS value requires identical whitespace. Visible button labels (· = space): "
                            + visible + ". Use button: {text: '<visible name>', exact: true}; button names normalize whitespace.";
                }
            } catch(Exception ignored) { /* Diagnostics must not replace the original failure. */ }
        }
        return "Check the target in the named YAML step against the current page; use the screenshot for context.";
    }
    private static Locator requireTarget(Locator locator, GuiStepConfig c) {
        if (locator == null) throw new IllegalArgumentException(c.op() + " requires target or legacy selector");
        return locator;
    }
    private static Path outputPath(GuiSession session,String name) {
        return session.directory().resolve(Path.of(name).getFileName());
    }
    private static void store(TestCase tc, ExecutionContext context,String key,Object value) {
        if (key.isBlank()) throw new IllegalArgumentException("GUI buffer name is empty");
        context.storeBuffer(key,value); context.setVar(key,value); tc.addVariable(key,value);
    }
    private static Path[] paths(Object value) {
        if (value == null) throw new IllegalArgumentException("UPLOAD requires value");
        List<?> list = value instanceof List ? (List<?>)value : List.of(value);
        return list.stream().map(v -> Path.of(v.toString())).toArray(Path[]::new);
    }
    static void select(Locator locator,Object value) { select(locator,value,30000); }
    static void select(Locator locator,Object value,double timeout) {
        if(value instanceof Map<?,?> m && (m.containsKey("labelPattern")||m.containsKey("labelGlob"))) {
            if(m.size()!=1)throw new IllegalArgumentException("labelPattern/labelGlob must be the only SELECT option key");
            java.util.regex.Pattern pattern=m.containsKey("labelGlob")?globPattern(Objects.requireNonNull(m.get("labelGlob")).toString()):java.util.regex.Pattern.compile(Objects.requireNonNull(m.get("labelPattern")).toString(),java.util.regex.Pattern.DOTALL);
            locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(timeout));
            long deadline=System.nanoTime()+(long)(timeout*1_000_000);
            do {
                @SuppressWarnings("unchecked") List<String> labels=(List<String>)locator.evaluate("el => Array.from(el.options).map(o => o.label)");
                List<Integer> matches=new ArrayList<>();
                for(int i=0;i<labels.size();i++)if(pattern.matcher(labels.get(i)).matches())matches.add(i);
                if(matches.size()>1)throw new IllegalArgumentException("SELECT labelPattern matches multiple options: "+matches.size());
                if(matches.size()==1){locator.selectOption(new SelectOption().setIndex(matches.get(0)),new Locator.SelectOptionOptions().setTimeout(Math.max(1,(deadline-System.nanoTime())/1_000_000.0)));return;}
                locator.page().waitForTimeout(Math.min(100,Math.max(1,(deadline-System.nanoTime())/1_000_000.0)));
            } while(System.nanoTime()<deadline);
            throw new IllegalArgumentException("SELECT labelPattern matched no option within timeout");
        }
        if (value == null) throw new IllegalArgumentException("SELECT requires value");
        if (value instanceof Map<?,?> map) locator.selectOption(option(map));
        else if (value instanceof List<?> list) {
            SelectOption[] options = list.stream().map(v -> v instanceof Map<?,?> m ? option(m)
                    : new SelectOption().setValue(v.toString())).toArray(SelectOption[]::new);
            locator.selectOption(options);
        } else locator.selectOption(value.toString());
    }
    static java.util.regex.Pattern globPattern(String glob) {
        StringBuilder source=new StringBuilder("^");
        for(char c:glob.toCharArray()) {
            if(c=='*')source.append(".*");
            else {if("\\.^$|?+()[]{}".indexOf(c)>=0)source.append('\\');source.append(c);}
        }
        return java.util.regex.Pattern.compile(source.append('$').toString(),java.util.regex.Pattern.DOTALL);
    }
    private static SelectOption option(Map<?,?> map) {
        if (map.size() != 1) throw new IllegalArgumentException("SELECT option requires exactly one of label, value, index");
        SelectOption option = new SelectOption();
        if (map.containsKey("label")) return option.setLabel(Objects.requireNonNull(map.get("label")).toString());
        if (map.containsKey("value")) return option.setValue(Objects.requireNonNull(map.get("value")).toString());
        if (map.containsKey("index")) {
            int index = GuiStepConfig.integer(map.get("index"));
            if (index < 0) throw new IllegalArgumentException("SELECT index must be nonnegative");
            return option.setIndex(index);
        }
        throw new IllegalArgumentException("SELECT option requires label, value or index");
    }
    private static void assertion(Locator locator, GuiStepConfig c) {
        Map<String,Object> assertion = GuiStepConfig.map(c.get("assertion"));
        String type = Objects.toString(assertion.get("type"), Objects.toString(c.get("action"),""));
        Object expected = assertion.containsKey("expected") ? assertion.get("expected") : c.get("expected");
        switch (type.toUpperCase(Locale.ROOT)) {
            case "CHECKED" -> assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(c.timeout()));
            case "UNCHECKED" -> assertThat(locator).not().isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(c.timeout()));
            case "EXISTS" -> locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(c.timeout()));
            case "NOT_EXISTS" -> assertThat(locator).hasCount(0,new LocatorAssertions.HasCountOptions().setTimeout(c.timeout()));
            case "TEXT_GLOB" -> assertThat(locator).hasText(GuiTextGlob.pattern(Objects.requireNonNull(expected).toString()),new LocatorAssertions.HasTextOptions().setTimeout(c.timeout()).setUseInnerText(true));
            case "VALUE_GLOB" -> assertThat(locator).hasValue(globPattern(Objects.requireNonNull(expected).toString()),new LocatorAssertions.HasValueOptions().setTimeout(c.timeout()));
            case "TEXT_MATCHES" -> assertThat(locator).hasText(java.util.regex.Pattern.compile(Objects.requireNonNull(expected).toString(),java.util.regex.Pattern.DOTALL),new LocatorAssertions.HasTextOptions().setTimeout(c.timeout()).setUseInnerText(true));
            case "VALUE_MATCHES" -> assertThat(locator).hasValue(java.util.regex.Pattern.compile(Objects.requireNonNull(expected).toString(),java.util.regex.Pattern.DOTALL),new LocatorAssertions.HasValueOptions().setTimeout(c.timeout()));
            case "VISIBLE" -> assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(c.timeout()));
            case "HIDDEN" -> assertThat(locator).isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(c.timeout()));
            case "ENABLED" -> assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setTimeout(c.timeout()));
            case "DISABLED" -> assertThat(locator).isDisabled(new LocatorAssertions.IsDisabledOptions().setTimeout(c.timeout()));
            case "TEXT", "TEXT_EQUALS" -> assertThat(locator).hasText(Objects.requireNonNull(expected).toString(),new LocatorAssertions.HasTextOptions().setTimeout(c.timeout()).setUseInnerText(true));
            case "TEXT_CONTAINS" -> assertThat(locator).containsText(Objects.requireNonNull(expected).toString(),new LocatorAssertions.ContainsTextOptions().setTimeout(c.timeout()).setUseInnerText(true));
            case "VALUE" -> assertThat(locator).hasValue(Objects.requireNonNull(expected).toString(),new LocatorAssertions.HasValueOptions().setTimeout(c.timeout()));
            case "COUNT" -> assertThat(locator).hasCount(GuiStepConfig.integer(expected),new LocatorAssertions.HasCountOptions().setTimeout(c.timeout()));
            default -> throw new IllegalArgumentException("Unsupported GUI assertion: " + type);
        }
    }
    public static void cleanup(ExecutionContext context, TestLogger logger) { GuiSessionManager.close(context,null); }
    public static void cleanup(String name, TestLogger logger) { GuiSessionManager.closeLegacy(name); }
    public static void cleanupAll(TestLogger logger) { GuiSessionManager.closeAll(); }
}
