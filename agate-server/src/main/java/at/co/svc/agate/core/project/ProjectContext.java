package at.co.svc.agate.core.project;

import java.nio.file.Path;

public final class ProjectContext {

    private final Path projectRoot;
    private final Path projectFile;
    private final ProjectConfig config;

    public ProjectContext(
            Path projectRoot,
            Path projectFile,
            ProjectConfig config) {

        if (projectRoot == null) {
            throw new IllegalArgumentException("projectRoot must not be null");
        }
        if (projectFile == null) {
            throw new IllegalArgumentException("projectFile must not be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }

        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.projectFile = projectFile.toAbsolutePath().normalize();
        this.config = config;
    }

    public String getProjectName() {
        return config.getProject() != null
                ? config.getProject().getName()
                : null;
    }

    public int getVersion() {
        return config.getVersion();
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public Path getProjectFile() {
        return projectFile;
    }

    public ProjectConfig getConfig() {
        return config;
    }

    public Path getTestsRoot() {
        return resolve(config.getPaths().getTests());
    }

    public Path getResponsesRoot() {
        return resolve(config.getPaths().getResponses());
    }

    public Path getReportsRoot() {
        return resolve(config.getPaths().getReports());
    }

    public Path getEnvironmentConfig() {
        return resolve(config.getConfig().getEnvironments());
    }

    public Path getUsersConfig() {
        return resolve(config.getConfig().getUsers());
    }

    public Path resolve(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            throw new IllegalArgumentException("Configured project path must not be empty");
        }

        Path path = Path.of(configuredPath.trim());
        if (path.isAbsolute()) {
            return path.normalize();
        }

        return projectRoot.resolve(path).normalize();
    }
}
