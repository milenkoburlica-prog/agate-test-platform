package at.co.svc.agate.ai;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CloudEvidenceSanitizerTest {
 @Test void hidesSecretsAndIdentifiers() {
  String in="PIN: 1111\ncardToken: geheim\nSVNR=4560048158\nserver=10.134.242.129";
  String out=CloudEvidenceSanitizer.sanitize(in);
  assertFalse(out.contains("1111")); assertFalse(out.contains("geheim"));
  assertFalse(out.contains("4560048158"));assertFalse(out.contains("10.134.242.129"));
 }
 @Test void limitsCloudEvidence() {assertTrue(CloudEvidenceSanitizer.sanitize("x".repeat(200000)).length()<15000);}
}
