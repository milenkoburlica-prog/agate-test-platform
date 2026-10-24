package at.co.svc.aga.transformator.utils;

/** Defensive YAML escaping for legacy text generators emitting condition: "...". */
public final class YamlConditionLineNormalizer {
    private YamlConditionLineNormalizer() { }
    public static String normalize(String line) {
        if (line == null) return null;
        int key = line.indexOf("condition:");
        if (key < 0 || !line.substring(0, key).isBlank()) return line;
        int start = key + "condition:".length();
        while (start < line.length() && Character.isWhitespace(line.charAt(start))) start++;
        if (start >= line.length() || line.charAt(start) != '"') return line;
        int end = line.length() - 1;
        while (end > start && Character.isWhitespace(line.charAt(end))) end--;
        if (end <= start || line.charAt(end) != '"') return line;
        StringBuilder out = new StringBuilder(line.length() + 8);
        out.append(line, 0, start + 1);
        for (int i = start + 1; i < end; i++) {
            char c = line.charAt(i);
            if (c == '"') {
                int slashes = 0;
                for (int j = i - 1; j > start && line.charAt(j) == '\\'; j--) slashes++;
                if (slashes % 2 == 0) out.append('\\');
            }
            out.append(c);
        }
        out.append(line.substring(end));
        return out.toString();
    }
    /** Normalize every condition line, independent of which legacy generator wrote it. */
    public static String normalizeDocument(String yaml) {
        if (yaml == null) return null;
        String[] lines = yaml.split("\\R", -1);
        StringBuilder result = new StringBuilder(yaml.length() + 32);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) result.append('\n');
            result.append(normalize(lines[i]));
        }
        return result.toString();
    }

}
