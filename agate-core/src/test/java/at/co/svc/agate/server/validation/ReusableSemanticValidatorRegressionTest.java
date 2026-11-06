package at.co.svc.agate.server.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReusableSemanticValidatorRegressionTest {
    @TempDir Path temp;

    private List<ValidationIssue> issuesFor(String reusable) throws Exception {
        Path reusableDir = Files.createDirectories(temp.resolve("reusable"));
        Files.writeString(reusableDir.resolve("sample.yaml"), reusable);
        Path main = temp.resolve("main.yaml");
        Files.writeString(main, """
            testCases:
              - name: demo
                steps:
                  - type: CALL
                    command: reusable.sample
            """);
        ValidationParser parser = new ValidationParser();
        ValidationParser.ParsedDocument document = parser.parse(main);
        return new ReusableSemanticValidator().validate(new ValidationContext(
            main, document.root(), document.source(), ValidationOptions.defaults()));
    }

    @Test void acceptsTopLevelReusableSteps() throws Exception {
        var issues = issuesFor("""
            steps:
              - type: BUFFER
                op: EXEC
                name: sample
                value: ready
            """);
        assertTrue(issues.stream().noneMatch(x -> x.code().equals("AGATE-V124")), issues.toString());
    }

    @Test void acceptsNamedReusableSteps() throws Exception {
        var issues = issuesFor("""
            name: sample
            steps:
              - type: BUFFER
                op: EXEC
                name: sample
                value: ready
            """);
        assertTrue(issues.stream().noneMatch(x -> x.code().equals("AGATE-V124")), issues.toString());
    }

    @Test void stillDetectsUnsupportedBreakInReusable() throws Exception {
        var issues = issuesFor("""
            steps:
              - type: LOOP
                mode: DO_WHILE
                condition: "{B[status]} == 0"
                maxIterations: 2
                timeoutMs: 10000
                steps:
                  - type: BREAK
                    condition: "true"
            """);
        assertTrue(issues.stream().anyMatch(x -> x.code().equals("AGATE-V121")), issues.toString());
        assertTrue(issues.stream().noneMatch(x -> x.code().equals("AGATE-V124")), issues.toString());
    }
}
