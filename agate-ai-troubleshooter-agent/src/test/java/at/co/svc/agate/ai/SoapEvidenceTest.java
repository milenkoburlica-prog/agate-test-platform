package at.co.svc.agate.ai;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
class SoapEvidenceTest {
 @Test void falscherElternknotenWirdErkannt() throws Exception {
   String log = Files.readString(Path.of("examples/beispiel-falscher-svtcode-pfad.log"));
   String result = new SoapEvidence().inspect(log);
   assertTrue(result.contains("SOAP_PRÜFUNG: XML_GELESEN"), result);
   assertTrue(result.matches("(?s).*HAUPT_XPATH:.*?treffer=1.*"), result);
   assertTrue(result.matches("(?s).*CONSTRAINT_XPATH:.*?treffer=0.*"), result);
 }
 @Test void korrekterConstraintHatTreffer() throws Exception {
   String log = Files.readString(Path.of("examples/beispiel-falscher-svtcode-pfad.log"))
     .replace("/*[local-name()='versichertenDaten']/*[local-name()='svtCode']", "/*[local-name()='anspruchsDaten']/*[local-name()='svtCode']");
   String result = new SoapEvidence().inspect(log);
   assertTrue(result.matches("(?s).*CONSTRAINT_XPATH:.*?treffer=1.*"), result);
 }
 @Test void ohneAntwortKeineBehauptung() {
   assertTrue(new SoapEvidence().inspect("Fehler").contains("NICHT_VERFÜGBAR"));
 }
 @Test void verwehrtDoctype() {
   String log="<<< SOAP response:\n<!DOCTYPE foo [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]>\n<Envelope>&xxe;</Envelope>";
   assertFalse(new SoapEvidence().inspect(log).contains("XML_GELESEN"));
 }
}
