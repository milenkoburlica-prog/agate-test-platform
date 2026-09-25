package at.co.svc.aga.transformator.utils;

import java.util.Map;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;


public class ProcessEvaluationStep {
    public static void processEvaluationStep(CleanStep step, String ident) {
        if (step.getValues() == null) return;

        for (Map.Entry<String, StepValueDetails> entry : step.getValuesAsMap().entrySet()) {
            StepValueDetails details = entry.getValue();
            String expression = details.getValue().trim();
            String mode = translateActionMode(details.getActionMode());

            if ("Verify".equalsIgnoreCase(mode)) {
                ToscaToAgaPhase1.writeLine("\n");
                ToscaToAgaPhase1.writeLine(ident + "      # Evaluation: " + step.getName());
                
                // Logika za razdvajanje "LevaStrana == DesnaStrana"
                String operator = "EQUALS";
                String[] parts;

                if (expression.contains("==")) {
                    parts = expression.split("==");
                    operator = "EQUALS";
                } else if (expression.contains("!=")) {
                    parts = expression.split("!=");
                    operator = "NOT_EQUALS";
                } else {
                    // Ako nema operatora, fallback na stari EVAL ili neki default
                    parts = new String[]{expression, "true"}; 
                }

                if (parts.length == 2) {
                    String left = parts[0].trim().replaceAll("^'|'$|", ""); // Skidamo navodnike ako postoje
                    String right = parts[1].trim().replaceAll("^'|'$|", "");

                    ToscaToAgaPhase1.writeLine(ident + "      - type: BUFFER");
                    ToscaToAgaPhase1.writeLine(ident + "        op: ASSERT");
                    if ((step.getCondition() != null) && (!step.getCondition().equals(""))) {
                        String condition = step.getCondition();
                        String cleanCondition = ToscaValueTranslator.translateToscaValues(condition);

                        String formattedCondition = cleanCondition.replace("\"", "'");
                        ToscaToAgaPhase1.writeLine(ident + "        condition: \"" + formattedCondition + "\"");
                    }
                    ToscaToAgaPhase1.writeLine(ident + "        name: \"" + left + "\"");
                    ToscaToAgaPhase1.writeLine(ident + "        action: " + operator);
                    ToscaToAgaPhase1.writeLine(ident + "        expected: \"" + right + "\"");
                }
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
