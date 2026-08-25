package at.co.svc.agate.openapi.impact.analysis.csv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;


public class AgateCsvArtifactReader {


    public AgateCsvSnapshot read(
            Path file)
            throws IOException {

        if (file == null) {

            throw new IllegalArgumentException(
                    "CSV file must not be null"
            );
        }


        if (!Files.exists(
                file
        )) {

            throw new IllegalArgumentException(
                    "CSV file does not exist: "
                            + file
            );
        }


        List<String> lines =
                Files.readAllLines(
                        file,
                        StandardCharsets.UTF_8
                );


        AgateCsvSnapshot result =
                new AgateCsvSnapshot();


        for (String line :
                lines) {

            if (line == null ||
                    line.isBlank()) {

                continue;
            }


            String[] columns =
                    line.split(
                            ";",
                            -1
                    );


            if (columns.length == 0) {

                continue;
            }


            String rowName =
                    columns[0].trim();


            List<String> values =
                    new ArrayList<>();


            for (int i = 1;
                 i < columns.length;
                 i++) {

                values.add(
                        columns[i]
                );
            }


            if ("testcaseName".equals(
                    rowName
            )) {

                result.setTestcaseNames(
                        values
                );

            } else {

                result.putRow(
                        rowName,
                        values
                );
            }
        }


        return result;
    }
}