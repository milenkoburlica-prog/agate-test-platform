package at.co.svc.agate.engine.soap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Expands the generated AGATE:LIST / ITEM markers using vertical TestSOAP reference CSVs. */
public final class SoapListReferenceExpander {
    private static final Pattern REF = Pattern.compile("^@([A-Za-z0-9_-]+)\\.([A-Za-z_][A-Za-z0-9_.-]*)\\[([^]\\r\\n]*)]$");
    private static final Pattern TAG = Pattern.compile("<!--\\s*AGATE:(LIST\\s+[^<>]*?|ENDLIST|ITEM|ENDITEM)\\s*-->", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALUE = Pattern.compile("\\{(?:B|R)\\[([^]]+)]}");

    private SoapListReferenceExpander() { }

    public static String expand(String xml, Map<String, ?> parameters, Path references) throws IOException {
        if (xml == null || parameters == null || parameters.isEmpty() || !xml.contains("AGATE:LIST")) return xml;
        return expandRegion(xml, parameters, references, null);
    }

    private static String expandRegion(String xml, Map<String, ?> parameters, Path refs, Map<String, String> row) throws IOException {
        Matcher m = TAG.matcher(xml);
        StringBuilder out = new StringBuilder();
        int cursor = 0;
        while (m.find(cursor)) {
            String marker = m.group(1).trim();
            if (!marker.toUpperCase(Locale.ROOT).startsWith("LIST ")) {
                cursor = m.end();
                continue;
            }
            String name = marker.substring(5).trim();
            int depth = 1, endStart = -1, endEnd = -1;
            Matcher scan = TAG.matcher(xml);
            scan.region(m.end(), xml.length());
            while (scan.find()) {
                String token = scan.group(1).trim().toUpperCase(Locale.ROOT);
                if (token.startsWith("LIST ")) depth++;
                else if (token.equals("ENDLIST") && --depth == 0) {
                    endStart = scan.start(); endEnd = scan.end(); break;
                }
            }
            if (endEnd < 0) throw new IllegalArgumentException("Unclosed AGATE:LIST " + name);
            String contents = xml.substring(m.end(), endStart);
            String replacement;
            Object raw = parameters.get(name);
            String spec = raw == null ? null : raw.toString().trim();
            if (spec == null && row != null) spec = field(row, name);
            if ((raw != null || row != null) && isNull(spec)) {
                replacement = "";
            } else if (spec != null && REF.matcher(spec).matches()) {
                Matcher r = REF.matcher(spec); r.matches();
                String filename = r.group(1), listName = r.group(2), ids = r.group(3);
                if (!listName.equals(name)) throw new IllegalArgumentException("List name mismatch: AGATE:LIST " + name + " versus " + spec);
                Path normalizedRoot = refs.toAbsolutePath().normalize();
                Path csv = normalizedRoot.resolve(filename + ".csv").normalize();
                if (!csv.startsWith(normalizedRoot)) throw new IOException("Invalid SOAP reference path: " + csv);
                Map<String, Map<String, String>> records = parseVertical(csv);
                String itemTemplate = extractItem(contents, name);
                List<Map<String, String>> selected = new ArrayList<>();
                for (String id : ids.split(";", -1)) {
                    id = id.trim();
                    if (id.isEmpty()) throw new IOException("Empty ID in reference " + spec);
                    Map<String, String> record = records.get(id);
                    if (record == null) throw new IOException("Reference record " + filename + "[" + id + "] not found: " + csv);
                    selected.add(record);
                }
                // A single risikogruppeCode identifies one parent element. Each selected
                // record contributes a child risikofaktorCode to that parent. Group only
                // when the XML template explicitly declares this nested list; do not
                // change unrelated AGATE:LIST structures.
                if ("anamneseRisikogruppen".equals(name)
                        && itemTemplate.matches("(?s).*AGATE:LIST\\s+risikofaktorCode\\s*-->.*")
                        && !"NONE".equalsIgnoreCase(asString(parameters.get(name + ".grouping")))) {
                    selected = groupRisikofaktoren(selected);
                }
                StringBuilder items = new StringBuilder();
                for (Map<String, String> record : selected) {
                    String nested = expandRegion(itemTemplate, parameters, refs, record);
                    items.append(replaceFields(nested, record));
                }
                replacement = items.toString();
            } else if (row != null && spec != null) {
                // Inner list is commonly stored as a scalar or semicolon-separated xls* value.
                String itemTemplate = extractItem(contents, name);
                StringBuilder items = new StringBuilder();
                for (String value : spec.split(";", -1)) {
                    Map<String, String> single = new LinkedHashMap<>(row);
                    single.put(name, value.trim()); single.put("xls" + upperFirst(name), value.trim());
                    items.append(replaceFields(expandRegion(itemTemplate, parameters, refs, single), single));
                }
                replacement = items.toString();
            } else {
                // No list reference: leave block unchanged for legacy processing.
                replacement = xml.substring(m.start(), endEnd);
            }
            out.append(xml, cursor, m.start()).append(replacement);
            cursor = endEnd;
            m = TAG.matcher(xml);
        }
        return out.append(xml, cursor, xml.length()).toString();
    }

    private static String extractItem(String contents, String listName) {
        Matcher markers = TAG.matcher(contents);
        int depth = 0, begin = -1;
        while (markers.find()) {
            String token = markers.group(1).trim().toUpperCase(Locale.ROOT);
            if (token.equals("ITEM")) {
                if (depth++ == 0) begin = markers.end();
            } else if (token.equals("ENDITEM")) {
                if (depth == 0) throw new IllegalArgumentException("Unexpected AGATE:ENDITEM in " + listName);
                if (--depth == 0) return contents.substring(begin, markers.start());
            }
        }
        throw new IllegalArgumentException("Missing or unclosed AGATE:ITEM in " + listName);
    }

    /** Combine factors of the same risk group into one parent, retaining first-seen order. */
    private static List<Map<String, String>> groupRisikofaktoren(List<Map<String, String>> selected) {
        Map<String, Map<String, String>> groups = new LinkedHashMap<>();
        List<Map<String, String>> ordered = new ArrayList<>();
        for (Map<String, String> record : selected) {
            String groupCode = field(record, "risikogruppeCode");
            // Unknown/missing group keys must stay distinct, including negative tests.
            if (isNull(groupCode) || groupCode.isBlank()) {
                ordered.add(record);
                continue;
            }
            Map<String, String> existing = groups.get(groupCode);
            if (existing == null) {
                existing = new LinkedHashMap<>(record);
                groups.put(groupCode, existing);
                ordered.add(existing);
                continue;
            }
            String nextFactor = field(record, "risikofaktorCode");
            if (nextFactor == null || nextFactor.isBlank() || isNull(nextFactor)) continue;
            String prevFactor = field(existing, "risikofaktorCode");
            String combined = prevFactor == null || prevFactor.isBlank() || isNull(prevFactor)
                    ? nextFactor : prevFactor + ";" + nextFactor;
            // Update existing key spelling (e.g. xlsRisikofaktorCode).
            for (String key : new ArrayList<>(existing.keySet())) {
                if (key.equalsIgnoreCase("risikofaktorCode") || key.equalsIgnoreCase("xlsRisikofaktorCode")) {
                    existing.put(key, combined);
                }
            }
        }
        return ordered;
    }

    private static String asString(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private static String replaceFields(String xml, Map<String, String> fields) {
        Matcher matcher = VALUE.matcher(xml);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String v = field(fields, matcher.group(1));
            if (v == null) continue; // not a reference field: resolve via normal buffer pipeline
            String replacement = isNull(v) ? "{NULL}" : isEmpty(v) ? "" : escapeXml(v);
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private static String field(Map<String, String> row, String name) {
        if (row.containsKey(name)) return row.get(name);
        String xls = "xls" + upperFirst(name);
        if (row.containsKey(xls)) return row.get(xls);
        for (Map.Entry<String, String> e : row.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name) || e.getKey().equalsIgnoreCase(xls)) return e.getValue();
        }
        return null;
    }
    private static String upperFirst(String value) {
        return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
    private static boolean isNull(String text) { return text == null || text.equalsIgnoreCase("NULL") || text.equalsIgnoreCase("{NULL}"); }
    private static boolean isEmpty(String text) { return "EMPTY".equalsIgnoreCase(text) || "{EMPTY}".equalsIgnoreCase(text) || "STRING:0:EMPTY".equalsIgnoreCase(text); }
    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static Map<String, Map<String, String>> parseVertical(Path file) throws IOException {
        if (!Files.isRegularFile(file)) throw new IOException("SOAP list reference CSV missing: " + file);
        String data = Files.readString(file, StandardCharsets.UTF_8);
        List<List<String>> rows = new ArrayList<>();
        List<String> current = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < data.length(); i++) {
            char ch = data.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < data.length() && data.charAt(i + 1) == '"') { cell.append('"'); i++; }
                else quoted = !quoted;
            } else if (ch == ';' && !quoted) { current.add(cell.toString()); cell.setLength(0); }
            else if ((ch == '\n' || ch == '\r') && !quoted) {
                if (ch == '\r' && i + 1 < data.length() && data.charAt(i + 1) == '\n') i++;
                current.add(cell.toString()); cell.setLength(0); rows.add(current); current = new ArrayList<>();
            } else cell.append(ch);
        }
        if (quoted) throw new IOException("Unclosed CSV quote in " + file);
        if (!current.isEmpty() || cell.length() > 0) { current.add(cell.toString()); rows.add(current); }
        if (rows.isEmpty() || rows.get(0).isEmpty() || !rows.get(0).get(0).replace("\uFEFF", "").trim().equalsIgnoreCase("Variable")) {
            throw new IOException("Expected vertical CSV header Variable;ID1;ID2... in " + file);
        }
        Map<String, Map<String, String>> records = new LinkedHashMap<>();
        for (int c = 1; c < rows.get(0).size(); c++) {
            String id = rows.get(0).get(c).trim();
            if (id.isEmpty() || records.containsKey(id)) throw new IOException("Empty/duplicate reference ID '" + id + "' in " + file);
            records.put(id, new LinkedHashMap<>());
        }
        for (int r = 1; r < rows.size(); r++) {
            List<String> line = rows.get(r);
            if (line.isEmpty() || line.get(0).trim().isEmpty()) continue;
            for (int c = 1; c < rows.get(0).size(); c++) {
                records.get(rows.get(0).get(c).trim()).put(line.get(0).trim(), c < line.size() ? line.get(c) : "");
            }
        }
        return records;
    }
}
