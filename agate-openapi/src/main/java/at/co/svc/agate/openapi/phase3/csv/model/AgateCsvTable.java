package at.co.svc.agate.openapi.phase3.csv.model;

import java.util.ArrayList;
import java.util.List;


public class AgateCsvTable {


    private String operationIdentity;


    private List<AgateCsvRow> rows =
            new ArrayList<>();




    public String getOperationIdentity() {

        return operationIdentity;
    }


    public void setOperationIdentity(
            String operationIdentity) {

        this.operationIdentity =
                operationIdentity;
    }




    public List<AgateCsvRow> getRows() {

        return rows;
    }


    public void setRows(
            List<AgateCsvRow> rows) {

        this.rows =
                rows != null
                        ? new ArrayList<>(
                                rows
                        )
                        : new ArrayList<>();
    }




    public void addRow(
            AgateCsvRow row) {

        if (row == null) {

            return;
        }


        rows.add(
                row
        );
    }




    public int rowCount() {

        return rows.size();
    }
}