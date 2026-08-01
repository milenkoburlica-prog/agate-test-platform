package at.co.svc.open.api.spec.ai;


import at.co.svc.open.api.spec.model.EndpointDescription;
import at.co.svc.open.api.spec.model.GeneratedTestCase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;



public class OllamaService {


    private final OllamaChatModel model;

    private final ObjectMapper mapper;



    public OllamaService() {


        this.model =
                OllamaChatModel.builder()

                .baseUrl(
                    "http://localhost:11434"
                )

                .modelName(
                    "deepseek-coder-v2:latest"
                )

                .timeout(
                    Duration.ofMinutes(5)
                )

                .build();


        this.mapper =
                new ObjectMapper();

    }





    public List<GeneratedTestCase> generateTestCases(
            EndpointDescription endpoint) {


        String prompt =
                buildPrompt(endpoint);



        String response =
                model.generate(prompt);



        return parseResponse(
                response,
                endpoint
        );

    }





    private String buildPrompt(
            EndpointDescription endpoint) {


        String allowedParameters =
                endpoint.getParameters()
                .stream()
                .map(p -> p.getName())
                .collect(Collectors.joining(","));



        return """

You are a senior QA engineer specialized in REST API testing.


Analyze the OpenAPI endpoint and create enterprise test scenarios.


IMPORTANT ARCHITECTURE RULE:

OpenAPI specification is the ONLY source of truth for parameters.

You MUST NOT invent parameters.

Allowed parameters:

%s


Do not use:
id
sector
id-type
or any other parameter
unless it exists in the allowed parameter list.



Generate test cases for:


Positive scenarios:

- valid request
- successful business operation


Negative scenarios:

- invalid format
- missing mandatory parameter
- invalid enum value
- invalid business condition


Error scenarios:

Generate tests only for HTTP responses defined by OpenAPI.


Test names:

Use meaningful business names.

Example:

Verify_getGino_returns_success_for_valid_clientIp


Avoid:

Test_API
Check_endpoint



VERY IMPORTANT:

Do NOT generate testData.

testData will be generated automatically from OpenAPI.


Return ONLY JSON.


Format:


[
 {
   "name":"Verify_operation_success",
   "description":"Verify successful operation",
   "category":"positive",
   "expectedResult":"HTTP 200"
 }
]



Endpoint:

Method:
%s


Path:
%s


Summary:
%s


Description:
%s


Parameters:
%s


Responses:
%s


""".formatted(

                allowedParameters,
                endpoint.getMethod(),
                endpoint.getPath(),
                endpoint.getSummary(),
                endpoint.getDescription(),
                endpoint.getParameters(),
                endpoint.getResponses()

        );

    }







    private List<GeneratedTestCase> parseResponse(
            String response,
            EndpointDescription endpoint) {


        try {


            String json =
                    extractJson(response);



            List<GeneratedTestCase> result =
                    mapper.readValue(
                            json,
                            new TypeReference<List<GeneratedTestCase>>() {}
                    );



            for(GeneratedTestCase tc:result) {


                tc.setEndpoint(
                        endpoint.getPath()
                );


                tc.setMethod(
                        endpoint.getMethod()
                );

            }



            return result;



        }
        catch(Exception e) {


            throw new RuntimeException(
                    "Cannot parse AI response:\n"
                    +response,
                    e
            );

        }

    }







    private String extractJson(
            String response) {


        response =
                response
                .replace("```json","")
                .replace("```","")
                .trim();



        int start =
                response.indexOf("[");


        int end =
                response.lastIndexOf("]");



        if(start==-1 || end==-1) {


            throw new RuntimeException(
                    "No JSON array found"
            );

        }



        return response.substring(
                start,
                end+1
        );

    }

}