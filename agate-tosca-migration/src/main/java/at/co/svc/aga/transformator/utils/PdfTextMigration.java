package at.co.svc.aga.transformator.utils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import at.co.svc.aga.transformator.dto.*;

/** Converts PDF content checks to the existing AGATE PDF assertions contract. */
public final class PdfTextMigration {
    private PdfTextMigration() {}
    public record Result(List<Map<String,Object>> steps, List<String> review) {}
    public static Result translate(CleanStep step) {
        return translate(step, Boolean.getBoolean("agate.migration.pdf.textOnly"));
    }
    public static Result translate(CleanStep step, boolean textOnly) {
        try {
            String target=null,password=null;
            List<StepValueDetails> checks=new ArrayList<>();
            for (StepValueDetails v:Objects.requireNonNull(step.getValues(),"PDF values")) {
                Map<String,Object> g=v.getGui();
                if(g==null)throw new IllegalArgumentException("PDF metadata missing");
                String name=Objects.toString(g.get("name"),Objects.toString(v.getName(),""));
                String mode=Objects.toString(v.getActionMode(),"");
                String raw=Objects.toString(v.getValue(),"");
                if(Set.of("Input","Insert","37").contains(mode) && "Target PDF".equals(name)) {
                    if(target!=null)throw new IllegalArgumentException("Duplicate Target PDF");
                    if(v.getxCondition()!=null&&!v.getxCondition().isBlank())throw new IllegalArgumentException("Conditional PDF target requires mapping");
                    target=convert(raw);continue;
                }
                if(Set.of("Input","Insert","37").contains(mode) && Set.of("PDF Password","Password").contains(name)) {
                    if(password!=null)throw new IllegalArgumentException("Duplicate PDF password");
                    if(v.getxCondition()!=null&&!v.getxCondition().isBlank())throw new IllegalArgumentException("Conditional PDF password requires mapping");
                    password=convert(raw);continue;
                }
                if(mode.isBlank() && (raw.isBlank()||raw.equals("{NULL}")))continue;
                if(Set.of("Select","83").contains(mode)) {
                    // Structural selection is already represented by attributeParents of each check.
                    if(!textOnly && !(raw.isBlank()||raw.equals("{NULL}")))
                        throw new IllegalArgumentException("PDF selection constraint requires mapping; textOnly mode searches whole document");
                    continue;
                }
                if(!Set.of("Verify","69").contains(mode))throw new IllegalArgumentException("Unsupported PDF action mode: "+mode);
                checks.add(v);
            }
            if(target==null||target.isBlank()||target.equals("{NULL}"))throw new IllegalArgumentException("Target PDF required");
            if(checks.isEmpty())throw new IllegalArgumentException("No supported PDF content checks");
            List<Map<String,Object>> outputs=new ArrayList<>();String lastCondition=null;
            List<Map<String,Object>> group=null;
            for(StepValueDetails v:checks) {
                Map<String,Object> g=v.getGui();
                String kind=Objects.toString(g.get("businessType"),"");
                if(!Set.of("Cell","PdfTextArea","PdfText","Text","Label").contains(kind))
                    throw new IllegalArgumentException("Unsupported PDF verification type: "+kind);
                if(!textOnly && hasScope(g))throw new IllegalArgumentException(
                        "PDF page/area/table verification cannot be preserved by whole-document engine; set -Dagate.migration.pdf.textOnly=true to explicitly accept content-only checks");
                String property=Objects.toString(v.getActionProperty(),"");
                if(!property.isBlank() && !Set.of("Text","InnerText","TextContent").contains(property))
                    throw new IllegalArgumentException("Unsupported PDF action property: "+property);
                String operator=Objects.toString(v.getOperator(),"");
                boolean negative=Set.of("2","!=","<>").contains(operator);
                if(!negative && !Set.of("","1","==","=").contains(operator))throw new IllegalArgumentException("Unsupported PDF operator: "+operator);
                String raw=Objects.toString(v.getValue(),"");
                if(raw.isBlank()||raw.equals("{NULL}"))throw new IllegalArgumentException("Empty/null PDF expectation requires mapping");
                String value=convert(raw),action;
                if(value.startsWith("{REGEX[") && value.endsWith("]}")) {
                    value=value.substring(7,value.length()-2);
                    if(value.isEmpty())throw new IllegalArgumentException("Empty PDF regex");
                    validateRegex(value);action=negative?"NO_REGEX":"REGEX";
                } else {
                    rejectUnresolved(value);
                    if(hasGlob(value)) { value=glob(value); action=negative?"NO_REGEX":"REGEX"; }
                    else action=negative?"NO_EXIST":"EXIST";
                }
                String condition=combined(step.getCondition(),v.getxCondition());
                // Keep adjacent groups, preserving fail-fast order across conditional checks.
                if(group==null||!Objects.equals(condition,lastCondition)) {
                    Map<String,Object> out=new LinkedHashMap<>();
                    out.put("id",Objects.toString(step.getSurrogate(),"pdf")+(outputs.isEmpty()?"":"_"+outputs.size()));
                    out.put("type","PDF");out.put("op","ASSERT");out.put("targetPDF",target);
                    if(password!=null&&!password.isBlank())out.put("pdfPassword",password);
                    if(condition!=null)out.put("condition",condition);
                    group=new ArrayList<>();out.put("assertions",group);outputs.add(out);lastCondition=condition;
                }
                Map<String,Object> assertion=new LinkedHashMap<>();assertion.put("value",value);assertion.put("action",action);group.add(assertion);
            }
            return new Result(outputs,List.of());
        }catch(IllegalArgumentException|NullPointerException e){return new Result(List.of(),List.of(Objects.toString(e.getMessage(),"Missing PDF parameters")));}
    }
    private static boolean hasScope(Map<String,Object> g) {
        Object parents=g.get("attributeParents");
        if(parents instanceof Collection<?> c&&!c.isEmpty())return true;
        Object properties=g.get("properties");
        if(properties instanceof Map<?,?> m)for(Object key:m.keySet())
            if(Set.of("Page","X","Y","Width","Height","Row","Column").contains(key.toString()))return true;
        return false;
    }
    private static String convert(String s){return ToscaValueTranslator.translateToscaValues(s);}
    private static String combined(String parent,String child) {
        String p=parent==null||parent.isBlank()?null:convert(parent);
        String c=child==null||child.isBlank()?null:convert(child);
        if(p==null)return c;if(c==null)return p;return "("+p+") AND ("+c+")";
    }
    private static void rejectUnresolved(String s) {
        java.util.regex.Matcher m=Pattern.compile("\\{([A-Z][A-Z0-9_]*)\\[").matcher(s);
        while(m.find())if(!Set.of("B","R","E","T","PL","XL").contains(m.group(1)))
            throw new IllegalArgumentException("Unresolved PDF expression: "+m.group(1));
    }
    private static void validateRegex(String s) {
        rejectUnresolved(s);
        // Replace dynamic tags solely for syntax checking; keep the runtime expression unchanged.
        String check=s.replaceAll("\\{(?:B|R|E|T|PL|XL)\\[[^\\]]*\\]\\}","placeholder");
        try{Pattern.compile(check);}catch(PatternSyntaxException e){throw new IllegalArgumentException("Invalid PDF regex: "+e.getDescription());}
    }
    private static boolean hasGlob(String s){return s.indexOf('*')>=0||s.indexOf('?')>=0;}
    private static String glob(String s) {
        StringBuilder result=new StringBuilder("(?s)");StringBuilder literal=new StringBuilder();
        for(char c:s.toCharArray()){
            if(c=='*'||c=='?'){if(literal.length()>0){result.append(Pattern.quote(literal.toString()));literal.setLength(0);}result.append(c=='*'?".*":".");}
            else literal.append(c);
        }
        if(literal.length()>0)result.append(Pattern.quote(literal.toString()));return result.toString();
    }
}
