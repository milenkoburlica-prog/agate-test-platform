package at.co.svc.agate.openapi.phase3.csv;

import at.co.svc.agate.openapi.model.AgateSchema;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestBodyModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestContentModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestModel;
import at.co.svc.agate.openapi.phase1.model.AgateRequestParameterModel;

import at.co.svc.agate.openapi.phase3.csv.model.AgateCsvRow;
import at.co.svc.agate.openapi.phase3.csv.model.AgateCsvTable;

import at.co.svc.agate.openapi.phase3.model.AgateExecutableRequest;
import at.co.svc.agate.openapi.phase3.model.AgateExecutableTest;
import at.co.svc.agate.openapi.phase3.model.AgateExecutableTestPlan;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AgateCsvGenerator {


    private final AgateRequestFlattener valueFlattener =
            new AgateRequestFlattener();


    private final AgateRequestSchemaFlattener schemaFlattener =
            new AgateRequestSchemaFlattener();


    private final AgateCsvValueFormatter formatter =
            new AgateCsvValueFormatter();




    public AgateCsvTable generate(
            AgateOperationModel operation,
            AgateExecutableTestPlan plan) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }


        if (plan == null) {

            throw new IllegalArgumentException(
                    "Executable test plan must not be null"
            );
        }


        AgateCsvTable result =
                new AgateCsvTable();


        result.setOperationIdentity(
                plan.getOperationIdentity()
        );


        addMetadataRows(
                result,
                plan
        );


        Set<String> dataKeys =
                collectSchemaKeys(
                        operation
                );


        /*
         * Safety net:
         *
         * If Phase 2 ever creates a value that is not present
         * in the collected schema keys, we still keep it.
         */

        dataKeys.addAll(
                collectValueKeys(
                        plan
                )
        );


        for (String key :
                dataKeys) {

            result.addRow(
                    createDataRow(
                            key,
                            plan
                    )
            );
        }


        return result;
    }




    private void addMetadataRows(
            AgateCsvTable table,
            AgateExecutableTestPlan plan) {

        table.addRow(
                createTestcaseNameRow(
                        plan
                )
        );


        table.addRow(
                createDescriptionRow(
                        plan
                )
        );


        table.addRow(
                createEndpointRow(
                        plan
                )
        );


        table.addRow(
                createStatusCodeRow(
                        plan
                )
        );
    }




    private AgateCsvRow createTestcaseNameRow(
            AgateExecutableTestPlan plan) {

        AgateCsvRow row =
                new AgateCsvRow(
                        AgateCsvConstants.ROW_TESTCASE_NAME
                );


        for (AgateExecutableTest test :
                plan.getTests()) {

            row.addValue(
                    test.getTechnicalName()
            );
        }


        return row;
    }




    private AgateCsvRow createDescriptionRow(
            AgateExecutableTestPlan plan) {

        AgateCsvRow row =
                new AgateCsvRow(
                        AgateCsvConstants.ROW_TESTCASE_DESCRIPTION
                );


        for (AgateExecutableTest test :
                plan.getTests()) {

            row.addValue(
                    formatter.format(
                            test.getName()
                    )
            );
        }


        return row;
    }




    private AgateCsvRow createEndpointRow(
            AgateExecutableTestPlan plan) {

        AgateCsvRow row =
                new AgateCsvRow(
                        AgateCsvConstants.ROW_API_ENDPOINT
                );


        for (AgateExecutableTest test :
                plan.getTests()) {

            row.addValue(
                    formatter.format(
                            test
                                    .getRequest()
                                    .getPathTemplate()
                    )
            );
        }


        return row;
    }




    private AgateCsvRow createStatusCodeRow(
            AgateExecutableTestPlan plan) {

        AgateCsvRow row =
                new AgateCsvRow(
                        AgateCsvConstants.ROW_STATUS_CODE
                );


        for (AgateExecutableTest test :
                plan.getTests()) {

            row.addValue(
                    formatter.format(
                            test
                                    .getExpectation()
                                    .getExpectedStatusCode()
                    )
            );
        }


        return row;
    }




    /*
     * =====================================================
     * SCHEMA KEYS
     * =====================================================
     */


    private Set<String> collectSchemaKeys(
            AgateOperationModel operation) {

        Set<String> result =
                new LinkedHashSet<>();


        AgateRequestModel request =
                operation.getRequest();


        if (request == null) {

            return result;
        }


        addParameterNames(
                result,
                request.getPathParameters()
        );


        addParameterNames(
                result,
                request.getQueryParameters()
        );


        addParameterNames(
                result,
                request.getHeaderParameters()
        );


        addParameterNames(
                result,
                request.getCookieParameters()
        );


        AgateSchema bodySchema =
                selectBodySchema(
                        request.getBody()
                );


        if (bodySchema != null) {

            result.addAll(
                    schemaFlattener.collectKeys(
                            bodySchema
                    )
            );
        }


        return result;
    }




    private void addParameterNames(
            Set<String> target,
            List<AgateRequestParameterModel> parameters) {

        if (parameters == null) {

            return;
        }


        for (AgateRequestParameterModel parameter :
                parameters) {

            target.add(
                    parameter.getName()
            );
        }
    }




    private AgateSchema selectBodySchema(
            AgateRequestBodyModel body) {

        if (body == null ||
                body.getContents() == null ||
                body.getContents().isEmpty()) {

            return null;
        }


        AgateRequestContentModel content =
                body
                        .getContents()
                        .stream()
                        .filter(value ->
                                "application/json".equals(
                                        value.getMediaType()
                                )
                        )
                        .findFirst()
                        .orElse(
                                body
                                        .getContents()
                                        .get(0)
                        );


        return content.getSchema();
    }




    /*
     * =====================================================
     * VALUE KEYS
     * =====================================================
     */


    private Set<String> collectValueKeys(
            AgateExecutableTestPlan plan) {

        Set<String> result =
                new LinkedHashSet<>();


        for (AgateExecutableTest test :
                plan.getTests()) {

            result.addAll(
                    collectTestValues(
                            test
                    )
                            .keySet()
            );
        }


        return result;
    }




    private AgateCsvRow createDataRow(
            String key,
            AgateExecutableTestPlan plan) {

        AgateCsvRow row =
                new AgateCsvRow(
                        key
                );


        for (AgateExecutableTest test :
                plan.getTests()) {

            Map<String, Object> values =
                    collectTestValues(
                            test
                    );


            Object value =
                    values.get(
                            key
                    );


            row.addValue(
                    formatter.format(
                            value
                    )
            );
        }


        return row;
    }




    private Map<String, Object> collectTestValues(
            AgateExecutableTest test) {

        Map<String, Object> result =
                new LinkedHashMap<>();


        AgateExecutableRequest request =
                test.getRequest();


        putAll(
                result,
                request.getPathParameters()
        );


        putAll(
                result,
                request.getQueryParameters()
        );


        putAll(
                result,
                request.getHeaders()
        );


        putAll(
                result,
                request.getCookies()
        );


        Map<String, Object> bodyValues =
                valueFlattener.flattenBody(
                        request.getBody()
                );


        putAll(
                result,
                bodyValues
        );


        return result;
    }




    private void putAll(
            Map<String, Object> target,
            Map<String, Object> source) {

        if (source == null) {

            return;
        }


        source.forEach(
                target::putIfAbsent
        );
    }
}