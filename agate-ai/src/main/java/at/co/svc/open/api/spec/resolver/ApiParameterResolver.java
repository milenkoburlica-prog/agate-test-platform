package at.co.svc.open.api.spec.resolver;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;



public class ApiParameterResolver {


    public Parameter resolve(
            OpenAPI api,
            Parameter parameter) {


        if(parameter == null) {
            return null;
        }



        if(parameter.getName()!=null) {
            return parameter;
        }



        String ref =
                parameter.get$ref();



        if(ref == null) {
            return parameter;
        }



        if(ref.startsWith(
                "#/components/parameters/")) {


            String name =
                    ref.substring(
                    "#/components/parameters/".length()
                    );



            Components components =
                    api.getComponents();



            if(components != null
                    && components.getParameters()!=null) {


                Parameter resolved =
                        components.getParameters()
                                .get(name);



                if(resolved!=null) {

                    return resolved;

                }

            }

        }



        return parameter;

    }

}