package at.co.svc.agate.openapi.phase2.model;

public class AgatePhase2Options {

    private boolean generateNegativeTests =
            true;

    private boolean generateOptionalParameterTests =
            true;

    private boolean generateEnumTests =
            true;

    private boolean generateBoundaryTests =
            true;

    private int maximumEnumCases =
            20;


    public boolean isGenerateNegativeTests() {
        return generateNegativeTests;
    }

    public void setGenerateNegativeTests(
            boolean generateNegativeTests) {

        this.generateNegativeTests =
                generateNegativeTests;
    }


    public boolean isGenerateOptionalParameterTests() {
        return generateOptionalParameterTests;
    }

    public void setGenerateOptionalParameterTests(
            boolean generateOptionalParameterTests) {

        this.generateOptionalParameterTests =
                generateOptionalParameterTests;
    }


    public boolean isGenerateEnumTests() {
        return generateEnumTests;
    }

    public void setGenerateEnumTests(
            boolean generateEnumTests) {

        this.generateEnumTests =
                generateEnumTests;
    }


    public boolean isGenerateBoundaryTests() {
        return generateBoundaryTests;
    }

    public void setGenerateBoundaryTests(
            boolean generateBoundaryTests) {

        this.generateBoundaryTests =
                generateBoundaryTests;
    }


    public int getMaximumEnumCases() {
        return maximumEnumCases;
    }

    public void setMaximumEnumCases(
            int maximumEnumCases) {

        this.maximumEnumCases =
                maximumEnumCases;
    }
}