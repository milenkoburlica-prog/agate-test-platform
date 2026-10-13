package at.co.svc.aga.transformator.utils;

/** General exact-name policy for textual links, including trailing Tosca stars. */
final class TextLinkMigration {
    private TextLinkMigration() {}
    static String exactName(String text) {
        if (text == null || text.contains("{REGEX[")) return null;
        String name = text.trim();
        while (name.endsWith("*")) name = name.substring(0, name.length()-1).trim();
        if (name.isEmpty() || name.contains("*")) return null;
        return name;
    }
}
