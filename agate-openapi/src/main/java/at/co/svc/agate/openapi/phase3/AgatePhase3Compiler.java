package at.co.svc.agate.openapi.phase3;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestCase;
import at.co.svc.agate.openapi.phase2.model.AgateGeneratedTestPlan;

import at.co.svc.agate.openapi.phase3.model.AgateExecutableTest;
import at.co.svc.agate.openapi.phase3.model.AgateExecutableTestPlan;


public class AgatePhase3Compiler {


    private final AgateExecutableTestBuilder testBuilder =
            new AgateExecutableTestBuilder();




    public AgateExecutableTestPlan compile(
            AgateOperationModel operation,
            AgateGeneratedTestPlan generatedPlan) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }


        if (generatedPlan == null) {

            throw new IllegalArgumentException(
                    "Generated test plan must not be null"
            );
        }


        AgateExecutableTestPlan result =
                new AgateExecutableTestPlan();


        result.setOperationIdentity(
                operation.getIdentity()
        );


        for (AgateGeneratedTestCase generatedTest :
                generatedPlan.getTestCases()) {

            AgateExecutableTest executableTest =
                    testBuilder.build(
                            operation,
                            generatedTest
                    );


            result.addTest(
                    executableTest
            );
        }


        return result;
    }
}