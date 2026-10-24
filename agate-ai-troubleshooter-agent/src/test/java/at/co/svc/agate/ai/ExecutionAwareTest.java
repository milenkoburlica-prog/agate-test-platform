package at.co.svc.agate.ai;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
@QuarkusTest
class ExecutionAwareTest {
  private static final String VERBOSE = """
>>> DSL      - type: SQL
>>> DSL        op: EXEC
>>> DSL        command: "SELECT COUNT(*) ID FROM FOTO_INFO"
>>> DSL        response: buffere_foto_id_count_res
    <<< | 0 |
    >>> SQL STEP FINISHED | Status: SUCCESS | Duration: 10 ms
>>> DSL      - type: SQL
>>> DSL        op: BUFFER
>>> DSL        name: COUNT_ID
>>> DSL        response: buffere_foto_id_count_res
    <<< OUT         : 0
    >>> SQL STEP FINISHED | Status: SUCCESS | Duration: 1 ms
>>> SKIPPED STEP DSL:
>>> DSL      - type: SQL
>>> DSL        op: EXEC
>>> DSL        condition: "'{B[COUNT_ID]}' == '1'"
>>> DSL        command: "SELECT ID FROM FOTO_INFO"
>>> DSL        response: buffere_foto_id_res
    >>> SKIPPED (Condition false: '0' == '1')
    >>> SQL STEP FINISHED | Status: SUCCESS | Duration: 2 ms
>>> DSL      - type: SQL
>>> DSL        op: BUFFER
>>> DSL        name: ID
>>> DSL        row: "0"
>>> DSL        column: "0"
>>> DSL        response: buffere_foto_id_res
    >>> SQL STEP FAILED | ERROR: SQL response was not found
    >>> SQL STEP FINISHED | Status: FAILED | Duration: 1 ms
        Reason   : SQL response was not found
        Response : buffere_foto_id_res
        Reusable : reusable.fotoinfo_db_delete_foto_info
    >>> CALL STEP FINISHED | Status: FAILED | Duration: 100 ms
Exit Code : 1
""";
  private static final String SHORT = """
>>> DSL      - type: SQL
>>> DSL        op: BUFFER
>>> DSL        name: ID
>>> DSL        response: buffere_foto_id_res
    >>> SQL STEP FAILED | ERROR: SQL response was not found
    >>> SQL STEP FINISHED | Status: FAILED | Duration: 1 ms
        Reason   : SQL response was not found
        Response : buffere_foto_id_res
Exit Code : 1
""";
  @Test void verboseCorrelatesSkippedProducer() {
    var f=new AgateExecutionParser().findFailure(VERBOSE).orElseThrow();
    assertEquals("SQL",f.engine());
    var finding=new SqlResponseCorrelationTool().analyze(f);
    assertEquals("KORREKTUR_VORGESCHLAGEN_NICHT_VERIFIZIERT",finding.status());
    assertTrue(finding.recommendations().stream().anyMatch(s->s.contains("condition:")));
    assertTrue(finding.evidence().stream().anyMatch(s->s.contains("'0' == '1'")));
  }
  @Test void shortStillFindsSqlNotCmd() {
    var f=new AgateExecutionParser().findFailure(SHORT).orElseThrow();
    assertEquals("SQL",f.engine());
    assertEquals("URSACHE_EINGEGRENZT_NICHT_VERIFIZIERT",new SqlResponseCorrelationTool().analyze(f).status());
  }
  @Test void apiVerboseDetectsSqlBeforeExitAndSoap() {
    given().contentType("application/json")
      .body(java.util.Map.of("message","Analysiere diesen fehlgeschlagenen AGATE-Test",
          "caseText",VERBOSE+"\n>>> SOAP STEP FINISHED | Status: SUCCESS"))
      .post("/api/v1/chat").then().statusCode(200)
      .body("verificationStatus",equalTo("KORREKTUR_VORGESCHLAGEN_NICHT_VERIFIZIERT"),
            "answer",containsString("SQL EXEC"));
  }
}
