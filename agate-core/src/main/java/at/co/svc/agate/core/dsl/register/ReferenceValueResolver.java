package at.co.svc.agate.core.dsl.register;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves scalar references in instantiated YAML values:
 * !ReferenceFile[recordId].fieldName
 *
 * Reference tables are vertical semicolon-separated CSV files under
 * template/references, with the first row "Variable;ID1;ID2;...".
 * This resolver intentionally does not touch the existing @... list syntax.
 */
final class ReferenceValueResolver {
    private static final Pattern REFERENCE = Pattern.compile(
            "!([A-Za-z0-9_-]+)\\[([^\\[\\]\\r\\n]+)\\]\\.([A-Za-z_][A-Za-z0-9_.-]*)");

    private final Path referenceDirectory;
    private final Map<String, ReferenceTable> cache = new HashMap<>();

    ReferenceValueResolver(Path applicationRoot) {
        this.referenceDirectory = applicationRoot.resolve("template").resolve("references").normalize();
    }

    @SuppressWarnings("unchecked")
    void resolveTree(Object node, String testCaseId) throws IOException {
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String text) {
                    ((Map.Entry<Object, Object>) entry).setValue(resolve(text, testCaseId));
                } else {
                    resolveTree(value, testCaseId);
                }
            }
        } else if (node instanceof List<?> list) {
            List<Object> mutable = (List<Object>) list;
            for (int i = 0; i < mutable.size(); i++) {
                Object value = mutable.get(i);
                if (value instanceof String text) {
                    mutable.set(i, resolve(text, testCaseId));
                } else {
                    resolveTree(value, testCaseId);
                }
            }
        }
    }

    String resolve(String text, String testCaseId) throws IOException {
        if (text == null || text.indexOf('!') < 0) return text;
        Matcher matcher = REFERENCE.matcher(text);
        StringBuffer output = new StringBuffer();
        boolean matched = false;
        while (matcher.find()) {
            matched = true;
            String name = matcher.group(1);
            String id = matcher.group(2);
            String field = matcher.group(3);
            if (id.isBlank() || id.contains("{XL[") || id.equalsIgnoreCase("{NULL}")
                    || id.equalsIgnoreCase("NULL")) {
                throw error(testCaseId, name, id, field,
                        "Reference ID is empty, NULL, or an unresolved XL placeholder");
            }
            String replacement = lookup(name, id, field, testCaseId);
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }
        if (!matched) {
            // Do not reinterpret unrelated '!' strings; reject malformed expressions
            // beginning with a recognizable scalar-reference prefix.
            if (text.matches("(?s).*![A-Za-z0-9_-]+\\[.*")) {
                throw new IllegalArgumentException("[REFERENCE][ERROR] testcase='" + testCaseId
                        + "' malformed scalar reference: " + text);
            }
            return text;
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private String lookup(String name, String id, String field, String testCaseId) throws IOException {
        ReferenceTable table = cache.get(name);
        if (table == null) {
            table = readTable(name, testCaseId);
            cache.put(name, table);
        }
        Integer column = table.ids.get(id);
        if (column == null) throw error(testCaseId, name, id, field, "Record ID not found");
        List<String> values = table.fields.get(field);
        if (values == null) throw error(testCaseId, name, id, field, "Field not found");
        return column < values.size() ? values.get(column) : "";
    }

    private ReferenceTable readTable(String name, String testCaseId) throws IOException {
        Path file = referenceDirectory.resolve(name + ".csv").normalize();
        if (!file.startsWith(referenceDirectory)) {
            throw error(testCaseId, name, "", "", "Invalid reference path");
        }
        if (!Files.isRegularFile(file)) {
            throw error(testCaseId, name, "", "", "Reference CSV not found: " + file);
        }
        List<List<String>> rows = parseCsv(Files.readString(file, StandardCharsets.UTF_8), file);
        if (rows.isEmpty() || rows.get(0).isEmpty()
                || !"Variable".equalsIgnoreCase(removeBom(rows.get(0).get(0)).trim())) {
            throw error(testCaseId, name, "", "", "Expected CSV header: Variable;ID1;ID2;...");
        }
        Map<String, Integer> ids = new LinkedHashMap<>();
        List<String> header = rows.get(0);
        for (int col = 1; col < header.size(); col++) {
            String id = header.get(col);
            if (id.isEmpty()) throw error(testCaseId, name, "", "", "Empty record ID in header column " + (col + 1));
            if (ids.putIfAbsent(id, col) != null)
                throw error(testCaseId, name, id, "", "Duplicate record ID");
        }
        Map<String, List<String>> fields = new LinkedHashMap<>();
        for (int row = 1; row < rows.size(); row++) {
            List<String> cells = rows.get(row);
            if (cells.isEmpty() || (cells.size() == 1 && cells.get(0).isBlank())) continue;
            String field = cells.get(0);
            if (field.isEmpty()) throw error(testCaseId, name, "", "", "Empty field name in row " + (row + 1));
            if (fields.putIfAbsent(field, cells) != null)
                throw error(testCaseId, name, "", field, "Duplicate field name");
        }
        return new ReferenceTable(ids, fields);
    }

    private static String removeBom(String value) {
        return value.startsWith("\uFEFF") ? value.substring(1) : value;
    }

    /** RFC-4180-style quoting with semicolon delimiter; preserves leading zeros. */
    private static List<List<String>> parseCsv(String input, Path path) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < input.length() && input.charAt(i + 1) == '"') {
                    cell.append('"'); i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ';' && !quoted) {
                row.add(cell.toString()); cell.setLength(0);
            } else if ((ch == '\n' || ch == '\r') && !quoted) {
                if (ch == '\r' && i + 1 < input.length() && input.charAt(i + 1) == '\n') i++;
                row.add(cell.toString()); cell.setLength(0);
                rows.add(row); row = new ArrayList<>();
            } else {
                cell.append(ch);
            }
        }
        if (quoted) throw new IOException("[REFERENCE][ERROR] Unclosed quoted CSV field: " + path);
        if (!row.isEmpty() || cell.length() != 0) {
            row.add(cell.toString()); rows.add(row);
        }
        return rows;
    }

    private static IllegalArgumentException error(String testCaseId, String name, String id,
                                                   String field, String reason) {
        return new IllegalArgumentException("[REFERENCE][ERROR] testcase='" + testCaseId
                + "' file='" + name + ".csv' id='" + id + "' field='" + field + "': " + reason);
    }

    private record ReferenceTable(Map<String, Integer> ids, Map<String, List<String>> fields) {}
}
