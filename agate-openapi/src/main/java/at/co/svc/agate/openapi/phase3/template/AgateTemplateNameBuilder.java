package at.co.svc.agate.openapi.phase3.template;


public class AgateTemplateNameBuilder {


    public String buildBaseName(
            String method,
            String path) {

        if (method == null ||
                method.isBlank()) {

            throw new IllegalArgumentException(
                    "Method must not be blank"
            );
        }


        if (path == null ||
                path.isBlank()) {

            throw new IllegalArgumentException(
                    "Path must not be blank"
            );
        }


        String normalizedMethod =
                normalizePart(
                        method
                );


        String normalizedPath =
                normalizePart(
                        path
                );


        String result;


        if (normalizedPath.isBlank()) {

            result =
                    normalizedMethod
                            + "_root";

        } else {

            result =
                    normalizedMethod
                            + "_"
                            + normalizedPath;
        }


        /*
         * Final safety normalization.
         *
         * No:
         *
         * __
         *
         * and no leading/trailing underscore.
         */

        result =
                collapseUnderscores(
                        result
                );


        result =
                trimUnderscores(
                        result
                );


        return result;
    }




    public String buildRestCommand(
            String method,
            String path) {

        return "rest."
                + buildBaseName(
                        method,
                        path
                );
    }




    private String normalizePart(
            String value) {

        if (value == null) {

            return "";
        }


        String result =
                value.trim();


        /*
         * Every character which is inconvenient for:
         *
         * - file names
         * - directory names
         * - AGATE command names
         *
         * becomes an underscore.
         *
         * Examples:
         *
         * /pet/{petId}
         *
         * ->
         *
         * _pet__petId_
         */

        result =
                result.replaceAll(
                        "[^a-zA-Z0-9]+",
                        "_"
                );


        /*
         * _pet__petId_
         *
         * ->
         *
         * _pet_petId_
         */

        result =
                collapseUnderscores(
                        result
                );


        /*
         * _pet_petId_
         *
         * ->
         *
         * pet_petId
         */

        result =
                trimUnderscores(
                        result
                );


        return result;
    }




    private String collapseUnderscores(
            String value) {

        if (value == null) {

            return "";
        }


        return value.replaceAll(
                "_+",
                "_"
        );
    }




    private String trimUnderscores(
            String value) {

        if (value == null) {

            return "";
        }


        return value.replaceAll(
                "^_+|_+$",
                ""
        );
    }
}