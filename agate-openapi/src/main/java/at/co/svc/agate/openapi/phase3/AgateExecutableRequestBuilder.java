package at.co.svc.agate.openapi.phase3;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestValues;

import at.co.svc.agate.openapi.phase3.model.AgateExecutableRequest;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AgateExecutableRequestBuilder {


    public AgateExecutableRequest build(
            AgateOperationModel operation,
            AgateRequestValues values) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }


        AgateRequestValues effectiveValues =
                values != null
                        ? values
                        : new AgateRequestValues();


        AgateExecutableRequest result =
                new AgateExecutableRequest();


        result.setMethod(
                operation.getMethod()
        );


        result.setPathTemplate(
                operation.getPath()
        );


        result.setPathParameters(
                deepCopyMap(
                        effectiveValues.getPath()
                )
        );


        result.setQueryParameters(
                deepCopyMap(
                        effectiveValues.getQuery()
                )
        );


        result.setHeaders(
                deepCopyMap(
                        effectiveValues.getHeaders()
                )
        );


        result.setCookies(
                deepCopyMap(
                        effectiveValues.getCookies()
                )
        );


        result.setBodyMediaType(
                effectiveValues.getBodyMediaType()
        );


        result.setBody(
                deepCopyValue(
                        effectiveValues.getBody()
                )
        );


        result.setResolvedPath(
                resolvePath(
                        operation.getPath(),
                        effectiveValues.getPath()
                )
        );


        return result;
    }




    private String resolvePath(
            String path,
            Map<String, Object> pathValues) {

        if (path == null) {

            return null;
        }


        String result =
                path;


        if (pathValues == null ||
                pathValues.isEmpty()) {

            return result;
        }


        for (Map.Entry<String, Object> entry :
                pathValues.entrySet()) {

            String placeholder =
                    "{"
                            + entry.getKey()
                            + "}";


            String encodedValue =
                    encodePathSegment(
                            entry.getValue()
                    );


            result =
                    result.replace(
                            placeholder,
                            encodedValue
                    );
        }


        return result;
    }




    private String encodePathSegment(
            Object value) {

        if (value == null) {

            return "";
        }


        String raw =
                String.valueOf(
                        value
                );


        /*
         * URLEncoder is intended primarily for query
         * parameters and uses '+' for spaces.
         *
         * For a path segment we explicitly convert
         * '+' to %20.
         */

        return java.net.URLEncoder
                .encode(
                        raw,
                        StandardCharsets.UTF_8
                )
                .replace(
                        "+",
                        "%20"
                );
    }




    private Map<String, Object> deepCopyMap(
            Map<String, Object> source) {

        Map<String, Object> result =
                new LinkedHashMap<>();


        if (source == null) {

            return result;
        }


        source.forEach(
                (key, value) ->
                        result.put(
                                key,
                                deepCopyValue(
                                        value
                                )
                        )
        );


        return result;
    }




    private Object deepCopyValue(
            Object value) {

        if (value instanceof Map<?, ?> map) {

            Map<String, Object> result =
                    new LinkedHashMap<>();


            map.forEach(
                    (key, nestedValue) ->
                            result.put(
                                    String.valueOf(
                                            key
                                    ),
                                    deepCopyValue(
                                            nestedValue
                                    )
                            )
            );


            return result;
        }


        if (value instanceof List<?> list) {

            List<Object> result =
                    new ArrayList<>();


            for (Object item :
                    list) {

                result.add(
                        deepCopyValue(
                                item
                        )
                );
            }


            return result;
        }


        return value;
    }
}