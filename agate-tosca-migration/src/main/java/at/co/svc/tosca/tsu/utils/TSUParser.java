package at.co.svc.tosca.tsu.utils;

import at.co.svc.tosca.tsu.dto.ToscaNode;
import com.google.gson.*;

import java.util.*;

public class TSUParser {

    public static Map<String, ToscaNode> parse(String jsonContent) {

        Map<String, ToscaNode> map = new HashMap<>();

        JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();
        JsonArray entities = root.getAsJsonArray("Entities");

        for (JsonElement el : entities) {

            JsonObject obj = el.getAsJsonObject();

            ToscaNode node = new ToscaNode();

            node.objectClass = obj.get("ObjectClass").getAsString();
            node.surrogate = obj.get("Surrogate").getAsString();

            JsonObject attrs = obj.getAsJsonObject("Attributes");
            if (attrs != null) {
                for (Map.Entry<String, JsonElement> e : attrs.entrySet()) {
                    node.attributes.put(
                            e.getKey(),
                            e.getValue().isJsonNull() ? "" : e.getValue().getAsString()
                    );
                }
            }

            JsonObject assocs = obj.getAsJsonObject("Assocs");
            if (assocs != null) {
                for (Map.Entry<String, JsonElement> e : assocs.entrySet()) {

                    if (!e.getValue().isJsonArray()) continue;

                    List<String> ids = new ArrayList<>();

                    for (JsonElement id : e.getValue().getAsJsonArray()) {
                        ids.add(id.getAsString());
                    }

                    node.extraAssocs.put(e.getKey(), ids);

                    if ("Items".equals(e.getKey())) {
                        node.childrenIds = ids;
                    }
                }
            }

            map.put(node.surrogate, node);
        }

        return map;
    }
}