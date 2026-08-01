package at.co.svc.open.api.spec.generator;


import java.nio.file.Files;
import java.nio.file.Path;



public class TemplateFileWriter {


    public void write(
            Path directory,
            String module,
            String yaml,
            String csv)
            throws Exception {



        Files.createDirectories(
                directory
        );



        Files.writeString(
                directory.resolve(
                        module + ".yaml"
                ),
                yaml
        );



        Files.writeString(
                directory.resolve(
                        module + ".csv"
                ),
                csv
        );

    }

}