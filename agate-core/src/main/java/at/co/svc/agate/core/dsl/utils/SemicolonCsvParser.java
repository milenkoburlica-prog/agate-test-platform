package at.co.svc.agate.core.dsl.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads semicolon-delimited CSV records, including quoted line breaks, quoted
 * semicolons, escaped double quotes (""), and trailing empty cells.
 * All fields remain strings, preserving leading zeros and NULL markers.
 */
public final class SemicolonCsvParser {
    private SemicolonCsvParser() { }

    public static List<List<String>> read(Path file) throws IOException {
        String content;
        try {
            content = Files.readString(file, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            throw new IOException("[CSV][ENCODING] Invalid UTF-8 in file: "
                    + file.toAbsolutePath() + ". Save this CSV as UTF-8 (not ANSI/Windows-1252)"
                    + " and retry instantiation. The file was not modified.", e);
        }
        if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
            content = content.substring(1);
        }
        return parse(content, file.toString());
    }

    public static List<List<String>> parse(String content, String source) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;
        boolean recordStarted = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                        closedQuote = true;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                if (field.length() == 0 && !closedQuote) {
                    quoted = true;
                    recordStarted = true;
                } else {
                    throw new IOException("Invalid quote in CSV " + source + " at character " + (i + 1));
                }
            } else if (c == ';') {
                row.add(field.toString());
                field.setLength(0);
                closedQuote = false;
                recordStarted = true;
            } else if (c == '\r' || c == '\n') {
                if (c == '\r' && i + 1 < content.length() && content.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(field.toString());
                if (recordStarted || !row.stream().allMatch(String::isBlank)) {
                    rows.add(row);
                }
                row = new ArrayList<>();
                field.setLength(0);
                closedQuote = false;
                recordStarted = false;
            } else {
                if (closedQuote && !Character.isWhitespace(c)) {
                    throw new IOException("Text after closing quote in CSV " + source + " at character " + (i + 1));
                }
                if (!closedQuote) field.append(c);
                recordStarted = true;
            }
        }
        if (quoted) throw new IOException("Unterminated quoted CSV field in " + source);
        if (recordStarted || field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }
}
