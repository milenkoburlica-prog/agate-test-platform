package at.co.svc.tosca.testcases.dto;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

import com.google.gson.*;

public class TsuLoader {

    public static JsonArray loadTsuAsJsonArray(String tsuFile) throws Exception {

        try (FileInputStream fis = new FileInputStream(tsuFile);
             GZIPInputStream gzip = new GZIPInputStream(fis);
             InputStreamReader isr = new InputStreamReader(gzip, StandardCharsets.UTF_8)) {

            JsonElement root = JsonParser.parseReader(isr);

            return root.getAsJsonArray();
        }
    }
    
}