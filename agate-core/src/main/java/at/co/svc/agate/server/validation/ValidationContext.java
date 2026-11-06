package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;

public record ValidationContext(
        Path file,
        JsonNode root,
        ValidationParser.SourceText source,
        ValidationOptions options
) {
}