package at.co.svc.agate.core.dsl.register;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * After XL substitution, an absent list index means the whole list argument
 * is NULL, not a lookup of the record named NULL.
 * This intentionally leaves non-null @... list references unchanged.
 */
final class NullListReferenceNormalizer {
    private static final Pattern NULL_LIST = Pattern.compile(
            "^@[A-Za-z0-9_-]+\\.[A-Za-z_][A-Za-z0-9_.-]*\\[\\s*(?:\\{NULL\\}|NULL)\\s*\\]$",
            Pattern.CASE_INSENSITIVE);

    private NullListReferenceNormalizer() { }

    @SuppressWarnings("unchecked")
    static void normalizeTree(Object node) {
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String text) {
                    ((Map.Entry<Object, Object>) entry).setValue(normalize(text));
                } else {
                    normalizeTree(value);
                }
            }
        } else if (node instanceof List<?> list) {
            List<Object> mutable = (List<Object>) list;
            for (int i = 0; i < mutable.size(); i++) {
                Object value = mutable.get(i);
                if (value instanceof String text) {
                    mutable.set(i, normalize(text));
                } else {
                    normalizeTree(value);
                }
            }
        }
    }

    static String normalize(String value) {
        return value != null && NULL_LIST.matcher(value.trim()).matches()
                ? "{NULL}" : value;
    }
}
