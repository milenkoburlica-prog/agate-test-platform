package at.co.svc.tosca.testcases;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class CorrectDBObjects {

    private final Map<String, JsonNode> tsuIndex = new HashMap<>();
    private final Map<String, JsonNode> testStepValueIndex = new HashMap<>();

    // =====================================================
    // MAIN
    // =====================================================
    public static void main(String[] args) throws Exception {

        String agateInput =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KH6JVGKVDS1614ZS90RBX03C_testcases-extended-compressed_step2_step3_step4.json";

        String tsuFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\EcSrvCTS.tsu";

        ObjectMapper mapper = new ObjectMapper();

        JsonNode agateRoot = mapper.readTree(new File(agateInput));
        JsonNode tsuRoot = loadTsuSafe(tsuFile, mapper);

        CorrectDBObjects parser = new CorrectDBObjects();
        parser.indexTsu(tsuRoot);

        ArrayNode result = mapper.createArrayNode();

        for (JsonNode node : agateRoot) {

            if (!"XTestStep".equals(node.path("ObjectClass").asText())) {
                result.add(node);
                continue;
            }

            ObjectNode step = (ObjectNode) node;

            if (containsSqlSelect(step)) {
                parser.enrichDbStep(step, mapper);
            }

            result.add(step);
        }

        String out = agateInput.replace(".json", "_db_enriched.json");

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(out), result);

        MigrationLog.success(
                "DB object correction output: " + out
        );
    }

    // =====================================================
    // TSU LOAD
    // =====================================================
    private static JsonNode loadTsuSafe(
            String path,
            ObjectMapper mapper) throws Exception {

        try (InputStream fis = new FileInputStream(path);
             GZIPInputStream gis = new GZIPInputStream(fis)) {

            return mapper.readTree(gis);
        }
    }

    // =====================================================
    // INDEX
    // =====================================================
    private void indexTsu(JsonNode tsuRoot) {

        JsonNode entities = tsuRoot.path("Entities");

        for (JsonNode node : entities) {

            String surrogate =
                    node.path("Surrogate").asText();

            if (!surrogate.isBlank()) {
                tsuIndex.put(surrogate, node);
            }

            if ("XTestStepValue".equals(
                    node.path("ObjectClass").asText())) {

                testStepValueIndex.put(
                        surrogate,
                        node
                );
            }
        }
    }

    // =====================================================
    // SQL DETECTION
    // =====================================================
    private static boolean containsSqlSelect(JsonNode node) {

        for (JsonNode p : node.path("Parameters")) {

            String explicit =
                    p.path("ExplicitName").asText("");

            String value =
                    p.path("Value").asText("");

            if ("SQL Statement".equals(explicit)
                    && value.toUpperCase().contains("SELECT")) {

                return true;
            }
        }

        return false;
    }

    // =====================================================
    // ENRICH DB STEP
    // =====================================================
    private ObjectNode enrichDbStep(
            ObjectNode agateNode,
            ObjectMapper mapper) {

        String surrogate =
                agateNode.path("surrogate").asText();

        JsonNode tsuStep =
                tsuIndex.get(surrogate);

        if (tsuStep == null) {
            return agateNode;
        }

        JsonNode values =
                tsuStep.path("Assocs")
                        .path("TestStepValues");

        ArrayNode newParams =
                mapper.createArrayNode();

        for (JsonNode valueId : values) {

            JsonNode valueNode =
                    testStepValueIndex.get(
                            valueId.asText()
                    );

            if (valueNode != null) {
                walk(
                        valueNode,
                        newParams,
                        mapper
                );
            }
        }

        agateNode.set(
                "Parameters",
                newParams
        );

        return agateNode;
    }

    // =====================================================
    // WALK
    // =====================================================
    private void walk(
            JsonNode node,
            ArrayNode out,
            ObjectMapper mapper) {

        String explicit =
                node.path("Attributes")
                        .path("ExplicitName")
                        .asText("");

        String value =
                node.path("Attributes")
                        .path("Value")
                        .asText("");

        // Special mapping required for DB Expert steps
        if ("token_db".equals(value)) {
            addDbExpertMapping(
                    out,
                    mapper
            );
        }

        if (explicit.startsWith("#")) {

            ObjectNode param =
                    mapper.createObjectNode();

            param.put(
                    "ModuleAttributeSurrogate",
                    node.path("Assocs")
                            .path("ModuleAttribute")
                            .path(0)
                            .asText("")
            );

            param.put(
                    "Value",
                    value
            );

            param.put(
                    "ExplicitName",
                    explicit
            );

            param.put(
                    "ActionMode",
                    node.path("Attributes")
                            .path("ActionMode")
                            .asText("")
            );

            out.add(param);
        }

        for (JsonNode sub :
                node.path("Assocs").path("SubValues")) {

            JsonNode child =
                    testStepValueIndex.get(
                            sub.asText()
                    );

            if (child != null) {
                walk(
                        child,
                        out,
                        mapper
                );
            }
        }
    }

    // =====================================================
    // DB EXPERT MAPPING
    // =====================================================
    private void addDbExpertMapping(
            ArrayNode out,
            ObjectMapper mapper) {

        ObjectNode param =
                mapper.createObjectNode();

        param.put(
                "ModuleAttributeSurrogate",
                "tosca"
        );

        param.put(
                "Value",
                "#1.#1"
        );

        param.put(
                "ExplicitName",
                "token_db"
        );

        param.put(
                "ActionMode",
                "Insert"
        );

        out.add(param);

        MigrationLog.debug(
                "DB Expert mapping added: token_db -> #1.#1"
        );
    }

    // =====================================================
    // PROCESS FILE
    // =====================================================
    public static String processFile(
            String agateFile,
            String tsuFile) throws Exception {

        ObjectMapper mapper =
                new ObjectMapper();

        JsonNode agateRoot =
                mapper.readTree(
                        new File(agateFile)
                );

        JsonNode tsuRoot =
                loadTsuSafe(
                        tsuFile,
                        mapper
                );

        CorrectDBObjects engine =
                new CorrectDBObjects();

        engine.indexTsu(tsuRoot);

        ArrayNode result =
                mapper.createArrayNode();

        for (JsonNode node : agateRoot) {

            if (!"XTestStep".equals(
                    node.path("ObjectClass").asText())) {

                result.add(node);
                continue;
            }

            ObjectNode step =
                    (ObjectNode) node;

            if (containsSqlSelect(step)) {
                engine.enrichDbStep(
                        step,
                        mapper
                );
            }

            result.add(step);
        }

        String out =
                agateFile.replace(
                        ".json",
                        "_db_fixed.json"
                );

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File(out),
                        result
                );

        return out;
    }
}
