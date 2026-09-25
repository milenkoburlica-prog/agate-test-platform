package at.co.svc.agate.core.dsl.register;

import java.util.Map;

import at.co.svc.agate.core.dsl.model.Constraint;
import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.interfaces.TestLogger;

public class PrintDslStepContext {

    public static void logDslStepContext(
            TestLogger logger,
            TestStep step) {

        logger.log("");

        /*
         * ---------------------------------------------------------
         * 1. Original YAML verwenden, wenn er wirklich gültig ist
         * ---------------------------------------------------------
         */
        String originalYaml =
                step.getTextYaml();

        if (isValidOriginalYaml(originalYaml)) {

            String[] lines =
                    originalYaml.split("\\R");

            for (String line : lines) {

                if (!line.trim().isEmpty()) {

                    logger.log(
                            ConsoleColors.BLUE
                                    + ">>> DSL"
                                    + ConsoleColors.RESET
                                    + "      "
                                    + line
                    );
                }
            }

            return;
        }

        /*
         * ---------------------------------------------------------
         * 2. Fallback aus dem bereits geparsten TestStep
         * ---------------------------------------------------------
         *
         * Wichtig:
         *
         * Eine interne YAML-Extraktionsfehlermeldung wie
         *
         *   # Error: Test Case ...
         *
         * darf niemals als DSL ausgegeben werden.
         */
        try {

            logLine(
                    logger,
                    "- type: " + step.getType()
            );

            if (notBlank(step.getCondition())) {

                logLine(
                        logger,
                        "  condition: \""
                                + step.getCondition()
                                + "\""
                );
            }

            /*
             * -----------------------------------------------------
             * CALL
             * -----------------------------------------------------
             */
            if (step.getType() == StepType.CALL) {

                if (notBlank(step.getCommand())) {

                    logLine(
                            logger,
                            "  command: "
                                    + quote(step.getCommand())
                    );
                }

                logParameters(
                        logger,
                        step
                );

                return;
            }

            /*
             * -----------------------------------------------------
             * BUFFER
             * -----------------------------------------------------
             */
            if (step.getType() == StepType.BUFFER) {

                if (notBlank(step.getOp())) {

                    logLine(
                            logger,
                            "  op: "
                                    + step.getOp()
                    );
                }

                if (notBlank(step.getName())) {

                    logLine(
                            logger,
                            "  name: "
                                    + step.getName()
                    );
                }

                if (step.getValue() != null) {

                    logLine(
                            logger,
                            "  value: "
                                    + formatValue(
                                            step.getValue()
                                    )
                    );
                }

                return;
            }

            /*
             * -----------------------------------------------------
             * REST / SOAP / SQL / CMD / FILE / OC / ...
             * -----------------------------------------------------
             */
            if (notBlank(step.getOp())) {

                logLine(
                        logger,
                        "  op: "
                                + step.getOp()
                );
            }

            if (notBlank(step.getCommand())) {

                logLine(
                        logger,
                        "  command: "
                                + quote(step.getCommand())
                );
            }

            if (notBlank(step.getEndpoint())) {

                logLine(
                        logger,
                        "  endpoint: "
                                + quote(step.getEndpoint())
                );
            }

            if (notBlank(step.getUrl())) {

                logLine(
                        logger,
                        "  url: "
                                + quote(step.getUrl())
                );
            }

            if (notBlank(step.getSource())) {

                logLine(
                        logger,
                        "  source: "
                                + step.getSource()
                );
            }

            if (notBlank(step.getPath())) {

                logLine(
                        logger,
                        "  path: "
                                + quote(step.getPath())
                );
            }

            if (notBlank(step.getAction())) {

                logLine(
                        logger,
                        "  action: "
                                + step.getAction()
                );
            }

            if (step.getExpected() != null) {

                logLine(
                        logger,
                        "  expected: "
                                + formatValue(
                                        step.getExpected()
                                )
                );
            }

            if (notBlank(step.getResponse())) {

                logLine(
                        logger,
                        "  response: "
                                + step.getResponse()
                );
            }

            logParameters(
                    logger,
                    step
            );

            /*
             * -----------------------------------------------------
             * Constraints
             * -----------------------------------------------------
             */
            if (step.getConstraints() != null
                    && !step.getConstraints().isEmpty()) {

                logLine(
                        logger,
                        "  constraints:"
                );

                for (Constraint c :
                        step.getConstraints()) {

                    logLine(
                            logger,
                            "    - path: "
                                    + c.getPath()
                    );

                    logLine(
                            logger,
                            "      action: "
                                    + c.getAction()
                    );

                    logLine(
                            logger,
                            "      expected: "
                                    + formatValue(
                                            c.getExpected()
                                    )
                    );
                }
            }

        } catch (Exception e) {

            logger.log(
                    ConsoleColors.BLUE
                            + ">>> DSL"
                            + ConsoleColors.RESET
                            + "      [Unable to display DSL step: "
                            + e.getMessage()
                            + "]"
            );
        }
    }

    private static boolean isValidOriginalYaml(
            String yaml) {

        if (yaml == null
                || yaml.isBlank()) {

            return false;
        }

        String normalized =
                yaml.trim();

        /*
         * Internal extraction errors must trigger fallback.
         */
        return !normalized.startsWith("# Error:")
                && !normalized.startsWith("Error extracting YAML")
                && !normalized.startsWith("# Greška:")
                && !normalized.startsWith("Greška:");
    }

    private static void logParameters(
            TestLogger logger,
            TestStep step) {

        if (step.getParameters() == null
                || step.getParameters().isEmpty()) {

            return;
        }

        logLine(
                logger,
                "  parameters:"
        );

        for (Map.Entry<String, Object> param :
                step.getParameters().entrySet()) {

            logLine(
                    logger,
                    "    "
                            + param.getKey()
                            + ": "
                            + formatValue(
                                    param.getValue()
                            )
            );
        }
    }

    private static void logLine(
            TestLogger logger,
            String text) {

        logger.log(
                ConsoleColors.BLUE
                        + ">>> DSL"
                        + ConsoleColors.RESET
                        + "      "
                        + text
        );
    }

    private static String formatValue(
            Object value) {

        if (value == null) {

            return "null";
        }

        if (value instanceof String) {

            return quote(
                    value.toString()
            );
        }

        return String.valueOf(
                value
        );
    }

    private static String quote(
            String value) {

        if (value == null) {

            return "\"\"";
        }

        return "\""
                + value.replace(
                        "\"",
                        "\\\""
                )
                + "\"";
    }

    private static boolean notBlank(
            String value) {

        return value != null
                && !value.isBlank();
    }
}