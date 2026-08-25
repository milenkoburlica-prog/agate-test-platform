package at.co.svc.agate.openapi.phase2.model;

import java.util.ArrayList;
import java.util.List;

public class AgateGeneratedTestPlan {

    private String operationIdentity;

    private List<AgateGeneratedTestCase> testCases =
            new ArrayList<>();


    public String getOperationIdentity() {
        return operationIdentity;
    }

    public void setOperationIdentity(
            String operationIdentity) {

        this.operationIdentity =
                operationIdentity;
    }


    public List<AgateGeneratedTestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(
            List<AgateGeneratedTestCase> testCases) {

        this.testCases =
                testCases != null
                        ? testCases
                        : new ArrayList<>();
    }


    public void addTestCase(
            AgateGeneratedTestCase testCase) {

        if (testCase != null) {

            testCases.add(
                    testCase
            );
        }
    }


    public int size() {

        return testCases.size();
    }
}