package at.co.svc.agate.engine.gui;

import java.util.*;
import java.util.regex.*;
import at.co.svc.agate.core.dsl.model.*;
import at.co.svc.agate.core.dsl.resolver.YamlPlaceholderResolver;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;

/** Immutable per-execution view. Never replace placeholder templates on TestStep. */
public final class GuiStepConfig {
    private static final Pattern SHORT_ENV = Pattern.compile("\\{E\\[([^]\\s]+)]}");
    private final Map<String, Object> fields;
    private GuiStepConfig(Map<String, Object> fields) { this.fields = fields; }

    public static GuiStepConfig resolve(TestCase tc, TestStep step, ExecutionContext context,
                                        String file, int index) {
        Map<String, Object> raw = new LinkedHashMap<>();
        if (step.getGui() != null) raw.putAll(step.getGui());
        // Programmatically created legacy steps also work.
        raw.putIfAbsent("op", step.getOp() == null ? "EXEC" : step.getOp());
        put(raw, "action", step.getAction()); put(raw, "selector", step.getSelector());
        put(raw, "value", step.getValue()); put(raw, "url", step.getUrl());
        put(raw, "expected", step.getExpected()); put(raw, "response", step.getResponse());
        put(raw, "source", step.getSource()); put(raw, "timeout", step.getTimeout());
        String op = Objects.toString(raw.get("op"), "EXEC").toUpperCase(Locale.ROOT);
        if (op.equals("EXEC") || op.equals("CALL")) op = Objects.toString(raw.get("action"), "").toUpperCase(Locale.ROOT);
        if (op.equals("INPUT")) op = "FILL";
        if (op.equals("CLOSE_BROWSER")) op = "CLOSE";
        if (op.equals("BUFFER") && Set.of("TEXT","VALUE","ATTRIBUTE").contains(Objects.toString(raw.get("action"),"").toUpperCase(Locale.ROOT))) {
            String extract=raw.get("action").toString().toUpperCase(Locale.ROOT);
            if (!(raw.get("name") instanceof String name) || name.isBlank())
                throw new IllegalArgumentException("GUI BUFFER requires name");
            if (extract.equals("ATTRIBUTE") && (!(raw.get("attribute") instanceof String attribute) || attribute.isBlank()))
                throw new IllegalArgumentException("BUFFER ATTRIBUTE requires attribute");
            raw.put("buffer",raw.get("name"));
            raw.put("source",extract.equals("VALUE")?"value":"text");
            op="EXTRACT";
        }
        if (op.equals("BUFFER") && "EXISTS".equalsIgnoreCase(Objects.toString(raw.get("action"),""))) {
            op = "BUFFER_TAB_EXISTS";
        }
        if (op.equals("BUFFER")) {
            op = "EXTRACT";
            raw.putIfAbsent("selector", Objects.toString(raw.get("action"), ""));
            raw.putIfAbsent("buffer", Objects.toString(raw.get("response"), "extracted"));
        }
        raw.put("op", op);
        Map<String,Object> simplified = GuiElementTarget.fromStep(raw);
        if (!simplified.isEmpty()) raw.put("target", simplified);
        if (op.equals("ASSERT") && raw.containsKey("assertion") && raw.containsKey("action"))
            throw new IllegalArgumentException("Use action OR legacy assertion for GUI ASSERT, not both");
        if (raw.get("target") instanceof String && !op.equals("ASSERT_PAGE_STRUCTURE")) {
            String ref = (String) raw.get("target");
            int dot = ref.indexOf('.');
            if (dot < 1) throw new IllegalArgumentException("GUI target reference requires page.element: " + ref);
            Map<String, Object> page = map(tc.getGuiPages().get(ref.substring(0, dot)));
            Object target = map(page.get("elements")).get(ref.substring(dot + 1));
            if (!(target instanceof Map)) throw new IllegalArgumentException("Unknown GUI target: " + ref);
            raw.put("target", target);
        }
        Map<String, Object> vars = new HashMap<>(tc.getVariables());
        vars.putAll(context.getVars()); vars.putAll(context.getBufferMap());
        Object resolved = resolveTree(raw, tc, step, vars, file, index);
        Map<String, Object> result = map(resolved);
        int timeout = result.get("timeout") != null ? integer(result.get("timeout"))
                : tc.getTimeout() != null ? tc.getTimeout() : 30000;
        if (timeout <= 0) throw new IllegalArgumentException("GUI timeout must be positive milliseconds");
        result.put("timeout", timeout);
        return new GuiStepConfig(result);
    }
    private static void put(Map<String, Object> m, String key, Object value) {
        if (value != null) m.putIfAbsent(key, value);
    }
    private static Object resolveTree(Object raw, TestCase tc, TestStep step, Map<String, Object> vars,
                                      String file, int index) {
        if (raw instanceof Map<?, ?> m) {
            Map<String, Object> out = new LinkedHashMap<>();
            m.forEach((k,v) -> out.put(k.toString(), resolveTree(v, tc, step, vars, file, index)));
            return GuiDateExpression.resolve(out, java.time.LocalDate.now());
        }
        if (raw instanceof List<?> list) return list.stream().map(v -> resolveTree(v, tc, step, vars, file, index)).toList();
        if (!(raw instanceof String text)) return raw;
        Matcher matcher = SHORT_ENV.matcher(text); StringBuffer replaced = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            if (key.startsWith("env.") || key.startsWith("users.")) continue;
            Object value = vars.get(key);
            if (value == null) value = System.getenv(key);
            if (value == null) throw new IllegalArgumentException("Missing GUI runtime parameter E[" + key + "]");
            matcher.appendReplacement(replaced, Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(replaced);
        return new YamlPlaceholderResolver().resolve(tc, replaced.toString(), vars, file, index, "GUI", step);
    }
    @SuppressWarnings("unchecked")
    public static Map<String, Object> map(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Collections.emptyMap();
    }
    public static int integer(Object value) { return Integer.parseInt(value.toString()); }
    public Object get(String key) { return fields.get(key); }
    public String text(String key) { return fields.get(key) == null ? null : fields.get(key).toString(); }
    public String required(String key) {
        String value = text(key);
        if (value == null) throw new IllegalArgumentException(op() + " requires " + key);
        return value;
    }
    public String op() { return required("op"); }
    public int timeout() { return integer(get("timeout")); }
    public Map<String, Object> target() {
        Map<String, Object> target = map(get("target"));
        if (!target.isEmpty()) return target;
        String css = text("selector");
        return css == null || css.isBlank() ? Collections.emptyMap() : Map.of("css", normalize(css));
    }
    private static String normalize(String value) {
        String s = value.trim();
        if (s.endsWith("]") && !s.contains("[")) s = s.substring(0, s.length() - 1);
        if (s.startsWith("data-test-id=") || s.startsWith("name=")) s = "[" + s + "]";
        return s;
    }
}
