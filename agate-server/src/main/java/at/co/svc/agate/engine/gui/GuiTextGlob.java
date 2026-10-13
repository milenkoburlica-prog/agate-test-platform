package at.co.svc.agate.engine.gui;

import java.util.Objects;
import java.util.regex.Pattern;

/** Whitespace-normalized text glob; keeps Playwright assertion polling. */
final class GuiTextGlob {
    private static final String SPACE = "[\\s\\u0085\\u00a0\\u1680\\u2000-\\u200a\\u2028\\u2029\\u202f\\u205f\\u3000]";
    private GuiTextGlob() {}

    static Pattern pattern(String expected) {
        String normalized = Objects.requireNonNull(expected)
                .replaceAll(SPACE + "+", " ").trim();
        StringBuilder regex = new StringBuilder("^" + SPACE + "*");
        for (char c : normalized.toCharArray()) {
            if (c == '*') regex.append(".*");
            else if (c == ' ') regex.append(SPACE).append('+');
            else {
                if ("\\.^$|?+()[]{}".indexOf(c) >= 0) regex.append('\\');
                regex.append(c);
            }
        }
        return Pattern.compile(regex.append(SPACE).append("*$").toString(), Pattern.DOTALL);
    }
}
