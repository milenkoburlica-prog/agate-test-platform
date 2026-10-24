package at.co.svc.agate.ai;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DiagnosticPromptTest {
 @Test void promptIstDeutschUndBehältTestdaten() {
  String text = new DiagnosticPrompt().userMessage(new DiagnosticRequest("SOAP XPath was not found", "Prüfe den Fehler"));
  assertTrue(text.contains("SOAP XPath was not found"));
  assertTrue(text.contains("Prüfe den Fehler"));
  assertTrue(DiagnosticPrompt.SYSTEM.contains("KONKRETE KORREKTUR"));
  assertTrue(DiagnosticPrompt.SYSTEM.contains("falschen Elternknoten"));
 }
}
