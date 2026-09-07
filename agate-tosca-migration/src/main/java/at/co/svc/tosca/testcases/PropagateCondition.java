package at.co.svc.tosca.testcases;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class PropagateCondition {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws Exception {

        String tsuFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\tsu\\DMP_getMedPatientenInformationen.tsu";

        String inputFile =
                "C:\\work\\projects\\playwright\\agate-studio\\agate-tosca-migration-f\\jsonOut\\01KRAYCQDX6FQZZY5B4FFZQJ4S_templates-extended-compressed_step2.json";

        String step1Output =
                PropagateCondition.run(inputFile);

        MigrationLog.info(
                "Condition propagation step 1 output: "
                        + step1Output
        );

        // STEP 2: propagate IF conditions
        String step2Output =
                PropagateConditionStep2.run(step1Output);

        MigrationLog.info(
                "Condition propagation step 2 output: "
                        + step2Output
        );

        // Additional processing steps can be added here later.
    }

    public static String run(String inputFile) throws Exception {

        String outputFile =
                buildOutputFile(inputFile);

        List<Map<String, Object>> nodes =
                MAPPER.readValue(
                        new File(inputFile),
                        new TypeReference<List<Map<String, Object>>>() {
                        }
                );

        Map<String, Map<String, Object>> index =
                new HashMap<>();

        for (Map<String, Object> n : nodes) {
            index.put(
                    asText(n.get("Surrogate")),
                    n
            );
        }

        for (Map<String, Object> node : nodes) {

            if ("TestCase".equals(
                    asText(node.get("ObjectClass")))) {

                propagate(
                        node,
                        null,
                        index,
                        new HashSet<>()
                );
            }
        }

        MAPPER.writeValue(
                new File(outputFile),
                nodes
        );

        return outputFile;
    }

    private static String buildOutputFile(String inputFile) {

        return inputFile.replace(
                ".json",
                "_condition1.json"
        );
    }

    // =====================================================
    // CORE PROPAGATION
    // =====================================================
    private static void propagate(
            Map<String, Object> node,
            String parentCondition,
            Map<String, Map<String, Object>> index,
            Set<String> visited) {

        String id =
                asText(node.get("Surrogate"));

        if (visited.contains(id)) {
            return;
        }

        visited.add(id);

        Map<String, Object> attrs =
                asMap(node.get("Attributes"));

        String currentCondition =
                normalize(
                        asText(
                                attrs.get("Condition")
                        )
                );

        String merged =
                merge(
                        parentCondition,
                        currentCondition
                );

        if (!merged.isEmpty()) {
            attrs.put(
                    "Condition",
                    merged
            );
        }

        Map<String, Object> assocs =
                asMap(node.get("Assocs"));

        // -------------------------------------------------
        // 1. NORMAL TREE (Items)
        // -------------------------------------------------
        List<String> items =
                asStringList(
                        assocs.get("Items")
                );

        for (String childId : items) {

            Map<String, Object> child =
                    index.get(childId);

            if (child != null) {
                propagate(
                        child,
                        merged,
                        index,
                        visited
                );
            }
        }

        // -------------------------------------------------
        // 2. FolderReference -> resolve reused item
        // -------------------------------------------------
        if ("TestStepFolderReference".equals(
                asText(node.get("ObjectClass")))) {

            List<String> reused =
                    asStringList(
                            assocs.get("ReusedItem")
                    );

            for (String targetId : reused) {

                Map<String, Object> target =
                        index.get(targetId);

                if (target != null) {
                    propagate(
                            target,
                            merged,
                            index,
                            visited
                    );
                }
            }
        }
    }

    // =====================================================
    // MERGE RULES
    // =====================================================
    private static String merge(
            String parent,
            String current) {

        parent = normalize(parent);
        current = normalize(current);

        if (parent.isEmpty()
                && current.isEmpty()) {

            return "";
        }

        if (parent.isEmpty()) {
            return current;
        }

        if (current.isEmpty()) {
            return parent;
        }

        return "("
                + parent
                + ") AND ("
                + current
                + ")";
    }

    private static String normalize(String s) {

        return s == null
                ? ""
                : s.trim();
    }

    // =====================================================
    // HELPERS
    // =====================================================
    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {

        return o == null
                ? new LinkedHashMap<>()
                : (Map<String, Object>) o;
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object o) {

        return o == null
                ? new ArrayList<>()
                : (List<String>) o;
    }

    private static String asText(Object o) {

        return o == null
                ? ""
                : String.valueOf(o);
    }
}
