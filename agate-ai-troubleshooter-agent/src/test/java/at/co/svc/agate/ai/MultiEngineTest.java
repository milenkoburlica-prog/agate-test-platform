package at.co.svc.agate.ai;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;
import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
@QuarkusTest
class MultiEngineTest {
 @Inject ToolRegistry registry;
 @Inject RequestRouter router;
 @Test void sql(){assertEquals("MUSTER_ERKANNT_NICHT_VERIFIZIERT",registry.get("SQL").orElseThrow().analyze("ORA-00942: table or view does not exist").status());}
 @Test void openshift(){var f=registry.get("OPENSHIFT").orElseThrow().analyze("CrashLoopBackOff\nOOMKilled"); assertEquals(2,f.evidence().size());}
 @Test void noEvidence(){assertEquals("NICHT_PRÜFBAR",registry.get("GUI").orElseThrow().analyze("alles gut").status());}
 @Test void routeWithEvidence(){assertEquals(RequestRouter.Intent.ENGINE_DIAGNOSE,router.classify("Analysiere CrashLoopBackOff",false,true).intent());}
 @Test void routeWithoutEvidence(){assertEquals(RequestRouter.Intent.UNSUPPORTED_ENGINE,router.classify("Analysiere CrashLoopBackOff",false,false).intent());}
 @Test void restApiSql(){given().contentType("application/json").body("{\"message\":\"Analysiere diesen SQL-Test\",\"caseText\":\"ORA-00942: table or view does not exist\"}").post("/api/v1/chat").then().statusCode(200).body("intent",equalTo("ENGINE_DIAGNOSE"),"verificationStatus",equalTo("MUSTER_ERKANNT_NICHT_VERIFIZIERT"));}
}
