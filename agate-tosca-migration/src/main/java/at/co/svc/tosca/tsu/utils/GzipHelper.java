package at.co.svc.tosca.tsu.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

public class GzipHelper {
    public static String extractGzipContent(String tsuPath) throws IOException {
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(tsuPath));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[16384];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            return out.toString("UTF-8");
        }
    }
    public static String base64ContentUnGzip(String base64Content) {
        if (base64Content == null || base64Content.isEmpty()) return "";
        String xml = null;
        try {
            // 1. Base64 Decode
            byte[] compressed = Base64.getDecoder().decode(base64Content);
            
            // 2. GZIP Decompress
            StringBuilder xmlBuilder = new StringBuilder();
            try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(gzis, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    xmlBuilder.append(line);
                }
            }
            xml = xmlBuilder.toString();
       
         } catch (Exception e) {
             // default value 
             xml = "";
         }
         return xml;
    }
}
