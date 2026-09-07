package at.co.svc.tosca.tsu.del;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import at.co.svc.tosca.tsu.utils.GzipHelper;

public class TSUDeepPrinter {

    private final Map<String, JsonObject> nodeMap = new HashMap<>();
    private final Map<String, String> nodePathCache = new HashMap<>();

    public static void main(String[] args) {

        String tsuPath =
            "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\DMP_getMedPatientenInformationen.tsu";

        String jsonPathOutput =
            "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\DMP_getMedPatientenInformationen_unpacked.json";

        new TSUDeepPrinter().process(tsuPath, jsonPathOutput);

        System.out.println(">>> FINISHED");
    }

    public void process(String inputTsuPath, String outputJsonPath) {
        try {
            String jsonContent = GzipHelper.extractGzipContent(inputTsuPath);

            JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();
            JsonArray entities = root.getAsJsonArray("Entities");

            JsonArray output = new JsonArray();

            // 1. FIRST PASS -> build lookup map
            for (JsonElement el : entities) {
                JsonObject obj = el.getAsJsonObject();
                nodeMap.put(obj.get("Surrogate").getAsString(), obj);
            }

            // 2. SECOND PASS -> build enriched JSON
            for (JsonElement el : entities) {
                JsonObject obj = el.getAsJsonObject();

                String id = obj.get("Surrogate").getAsString();

                JsonObject clean = new JsonObject();

                clean.addProperty("ObjectClass", obj.get("ObjectClass").getAsString());
                clean.addProperty("Surrogate", id);

                // ⭐ NODEPATH (NEW)
                clean.addProperty("NodePath", buildNodePath(id));

                // Attributes
                JsonObject attrs = obj.getAsJsonObject("Attributes");
                JsonObject newAttrs = new JsonObject();

                if (attrs != null) {
                    for (var entry : attrs.entrySet()) {
                        String key = entry.getKey();
                        JsonElement value = entry.getValue();

                        if ("TCProperties".equals(key) && value != null && !value.isJsonNull()) {
                            String base64 = value.getAsString();
                            newAttrs.add("TCProperties", decodeTcProperties(base64));
                        } else {
                            newAttrs.add(key, value.isJsonNull() ? JsonNull.INSTANCE : value);
                        }
                    }
                }

                clean.add("Attributes", newAttrs);

                // Assocs
                JsonObject assocs = obj.getAsJsonObject("Assocs");
                if (assocs != null) {
                    clean.add("Assocs", assocs.deepCopy());
                }

                output.add(clean);
            }

            try (FileWriter fw = new FileWriter(outputJsonPath)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(output, fw);
            }

            System.out.println(">>> DONE: " + outputJsonPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------
    // NODE PATH BUILDER
    // -------------------------
    private String buildNodePath(String id) {

        if (nodePathCache.containsKey(id)) {
            return nodePathCache.get(id);
        }

        JsonObject node = nodeMap.get(id);
        if (node == null) return "";

        String name = getName(node);

        JsonObject assocs = node.getAsJsonObject("Assocs");

        if (assocs == null || !assocs.has("ParentFolder")) {
            nodePathCache.put(id, name);
            return name;
        }

        JsonArray parents = assocs.getAsJsonArray("ParentFolder");

        if (parents == null || parents.size() == 0) {
            nodePathCache.put(id, name);
            return name;
        }

        String parentId = parents.get(0).getAsString();

        String parentPath = buildNodePath(parentId);

        String fullPath = parentPath + "/" + name;

        nodePathCache.put(id, fullPath);

        return fullPath;
    }

    private String getName(JsonObject obj) {
        JsonObject attrs = obj.getAsJsonObject("Attributes");
        if (attrs != null && attrs.has("Name")) {
            return attrs.get("Name").getAsString();
        }
        return "UNKNOWN";
    }

    // -------------------------
    // TCProperties decoder
    // -------------------------
    private JsonObject decodeTcProperties(String base64) {

        JsonObject result = new JsonObject();

        try {
            byte[] compressed = Base64.getDecoder().decode(base64);

            StringBuilder xml = new StringBuilder();

            try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                 BufferedReader br = new BufferedReader(new InputStreamReader(gzis, StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    xml.append(line);
                }
            }

            Pattern p = Pattern.compile("<TCProperty Name=\"(.*?)\" Value=\"(.*?)\"");
            Matcher m = p.matcher(xml.toString());

            while (m.find()) {
                result.addProperty(m.group(1), m.group(2));
            }

        } catch (Exception e) {
            result.addProperty("error", "decode_failed");
        }

        return result;
    }
}