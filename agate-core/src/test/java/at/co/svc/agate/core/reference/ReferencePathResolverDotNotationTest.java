package at.co.svc.agate.core.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.project.ProjectConfig;
import at.co.svc.agate.core.project.ProjectContext;

class ReferencePathResolverDotNotationTest {
    @TempDir Path root;

    @AfterEach void cleanup() {
        ReferencePathResolver.setProjectContext(null);
        System.clearProperty("APPLICATION");
    }

    @Test void matchReferenceUsesHierarchicalAppDirectory() {
        ProjectConfig config = new ProjectConfig();
        config.getPaths().setResponses("svc/responses");
        ReferencePathResolver.setProjectContext(
                new ProjectContext(root, root.resolve("project-agate.yaml"), config));
        System.setProperty("APPLICATION", "AUM.SS12.8");

        TestCase testCase = new TestCase();
        testCase.setName("GF valid Ticket 19120");
        TestStep step = new TestStep();
        step.setId("step_6");

        Path actual = new ReferencePathResolver().resolve(
                root.resolve("svc/regression/aum/ss12/8/Instance_t_09_getSVTs.yaml").toString(),
                testCase, step, ResponseFormat.XML);
        Path expected = root.resolve(
                "svc/responses/aum/ss12/8/Instance_t_09_getSVTs/GF_valid_Ticket_19120__step_6.xml");
        assertEquals(expected.toAbsolutePath().normalize(), actual);
    }
}
