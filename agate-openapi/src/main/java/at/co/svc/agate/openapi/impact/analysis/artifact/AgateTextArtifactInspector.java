package at.co.svc.agate.openapi.impact.analysis.artifact;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class AgateTextArtifactInspector {


    public String read(
            Path file)
            throws IOException {

        if (file == null ||
                !Files.exists(
                        file
                )) {

            return "";
        }


        return Files.readString(
                file,
                StandardCharsets.UTF_8
        );
    }




    public boolean containsXlReference(
            String yaml,
            String fieldPath) {

        if (yaml == null ||
                fieldPath == null) {

            return false;
        }


        return yaml.contains(
                "{XL["
                        + fieldPath
                        + "]}"
        );
    }




    public boolean containsBufferReference(
            String json,
            String fieldPath) {

        if (json == null ||
                fieldPath == null) {

            return false;
        }


        return json.contains(
                "{B["
                        + fieldPath
                        + "]}"
        );
    }
}