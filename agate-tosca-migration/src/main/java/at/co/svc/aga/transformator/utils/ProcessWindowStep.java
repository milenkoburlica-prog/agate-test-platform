package at.co.svc.aga.transformator.utils;

import java.util.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

/** Migration contract only. WINDOW execution requires a future Windows engine. */
public final class ProcessWindowStep {
    private ProcessWindowStep() {}
    public record Translation(List<Map<String,Object>> steps, List<String> review, List<String> runtimeNotes) {
        public Translation(List<Map<String,Object>> steps, List<String> review) {
            this(steps, review, List.of());
        }
    }
    public static boolean supports(CleanStep s) {
        return "TBox Send Keys".equalsIgnoreCase(s.getModule()) || "TBox Window Operation".equalsIgnoreCase(s.getModule())
            || (s.getValues()!=null && s.getValues().stream().anyMatch(v->Set.of("anyui","uia","winx").contains(text(map(map(v.getGui()).get("properties")).get("Engine")).toLowerCase(Locale.ROOT))));
    }
    public static boolean process(CleanStep s,String indent,String file) {
        if(!supports(s))return false;
        Translation result=translate(s);
        for(String issue:result.review()) {
            ToscaToAgaPhase1.writeLine(indent+"      # [Pending] WINDOW review: "+issue.replace('\n',' '));
            MigrationLog.warn("WINDOW review: "+issue+" | step="+s.getName());
            MigrationReport.registerUnprocessedModule(s.getModule(),s.getName()+" | "+issue,file);
        }
        for (String note : result.runtimeNotes()) {
            ToscaToAgaPhase1.writeLine(indent+"      # [Runtime] "+note.replace('\n',' '));
            if (MigrationLog.isEnabled()) System.err.println("[INFO] WINDOW runtime: "+note+" | step="+s.getName());
        }
        if(result.steps().stream().anyMatch(ProcessWindowStep::requiresWindowRuntime)) {
            ToscaToAgaPhase1.writeLine(indent+"      # Runtime erforderlich: WINDOW-Engine.");
            if (MigrationLog.isEnabled()) System.err.println("[INFO] WINDOW migrated; execution requires WINDOW engine | step="+s.getName());
        }
        DumperOptions opts=new DumperOptions();opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);opts.setSplitLines(false);
        Yaml yaml=new Yaml(opts);
        for(Map<String,Object> step:result.steps()) {
            ToscaToAgaPhase1.writeLine("");
            for(String line:AgateYamlFormatting.dump(yaml,List.of(step)).split("\\R"))ToscaToAgaPhase1.writeLine(indent+"      "+line);
        }
        return true;
    }
    private static boolean requiresWindowRuntime(Map<String,Object> step) {
        if("WINDOW".equals(step.get("type")))return true;
        for(String key:List.of("steps","elseSteps"))for(Object child:list(step.get(key)))
            if(requiresWindowRuntime(map(child)))return true;
        for(Object child:list(map(step.get("predicate")).get("all")))
            if(requiresWindowRuntime(map(child)))return true;
        return false;
    }
    public static Translation translate(CleanStep s) {
        Translation legacy=translateInternal(s);
        try {
            List<Map<String,Object>> normalized=AgateWindowDsl.normalize(legacy.steps());
            Set<String> resolved=new HashSet<>();
            collectResolvedMappings(normalized,resolved);
            List<String> notes=legacy.runtimeNotes().stream()
                    .filter(note->resolved.stream().noneMatch(id->note.contains("tosca:"+id+" ") || note.contains("tosca:"+id+")")))
                    .toList();
            return new Translation(normalized,legacy.review(),notes);
        }
        catch(IllegalArgumentException ex) {
            List<String> review=new ArrayList<>(legacy.review());review.add(ex.getMessage());
            return new Translation(List.of(),review,legacy.runtimeNotes());
        }
    }
    private static void collectResolvedMappings(List<Map<String,Object>> steps,Set<String> ids) {
        for(Map<String,Object> step:steps) {
            if(map(step.get("target")).containsKey("edgeRestore")) {
                String id=text(map(step.get("source")).get("moduleAttribute"));
                if(!id.isEmpty())ids.add(id);
            }
            for(String key:List.of("steps","elseSteps")) {
                List<Map<String,Object>> children=new ArrayList<>();
                for(Object child:list(step.get(key)))children.add(map(child));
                collectResolvedMappings(children,ids);
            }
        }
    }
    private static Translation translateInternal(CleanStep s) {
        List<Map<String,Object>> steps=new ArrayList<>();List<String> review=new ArrayList<>();
        if(s.getValues()==null)return new Translation(steps,List.of("window values missing"));
        for (StepValueDetails v : s.getValues()) {
            Map<String,Object> gui=map(v.getGui());
            Map<String,Object> nativeIf=map(gui.get("nativeIfContract"));
            if(!nativeIf.isEmpty()) {
                List<String> issues=new ArrayList<>();
                for(Object issue:list(gui.get("nativeIfReview"))) issues.add(text(issue));
                // Structure and actions are migrated. Locator binding belongs to runtime setup.
                return new Translation(List.of(nativeIf), List.of(), issues);
            }
            String issue=text(gui.get("migrationReview"));
            if(!issue.isEmpty()) return new Translation(List.of(), List.of(issue));
        }
        if(s.getValues().stream().anyMatch(v->"WinX".equalsIgnoreCase(text(map(map(v.getGui()).get("properties")).get("Engine"))))) {
            try {
                List<String> notes=new ArrayList<>();
                for(StepValueDetails v:s.getValues()) {
                    Map<String,Object> gui=map(v.getGui());String raw=text(gui.get("rawValue"));
                    if(raw.isEmpty()||raw.equals("{NULL}"))continue;
                    String mode=text(v.getActionMode()),type=text(gui.get("businessType"));String op;
                    if(Set.of("37","Input","Insert").contains(mode)) {
                        if(type.equalsIgnoreCase("Button") && raw.equalsIgnoreCase("X"))op="CLICK";
                        else if(type.equalsIgnoreCase("TextBox")||type.equalsIgnoreCase("ComboBox"))op="FILL";
                        else throw new IllegalArgumentException("unsupported WinX input");
                    }else if(Set.of("69","Verify","101","WaitOn").contains(mode) && text(v.getActionProperty()).equalsIgnoreCase("Exists") && Set.of("True","False").contains(raw))op=Set.of("101","WaitOn").contains(mode)?"WAIT":"ASSERT";
                    else throw new IllegalArgumentException("unsupported WinX action");
                    String caption=text(map(gui.get("windowProperties")).get("Caption"));if(caption.isEmpty())throw new IllegalArgumentException("WinX owning Caption missing");
                    Map<String,Object> out=base(s,text(gui.get("valueSurrogate")),op);
                    out.put("window",Map.of("titleGlob",caption));out.put("target",Map.of("locatorRef","tosca:"+text(gui.get("moduleAttributeSurrogate"))));
                    out.put("source",gui);
                    if(op.equals("FILL"))out.put("value",Map.of("syntax","tosca","expression",ToscaValueTranslator.translateToscaValues(raw)));
                    if(op.equals("ASSERT")||op.equals("WAIT"))out.put("assertion",Map.of("type",raw.equals("True")?"EXISTS":"NOT_EXISTS"));
                    condition(out,s.getCondition(),v.getxCondition());steps.add(out);
                    notes.add("WinX locator binding required: "+out.get("target"));
                }
                return new Translation(steps,List.of(),notes);
            }catch(IllegalArgumentException ex){return new Translation(List.of(),List.of(ex.getMessage()));}
        }
        if(s.getValues().stream().anyMatch(v->"UIA".equalsIgnoreCase(text(map(map(v.getGui()).get("properties")).get("Engine"))))) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper=new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String,Object> raw=new LinkedHashMap<>();raw.put("ObjectClass","XTestStep");raw.put("Surrogate",s.getSurrogate());raw.put("ModuleName",s.getModule());raw.put("Attributes",Map.of());
                List<Map<String,Object>> parameters=new ArrayList<>();
                for(StepValueDetails v:s.getValues()) {
                    if(v.getxCondition()!=null&&!v.getxCondition().isBlank())throw new IllegalArgumentException("native value condition requires mapping");
                    Map<String,Object> p=new LinkedHashMap<>();p.put("Gui",v.getGui());p.put("Value",v.getValue());p.put("ActionMode",v.getActionMode());p.put("ActionProperty",v.getActionProperty());p.put("Operator",v.getOperator());parameters.add(p);
                }
                raw.put("Parameters",parameters);List<String> notes=new ArrayList<>();
                List<Map<String,Object>> result=at.co.svc.tosca.testcases.NativeWindowIfContract.nativeSteps(mapper.valueToTree(raw),false,notes,Map.of());
                for(Map<String,Object> action:result)condition(action,s.getCondition());
                return new Translation(result,List.of(),notes);
            } catch(IllegalArgumentException ex) {return new Translation(List.of(),List.of(ex.getMessage()));}
        }
        if("TBox Send Keys".equalsIgnoreCase(s.getModule())) {
            try {
                Map<String,String> parameters=new LinkedHashMap<>();
                for(StepValueDetails v:s.getValues()) {
                    String raw=text(v.getValue());if(raw.isEmpty()||raw.equals("{NULL}"))continue;
                    if(!Set.of("37","Input","Insert").contains(text(v.getActionMode())) || !text(v.getxCondition()).isEmpty())throw new IllegalArgumentException("unsupported Send Keys parameter");
                    if(!Set.of("Caption","Keys").contains(text(v.getName())) || parameters.putIfAbsent(v.getName(),raw)!=null)throw new IllegalArgumentException("unknown or duplicate Send Keys parameter");
                }
                if(!parameters.containsKey("Keys"))throw new IllegalArgumentException("Send Keys requires Keys");
                Map<String,Object> out=base(s,"keys","PRESS");
                if(parameters.containsKey("Caption"))out.put("window",Map.of("titleGlob",ToscaValueTranslator.translateToscaValues(parameters.get("Caption"))));
                else out.put("window",Map.of("foreground",true));
                // Keep Tosca key grammar explicitly; do not interpret it as Playwright grammar.
                out.put("keys",Map.of("syntax","tosca-sendkeys","value",ToscaValueTranslator.translateToscaValues(parameters.get("Keys"))));
                condition(out,s.getCondition());steps.add(out);
            }catch(IllegalArgumentException ex){review.add(ex.getMessage());}
        } else if("TBox Window Operation".equalsIgnoreCase(s.getModule())) {
            try {
                String caption=null;List<StepValueDetails> operations=new ArrayList<>();
                for(StepValueDetails v:s.getValues()) {
                    String raw=text(v.getValue());if(raw.isEmpty()||raw.equals("{NULL}"))continue;
                    String name=text(v.getName());
                    String mode=text(v.getActionMode());
                    boolean input=Set.of("37","Insert","Input").contains(mode);
                    // TBox Caption is a window selector; its scanned default is Verify.
                    // Operation remains an Input parameter, even when Caption uses Verify.
                    boolean captionVerify=name.equalsIgnoreCase("Caption")
                            && Set.of("69","Verify").contains(mode)
                            && Set.of("1","Equals").contains(text(v.getOperator()))
                            && text(v.getActionProperty()).isEmpty();
                    boolean operationWait=name.equalsIgnoreCase("Operation") && (Set.of("101","WaitOn","69","Verify").contains(mode)
                            || "101".equals(text(map(v.getGui()).get("rawActionMode"))))
                            && (Set.of("Wait On Open","Wait On Close").contains(raw)
                                || (Set.of("69","Verify").contains(mode) && Set.of("Verify Window Exists","Verify Window Does Not Exist").contains(raw)))
                            && "1".equals(text(v.getOperator())) && text(v.getActionProperty()).isEmpty();
                    if(!input&&!captionVerify&&!operationWait)throw new IllegalArgumentException("unsupported window parameter action mode: "+name+"="+mode);
                    if(name.equalsIgnoreCase("Caption")) {if(caption!=null)throw new IllegalArgumentException("duplicate Caption");caption=raw;}
                    else if(name.equalsIgnoreCase("Operation"))operations.add(v);
                    else throw new IllegalArgumentException("unsupported window parameter: "+name);
                    if(!text(v.getxCondition()).isEmpty())throw new IllegalArgumentException("parameter-level window condition requires mapping");
                }
                if(caption==null||operations.isEmpty())throw new IllegalArgumentException("Caption and Operation are required");
                for(int i=0;i<operations.size();i++) {
                    String operation=text(operations.get(i).getValue());String op=switch(operation) {
                        case "Close"->"CLOSE";case "Bring To Front"->"ACTIVATE";case "Try Bring To Front"->"TRY_ACTIVATE";
                        case "Maximize"->"MAXIMIZE";case "Minimize"->"MINIMIZE";case "Normal"->"RESTORE";
                        case "Verify Window Exists","Verify Window Does Not Exist"->"ASSERT";
                        case "Wait On Open","Wait On Close"->"WAIT";
                        default->throw new IllegalArgumentException("unsupported window operation: "+operation);
                    };
                    Map<String,Object> out=base(s,"window_"+i,op);
                    out.put("window",Map.of("titleGlob",ToscaValueTranslator.translateToscaValues(caption)));
                    if(op.equals("ASSERT")||op.equals("WAIT"))out.put("assertion",Map.of("type",Set.of("Verify Window Exists","Wait On Open").contains(operation)?"EXISTS":"NOT_EXISTS"));
                    condition(out,s.getCondition());steps.add(out);
                }
            } catch(IllegalArgumentException ex) {steps.clear();review.add(ex.getMessage());}
        } else {
            for(StepValueDetails v:s.getValues()) {
                Map<String,Object> gui=map(v.getGui()),props=map(gui.get("properties"));
                if(!"AnyUI".equalsIgnoreCase(text(props.get("Engine"))))continue;
                String raw=text(gui.get("rawValue"));if(raw.isEmpty()||raw.equals("{NULL}"))continue;
                String name=text(gui.get("name"));
                if (Boolean.getBoolean("agate.migration.window.trace")) {
                    String filter=System.getProperty("agate.migration.window.trace.filter", "");
                    if(filter.isEmpty() || (text(s.getName())+" "+text(s.getModule())).contains(filter)) {
                        System.err.println("[WINDOW TRACE] step="+s.getSurrogate()+" module="+s.getModule()
                                +" control="+name+" valueId="+gui.get("valueSurrogate")
                                +" mode="+v.getActionMode()+" actionProperty="+v.getActionProperty()
                                +" properties="+props+" window="+gui.get("windowProperties")
                                +" ancestors="+gui.get("attributeParents")
                                +" condition="+s.getCondition());
                    }
                }
                try {
                    if(!text(gui.get("disabledDescription")).isEmpty())throw new IllegalArgumentException("disabled native value requires review");
                    Map<String,Object> windowProps=map(gui.get("windowProperties"));String caption=text(windowProps.get("Caption"));
                    if(caption.isEmpty())throw new IllegalArgumentException("owning native window Caption missing");
                    // ModuleAttribute.Name is a Tosca business name, not a verified UI Automation Name.
                    Map<String,Object> target=new LinkedHashMap<>();
                    for(String key:List.of("AutomationId","Name","ControlType"))if(!text(props.get(key)).isEmpty())target.put(Character.toLowerCase(key.charAt(0))+key.substring(1),props.get(key));
                    if(!target.containsKey("automationId")&&!target.containsKey("name"))throw new IllegalArgumentException("no active native Name/AutomationId; Tosca business name cannot be used as UI locator");
                    if(!list(gui.get("attributeParents")).isEmpty())throw new IllegalArgumentException("native ancestor scope requires mapping");
                    for(String key:props.keySet())if(!Set.of("Engine","Name","AutomationId","ControlType","BusinessAssociation","WaitAfter").contains(key))throw new IllegalArgumentException("unsupported native locator property: "+key);
                    String mode=text(v.getActionMode());String op;
                    if(Set.of("37","Input","Insert").contains(mode)&&raw.equalsIgnoreCase("{Click}"))op="CLICK";
                    else if(Set.of("69","101","Verify","WaitOn").contains(mode)&&"Exists".equalsIgnoreCase(text(v.getActionProperty()))&&Set.of("True","False").contains(raw))op=Set.of("101","WaitOn").contains(mode)?"WAIT":"ASSERT";
                    else throw new IllegalArgumentException("unsupported AnyUI action");
                    Map<String,Object> out=base(s,text(gui.get("valueSurrogate")),op);out.put("window",Map.of("titleGlob",caption));out.put("target",target);
                    if(!op.equals("CLICK"))out.put("assertion",Map.of("type",raw.equals("True")?"EXISTS":"NOT_EXISTS"));
                    if(!text(props.get("WaitAfter")).isEmpty())throw new IllegalArgumentException("AnyUI WaitAfter units require confirmation");
                    condition(out,s.getCondition(),v.getxCondition());steps.add(out);
                } catch(IllegalArgumentException ex) {review.add(name+": "+ex.getMessage());}
            }
        }
        return new Translation(steps,review);
    }
    private static Map<String,Object> base(CleanStep s,String suffix,String op) {
        Map<String,Object> out=new LinkedHashMap<>();out.put("id",text(s.getSurrogate())+"_"+suffix);out.put("type","WINDOW");out.put("op",op);return out;
    }
    private static void condition(Map<String,Object> out,String... conditions) {
        List<String> parts=new ArrayList<>();for(String c:conditions)if(c!=null&&!c.isBlank())parts.add("("+c+")");
        if(!parts.isEmpty())out.put("condition",ToscaValueTranslator.translateToscaValues(String.join(" AND ",parts)));
    }
    private static String text(Object o){return Objects.toString(o,"");}
    @SuppressWarnings("unchecked") private static Map<String,Object> map(Object o){return o instanceof Map?(Map<String,Object>)o:Map.of();}
    private static List<?> list(Object o){return o instanceof List<?> l?l:List.of();}
}
