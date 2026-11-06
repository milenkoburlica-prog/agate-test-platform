package at.co.svc.agate.core.dsl.resolver;

import java.util.List;
import java.util.Map;

/**
 * Extracts values from SQL result sets.
 *
 * SQL results are expected as a list of rows:
 * List<Map<String,Object>>
 *
 * Example path:
 * [0].SV_PERSON
 */
public class SqlResultResolver {

    public Object resolve(List<?> rows, String path) {

        if(!path.startsWith("[")) {
            throw new RuntimeException("Invalid SQL path: " + path);
        }

        int indexEnd = path.indexOf("]");
        int index = Integer.parseInt(path.trim().substring(1, indexEnd));

        String column = path.substring(indexEnd + 2);

        Map<?,?> row = (Map<?,?>) rows.get(index);

        return row.get(column);
    }
}