package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public final class ValidationParser {

    private final ObjectMapper mapper;

    public ValidationParser() {

        YAMLFactory factory =
                YAMLFactory.builder()
                        .enable(
                                StreamReadFeature
                                        .STRICT_DUPLICATE_DETECTION)
                        .build();

        mapper =
                new ObjectMapper(factory);
    }

    public ParsedDocument parse(
            Path file) throws IOException {

        String text =
                Files.readString(file);

        try (JsonParser parser =
                     mapper.getFactory()
                             .createParser(text)) {

            JsonNode root =
                    mapper.readTree(parser);

            if (root == null) {

                throw new InvalidYamlException(
                        "Empty YAML document",
                        1,
                        1,
                        null
                );
            }

            return new ParsedDocument(
                    root,
                    new SourceText(text)
            );

        } catch (
                com.fasterxml.jackson.core.JsonProcessingException e) {

            JsonLocation location =
                    e.getLocation();

            throw new InvalidYamlException(
                    e.getOriginalMessage(),
                    location == null
                            ? 1
                            : (int) location.getLineNr(),
                    location == null
                            ? 1
                            : (int) location.getColumnNr(),
                    e
            );
        }
    }

    public record ParsedDocument(
            JsonNode root,
            SourceText source) {
    }

    public static final class SourceText {

        private final List<String> lines;

        public SourceText(
                String text) {

            /*
             * \\R is the Java regex for any line break:
             *
             * Windows: \\r\\n
             * Linux  : \\n
             * macOS  : \\r / \\n
             *
             * Important:
             *
             * Java string:
             *     "\\R"
             *
             * becomes regex:
             *     \R
             *
             * The previous "\\\\R" searched for the
             * literal characters "\\R" and therefore the
             * complete YAML was effectively treated as one line.
             */
            lines =
                    Arrays.asList(
                            text.split("\\R", -1)
                    );
        }

        public int lineOf(
                String token) {

            if (token == null
                    || token.isBlank()) {

                return 1;
            }

            for (int i = 0;
                 i < lines.size();
                 i++) {

                if (lines.get(i)
                        .contains(token)) {

                    return i + 1;
                }
            }

            /*
             * Fallback if the token cannot be located.
             */
            return 1;
        }
    }

    public static final class InvalidYamlException
            extends IOException {

        private final int line;
        private final int column;

        public InvalidYamlException(
                String message,
                int line,
                int column,
                Throwable cause) {

            super(
                    message,
                    cause);

            this.line =
                    line;

            this.column =
                    column;
        }

        public int line() {
            return line;
        }

        public int column() {
            return column;
        }
    }
}