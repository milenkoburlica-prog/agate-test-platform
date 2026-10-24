package at.co.svc.agate.ai;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class EvidenceExplorerTest {
 @Test void skippedProducerAndFailedConsumerHaveGroundedLines() throws Exception {
  String log=Files.readString(Path.of("examples/demo-logs/09-tf080-sql-skipped.log"));
  var p=new AgateExecutionParser().findFailure(log).orElseThrow();
  var finding=new SqlResponseCorrelationTool().analyze(p);
  var view=new EvidenceExplorer().forFinding(log,finding);
  assertEquals("SQL · Fehleranalyse",view.title());
  assertFalse(view.steps().isEmpty());
  assertTrue(view.steps().stream().anyMatch(x->"FAILED".equals(x.status())));
  for(var step:view.steps()){assertTrue(step.startLine()>0);assertTrue(step.endLine()>=step.startLine());}
 }
}
