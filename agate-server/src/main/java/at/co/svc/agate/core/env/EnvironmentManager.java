package at.co.svc.agate.core.env;

import java.io.FileInputStream;
import java.util.Properties;

public final class EnvironmentManager {
    public static Properties envConfig = new Properties();
    public static Properties readersConfig = new Properties();
    private static boolean isLoaded=false;

    public static void init() {
        try {
          if (!isLoaded) {
              
              try (FileInputStream fis = new FileInputStream("env/env.conf")) {
                  envConfig.load(fis);
              }
              try (FileInputStream fis = new FileInputStream("env/users.conf")) {
                  readersConfig.load(fis);
              }
              
              isLoaded = true;
          } else {
              
          }
        } catch (Exception e) {
            throw new RuntimeException("Conf File reading error", e);
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

