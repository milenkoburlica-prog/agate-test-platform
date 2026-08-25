package at.co.svc.agate.openapi.phase3.csv.model;

import java.util.ArrayList;
import java.util.List;


public class AgateCsvRow {


    private String name;


    private List<String> values =
            new ArrayList<>();




    public AgateCsvRow() {
    }




    public AgateCsvRow(
            String name) {

        this.name = name;
    }




    public String getName() {

        return name;
    }


    public void setName(
            String name) {

        this.name = name;
    }




    public List<String> getValues() {

        return values;
    }


    public void setValues(
            List<String> values) {

        this.values =
                values != null
                        ? new ArrayList<>(
                                values
                        )
                        : new ArrayList<>();
    }




    public void addValue(
            String value) {

        values.add(
                value
        );
    }
}