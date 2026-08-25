package at.co.svc.agate.openapi.phase3.csv;

import at.co.svc.agate.openapi.phase3.csv.model.AgateCsvRow;
import at.co.svc.agate.openapi.phase3.csv.model.AgateCsvTable;


public class AgateCsvSerializer {


    public String serialize(
            AgateCsvTable table) {

        if (table == null) {

            throw new IllegalArgumentException(
                    "CSV table must not be null"
            );
        }


        StringBuilder result =
                new StringBuilder();


        for (AgateCsvRow row :
                table.getRows()) {

            appendCell(
                    result,
                    row.getName()
            );


            for (String value :
                    row.getValues()) {

                result.append(
                        ";"
                );


                appendCell(
                        result,
                        value
                );
            }


            result.append(
                    System.lineSeparator()
            );
        }


        return result.toString();
    }




    private void appendCell(
            StringBuilder target,
            String value) {

        String effective =
                value != null
                        ? value
                        : AgateCsvConstants.NULL_VALUE;


        boolean quote =
                effective.contains(";")
                        ||
                        effective.contains("\"")
                        ||
                        effective.contains("\n")
                        ||
                        effective.contains("\r");


        if (!quote) {

            target.append(
                    effective
            );

            return;
        }


        target.append(
                "\""
        );


        target.append(
                effective.replace(
                        "\"",
                        "\"\""
                )
        );


        target.append(
                "\""
        );
    }
}