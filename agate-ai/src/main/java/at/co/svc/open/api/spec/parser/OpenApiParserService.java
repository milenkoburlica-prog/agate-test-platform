package at.co.svc.open.api.spec.parser;

import java.nio.file.Path;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class OpenApiParserService {


    public OpenAPI load(Path file) {


        ParseOptions options = new ParseOptions();

        /*
         * Resolve internal and external references
         */
        options.setResolve(true);

        /*
         * Replace $ref objects with real objects
         */
        options.setResolveFully(true);
        options.setResolveCombinators(true);

        SwaggerParseResult result =
                new OpenAPIParser()
                .readLocation(
                        file.toUri().toString(),
                        null,
                        options);


        if (result == null) {

            throw new RuntimeException(
                    "Swagger parser returned null result.");

        }


        if (result.getMessages() != null &&
                !result.getMessages().isEmpty()) {

            /** only for DEBUGING 
            System.out.println();
            System.out.println("========== PARSER MESSAGES ==========");


            result.getMessages()
                    .forEach(System.out::println);


            System.out.println("=====================================");
            System.out.println();
            **/

        }



        OpenAPI api = result.getOpenAPI();
//        System.out.println(
//                "External schemas loaded: "
//                + api.getComponents()
//                     .getParameters()
//                     .keySet()
//        );

        if (api == null) {

            throw new RuntimeException(
                    "OpenAPI object is null.");

        }



        /** Only for Debug purposes
        debugComponents(api);
        debugPaths(api);
        **/



        return api;

    }



//    private void debugComponents(OpenAPI api) {
//
//
//        System.out.println();
//        System.out.println("========================================");
//        System.out.println("COMPONENTS DEBUG");
//        System.out.println("========================================");
//
//
//        Components components = api.getComponents();
//
//
//        if (components == null) {
//
//            System.out.println("Components = NULL");
//            return;
//
//        }
//
//
//        if (components.getParameters() == null) {
//
//            System.out.println("No component parameters found.");
//
//            return;
//
//        }
//
//
//
//        System.out.println(
//                "Component parameters count = "
//                        + components.getParameters().size());
//
//
//
//        components.getParameters()
//                .forEach((name, parameter) -> {
//
//
//                    System.out.println("--------------------------------");
//
//                    System.out.println(
//                            "Component name : "
//                                    + name);
//
//
//                    System.out.println(
//                            "Parameter name  : "
//                                    + parameter.getName());
//
//
//                    System.out.println(
//                            "In              : "
//                                    + parameter.getIn());
//
//
//                    System.out.println(
//                            "Schema          : "
//                                    + parameter.getSchema());
//
//
//                });
//
//
//        System.out.println();
//
//    }
//
//
//
//
//
//    private void debugPaths(OpenAPI api) {
//
//
//        System.out.println();
//        System.out.println("========================================");
//        System.out.println("PATH DEBUG");
//        System.out.println("========================================");
//
//
//
//        if (api.getPaths() == null) {
//
//            System.out.println("Paths = NULL");
//            return;
//
//        }
//
//
//
//        api.getPaths()
//                .forEach((path, pathItem) -> {
//
//
//                    System.out.println();
//                    System.out.println("PATH: " + path);
//
//
//
//                    /*
//                     * Parameters can exist on PathItem level
//                     */
//                    debugParameters(
//                            "PATH LEVEL",
//                            pathItem.getParameters());
//
//
//
//                    printOperation(
//                            "GET",
//                            path,
//                            pathItem.getGet());
//
//
//                    printOperation(
//                            "POST",
//                            path,
//                            pathItem.getPost());
//
//
//                    printOperation(
//                            "PUT",
//                            path,
//                            pathItem.getPut());
//
//
//                    printOperation(
//                            "DELETE",
//                            path,
//                            pathItem.getDelete());
//
//
//                    printOperation(
//                            "PATCH",
//                            path,
//                            pathItem.getPatch());
//
//
//                    printOperation(
//                            "HEAD",
//                            path,
//                            pathItem.getHead());
//
//
//                });
//
//
//        System.out.println("========================================");
//        System.out.println();
//
//    }
//





//    private void printOperation(
//            String method,
//            String path,
//            Operation operation) {
//
//
//        if (operation == null) {
//            return;
//        }
//
//
//        System.out.println();
//        System.out.println(
//                method + " " + path);
//
//
//
//        debugParameters(
//                "OPERATION LEVEL",
//                operation.getParameters());
//
//
//    }
//
//
//



//    private void debugParameters(
//            String level,
//            java.util.List<Parameter> parameters) {
//
//
//
//        if (parameters == null) {
//
//            return;
//
//        }
//
//
//
//        System.out.println(level);
//
//        System.out.println(
//                "Parameter count = "
//                        + parameters.size());
//
//
//
//        for (Parameter p : parameters) {
//
//
//            System.out.println("--------------------------------");
//
//
//            System.out.println(
//                    "Class    : "
//                            + p.getClass().getName());
//
//
//            System.out.println(
//                    "Name     : "
//                            + p.getName());
//
//
//            System.out.println(
//                    "In       : "
//                            + p.getIn());
//
//
//            System.out.println(
//                    "Required : "
//                            + p.getRequired());
//
//
//            System.out.println(
//                    "$ref     : "
//                            + p.get$ref());
//
//
//
//            if (p.getSchema() != null) {
//
//
//                System.out.println(
//                        "Type     : "
//                                + p.getSchema().getType());
//
//
//                System.out.println(
//                        "Pattern  : "
//                                + p.getSchema().getPattern());
//
//
//                System.out.println(
//                        "Example  : "
//                                + p.getSchema().getExample());
//
//
//            } else {
//
//
//                System.out.println(
//                        "Schema   : NULL");
//
//
//            }
//
//
//        }
//
//
//    }

}