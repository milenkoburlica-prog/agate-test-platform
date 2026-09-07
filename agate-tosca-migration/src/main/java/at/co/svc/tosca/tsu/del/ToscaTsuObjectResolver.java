package at.co.svc.tosca.tsu.del;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class ToscaTsuObjectResolver {

    private final ObjectMapper mapper = new ObjectMapper();

    // surrogate -> entity
    private final Map<String, JsonNode> entityIndex = new HashMap<>();

    // visited objects
    private final Set<String> visited = new HashSet<>();

    // =====================================================
    // MAIN
    // =====================================================
    public static void main(String[] args) throws Exception {

        // -------------------------------------------------
        // TEST INPUT
        // -------------------------------------------------
        String tsuFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\EcSrvCTS.tsu";

        String rootSurrogate =
                "01KH6JVGKW29NNAAHYF5C464MW";

        // -------------------------------------------------
        // RUN
        // -------------------------------------------------
        ToscaTsuObjectResolver resolver =
                new ToscaTsuObjectResolver();

        resolver.load(tsuFile);

        ArrayNode result =
                resolver.extractTree(rootSurrogate);

        // -------------------------------------------------
        // SUMMARY
        // -------------------------------------------------
        System.out.println();
        System.out.println("====================================");
        System.out.println("✔ EXTRACTION FINISHED");
        System.out.println("✔ TOTAL OBJECTS : " + result.size());
        System.out.println("====================================");

        // optional export
        ObjectMapper mapper = new ObjectMapper();

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File("extracted-tree.json"),
                        result
                );

        System.out.println("✔ FILE WRITTEN : extracted-tree.json");
    }

    // =====================================================
    // LOAD TSU
    // =====================================================
    public void load(String tsuFile) throws Exception {

        try (InputStream fis = new FileInputStream(tsuFile);
             GZIPInputStream gis = new GZIPInputStream(fis)) {

            JsonNode root = mapper.readTree(gis);

            JsonNode entities = root.path("Entities");

            for (JsonNode entity : entities) {

                String surrogate =
                        entity.path("Surrogate").asText(null);

                if (surrogate != null) {

                    entityIndex.put(
                            surrogate,
                            entity
                    );
                }
            }
        }

        System.out.println(
                "✔ INDEXED ENTITIES : " + entityIndex.size()
        );
    }

    // =====================================================
    // EXTRACT TREE
    // =====================================================
    public ArrayNode extractTree(String rootSurrogate) {

        visited.clear();

        ArrayNode result =
                mapper.createArrayNode();

        collectRecursive(rootSurrogate, result);

        return result;
    }

    // =====================================================
    // RECURSIVE COLLECTION
    // =====================================================
    private void collectRecursive(
            String surrogate,
            ArrayNode result
    ) {

        if (surrogate == null || surrogate.isBlank()) {
            return;
        }

        // prevent cycles
        if (visited.contains(surrogate)) {
            return;
        }

        visited.add(surrogate);

        JsonNode entity =
                entityIndex.get(surrogate);

        if (entity == null) {

            System.out.println(
                    "[WARN] MISSING : " + surrogate
            );

            return;
        }

        result.add(entity);

        // -------------------------------------------------
        // PRINT ONLY EXTRACTED OBJECT
        // -------------------------------------------------
        String objectClass =
                entity.path("ObjectClass").asText("UNKNOWN");

        String name =
                entity.path("Attributes")
                        .path("Name")
                        .asText("");

        System.out.println(
                "✔ EXTRACTED : "
                        + objectClass
                        + " | "
                        + surrogate
                        + (name.isBlank() ? "" : " | " + name)
        );

        // -------------------------------------------------
        // PROCESS ASSOCS
        // -------------------------------------------------
        JsonNode assocs =
                entity.path("Assocs");

        if (!assocs.isObject()) {
            return;
        }

        Iterator<Map.Entry<String, JsonNode>> fields =
                assocs.fields();

        while (fields.hasNext()) {

            Map.Entry<String, JsonNode> assoc =
                    fields.next();

            JsonNode value =
                    assoc.getValue();

            // array refs
            if (value.isArray()) {

                for (JsonNode ref : value) {

                    if (ref.isTextual()) {

                        collectRecursive(
                                ref.asText(),
                                result
                        );
                    }
                }
            }

            // single ref
            else if (value.isTextual()) {

                collectRecursive(
                        value.asText(),
                        result
                );
            }
        }
    }
}