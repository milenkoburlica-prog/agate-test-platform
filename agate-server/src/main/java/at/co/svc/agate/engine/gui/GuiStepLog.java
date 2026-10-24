package at.co.svc.agate.engine.gui;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.*;
import com.microsoft.playwright.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.interfaces.TestLogger;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;

/** Human-readable action context. Diagnostic reads never change the step outcome. */
public final class GuiStepLog {
    private static final Pattern PARAMETER = Pattern.compile("\\{([ERBL])\\[([^]]+)]}");
    private static final Pattern SECRET_NAME = Pattern.compile("(?i)(password|passwd|pwd|pin|secret|token|credential)");
    private final TestLogger logger;
    private final GuiSession session;
    private final GuiStepConfig config;
    private final TestStep step;
    private final boolean enabled;

    public GuiStepLog(TestLogger logger, boolean enabled, GuiSession session, GuiStepConfig config, TestStep step) {
        this.logger=logger;this.enabled=enabled && logger!=null;this.session=session;this.config=config;this.step=step;
        // Register even for silent steps so later failure logs cannot expose their input.
        if (sensitive()) {
            remember(config.get("value"));
            remember(GuiStepConfig.map(config.get("assertion")).get("expected"));
            remember(config.get("expected"));
        }
    }
    public void begin(int index,String id,boolean verbose) {
        if(!enabled)return;
        if(verbose)dsl();
        field("GUI", "[" + id + "] | " + config.op());
        if(config.text("caption")!=null)field("Caption",config.text("caption"));
        if(config.text("page")!=null)field("Page",config.text("page"));
        if(!config.target().isEmpty())field("Target",config.target());
        field("Timeout",config.timeout()+" ms");
        switch(config.op()) {
            case "OPEN" -> field("URL",GuiSession.safeUrl(Objects.toString(config.get("url"),config.text("value"))));
            case "SELECT" -> field("Select",sensitive()?"***":selection(config.get("value")));
            case "FILL" -> field("Value","***"+parameterHint());
            case "PRESS", "UPLOAD" -> field("Value",sensitive()?"***":config.get("value"));
            case "WAIT" -> field("Wait",config.target().isEmpty()?Objects.toString(config.get("value"),"1000")+" ms":Objects.toString(config.get("state"),"visible"));
            case "ASSERT" -> {
                Map<String,Object> a=GuiStepConfig.map(config.get("assertion"));
                field("Assert",Objects.toString(a.get("type"),config.text("action")));
                Object expected=a.containsKey("expected")?a.get("expected"):config.get("expected");
                if(expected!=null)field("Expected",sensitive()?"***":expected);
            }
        }
    }
    public void browser(String description) {field("Browser",description);}
    public void navigation(String before,String after) {
        if(!Objects.equals(before,after))field("Navigate",GuiSession.safeUrl(before)+" -> "+GuiSession.safeUrl(after));
    }
    public void success(){field("Result","SUCCESS (Action completed)");}
    public void selectionResult(Locator locator) {
        if(!enabled)return;
        observe("Selected",() -> locator.evaluate("el => Array.from(el.selectedOptions).map(o => ({label: o.label, value: o.value, index: o.index}))",
                null,new Locator.EvaluateOptions().setTimeout(250)));
    }
    public void extracted(String buffer,String value,Locator locator) {
        boolean hidden=sensitive()||passwordField(locator);
        if(hidden)session.rememberSecret(value);
        field("Buffer",buffer);field("Actual",hidden?"***":value);
    }
    public void artifact(Object path) {field("File",path);}
    public void assertionResult(Locator locator) {
        if(!enabled)return;
        Map<String,Object> a=GuiStepConfig.map(config.get("assertion"));
        String type=Objects.toString(a.get("type"),Objects.toString(config.get("action"),"")).toUpperCase(Locale.ROOT);
        if(Set.of("VALUE","VALUE_MATCHES","VALUE_GLOB").contains(type) && passwordField(locator)) {field("Actual","***");return;}
        observe("Actual",()->switch(type) {
            case "TEXT", "TEXT_EQUALS", "TEXT_CONTAINS", "TEXT_MATCHES", "TEXT_GLOB", "TEXT_PATTERN" -> locator.innerText(new Locator.InnerTextOptions().setTimeout(250));
            case "VALUE", "VALUE_MATCHES", "VALUE_GLOB" -> locator.inputValue(new Locator.InputValueOptions().setTimeout(250));
            case "COUNT", "EXISTS", "NOT_EXISTS" -> locator.count();
            case "VISIBLE", "HIDDEN" -> locator.isVisible()?"visible":"hidden";
            case "ENABLED", "DISABLED" -> locator.isEnabled(new Locator.IsEnabledOptions().setTimeout(250))?"enabled":"disabled";
            default -> "Assertion passed";
        });
    }
    // Read expected/actual secret input before an assertion can include it in an error.
    public void prepareInput(Locator locator) {
        if (sensitive() || passwordField(locator)) remember(config.get("value"));
    }
    public void prepareAssertion(Locator locator) {
        Map<String,Object> a=GuiStepConfig.map(config.get("assertion"));
        String type=Objects.toString(a.get("type"),Objects.toString(config.get("action"),"")).toUpperCase(Locale.ROOT);
        if(Set.of("VALUE","VALUE_MATCHES","VALUE_GLOB").contains(type) && passwordField(locator)) {
            remember(a.containsKey("expected")?a.get("expected"):config.get("expected"));
            try {remember(locator.inputValue(new Locator.InputValueOptions().setTimeout(250)));}catch(Exception ignored){}
        }
    }
    public String failure(Throwable failure) {
        String message=failure.getMessage();
        return session.redact(GuiFailureMessage.compact(failure.getClass().getSimpleName(), message, config.timeout()));
    }
    private void observe(String field,Supplier<Object> read) {
        if(!enabled)return;
        if(sensitive()){field(field,"***");return;}
        try{field(field,read.get());}
        catch(Exception e){field(field,"Unavailable after action (page/element may have changed)");}
    }
    private boolean passwordField(Locator locator) {
        if(locator==null)return false;
        try{return Boolean.TRUE.equals(locator.evaluate("el => el instanceof HTMLInputElement && el.type === 'password'",
                null,new Locator.EvaluateOptions().setTimeout(250)));}
        catch(Exception ignored){return false;}
    }
    private boolean sensitive() {
        if(Boolean.TRUE.equals(config.get("sensitive")))return true;
        Map<String,Object> assertion=GuiStepConfig.map(config.get("assertion"));
        Object descriptor=List.of(config.target(),Objects.toString(config.get("buffer"),""),Objects.toString(config.get("response"),""));
        return SECRET_NAME.matcher(descriptor.toString()).find();
    }
    private void remember(Object value) {
        if(value instanceof Map<?,?> m)m.values().forEach(this::remember);
        else if(value instanceof List<?> list)list.forEach(this::remember);
        else if(value!=null)session.rememberSecret(value.toString());
    }
    private String parameterHint() {
        Object raw=step.getGui()==null?step.getValue():step.getGui().get("value");
        Matcher m=PARAMETER.matcher(Objects.toString(raw,""));
        List<String> names=new ArrayList<>();while(m.find())names.add(m.group(1)+"["+m.group(2)+"]");
        return names.isEmpty()?"":" [parameter: "+String.join(", ",names)+"]";
    }
    private static Object selection(Object value) {
        if(value instanceof Map || value instanceof List)return value;
        return "value='"+value+"'";
    }
    private void dsl() {
        Map<String,Object> original=step.getGui();
        Map<String,Object> ordered=new LinkedHashMap<>();
        ordered.put("id",step.getId());ordered.put("type","GUI");ordered.put("op",original != null ? original.getOrDefault("op",config.op()) : config.op());
        if(original!=null) {
            for(String key:List.of("page","action","caption","link","button","textbox","dropdown","checkbox","radio","text","heading","expected","url","target","selector","value","assertion","source","attribute","name","buffer","response","state","timeout","sensitive"))
                if(original.containsKey(key))ordered.put(key,original.get(key));
        }else {
            if(step.getSelector()!=null)ordered.put("selector",step.getSelector());
            if(step.getValue()!=null)ordered.put("value",step.getValue());
            if(step.getExpected()!=null)ordered.put("expected",step.getExpected());
        }
        if(config.op().equals("FILL")||sensitive()) {
            if(ordered.containsKey("value"))ordered.put("value","***");
            if(ordered.containsKey("expected"))ordered.put("expected","***");
            if(ordered.get("assertion") instanceof Map<?,?> a) {
                Map<String,Object> masked=new LinkedHashMap<>(GuiStepConfig.map(a));
                if(masked.containsKey("expected"))masked.put("expected","***");ordered.put("assertion",masked);
            }
        }
        // Do not dump parameter maps, which can contain resolved credentials.
        if(ordered.containsKey("url"))ordered.put("url",GuiSession.safeUrl(Objects.toString(ordered.get("url"),"")));
        DumperOptions options=new DumperOptions();options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String yaml=new Yaml(options).dump(List.of(ordered));
        for(String line:yaml.split("\\R"))line(">>> DSL      "+line);
    }
    public void field(String name,Object value) {
        if(enabled)line(String.format("    %s>>> %-11s%s: %s", ConsoleColors.GREEN,
                name.toUpperCase(Locale.ROOT), ConsoleColors.RESET, Objects.toString(value,"")));
    }
    private void line(String text) {if(enabled)logger.info(session.redact(text));}
}
