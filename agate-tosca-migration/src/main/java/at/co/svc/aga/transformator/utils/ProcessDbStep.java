package at.co.svc.aga.transformator.utils;

import java.util.Map;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

public class ProcessDbStep {
    public static void processDbStep(CleanStep step, String ident) {
        Map<String, StepValueDetails> values = step.getValuesAsMap();
        if (values == null) return;

        String sqlCommand = "";
        String lastSqlResponse = step.getName().toLowerCase().replace(" ", "_") + "_res";

        // 1. Prvo tražimo SQL komandu za EXEC korak
        for (Map.Entry<String, StepValueDetails> entry : values.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("SQL Statement")) {
                sqlCommand = entry.getValue().getValue();
                break;
            }
        }

        if (!sqlCommand.isEmpty()) {
         // ČIŠĆENJE: Zamenjujemo nove redove (LF i CR) razmakom
            sqlCommand = sqlCommand.replace("\n", " ").replace("\r", " ");
            
            // OPCIONO: Smanjujemo višestruke razmake na samo jedan
            sqlCommand = sqlCommand.replaceAll("\\s+", " ").trim();
            
            ToscaToAgaPhase1.writeLine("\n");
            ToscaToAgaPhase1.writeLine(ident + "\n      # Execute SQL Query");
            ToscaToAgaPhase1.writeLine(ident + "      - type: SQL");
            ToscaToAgaPhase1.writeLine(ident + "        op: EXEC");
            if ((step.getCondition() != null) && (!step.getCondition().equals(""))) {
                String condition = step.getCondition();
                String cleanCondition = SVCToscaTranslator.translateToscaValues(condition);

                String formattedCondition = cleanCondition.replace("\"", "'");
                ToscaToAgaPhase1.writeLine(ident + "        condition: \"" + formattedCondition + "\"");
            }
            String translatedCommand = SVCToscaTranslator.translateToscaValues(sqlCommand);

            if (translatedCommand.contains("\"")) {
                // Ako sadrži navodnike, koristimo | format
                ToscaToAgaPhase1.writeLine(ident + "        command: |");
                String cleaned = translatedCommand.replaceAll("\\r?\\n", " ").trim();
                // Regex "^\"+|\"+$" znači: nađi sve navodnike na početku (^) ili na kraju ($)
                cleaned = cleaned.replaceAll("^\"+|\"+$", "");
                cleaned = cleaned.replaceAll("^'+|'+$", "");
                                
                // Ovde pretpostavljam da 'newCommand' dolazi iz neke obrade ili je to 'translatedCommand'
                // Ako SQL sadrži više linija, možeš ih odvojiti sa System.lineSeparator()
                translatedCommand = cleaned.replace("\"\"\"\"", "\"");
                translatedCommand = translatedCommand.replace("\"\"\"", "\"");

                String formattedSql = translatedCommand.replace("   ", " "); // Opciono čišćenje
                ToscaToAgaPhase1.writeLine(ident + "            " + formattedSql);
            } else {
                // Standardni format za jednostavne komande
                translatedCommand = translatedCommand.replace("' ", "'");
                if (translatedCommand != null && translatedCommand.endsWith(";")) {
                    translatedCommand = translatedCommand.substring(0, translatedCommand.length() - 1);
                }
                ToscaToAgaPhase1.writeLine(ident + "        command: \"" + translatedCommand + "\"");
            }
            ToscaToAgaPhase1.writeLine(ident + "        response: " + lastSqlResponse);
            ToscaToAgaPhase1.writeLine("");
        }

        if ((!sqlCommand.isEmpty()) &&(sqlCommand != null) && (!sqlCommand.toUpperCase().startsWith("SELECT")) ) {
            return;
        }

        // 2. Prolazimo kroz ostale vrednosti za ASSERT i BUFFER
        for (Map.Entry<String, StepValueDetails> entry : values.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("SQL Statement")) continue;

            StepValueDetails details = entry.getValue();
            String mode = translateActionMode(details.getActionMode());
            String value = details.getValue();
            
            String toscaPath = details.getToscaPath();
            String cleaned = toscaPath.replace("#", "");
            String[] parts = cleaned.split("\\.");
            String row = parts[0];
            String column = parts[1];
            if (row.matches("\\d+")) {
                row = String.valueOf(Integer.parseInt(row) - 2);
            }

            if (column.matches("\\d+")) {
                column = String.valueOf(Integer.parseInt(column) - 1);
            }
            if ((mode.equals("Buffer")) || (mode.equals("Verify"))) {
                if ((mode.equals("Buffer"))) {
                    ToscaToAgaPhase1.writeLine("\n");
                    ToscaToAgaPhase1.writeLine(ident + "      # DB Check: " + column);
                    ToscaToAgaPhase1.writeLine(ident + "      - type: SQL");
                    
                    {
                        ToscaToAgaPhase1.writeLine(ident + "        op: BUFFER");
                        ToscaToAgaPhase1.writeLine(ident + "        name: " + details.getValue()); // U Tosca Buffer vrednost je ime varijable
                    }


                    ToscaToAgaPhase1.writeLine(ident + "        row: \"" + row +"\""); //1 BUFFER
                    ToscaToAgaPhase1.writeLine(ident + "        column: \"" + column + "\"");
                    ToscaToAgaPhase1.writeLine(ident + "        response: " + lastSqlResponse);
                    ToscaToAgaPhase1.writeLine("");
                    
                } else {
                    ToscaToAgaPhase1.writeLine("\n");
                    ToscaToAgaPhase1.writeLine(ident + "\n      # DB Check: " + column);
                    ToscaToAgaPhase1.writeLine(ident + "      - type: SQL");
                    
                    {
                        ToscaToAgaPhase1.writeLine(ident + "        op: ASSERT");
                        if ((step.getCondition() != null)&& (!step.getCondition().equals(""))) {
                            String condition = step.getCondition();
                            String cleanCondition = SVCToscaTranslator.translateToscaValues(condition);
                            
                            String formattedCondition = cleanCondition.replace("\"", "'");
                            ToscaToAgaPhase1.writeLine(ident + "        condition: \"" + formattedCondition + "\"");
                        }

                        if (details.getValue().contains("*"))
                           ToscaToAgaPhase1.writeLine(ident + "        action: CONTAINS");
                        else
                           ToscaToAgaPhase1.writeLine(ident + "        action: EQUALS");
                        ToscaToAgaPhase1.writeLine(ident + "        expected: \"" + details.getValue() + "\"");
                    }

                    ToscaToAgaPhase1.writeLine(ident + "        row: \"" + row +"\""); // 1 ASSERT
                    ToscaToAgaPhase1.writeLine(ident + "        column: \"" + column + "\"");
                    ToscaToAgaPhase1.writeLine(ident + "        response: " + lastSqlResponse);
                    ToscaToAgaPhase1.writeLine("");
                    
                }
            } else if ((mode.equals("Unknown_519"))) { 
                ToscaToAgaPhase1.writeLine(ident + "        constraints: "); 
                ToscaToAgaPhase1.writeLine(ident + "          - column: \"" + details.getName() + "\""); 
                ToscaToAgaPhase1.writeLine(ident + "            action: EQUALS"); 
                ToscaToAgaPhase1.writeLine(ident + "            expected: \"" + value + "\""); 
            }else {
                continue;
            }
        }
    }

    private static String translateActionMode(String code) {
        if (code == null) return "";
        // Ako je već tekstualno, vrati kako jeste radi lakše provere
        if (code.equalsIgnoreCase("Input")) return "Input";
        if (code.equalsIgnoreCase("Verify")) return "Verify";
        if (code.equalsIgnoreCase("Buffer")) return "Buffer";

        switch (code) {
            // INPUT
            case "1": return "Select";
            case "37": 
            case "515": return "Input";
            
            // VERIFY
            case "38": 
            case "517": 
            case "69": return "Verify";
            
            // BUFFER
            case "39": 
            case "514": 
            case "165": return "Buffer"; // 165 se često koristi za uzimanje vrednosti u API-ju
            
            default: return "Unknown_" + code;
        }
    }

    
}
