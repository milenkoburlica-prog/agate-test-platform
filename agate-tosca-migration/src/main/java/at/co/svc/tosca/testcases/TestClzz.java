package at.co.svc.tosca.testcases;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import at.co.svc.aga.transformator.utils.MigrationLog;
import at.co.svc.tosca.tsu.dto.ToscaNode;

public class TestClzz {

    public static void main(String[] args) throws Exception {

        String tsuFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\DMP_getMedPatientenInformationen.tsu";

        String inputFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KES2VE001YTSWWGQQQ2YY28M_testcases.json";

        // Load test cases
        List<ToscaNode> nodes =
                loadNodes(inputFile);

        MigrationLog.info(
                "Loaded nodes: "
                        + nodes.size()
        );

        // Load the TSU module index from the GZIP-compressed TSU file
        Map<String, ToscaNode> moduleIndex =
                loadTsuModules(tsuFile);

        TSUClassifier classifier =
                new TSUClassifier(moduleIndex);

        TSURouter router =
                new TSURouter(classifier);

        router.route(nodes);

        MigrationLog.success(
                "TSU classification completed."
        );
    }

    // -------------------------
    // TSU LOADER
    // -------------------------
    private static Map<String, ToscaNode> loadTsuModules(
            String file) throws Exception {

        Map<String, ToscaNode> index =
                new HashMap<>();

        try (FileInputStream fis =
                     new FileInputStream(file);
             GZIPInputStream gzip =
                     new GZIPInputStream(fis);
             InputStreamReader isr =
                     new InputStreamReader(
                             gzip,
                             StandardCharsets.UTF_8
                     )) {

            JsonElement root =
                    JsonParser.parseReader(isr);

            JsonObject rootObj =
                    root.getAsJsonObject();

            // The TSU contains a wrapper object with an Entities array.
            JsonArray entities =
                    rootObj.getAsJsonArray("Entities");

            for (JsonElement el : entities) {

                JsonObject obj =
                        el.getAsJsonObject();

                String objectClass =
                        obj.get("ObjectClass")
                                .getAsString();

                if (!"XModule".equals(objectClass)
                        && !"ApiModule".equals(objectClass)) {

                    continue;
                }

                ToscaNode node =
                        new ToscaNode();

                node.surrogate =
                        obj.get("Surrogate")
                                .getAsString();

                node.objectClass =
                        objectClass;

                JsonObject attrs =
                        obj.getAsJsonObject("Attributes");

                if (attrs != null) {

                    for (var e : attrs.entrySet()) {

                        node.attributes.put(
                                e.getKey(),
                                attrs.get(e.getKey()).isJsonNull()
                                        ? ""
                                        : attrs.get(e.getKey()).getAsString()
                        );
                    }
                }

                index.put(
                        node.surrogate,
                        node
                );
            }
        }

        return index;
    }

    // -------------------------
    // TESTCASE LOADER
    // -------------------------
    private static List<ToscaNode> loadNodes(
            String file) throws Exception {

        try (FileReader reader =
                     new FileReader(file)) {

            JsonArray arr =
                    JsonParser.parseReader(reader)
                            .getAsJsonArray();

            List<ToscaNode> nodes =
                    new ArrayList<>();

            for (JsonElement el : arr) {

                JsonObject obj =
                        el.getAsJsonObject();

                ToscaNode node =
                        new ToscaNode();

                node.surrogate =
                        obj.get("Surrogate")
                                .getAsString();

                node.objectClass =
                        obj.get("ObjectClass")
                                .getAsString();

                JsonObject attrs =
                        obj.getAsJsonObject("Attributes");

                if (attrs != null) {

                    for (var e : attrs.entrySet()) {

                        node.attributes.put(
                                e.getKey(),
                                attrs.get(e.getKey()).isJsonNull()
                                        ? ""
                                        : attrs.get(e.getKey()).getAsString()
                        );
                    }
                }

                JsonObject assocs =
                        obj.getAsJsonObject("Assocs");

                if (assocs != null) {

                    for (var e : assocs.entrySet()) {

                        if (e.getValue().isJsonArray()) {

                            List<String> ids =
                                    new ArrayList<>();

                            for (JsonElement id :
                                    e.getValue().getAsJsonArray()) {

                                ids.add(
                                        id.getAsString()
                                );
                            }

                            node.extraAssocs.put(
                                    e.getKey(),
                                    ids
                            );
                        }
                    }
                }

                nodes.add(node);
            }

            return nodes;
        }
    }
}
