package at.co.svc.agate.openapi.impact.analysis.csv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class AgateCsvSnapshot {


    private final List<String> testcaseNames =
            new ArrayList<>();


    private final Map<String, List<String>> rows =
            new LinkedHashMap<>();




    public void setTestcaseNames(
            List<String> values) {

        testcaseNames.clear();


        if (values != null) {

            testcaseNames.addAll(
                    values
            );
        }
    }




    public List<String> getTestcaseNames() {

        return new ArrayList<>(
                testcaseNames
        );
    }




    public void putRow(
            String name,
            List<String> values) {

        rows.put(
                name,
                values != null
                        ? new ArrayList<>(
                                values
                        )
                        : new ArrayList<>()
        );
    }




    public boolean hasRow(
            String name) {

        return rows.containsKey(
                name
        );
    }




    public List<String> getRow(
            String name) {

        List<String> result =
                rows.get(
                        name
                );


        return result != null
                ? new ArrayList<>(
                        result
                )
                : null;
    }




    public String getValue(
            String row,
            int testcaseIndex) {

        List<String> values =
                rows.get(
                        row
                );


        if (values == null) {

            return null;
        }


        if (testcaseIndex < 0 ||
                testcaseIndex >= values.size()) {

            return null;
        }


        return values.get(
                testcaseIndex
        );
    }




    public String getTestcaseName(
            int index) {

        if (index < 0 ||
                index >= testcaseNames.size()) {

            return null;
        }


        return testcaseNames.get(
                index
        );
    }




    public int getTestcaseCount() {

        return testcaseNames.size();
    }




    public boolean isPositiveTest(
            int index) {

        String status =
                getValue(
                        "statusCode",
                        index
                );


        return status != null
                &&
                !status.isBlank()
                &&
                !isNullValue(
                        status
                );
    }




    public static boolean isNullValue(
            String value) {

        if (value == null) {

            return true;
        }


        String normalized =
                value.trim();


        return normalized.isEmpty()
                ||
                "{NULL}".equalsIgnoreCase(
                        normalized
                );
    }
}