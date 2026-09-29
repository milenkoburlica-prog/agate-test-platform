package at.co.svc.agate.core.dsl.resolver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import at.co.svc.agate.core.project.ProjectContext;

public final class ReusablePathResolver {

    private static volatile ProjectContext projectContext;

    private ReusablePathResolver() {
    }

    public static void setProjectContext(ProjectContext context) {
        projectContext = context;
    }

    public static String resolve(
            String action,
            String application) {

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Reusable action must not be empty");
        }

        if (application == null || application.isBlank()) {
            throw new IllegalArgumentException("Application must not be empty");
        }

        String normalizedApplication =
                application.trim().toLowerCase(Locale.ROOT);

        Path applicationRoot;

        ProjectContext context = projectContext;
        if (context != null) {
            applicationRoot =
                    context.getTestsRoot()
                            .resolve(normalizedApplication)
                            .normalize();
        } else {
            // Legacy fallback for direct executions without ProjectContext.
            applicationRoot =
                    Paths.get("data", normalizedApplication)
                            .normalize();
        }

        return applicationRoot
                .resolve(action.replace(".", File.separator) + ".yaml")
                .normalize()
                .toString();
    }
}
