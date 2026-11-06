package at.co.svc.agate.core.report.analysis;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResult {

    private String application;

    private String environment;

    private int totalTests;

    private int failedTests;

    private int passedTests;

    private List<FailureCluster> failureClusters =
            new ArrayList<>();

    public String getApplication() {
        return application;
    }

    public void setApplication(
            String application) {

        this.application = application;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(
            String environment) {

        this.environment = environment;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(
            int totalTests) {

        this.totalTests = totalTests;
    }

    public int getFailedTests() {
        return failedTests;
    }

    public void setFailedTests(
            int failedTests) {

        this.failedTests = failedTests;
    }

    public int getPassedTests() {
        return passedTests;
    }

    public void setPassedTests(
            int passedTests) {

        this.passedTests = passedTests;
    }

    public List<FailureCluster> getFailureClusters() {
        return failureClusters;
    }

    public void setFailureClusters(
            List<FailureCluster> failureClusters) {

        this.failureClusters = failureClusters;
    }
}