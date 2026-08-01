package at.co.svc.open.api.spec.generator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.co.svc.open.api.spec.ai.OllamaService;
import at.co.svc.open.api.spec.downloader.OpenApiDownloader;
import at.co.svc.open.api.spec.extractor.EndpointExtractor;
import at.co.svc.open.api.spec.loader.OpenApiLoader;
import at.co.svc.open.api.spec.model.EndpointDescription;
import at.co.svc.open.api.spec.model.GeneratedTestCase;
import at.co.svc.open.api.spec.validaton.OpenApiTestQualityValidator;
import at.co.svc.open.api.spec.validaton.ResponseValidator;
import at.co.svc.open.api.spec.validaton.TestCaseConsistencyValidator;


public class TestCaseGenerator {


    private final ResponseValidationGenerator responseValidationGenerator =
            new ResponseValidationGenerator();


    private final OpenApiDownloader downloader =
            new OpenApiDownloader();


    private final OpenApiLoader loader =
            new OpenApiLoader();


    private final EndpointExtractor extractor =
            new EndpointExtractor();


    private final OllamaService ollama =
            new OllamaService();


    private final ResponseValidator responseValidator =
            new ResponseValidator();


    private final TestCaseConsistencyValidator consistencyValidator =
            new TestCaseConsistencyValidator();


    private final OpenApiTestQualityValidator qualityValidator =
            new OpenApiTestQualityValidator();


    private final HallucinationDetector hallucinationDetector =
            new HallucinationDetector();


    private final TestNameGenerator testNameGenerator =
            new TestNameGenerator();


    private final OpenApiTestDataGenerator dataGenerator =
            new OpenApiTestDataGenerator();




    public List<EndpointDescription> extractEndpoints(
            String url)
            throws Exception {


        Path file =
                downloader.download(url);


        return extractor.extract(
                loader.load(file)
        );

    }





    public List<GeneratedTestCase> generateForEndpoint(
            EndpointDescription endpoint)
            throws Exception {



        List<GeneratedTestCase> cases;



        if(endpoint.getMethod().equalsIgnoreCase("HEAD")) {


            GeneratedTestCase tc =
                    new GeneratedTestCase();



            tc.setName(
                    "Verify_"
                    + endpoint.getOperationId()
                    + "_returns_200"
            );



            tc.setDescription(
                    "Verify HEAD endpoint is available"
            );



            tc.setEndpoint(
                    endpoint.getPath()
            );



            tc.setMethod(
                    endpoint.getMethod()
            );



            tc.setCategory(
                    "availability"
            );



            tc.setExpectedResult(
                    "HTTP 200"
            );



            cases =
                    new ArrayList<>();


            cases.add(tc);



        }
        else {


            cases =
                    ollama.generateTestCases(endpoint);

        }




        String validData =
                dataGenerator.generateValid(endpoint);





        String command =
                endpoint.getMethod()
                        .toLowerCase()
                        +
                        endpoint.getPath()
                                .replaceAll(
                                        "[^a-zA-Z0-9]",
                                        "_"
                                );





        for(GeneratedTestCase tc : cases) {



            hallucinationDetector.sanitize(
                    tc,
                    endpoint
            );



            if(tc.getTestData() == null
                    || tc.getTestData().isBlank()) {


                tc.setTestData(
                        validData
                );

            }



            tc.setParameters(
                    endpoint.getParameters()
            );



            tc.setCommand(
                    command
            );

        }





        GeneratedTestCase responseValidation =
                responseValidationGenerator.generate(
                        endpoint
                );



        if(responseValidation != null) {


            responseValidation.setTestData(
                    validData
            );


            responseValidation.setParameters(
                    endpoint.getParameters()
            );


            responseValidation.setCommand(
                    command
            );


            cases.add(
                    responseValidation
            );

        }






        List<GeneratedTestCase> cleaned =
                new ArrayList<>();



        for(GeneratedTestCase tc : cases) {



            if(!responseValidator.isAllowed(
                    tc,
                    endpoint)) {

                continue;

            }



            testNameGenerator.normalize(
                    tc,
                    endpoint
            );



            cleaned.add(tc);

        }






        List<GeneratedTestCase> consistent =
                consistencyValidator.validate(
                        cleaned
                );




        Set<String> keys =
                new HashSet<>();



        List<GeneratedTestCase> unique =
                new ArrayList<>();



        for(GeneratedTestCase tc : consistent) {


            String key =
                    tc.getName()
                    + "|"
                    + tc.getExpectedResult()
                    + "|"
                    + tc.getTestData()
                    + "|"
                    + tc.getResponseValidations();



            if(keys.add(key)) {


                unique.add(tc);

            }

        }




        return qualityValidator.validate(
                unique,
                endpoint
        );


    }

}