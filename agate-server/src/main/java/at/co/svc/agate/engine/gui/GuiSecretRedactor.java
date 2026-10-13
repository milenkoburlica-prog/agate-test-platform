package at.co.svc.agate.engine.gui;

import java.util.Collection;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/** Short secrets are masked as whole tokens, never inside IDs or numeric metadata. */
final class GuiSecretRedactor {
    private GuiSecretRedactor() {}
    static String redact(String text, Collection<String> secrets) {
        if (text == null) return null;
        String safe = text;
        for (String secret : secrets.stream().filter(s -> s != null && !s.isEmpty())
                .sorted(Comparator.comparingInt(String::length).reversed()).toList()) {
            if (secret.length() < 4) {
                String expression = "(?<![\\p{L}\\p{N}_])" + Pattern.quote(secret)
                        + "(?![\\p{L}\\p{N}_])";
                safe = Pattern.compile(expression).matcher(safe).replaceAll(Matcher.quoteReplacement("***"));
            } else safe = safe.replace(secret, "***");
        }
        return safe;
    }
}
