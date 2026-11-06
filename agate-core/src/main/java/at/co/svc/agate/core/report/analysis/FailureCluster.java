package at.co.svc.agate.core.report.analysis;

import java.util.ArrayList;
import java.util.List;

public class FailureCluster {

    private FailureSignature signature;

    private int occurrences;

    private int affectedTests;

    private List<String> testCases =
            new ArrayList<>();

    private String reusableStep;

    private String hint;

    private String assessment;

    private String suggestedFocus;

    public FailureSignature getSignature() {
        return signature;
    }

    public void setSignature(
            FailureSignature signature) {

        this.signature = signature;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(
            int occurrences) {

        this.occurrences = occurrences;
    }

    public int getAffectedTests() {
        return affectedTests;
    }

    public void setAffectedTests(
            int affectedTests) {

        this.affectedTests = affectedTests;
    }

    public List<String> getTestCases() {
        return testCases;
    }

    public void setTestCases(
            List<String> testCases) {

        this.testCases = testCases;
    }

    public String getReusableStep() {
        return reusableStep;
    }

    public void setReusableStep(
            String reusableStep) {

        this.reusableStep = reusableStep;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(
            String hint) {

        this.hint = hint;
    }

    public String getAssessment() {
        return assessment;
    }

    public void setAssessment(
            String assessment) {

        this.assessment = assessment;
    }

    public String getSuggestedFocus() {
        return suggestedFocus;
    }

    public void setSuggestedFocus(
            String suggestedFocus) {

        this.suggestedFocus = suggestedFocus;
    }
}