package at.co.svc.tosca.transformation;

import java.io.File;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class ToscaParserPhase2 {

    // =====================================================
    // MAIN
    // =====================================================
    public static void main(String[] args) throws Exception {

        String input =
                "C:\\work\\projects\\agate-studio\\agate-tosca-migration-svc\\jsonOut\\01KTFF9GQE1KGEW9PHQGGR2JQX_testcases-extended-compressed_step2_step3_step4_agate-step1.json";

        ObjectMapper mapper =
                new ObjectMapper();

        JsonNode root =
                mapper.readTree(
                        new File(input)
                );

        ObjectNode output =
                mapper.createObjectNode();

        ArrayNode stepsOut =
                mapper.createArrayNode();

        // =====================================================
        // HEADER
        // =====================================================
        JsonNode header =
                root.get(0);

        output.put(
                "name",
                extractName(header)
        );

        output.put(
                "surrogate",
                header.path("Surrogate")
                        .asText()
        );

        // =====================================================
        // STEPS
        // =====================================================
        int index = 1;

        for (int i = 1; i < root.size(); i++) {

            JsonNode node =
                    root.get(i);

            if (!"XTestStep".equals(
                    node.path("ObjectClass").asText())
                    && !"TestStepFolderReference".equals(
                    node.path("ObjectClass").asText())) {

                continue;
            }

            ObjectNode step =
                    transformStep(
                            node,
                            mapper,
                            index++
                    );

            stepsOut.add(step);
        }

        output.set(
                "steps",
                stepsOut
        );

        // =====================================================
        // WRITE OUTPUT
        // =====================================================
        String out =
                input.replace(
                        "_agate-step1.json",
                        "_agate-step2.json"
                );

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File(out),
                        output
                );

        MigrationLog.success(
                "Tosca parser phase 2 output: "
                        + out
        );
    }

    // =====================================================
    // HEADER NAME EXTRACTION
    // =====================================================
    private static String extractName(
            JsonNode header) {

        String name =
                header.path("Attributes")
                        .path("Name")
                        .asText("");

        if (name.isBlank()) {
            name =
                    header.path("Name")
                            .asText("");
        }

        return name;
    }

    // =====================================================
    // STEP TRANSFORM
    // =====================================================
    private static ObjectNode transformStep(
            JsonNode node,
            ObjectMapper mapper,
            int index) {

        ObjectNode out =
                mapper.createObjectNode();

        String oc =
                node.path("ObjectClass")
                        .asText();

        out.put("index", index);
        out.put("name", node.path("Name").asText(""));
        out.putNull("type");
        out.putNull("op");
        out.put("surrogate", node.path("surrogate").asText(""));

        out.put("module", node.path("module").asText(null));
        out.put("moduleSurrogate", node.path("moduleSurrogate").asText(null));
        out.put("moduleClass", node.path("moduleClass").asText(null));

        // Reusable detection
        if ("TestStepFolderReference".equals(oc)) {

            out.put("type", "Reusable");
            out.put("reusableName", node.path("reusableName").asText(null));
            out.put("reusableSurrogate", node.path("reusableSurrogate").asText(null));

        } else {

            out.putNull("reusableName");
            out.putNull("reusableSurrogate");
        }

        out.put("condition", node.path("condition").asText(null));
        out.put("action", node.path("op").asText(null));

        // =====================================================
        // VALUES TRANSFER
        // Supports both object and array representations.
        // =====================================================
        ArrayNode valuesOut =
                mapper.createArrayNode();

        JsonNode values =
                node.path("values");

        if (values.isArray()) {

            // CASE 1: values is an array.
            for (JsonNode entry : values) {

                ObjectNode v =
                        mapper.createObjectNode();

                v.put("name", entry.path("name").asText(""));
                v.put("value", entry.path("value").asText(""));
                v.put("actionMode", entry.path("actionMode").asText(""));

                copyOptionalField(entry, v, "actionProperty");
                copyOptionalField(entry, v, "toscaPath");
                copyOptionalField(entry, v, "toscaPathID");
                copyOptionalField(entry, v, "xmlPath");
                copyOptionalField(entry, v, "jsonPath");
                copyOptionalField(entry, v, "xCondition");

                valuesOut.add(v);
            }

        } else if (values.isObject()) {

            // CASE 2: values is an object.
            java.util.Iterator<Map.Entry<String, JsonNode>> fields =
                    values.fields();

            while (fields.hasNext()) {

                Map.Entry<String, JsonNode> field =
                        fields.next();

                JsonNode entry =
                        field.getValue();

                ObjectNode v =
                        mapper.createObjectNode();

                v.put("name", field.getKey());
                v.put("value", entry.path("value").asText(""));
                v.put("actionMode", entry.path("actionMode").asText(""));

                copyOptionalField(entry, v, "actionProperty");
                copyOptionalField(entry, v, "toscaPath");
                copyOptionalField(entry, v, "toscaPathID");
                copyOptionalField(entry, v, "xmlPath");
                copyOptionalField(entry, v, "jsonPath");
                copyOptionalField(entry, v, "xCondition");

                valuesOut.add(v);
            }
        }

        out.set(
                "values",
                valuesOut
        );

        return out;
    }

    private static void copyOptionalField(
            JsonNode source,
            ObjectNode target,
            String fieldName) {

        if (source.has(fieldName)
                && !source.path(fieldName).asText().isBlank()) {

            target.put(
                    fieldName,
                    source.path(fieldName).asText()
            );
        }
    }

    public static String processFile(
            String inputFile,
            String tsuFile) throws Exception {

        ObjectMapper mapper =
                new ObjectMapper();

        JsonNode root =
                mapper.readTree(
                        new File(inputFile)
                );

        ObjectNode output =
                mapper.createObjectNode();

        ArrayNode stepsOut =
                mapper.createArrayNode();

        // Header
        JsonNode header =
                root.get(0);

        output.put(
                "name",
                extractName(header)
        );

        // Resolve the surrogate with an Attributes fallback.
        String surrogate =
                header.path("Surrogate")
                        .asText();

        if (surrogate.isBlank()) {

            surrogate =
                    header.path("Attributes")
                            .path("Surrogate")
                            .asText("");
        }

        output.put(
                "surrogate",
                surrogate
        );

        int index = 1;

        for (int i = 1; i < root.size(); i++) {

            JsonNode node =
                    root.get(i);

            if (!"XTestStep".equals(
                    node.path("ObjectClass").asText())
                    && !"TestStepFolderReference".equals(
                    node.path("ObjectClass").asText())) {

                continue;
            }

            ObjectNode step =
                    transformStep(
                            node,
                            mapper,
                            index++
                    );

            stepsOut.add(step);
        }

        output.set(
                "steps",
                stepsOut
        );

        String out =
                inputFile.replace(
                        "_agate-step1.json",
                        "_agate-step2.json"
                );

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File(out),
                        output
                );

        return out;
    }
}
