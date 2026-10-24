package at.co.svc.agate.ai;

import java.util.*;
import java.util.regex.*;

/** Parses AGATE DSL step blocks, not generic shell exit codes.
 * This parser only reads supplied text. It does not execute any step. */
public final class AgateExecutionParser {
    public record Step(String engine, String op, String response, String condition,
                       String command, String name, String status, String reason,
                       int startLine, int endLine, boolean skipped, String skipReason) {}
    public record Failure(String engine, String reason, String response, String reusable,
                          int line, Step failedStep, List<Step> steps) {}
    private static final Pattern TYPE=Pattern.compile("^\\s*>>> DSL\\s+- type: ([A-Z]+)\\s*$");
    private static final Pattern FIELD=Pattern.compile("^\\s*>>> DSL\\s+(op|response|condition|command|name):\\s*(.*)$");
    private static final Pattern FINISH2=Pattern.compile("^\\s*>>> ([A-Z]+) STEP FINISHED\\s*\\| Status: (SUCCESS|FAILED|SKIPPED).*",Pattern.CASE_INSENSITIVE);
    private static final Pattern REASON=Pattern.compile("^\\s*Reason\\s*:\\s*(.*)$");
    private static final Pattern RESPONSE=Pattern.compile("^\\s*Response\\s*:\\s*(.*)$");
    private static final Pattern REUSABLE=Pattern.compile("^\\s*Reusable\\s*:\\s*(.*)$");
    private static final Pattern SKIPPED=Pattern.compile("^\\s*>>> SKIPPED \\(Condition false: (.*)\\)");
    private static final Pattern ERROR=Pattern.compile("^\\s*>>> (SQL|SOAP|REST|GUI|CMD|BUFFER) STEP FAILED \\| ERROR: (.*)$");
    private static final Pattern SUMMARY=Pattern.compile("^REASON:\\s*(.*)$");

    private static final class Draft {
        String engine,op,response,condition,command,name,status,reason,skipReason;
        boolean skipped;
        int start,end;
        Step freeze(){return new Step(engine,op,response,condition,command,name,status,reason,start,end,skipped,skipReason);}
    }
    public Optional<Failure> findFailure(String log) {
        if (log == null || !log.contains(">>> DSL")) return Optional.empty();
        String[] lines=log.split("\\R");
        List<Step> steps=new ArrayList<>(); Draft current=null;
        String pendingReason=null, pendingResponse=null, reusable=null, summaryReason=null;
        int failureLine=-1; String failureEngine=null;
        for(int i=0;i<lines.length;i++) {
            String line=lines[i];
            Matcher m=TYPE.matcher(line);
            if(m.matches()) {
                if(current!=null && current.status==null) {current.end=i; steps.add(current.freeze());}
                current=new Draft();current.engine=m.group(1);current.start=i+1;current.end=i+1;
                continue;
            }
            m=FIELD.matcher(line);
            if(current!=null && m.matches()) {
                String key=m.group(1),v=unquote(m.group(2).strip());
                switch(key){case "op" -> current.op=v;case "response" -> current.response=v;
                    case "condition" -> current.condition=v;case "command" -> current.command=v;case "name" -> current.name=v;}
            }
            m=SKIPPED.matcher(line);
            if(current!=null && m.find()){current.skipped=true;current.skipReason=m.group(1);}
            m=ERROR.matcher(line);
            if(current!=null && m.find() && m.group(1).equals(current.engine)){current.reason=m.group(2);}
            m=FINISH2.matcher(line);
            if(current!=null && m.matches() && m.group(1).equals(current.engine)){
                current.status=m.group(2);current.end=i+1;
                if("FAILED".equals(current.status) && (failureEngine==null || "CALL".equals(failureEngine))) {
                    failureEngine=current.engine;failureLine=i+1;
                }
                steps.add(current.freeze()); current=null;
            }
            m=REASON.matcher(line);
            if(m.matches() && pendingReason==null)pendingReason=m.group(1);
            m=RESPONSE.matcher(line);
            if(m.matches() && pendingResponse==null)pendingResponse=m.group(1);
            m=REUSABLE.matcher(line);
            if(m.matches() && reusable==null)reusable=m.group(1);
            m=SUMMARY.matcher(line);
            if(m.matches())summaryReason=m.group(1);
        }
        Step failed=null;
        for(Step step:steps)if("FAILED".equals(step.status()) && !"CALL".equals(step.engine())){failed=step;break;}
        if(failed==null)return Optional.empty();
        String response=failed.response()!=null?failed.response():pendingResponse;
        String reason=failed.reason()!=null?failed.reason():pendingReason!=null?pendingReason:summaryReason;
        return Optional.of(new Failure(failed.engine(),reason,response,reusable,failed.endLine(),failed,List.copyOf(steps)));
    }
    private static String unquote(String s){return s.length()>=2 && ((s.startsWith("\"")&&s.endsWith("\""))||(s.startsWith("'")&&s.endsWith("'")))?s.substring(1,s.length()-1):s;}
}
