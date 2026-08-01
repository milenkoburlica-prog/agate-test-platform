package at.co.svc.agate.core.dsl.resolver;

import java.util.List;
import java.util.Map;

/**
 * UniversalPathResolver extracts values from different response types
 * using a unified DSL path syntax.
 *
 * Supported response types:
 * - REST JSON responses
 * - SQL result tables
 * - plain text responses (CMD, shell)
 *
 * Example DSL paths:
 *
 * HEADER.sv-txid
 * BODY.user.id
 * BODY.data[0].id
 * [0].SV_PERSON
 * TEXT
 */
public class UniversalPathResolver {

    private final JsonPathResolver jsonResolver = new JsonPathResolver();
    private final SqlResultResolver sqlResolver = new SqlResultResolver();
    private final TextResolver textResolver = new TextResolver();

    /**
     * Extracts a value from a response using a DSL path.
     */
    public Object resolve(Object response, String path) {

        if (response == null) {
            throw new RuntimeException("Response is null");
        }

        if (response instanceof Map) {
            return jsonResolver.resolve((Map<?,?>) response, path);
        }

        if (response instanceof List) {
            return sqlResolver.resolve((List<?>) response, path);
        }

        if (response instanceof String) {
            return textResolver.resolve((String) response, path);
        }

        throw new RuntimeException("Unsupported response type: " + response.getClass());
    }
}