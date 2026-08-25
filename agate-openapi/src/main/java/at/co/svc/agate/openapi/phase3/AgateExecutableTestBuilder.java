package at.co.svc.agate.openapi.phase3;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestCase;

import at.co.svc.agate.openapi.phase3.model.AgateExecutableTest;


public class AgateExecutableTestBuilder {


    private final AgateExecutableRequestBuilder requestBuilder =
            new AgateExecutableRequestBuilder();


    private final AgateExecutableExpectationBuilder expectationBuilder =
            new AgateExecutableExpectationBuilder();




    public AgateExecutableTest build(
            AgateOperationModel operation,
            AgateGeneratedTestCase generatedTestCase) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }


        if (generatedTestCase == null) {

            throw new IllegalArgumentException(
                    "Generated test case must not be null"
            );
        }


        AgateExecutableTest result =
                new AgateExecutableTest();


        result.setId(
                generatedTestCase.getId()
        );


        result.setOperationIdentity(
                operation.getIdentity()
        );


        result.setName(
                generatedTestCase.getName()
        );


        result.setTechnicalName(
                createTechnicalName(
                        generatedTestCase
                )
        );


        result.setReason(
                generatedTestCase.getReason()
        );


        result.setSourceType(
                generatedTestCase.getType()
        );


        result.setRequest(
                requestBuilder.build(
                        operation,
                        generatedTestCase.getRequestValues()
                )
        );


        result.setExpectation(
                expectationBuilder.build(
                        generatedTestCase
                )
        );


        return result;
    }




    private String createTechnicalName(
            AgateGeneratedTestCase testCase) {

        String sequence =
                extractSequence(
                        testCase.getId()
                );


        String normalizedName =
                normalizeName(
                        testCase.getName()
                );


        if (normalizedName.isBlank()) {

            return sequence;
        }


        return sequence
                + "_"
                + normalizedName;
    }




    private String extractSequence(
            String id) {

        if (id == null ||
                id.isBlank()) {

            return "TC000";
        }


        int marker =
                id.lastIndexOf(
                        "#TC"
                );


        if (marker >= 0) {

            return id.substring(
                    marker + 1
            );
        }


        /*
         * Fallback in case the Phase 2 ID format
         * ever changes.
         */
        int hash =
                id.lastIndexOf('#');


        if (hash >= 0 &&
                hash < id.length() - 1) {

            return id.substring(
                    hash + 1
            );
        }


        return id;
    }




    private String normalizeName(
            String name) {

        if (name == null ||
                name.isBlank()) {

            return "";
        }


        String result =
                name.trim();


        /*
         * Everything that is inconvenient as a CLI/script
         * argument becomes an underscore.
         */
        result =
                result.replaceAll(
                        "[^a-zA-Z0-9]+",
                        "_"
                );


        /*
         * Avoid multiple consecutive underscores.
         */
        result =
                result.replaceAll(
                        "_+",
                        "_"
                );


        /*
         * Remove leading/trailing underscores.
         */
        result =
                result.replaceAll(
                        "^_+|_+$",
                        ""
                );


        return result;
    }
}