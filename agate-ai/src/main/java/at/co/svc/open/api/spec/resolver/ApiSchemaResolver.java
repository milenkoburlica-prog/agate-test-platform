package at.co.svc.open.api.spec.resolver;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;



public class ApiSchemaResolver {


    public Schema<?> resolve(
            OpenAPI api,
            Schema<?> schema) {


        if (schema == null) {
            return null;
        }


        if (schema.get$ref() == null) {
            return schema;
        }



        String ref =
                schema.get$ref();



        if (ref.startsWith(
                "#/components/schemas/")) {


            String name =
                    ref.substring(
                            "#/components/schemas/"
                                    .length()
                    );



            Components components =
                    api.getComponents();



            if (components != null
                    && components.getSchemas() != null) {


                Schema<?> resolved =
                        components.getSchemas()
                                .get(name);



                if (resolved != null) {

                    return resolved;

                }

            }

        }


        return schema;

    }

}