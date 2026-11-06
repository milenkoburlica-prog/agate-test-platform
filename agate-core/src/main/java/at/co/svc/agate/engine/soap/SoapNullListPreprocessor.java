package at.co.svc.agate.engine.soap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Removes AGATE:LIST blocks whose corresponding SOAP parameter is NULL before
 * buffer placeholders within the blocks are resolved.
 *
 * Nested markers are paired using a stack; the entire outer block disappears
 * when its parameter is NULL, including nested markers and unresolved buffers.
 * Non-NULL blocks are left byte-for-byte unchanged for existing handling.
 */
public final class SoapNullListPreprocessor {
    private static final Pattern MARKER = Pattern.compile(
            "<!--\\s*AGATE:(LIST(?:\\s+[^<>]*?)?|ENDLIST)\\s*-->", Pattern.CASE_INSENSITIVE);

    private SoapNullListPreprocessor() { }

    public static String removeNullLists(String xml, Map<String, ?> parameters) {
        if (xml == null || xml.isEmpty() || parameters == null || parameters.isEmpty()) {
            return xml;
        }

        Matcher matcher = MARKER.matcher(xml);
        Deque<Start> stack = new ArrayDeque<>();
        List<Span> removals = new ArrayList<>();
        while (matcher.find()) {
            String token = matcher.group(1).trim();
            if (token.equalsIgnoreCase("ENDLIST")) {
                if (stack.isEmpty()) {
                    throw new IllegalArgumentException("Unexpected AGATE:ENDLIST in SOAP request.xml");
                }
                Start start = stack.pop();
                if (isNull(parameters, start.name)) {
                    // Only outermost NULL blocks need a span. Nested removals are harmless,
                    // but merge them before deleting to prevent overlapping offsets.
                    removals.add(new Span(start.position, matcher.end()));
                }
            } else {
                String name = token.substring(4).trim();
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("AGATE:LIST requires a parameter name");
                }
                stack.push(new Start(name, matcher.start()));
            }
        }
        if (!stack.isEmpty()) {
            throw new IllegalArgumentException("Unclosed AGATE:LIST " + stack.peek().name + " in SOAP request.xml");
        }
        if (removals.isEmpty()) {
            return xml;
        }
        removals.sort((a, b) -> Integer.compare(a.from, b.from));
        List<Span> merged = new ArrayList<>();
        for (Span span : removals) {
            if (merged.isEmpty() || span.from > merged.get(merged.size() - 1).to) {
                merged.add(span);
            } else {
                Span prev = merged.get(merged.size() - 1);
                merged.set(merged.size() - 1, new Span(prev.from, Math.max(prev.to, span.to)));
            }
        }
        StringBuilder result = new StringBuilder(xml);
        for (int i = merged.size() - 1; i >= 0; i--) {
            Span span = merged.get(i);
            result.delete(span.from, span.to);
        }
        return result.toString();
    }

    private static boolean isNull(Map<String, ?> parameters, String name) {
        if (!parameters.containsKey(name)) return false;
        Object value = parameters.get(name);
        if (value == null) return true;
        String text = value.toString().trim();
        return "{NULL}".equalsIgnoreCase(text) || "NULL".equalsIgnoreCase(text);
    }

    private static final class Start {
        final String name;
        final int position;
        Start(String name, int position) { this.name = name; this.position = position; }
    }
    private static final class Span {
        final int from;
        final int to;
        Span(int from, int to) { this.from = from; this.to = to; }
    }
}
