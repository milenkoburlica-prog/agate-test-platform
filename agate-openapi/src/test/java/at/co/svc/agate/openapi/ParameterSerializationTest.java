package at.co.svc.agate.openapi;

import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;
import at.co.svc.agate.openapi.phase1.serialization.AgateParameterSerializer;
import at.co.svc.agate.openapi.phase1.serialization.AgateSerializedParameter;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParameterSerializationTest {

    private final AgateParameterSerializer serializer =
            new AgateParameterSerializer();


    @Test
    void shouldSerializeQueryPrimitive() {

        AgateRequestParameterModel parameter =
                parameter(
                        "details",
                        "query",
                        "form",
                        true
                );

        AgateSerializedParameter result =
                serializer.serialize(
                        parameter,
                        true
                );

        assertEquals(
                "details",
                result.getName()
        );

        assertEquals(
                List.of(
                        "true"
                ),
                result.getValues()
        );
    }


    @Test
    void shouldSerializeQueryArrayFormExplodeTrue() {

        AgateRequestParameterModel parameter =
                parameter(
                        "groups",
                        "query",
                        "form",
                        true
                );

        AgateSerializedParameter result =
                serializer.serialize(
                        parameter,
                        List.of(
                                "Gruppe-1",
                                "Gruppe-2"
                        )
                );

        assertEquals(
                "groups",
                result.getName()
        );

        assertEquals(
                List.of(
                        "Gruppe-1",
                        "Gruppe-2"
                ),
                result.getValues()
        );
    }


    @Test
    void shouldSerializeQueryArrayFormExplodeFalse() {

        AgateRequestParameterModel parameter =
                parameter(
                        "groups",
                        "query",
                        "form",
                        false
                );

        AgateSerializedParameter result =
                serializer.serialize(
                        parameter,
                        List.of(
                                "Gruppe-1",
                                "Gruppe-2"
                        )
                );

        assertEquals(
                List.of(
                        "Gruppe-1,Gruppe-2"
                ),
                result.getValues()
        );
    }


    @Test
    void shouldSerializePathPrimitiveSimple() {

        AgateRequestParameterModel parameter =
                parameter(
                        "id",
                        "path",
                        "simple",
                        false
                );

        AgateSerializedParameter result =
                serializer.serialize(
                        parameter,
                        42
                );

        assertEquals(
                List.of(
                        "42"
                ),
                result.getValues()
        );
    }


    @Test
    void shouldSerializePathArraySimple() {

        AgateRequestParameterModel parameter =
                parameter(
                        "ids",
                        "path",
                        "simple",
                        false
                );

        AgateSerializedParameter result =
                serializer.serialize(
                        parameter,
                        List.of(
                                10,
                                20,
                                30
                        )
                );

        assertEquals(
                List.of(
                        "10,20,30"
                ),
                result.getValues()
        );
    }


    @Test
    void shouldSerializeHeaderArraySimple() {

        AgateRequestParameterModel parameter =
                parameter(
                        "X-Values",
                        "header",
                        "simple",
                        false
                );

        AgateSerializedParameter result =
                serializer.serialize(
                        parameter,
                        List.of(
                                "A",
                                "B",
                                "C"
                        )
                );

        assertEquals(
                List.of(
                        "A,B,C"
                ),
                result.getValues()
        );
    }


    @Test
    void shouldSerializeQueryObjectFormExplodeFalse() {

        AgateRequestParameterModel parameter =
                parameter(
                        "filter",
                        "query",
                        "form",
                        false
                );

        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put(
                "role",
                "ADMIN"
        );

        value.put(
                "active",
                true
        );

        AgateSerializedParameter result =
                serializer.serialize(
                        parameter,
                        value
                );

        assertEquals(
                List.of(
                        "role,ADMIN,active,true"
                ),
                result.getValues()
        );
    }


    @Test
    void shouldUseOpenApiQueryDefaults() {

        AgateRequestParameterModel parameter =
                parameter(
                        "groups",
                        "query",
                        null,
                        null
                );

        AgateSerializedParameter result =
                serializer.serialize(
                        parameter,
                        List.of(
                                "A",
                                "B"
                        )
                );

        assertEquals(
                List.of(
                        "A",
                        "B"
                ),
                result.getValues()
        );
    }


    private AgateRequestParameterModel parameter(
            String name,
            String location,
            String style,
            Boolean explode) {

        AgateRequestParameterModel result =
                new AgateRequestParameterModel();

        result.setName(
                name
        );

        result.setLocation(
                location
        );

        result.setStyle(
                style
        );

        result.setExplode(
                explode
        );

        return result;
    }
}