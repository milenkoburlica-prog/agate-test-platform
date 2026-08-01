package at.co.svc.open.api.spec.extractor;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.co.svc.open.api.spec.model.ResponseField;
import at.co.svc.open.api.spec.resolver.ApiSchemaResolver;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;



public class ResponseSchemaExtractor {


    private final ApiSchemaResolver resolver =
            new ApiSchemaResolver();



    public List<ResponseField> extract(
            OpenAPI api,
            Schema<?> schema) {


        List<ResponseField> result =
                new ArrayList<>();


        Schema<?> resolved =
                resolver.resolve(
                        api,
                        schema
                );


        extractFields(
                api,
                resolved,
                "",
                result
        );


        return result;

    }







    private void extractFields(
            OpenAPI api,
            Schema<?> schema,
            String prefix,
            List<ResponseField> result) {


        if(schema == null) {
            return;
        }



        Schema<?> resolved =
                resolver.resolve(
                        api,
                        schema
                );



        if("array".equals(resolved.getType())
                && resolved.getItems()!=null) {


            extractFields(
                    api,
                    resolved.getItems(),
                    prefix + "[*]",
                    result
            );

            return;

        }



        if(resolved.getProperties()==null) {
            return;
        }




        for(Map.Entry<String,Schema> entry :
                resolved.getProperties()
                        .entrySet()) {


            String path =
                    prefix.isBlank()
                    ?
                    entry.getKey()
                    :
                    prefix + "." + entry.getKey();



            Schema<?> fieldSchema =
                    resolver.resolve(
                            api,
                            entry.getValue()
                    );



            result.add(
                    new ResponseField(
                            path,
                            fieldSchema
                    )
            );



            extractFields(
                    api,
                    fieldSchema,
                    path,
                    result
            );

        }

    }

}