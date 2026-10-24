package at.co.svc.agate.core.dsl.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import at.co.svc.agate.core.project.ProjectConfig;
import at.co.svc.agate.core.project.ProjectContext;

class ReusablePathResolverDotNotationTest {
    @TempDir Path projectRoot;

    @AfterEach void resetContext() {
        ReusablePathResolver.setProjectContext(null);
    }

    private void initContext() {
        ProjectConfig config = new ProjectConfig();
        config.getPaths().setTests("data");
        ReusablePathResolver.setProjectContext(
                new ProjectContext(projectRoot, projectRoot.resolve("agate-project.yaml"), config));
    }

    @Test void resolvesReusableUnderDottedApplicationDirectory() throws Exception {
        initContext();
        Path root = Files.createDirectories(projectRoot.resolve("data/dmp/11/all/reusable"));
        assertEquals(root.resolve("example.yaml").toString(),
                ReusablePathResolver.resolve("reusable.example", "DMP.11.all"));
    }

    @Test void literalApplicationDirectoryHasPriority() throws Exception {
        initContext();
        Path root = Files.createDirectories(projectRoot.resolve("data/dmp.11.all/reusable"));
        Files.createDirectories(projectRoot.resolve("data/dmp/11/all/reusable"));
        assertEquals(root.resolve("example.yaml").toString(),
                ReusablePathResolver.resolve("reusable.example", "DMP.11.all"));
    }
}
