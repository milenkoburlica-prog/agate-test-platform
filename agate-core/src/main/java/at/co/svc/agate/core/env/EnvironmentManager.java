package at.co.svc.agate.core.env;

import java.io.FileInputStream;
import java.nio.file.Path;
import at.co.svc.agate.core.project.ProjectRuntime;
import java.util.Properties;

public final class EnvironmentManager {
    public static Properties envConfig = new Properties();
    public static Properties readersConfig = new Properties();
    private static boolean isLoaded=false;

    public static synchronized void init() {
        Path environmentPath = ProjectRuntime.getContext().getEnvironmentConfig();
        Path usersPath = ProjectRuntime.getContext().getUsersConfig();
        if (isLoaded && environmentPath.equals(loadedEnvironmentPath)
                && usersPath.equals(loadedUsersPath)) {
            return;
        }
        Properties newEnv = load(environmentPath, "environment");
        Properties newUsers = load(usersPath, "users");
        envConfig = newEnv;
        readersConfig = newUsers;
        loadedEnvironmentPath = environmentPath;
        loadedUsersPath = usersPath;
        isLoaded = true;
    }

    private static Path loadedEnvironmentPath;
    private static Path loadedUsersPath;

    private static Properties load(Path file, String kind) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(file.toFile())) {
            properties.load(fis);
            return properties;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "AGATE " + kind + " configuration could not be loaded: "
                            + file.toAbsolutePath().normalize() + " (" + e.getMessage() + ")", e);
        }
    }

    public static String getEnvValue(String path) {
        if (!isLoaded) {
            init();
        }
        if (path != null) {
            if (path.startsWith("env.")) {
                path = path.substring(4);
            }
        }
        if (envConfig.isEmpty()) 
            init(); 

        String instance = System.getProperty("INSTANCE");
        return envConfig.getProperty(instance.trim() + "." + path.trim());
    }

    public static String getReaderValue(String path) {
        if (!isLoaded) {
            init();
        }
        if (readersConfig.isEmpty()) init();
        
        String instance = System.getProperty("INSTANCE");
        String person = System.getProperty("PERSON");
        
        String fullKey = instance + "." + person + "." + path;
        
        return readersConfig.getProperty(fullKey);
    }
}

