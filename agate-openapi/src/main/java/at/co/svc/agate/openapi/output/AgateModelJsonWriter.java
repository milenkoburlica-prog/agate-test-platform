package at.co.svc.agate.openapi.output;

import at.co.svc.agate.openapi.model.AgateOpenApiModel;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class AgateModelJsonWriter {

    private final ObjectMapper objectMapper;


    public AgateModelJsonWriter() {

        this.objectMapper =
                new ObjectMapper();

        objectMapper.enable(
                SerializationFeature.INDENT_OUTPUT
        );

        objectMapper.enable(
                SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS
        );

        objectMapper.configure(
                MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,
                true
        );
    }


    public String write(
            AgateOpenApiModel model) throws Exception {

        return objectMapper
                .writeValueAsString(
                        model
                );
    }
}