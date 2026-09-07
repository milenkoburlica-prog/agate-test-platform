package at.co.svc.tosca.tsu.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class TsuExtractor {

    public static String extractTsuToJson(
            String tsuPath,
            String outputDir) throws Exception {

        String tsuName =
                Paths.get(tsuPath)
                        .getFileName()
                        .toString()
                        .replace(
                                ".tsu",
                                ""
                        );

        ObjectMapper mapper =
                new ObjectMapper();

        JsonNode tsuRoot;

        // =========================
        // READ GZIP TSU
        // =========================
        try (InputStream fis =
                     new FileInputStream(tsuPath);
             GZIPInputStream gis =
                     new GZIPInputStream(fis)) {

            tsuRoot =
                    mapper.readTree(gis);
        }

        // =========================
        // OUTPUT FILE
        // =========================
        String finalFile =
                outputDir
                        + "\\"
                        + tsuName
                        + ".json";

        // =========================
        // PRETTY PRINT JSON
        // =========================
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File(finalFile),
                        tsuRoot
                );

        MigrationLog.success(
                "TSU extracted as pretty JSON: "
                        + finalFile
        );

        return finalFile;
    }
}
