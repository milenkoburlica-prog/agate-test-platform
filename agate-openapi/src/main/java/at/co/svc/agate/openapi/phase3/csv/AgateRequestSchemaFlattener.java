package at.co.svc.agate.openapi.phase3.csv;

import at.co.svc.agate.openapi.model.AgateSchema;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public class AgateRequestSchemaFlattener {


    public Set<String> collectKeys(
            AgateSchema schema) {

        Set<String> result =
                new LinkedHashSet<>();


        collect(
                "",
                schema,
                result
        );


        return result;
    }




    private void collect(
            String path,
            AgateSchema schema,
            Set<String> result) {

        if (schema == null) {

            return;
        }


        if (schema.isReadOnly()) {

            return;
        }


        String type =
                schema.getType();


        if ("object".equals(type)) {

            collectObject(
                    path,
                    schema,
                    result
            );

            return;
        }


        if ("array".equals(type)) {

            collectArray(
                    path,
                    schema,
                    result
            );

            return;
        }


        if (!path.isBlank()) {

            result.add(
                    path
            );
        }
    }




    private void collectObject(
            String path,
            AgateSchema schema,
            Set<String> result) {

        if (schema.getProperties() == null ||
                schema.getProperties().isEmpty()) {

            if (!path.isBlank()) {

                result.add(
                        path
                );
            }


            return;
        }


        for (Map.Entry<String, AgateSchema> entry :
                schema.getProperties().entrySet()) {

            String childPath =
                    path.isBlank()
                            ? entry.getKey()
                            : path
                            + "."
                            + entry.getKey();


            collect(
                    childPath,
                    entry.getValue(),
                    result
            );
        }
    }




    private void collectArray(
            String path,
            AgateSchema schema,
            Set<String> result) {

        AgateSchema items =
                schema.getItems();


        /*
         * Array<Object>:
         *
         * tags[].id is represented in our AGATE CSV convention
         * as:
         *
         * tags.id
         * tags.name
         */

        if (items != null &&
                "object".equals(
                        items.getType()
                )) {

            collect(
                    path,
                    items,
                    result
            );

            return;
        }


        /*
         * Primitive array remains one CSV row:
         *
         * photoUrls
         * groups
         */

        if (!path.isBlank()) {

            result.add(
                    path
            );
        }
    }
}