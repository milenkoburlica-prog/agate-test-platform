package at.co.svc.tosca.testcases;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class XTestStepExtend {

    public static void main(String[] args) throws Exception {

        String tsuFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\DMP_getMedPatientenInformationen.tsu";

        String inputFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KES2VE001YTSWWGQQQ2YY28M_testcases.json";

        inputFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\3a0ae521-a4bb-d67d-c70e-2d019bfb33f2_reusables.json";

        String outDir =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut";

        extend(
                inputFile,
                tsuFile,
                outDir
        );

        MigrationLog.success(
                "XTestStep extension completed."
        );
    }

    // =====================================================
    // EXTEND
    // =====================================================

    public static void extend(
            String inputFile,
            String tsuFile,
            String outDir) throws Exception {

        JsonArray testcaseArray;

        try (FileReader reader =
                     new FileReader(inputFile)) {

            testcaseArray =
                    JsonParser.parseReader(reader)
                            .getAsJsonArray();
        }

        JsonObject tsuRoot =
                loadTsu(tsuFile);

        JsonArray entities =
                tsuRoot.getAsJsonArray("Entities");

        // Index TSU entities by surrogate.
        Map<String, JsonObject> tsuIndex =
                new HashMap<>();

        for (JsonElement el : entities) {

            JsonObject obj =
                    el.getAsJsonObject();

            String id =
                    getString(
                            obj,
                            "Surrogate"
                    );

            tsuIndex.put(
                    id,
                    obj
            );
        }

        JsonArray result =
                new JsonArray();

        Set<String> visited =
                new HashSet<>();

        Set<String> stack =
                new HashSet<>();

        for (JsonElement el : testcaseArray) {

            JsonObject node =
                    el.getAsJsonObject();

            expandNode(
                    node,
                    tsuIndex,
                    result,
                    visited,
                    stack
            );
        }

        // Save the extended JSON file.
        String inputName =
                Paths.get(inputFile)
                        .getFileName()
                        .toString();

        String outputName =
                inputName.replace(
                        ".json",
                        "-extended.json"
                );

        Path outputPath =
                Paths.get(
                        outDir,
                        outputName
                );

        Gson gson =
                new GsonBuilder()
                        .setPrettyPrinting()
                        .create();

        Files.writeString(
                outputPath,
                gson.toJson(result),
                StandardCharsets.UTF_8
        );

        MigrationLog.info(
                "Extended JSON written: "
                        + outputPath
        );
    }

    // =====================================================
    // CORE EXPAND LOGIC
    // =====================================================

    private static void expandNode(
            JsonObject node,
            Map<String, JsonObject> tsuIndex,
            JsonArray result,
            Set<String> visited,
            Set<String> stack) {

        String surrogate =
                getString(
                        node,
                        "Surrogate"
                );

        if (surrogate.isEmpty()) {
            return;
        }

        // =====================================================
        // SKIP DISABLED STEPS
        // =====================================================

        if (node.has("Attributes")
                && node.getAsJsonObject("Attributes")
                        .has("DisabledDescription")) {

            String desc =
                    node.getAsJsonObject("Attributes")
                            .get("DisabledDescription")
                            .getAsString();

            if (!desc.isEmpty()) {

                // A non-empty DisabledDescription means that the step is disabled.
                return;
            }
        }

        // =====================================================
        // CYCLE GUARD
        // =====================================================

        if (stack.contains(surrogate)) {
            return;
        }

        stack.add(surrogate);

        // =====================================================
        // OUTPUT DEDUPLICATION
        // =====================================================

        boolean isNew =
                visited.add(surrogate);

        if (isNew) {
            result.add(node);
        }

        String objectClass =
                getString(
                        node,
                        "ObjectClass"
                );

        // =====================================================
        // XTESTSTEP
        //
        // EXISTING LOGIC - UNCHANGED
        // =====================================================

        if ("XTestStep".equals(objectClass)) {

            JsonArray values =
                    safeArray(
                            node,
                            "Assocs",
                            "TestStepValues"
                    );

            for (JsonElement el : values) {

                JsonObject obj =
                        tsuIndex.get(
                                el.getAsString()
                        );

                if (obj != null) {

                    expandNode(
                            obj,
                            tsuIndex,
                            result,
                            visited,
                            stack
                    );
                }
            }
        }

        // =====================================================
        // XTESTSTEPVALUE
        //
        // EXISTING LOGIC - UNCHANGED
        // =====================================================

        else if ("XTestStepValue".equals(objectClass)) {

            JsonObject assocs =
                    node.getAsJsonObject("Assocs");

            if (assocs != null) {

                // -------------------------------------------------
                // SUB VALUES
                // -------------------------------------------------

                JsonArray subValues =
                        assocs.getAsJsonArray("SubValues");

                if (subValues != null) {

                    for (JsonElement el : subValues) {

                        JsonObject obj =
                                tsuIndex.get(
                                        el.getAsString()
                                );

                        if (obj != null) {

                            expandNode(
                                    obj,
                                    tsuIndex,
                                    result,
                                    visited,
                                    stack
                            );
                        }
                    }
                }

                // -------------------------------------------------
                // PARENT VALUES
                // -------------------------------------------------

                JsonArray parentValues =
                        assocs.getAsJsonArray("ParentValue");

                if (parentValues != null) {

                    for (JsonElement el : parentValues) {

                        JsonObject obj =
                                tsuIndex.get(
                                        el.getAsString()
                                );

                        if (obj != null) {

                            expandNode(
                                    obj,
                                    tsuIndex,
                                    result,
                                    visited,
                                    stack
                            );
                        }
                    }
                }
            }
        }

        // =====================================================
        // TEST CASE CONTROL FLOW ITEM
        //
        // NEW:
        //
        // TestCaseControlFlowItem
        //      |
        //      +-- ControlFlowFolders[]
        //
        // Example:
        //
        // Warte bis Datensätze ... existieren
        //
        //      +-- Wenn Datensatz nicht existiert
        //      +-- Warte auf Datensätze in der DB
        //
        // Existing behavior for all other object types
        // remains unchanged.
        // =====================================================

        else if ("TestCaseControlFlowItem".equals(objectClass)) {

            JsonArray folders =
                    safeArray(
                            node,
                            "Assocs",
                            "ControlFlowFolders"
                    );

            for (JsonElement el : folders) {

                JsonObject obj =
                        tsuIndex.get(
                                el.getAsString()
                        );

                if (obj != null) {

                    expandNode(
                            obj,
                            tsuIndex,
                            result,
                            visited,
                            stack
                    );
                }
            }
        }

        // =====================================================
        // TEST CASE CONTROL FLOW FOLDER
        //
        // NEW:
        //
        // TestCaseControlFlowFolder
        //      |
        //      +-- Items[]
        //             |
        //             +-- XTestStep
        //
        // The child XTestStep is then processed by the EXISTING
        // XTestStep logic above.
        //
        // This is important:
        //
        // We do NOT introduce a special SQL/WAIT implementation
        // for LOOPs.
        //
        // The existing SQL/WAIT processing remains responsible
        // for those steps.
        // =====================================================

        else if ("TestCaseControlFlowFolder".equals(objectClass)) {

            JsonArray items =
                    safeArray(
                            node,
                            "Assocs",
                            "Items"
                    );

            for (JsonElement el : items) {

                JsonObject obj =
                        tsuIndex.get(
                                el.getAsString()
                        );

                if (obj != null) {

                    expandNode(
                            obj,
                            tsuIndex,
                            result,
                            visited,
                            stack
                    );
                }
            }
        }

        // =====================================================
        // TESTSTEPFOLDERREFERENCE
        //
        // EXISTING LOGIC - UNCHANGED
        // =====================================================

        else if ("TestStepFolderReference".equals(objectClass)) {

            JsonArray layers =
                    safeArray(
                            node,
                            "Assocs",
                            "ParameterLayerReference"
                    );

            for (JsonElement el : layers) {

                JsonObject obj =
                        tsuIndex.get(
                                el.getAsString()
                        );

                if (obj != null) {

                    expandNode(
                            obj,
                            tsuIndex,
                            result,
                            visited,
                            stack
                    );
                }
            }
        }

        // =====================================================
        // PARAMETER LAYER REFERENCE
        //
        // EXISTING LOGIC - UNCHANGED
        // =====================================================

        else if ("ParameterLayerReference".equals(objectClass)) {

            JsonArray refs =
                    safeArray(
                            node,
                            "Assocs",
                            "AllParameterReferences"
                    );

            for (JsonElement el : refs) {

                JsonObject obj =
                        tsuIndex.get(
                                el.getAsString()
                        );

                if (obj != null) {

                    expandNode(
                            obj,
                            tsuIndex,
                            result,
                            visited,
                            stack
                    );
                }
            }
        }

        // =====================================================
        // PARAMETER REFERENCE
        //
        // EXISTING LOGIC - UNCHANGED
        // =====================================================

        else if ("ParameterReference".equals(objectClass)) {

            JsonArray params =
                    safeArray(
                            node,
                            "Assocs",
                            "Parameter"
                    );

            for (JsonElement el : params) {

                JsonObject obj =
                        tsuIndex.get(
                                el.getAsString()
                        );

                if (obj != null) {

                    expandNode(
                            obj,
                            tsuIndex,
                            result,
                            visited,
                            stack
                    );
                }
            }
        }

        // =====================================================
        // LEAF: Parameter
        //
        // EXISTING LOGIC - UNCHANGED
        // =====================================================

        else if ("Parameter".equals(objectClass)) {

            // No further expansion is required.
        }

        // =====================================================
        // BACKTRACK
        // =====================================================

        stack.remove(surrogate);
    }

    // =====================================================
    // SAFE ARRAY
    // =====================================================

    private static JsonArray safeArray(
            JsonObject node,
            String a,
            String b) {

        JsonObject obj =
                node.getAsJsonObject(a);

        if (obj == null) {
            return new JsonArray();
        }

        JsonArray arr =
                obj.getAsJsonArray(b);

        return arr != null
                ? arr
                : new JsonArray();
    }

    // =====================================================
    // LOAD TSU
    // =====================================================

    private static JsonObject loadTsu(
            String tsuFile) throws Exception {

        try (FileInputStream fis =
                     new FileInputStream(tsuFile);

             GZIPInputStream gis =
                     new GZIPInputStream(fis);

             InputStreamReader reader =
                     new InputStreamReader(
                             gis,
                             StandardCharsets.UTF_8
                     )) {

            return JsonParser.parseReader(reader)
                    .getAsJsonObject();
        }
    }

    // =====================================================
    // HELPER
    // =====================================================

    private static String getString(
            JsonObject obj,
            String key) {

        if (obj == null
                || obj.get(key) == null
                || obj.get(key).isJsonNull()) {

            return "";
        }

        return obj.get(key)
                .getAsString();
    }
}