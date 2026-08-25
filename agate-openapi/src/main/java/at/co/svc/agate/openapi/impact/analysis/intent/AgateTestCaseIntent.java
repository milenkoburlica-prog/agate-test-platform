package at.co.svc.agate.openapi.impact.analysis.intent;


public class AgateTestCaseIntent {


    private final String testcaseName;

    private final String field;

    private final ConstraintType constraintType;

    private final IntentType intentType;




    public AgateTestCaseIntent(
            String testcaseName,
            String field,
            ConstraintType constraintType,
            IntentType intentType) {

        this.testcaseName =
                testcaseName;


        this.field =
                field;


        this.constraintType =
                constraintType;


        this.intentType =
                intentType;
    }




    public String getTestcaseName() {

        return testcaseName;
    }




    public String getField() {

        return field;
    }




    public ConstraintType getConstraintType() {

        return constraintType;
    }




    public IntentType getIntentType() {

        return intentType;
    }




    public boolean targetsField(
            String field) {

        if (this.field == null ||
                field == null) {

            return false;
        }


        return normalize(
                this.field
        ).equals(
                normalize(
                        field
                )
        );
    }




    public boolean targetsConstraint(
            ConstraintType constraintType) {

        return this.constraintType
                == constraintType;
    }




    public boolean isGeneralPositiveTest() {

        return intentType
                == IntentType.BASELINE
                ||
                intentType
                        == IntentType.FULL_REQUEST;
    }




    public boolean isNegativeTest() {

        return intentType
                == IntentType.INVALID;
    }




    public boolean isBoundaryTest() {

        return intentType
                == IntentType.BOUNDARY_VALID
                ||
                intentType
                        == IntentType.BOUNDARY_INVALID;
    }




    private String normalize(
            String value) {

        return value
                .trim()
                .toLowerCase()
                .replace(
                        '.',
                        '_'
                )
                .replace(
                        '-',
                        '_'
                );
    }




    public enum ConstraintType {

        NONE,

        REQUIRED,

        MINIMUM,

        MAXIMUM,

        MIN_LENGTH,

        MAX_LENGTH,

        MIN_ITEMS,

        MAX_ITEMS,

        ENUM,

        PATTERN,

        TYPE,

        FORMAT,

        UNKNOWN
    }




    public enum IntentType {

        BASELINE,

        FULL_REQUEST,

        OPTIONAL_PARAMETER,

        BOUNDARY_VALID,

        BOUNDARY_INVALID,

        ENUM_VALID,

        INVALID,

        OTHER
    }
}