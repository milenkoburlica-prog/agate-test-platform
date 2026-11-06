package at.co.svc.agate.engine.window;

import java.util.*;
import at.co.svc.agate.core.dsl.model.*;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.engine.AbstractStepEngine;
import at.co.svc.agate.core.interfaces.TestLogger;

/**
 * Desktop operations only. LOOP and condition remain framework
 * responsibilities.
 */
public class WindowEngine extends AbstractStepEngine {
    private final WindowsDesktop desktop;

    public WindowEngine() {
        this(new WindowsDesktop());
    }

    public WindowEngine(WindowsDesktop desktop) {
        this.desktop = desktop;
    }

    @Override
    public boolean canExecute(StepType type) {
        return type == StepType.WINDOW;
    }

    @Override
    public void doExecute(TestCase tc, TestStep step, ExecutionContext context, String file, int index, Boolean print,
            TestLogger logger, boolean verbose) throws Exception {
        Map<String, Object> raw = step.getGui() == null ? Map.of() : step.getGui();
        String op = Objects.toString(step.getOp(), "").toUpperCase(Locale.ROOT);
        String action = resolve(tc, step, context, raw.getOrDefault("action", step.getAction()), file, index)
                .toUpperCase(Locale.ROOT);
        if (Boolean.TRUE.equals(print))
            logger.info("    >>> WINDOW     : [" + step.getId() + "] | " + op + " | " + action);
        if (raw.containsKey("target"))
            throw new IllegalArgumentException(
                    "WINDOW native control targets require a UIA provider; supported scope is top-level desktop windows");
        if (op.equals("EXEC")) {
            String caption = required(resolve(tc, step, context, raw.get("caption"), file, index), "caption");
            if (action.equals("FIND")) {
                WindowResult result = desktop.find(caption);
                String response = required(step.getResponse(), "response");
                context.storeBuffer(response, result);
                log(logger, print, "RESPONSE", response + " | windows=" + result.windows().size());
                return;
            }
            if (!Set.of("CLOSE", "MAXIMIZE", "MINIMIZE", "RESTORE").contains(action))
                throw new IllegalArgumentException("Unsupported WINDOW EXEC action: " + action);
            log(logger, print, "RESULT", action + " requested | matches=" + desktop.action(caption, action));
            return;
        }
        if (op.equals("BUFFER") || op.equals("ASSERT")) {
            WindowResult result;
            if (step.getResponse() != null) {
                result = context.getResponse(step.getResponse(), WindowResult.class);
                if (result == null)
                    throw new IllegalArgumentException("Missing WINDOW response: " + step.getResponse());
            } else if (op.equals("ASSERT"))
                result = desktop.find(required(resolve(tc, step, context, raw.get("caption"), file, index), "caption"));
            else
                throw new IllegalArgumentException("WINDOW BUFFER requires response");
            Object actual = extract(result, action, raw);
            if (op.equals("BUFFER")) {
                String name = required(step.getName(), "name");
                String value = Objects.toString(actual);
                tc.addVariable(name, value);
                context.storeBuffer(name, value);
                context.setVar(name, value);
                log(logger, print, "BUFFER", name + " = " + value);
                return;
            }
            String expected = resolve(tc, step, context, raw.get("expected"), file, index);
            if (expected.isBlank())
                throw new IllegalArgumentException("WINDOW ASSERT requires expected");
            if (action.equals("EXISTS") && !Set.of("true", "false").contains(expected.toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException("EXISTS expected must be true or false");
            boolean wanted = Boolean.parseBoolean(expected);
            if (Boolean.TRUE.equals(raw.get("wait"))) {
                if (step.getResponse() != null || !action.equals("EXISTS"))
                    throw new IllegalArgumentException("WINDOW wait requires a live caption and EXISTS action");
                long timeout = step.getTimeout() == null ? 30000 : step.getTimeout();
                long deadline = System.nanoTime() + timeout * 1_000_000L;
                String caption = required(resolve(tc, step, context, raw.get("caption"), file, index), "caption");
                while ((Boolean) actual != wanted) {
                    if (System.nanoTime() >= deadline)
                        throw new IllegalStateException("WINDOW wait timed out: " + caption);
                    Thread.sleep(200);
                    actual = !desktop.find(caption).windows().isEmpty();
                }
            }
            boolean passed = action.equals("EXISTS") ? actual.equals(wanted)
                    : Objects.toString(actual).equals(expected);
            if (!passed)
                throw new IllegalStateException(
                        "WINDOW ASSERT " + action + " expected=" + expected + " actual=" + actual);
            log(logger, print, "RESULT", "SUCCESS | " + action + " = " + actual);
            return;
        }
        throw new IllegalArgumentException("Unsupported WINDOW op: " + op);
    }

    public static Object extract(WindowResult result, String action, Map<String, Object> config) {
        if (action.equals("EXISTS"))
            return !result.windows().isEmpty();
        if (action.equals("COUNT"))
            return result.windows().size();
        if (!Set.of("CAPTION", "HANDLE", "PROCESS_ID").contains(action))
            throw new IllegalArgumentException("Unsupported WINDOW extraction: " + action);
        int index = config.containsKey("index") ? Integer.parseInt(config.get("index").toString()) : 0;
        if (index < 0 || index >= result.windows().size())
            throw new IllegalArgumentException("WINDOW result index out of range: " + index);
        var window = result.windows().get(index);
        return switch (action) {
        case "CAPTION" -> window.caption();
        case "HANDLE" -> window.handle();
        default -> window.processId();
        };
    }

    private static String resolve(TestCase tc, TestStep step, ExecutionContext context, Object raw, String file,
            int index) {
        if (raw == null)
            return "";
        Map<String, Object> vars = new HashMap<>(tc.getVariables());
        vars.putAll(context.getVars());
        vars.putAll(context.getBufferMap());
        return new YamlPlaceholderResolver().resolve(tc, raw.toString(), vars, file, index, "WINDOW", step);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("WINDOW requires " + field);
        return value;
    }

    private static void log(TestLogger logger, Boolean print, String field, Object value) {
        if (Boolean.TRUE.equals(print))
            logger.info(String.format("    >>> %-11s: %s", field, value));
    }
}
