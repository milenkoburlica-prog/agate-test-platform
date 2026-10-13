package at.co.svc.agate.server.validation;

import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.options.AriaRole;

/** GUI contract checks before launching a browser; keeps other engines untouched. */
public final class GuiSchemaValidator implements AgateValidator {
    private static final Set<String> OPS=Set.of("OPEN","BACK","CLICK","FILL","SELECT","CHECK","UNCHECK","HOVER","PRESS","UPLOAD","DOWNLOAD","DOWNLOAD_PDF","WAIT","ASSERT","EXTRACT","SCREENSHOT","ASSERT_PAGE_STRUCTURE","CLOSE","OPEN_TAB","CLOSE_TAB","FIND_TAB","BUFFER_TAB_EXISTS");
    private static final Set<String> ASSERTIONS=Set.of("CHECKED","UNCHECKED","VISIBLE","HIDDEN","ENABLED","DISABLED","TEXT","TEXT_EQUALS","TEXT_CONTAINS","VALUE","COUNT","EXISTS","NOT_EXISTS","TEXT_MATCHES","VALUE_MATCHES","TEXT_GLOB","VALUE_GLOB");
    @Override public List<ValidationIssue> validate(ValidationContext c) {
        List<ValidationIssue> issues=new ArrayList<>();
        for(var tc:ValidationUtil.testCases(c.root())) for(var ref:ValidationUtil.steps(tc.node(),tc.index())) {
            JsonNode step=ref.node();
            if(!"GUI".equals(ValidationUtil.type(step)))continue;
            try { check(step,tc.node()); }
            catch(IllegalArgumentException e) {issues.add(ValidationIssue.error("AGATE-V800",ref.path()+": "+e.getMessage(),c.file(),null));}
        }
        return issues;
    }
    private static void check(JsonNode step,JsonNode tc) {
        String op=Objects.toString(ValidationUtil.text(step,"op"),"EXEC").toUpperCase(Locale.ROOT);
        if(op.equals("EXEC"))op=Objects.toString(ValidationUtil.text(step,"action"),"").toUpperCase(Locale.ROOT);
        if(op.equals("CLOSE_BROWSER"))op="CLOSE";
        if(op.equals("BUFFER") && Set.of("TEXT","VALUE","ATTRIBUTE").contains(Objects.toString(ValidationUtil.text(step,"action"),"").toUpperCase(Locale.ROOT))) {
            if(!step.path("name").isTextual() || step.path("name").asText().isBlank())throw new IllegalArgumentException("GUI BUFFER requires name");
            if("ATTRIBUTE".equalsIgnoreCase(ValidationUtil.text(step,"action")) && (!step.path("attribute").isTextual() || step.path("attribute").asText().isBlank()))throw new IllegalArgumentException("BUFFER ATTRIBUTE requires attribute");
        }
        if(op.equals("BUFFER") && "EXISTS".equalsIgnoreCase(ValidationUtil.text(step,"action")))op="BUFFER_TAB_EXISTS";
        if(op.equals("INPUT"))op="FILL";
        if(op.equals("BUFFER"))op="EXTRACT";
        if(!OPS.contains(op))throw new IllegalArgumentException("Unsupported GUI operation "+op);
        if(step.has("caption") && (!step.get("caption").isTextual() || step.get("caption").asText().isBlank()))throw new IllegalArgumentException("caption requires nonempty text");
        Map<String,Object> simplifiedInput = new LinkedHashMap<>();
        for(String kind:at.co.svc.agate.engine.gui.GuiElementTarget.KINDS)
            if(step.has(kind)) simplifiedInput.put(kind, jsonValue(step.get(kind)));
        if(step.has("target"))simplifiedInput.put("target",jsonValue(step.get("target")));
        if(step.has("selector"))simplifiedInput.put("selector",jsonValue(step.get("selector")));
        boolean simplified = !at.co.svc.agate.engine.gui.GuiElementTarget.fromStep(simplifiedInput).isEmpty();
        JsonNode timeout=step.has("timeout")?step.get("timeout"):tc.get("timeout");
        if(timeout!=null && (!timeout.isIntegralNumber() || timeout.asLong()<=0 || timeout.asLong()>Integer.MAX_VALUE))throw new IllegalArgumentException("timeout must be positive milliseconds");
        if(op.equals("OPEN") && !step.hasNonNull("url") && !step.hasNonNull("value"))throw new IllegalArgumentException("OPEN requires url or legacy value");
        if(Set.of("FILL","SELECT","PRESS","UPLOAD").contains(op)&&!step.hasNonNull("value"))throw new IllegalArgumentException(op+" requires value");
        if(!Set.of("OPEN","BACK","WAIT","SCREENSHOT","CLOSE","ASSERT_PAGE_STRUCTURE","OPEN_TAB","CLOSE_TAB","FIND_TAB","BUFFER_TAB_EXISTS").contains(op)) {
            JsonNode target=step.get("target");
            if(simplified) { /* validated by shared contract */ }
            else if(target!=null && target.isObject())target(target);
            else if(target!=null && target.isTextual()) {
                String name=target.asText();int dot=name.indexOf('.');
                if(dot<1 || !tc.path("pages").path(name.substring(0,dot)).path("elements").has(name.substring(dot+1)))throw new IllegalArgumentException("Unknown GUI target reference "+name);
            }else if(!step.hasNonNull("selector") && !("BUFFER".equals(ValidationUtil.op(step))&&step.hasNonNull("action")))throw new IllegalArgumentException(op+" requires target or legacy selector");
        }
        if(op.equals("DOWNLOAD_PDF")) {
            for(String key:List.of("outputFile","responseUrlGlob"))
                if(!step.path(key).isTextual() || step.path(key).asText().isBlank())
                    throw new IllegalArgumentException("DOWNLOAD_PDF requires " + key);
        }
        if(op.equals("SELECT"))checkSelection(step.get("value"));
        if(op.equals("ASSERT")) {
            JsonNode assertion=step.get("assertion");
            if(assertion!=null && step.has("action"))throw new IllegalArgumentException("Use action OR legacy assertion, not both");
            String type=assertion==null?ValidationUtil.text(step,"action"):ValidationUtil.text(assertion,"type");
            if(type==null||!ASSERTIONS.contains(type.toUpperCase(Locale.ROOT)))throw new IllegalArgumentException("Unsupported GUI assertion");
            if(Set.of("TEXT","TEXT_EQUALS","TEXT_CONTAINS","VALUE","COUNT","TEXT_MATCHES","VALUE_MATCHES","TEXT_GLOB","VALUE_GLOB").contains(type.toUpperCase(Locale.ROOT)) && !(assertion!=null&&assertion.hasNonNull("expected"))&&!step.hasNonNull("expected"))throw new IllegalArgumentException(type+" requires expected");
        }
    }
    private static Object jsonValue(JsonNode node) {
        if(node.isObject()) {
            Map<String,Object> out=new LinkedHashMap<>();
            node.fields().forEachRemaining(e->out.put(e.getKey(),jsonValue(e.getValue())));return out;
        }
        if(node.isTextual())return node.asText();
        if(node.isBoolean())return node.booleanValue();
        if(node.isIntegralNumber())return node.bigIntegerValue().bitLength()<63?node.longValue():node.bigIntegerValue();
        if(node.isNumber())return node.decimalValue();
        return null;
    }
    private static void checkSelection(JsonNode value) {
        if(value==null||value.isNull())throw new IllegalArgumentException("SELECT requires value");
        if(value.isArray()){if(value.isEmpty())throw new IllegalArgumentException("SELECT options must not be empty");for(JsonNode option:value){if((option.has("labelPattern")||option.has("labelGlob")))throw new IllegalArgumentException("labelPattern requires a single SELECT option");checkSelection(option);}return;}
        if(value.isObject()) {
            if(value.size()!=1 || !(value.hasNonNull("label")||value.hasNonNull("value")||value.hasNonNull("index")||value.hasNonNull("labelPattern")||value.hasNonNull("labelGlob")))throw new IllegalArgumentException("SELECT option requires exactly one of label, value, index, labelPattern, labelGlob");
            if(value.has("labelGlob")&&!value.get("labelGlob").isTextual())throw new IllegalArgumentException("labelGlob must be text");
            if(value.has("labelPattern")) {
                if(!value.get("labelPattern").isTextual())throw new IllegalArgumentException("labelPattern must be text");
                try{java.util.regex.Pattern.compile(value.get("labelPattern").asText());}catch(java.util.regex.PatternSyntaxException e){throw new IllegalArgumentException("Invalid labelPattern regex");}
            }
            if(value.has("index") && (!value.get("index").isIntegralNumber()||value.get("index").asInt()<0))throw new IllegalArgumentException("SELECT index must be nonnegative integer");
        } else if(!value.isTextual()&&!value.isNumber())throw new IllegalArgumentException("SELECT value must be text or option object");
    }
    private static void target(JsonNode target) {
        int count=0;for(String key:List.of("css","role","testId","label","text","placeholder"))if(target.hasNonNull(key))count++;
        if(count!=1)throw new IllegalArgumentException("target requires exactly one locator strategy");
        if(target.has("role"))try{AriaRole.valueOf(target.get("role").asText().toUpperCase(Locale.ROOT));}catch(Exception e){throw new IllegalArgumentException("Unknown ARIA role");}
        if(target.has("scope")){if(!target.get("scope").isObject())throw new IllegalArgumentException("scope must be a locator object");target(target.get("scope"));}
        if(target.has("frames")&&!target.get("frames").isArray())throw new IllegalArgumentException("frames must be a list of iframe selectors");
        if(target.has("nth")&&(!target.get("nth").isIntegralNumber()||target.get("nth").asInt()<0))throw new IllegalArgumentException("nth must be a nonnegative integer");
    }
}
