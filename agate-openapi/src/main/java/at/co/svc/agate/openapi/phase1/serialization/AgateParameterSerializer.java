package at.co.svc.agate.openapi.phase1.serialization;

import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AgateParameterSerializer {


    public AgateSerializedParameter serialize(
            AgateRequestParameterModel parameter,
            Object value) {

        if (parameter == null) {

            throw new AgateParameterSerializationException(
                    "Parameter must not be null"
            );
        }

        if (value == null) {

            return new AgateSerializedParameter(
                    parameter.getName(),
                    List.of()
            );
        }

        String location =
                parameter.getLocation();

        String style =
                resolveStyle(
                        parameter
                );

        boolean explode =
                resolveExplode(
                        parameter,
                        style
                );

        if ("query".equals(location)) {

            return serializeQuery(
                    parameter,
                    value,
                    style,
                    explode
            );
        }

        if ("path".equals(location)) {

            return serializePath(
                    parameter,
                    value,
                    style,
                    explode
            );
        }

        if ("header".equals(location)) {

            return serializeHeader(
                    parameter,
                    value,
                    style,
                    explode
            );
        }

        if ("cookie".equals(location)) {

            return serializeCookie(
                    parameter,
                    value,
                    style,
                    explode
            );
        }

        throw new AgateParameterSerializationException(
                "Unsupported parameter location: "
                        + location
        );
    }


    private AgateSerializedParameter serializeQuery(
            AgateRequestParameterModel parameter,
            Object value,
            String style,
            boolean explode) {

        if (!"form".equals(style)) {

            throw new AgateParameterSerializationException(
                    "Unsupported query style: "
                            + style
            );
        }

        String name =
                parameter.getName();

        if (isPrimitive(value)) {

            return single(
                    name,
                    value
            );
        }

        if (isArray(value)) {

            List<String> values =
                    toValues(
                            value
                    );

            if (explode) {

                return new AgateSerializedParameter(
                        name,
                        values
                );
            }

            return new AgateSerializedParameter(
                    name,
                    List.of(
                            String.join(
                                    ",",
                                    values
                            )
                    )
            );
        }

        if (value instanceof Map<?, ?> map) {

            if (explode) {

                List<String> values =
                        new ArrayList<>();

                map.forEach(
                        (key, mapValue) ->
                                values.add(
                                        String.valueOf(key)
                                                + "="
                                                + String.valueOf(mapValue)
                                )
                );

                return new AgateSerializedParameter(
                        name,
                        values
                );
            }

            List<String> values =
                    new ArrayList<>();

            map.forEach(
                    (key, mapValue) -> {

                        values.add(
                                String.valueOf(key)
                        );

                        values.add(
                                String.valueOf(mapValue)
                        );
                    }
            );

            return new AgateSerializedParameter(
                    name,
                    List.of(
                            String.join(
                                    ",",
                                    values
                            )
                    )
            );
        }

        throw unsupportedValue(
                parameter,
                value
        );
    }


    private AgateSerializedParameter serializePath(
            AgateRequestParameterModel parameter,
            Object value,
            String style,
            boolean explode) {

        if (!"simple".equals(style)) {

            throw new AgateParameterSerializationException(
                    "Unsupported path style: "
                            + style
            );
        }

        String name =
                parameter.getName();

        if (isPrimitive(value)) {

            return single(
                    name,
                    value
            );
        }

        if (isArray(value)) {

            return new AgateSerializedParameter(
                    name,
                    List.of(
                            String.join(
                                    ",",
                                    toValues(
                                            value
                                    )
                            )
                    )
            );
        }

        if (value instanceof Map<?, ?> map) {

            List<String> values =
                    new ArrayList<>();

            if (explode) {

                map.forEach(
                        (key, mapValue) ->
                                values.add(
                                        String.valueOf(key)
                                                + "="
                                                + String.valueOf(mapValue)
                                )
                );

            } else {

                map.forEach(
                        (key, mapValue) -> {

                            values.add(
                                    String.valueOf(key)
                            );

                            values.add(
                                    String.valueOf(mapValue)
                            );
                        }
                );
            }

            return new AgateSerializedParameter(
                    name,
                    List.of(
                            String.join(
                                    ",",
                                    values
                            )
                    )
            );
        }

        throw unsupportedValue(
                parameter,
                value
        );
    }


    private AgateSerializedParameter serializeHeader(
            AgateRequestParameterModel parameter,
            Object value,
            String style,
            boolean explode) {

        if (!"simple".equals(style)) {

            throw new AgateParameterSerializationException(
                    "Unsupported header style: "
                            + style
            );
        }

        return serializePath(
                parameter,
                value,
                style,
                explode
        );
    }


    private AgateSerializedParameter serializeCookie(
            AgateRequestParameterModel parameter,
            Object value,
            String style,
            boolean explode) {

        if (!"form".equals(style)) {

            throw new AgateParameterSerializationException(
                    "Unsupported cookie style: "
                            + style
            );
        }

        return serializeQuery(
                parameter,
                value,
                style,
                explode
        );
    }


    private String resolveStyle(
            AgateRequestParameterModel parameter) {

        if (parameter.getStyle() != null &&
                !parameter.getStyle().isBlank()) {

            return parameter.getStyle();
        }

        String location =
                parameter.getLocation();

        if ("query".equals(location) ||
                "cookie".equals(location)) {

            return "form";
        }

        if ("path".equals(location) ||
                "header".equals(location)) {

            return "simple";
        }

        return null;
    }


    private boolean resolveExplode(
            AgateRequestParameterModel parameter,
            String style) {

        if (parameter.getExplode() != null) {

            return parameter.getExplode();
        }

        return "form".equals(
                style
        );
    }


    private AgateSerializedParameter single(
            String name,
            Object value) {

        return new AgateSerializedParameter(
                name,
                List.of(
                        String.valueOf(
                                value
                        )
                )
        );
    }


    private boolean isPrimitive(
            Object value) {

        return value instanceof String
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof Enum<?>;
    }


    private boolean isArray(
            Object value) {

        return value instanceof Collection<?>
                || value
                        .getClass()
                        .isArray();
    }


    private List<String> toValues(
            Object value) {

        List<String> result =
                new ArrayList<>();

        if (value instanceof Collection<?> collection) {

            for (Object item :
                    collection) {

                result.add(
                        String.valueOf(
                                item
                        )
                );
            }

            return result;
        }

        if (value
                .getClass()
                .isArray()) {

            int length =
                    Array.getLength(
                            value
                    );

            for (int i = 0;
                 i < length;
                 i++) {

                result.add(
                        String.valueOf(
                                Array.get(
                                        value,
                                        i
                                )
                        )
                );
            }

            return result;
        }

        throw new AgateParameterSerializationException(
                "Value is not an array or collection"
        );
    }


    private AgateParameterSerializationException unsupportedValue(
            AgateRequestParameterModel parameter,
            Object value) {

        return new AgateParameterSerializationException(
                "Unsupported value type for parameter "
                        + parameter.getName()
                        + ": "
                        + value
                                .getClass()
                                .getName()
        );
    }
}