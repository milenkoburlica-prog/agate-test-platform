package at.co.svc.agate.core.project;

public class ProjectConfig {

    private int version = 1;
    private Project project = new Project();
    private PathsConfig paths = new PathsConfig();
    private ConfigFiles config = new ConfigFiles();

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project != null ? project : new Project();
    }

    public PathsConfig getPaths() {
        return paths;
    }

    public void setPaths(PathsConfig paths) {
        this.paths = paths != null ? paths : new PathsConfig();
    }

    public ConfigFiles getConfig() {
        return config;
    }

    public void setConfig(ConfigFiles config) {
        this.config = config != null ? config : new ConfigFiles();
    }

    public static class Project {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class PathsConfig {
        private String tests = "tests";
        private String responses = "responses";
        private String reports = "reports";

        public String getTests() {
            return tests;
        }

        public void setTests(String tests) {
            this.tests = tests;
        }

        public String getResponses() {
            return responses;
        }

        public void setResponses(String responses) {
            this.responses = responses;
        }

        public String getReports() {
            return reports;
        }

        public void setReports(String reports) {
            this.reports = reports;
        }
    }

    public static class ConfigFiles {
        private String environments = "config/env.conf";
        private String users = "config/users.conf";

        public String getEnvironments() {
            return environments;
        }

        public void setEnvironments(String environments) {
            this.environments = environments;
        }

        public String getUsers() {
            return users;
        }

        public void setUsers(String users) {
            this.users = users;
        }
    }
}
