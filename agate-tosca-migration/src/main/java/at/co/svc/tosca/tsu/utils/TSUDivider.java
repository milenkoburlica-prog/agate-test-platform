package at.co.svc.tosca.tsu.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import at.co.svc.aga.transformator.utils.MigrationLog;
import at.co.svc.tosca.tsu.dto.ToscaNode;

public class TSUDivider {

    private static final String TESTCASES = "testcases";
    private static final String REUSABLES = "reusables";
    private static final String TEMPLATES = "templates";
    private static final String TCDSHEETS = "tcdsheets";

    private final Map<String, ToscaNode> fullObjectMap = new HashMap<>();
    private final Map<String, List<ToscaNode>> tsuInventory = new LinkedHashMap<>();

    private final Map<String, ToscaNode> parameterLayerMap = new HashMap<>();
    private final Map<String, ToscaNode> parameterMap = new HashMap<>();

    public TSUDivider() {

        tsuInventory.put(TESTCASES, new ArrayList<>());
        tsuInventory.put(REUSABLES, new ArrayList<>());
        tsuInventory.put(TEMPLATES, new ArrayList<>());
        tsuInventory.put(TCDSHEETS, new ArrayList<>());
    }

    // ---------------------------------------------------
    // ENTRY POINT
    // ---------------------------------------------------
    public Map<String, List<ToscaNode>> start(String tsuPath) {

        try {
            String json =
                    GzipHelper.extractGzipContent(tsuPath);

            parse(json);
            categorize();

            return tsuInventory;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to process TSU",
                    e
            );
        }
    }

    // ---------------------------------------------------
    // PARSE TSU -> MAP
    // ---------------------------------------------------
    public Set<String> getAllReferencedIds(ToscaNode node) {

        Set<String> result =
                new HashSet<>();

        for (List<String> ids :
                node.extraAssocs.values()) {

            result.addAll(ids);
        }

        return result;
    }

    private void parse(String content) {

        JsonObject root =
                JsonParser.parseString(content)
                        .getAsJsonObject();

        JsonArray entities =
                root.getAsJsonArray("Entities");

        for (JsonElement el : entities) {

            JsonObject obj =
                    el.getAsJsonObject();

            ToscaNode node =
                    new ToscaNode();

            node.objectClass =
                    obj.get("ObjectClass")
                            .getAsString();

            node.surrogate =
                    obj.get("Surrogate")
                            .getAsString();

            // Attributes
            JsonObject attrs =
                    obj.getAsJsonObject("Attributes");

            if (attrs != null) {

                for (Map.Entry<String, JsonElement> e :
                        attrs.entrySet()) {

                    node.attributes.put(
                            e.getKey(),
                            e.getValue().isJsonNull()
                                    ? ""
                                    : e.getValue().getAsString()
                    );
                }
            }

            // Associations
            JsonObject assocs =
                    obj.getAsJsonObject("Assocs");

            if (assocs != null) {

                for (Map.Entry<String, JsonElement> e :
                        assocs.entrySet()) {

                    if (!e.getValue().isJsonArray()) {
                        continue;
                    }

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

                    if ("Items".equals(e.getKey())) {
                        node.childrenIds = ids;
                    }
                }
            }

            fullObjectMap.put(
                    node.surrogate,
                    node
            );

            // Index special object types.
            switch (node.objectClass) {

                case "ParameterLayer" ->
                        parameterLayerMap.put(
                                node.surrogate,
                                node
                        );

                case "Parameter" ->
                        parameterMap.put(
                                node.surrogate,
                                node
                        );

                default -> {
                    // No special index required.
                }
            }
        }
    }

    public Map<String, ToscaNode> getParameterLayerMap() {
        return parameterLayerMap;
    }

    public Map<String, ToscaNode> getParameterMap() {
        return parameterMap;
    }

    public ToscaNode getParameterLayer(String id) {
        return parameterLayerMap.get(id);
    }

    public ToscaNode getParameter(String id) {
        return parameterMap.get(id);
    }

    // ---------------------------------------------------
    // CATEGORIZATION
    // ---------------------------------------------------
    private void categorize() {

        for (ToscaNode node :
                fullObjectMap.values()) {

            String type =
                    node.objectClass;

            if ("TestCase".equals(type)) {

                if (node.extraAssocs.containsKey("TemplateDetail")) {
                    tsuInventory.get(TEMPLATES).add(node);
                } else {
                    tsuInventory.get(TESTCASES).add(node);
                }

            } else if ("ReuseableTestStepBlock".equals(type)) {

                tsuInventory.get(REUSABLES).add(node);

            } else if ("TestSheet".equals(type)) {

                tsuInventory.get(TCDSHEETS).add(node);
            }
        }
    }

    // ---------------------------------------------------
    // GETTERS
    // ---------------------------------------------------
    public Map<String, List<ToscaNode>> getTsuInventory() {
        return tsuInventory;
    }

    public Map<String, ToscaNode> getFullObjectMap() {
        return fullObjectMap;
    }

    public List<ToscaNode> getTestCases() {
        return tsuInventory.get(TESTCASES);
    }

    public List<ToscaNode> getReusables() {
        return tsuInventory.get(REUSABLES);
    }

    public List<ToscaNode> getTemplates() {
        return tsuInventory.get(TEMPLATES);
    }

    public List<ToscaNode> getTcdSheets() {
        return tsuInventory.get(TCDSHEETS);
    }

    public void printInventorySummary(
            Map<String, List<ToscaNode>> tsuInventoryLoc) {

        print(
                "TEST CASES",
                tsuInventoryLoc.get(TESTCASES)
        );

        print(
                "REUSABLES",
                tsuInventoryLoc.get(REUSABLES)
        );

        print(
                "TEMPLATES",
                tsuInventoryLoc.get(TEMPLATES)
        );

        print(
                "TCD SHEETS",
                tsuInventoryLoc.get(TCDSHEETS)
        );
    }

    private void print(
            String title,
            List<ToscaNode> list) {

        MigrationLog.info(
                title
                        + " ("
                        + (list == null ? 0 : list.size())
                        + ")"
        );

        if (list == null || list.isEmpty()) {

            MigrationLog.info(
                    "   (empty)"
            );

            return;
        }

        for (ToscaNode n : list) {

            MigrationLog.debug(
                    n.surrogate
                            + " | "
                            + n.getName()
            );
        }
    }
}
