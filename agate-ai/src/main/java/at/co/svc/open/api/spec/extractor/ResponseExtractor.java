package at.co.svc.open.api.spec.extractor;


import java.util.ArrayList;
import java.util.List;

import at.co.svc.open.api.spec.model.ResponseField;
import at.co.svc.open.api.spec.resolver.ApiSchemaResolver;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.OpenAPI;



public class ResponseExtractor {


    private final ResponseSchemaExtractor schemaExtractor =
            new ResponseSchemaExtractor();



    public List<String> extract(
            Operation operation) {


        List<String> result =
                new ArrayList<>();


        if(operation == null
                || operation.getResponses() == null) {

            return result;
        }



        operation.getResponses()
                .forEach((code,response)-> {

                    result.add(code);

                });



        return result;

    }





    public List<ResponseField> extractFields(
            OpenAPI api,
            Operation operation) {


        List<ResponseField> result =
                new ArrayList<>();


        if(operation == null
                || operation.getResponses()==null) {

            return result;
        }



        ApiResponse response =
                findSuccessResponse(
                        operation
                );



        if(response==null
                || response.getContent()==null) {

            return result;
        }



        Content content =
                response.getContent();



        MediaType media =
                content.values()
                       .stream()
                       .findFirst()
                       .orElse(null);



        if(media==null
                || media.getSchema()==null) {

            return result;
        }



        return schemaExtractor.extract(
                api,
                media.getSchema()
        );

    }





    private ApiResponse findSuccessResponse(
            Operation operation) {


        if(operation.getResponses()
                .containsKey("200")) {

            return operation.getResponses()
                    .get("200");
        }


        if(operation.getResponses()
                .containsKey("201")) {

            return operation.getResponses()
                    .get("201");
        }


        if(operation.getResponses()
                .containsKey("202")) {

            return operation.getResponses()
                    .get("202");
        }


        return null;

    }

}