package at.co.svc.agate.openapi.impact.analysis.artifact;

import at.co.svc.agate.openapi.phase1.model.AgateOperationModel;

import at.co.svc.agate.openapi.phase3.template.AgateTemplateNameBuilder;

import java.nio.file.Files;
import java.nio.file.Path;


public class AgateOperationArtifactResolver {


    private final AgateTemplateNameBuilder nameBuilder =
            new AgateTemplateNameBuilder();




    public AgateOperationArtifactSet resolve(
            AgateOperationModel operation,
            Path appDirectory) {

        if (operation == null) {

            throw new IllegalArgumentException(
                    "Operation must not be null"
            );
        }


        if (appDirectory == null) {

            throw new IllegalArgumentException(
                    "Application directory must not be null"
            );
        }


        String baseName =
                nameBuilder.buildBaseName(
                        operation.getMethod(),
                        operation.getPath()
                );


        Path templateDirectory =
                appDirectory.resolve(
                        "template"
                );


        Path restDirectory =
                appDirectory
                        .resolve(
                                "modules"
                        )
                        .resolve(
                                "rest"
                        );


        Path csv =
                templateDirectory.resolve(
                        baseName
                                + ".csv"
                );


        Path yaml =
                templateDirectory.resolve(
                        baseName
                                + ".yaml"
                );


        Path module =
                restDirectory.resolve(
                        baseName
                );


        /*
         * Compatibility fallback.
         *
         * Older generated applications may still use:
         *
         * POST_user
         *
         * instead of:
         *
         * post_user
         *
         * This allows the impact analyzer to work with
         * already existing tester projects.
         */

        if (!Files.exists(
                csv
        )) {

            String legacyName =
                    buildLegacyName(
                            operation
                    );


            Path legacyCsv =
                    templateDirectory.resolve(
                            legacyName
                                    + ".csv"
                    );


            if (Files.exists(
                    legacyCsv
            )) {

                baseName =
                        legacyName;


                csv =
                        legacyCsv;


                yaml =
                        templateDirectory.resolve(
                                legacyName
                                        + ".yaml"
                        );


                module =
                        restDirectory.resolve(
                                legacyName
                        );
            }
        }


        return new AgateOperationArtifactSet(
                operation.getIdentity(),
                baseName,
                csv,
                yaml,
                module,
                module.resolve(
                        "request.json"
                ),
                module.resolve(
                        "metdata.json"
                )
        );
    }




    private String buildLegacyName(
            AgateOperationModel operation) {

        String method =
                operation
                        .getMethod()
                        .toUpperCase();


        String path =
                operation
                        .getPath()
                        .replaceAll(
                                "[^a-zA-Z0-9]+",
                                "_"
                        )
                        .replaceAll(
                                "_+",
                                "_"
                        )
                        .replaceAll(
                                "^_+|_+$",
                                ""
                        );


        if (path.isBlank()) {

            path =
                    "root";
        }


        return method
                + "_"
                + path;
    }
}