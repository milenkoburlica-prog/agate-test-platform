package at.co.svc.open.api.spec.extractor;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.co.svc.open.api.spec.model.ApiParameter;
import at.co.svc.open.api.spec.model.ParameterLocation;
import at.co.svc.open.api.spec.resolver.ApiParameterResolver;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;


public class ParameterExtractor {


    private final ApiParameterResolver resolver =
            new ApiParameterResolver();



    public List<ApiParameter> extract(
            OpenAPI api,
            PathItem pathItem,
            Operation operation) {


        Map<String, ApiParameter> unique =
                new LinkedHashMap<>();


        List<Parameter> parameters =
                new ArrayList<>();



        if(pathItem != null
                && pathItem.getParameters() != null) {

            parameters.addAll(
                    pathItem.getParameters()
            );
        }



        if(operation != null
                && operation.getParameters() != null) {

            parameters.addAll(
                    operation.getParameters()
            );
        }




        for(Parameter raw : parameters) {


            Parameter parameter =
                    resolver.resolve(
                            api,
                            raw
                    );


            if(parameter == null
                    || parameter.getName() == null) {

                continue;
            }



            String key =
                    parameter.getName()
                    + ":"
                    + parameter.getIn();



            if(unique.containsKey(key)) {
                continue;
            }



            ApiParameter result =
                    new ApiParameter();



            result.setName(
                    parameter.getName()
            );


            result.setLocation(
                    convertLocation(
                            parameter.getIn()
                    )
            );


            result.setRequired(
                    Boolean.TRUE.equals(
                            parameter.getRequired()
                    )
            );



            if(parameter.getSchema() != null) {


                result.setType(
                        parameter.getSchema()
                                .getType()
                );


                result.setFormat(
                        parameter.getSchema()
                                .getFormat()
                );


                result.setPattern(
                        parameter.getSchema()
                                .getPattern()
                );


                if(parameter.getSchema()
                        .getEnum() != null) {

                    result.setEnumValues(
                            parameter.getSchema()
                                    .getEnum()
                                    .stream()
                                    .map(Object::toString)
                                    .toList()
                    );
                }

            }



            if(parameter.getExample()!=null) {

                result.setExample(
                        parameter.getExample()
                                .toString()
                );
            }



            if(parameter.getDescription()!=null) {

                result.setDescription(
                        parameter.getDescription()
                );
            }



            unique.put(
                    key,
                    result
            );

        }



        return new ArrayList<>(
                unique.values()
        );
    }






    private ParameterLocation convertLocation(
            String location) {


        if("path".equalsIgnoreCase(location)) {
            return ParameterLocation.PATH;
        }


        if("header".equalsIgnoreCase(location)) {
            return ParameterLocation.HEADER;
        }


        if("cookie".equalsIgnoreCase(location)) {
            return ParameterLocation.COOKIE;
        }


        if("query".equalsIgnoreCase(location)) {
            return ParameterLocation.QUERY;
        }


        return ParameterLocation.BODY;
    }

}