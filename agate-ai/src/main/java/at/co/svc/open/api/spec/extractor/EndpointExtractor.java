package at.co.svc.open.api.spec.extractor;


import java.util.ArrayList;
import java.util.List;

import at.co.svc.open.api.spec.model.EndpointDescription;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;



public class EndpointExtractor {


    private final ParameterExtractor parameterExtractor =
            new ParameterExtractor();


    private final ResponseExtractor responseExtractor =
            new ResponseExtractor();




    public List<EndpointDescription> extract(
            OpenAPI api) {


        List<EndpointDescription> result =
                new ArrayList<>();


        if(api == null
                || api.getPaths()==null) {

            return result;
        }



        api.getPaths()
                .forEach((path,item)-> {


                    add(result,api,path,"GET",item,item.getGet());

                    add(result,api,path,"HEAD",item,item.getHead());

                    add(result,api,path,"POST",item,item.getPost());

                    add(result,api,path,"PUT",item,item.getPut());

                    add(result,api,path,"DELETE",item,item.getDelete());

                    add(result,api,path,"PATCH",item,item.getPatch());

                });



        return result;

    }








    private void add(
            List<EndpointDescription> result,
            OpenAPI api,
            String path,
            String method,
            PathItem item,
            Operation operation) {


        if(operation == null) {
            return;
        }




        EndpointDescription endpoint =
                new EndpointDescription();




        endpoint.setPath(path);

        endpoint.setMethod(method);



        endpoint.setOperationId(
                operation.getOperationId()
        );


        endpoint.setSummary(
                operation.getSummary()
        );


        endpoint.setDescription(
                operation.getDescription()
        );





        endpoint.setParameters(
                parameterExtractor.extract(
                        api,
                        item,
                        operation
                )
        );




        endpoint.setResponses(
                responseExtractor.extract(
                        operation
                )
        );





        endpoint.setValidations(
                responseExtractor.extractFields(
                        api,
                        operation
                )
        );





        System.out.println(
                endpoint.getPath()
                + " RESPONSE FIELDS="
                + endpoint.getValidations()
        );




        result.add(endpoint);

    }

}