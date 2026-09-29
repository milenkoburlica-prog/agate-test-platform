package at.co.svc.agate.core.project;

import java.nio.file.Path;

/**
 * Process-wide access to the active AGATE project context.
 *
 * This is intentionally limited to path resolution so legacy static helpers
 * can become project-aware without changing unrelated engine APIs.
 */
public final class ProjectRuntime {

    private static volatile ProjectContext context;

    private ProjectRuntime() {
    }

    public static void initialize(
            ProjectContext projectContext) {

        if (projectContext == null) {
            throw new IllegalArgumentException(
                    "projectContext must not be null");
        }

        context = projectContext;
    }

    public static void clear() {
        context = null;
    }

    public static boolean isInitialized() {
        return context != null;
    }

    public static ProjectContext getContext() {

        ProjectContext current =
                context;

        if (current == null) {
            throw new IllegalStateException(
                    "AGATE ProjectContext has not been initialized.");
        }

        return current;
    }

    public static Path applicationRoot(
            String application) {

        if (application == null
                || application.isBlank()) {

            throw new IllegalArgumentException(
                    "application must not be empty");
        }

        return getContext()
                .getTestsRoot()
                .resolve(
                        application
                                .trim()
                                .toLowerCase())
                .normalize();
    }

    public static Path modulesRoot(
            String application) {

        return applicationRoot(application)
                .resolve("modules")
                .normalize();
    }

    public static Path responsesRoot() {

        return getContext()
                .getResponsesRoot()
                .normalize();
    }

    public static Path applicationResponsesRoot(
            String application) {

        if (application == null
                || application.isBlank()) {

            throw new IllegalArgumentException(
                    "application must not be empty");
        }

        return responsesRoot()
                .resolve(
                        application
                                .trim()
                                .toLowerCase())
                .normalize();
    }
}