package at.co.svc.agate.openapi.impact.analysis;

import at.co.svc.agate.openapi.change.detection.AgateApiChange;
import at.co.svc.agate.openapi.change.detection.AgateChangeType;
import at.co.svc.agate.openapi.change.detection.AgateOpenApiChangeSet;
import at.co.svc.agate.openapi.impact.analysis.artifact.AgateOperationArtifactResolver;
import at.co.svc.agate.openapi.impact.analysis.artifact.AgateOperationArtifactSet;
import at.co.svc.agate.openapi.impact.analysis.artifact.AgateTextArtifactInspector;
import at.co.svc.agate.openapi.impact.analysis.contract.AgateContractSchemaResolver;
import at.co.svc.agate.openapi.impact.analysis.contract.AgateContractSchemaResolver.ResolvedContractField;
import at.co.svc.agate.openapi.impact.analysis.contract.AgateNewContractIndex;
import at.co.svc.agate.openapi.impact.analysis.coverage.AgateCoverageImpactAnalyzer;
import at.co.svc.agate.openapi.impact.analysis.csv.AgateCsvArtifactReader;
import at.co.svc.agate.openapi.impact.analysis.csv.AgateCsvSnapshot;
import at.co.svc.agate.openapi.impact.analysis.intent.AgateTestCaseIntent;
import at.co.svc.agate.openapi.impact.analysis.intent.AgateTestCaseIntent.ConstraintType;
import at.co.svc.agate.openapi.impact.analysis.intent.AgateTestCaseIntent.IntentType;
import at.co.svc.agate.openapi.impact.analysis.intent.AgateTestCaseIntentResolver;
import at.co.svc.agate.openapi.impact.analysis.model.AgateArtifactType;
import at.co.svc.agate.openapi.impact.analysis.model.AgateImpactReport;
import at.co.svc.agate.openapi.impact.analysis.model.AgateRecommendedAction;
import at.co.svc.agate.openapi.impact.analysis.model.AgateTestCaseImpact;
import at.co.svc.agate.openapi.impact.analysis.validation.AgateChangedConstraintValidator;
import at.co.svc.agate.openapi.impact.analysis.validation.AgateChangedConstraintValidator.ValidationResult;
import at.co.svc.agate.openapi.model.AgateOpenApiModel;
import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import java.nio.file.Files;
import java.nio.file.Path;


public class AgateImpactAnalyzer {


    private final AgateCsvArtifactReader csvReader =
            new AgateCsvArtifactReader();


    private final AgateTextArtifactInspector textInspector =
            new AgateTextArtifactInspector();


    private final AgateContractSchemaResolver schemaResolver =
            new AgateContractSchemaResolver();


    private final AgateChangedConstraintValidator changedConstraintValidator =
            new AgateChangedConstraintValidator();


    private final AgateOperationArtifactResolver artifactResolver =
            new AgateOperationArtifactResolver();


    private final AgateTestCaseIntentResolver intentResolver =
            new AgateTestCaseIntentResolver();


    private final AgateCoverageImpactAnalyzer coverageAnalyzer =
            new AgateCoverageImpactAnalyzer();




    public AgateImpactReport analyze(
            AgateOpenApiChangeSet changeSet,
            AgateOpenApiModel newContract,
            Path appDirectory)
            throws Exception {

        if (changeSet == null) {

            throw new IllegalArgumentException(
                    "Change set must not be null"
            );
        }


        if (newContract == null) {

            throw new IllegalArgumentException(
                    "New OpenAPI contract must not be null"
            );
        }


        if (appDirectory == null) {

            throw new IllegalArgumentException(
                    "Application directory must not be null"
            );
        }


        if (!Files.exists(
                appDirectory
        )) {

            throw new IllegalArgumentException(
                    "Application directory does not exist: "
                            + appDirectory
            );
        }


        AgateImpactReport report =
                new AgateImpactReport();


        report.setChangeSet(
                changeSet
        );


        AgateNewContractIndex contractIndex =
                new AgateNewContractIndex(
                        newContract
                );


        /*
         * =====================================================
         * PASS 1
         *
         * Existing artifact / value impact
         * =====================================================
         */

        for (AgateApiChange change :
                changeSet.getChanges()) {

            analyzeChange(
                    change,
                    contractIndex,
                    appDirectory,
                    report
            );
        }


        /*
         * =====================================================
         * PASS 2
         *
         * Coverage impact
         *
         * Is every deterministic requirement of the new
         * contract represented by an existing testcase?
         * =====================================================
         */

        coverageAnalyzer.analyze(
                changeSet,
                newContract,
                appDirectory,
                report
        );


        return report;
    }




    private void analyzeChange(
            AgateApiChange change,
            AgateNewContractIndex contractIndex,
            Path appDirectory,
            AgateImpactReport report)
            throws Exception {

        if ("operation".equals(
                change.getLocation()
        )
                &&
                change.getChangeType()
                        == AgateChangeType.ADDED) {

            return;
        }


        AgateOperationModel operation =
                contractIndex.getOperation(
                        change.getOperationIdentity()
                );


        if (operation == null) {

            return;
        }


        AgateOperationArtifactSet artifacts =
                artifactResolver.resolve(
                        operation,
                        appDirectory
                );


        if (!Files.exists(
                artifacts.getCsvFile()
        )) {

            add(
                    report,
                    change,
                    AgateArtifactType.CSV,
                    artifacts.getCsvFile(),
                    "test suite",
                    null,
                    null,
                    null,
                    AgateRecommendedAction.ADD_CSV_ROW,
                    "Test suite CSV for operation "
                            + operation.getIdentity()
                            + " does not exist"
            );


            return;
        }


        AgateCsvSnapshot csv =
                csvReader.read(
                        artifacts.getCsvFile()
                );


        String yaml =
                textInspector.read(
                        artifacts.getYamlFile()
                );


        String requestJson =
                textInspector.read(
                        artifacts.getRequestJsonFile()
                );


        String field =
                fieldName(
                        change.getLocation()
                );


        if (field == null) {

            return;
        }


        ResolvedContractField newField =
                schemaResolver.resolve(
                        operation,
                        change.getLocation()
                );


        if (change.getChangeType()
                == AgateChangeType.ADDED) {

            analyzeAddedField(
                    change,
                    field,
                    newField,
                    csv,
                    yaml,
                    requestJson,
                    artifacts,
                    report
            );


            return;
        }


        if (change.getChangeType()
                == AgateChangeType.REMOVED) {

            analyzeRemovedField(
                    change,
                    field,
                    csv,
                    yaml,
                    requestJson,
                    artifacts,
                    report
            );


            return;
        }


        analyzeModifiedField(
                change,
                field,
                newField,
                csv,
                yaml,
                requestJson,
                artifacts,
                report
        );
    }




    private void analyzeAddedField(
            AgateApiChange change,
            String field,
            ResolvedContractField newField,
            AgateCsvSnapshot csv,
            String yaml,
            String requestJson,
            AgateOperationArtifactSet artifacts,
            AgateImpactReport report) {

        if (!csv.hasRow(
                field
        )) {

            add(
                    report,
                    change,
                    AgateArtifactType.CSV,
                    artifacts.getCsvFile(),
                    "row: " + field,
                    null,
                    null,
                    null,
                    AgateRecommendedAction.ADD_CSV_ROW,
                    "New API field is missing from current CSV"
            );

        } else if (newField != null) {

            validateCurrentValues(
                    change,
                    field,
                    newField,
                    csv,
                    artifacts.getCsvFile(),
                    report
            );
        }


        if (isBodyField(
                change
        )) {

            if (!textInspector.containsXlReference(
                    yaml,
                    field
            )) {

                add(
                        report,
                        change,
                        AgateArtifactType.YAML,
                        artifacts.getYamlFile(),
                        "REST EXEC: " + field,
                        null,
                        null,
                        null,
                        AgateRecommendedAction.ADD_YAML_FIELD,
                        "New request body field is missing from YAML template"
                );
            }


            if (!textInspector.containsBufferReference(
                    requestJson,
                    field
            )) {

                add(
                        report,
                        change,
                        AgateArtifactType.REQUEST_JSON,
                        artifacts.getRequestJsonFile(),
                        "$." + field,
                        null,
                        null,
                        null,
                        AgateRecommendedAction.ADD_REQUEST_JSON_FIELD,
                        "New request body field is missing from request.json"
                );
            }
        }
    }




    private void analyzeRemovedField(
            AgateApiChange change,
            String field,
            AgateCsvSnapshot csv,
            String yaml,
            String requestJson,
            AgateOperationArtifactSet artifacts,
            AgateImpactReport report) {

        if (csv.hasRow(
                field
        )) {

            add(
                    report,
                    change,
                    AgateArtifactType.CSV,
                    artifacts.getCsvFile(),
                    "row: " + field,
                    null,
                    null,
                    null,
                    AgateRecommendedAction.REMOVE_CSV_ROW,
                    "Removed API field still exists in current CSV"
            );
        }


        if (isBodyField(
                change
        )) {

            if (textInspector.containsXlReference(
                    yaml,
                    field
            )) {

                add(
                        report,
                        change,
                        AgateArtifactType.YAML,
                        artifacts.getYamlFile(),
                        "REST EXEC: " + field,
                        null,
                        null,
                        null,
                        AgateRecommendedAction.REMOVE_YAML_FIELD,
                        "Removed request field still exists in YAML template"
                );
            }


            if (textInspector.containsBufferReference(
                    requestJson,
                    field
            )) {

                add(
                        report,
                        change,
                        AgateArtifactType.REQUEST_JSON,
                        artifacts.getRequestJsonFile(),
                        "$." + field,
                        null,
                        null,
                        null,
                        AgateRecommendedAction.REMOVE_REQUEST_JSON_FIELD,
                        "Removed request field still exists in request.json"
                );
            }
        }
    }




    private void analyzeModifiedField(
            AgateApiChange change,
            String field,
            ResolvedContractField newField,
            AgateCsvSnapshot csv,
            String yaml,
            String requestJson,
            AgateOperationArtifactSet artifacts,
            AgateImpactReport report) {

        if (!csv.hasRow(
                field
        )) {

            add(
                    report,
                    change,
                    AgateArtifactType.CSV,
                    artifacts.getCsvFile(),
                    "row: " + field,
                    null,
                    null,
                    null,
                    AgateRecommendedAction.ADD_CSV_ROW,
                    "Changed API field is missing from current CSV"
            );


            return;
        }


        if (newField != null) {

            validateCurrentValues(
                    change,
                    field,
                    newField,
                    csv,
                    artifacts.getCsvFile(),
                    report
            );
        }


        if (isBodyField(
                change
        )) {

            if (!textInspector.containsXlReference(
                    yaml,
                    field
            )) {

                add(
                        report,
                        change,
                        AgateArtifactType.YAML,
                        artifacts.getYamlFile(),
                        "REST EXEC: " + field,
                        null,
                        null,
                        null,
                        AgateRecommendedAction.ADD_YAML_FIELD,
                        "Changed request field is missing from YAML template"
                );
            }


            if (!textInspector.containsBufferReference(
                    requestJson,
                    field
            )) {

                add(
                        report,
                        change,
                        AgateArtifactType.REQUEST_JSON,
                        artifacts.getRequestJsonFile(),
                        "$." + field,
                        null,
                        null,
                        null,
                        AgateRecommendedAction.ADD_REQUEST_JSON_FIELD,
                        "Changed request field is missing from request.json"
                );
            }


            if ("type".equals(
                    change.getProperty()
            )
                    &&
                    textInspector.containsBufferReference(
                            requestJson,
                            field
                    )) {

                add(
                        report,
                        change,
                        AgateArtifactType.REQUEST_JSON,
                        artifacts.getRequestJsonFile(),
                        "$." + field,
                        null,
                        String.valueOf(
                                change.getOldValue()
                        ),
                        String.valueOf(
                                change.getNewValue()
                        ),
                        AgateRecommendedAction.CHANGE_REQUEST_JSON_FIELD,
                        "Request field type changed; verify quoted/raw placeholder representation"
                );
            }
        }
    }




    private void validateCurrentValues(
            AgateApiChange change,
            String field,
            ResolvedContractField newField,
            AgateCsvSnapshot csv,
            Path csvFile,
            AgateImpactReport report) {

        ConstraintType changedConstraint =
                intentResolver.resolveConstraint(
                        change.getProperty()
                );


        for (int i = 0;
             i < csv.getTestcaseCount();
             i++) {

            String testcase =
                    csv.getTestcaseName(
                            i
                    );


            String current =
                    csv.getValue(
                            field,
                            i
                    );


            AgateTestCaseIntent intent =
                    intentResolver.resolve(
                            testcase
                    );


            if (intent.targetsField(
                    field
            )) {

                if (!intent.targetsConstraint(
                        changedConstraint
                )) {

                    continue;
                }


                if (intent.isNegativeTest()) {

                    continue;
                }
            }


            ValidationResult validation =
                    changedConstraintValidator.validate(
                            change,
                            newField.getSchema(),
                            current
                    );


            if (validation.isValid()) {

                continue;
            }


            add(
                    report,
                    change,
                    AgateArtifactType.CSV,
                    csvFile,
                    "row: " + field,
                    testcase,
                    current,
                    String.valueOf(
                            change.getNewValue()
                    ),
                    determineAction(
                            intent,
                            field,
                            changedConstraint
                    ),
                    validation.getReason()
            );
        }
    }




    private AgateRecommendedAction determineAction(
            AgateTestCaseIntent intent,
            String changedField,
            ConstraintType changedConstraint) {

        if (intent == null) {

            return AgateRecommendedAction
                    .CHANGE_CSV_VALUE;
        }


        if (intent.isGeneralPositiveTest()) {

            return AgateRecommendedAction
                    .CHANGE_CSV_VALUE;
        }


        if (intent.targetsField(
                changedField
        )
                &&
                intent.targetsConstraint(
                        changedConstraint
                )) {

            if (intent.isBoundaryTest()) {

                return AgateRecommendedAction
                        .REGENERATE_BOUNDARY_VALUE;
            }


            if (intent.getIntentType()
                    == IntentType.ENUM_VALID) {

                return AgateRecommendedAction
                        .REVIEW_EXPECTED_OUTCOME;
            }
        }


        return AgateRecommendedAction
                .CHANGE_CSV_VALUE;
    }




    private String fieldName(
            String location) {

        if (location == null) {

            return null;
        }


        if (location.startsWith(
                "request.body."
        )) {

            return location.substring(
                    "request.body."
                            .length()
            );
        }


        int index =
                location.lastIndexOf(
                        '.'
                );


        return index >= 0
                ? location.substring(
                        index + 1
                )
                : location;
    }




    private boolean isBodyField(
            AgateApiChange change) {

        return change.getLocation() != null
                &&
                change
                        .getLocation()
                        .startsWith(
                                "request.body."
                        );
    }




    private void add(
            AgateImpactReport report,
            AgateApiChange change,
            AgateArtifactType artifactType,
            Path artifact,
            String artifactLocation,
            String testcase,
            String currentValue,
            String expectedValue,
            AgateRecommendedAction action,
            String reason) {

        AgateTestCaseImpact impact =
                new AgateTestCaseImpact();


        impact.setOperationIdentity(
                change.getOperationIdentity()
        );


        impact.setChangeLocation(
                change.getLocation()
        );


        impact.setChangeProperty(
                change.getProperty()
        );


        impact.setArtifactType(
                artifactType
        );


        impact.setArtifact(
                artifact
        );


        impact.setArtifactLocation(
                artifactLocation
        );


        impact.setTestcaseName(
                testcase
        );


        impact.setCurrentValue(
                currentValue
        );


        impact.setExpectedValue(
                expectedValue
        );


        impact.setAction(
                action
        );


        impact.setReason(
                reason
        );


        report.addImpact(
                impact
        );
    }
}