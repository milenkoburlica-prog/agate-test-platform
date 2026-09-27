package at.co.svc.agate.server.validation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ValidationService {

    private final ValidationParser parser;
    private final List<AgateValidator> validators;

    public ValidationService(
            ValidationParser parser,
            List<AgateValidator> validators) {

        this.parser =
                parser;

        this.validators =
                List.copyOf(validators);
    }

    public static ValidationService defaultService() {

        return new ValidationService(
                new ValidationParser(),
                List.of(
                        new DocumentStructureValidator(),
                        new EngineTypeValidator(),
                        new RestSoapSchemaValidator(),
                        new BufferSchemaValidator(),
                        new FileSchemaValidator(),
                        new CmdSchemaValidator(),
                        new WaitSchemaValidator(),
                        new LoopSchemaValidator(),
                        new SqlSchemaValidator(),
                        new OcSchemaValidator(),

                        /*
                         * Validate YAML syntax of reusable modules
                         * before attempting data-flow analysis.
                         */
                        new ReusableYamlSyntaxValidator(),
                        new SuspiciousYamlEscapeValidator(),
                        new PlaceholderSyntaxValidator(),

                        new DataFlowValidator()
                )
        );
    }

    public ValidationResult validate(
            Path file) {

        return validate(
                file,
                ValidationOptions.defaults());
    }

    public ValidationResult validate(
            Path file,
            ValidationOptions options) {

        List<ValidationIssue> issues =
                new ArrayList<>();

        ValidationParser.ParsedDocument document;

        /*
         * PHASE 1:
         * YAML parsing.
         *
         * Syntax errors are fail-fast because no reliable
         * document tree exists after parsing fails.
         */
        try {

            document =
                    parser.parse(file);

        } catch (ValidationParser.InvalidYamlException e) {

            issues.add(
                    new ValidationIssue(
                            ValidationSeverity.ERROR,
                            "AGATE-V001",
                            "Invalid YAML: "
                                    + e.getMessage(),
                            file,
                            e.line(),
                            e.column()
                    )
            );

            return new ValidationResult(
                    issues);

        } catch (IOException e) {

            issues.add(
                    new ValidationIssue(
                            ValidationSeverity.ERROR,
                            "AGATE-V002",
                            "Could not read YAML file: "
                                    + e.getMessage(),
                            file,
                            null,
                            null
                    )
            );

            return new ValidationResult(
                    issues);
        }

        ValidationContext context =
                new ValidationContext(
                        file,
                        document.root(),
                        document.source(),
                        options
                );

        /*
         * PHASE 2+:
         *
         * Structural, engine-specific and data-flow
         * validators collect as many errors as possible.
         */
        for (AgateValidator validator :
                validators) {

            try {

                List<ValidationIssue> validatorIssues =
                        validator.validate(
                                context);

                if (validatorIssues != null) {

                    issues.addAll(
                            validatorIssues);
                }

            } catch (RuntimeException e) {

                issues.add(
                        new ValidationIssue(
                                ValidationSeverity.ERROR,
                                "AGATE-V999",
                                "Validator "
                                        + validator.getClass()
                                                .getSimpleName()
                                        + " failed unexpectedly: "
                                        + e.getMessage(),
                                file,
                                null,
                                null
                        )
                );
            }
        }

        return new ValidationResult(
                issues);
    }
}
