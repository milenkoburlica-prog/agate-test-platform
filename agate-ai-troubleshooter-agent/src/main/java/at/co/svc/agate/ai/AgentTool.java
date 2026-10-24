package at.co.svc.agate.ai;
/** Read-only analysis of supplied execution logs. No external commands or network calls. */
public interface AgentTool {
    String id();
    String description();
    ToolFinding analyze(String log);
}
