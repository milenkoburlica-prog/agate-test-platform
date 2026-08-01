package at.co.svc.open.api.spec.generator;

import at.co.svc.open.api.spec.model.EndpointDescription;
import at.co.svc.open.api.spec.model.GeneratedTestCase;

public class TestNameGenerator {

    public void normalize(GeneratedTestCase tc, EndpointDescription endpoint) {

        if (tc == null || endpoint == null) {
            return;
        }
        /*
         * Response validation tests already have meaningful names:
         *
         * Verify_getGino_response_contains_serialNumber
         *
         * Do not overwrite them.
         */
        if ("response-validation".equals(tc.getCategory())) {
            return;
        }
        
        
        String operation = endpoint.getOperationId();

        if (operation == null || operation.isBlank()) {

            operation = endpoint.getMethod().toLowerCase()
                    + "_"
                    + endpoint.getPath()
                    .replaceAll("[^a-zA-Z0-9]", "_")
                    .replaceAll("_+", "_")
                    .replaceAll("^_+|_+$", "");
        }


        String scenario = detectScenario(tc);


        String expected = tc.getExpectedResult();

        if (expected != null) {
            expected = expected.replace("HTTP ", "");
        }


        tc.setName(
                "Verify_"
                + operation
                + "_"
                + scenario
                + "_returns_"
                + expected
        );
    }


    private String detectScenario(GeneratedTestCase tc) {

        String text = (
                tc.getDescription()
                + " "
                + tc.getExpectedResult()
                ).toLowerCase();


        if (text.contains("missing")
                || text.contains("without")) {

            return "missing_parameter_returns_400";
        }


        if (text.contains("invalid")
                || text.contains("wrong")) {

            return "invalid_input_returns_400";
        }


        if (text.contains("non-existent")
                || text.contains("unknown")) {

            return "not_found_returns_404";
        }


        if (tc.getExpectedResult().equals("HTTP 200")) {

            return "success_returns_200";
        }


        return "scenario_returns_" +
                tc.getExpectedResult()
                  .replace("HTTP ", "");

    }
}