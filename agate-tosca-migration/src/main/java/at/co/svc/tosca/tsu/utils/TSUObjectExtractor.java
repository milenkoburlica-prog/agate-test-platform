package at.co.svc.tosca.tsu.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import at.co.svc.aga.transformator.utils.MigrationLog;
import at.co.svc.tosca.tsu.dto.ToscaNode;

public class TSUObjectExtractor {

    /**
     * Which associations should be traversed
     */
    private static final Set<String> REF_KEYS = Set.of(
            "Items",
            "ControlFlowFolders",
            "TestCase",
            "ConfigurationLinks",
            "Properties",
            "ReferencedBy",
            "ExecutionEntries",
            "ExecutionLogs",
            "AttachedFiles",

            // === IMPORTANT TSU MODEL EXTENSIONS ===
            //"Module",
            "ParameterLayer",
            //"ParameterLayerReference",
            //"ParameterLayerReferences",
            "Parameters",
            "TestStepValues",
            "SubValues",
            "Parameter",
            "ParameterReference"
    );
    
    /**
     * Optional traversal switches
     */
    private final boolean includeDerived;
    private final boolean skipDisabled;

    /**
     * All objects from TSU
     */
    private final Map<String, ToscaNode> fullObjectMap;

    /**
     * Object classes to skip completely
     */
    private final Set<String> excludedObjectClasses;

    // =====================================================
    // CONSTRUCTORS
    // =====================================================

    public TSUObjectExtractor(
            Map<String, ToscaNode> fullObjectMap,
            boolean includeDerived,
            boolean skipDisabled,
            Set<String> excludedObjectClasses) {

        this.fullObjectMap = fullObjectMap;
        this.includeDerived = includeDerived;
        this.skipDisabled = skipDisabled;

        this.excludedObjectClasses =
                excludedObjectClasses != null
                        ? excludedObjectClasses
                        : Set.of();
    }

    public TSUObjectExtractor(String tsuPath) throws Exception {
        this(tsuPath, false, true, Set.of());
    }

    public TSUObjectExtractor(
            String tsuPath,
            boolean includeDerived,
            boolean skipDisabled,
            Set<String> excludedObjectClasses) throws Exception {

        this.includeDerived = includeDerived;
        this.skipDisabled = skipDisabled;

        this.excludedObjectClasses =
                excludedObjectClasses != null
                        ? excludedObjectClasses
                        : Set.of();

        String jsonContent =
                GzipHelper.extractGzipContent(tsuPath);

        this.fullObjectMap = new HashMap<>();

        parse(jsonContent);
    }

    // =====================================================
    // MAIN
    // =====================================================

    public static void main(String[] args) throws Exception {

        String tsuPath =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\DMP_getMedPatientenInformationen.tsu";

        String rootSurrogate =
                "01KES2VDZWDTPT28AFD9FRWJMG";

        // OUTPUT SETTINGS
        String jsonOutFolder =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut";

        String suffix = "testcases";
 
        /**
         * TSUObjectExtractor toe =
        new TSUObjectExtractor(
                false,
                true,
                Set.of(
                        "RecoveryScenarioCollection",
                        "RecoveryScenario"
                )
        );

toe.start(
        tsuPath,
        rootSurrogate,
        jsonOutFolder,
        suffix
);
         */
        start(tsuPath, rootSurrogate, jsonOutFolder, suffix);
    }

    public static void start(String tsuPath, String rootSurrogate, String jsonOutFolder, String suffix)
            throws Exception, IOException {
        // OPTIONAL EXCLUSIONS
        Set<String> excludedClasses = Set.of(
                "RecoveryScenarioCollection",
                "RecoveryScenario"
        );

        TSUObjectExtractor extractor =
                new TSUObjectExtractor(
                        tsuPath,
                        false,               // includeDerived
                        true,                // skipDisabled
                        excludedClasses
                );

        JsonArray result =
                extractor.extractAsJson(rootSurrogate);

        // -------------------------------------------------
        // CREATE OUTPUT FOLDER
        // -------------------------------------------------

        File outDir = new File(jsonOutFolder);

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        // -------------------------------------------------
        // OUTPUT FILE
        // -------------------------------------------------

        String outputFile =
                jsonOutFolder
                        + File.separator
                        + rootSurrogate
                        + "_"
                        + suffix
                        + ".json";

        // -------------------------------------------------
        // SAVE JSON
        // -------------------------------------------------

        try (FileWriter fw = new FileWriter(outputFile)) {

            new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(result, fw);
        }

        MigrationLog.success(
                "TSU object extraction completed: "
                        + outputFile
                        + " | objects="
                        + result.size()
        );

        MigrationLog.debug(
                "Input TSU: "
                        + tsuPath
        );
    }

    // =====================================================
    // PUBLIC API
    // =====================================================

    public Map<String, ToscaNode> extract(String surrogateId) {

        Map<String, ToscaNode> result =
                new LinkedHashMap<>();

        Set<String> visiting = new HashSet<>();
        extractRecursive(surrogateId, result, visiting);
        

        return result;
    }

    public JsonArray extractAsJson(String rootSurrogateId) {

        Map<String, ToscaNode> result = extract(rootSurrogateId);

        JsonArray arr = new JsonArray();

        for (ToscaNode node : result.values()) {

            JsonObject obj = new JsonObject();

            // -------------------------------------------------
            // EXACT ORIGINAL STRUCTURE
            // -------------------------------------------------

            obj.addProperty("ObjectClass", node.objectClass);
            obj.addProperty("Surrogate", node.surrogate);

            // -------------------------------------------------
            // ATTRIBUTES
            // -------------------------------------------------

            JsonObject attrs = new JsonObject();

            for (Map.Entry<String, Object> e :
                    node.attributes.entrySet()) {

                Object val = e.getValue();

                attrs.add(
                        e.getKey(),
                        val == null
                                ? JsonNull.INSTANCE
                                : new Gson().toJsonTree(val)
                );
            }

            obj.add("Attributes", attrs);

            // -------------------------------------------------
            // ASSOCIATIONS
            // -------------------------------------------------

            JsonObject assocs = new JsonObject();

            for (Map.Entry<String, List<String>> e :
                    node.extraAssocs.entrySet()) {

                JsonArray ids = new JsonArray();

                for (String id : e.getValue()) {
                    ids.add(id);
                }

                assocs.add(e.getKey(), ids);
            }

            obj.add("Assocs", assocs);

            arr.add(obj);
        }

        return arr;
    }

    // =====================================================
    // RECURSION ENGINE
    // =====================================================

    private void extractRecursive(
            String surrogateId,
            Map<String, ToscaNode> result,
            Set<String> visiting) {
        
        if (visiting.contains(surrogateId)) {
            return;
        }

        visiting.add(surrogateId);

        // already processed
        if (result.containsKey(surrogateId)) {
            return;
        }

        ToscaNode node =
                fullObjectMap.get(surrogateId);

        if (node == null) {
            return;
        }

        // -------------------------------------------------
        // EXCLUDED OBJECT CLASSES
        // -------------------------------------------------

        if (excludedObjectClasses.contains(node.objectClass)) {
            return;
        }

        // -------------------------------------------------
        // SKIP DISABLED
        // -------------------------------------------------

        if (skipDisabled && isDisabled(node)) {
            return;
        }

        // store
        result.put(surrogateId, node);

        // -------------------------------------------------
        // FOLLOW STANDARD ASSOCIATIONS
        // -------------------------------------------------

        for (String refKey : REF_KEYS) {

            List<String> refs =
                    node.extraAssocs.get(refKey);

            if (refs == null) {
                continue;
            }

            for (String refId : refs) {
                extractRecursive(refId, result, visiting);
            }
        }

        // -------------------------------------------------
        // OPTIONAL: FOLLOW DERIVED
        // -------------------------------------------------

        if (includeDerived) {

            List<String> derived =
                    node.extraAssocs.get("DerivedFrom");

            if (derived != null) {

                for (String id : derived) {
                    extractRecursive(id, result, visiting);
                }
            }
        }
        visiting.remove(surrogateId);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private boolean isDisabled(ToscaNode node) {

        Object d =
                node.attributes.get("DisabledDescription");

        if ((d == null) || ("".equals(d)))  
            return false;
        else
           return true;
//        return d != null
//                && !String.valueOf(d).isEmpty();
    }

    // =====================================================
    // PARSER
    // =====================================================

    private void parse(String jsonContent) {

        JsonObject root =
                JsonParser.parseString(jsonContent)
                        .getAsJsonObject();

        JsonArray entities =
                root.getAsJsonArray("Entities");

        for (JsonElement el : entities) {

            JsonObject obj = el.getAsJsonObject();

            ToscaNode node = new ToscaNode();

            node.objectClass =
                    obj.get("ObjectClass").getAsString();

            node.surrogate =
                    obj.get("Surrogate").getAsString();

            // -------------------------------------------------
            // ATTRIBUTES
            // -------------------------------------------------

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

            // -------------------------------------------------
            // ASSOCIATIONS
            // -------------------------------------------------

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

                        ids.add(id.getAsString());
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
        }
    }
}