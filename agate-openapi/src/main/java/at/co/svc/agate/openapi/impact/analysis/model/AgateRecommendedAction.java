package at.co.svc.agate.openapi.impact.analysis.model;


public enum AgateRecommendedAction {

    REVIEW,

    CHANGE_CSV_VALUE,

    ADD_CSV_ROW,

    REMOVE_CSV_ROW,

    REGENERATE_BOUNDARY_VALUE,

    REVIEW_EXPECTED_OUTCOME,

    ADD_TEST_CASE,

    ADD_YAML_FIELD,

    REMOVE_YAML_FIELD,

    CHANGE_YAML_FIELD,

    ADD_REQUEST_JSON_FIELD,

    REMOVE_REQUEST_JSON_FIELD,

    CHANGE_REQUEST_JSON_FIELD
}