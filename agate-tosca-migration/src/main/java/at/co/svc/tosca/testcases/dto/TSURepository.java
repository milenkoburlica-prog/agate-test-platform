package at.co.svc.tosca.testcases.dto;

import at.co.svc.tosca.tsu.dto.ToscaNode;
import com.google.gson.*;

import java.io.FileReader;
import java.util.*;

public class TSURepository {

    private final Map<String, ToscaNode> moduleIndex = new HashMap<>();

    public TSURepository(String tsuFile) throws Exception {

        JsonArray arr;

        try (FileReader reader = new FileReader(tsuFile)) {
            arr = JsonParser.parseReader(reader).getAsJsonArray();
        }

        for (JsonElement el : arr) {

            JsonObject obj = el.getAsJsonObject();

            ToscaNode node = new ToscaNode();
            node.surrogate = obj.get("Surrogate").getAsString();
            node.objectClass = obj.get("ObjectClass").getAsString();

            // Attributes
            JsonObject attrs = obj.getAsJsonObject("Attributes");
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

            moduleIndex.put(node.surrogate, node);
        }
    }

    public ToscaNode getModule(String id) {
        return moduleIndex.get(id);
    }
}