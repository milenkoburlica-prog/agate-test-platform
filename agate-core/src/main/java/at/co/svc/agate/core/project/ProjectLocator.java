package at.co.svc.agate.core.project;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ProjectLocator {

    public static final String PROJECT_FILE_NAME = "project-agate.yaml";

    private ProjectLocator() {
    }

    public static LocatedProject locate(String projectArgument) {
        Path candidate;

        if (projectArgument == null || projectArgument.isBlank()) {
            candidate = Path.of(System.getProperty("user.dir"));
        } else {
            candidate = Path.of(projectArgument);
        }

        candidate = candidate.toAbsolutePath().normalize();

        final Path projectRoot;
        final Path projectFile;

        if (Files.isRegularFile(candidate)) {
            projectFile = candidate;
            projectRoot = candidate.getParent();
        } else {
            projectRoot = candidate;
            projectFile = projectRoot.resolve(PROJECT_FILE_NAME);
        }

        if (!Files.isRegularFile(projectFile)) {
            throw new IllegalStateException(
                    "AGATE project configuration was not found | Expected: "
                            + projectFile);
        }

        return new LocatedProject(projectRoot, projectFile);
    }

    public record LocatedProject(
            Path projectRoot,
            Path projectFile) {
    }
}
