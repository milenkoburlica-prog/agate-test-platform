package at.co.svc.agate.ai;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RequestRouterTest {
  RequestRouter router = new RequestRouter();
  @Test void testCreation() {assertEquals(RequestRouter.Intent.CREATE_TEST, router.classify("Erstelle mir einen vollständigen AGATE-Test für den DMP-Webservice",false,false).intent());}
  @Test void followUp() {assertEquals(RequestRouter.Intent.EXPLAIN_PREVIOUS, router.classify("Warum hast du diesen XPath geändert und nicht den Haupt-XPath?",true,false).intent());}
  @Test void openShiftOverridesOldContext() {assertEquals(RequestRouter.Intent.UNSUPPORTED_ENGINE,router.classify("Mein OpenShift-Pod ist im CrashLoopBackOff",true,false).intent());}
  @Test void unknownRequiresInput() {assertEquals(RequestRouter.Intent.NEEDS_INPUT,router.classify("Mein AGATE-Test funktioniert nicht mehr",false,false).intent());}
  @Test void freshLogDiagnosed() {assertEquals(RequestRouter.Intent.DIAGNOSE,router.classify("Analysiere diesen fehlgeschlagenen AGATE-Test",false,true).intent());}
}
