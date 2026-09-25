package at.co.svc.aga.transformator.utils;

import java.util.Map;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.Constraint;
import at.co.svc.aga.transformator.dto.StepValueDetails;

public class ProcessDbStep {

    public static void processDbStep(
            CleanStep step,
            String ident) {

        Map<String, StepValueDetails> values =
                step.getValuesAsMap();

        if (values == null) {
            return;
        }

        String sqlCommand = "";

        String lastSqlResponse =
                step.getName()
                        .toLowerCase()
                        .replace(" ", "_")
                        + "_res";

        /*
         * ------------------------------------------------------------
         * 1. SQL statement suchen und EXEC erzeugen
         * ------------------------------------------------------------
         */
        for (Map.Entry<String, StepValueDetails> entry :
                values.entrySet()) {

            if (entry.getKey()
                    .equalsIgnoreCase("SQL Statement")) {

                sqlCommand =
                        entry.getValue()
                                .getValue();

                break;
            }
        }

        if (!sqlCommand.isEmpty()) {

            /*
             * Zeilenumbrüche entfernen.
             */
            sqlCommand =
                    sqlCommand
                            .replace("\n", " ")
                            .replace("\r", " ");

            /*
             * Mehrfache Leerzeichen reduzieren.
             */
            sqlCommand =
                    sqlCommand
                            .replaceAll("\\s+", " ")
                            .trim();

            ToscaToAgaPhase1.writeLine("\n");

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "\n      # Execute SQL Query"
            );

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "      - type: SQL"
            );

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        op: EXEC"
            );

            if (step.getCondition() != null
                    && !step.getCondition().equals("")) {

                String condition =
                        step.getCondition();

                String cleanCondition =
                        ToscaValueTranslator
                                .translateToscaValues(
                                        condition
                                );

                String formattedCondition =
                        cleanCondition
                                .replace("\"", "'");

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "        condition: \""
                                + formattedCondition
                                + "\""
                );
            }

            String translatedCommand =
                    ToscaValueTranslator
                            .translateToscaValues(
                                    sqlCommand
                            );

            if (translatedCommand.contains("\"")) {

                /*
                 * SQL enthält Double Quotes:
                 * YAML Block-Format verwenden.
                 */
                ToscaToAgaPhase1.writeLine(
                        ident
                                + "        command: |"
                );

                String cleaned =
                        translatedCommand
                                .replaceAll("\\r?\\n", " ")
                                .trim();

                /*
                 * Führende / abschließende Quotes entfernen.
                 */
                cleaned =
                        cleaned.replaceAll(
                                "^\"+|\"+$",
                                ""
                        );

                cleaned =
                        cleaned.replaceAll(
                                "^'+|'+$",
                                ""
                        );

                translatedCommand =
                        cleaned.replace(
                                "\"\"\"\"",
                                "\""
                        );

                translatedCommand =
                        translatedCommand.replace(
                                "\"\"\"",
                                "\""
                        );

                String formattedSql =
                        translatedCommand.replace(
                                "   ",
                                " "
                        );

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "            "
                                + formattedSql
                );

            } else {

                /*
                 * Standardformat für einfache SQL Statements.
                 */
                translatedCommand =
                        translatedCommand.replace(
                                "' ",
                                "'"
                        );

                if (translatedCommand != null
                        && translatedCommand.endsWith(";")) {

                    translatedCommand =
                            translatedCommand.substring(
                                    0,
                                    translatedCommand.length() - 1
                            );
                }

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "        command: \""
                                + translatedCommand
                                + "\""
                );
            }

            ToscaToAgaPhase1.writeLine(
                    ident
                            + "        response: "
                            + lastSqlResponse
            );

            ToscaToAgaPhase1.writeLine("");
        }

        /*
         * Für INSERT / UPDATE / DELETE sind keine Result-Set ASSERTs
         * bzw. BUFFER-Schritte möglich.
         */
        if (sqlCommand != null
                && !sqlCommand.isEmpty()
                && !sqlCommand.toUpperCase().startsWith("SELECT")
                && !sqlCommand.startsWith("{")) {
            
            return;
        }

        /*
         * ------------------------------------------------------------
         * 2. Weitere Tosca-Werte verarbeiten:
         *    ASSERT / BUFFER / Constraints
         * ------------------------------------------------------------
         */
        for (Map.Entry<String, StepValueDetails> entry :
                values.entrySet()) {

            String key =
                    entry.getKey();

            if (key.startsWith("SQL Statement")) {
                continue;
            }

            StepValueDetails details =
                    entry.getValue();

            /*
             * --------------------------------------------------------
             * TEMP DEBUG:
             *
             * Zeigt uns ALLE Werte, die ProcessDbStep tatsächlich
             * vom vorherigen Migration-Schritt bekommt.
             *
             * Besonders wichtig für:
             *
             * Result Table
             *   #2
             *     #1 = DB_COUNT / Buffer
             * --------------------------------------------------------
             */
//            System.err.println(
//                    "[DB-VALUE-TRACE]"
//                            + " step='"
//                            + step.getName()
//                            + "'"
//                            + " key='"
//                            + key
//                            + "'"
//                            + " value='"
//                            + details.getValue()
//                            + "'"
//                            + " mode='"
//                            + details.getActionMode()
//                            + "'"
//                            + " path='"
//                            + details.getToscaPath()
//                            + "'"
//                            + " property='"
//                            + details.getActionProperty()
//                            + "'"
//            );

            String mode =
                    translateActionMode(
                            details.getActionMode()
                    );

            String value =
                    details.getValue();

            /*
             * --------------------------------------------------------
             * Tosca Result Table / RowCount
             *
             * Beispiel:
             *
             * Tosca:
             *   RowCount != 0
             *
             * AGATE:
             *
             *   source: ROW_COUNT
             *   action: NOT_EQUALS
             *   expected: "0"
             * --------------------------------------------------------
             */
            if ("Verify".equals(mode)
                    && "RowCount".equalsIgnoreCase(
                            details.getActionProperty()
                    )
                    && "Result Table".equalsIgnoreCase(
                            details.getToscaPath()
                    )) {

                String operator =
                        translateOperator(
                                details.getOperator()
                        );

                String expected =
                        details.getValue();

//                System.err.println(
//                        "[DB-ROWCOUNT-TRACE]"
//                                + " step='"
//                                + step.getName()
//                                + "'"
//                                + " toscaOperator='"
//                                + details.getOperator()
//                                + "'"
//                                + " agateOperator='"
//                                + operator
//                                + "'"
//                                + " expected='"
//                                + expected
//                                + "'"
//                );

                ToscaToAgaPhase1.writeLine("\n");

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "      # DB Check: RowCount"
                );

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "      - type: SQL"
                );

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "        op: ASSERT"
                );

                if (step.getCondition() != null
                        && !step.getCondition().isEmpty()) {

                    String cleanCondition =
                            ToscaValueTranslator
                                    .translateToscaValues(
                                            step.getCondition()
                                    );

                    String formattedCondition =
                            cleanCondition.replace(
                                    "\"",
                                    "'"
                            );

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "        condition: \""
                                    + formattedCondition
                                    + "\""
                    );
                }

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "        source: ROW_COUNT"
                );

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "        action: "
                                + operator
                );

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "        expected: \""
                                + expected
                                + "\""
                );

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "        response: "
                                + lastSqlResponse
                );

                ToscaToAgaPhase1.writeLine("");

                continue;
            }

            /*
             * --------------------------------------------------------
             * Normale SQL Tabellenzelle
             * --------------------------------------------------------
             */
            String toscaPath =
                    details.getToscaPath();

            if (toscaPath == null
                    || toscaPath.isBlank()) {

                continue;
            }

            /*
             * Zusätzlicher Trace speziell für DB-Zellen.
             */
//            System.err.println(
//                    "[DB-CELL-TRACE]"
//                            + " step='"
//                            + step.getName()
//                            + "'"
//                            + " key='"
//                            + key
//                            + "'"
//                            + " path='"
//                            + toscaPath
//                            + "'"
//                            + " mode='"
//                            + mode
//                            + "'"
//                            + " value='"
//                            + details.getValue()
//                            + "'"
//            );

            String cleaned =
                    toscaPath
                            .replace("#", "")
                            .trim();

            /*
             * Tosca kann einen strukturellen Prefix liefern:
             *
             * Result Table.#2.#1
             *
             * Der Prefix gehört nicht zum eigentlichen
             * Row-/Column-Index.
             */
            if (cleaned
                    .toLowerCase()
                    .startsWith("result table.")) {

                cleaned =
                        cleaned.substring(
                                "result table.".length()
                        );
            }

            String[] parts =
                    cleaned.split("\\.");

            if (parts.length < 2) {

//                System.err.println(
//                        "[DB-CELL-TRACE]"
//                                + " skipped - unsupported path='"
//                                + toscaPath
//                                + "'"
//                );

                continue;
            }

            String row =
                    parts[0].trim();

            String column =
                    parts[1].trim();

            /*
             * Tosca Tabellenzeilen beginnen unter Berücksichtigung
             * des Headers bei 2.
             *
             * AGATE arbeitet zero-based.
             *
             * Tosca #2 -> AGATE 0
             * Tosca #3 -> AGATE 1
             */
            if (row.matches("\\d+")) {

                row =
                        String.valueOf(
                                Integer.parseInt(row) - 2
                        );
            }

            /*
             * Tosca Spaltenindex -> AGATE zero-based.
             *
             * Tosca #1 -> AGATE 0
             * Tosca #2 -> AGATE 1
             */
            if (column.matches("\\d+")) {

                column =
                        String.valueOf(
                                Integer.parseInt(column) - 1
                        );
            }

            if (mode.equals("Buffer")
                    || mode.equals("Verify")) {

                /*
                 * ----------------------------------------------------
                 * BUFFER
                 * ----------------------------------------------------
                 */
                if (mode.equals("Buffer")) {

                    ToscaToAgaPhase1.writeLine("\n");

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "      # DB Check: "
                                    + column
                    );

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "      - type: SQL"
                    );

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "        op: BUFFER"
                    );

                    /*
                     * In Tosca ist bei Buffer der Value das Ziel-
                     * Buffer-Variablenname.
                     */
                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "        name: "
                                    + details.getValue()
                    );

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "        row: \""
                                    + row
                                    + "\""
                    );

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "        column: \""
                                    + column
                                    + "\""
                    );

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "        response: "
                                    + lastSqlResponse
                    );

                    ToscaToAgaPhase1.writeLine("");

                } else {

                    /*
                     * ------------------------------------------------
                     * Normales ASSERT auf eine Zelle
                     * ------------------------------------------------
                     */
                    ToscaToAgaPhase1.writeLine("\n");

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "\n      # DB Check: "
                                    + column
                    );

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "      - type: SQL"
                    );

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "        op: ASSERT"
                    );

                    if (step.getCondition() != null
                            && !step.getCondition().equals("")) {

                        String condition =
                                step.getCondition();

                        String cleanCondition =
                                ToscaValueTranslator
                                        .translateToscaValues(
                                                condition
                                        );

                        String formattedCondition =
                                cleanCondition.replace(
                                        "\"",
                                        "'"
                                );

                        ToscaToAgaPhase1.writeLine(
                                ident
                                        + "        condition: \""
                                        + formattedCondition
                                        + "\""
                        );
                    }

                    /*
                     * Tosca Wildcard:
                     *
                     * *TEXT* -> AGATE CONTAINS
                     *
                     * sonst EQUALS.
                     */
                    if (details.getValue().contains("*")) {

                        ToscaToAgaPhase1.writeLine(
                                ident
                                        + "        action: CONTAINS"
                        );

                    } else {

                        ToscaToAgaPhase1.writeLine(
                                ident
                                        + "        action: EQUALS"
                        );
                    }

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "        expected: \""
                                    + details.getValue()
                                    + "\""
                    );

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "        row: \""
                                    + row
                                    + "\""
                    );

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "        column: \""
                                    + column
                                    + "\""
                    );

                    ToscaToAgaPhase1.writeLine(
                            ident
                                    + "        response: "
                                    + lastSqlResponse
                    );

                    ToscaToAgaPhase1.writeLine("");

                }

            } else if (mode.equals("Unknown_519")) {

                /*
                 * ----------------------------------------------------
                 * Constraints
                 * ----------------------------------------------------
                 */
                ToscaToAgaPhase1.writeLine(
                        ident
                                + "        constraints: "
                );

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "          - column: \""
                                + details.getName()
                                + "\""
                );

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "            action: EQUALS"
                );

                ToscaToAgaPhase1.writeLine(
                        ident
                                + "            expected: \""
                                + value
                                + "\""
                );

            } else {

                continue;
            }
        }
    }

    /*
     * Tosca Verify operator -> AGATE assertion action.
     *
     * Aktuell bekannte Tosca-Codes:
     *
     * 1 = EQUALS
     * 2 = NOT_EQUALS
     */
    private static String translateOperator(
            String operator) {

        if (operator == null) {
            return "";
        }

        return switch (operator) {

            case "1" ->
                    "EQUALS";

            case "2" ->
                    "NOT_EQUALS";

            default ->
                    "Unknown_" + operator;
        };
    }

    private static String translateActionMode(
            String code) {

        if (code == null) {
            return "";
        }

        /*
         * Wenn bereits als Text geliefert,
         * direkt übernehmen.
         */
        if (code.equalsIgnoreCase("Input")) {
            return "Input";
        }

        if (code.equalsIgnoreCase("Verify")) {
            return "Verify";
        }

        if (code.equalsIgnoreCase("Buffer")) {
            return "Buffer";
        }

        return switch (code) {

            /*
             * INPUT
             */
            case "1" ->
                    "Select";

            case "37",
                 "515" ->
                    "Input";

            /*
             * VERIFY
             */
            case "38",
                 "517",
                 "69" ->
                    "Verify";

            /*
             * BUFFER
             */
            case "39",
                 "514",
                 "165" ->
                    "Buffer";

            default ->
                    "Unknown_" + code;
        };
    }
}

