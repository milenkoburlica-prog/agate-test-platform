package at.co.svc.open.api.spec.runner;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import at.co.svc.open.api.spec.generator.AgateTemplateCsvGenerator;
import at.co.svc.open.api.spec.generator.AgateTemplateGenerator;
import at.co.svc.open.api.spec.generator.AgateYamlGenerator;
import at.co.svc.open.api.spec.generator.ModuleMetadataGenerator;
import at.co.svc.open.api.spec.generator.TemplateFileWriter;
import at.co.svc.open.api.spec.generator.TestCaseGenerator;
import at.co.svc.open.api.spec.model.EndpointDescription;
import at.co.svc.open.api.spec.model.GeneratedTestCase;



public class OpenApiTestRunner {


    public static void main(String[] args) {


        String url =
        "https://ecs-webapps-ecs-syst-syst.ecs.ocp-stag.cloud.svc.co.at/clv-svsc/openapi/openapi-clv-svsc-v1.yaml";



        try {


            String baseUrl =
                    extractBaseUrl(url);



            String app =
                    "clv-svsc";



            TestCaseGenerator generator =
                    new TestCaseGenerator();



            AgateYamlGenerator yamlGenerator =
                    new AgateYamlGenerator();



            ModuleMetadataGenerator metadataGenerator =
                    new ModuleMetadataGenerator();



            AgateTemplateGenerator templateGenerator =
                    new AgateTemplateGenerator();



            AgateTemplateCsvGenerator csvGenerator =
                    new AgateTemplateCsvGenerator();



            TemplateFileWriter templateWriter =
                    new TemplateFileWriter();




            List<EndpointDescription> endpoints =
                    generator.extractEndpoints(url);




            for(EndpointDescription endpoint :
                    endpoints) {



                List<GeneratedTestCase> tests =
                        generator.generateForEndpoint(
                                endpoint
                        );



                System.out.println(
                        "GENERATED TESTS: "
                        + endpoint.getMethod()
                        + " "
                        + endpoint.getPath()
                        + " count="
                        + tests.size()
                );



                System.out.println(
                        endpoint.getPath()
                        + " RESPONSE FIELDS="
                        + endpoint.getValidations()
                );




                String module =
                        endpoint.getMethod()
                        .toLowerCase()
                        +
                        endpoint.getPath()
                                .replaceAll(
                                        "[^a-zA-Z0-9]",
                                        "_"
                                );




                Path moduleDir =
                        Path.of(
                                "data",
                                app,
                                "modules",
                                "rest",
                                module
                        );



                Files.createDirectories(
                        moduleDir
                );




                Files.writeString(
                        moduleDir.resolve(
                                "metadata.json"
                        ),
                        metadataGenerator.generate(
                                endpoint
                        )
                );




                Files.writeString(
                        Path.of(
                                "data",
                                app,
                                module + ".yaml"
                        ),
                        yamlGenerator.generate(
                                module,
                                baseUrl,
                                tests
                        )
                );




                /*
                 * Template generation per endpoint
                 */
                Path templateDir =
                        Path.of(
                                "data",
                                app,
                                "template"
                        );



                String templateYaml =
                        templateGenerator.generate(
                                tests
                        );



                String templateCsv =
                        csvGenerator.generate(
                                baseUrl,
                                tests
                        );


                templateWriter.write(
                        templateDir,
                        module,
                        templateYaml,
                        templateCsv
                );

            }


        }
        catch(Exception e) {

            e.printStackTrace();

        }

    }



    private static String extractBaseUrl(
            String openApiUrl) {


        java.net.URI uri =
                java.net.URI.create(
                        openApiUrl
                );


        return uri.getScheme()
                + "://"
                + uri.getHost();

    }

}