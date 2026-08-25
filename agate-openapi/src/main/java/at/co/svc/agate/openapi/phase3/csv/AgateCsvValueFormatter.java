package at.co.svc.agate.openapi.phase3.csv;

import java.util.List;


public class AgateCsvValueFormatter {


    public String format(
            Object value) {

        if (value == null) {

            return AgateCsvConstants.NULL_VALUE;
        }


        if (value instanceof String stringValue) {

            if (stringValue.isEmpty()) {

                return AgateCsvConstants.EMPTY_VALUE;
            }


            return stringValue;
        }


        if (value instanceof List<?> list) {

            return formatList(
                    list
            );
        }


        return String.valueOf(
                value
        );
    }




    private String formatList(
            List<?> list) {

        StringBuilder result =
                new StringBuilder();


        result.append(
                "["
        );


        for (int i = 0;
             i < list.size();
             i++) {

            if (i > 0) {

                result.append(
                        ","
                );
            }


            Object value =
                    list.get(i);


            if (value != null) {

                result.append(
                        value
                );
            }
        }


        result.append(
                "]"
        );


        return result.toString();
    }
}