package at.co.svc.agate.core.dsl.resolver;

/**
 * Extracts values from plain text responses.
 *
 * Used for CMD / shell output.
 */
public class TextResolver {

    public Object resolve(String text, String path) {

        if("TEXT".equals(path)) {
            return text;
        }

        if(path.startsWith("CONTAINS(")) {

            String value = path.substring(9, path.length()-1);

            return text.contains(value);
        }

        return text;
    }
}