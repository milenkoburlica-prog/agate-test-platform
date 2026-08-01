package at.co.svc.open.api.spec.validaton;

import java.util.List;

import at.co.svc.open.api.spec.model.GeneratedTestCase;


public class TestCaseConsistencyValidator {


    public List<GeneratedTestCase> validate(
            List<GeneratedTestCase> tests) {


        tests.removeIf(this::isInconsistent);


        return tests;
    }



    private boolean isInconsistent(GeneratedTestCase tc) {

        String description = tc.getDescription();

        if (description == null) {
            return false;
        }

        description = description.toLowerCase();

        String expected = tc.getExpectedResult();


        if (expected == null) {
            return true;
        }


        /*
         * Missing mandatory parameter
         */
        if ((description.contains("missing")
                || description.contains("without"))
                && !expected.equals("HTTP 400")) {

            return true;
        }


        /*
         * Invalid format/value
         */
        if ((description.contains("invalid")
                || description.contains("wrong")
                || description.contains("malformed"))
                && !expected.equals("HTTP 400")) {

            return true;
        }


        /*
         * Resource not found
         */
        if ((description.contains("non-existent")
                || description.contains("unknown")
                || description.contains("not exist"))
                && !expected.equals("HTTP 404")) {

            return true;
        }


        return false;
    }
    
}