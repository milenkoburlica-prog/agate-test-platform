package at.co.svc.agate.core.reference.json;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import at.co.svc.agate.core.reference.ComparisonDifference;
import at.co.svc.agate.core.reference.ComparisonResult;
import at.co.svc.agate.core.reference.DifferenceType;
import at.co.svc.agate.core.reference.ReferenceCompareConfig;
import at.co.svc.agate.core.reference.ResponseComparator;
import at.co.svc.agate.core.reference.ResponseFormat;
import at.co.svc.agate.core.reference.UnorderedRule;

/**
 * Semantic JSON comparator used by REST ASSERT MATCH_REFERENCE.
 *
 * Supported behaviour:
 * - JSON object property order is ignored automatically.
 * - Scalar values and node types are compared strictly.
 * - Dynamic fields can be excluded through config.ignore using simple JSONPath
 *   expressions such as $.timestamp, $.meta.requestId or $.items[*].timestamp.
 * - Arrays are order-sensitive by default.
 * - Arrays configured through config.unordered can be compared independently of
 *   order and optionally matched through a stable matchBy key, e.g.
 *   path: $.readers / matchBy: $.id.
 */
public class JsonResponseComparator implements ResponseComparator {

    private static final int MAX_DIFFERENCES = 100;

    private final ObjectMapper mapper;

    public JsonResponseComparator() {
        this(new ObjectMapper());
    }

    public JsonResponseComparator(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public ResponseFormat getFormat() {
        return ResponseFormat.JSON;
    }

    @Override
    public String prepareReference(String content) throws Exception {
        JsonNode root = parse(content);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    @Override
    public ComparisonResult compare(
            String reference,
            String actual,
            ReferenceCompareConfig config) throws Exception {

        JsonNode expectedRoot = parse(reference).deepCopy();
        JsonNode actualRoot = parse(actual).deepCopy();

        ReferenceCompareConfig effectiveConfig =
                config != null ? config : new ReferenceCompareConfig();

        applyIgnoreRules(expectedRoot, effectiveConfig.getIgnore());
        applyIgnoreRules(actualRoot, effectiveConfig.getIgnore());

        List<ComparisonDifference> differences = new ArrayList<>();

        compareNode(
                expectedRoot,
                actualRoot,
                "$",
                effectiveConfig,
                differences);

        return new ComparisonResult(differences);
    }

    private JsonNode parse(String content) throws Exception {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("JSON content must not be null or blank.");
        }
        return mapper.readTree(content);
    }

    private void compareNode(
            JsonNode expected,
            JsonNode actual,
            String path,
            ReferenceCompareConfig config,
            List<ComparisonDifference> differences) {

        if (differences.size() >= MAX_DIFFERENCES) {
            return;
        }

        if (expected == null && actual == null) {
            return;
        }

        if (expected == null) {
            addDifference(
                    differences,
                    DifferenceType.UNEXPECTED_ELEMENT,
                    path,
                    null,
                    display(actual));
            return;
        }

        if (actual == null) {
            addDifference(
                    differences,
                    DifferenceType.ELEMENT_MISSING,
                    path,
                    display(expected),
                    null);
            return;
        }

        if (expected.getNodeType() != actual.getNodeType()) {
            addDifference(
                    differences,
                    DifferenceType.NODE_TYPE_CHANGED,
                    path,
                    expected.getNodeType() + " " + display(expected),
                    actual.getNodeType() + " " + display(actual));
            return;
        }

        if (expected.isObject()) {
            compareObjects(
                    (ObjectNode) expected,
                    (ObjectNode) actual,
                    path,
                    config,
                    differences);
            return;
        }

        if (expected.isArray()) {
            compareArrays(
                    (ArrayNode) expected,
                    (ArrayNode) actual,
                    path,
                    config,
                    differences);
            return;
        }

        if (!expected.equals(actual)) {
            addDifference(
                    differences,
                    DifferenceType.VALUE_CHANGED,
                    path,
                    display(expected),
                    display(actual));
        }
    }

    private void compareObjects(
            ObjectNode expected,
            ObjectNode actual,
            String path,
            ReferenceCompareConfig config,
            List<ComparisonDifference> differences) {

        expected.fieldNames().forEachRemaining(field -> {
            if (differences.size() >= MAX_DIFFERENCES) {
                return;
            }

            String childPath = appendField(path, field);

            if (!actual.has(field)) {
                addDifference(
                        differences,
                        DifferenceType.ELEMENT_MISSING,
                        childPath,
                        display(expected.get(field)),
                        null);
                return;
            }

            compareNode(
                    expected.get(field),
                    actual.get(field),
                    childPath,
                    config,
                    differences);
        });

        actual.fieldNames().forEachRemaining(field -> {
            if (differences.size() >= MAX_DIFFERENCES) {
                return;
            }

            if (!expected.has(field)) {
                addDifference(
                        differences,
                        DifferenceType.UNEXPECTED_ELEMENT,
                        appendField(path, field),
                        null,
                        display(actual.get(field)));
            }
        });
    }

    private void compareArrays(
            ArrayNode expected,
            ArrayNode actual,
            String path,
            ReferenceCompareConfig config,
            List<ComparisonDifference> differences) {

        UnorderedRule unorderedRule = findUnorderedRule(config, path);

        if (unorderedRule != null) {
            compareUnorderedArray(
                    expected,
                    actual,
                    path,
                    unorderedRule,
                    config,
                    differences);
            return;
        }

        int commonSize = Math.min(expected.size(), actual.size());

        for (int i = 0; i < commonSize && differences.size() < MAX_DIFFERENCES; i++) {
            compareNode(
                    expected.get(i),
                    actual.get(i),
                    path + "[" + i + "]",
                    config,
                    differences);
        }

        for (int i = commonSize; i < expected.size() && differences.size() < MAX_DIFFERENCES; i++) {
            addDifference(
                    differences,
                    DifferenceType.ELEMENT_MISSING,
                    path + "[" + i + "]",
                    display(expected.get(i)),
                    null);
        }

        for (int i = commonSize; i < actual.size() && differences.size() < MAX_DIFFERENCES; i++) {
            addDifference(
                    differences,
                    DifferenceType.UNEXPECTED_ELEMENT,
                    path + "[" + i + "]",
                    null,
                    display(actual.get(i)));
        }
    }

    private void compareUnorderedArray(
            ArrayNode expected,
            ArrayNode actual,
            String path,
            UnorderedRule rule,
            ReferenceCompareConfig config,
            List<ComparisonDifference> differences) {

        String matchBy = rule.getMatchBy();

        if (matchBy != null && !matchBy.isBlank()) {
            compareUnorderedArrayByKey(
                    expected,
                    actual,
                    path,
                    matchBy,
                    config,
                    differences);
            return;
        }

        List<JsonNode> expectedItems = new ArrayList<>();
        expected.forEach(expectedItems::add);

        List<JsonNode> actualItems = new ArrayList<>();
        actual.forEach(actualItems::add);

        Comparator<JsonNode> comparator = Comparator.comparing(this::canonical);
        expectedItems.sort(comparator);
        actualItems.sort(comparator);

        int commonSize = Math.min(expectedItems.size(), actualItems.size());

        for (int i = 0; i < commonSize && differences.size() < MAX_DIFFERENCES; i++) {
            compareNode(
                    expectedItems.get(i),
                    actualItems.get(i),
                    path + "[*]",
                    config,
                    differences);
        }

        for (int i = commonSize; i < expectedItems.size() && differences.size() < MAX_DIFFERENCES; i++) {
            addDifference(
                    differences,
                    DifferenceType.ELEMENT_MISSING,
                    path + "[*]",
                    display(expectedItems.get(i)),
                    null);
        }

        for (int i = commonSize; i < actualItems.size() && differences.size() < MAX_DIFFERENCES; i++) {
            addDifference(
                    differences,
                    DifferenceType.UNEXPECTED_ELEMENT,
                    path + "[*]",
                    null,
                    display(actualItems.get(i)));
        }
    }

    private void compareUnorderedArrayByKey(
            ArrayNode expected,
            ArrayNode actual,
            String path,
            String matchBy,
            ReferenceCompareConfig config,
            List<ComparisonDifference> differences) {

        Map<String, JsonNode> expectedByKey = indexByKey(expected, matchBy);
        Map<String, JsonNode> actualByKey = indexByKey(actual, matchBy);

        for (Map.Entry<String, JsonNode> entry : expectedByKey.entrySet()) {
            if (differences.size() >= MAX_DIFFERENCES) {
                return;
            }

            String key = entry.getKey();
            String itemPath = path + "[" + matchBy + "=" + key + "]";

            JsonNode actualItem = actualByKey.get(key);

            if (actualItem == null) {
                addDifference(
                        differences,
                        DifferenceType.ELEMENT_MISSING,
                        itemPath,
                        display(entry.getValue()),
                        null);
                continue;
            }

            compareNode(
                    entry.getValue(),
                    actualItem,
                    itemPath,
                    config,
                    differences);
        }

        for (Map.Entry<String, JsonNode> entry : actualByKey.entrySet()) {
            if (differences.size() >= MAX_DIFFERENCES) {
                return;
            }

            if (!expectedByKey.containsKey(entry.getKey())) {
                addDifference(
                        differences,
                        DifferenceType.UNEXPECTED_ELEMENT,
                        path + "[" + matchBy + "=" + entry.getKey() + "]",
                        null,
                        display(entry.getValue()));
            }
        }
    }

    private Map<String, JsonNode> indexByKey(ArrayNode array, String matchBy) {
        Map<String, JsonNode> result = new LinkedHashMap<>();

        for (JsonNode item : array) {
            JsonNode keyNode = resolveRelativeJsonPath(item, matchBy);

            if (keyNode == null || keyNode.isMissingNode() || keyNode.isNull()) {
                throw new IllegalArgumentException(
                        "MATCH_REFERENCE unordered rule cannot resolve matchBy '"
                                + matchBy
                                + "' for JSON element: "
                                + display(item));
            }

            String key = keyNode.isValueNode()
                    ? keyNode.asText()
                    : canonical(keyNode);

            if (result.containsKey(key)) {
                throw new IllegalArgumentException(
                        "MATCH_REFERENCE unordered rule produced duplicate matchBy key '"
                                + key
                                + "' for path '"
                                + matchBy
                                + "'.");
            }

            result.put(key, item);
        }

        return result;
    }

    private UnorderedRule findUnorderedRule(
            ReferenceCompareConfig config,
            String currentPath) {

        if (config == null || config.getUnordered() == null) {
            return null;
        }

        for (UnorderedRule rule : config.getUnordered()) {
            if (rule == null || rule.getPath() == null) {
                continue;
            }

            if (normalizePath(rule.getPath()).equals(normalizePath(currentPath))) {
                return rule;
            }
        }

        return null;
    }

    private void applyIgnoreRules(JsonNode root, List<String> ignoreRules) {
        if (root == null || ignoreRules == null || ignoreRules.isEmpty()) {
            return;
        }

        for (String expression : ignoreRules) {
            if (expression == null || expression.isBlank()) {
                continue;
            }

            List<PathToken> tokens = parsePath(expression);

            if (tokens.isEmpty()) {
                continue;
            }

            removeMatches(root, tokens, 0);
        }
    }

    private void removeMatches(JsonNode current, List<PathToken> tokens, int tokenIndex) {
        if (current == null || tokenIndex >= tokens.size()) {
            return;
        }

        PathToken token = tokens.get(tokenIndex);
        boolean last = tokenIndex == tokens.size() - 1;

        if (token.type == PathTokenType.FIELD) {
            if (!current.isObject()) {
                return;
            }

            ObjectNode object = (ObjectNode) current;

            if (last) {
                object.remove(token.value);
                return;
            }

            JsonNode child = object.get(token.value);
            if (child != null) {
                removeMatches(child, tokens, tokenIndex + 1);
            }
            return;
        }

        if (token.type == PathTokenType.ARRAY_INDEX) {
            if (!current.isArray()) {
                return;
            }

            ArrayNode array = (ArrayNode) current;
            int index = Integer.parseInt(token.value);

            if (index < 0 || index >= array.size()) {
                return;
            }

            if (last) {
                array.remove(index);
                return;
            }

            removeMatches(array.get(index), tokens, tokenIndex + 1);
            return;
        }

        if (token.type == PathTokenType.ARRAY_WILDCARD) {
            if (!current.isArray()) {
                return;
            }

            ArrayNode array = (ArrayNode) current;

            if (last) {
                array.removeAll();
                return;
            }

            for (JsonNode child : array) {
                removeMatches(child, tokens, tokenIndex + 1);
            }
        }
    }

    private JsonNode resolveRelativeJsonPath(JsonNode root, String expression) {
        if (root == null || expression == null || expression.isBlank()) {
            return root;
        }

        List<PathToken> tokens = parsePath(expression);
        JsonNode current = root;

        for (PathToken token : tokens) {
            if (current == null) {
                return null;
            }

            switch (token.type) {
            case FIELD:
                current = current.get(token.value);
                break;
            case ARRAY_INDEX:
                if (!current.isArray()) {
                    return null;
                }
                int index = Integer.parseInt(token.value);
                current = index >= 0 && index < current.size()
                        ? current.get(index)
                        : null;
                break;
            case ARRAY_WILDCARD:
                throw new IllegalArgumentException(
                        "Wildcard [*] is not supported in unordered.matchBy: " + expression);
            default:
                return null;
            }
        }

        return current;
    }

    private List<PathToken> parsePath(String expression) {
        String path = normalizePath(expression);
        List<PathToken> tokens = new ArrayList<>();

        if ("$".equals(path)) {
            return tokens;
        }

        int i = 1;

        while (i < path.length()) {
            char c = path.charAt(i);

            if (c == '.') {
                i++;
                int start = i;
                while (i < path.length()
                        && path.charAt(i) != '.'
                        && path.charAt(i) != '[') {
                    i++;
                }

                if (start < i) {
                    tokens.add(new PathToken(
                            PathTokenType.FIELD,
                            path.substring(start, i)));
                }
                continue;
            }

            if (c == '[') {
                int close = path.indexOf(']', i);
                if (close < 0) {
                    throw new IllegalArgumentException(
                            "Invalid JSONPath expression: " + expression);
                }

                String content = path.substring(i + 1, close).trim();

                if ("*".equals(content)) {
                    tokens.add(new PathToken(
                            PathTokenType.ARRAY_WILDCARD,
                            "*"));
                } else {
                    tokens.add(new PathToken(
                            PathTokenType.ARRAY_INDEX,
                            content));
                }

                i = close + 1;
                continue;
            }

            int start = i;
            while (i < path.length()
                    && path.charAt(i) != '.'
                    && path.charAt(i) != '[') {
                i++;
            }

            if (start < i) {
                tokens.add(new PathToken(
                        PathTokenType.FIELD,
                        path.substring(start, i)));
            }
        }

        return tokens;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "$";
        }

        String value = path.trim();

        if (value.startsWith("$.")) {
            return value;
        }

        if (value.startsWith("$[")) {
            return value;
        }

        if ("$".equals(value)) {
            return value;
        }

        if (value.startsWith(".")) {
            return "$" + value;
        }

        return "$." + value;
    }

    private String appendField(String parent, String field) {
        return "$".equals(parent)
                ? "$." + field
                : parent + "." + field;
    }

    private String canonical(JsonNode node) {
        try {
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            return String.valueOf(node);
        }
    }

    private String display(JsonNode node) {
        if (node == null) {
            return null;
        }

        if (node.isTextual()) {
            return node.asText();
        }

        return canonical(node);
    }

    private void addDifference(
            List<ComparisonDifference> differences,
            DifferenceType type,
            String path,
            String expected,
            String actual) {

        if (differences.size() >= MAX_DIFFERENCES) {
            return;
        }

        differences.add(new ComparisonDifference(
                type,
                path,
                expected,
                actual));
    }

    private enum PathTokenType {
        FIELD,
        ARRAY_INDEX,
        ARRAY_WILDCARD
    }

    private static final class PathToken {
        private final PathTokenType type;
        private final String value;

        private PathToken(PathTokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }
}
