package at.co.svc.agate.ai;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
@QuarkusTest
class ChatResourceTest {
 @Test void capabilities(){given().when().get("/api/v1/chat/capabilities").then().statusCode(200).body("size()",greaterThan(2));}
 @Test void help(){given().contentType("application/json").body("{\"message\":\"Was kannst du?\"}").post("/api/v1/chat").then().statusCode(200).body("intent",equalTo("CAPABILITIES"));}
 @Test void needsLog(){given().contentType("application/json").body("{\"message\":\"Analysiere meinen Test\"}").post("/api/v1/chat").then().statusCode(200).body("intent",equalTo("NEEDS_INPUT"));}
 @Test void createTestIsUnsupported(){given().contentType("application/json").body("{\"message\":\"Erstelle mir einen vollständigen AGATE-Test für den DMP-Webservice mit SOAP-Request und Reusables.\"}").post("/api/v1/chat").then().statusCode(200).body("intent",equalTo("UNSUPPORTED"));}
 @Test void openshiftDoesNotReuseSoap(){given().contentType("application/json").body("{\"sessionId\":\"existing-session\",\"message\":\"Mein OpenShift-Pod ist im CrashLoopBackOff. Analysiere Pod-Logs und Events.\"}").post("/api/v1/chat").then().statusCode(200).body("intent",equalTo("UNSUPPORTED_ENGINE"),"answer",containsString("OPENSHIFT"));}
 @Test void missingDataStillWorks(){given().contentType("application/json").body("{\"message\":\"Mein AGATE-Test funktioniert nicht mehr.\"}").post("/api/v1/chat").then().statusCode(200).body("intent",equalTo("NEEDS_INPUT"));}
}
