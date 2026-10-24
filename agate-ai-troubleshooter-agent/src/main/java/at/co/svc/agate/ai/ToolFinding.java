package at.co.svc.agate.ai;
import java.util.List;
public record ToolFinding(String engine, String summary, List<String> evidence,
                          List<String> recommendations, String status) {}
