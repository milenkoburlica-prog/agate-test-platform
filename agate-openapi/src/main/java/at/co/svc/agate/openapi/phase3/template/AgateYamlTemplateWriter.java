package at.co.svc.agate.openapi.phase3.template;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class AgateYamlTemplateWriter {


    private final AgateYamlTemplateGenerator generator =
            new AgateYamlTemplateGenerator();




    public void write(
            AgateOperationModel operation,
            Path target)
            throws IOException {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }


        if (target == null) {

            throw new IllegalArgumentException(
                    "Target path must not be null"
            );
        }


        Path parent =
                target.getParent();


        if (parent != null) {

            Files.createDirectories(
                    parent
            );
        }


        String yaml =
                generator.generate(
                        operation
                );


        Files.writeString(
                target,
                yaml,
                StandardCharsets.UTF_8
        );
    }
}