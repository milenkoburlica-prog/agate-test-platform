package at.co.svc.agate.ai;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class SoapResponseCorrelationTest {
 @Test void existingElementMustNotBeCalledMissing() throws Exception {
   String log=Files.readString(Path.of("examples/demo-logs/11-soap-reported-not-found.log"));
   var f=new SoapResponseCorrelation().inspect(log).orElseThrow();
   assertEquals(1,f.hits());
   assertEquals("URSACHE_EINGEGRENZT",f.status());
   assertTrue(f.explanation().contains("intern"));
 }
 @Test void absentElementIsReportedAsAbsent() throws Exception {
   String log=Files.readString(Path.of("examples/demo-logs/11-soap-reported-not-found.log"))
     .replace("/*[local-name()='fotoInformation']", "/*[local-name()='fehlendesElement']");
   assertEquals(0,new SoapResponseCorrelation().inspect(log).orElseThrow().hits());
 }
}
