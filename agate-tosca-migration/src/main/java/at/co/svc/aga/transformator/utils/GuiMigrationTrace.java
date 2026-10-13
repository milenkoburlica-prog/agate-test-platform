package at.co.svc.aga.transformator.utils;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

/** Opt-in diagnostics; stderr never becomes generated reusable YAML. */
public final class GuiMigrationTrace {
    private GuiMigrationTrace() {}
    private static final ObjectMapper JSON=new ObjectMapper();
    public static void emit(String event,CleanStep step,StepValueDetails value,Object detail) {
        if(!Boolean.getBoolean("agate.migration.gui.trace"))return;
        String filter=System.getProperty("agate.migration.gui.trace.filter","").toLowerCase(Locale.ROOT);
        String context=Objects.toString(step.getModule(),"")+" "+Objects.toString(step.getName(),"")+" "+Objects.toString(step.getSurrogate(),"");
        if(value!=null && value.getGui()!=null)context+=" "+Objects.toString(value.getGui().get("name"),"")+" "+Objects.toString(value.getGui().get("valueSurrogate"),"");
        if(!filter.isEmpty()&&!context.toLowerCase(Locale.ROOT).contains(filter))return;
        Map<String,Object> record=new LinkedHashMap<>();record.put("event",event);record.put("stepId",step.getSurrogate());record.put("step",step.getName());record.put("module",step.getModule());
        if(value!=null) {record.put("actionMode",value.getActionMode());record.put("actionProperty",value.getActionProperty());record.put("operator",value.getOperator());record.put("gui",safe(value.getGui(),false));}
        record.put("detail",safe(detail,false));
        try {System.err.println("[GUI-TRACE] "+JSON.writeValueAsString(record));}
        catch(Exception ignored){System.err.println("[GUI-TRACE] diagnostic serialization failed; migration continues");}
    }
    private static Object safe(Object data,boolean properties) {
        if(data instanceof Map<?,?> map) {
            Map<String,Object> out=new LinkedHashMap<>();
            for(var entry:map.entrySet()) {
                String key=Objects.toString(entry.getKey(),"");String lower=key.toLowerCase(Locale.ROOT);
                boolean redacted=Set.of("value","rawvalue","expected","condition","disabledDescription".toLowerCase(Locale.ROOT),"buffer").contains(lower)
                    || lower.contains("password")||lower.contains("secret")||lower.contains("token")
                    || properties && !Set.of("id","name","tag","engine","type","classname").contains(lower);
                out.put(key,redacted?shape(entry.getValue()):safe(entry.getValue(),lower.equals("properties")));
            }return out;
        }
        if(data instanceof Collection<?> list)return list.stream().map(x->safe(x,false)).toList();
        return data;
    }
    private static Object shape(Object data) {
        if(data instanceof Map<?,?> map){Map<String,Object> out=new LinkedHashMap<>();map.forEach((k,v)->out.put(Objects.toString(k),shape(v)));return out;}
        if(data==null)return null;
        String text=Objects.toString(data);String kind=text.isEmpty()?"empty":text.equals("{NULL}")?"NULL":text.equals("X")||text.equals("{CLICK}")||text.equals("{JUMPTO[CLICK]}")?"click-marker":text.contains("{")?"expression":"literal";
        return Map.of("redacted",true,"kind",kind,"length",text.length(),"wildcard",text.contains("*"));
    }
}
