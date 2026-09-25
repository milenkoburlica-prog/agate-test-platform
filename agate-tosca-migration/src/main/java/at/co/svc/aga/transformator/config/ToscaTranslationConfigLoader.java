package at.co.svc.aga.transformator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public final class ToscaTranslationConfigLoader {

    private static final String SYSTEM_PROPERTY =
            "agate.tosca.translation.config";

    private static final String ENV_VARIABLE =
            "AGATE_TOSCA_TRANSLATION_CONFIG";

    private static final String DEFAULT_FILE =
            "config/tosca-translations.yaml";


    private ToscaTranslationConfigLoader() {
    }


    public static ToscaTranslationConfig load() {

        String path =
                resolveConfigPath();

        File file =
                new File(path);

        if (!file.isFile()) {

            throw new IllegalStateException(
                    "Tosca translation configuration was not found: "
                            + file.getAbsolutePath()
                            + System.lineSeparator()
                            + "Configure it with -D"
                            + SYSTEM_PROPERTY
                            + "=<file> or environment variable "
                            + ENV_VARIABLE
            );
        }

        ObjectMapper mapper =
                new ObjectMapper(
                        new YAMLFactory()
                );

        try {

            ToscaTranslationConfig config =
                    mapper.readValue(
                            file,
                            ToscaTranslationConfig.class
                    );

            
            
            
            validate(
                    config,
                    file
            );

            return config;

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Cannot read Tosca translation configuration: "
                            + file.getAbsolutePath(),
                    e
            );
        }
    }


    private static String resolveConfigPath() {

        String property =
                System.getProperty(
                        SYSTEM_PROPERTY
                );

        if (property != null
                && !property.isBlank()) {

            return property.trim();
        }


        String env =
                System.getenv(
                        ENV_VARIABLE
                );

        if (env != null
                && !env.isBlank()) {

            return env.trim();
        }


        return DEFAULT_FILE;
    }


    private static void validate(
            ToscaTranslationConfig config,
            File file) {

        if (config == null) {

            throw new IllegalStateException(
                    "Empty Tosca translation configuration: "
                            + file.getAbsolutePath()
            );
        }

        if (config.getBufferMappings() == null) {

            throw new IllegalStateException(
                    "bufferMappings is missing in "
                            + file.getAbsolutePath()
            );
        }

        if (config.getSettingMappings() == null) {

            throw new IllegalStateException(
                    "settingMappings is missing in "
                            + file.getAbsolutePath()
            );
        }

        if (config.getLiteralMappings() == null) {

            throw new IllegalStateException(
                    "literalMappings is missing in "
                            + file.getAbsolutePath()
            );
        }
    }
}