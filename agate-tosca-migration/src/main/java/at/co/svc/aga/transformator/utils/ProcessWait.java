package at.co.svc.aga.transformator.utils;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;



public class ProcessWait {
    public static void processWait(CleanStep step, String ident) {
        // Sada proveravamo da li je lista null
        if (step.getValues() == null) return;

        // Iteriramo kroz listu umesto kroz entrySet mape
        for (StepValueDetails details : step.getValues()) {
            ToscaToAgaPhase1.writeLine(""); // Razmak za svaki pojedinačni buffer element
            
            String name = details.getName(); // Uzimamo ime iz objekta
            String mode = details.getActionMode();
            String action = details.getValue();
            String condition = step.getCondition();
            
            String actionNew = action;
            if (actionNew != null && actionNew.contains("{B[")) {
                actionNew = ToscaValueTranslator.translateToscaValues(action);
            }

            /** This is done with ProcessVariablesBlock **/
            if ("Insert".equalsIgnoreCase(mode)) {
                if (condition != null) {
                    condition = condition.replace("{PL[", "{R[");
                }
                printYamlStep("WAIT", "", name, actionNew, null, condition, ident);
            } 
        }
    }

    private static void printYamlStep(String type, String op, String name, String action, String expected, String condition, String ident) {
        condition = ToscaValueTranslator.translateToscaValues(condition);

        ToscaToAgaPhase1.writeLine("");
        ToscaToAgaPhase1.writeLine(ident + "      - type: " + type);
        if ((condition != null) && (!"".equals(condition))) {
            String formattedCondition = condition.replace("\"", "'");
            ToscaToAgaPhase1.writeLine(ident + "        condition: \"" + formattedCondition + "\"");
        }
        if (action != null) {
            ToscaToAgaPhase1.writeLine(ident + "        value: \"" + ToscaValueTranslator.translateToscaValues(action) + "\"");
        }
    }
} 
