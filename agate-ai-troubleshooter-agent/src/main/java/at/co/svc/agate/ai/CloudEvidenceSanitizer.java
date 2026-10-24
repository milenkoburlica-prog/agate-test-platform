package at.co.svc.agate.ai;

import java.util.regex.Pattern;

/** Safety boundary: only bounded, pseudonymized evidence may be sent to a cloud model. */
public final class CloudEvidenceSanitizer {
    private CloudEvidenceSanitizer() {}
    private static final int MAX_CLOUD_CHARS = 14000;
    private static final Pattern JWT = Pattern.compile("\\beyJ[A-Za-z0-9_-]{12,}\\.[A-Za-z0-9_-]{10,}(?:\\.[A-Za-z0-9_-]{10,})?\\b");
    private static final Pattern KEY_VALUE = Pattern.compile("(?i)(\\b(?:authorization|password|passwd|secret|api[_-]?key|pin|cardtoken|tokenwert|set-cookie)\\b\\s*[:=]\\s*)[^\\r\\n]+", Pattern.MULTILINE);
    private static final Pattern IP = Pattern.compile("(?<![\\d.])(?:\\d{1,3}\\.){3}\\d{1,3}(?![\\d.])");
    private static final Pattern SVNR = Pattern.compile("(?<!\\d)\\d{10}(?!\\d)");
    public static String sanitize(String raw) {
        if (raw == null) return "";
        String s = JWT.matcher(raw).replaceAll("[TOKEN_MASKIERT]");
        s = KEY_VALUE.matcher(s).replaceAll("$1[GEHEIMNIS_MASKIERT]");
        s = IP.matcher(s).replaceAll("[IP_MASKIERT]");
        s = SVNR.matcher(s).replaceAll("[SVNR_MASKIERT]");
        if (s.length() > MAX_CLOUD_CHARS) s=s.substring(0, MAX_CLOUD_CHARS)+"\n[WEITERE_EVIDENZ_GEKuERZT]";
        return s;
    }
}
