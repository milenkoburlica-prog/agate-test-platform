package at.co.svc.agate.core.dsl.resolver;

import java.util.Map;

/**
 * Extracts values from JSON responses.
 *
 * Example paths:
 * BODY.user.id
 * BODY.data[0].id
 */
public class JsonPathResolver {

    public Object resolve(Map<?,?> json, String path) {

        if(path.startsWith("HEADER.")) {
            String key = path.substring(7);
            return json.get(key);
        }

        if(path.startsWith("BODY.")) {
            String key = path.substring(5);
            return json.get(key);
        }

        return json.get(path);
    }
}