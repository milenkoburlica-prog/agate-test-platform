package at.co.svc.agate.openapi.phase3.csv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AgateRequestFlattener {


    public Map<String, Object> flattenBody(
            Object body) {

        Map<String, Object> result =
                new LinkedHashMap<>();


        if (body == null) {

            return result;
        }


        flattenValue(
                "",
                body,
                result
        );


        return result;
    }




    private void flattenValue(
            String path,
            Object value,
            Map<String, Object> result) {

        if (value instanceof Map<?, ?> map) {

            flattenMap(
                    path,
                    map,
                    result
            );

            return;
        }


        if (value instanceof List<?> list) {

            flattenList(
                    path,
                    list,
                    result
            );

            return;
        }


        if (!path.isBlank()) {

            result.put(
                    path,
                    value
            );
        }
    }




    private void flattenMap(
            String path,
            Map<?, ?> map,
            Map<String, Object> result) {

        for (Map.Entry<?, ?> entry :
                map.entrySet()) {

            String name =
                    String.valueOf(
                            entry.getKey()
                    );


            String childPath =
                    path.isBlank()
                            ? name
                            : path
                            + "."
                            + name;


            flattenValue(
                    childPath,
                    entry.getValue(),
                    result
            );
        }
    }




    private void flattenList(
            String path,
            List<?> list,
            Map<String, Object> result) {

        if (path.isBlank()) {

            return;
        }


        if (list.isEmpty()) {

            result.put(
                    path,
                    new ArrayList<>()
            );

            return;
        }


        /*
         * Important AGATE convention:
         *
         * An array containing exactly one object is flattened
         * without an array index.
         *
         * Example:
         *
         * tags:
         *   - id: 1
         *     name: example
         *
         * becomes:
         *
         * tags.id
         * tags.name
         *
         * This matches the existing AGATE CSV convention.
         */
        if (list.size() == 1 &&
                list.get(0)
                        instanceof Map<?, ?> map) {

            flattenMap(
                    path,
                    map,
                    result
            );

            return;
        }


        /*
         * Primitive arrays and more complex arrays remain
         * one CSV value.
         */
        result.put(
                path,
                new ArrayList<>(
                        list
                )
        );
    }
}