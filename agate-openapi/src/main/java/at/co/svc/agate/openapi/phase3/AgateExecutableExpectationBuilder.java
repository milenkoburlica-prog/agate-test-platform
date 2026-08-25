package at.co.svc.agate.openapi.phase3;

import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestCase;

import at.co.svc.agate.openapi.phase3.model.AgateExecutableExpectation;


public class AgateExecutableExpectationBuilder {


    public AgateExecutableExpectation build(
            AgateGeneratedTestCase testCase) {

        if (testCase == null) {

            throw new IllegalArgumentException(
                    "Generated test case must not be null"
            );
        }


        AgateExecutableExpectation result =
                new AgateExecutableExpectation();


        result.setExpectedOutcome(
                testCase.getExpectedOutcome()
        );


        result.setExpectedStatusCode(
                testCase.getExpectedStatusCode()
        );


        result.setExactStatusRequired(
                testCase.getExpectedStatusCode()
                        != null
                        &&
                        !testCase
                                .getExpectedStatusCode()
                                .isBlank()
        );


        return result;
    }
}