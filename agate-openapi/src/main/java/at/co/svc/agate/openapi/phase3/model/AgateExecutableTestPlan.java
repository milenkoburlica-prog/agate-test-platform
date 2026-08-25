package at.co.svc.agate.openapi.phase3.model;

import java.util.ArrayList;
import java.util.List;


public class AgateExecutableTestPlan {


    private String operationIdentity;


    private List<AgateExecutableTest> tests =
            new ArrayList<>();




    public String getOperationIdentity() {

        return operationIdentity;
    }


    public void setOperationIdentity(
            String operationIdentity) {

        this.operationIdentity =
                operationIdentity;
    }




    public List<AgateExecutableTest> getTests() {

        return tests;
    }


    public void setTests(
            List<AgateExecutableTest> tests) {

        this.tests =
                tests != null
                        ? new ArrayList<>(
                                tests
                        )
                        : new ArrayList<>();
    }




    public void addTest(
            AgateExecutableTest test) {

        if (test == null) {

            return;
        }


        tests.add(
                test
        );
    }




    public int size() {

        return tests.size();
    }
}