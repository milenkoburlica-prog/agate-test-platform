package at.co.svc.tosca.testcases;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;
import at.co.svc.aga.transformator.utils.ProcessGuiStep;

/** Declarative WINDOW IF migration contract. Not a Windows runtime implementation. */
public final class NativeWindowIfContract {
    private NativeWindowIfContract() {}
    public record Result(Map<String,Object> step, List<String> review) {}

    public static Result translate(JsonNode control, Map<String,JsonNode> index, String parentCondition) {
        List<String> review = new ArrayList<>();
        try {
            Map<String,Object> step = translateIf(control,index,review,new HashSet<>());
            if(parentCondition != null && !parentCondition.isBlank()) step.put("condition",parentCondition);
            return new Result(step,review);
        } catch (IllegalArgumentException ex) {
            return new Result(Map.of(),List.of(ex.getMessage()));
        }
    }
    private static Map<String,Object> translateIf(JsonNode control, Map<String,JsonNode> index,
            List<String> review, Set<String> stack) {
        String statement=control.path("Attributes").path("StatementType").asText("1");
        boolean loop=Set.of("2","3").contains(statement);
        if(loop) {
            if(!control.path("Attributes").path("Condition").asText().isBlank() || !control.path("Attributes").path("Repetition").asText().isBlank())throw new IllegalArgumentException("loop condition/repetition requires mapping");
        } else requirePlainContainer(control);
        String id = control.path("Surrogate").asText();
        if(!stack.add(id)) throw new IllegalArgumentException("cyclic native IF: "+id);
        try {
            List<Map<String,Object>> predicates = new ArrayList<>();
            List<Map<String,Object>> thenSteps = new ArrayList<>(), elseSteps = new ArrayList<>();
            for(JsonNode folderId:control.path("Assocs").path("ControlFlowFolders")) {
                JsonNode folder=required(index,folderId.asText());
                requirePlainContainer(folder);
                String kind=folder.path("Attributes").path("StatementType").asText();
                if(!Set.of("0","1","2").contains(kind)) throw new IllegalArgumentException("unsupported IF folder kind: "+kind);
                for(JsonNode itemId:folder.path("Assocs").path("Items")) {
                    JsonNode item=required(index,itemId.asText());
                    if(disabled(item)) continue;
                    if(kind.equals("0")) collectPredicates(item,index,predicates,review,new HashSet<>());
                    else collectBranch(item,index,kind.equals("1")?thenSteps:elseSteps,review,stack);
                }
            }
            if(predicates.isEmpty()) throw new IllegalArgumentException("native IF has no supported predicate");
            Map<String,Object> out=base(id,loop?(statement.equals("3")?"DO_WHILE":"WHILE"):"IF");
            if(loop) {
                String maximum=control.path("Attributes").path("MaximumRepetitions").asText();
                if(!maximum.matches("[1-9][0-9]*"))throw new IllegalArgumentException("native loop needs positive MaximumRepetitions");
                out.put("maxIterations",Integer.parseInt(maximum));
                if(!elseSteps.isEmpty())throw new IllegalArgumentException("native loop has unexpected else branch");
            }
            if(predicates.stream().allMatch(p -> "GUI".equals(p.get("type"))))out.put("type","GUI");
            out.put("predicate",Map.of("all",predicates));
            out.put("steps",thenSteps);
            if(!elseSteps.isEmpty()) out.put("elseSteps",elseSteps);
            return out;
        } finally { stack.remove(id); }
    }
    private static void collectBranch(JsonNode item,Map<String,JsonNode> index,List<Map<String,Object>> out,
            List<String> review,Set<String> stack) {
        requirePlainContainer(item);
        String kind=item.path("ObjectClass").asText();
        if(kind.equals("XTestStep")) out.addAll(nativeSteps(item,false,review,index));
        else if(kind.equals("TestCaseControlFlowItem")) out.add(translateIf(item,index,review,stack));
        else if(kind.equals("TestStepFolder")) {
            String id=item.path("Surrogate").asText();
            if(!stack.add(id)) throw new IllegalArgumentException("cyclic native folder: "+id);
            try {
                for(JsonNode child:item.path("Assocs").path("Items")) {
                    JsonNode node=required(index,child.asText());
                    if(!disabled(node)) collectBranch(node,index,out,review,stack);
                }
            } finally {stack.remove(id);}
        } else throw new IllegalArgumentException("unsupported native IF branch item: "+kind);
    }
    public static List<Map<String,Object>> nativeSteps(JsonNode step,boolean predicate,List<String> review,Map<String,JsonNode> index) {
        requirePlainContainer(step);
        if(!"XTestStep".equals(step.path("ObjectClass").asText())) throw new IllegalArgumentException("native predicate is not XTestStep");
        String module=moduleName(step,index);
        if(module.equalsIgnoreCase("TBox Window Operation"))return windowOperation(step,predicate);
        if(!predicate && module.equalsIgnoreCase("TBox Wait")) {
            String duration=parameter(step,"Duration");
            if(!duration.matches("[0-9]+"))throw new IllegalArgumentException("unsupported wait duration: "+duration);
            Map<String,Object> wait=new LinkedHashMap<>();wait.put("id",step.path("Surrogate").asText());wait.put("type","WAIT");wait.put("value",duration);return List.of(wait);
        }
        if(!predicate && module.equalsIgnoreCase("CloseBrowser")) {
            String title=parameter(step,"Title");
            if(!title.isBlank() && !title.equals("{NULL}"))throw new IllegalArgumentException("CloseBrowser Title requires mapping");
            Map<String,Object> close=new LinkedHashMap<>();close.put("id",step.path("Surrogate").asText()+"_close");close.put("type","GUI");close.put("op","EXEC");close.put("action","CLOSE_BROWSER");return List.of(close);
        }
        if(isHtmlStep(step))return htmlSteps(step,predicate,index);
        List<Map<String,Object>> out=new ArrayList<>();
        for(JsonNode p:step.path("Parameters")) {
            String value=p.path("Value").asText();
            if(value.isBlank()||value.equals("{NULL}")) continue;
            JsonNode gui=p.has("Gui")?p.path("Gui"):p.path("gui");
            if(!Set.of("anyui","uia").contains(gui.path("properties").path("Engine").asText().toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException("mixed or non-native predicate/action requires mapping");
            if(!gui.path("disabledDescription").asText().isBlank()) continue;
            if(!p.path("XCondition").asText().isBlank()) throw new IllegalArgumentException("native parameter condition requires mapping");
            String mode=p.path("ActionMode").asText(), operator=p.path("Operator").asText();
            String op;
            String property=p.path("ActionProperty").asText();
            boolean booleanProperty=Set.of("exists","visible","enabled").contains(property.toLowerCase(Locale.ROOT))
                    && Set.of("True","False").contains(value);
            boolean verify=Set.of("Verify","69","WaitOn","101").contains(mode);
            boolean input=Set.of("Input","Insert","37").contains(mode);
            if(predicate) {
                if(!verify||!booleanProperty||!Set.of("1","Equals","2").contains(operator))
                    throw new IllegalArgumentException("unsupported native IF comparison: "+mode+" / "+operator);
                op=property.toUpperCase(Locale.ROOT);
            } else if(input && (value.equalsIgnoreCase("{Click}") || value.equalsIgnoreCase("X") && Set.of("button","genericgui").contains(gui.path("businessType").asText().toLowerCase(Locale.ROOT))))op="CLICK";
            else if(input && Set.of("{UP}","{ENTER}").contains(value.toUpperCase(Locale.ROOT)))op="PRESS";
            else if(input && "TextBox".equalsIgnoreCase(gui.path("businessType").asText()))op="FILL";
            else if(verify && (booleanProperty || property.equalsIgnoreCase("Name")))op=Set.of("WaitOn","101").contains(mode)?"WAIT":"ASSERT";
            else throw new IllegalArgumentException("unsupported native IF branch action: "+mode);
            String valueId=gui.path("valueSurrogate").asText(p.path("TestStepValueSurrogate").asText());
            Map<String,Object> action=base(step.path("Surrogate").asText()+"_"+valueId,op);
            String caption=gui.path("windowProperties").path("Caption").asText();
            if(caption.isBlank())caption=gui.path("windowProperties").path("Name").asText();
            if(caption.isBlank()) throw new IllegalArgumentException("native window Caption missing");
            action.put("window",Map.of("titleGlob",caption));
            Map<String,Object> target=new LinkedHashMap<>();
            JsonNode props=gui.path("properties");
            for(String key:List.of("Name","AutomationId","ControlType")) {
                String v=props.path(key).asText();
                if(!v.isBlank())target.put(Character.toLowerCase(key.charAt(0))+key.substring(1),v);
            }
            boolean extra=false;
            Iterator<String> keys=props.fieldNames();
            while(keys.hasNext()) if(!Set.of("Engine","Name","AutomationId","ControlType","BusinessAssociation").contains(keys.next())) extra=true;
            if(!target.containsKey("name")&&!target.containsKey("automationId")
                    || !gui.path("attributeParents").isEmpty() || extra) {
                target.clear();
                String attr=gui.path("moduleAttributeSurrogate").asText();
                if(attr.isBlank())throw new IllegalArgumentException("native attribute identity missing");
                target.put("locatorRef","tosca:"+attr);
                String issue="native locator mapping required: tosca:"+attr+" ("+gui.path("name").asText()+")";
                if(!review.contains(issue))review.add(issue);
            }
            action.put("target",target);
            if(predicate) action.put("expected",Boolean.parseBoolean(value)^operator.equals("2"));
            else if(op.equals("FILL"))action.put("value",value);
            else if(op.equals("PRESS"))action.put("key",value.substring(1,value.length()-1));
            else if(op.equals("ASSERT")||op.equals("WAIT")) {
                if(!Set.of("0","1","2","Equals").contains(operator))throw new IllegalArgumentException("unsupported native assertion operator");
                if(booleanProperty) {
                    boolean wanted=Boolean.parseBoolean(value)^operator.equals("2");
                    String check=switch(property.toLowerCase(Locale.ROOT)){case "exists"->wanted?"EXISTS":"NOT_EXISTS";case "visible"->wanted?"VISIBLE":"HIDDEN";default->wanted?"ENABLED":"DISABLED";};
                    action.put("assertion",Map.of("type",check));
                } else action.put("assertion",Map.of("type",operator.equals("2")?"PROPERTY_NOT_EQUALS":"PROPERTY_EQUALS","property",property,"expected",value));
            }
            Map<String,Object> source=new LinkedHashMap<>();
            source.put("moduleAttribute",gui.path("moduleAttributeSurrogate").asText());
            source.put("businessName",gui.path("name").asText());
            source.put("properties",new ObjectMapper().convertValue(props,Map.class));
            source.put("attributeParents",new ObjectMapper().convertValue(gui.path("attributeParents"),Object.class));
            action.put("source",source);
            out.add(action);
        }
        if(out.isEmpty())throw new IllegalArgumentException("native IF item has no supported active values");
        return out;
    }
    private static boolean isHtmlStep(JsonNode step) {
        boolean found=false;
        for(JsonNode p:step.path("Parameters")) {
            JsonNode gui=p.has("Gui")?p.path("Gui"):p.path("gui");
            if(gui.isMissingNode())return false;
            if(!"Html".equalsIgnoreCase(gui.path("properties").path("Engine").asText()))return false;
            found=true;
        }
        return found;
    }
    private static List<Map<String,Object>> htmlSteps(JsonNode step,boolean predicate,Map<String,JsonNode> index) {
        CleanStep clean=new CleanStep();
        clean.setSurrogate(step.path("Surrogate").asText());
        clean.setName(step.path("Attributes").path("Name").asText());
        clean.setModule(moduleName(step,index));
        List<StepValueDetails> values=new ArrayList<>();
        ObjectMapper mapper=new ObjectMapper();
        for(JsonNode p:step.path("Parameters")) {
            if(!p.path("XCondition").asText().isBlank())throw new IllegalArgumentException("HTML parameter condition requires mapping");
            StepValueDetails v=new StepValueDetails();
            v.setGui(mapper.convertValue(p.has("Gui")?p.path("Gui"):p.path("gui"),Map.class));
            v.setValue(p.path("Value").asText());v.setActionMode(p.path("ActionMode").asText());
            v.setActionProperty(p.path("ActionProperty").asText());v.setOperator(p.path("Operator").asText());
            values.add(v);
        }
        clean.setValues(values);
        var translated=ProcessGuiStep.translate(clean);
        if(!translated.review().isEmpty())throw new IllegalArgumentException("HTML IF: "+String.join("; ",translated.review()));
        if(!predicate)return translated.steps();
        List<Map<String,Object>> result=new ArrayList<>();
        for(Map<String,Object> action:translated.steps()) {
            if(!"ASSERT".equals(action.get("op")))throw new IllegalArgumentException("HTML IF predicate must be a verification");
            Map<String,Object> assertion=(Map<String,Object>)action.get("assertion");
            String type=Objects.toString(assertion.get("type"),"");
            if(!Set.of("VISIBLE","HIDDEN","EXISTS","NOT_EXISTS","ENABLED","DISABLED").contains(type))
                throw new IllegalArgumentException("unsupported HTML IF predicate: "+type);
            Map<String,Object> condition=new LinkedHashMap<>(action);condition.remove("assertion");condition.remove("sensitive");
            condition.put("op",switch(type){case "HIDDEN"->"VISIBLE";case "NOT_EXISTS"->"EXISTS";case "DISABLED"->"ENABLED";default->type;});
            condition.put("expected",assertion.containsKey("expected")?assertion.get("expected"):
                    !Set.of("HIDDEN","NOT_EXISTS","DISABLED").contains(type));
            if(assertion.containsKey("negate"))condition.put("negate",assertion.get("negate"));
            result.add(condition);
        }
        if(result.isEmpty())throw new IllegalArgumentException("HTML IF has no active predicate");
        return result;
    }
    private static String moduleName(JsonNode step,Map<String,JsonNode> index) {
        JsonNode module=index.get(step.path("Assocs").path("Module").path(0).asText());
        return module==null?step.path("ModuleName").asText():module.path("Attributes").path("Name").asText();
    }
    private static String parameter(JsonNode step,String name) {
        String result="";
        for(JsonNode p:step.path("Parameters")) {
            String key=p.path("Gui").path("name").asText(p.path("ToscaPath").asText());
            if(!key.equalsIgnoreCase(name))continue;
            String value=p.path("Value").asText();
            if(value.isBlank()||value.equals("{NULL}"))continue;
            if(!result.isEmpty())throw new IllegalArgumentException("duplicate parameter "+name);
            if(!Set.of("Input","Insert","37").contains(p.path("ActionMode").asText()))throw new IllegalArgumentException("unsupported parameter mode: "+name);
            result=value;
        }
        return result;
    }
    private static List<Map<String,Object>> windowOperation(JsonNode step,boolean predicate) {
        String caption=parameter(step,"Caption"),operation=parameter(step,"Operation");
        if(caption.isBlank())throw new IllegalArgumentException("window Caption missing");
        for(JsonNode p:step.path("Parameters")) {
            String raw=p.path("Value").asText();if(raw.isBlank()||raw.equals("{NULL}"))continue;
            String key=p.path("Gui").path("name").asText(p.path("ToscaPath").asText());
            if(!Set.of("Caption","Operation").contains(key))throw new IllegalArgumentException("unsupported window parameter: "+key);
        }
        Map<String,Object> action=base(step.path("Surrogate").asText()+"_window_0",predicate?"EXISTS":"CLOSE");
        action.put("window",Map.of("titleGlob",caption));
        if(predicate) {
            if(!Set.of("Verify Window Exists","Verify Window Does Not Exist").contains(operation))throw new IllegalArgumentException("unsupported window predicate: "+operation);
            action.put("expected",operation.equals("Verify Window Exists"));
        } else if(!operation.equals("Close"))throw new IllegalArgumentException("unsupported window branch operation: "+operation);
        return List.of(action);
    }
    private static void collectPredicates(JsonNode item,Map<String,JsonNode> index,List<Map<String,Object>> out,List<String> review,Set<String> seen) {
        if(disabled(item))return;
        requirePlainContainer(item);
        String id=item.path("Surrogate").asText();if(!seen.add(id))throw new IllegalArgumentException("cyclic predicate folder");
        try {
            if(item.path("ObjectClass").asText().equals("TestStepFolder")) {
                for(JsonNode child:item.path("Assocs").path("Items"))collectPredicates(required(index,child.asText()),index,out,review,seen);
            } else out.addAll(nativeSteps(item,true,review,index));
        } finally {seen.remove(id);}
    }
    public static boolean hasWindowPredicate(JsonNode control,Map<String,JsonNode> index) {
        Deque<JsonNode> queue=new ArrayDeque<>();Set<String> seen=new HashSet<>();
        for(JsonNode id:control.path("Assocs").path("ControlFlowFolders")) {
            JsonNode f=index.get(id.asText());if(f!=null && f.path("Attributes").path("StatementType").asText().equals("0"))queue.add(f);
        }
        while(!queue.isEmpty()) {
            JsonNode n=queue.removeFirst();if(!seen.add(n.path("Surrogate").asText()))continue;
            if(moduleName(n,index).equalsIgnoreCase("TBox Window Operation") || isHtmlStep(n))return true;
            for(JsonNode p:n.path("Parameters"))if(Set.of("anyui","uia").contains(p.path("Gui").path("properties").path("Engine").asText().toLowerCase(Locale.ROOT)))return true;
            for(JsonNode child:n.path("Assocs").path("Items")){JsonNode c=index.get(child.asText());if(c!=null)queue.add(c);}
        }
        return false;
    }
    private static void requirePlainContainer(JsonNode node) {
        if(!node.path("Attributes").path("Condition").asText().isBlank()
                || !node.path("Attributes").path("Repetition").asText().isBlank()
                || !node.path("Attributes").path("MaximumRepetitions").asText().isBlank())
            throw new IllegalArgumentException("native IF container condition/repetition requires mapping: "
                    +node.path("Surrogate").asText());
    }
    private static JsonNode required(Map<String,JsonNode> index,String id) {
        JsonNode node=index.get(id);if(node==null)throw new IllegalArgumentException("native IF node missing: "+id);return node;
    }
    private static boolean disabled(JsonNode n){return !n.path("Attributes").path("DisabledDescription").asText().isBlank();}
    private static Map<String,Object> base(String id,String op){Map<String,Object> out=new LinkedHashMap<>();out.put("id",id);out.put("type","WINDOW");out.put("op",op);return out;}
}
