package at.co.svc.tosca.tsu.cleaner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class TestCaseYamlSanitizer {

    private TestCaseYamlSanitizer() {
    }

    /**
     * Sanitizes:
     *
     * data/<app>/<testsuite.yaml>
     * data/<app>/reusable/*.yaml
     *
     * Currently implemented rule:
     *
     * Invalid YAML double quoted scalar containing backslashes:
     *
     *     value: "{B[PATH]}\plink_utf8.bat"
     *
     * becomes:
     *
     *     value: '{B[PATH]}\plink_utf8.bat'
     *
     * This avoids YAML interpreting '\' as an escape character.
     */
    public static void sanitize(
            Path testSuitePath,
            Path reusableDirectory) throws IOException {

        System.out.println();
        System.out.println(">>> TESTCASE YAML CLEAN");
        System.out.println("    Test suite : " + testSuitePath);
        System.out.println("    Reusable   : " + reusableDirectory);
        System.out.println();

        if (!Files.exists(testSuitePath)) {
            throw new IOException(
                    "Test suite does not exist: " + testSuitePath
            );
        }

        sanitizeFile(testSuitePath);

        if (!Files.exists(reusableDirectory)) {
            System.out.println(
                    "[WARNING] Reusable directory does not exist: "
                            + reusableDirectory
            );
            return;
        }

        List<Path> reusableFiles = new ArrayList<>();

        try (Stream<Path> stream = Files.list(reusableDirectory)) {

            stream
                    .filter(Files::isRegularFile)
                    .filter(TestCaseYamlSanitizer::isYamlFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(reusableFiles::add);
        }

        for (Path reusableFile : reusableFiles) {
            sanitizeFile(reusableFile);
        }

        System.out.println();
        System.out.println(
                "[SUCCESS] Testcase YAML clean completed."
        );
        System.out.println(
                "          Reusable files processed: "
                        + reusableFiles.size()
        );
    }

    private static void sanitizeFile(Path file) throws IOException {

        System.out.println("  Processing: " + file);

        List<String> originalLines =
                Files.readAllLines(file, StandardCharsets.UTF_8);

        List<String> sanitizedLines =
                new ArrayList<>(originalLines.size());

        int changes = 0;

        for (String line : originalLines) {

            String sanitized =
                    sanitizeLine(line);

            if (!line.equals(sanitized)) {

                changes++;

                System.out.println("    CHANGED:");
                System.out.println("      FROM: " + line);
                System.out.println("      TO  : " + sanitized);
            }

            sanitizedLines.add(sanitized);
        }

        Files.write(
                file,
                sanitizedLines,
                StandardCharsets.UTF_8
        );

        System.out.println(
                "    -> " + changes + " change(s)"
        );
    }
    
    
    private static String sanitizeLine(String line) {

        if (line == null || line.isEmpty()) {
            return line;
        }

        String trimmed = line.trim();

        /*
         * Ignore comments.
         */
        if (trimmed.startsWith("#")) {
            return line;
        }

        int colonIndex =
                findYamlKeySeparator(line);

        if (colonIndex < 0) {
            return line;
        }

        int firstQuote =
                findFirstNonWhitespace(line, colonIndex + 1);

        /*
         * We are currently only interested in double quoted YAML values.
         */
        if (firstQuote < 0
                || line.charAt(firstQuote) != '"') {

            return line;
        }

        int lastQuote =
                findClosingQuote(line);

        if (lastQuote <= firstQuote) {
            return line;
        }

        String value =
                line.substring(
                        firstQuote + 1,
                        lastQuote
                );

        /*
         * No backslash -> nothing to sanitize.
         */
        if (!value.contains("\\")) {
            return line;
        }

        /*
         * Only convert if the double quoted YAML value contains
         * an invalid YAML escape sequence.
         */
        if (!containsInvalidYamlEscape(value)) {
            return line;
        }

        /*
         * YAML single quoted strings do not interpret backslashes
         * as escape characters.
         *
         * If the value itself contains a single quote, YAML represents
         * it by doubling the quote.
         */
        String singleQuotedValue =
                value.replace("'", "''");

        return line.substring(0, firstQuote)
                + "'"
                + singleQuotedValue
                + "'"
                + line.substring(lastQuote + 1);
    }

    private static boolean containsInvalidYamlEscape(String value) {

        for (int i = 0; i < value.length(); i++) {

            if (value.charAt(i) != '\\') {
                continue;
            }

            /*
             * Backslash at the end of a double quoted YAML scalar
             * is invalid.
             */
            if (i + 1 >= value.length()) {
                return true;
            }

            char next =
                    value.charAt(i + 1);

            switch (next) {

                /*
                 * Valid YAML escape characters.
                 */
                case '0':
                case 'a':
                case 'b':
                case 't':
                case 'n':
                case 'v':
                case 'f':
                case 'r':
                case 'e':
                case ' ':
                case '"':
                case '/':
                case '\\':
                case 'N':
                case '_':
                case 'L':
                case 'P':
                    i++;
                    break;

                case 'x':
                    if (!hasHexDigits(value, i + 2, 2)) {
                        return true;
                    }
                    i += 3;
                    break;

                case 'u':
                    if (!hasHexDigits(value, i + 2, 4)) {
                        return true;
                    }
                    i += 5;
                    break;

                case 'U':
                    if (!hasHexDigits(value, i + 2, 8)) {
                        return true;
                    }
                    i += 9;
                    break;

                default:
                    /*
                     * Examples:
                     *
                     * \{
                     * \T
                     * \p
                     *
                     * These are invalid inside YAML double quotes.
                     */
                    return true;
            }
        }

        return false;
    }

    private static boolean hasHexDigits(
            String value,
            int start,
            int count) {

        if (start + count > value.length()) {
            return false;
        }

        for (int i = start; i < start + count; i++) {

            char c =
                    value.charAt(i);

            boolean hex =
                    (c >= '0' && c <= '9')
                            || (c >= 'a' && c <= 'f')
                            || (c >= 'A' && c <= 'F');

            if (!hex) {
                return false;
            }
        }

        return true;
    }

    private static int findYamlKeySeparator(String line) {

        boolean insideSingleQuote = false;
        boolean insideDoubleQuote = false;

        for (int i = 0; i < line.length(); i++) {

            char c =
                    line.charAt(i);

            if (c == '\''
                    && !insideDoubleQuote) {

                insideSingleQuote =
                        !insideSingleQuote;

            } else if (c == '"'
                    && !insideSingleQuote) {

                insideDoubleQuote =
                        !insideDoubleQuote;

            } else if (c == ':'
                    && !insideSingleQuote
                    && !insideDoubleQuote) {

                return i;
            }
        }

        return -1;
    }

    private static int findFirstNonWhitespace(
            String line,
            int start) {

        for (int i = start; i < line.length(); i++) {

            if (!Character.isWhitespace(
                    line.charAt(i))) {

                return i;
            }
        }

        return -1;
    }

    private static int findClosingQuote(String line) {

        for (int i = line.length() - 1; i >= 0; i--) {

            if (line.charAt(i) == '"') {
                return i;
            }
        }

        return -1;
    }

    private static boolean isYamlFile(Path path) {

        String name =
                path.getFileName()
                        .toString()
                        .toLowerCase();

        return name.endsWith(".yaml")
                || name.endsWith(".yml");
    }
}