package at.co.svc.tosca.util;

public final class FileNameSanitizer {

    private FileNameSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unnamed";
        }

        String result = value.toLowerCase().trim();

        result = result.split(" \\[")[0];

        result = result.replaceAll("[<>:\"/\\\\|?*]", "_");

        result = result
                .replace("(", "_")
                .replace(")", "_")
                .replace("#", "_")
                .replace("=", "_")
                .replace("{b", "")
                .replace("{xl", "")
                .replace("{", "")
                .replace("}", "")
                .replace("[", "")
                .replace("]", "");

        result = result.replaceAll("[\\s\\-_]+", "_");
        result = result.replaceAll("\\.{2,}", ".");
        result = result.replaceAll("[. ]+$", "");
        result = result.replaceAll("^_+|_+$", "");

        return result.isBlank() ? "unnamed" : result;
    }
}