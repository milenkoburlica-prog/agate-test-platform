package at.co.svc.aga.transformator.utils;

import java.util.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

/** Migrates the supplied ExecuteSql SET implementation to the existing CMD engine. */
public final class ProcessExecuteSqlScript {
    private ProcessExecuteSqlScript() {}
    private static final List<String> PARAMETERS=List.of("Server Endpoint","User Name","Password",
            "SQL Plus Executeable","SQL Script Path","Arguments","To File");
    public record Translation(List<Map<String,Object>> steps,List<String> review) {}
    public static boolean supports(CleanStep step) {
        return step.getModule()!=null && step.getModule().trim().equalsIgnoreCase("SET - ExecuteSQL Scripte");
    }
    public static boolean process(CleanStep step,String indent,String file) {
        if(!supports(step))return false;
        Translation translated=translate(step);
        for(String issue:translated.review()) {
            ToscaToAgaPhase1.writeLine(indent+"      # [Pending] ExecuteSQL review: "+issue);
            MigrationLog.warn("ExecuteSQL review: "+issue+" | step="+step.getName());
            MigrationReport.registerUnprocessedModule(step.getModule(),step.getName()+" | "+issue,file);
        }
        DumperOptions opts=new DumperOptions();opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);opts.setSplitLines(false);
        for(Map<String,Object> output:translated.steps()) {
            ToscaToAgaPhase1.writeLine("");
            for(String line:new Yaml(opts).dump(List.of(output)).split("\\R"))
                ToscaToAgaPhase1.writeLine(indent+"      "+line);
        }
        return true;
    }
    public static Translation translate(CleanStep step) {
        try {
            Map<String,String> params=new LinkedHashMap<>();
            if(step.getValues()==null)throw new IllegalArgumentException("module parameters missing");
            for(StepValueDetails value:step.getValues()) {
                String name=Objects.toString(value.getName(),"").trim();
                String raw=value.getValue();
                if(!PARAMETERS.contains(name)) {
                    if(raw!=null&&!raw.isBlank()&&!raw.equals("{NULL}"))throw new IllegalArgumentException("unsupported active parameter: "+name);
                    continue;
                }
                if(!Set.of("Input","Insert","37").contains(Objects.toString(value.getActionMode(),"")))
                    throw new IllegalArgumentException("unsupported action mode for parameter: "+name);
                if(value.getxCondition()!=null&&!value.getxCondition().isBlank())
                    throw new IllegalArgumentException("parameter condition requires mapping: "+name);
                if(params.containsKey(name))throw new IllegalArgumentException("duplicate parameter: "+name);
                if(raw==null||raw.equals("{NULL}")||(!name.equals("Arguments")&&raw.isBlank()))
                    throw new IllegalArgumentException("required parameter is empty: "+name);
                if(raw.indexOf('\n')>=0||raw.indexOf('\r')>=0)
                    throw new IllegalArgumentException("multiline parameter requires mapping: "+name);
                params.put(name,ToscaValueTranslator.translateToscaValues(raw));
            }
            for(String name:PARAMETERS)if(!params.containsKey(name))throw new IllegalArgumentException("required parameter missing: "+name);
            if(params.get("Password").matches("\\*+"))throw new IllegalArgumentException("Password is masked; original value or a buffer/environment reference is required");
            for(String name:List.of("SQL Plus Executeable","SQL Script Path","Server Endpoint","User Name","Password"))
                if(params.get(name).contains("\""))throw new IllegalArgumentException("embedded double quote requires mapping: "+name);
            String command=quote(params.get("SQL Plus Executeable"))+" -L -S "
                    +quote(params.get("User Name")+"/"+params.get("Password")+"@"+params.get("Server Endpoint"))
                    +" @"+quote(params.get("SQL Script Path"));
            if(!params.get("Arguments").isEmpty())command+=" "+params.get("Arguments");
            Map<String,Object> out=new LinkedHashMap<>();
            String id=Objects.toString(step.getSurrogate(),"");
            out.put("id",id.isBlank()?"execute_sql_script":id);
            out.put("type","CMD");out.put("op","EXEC");
            if(step.getCondition()!=null&&!step.getCondition().isBlank())out.put("condition",step.getCondition());
            out.put("command",command);
            out.put("outputFile",params.get("To File"));
            out.put("ignoreExitCode",true);
            return new Translation(List.of(out),List.of());
        } catch(IllegalArgumentException error) {
            return new Translation(List.of(),List.of(error.getMessage()));
        }
    }
    private static String quote(String value){return "\""+value+"\"";}
}
