package at.co.svc.agate.server.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SoapValidatorParityRegressionTest {
    @TempDir Path temp;

    @Test
    void soapExistsAndFilteredAssertionsAreAccepted() throws Exception {
        Path yaml = temp.resolve("instantiated.yaml");
        Files.writeString(yaml, """
                testCases:
                  - id: selected
                    template: template/source.yaml
                    testdata: template/data.csv
                    variables:
                      migratedLiteral: 'abc'
                    steps:
                      - type: SOAP
                        op: ASSERT
                        source: BODY
                        response: response1
                        action: EXISTS
                        path: /*[local-name()='return']
                      - type: SOAP
                        op: ASSERT
                        source: BODY
                        response: response1
                        action: EQUALS
                        expected: 'false'
                        path: /*[local-name()='return']/*[local-name()='active']
                        constraints:
                          - path: /*[local-name()='return']/*[local-name()='code']
                            action: EQUALS
                            expected: '01'
                """);
        List<ValidationIssue> issues = ValidationService.defaultService()
                .validate(yaml).issues();
        assertTrue(issues.stream().noneMatch(i -> i.code().equals("AGATE-V229")
                || i.code().equals("AGATE-V123") || i.code().equals("AGATE-V420")),
                () -> "Unexpected errors: " + issues);
    }
}
