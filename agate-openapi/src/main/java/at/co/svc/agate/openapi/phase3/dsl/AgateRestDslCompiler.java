package at.co.svc.agate.openapi.phase3.dsl;

import at.co.svc.agate.openapi.phase3.dsl.model.AgateRestDslExpectation;
import at.co.svc.agate.openapi.phase3.dsl.model.AgateRestDslRequest;
import at.co.svc.agate.openapi.phase3.dsl.model.AgateRestDslTest;

import at.co.svc.agate.openapi.phase3.model.AgateExecutableExpectation;
import at.co.svc.agate.openapi.phase3.model.AgateExecutableRequest;
import at.co.svc.agate.openapi.phase3.model.AgateExecutableTest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AgateRestDslCompiler {


    public AgateRestDslTest compile(
            AgateExecutableTest executableTest) {

        if (executableTest == null) {

            throw new IllegalArgumentException(
                    "Executable test must not be null"
            );
        }


        AgateRestDslTest result =
                new AgateRestDslTest();


        result.setTechnicalName(
                executableTest.getTechnicalName()
        );


        result.setSourceTestId(
                executableTest.getId()
        );


        result.setOperationIdentity(
                executableTest.getOperationIdentity()
        );


        result.setName(
                executableTest.getName()
        );


        result.setSourceType(
                executableTest.getSourceType()
        );


        result.setRequest(
                compileRequest(
                        executableTest.getRequest()
                )
        );


        result.setExpectation(
                compileExpectation(
                        executableTest.getExpectation()
                )
        );


        return result;
    }




    private AgateRestDslRequest compileRequest(
            AgateExecutableRequest source) {

        if (source == null) {

            throw new IllegalArgumentException(
                    "Executable request must not be null"
            );
        }


        AgateRestDslRequest result =
                new AgateRestDslRequest();


        result.setMethod(
                source.getMethod()
        );


        result.setPath(
                source.getResolvedPath()
        );


        result.setQuery(
                deepCopyMap(
                        source.getQueryParameters()
                )
        );


        result.setHeaders(
                deepCopyMap(
                        source.getHeaders()
                )
        );


        result.setCookies(
                deepCopyMap(
                        source.getCookies()
                )
        );


        result.setBodyMediaType(
                source.getBodyMediaType()
        );


        result.setBody(
                deepCopyValue(
                        source.getBody()
                )
        );


        return result;
    }




    private AgateRestDslExpectation compileExpectation(
            AgateExecutableExpectation source) {

        if (source == null) {

            throw new IllegalArgumentException(
                    "Executable expectation must not be null"
            );
        }


        AgateRestDslExpectation result =
                new AgateRestDslExpectation();


        result.setOutcome(
                source.getExpectedOutcome()
        );


        result.setStatusCode(
                source.getExpectedStatusCode()
        );


        result.setExactStatusRequired(
                source.isExactStatusRequired()
        );


        return result;
    }




    private Map<String, Object> deepCopyMap(
            Map<String, Object> source) {

        Map<String, Object> result =
                new LinkedHashMap<>();


        if (source == null) {

            return result;
        }


        source.forEach(
                (key, value) ->
                        result.put(
                                key,
                                deepCopyValue(
                                        value
                                )
                        )
        );


        return result;
    }




    private Object deepCopyValue(
            Object value) {

        if (value instanceof Map<?, ?> map) {

            Map<String, Object> result =
                    new LinkedHashMap<>();


            map.forEach(
                    (key, nestedValue) ->
                            result.put(
                                    String.valueOf(
                                            key
                                    ),
                                    deepCopyValue(
                                            nestedValue
                                    )
                            )
            );


            return result;
        }


        if (value instanceof List<?> list) {

            List<Object> result =
                    new ArrayList<>();


            for (Object item :
                    list) {

                result.add(
                        deepCopyValue(
                                item
                        )
                );
            }


            return result;
        }


        return value;
    }
}