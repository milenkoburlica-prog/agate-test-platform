package at.co.svc.agate.openapi.phase3.csv;

import at.co.svc.agate.openapi.phase3.csv.model.AgateCsvTable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class AgateCsvWriter {


    private final AgateCsvSerializer serializer =
            new AgateCsvSerializer();




    public void write(
            AgateCsvTable table,
            Path target)
            throws IOException {

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


        String csv =
                serializer.serialize(
                        table
                );


        Files.writeString(
                target,
                csv,
                StandardCharsets.UTF_8
        );
    }
}