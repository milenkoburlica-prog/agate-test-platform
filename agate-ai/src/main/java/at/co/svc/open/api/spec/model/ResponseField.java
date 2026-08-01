package at.co.svc.open.api.spec.model;


import io.swagger.v3.oas.models.media.Schema;



public class ResponseField {


    private String path;

    private String type;

    private String format;

    private boolean nullable;



    public ResponseField(
            String path,
            Schema<?> schema) {


        this.path = path;


        if(schema != null) {

            this.type =
                    schema.getType();

            this.format =
                    schema.getFormat();

            this.nullable =
                    Boolean.TRUE.equals(
                            schema.getNullable()
                    );
        }

    }





    public String getPath() {
        return path;
    }




    public void setPath(String path) {
        this.path = path;
    }





    public String getType() {
        return type;
    }




    public void setType(String type) {
        this.type = type;
    }





    public String getFormat() {
        return format;
    }




    public void setFormat(String format) {
        this.format = format;
    }





    public boolean isNullable() {
        return nullable;
    }




    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }





    @Override
    public String toString() {

        return path;
    }

}