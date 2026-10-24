package at.co.svc.agate.ai;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
@QuarkusTest
public class DiagnosticResourceTest {
 @Test void health(){given().when().get("/api/v1/diagnostics/health").then().statusCode(200).body("status",equalTo("UP"));}
 @Test void blankInputRejected(){given().contentType("application/json").body("{\"caseText\":\"\"}").post("/api/v1/diagnostics/analyze").then().statusCode(400);}
}
