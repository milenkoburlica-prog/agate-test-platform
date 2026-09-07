package at.co.svc.aga.transformator.utils;

import java.util.Map;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;



public class ProcessBuffer {
    public static void processBuffer(CleanStep step, String ident) {
        if (step.getValues() == null) return;

        for (Map.Entry<String, StepValueDetails> entry : step.getValuesAsMap().entrySet()) {
            ToscaToAgaPhase1.writeLine(""); // Razmak za svaki pojedinačni buffer element
            String name = entry.getKey();
            StepValueDetails details = entry.getValue();
            String mode = details.getActionMode();
            String action = details.getValue();
            String condition = step.getCondition();
            
            String actionNew = action;
            if (actionNew.contains("{B["))
                actionNew = SVCToscaTranslator.translateToscaValues(action);

            /** This is done with ProcessVariablesBlock **/
            if (("Input".equalsIgnoreCase(mode)) || ("Insert".equalsIgnoreCase(mode))) {
                if (condition!=null)
                    condition = condition.replace("{PL[", "{R[");
                printYamlStep("BUFFER", "EXEC", name, actionNew, null, condition, ident);
            } 
            else
            if ("Verify".equalsIgnoreCase(mode)) {
                if (condition!=null)
                    condition = condition.replace("{PL[", "{R[");
                printYamlStep("BUFFER", "ASSERT", name, null, actionNew, condition, ident);
            }
        }
    }


    private static void printYamlStep(String type, String op, String name, String action, String expected, String condition, String ident) {
        condition = SVCToscaTranslator.translateToscaValues(condition);

        ToscaToAgaPhase1.writeLine("");
        ToscaToAgaPhase1.writeLine(ident + "      - type: " + type);
        ToscaToAgaPhase1.writeLine(ident + "        op: " + op);
        if ((condition != null) && (!"".equals(condition))) {
            String formattedCondition = condition.replace("\"", "'");
            ToscaToAgaPhase1.writeLine(ident + "        condition: \"" + formattedCondition + "\"");
        }
        ToscaToAgaPhase1.writeLine(ident + "        name: " + name);
        
        if (action != null) {
            ToscaToAgaPhase1.writeLine(ident + "        value: \"" + SVCToscaTranslator.translateToscaValues(action) + "\"");
        }
        if (expected != null) {
            ToscaToAgaPhase1.writeLine(ident + "        action: EQUALS");
            ToscaToAgaPhase1.writeLine(ident + "        expected: \"" + expected + "\"");
        }
    }
    
    
    public static void processInlineBuffer(CleanStep step, String ident) {
        if (step.getValues() == null) return;
        
        for (Map.Entry<String, StepValueDetails> entry : step.getValuesAsMap().entrySet()) {
            String val = (entry.getValue().getValue() == null) ? "" : entry.getValue().getValue();
            val = SVCToscaTranslator.translateToscaValues(val);
            
            ToscaToAgaPhase1.writeLine(ident + " ");
            ToscaToAgaPhase1.writeLine(ident + "      - type: BUFFER");
            ToscaToAgaPhase1.writeLine(ident + "        op: EXEC");
            ToscaToAgaPhase1.writeLine(ident + "        name: \"" + entry.getKey() + "\"");
            if (step.getCondition() != null) {
                String condition = step.getCondition();
                String cleanCondition = SVCToscaTranslator.translateToscaValues(condition);

                String formattedCondition = condition.replace("\"", "'");
                ToscaToAgaPhase1.writeLine(ident + "        condition: \"" + formattedCondition + "\"");
            }
            
            String translatedValue = val;
            if (translatedValue.contains("\"")) {
                // Ako sadrži navodnike, koristimo | format
                ToscaToAgaPhase1.writeLine(ident + "        value: |");
                
                // Ovde pretpostavljam da 'newCommand' dolazi iz neke obrade ili je to 'translatedCommand'
                // Ako SQL sadrži više linija, možeš ih odvojiti sa System.lineSeparator()
                translatedValue = translatedValue.replace("\"\"\"\"", "\"");
                translatedValue = translatedValue.replace("\"\"\"", "\"");

                ToscaToAgaPhase1.writeLine(ident + "            " + translatedValue);
            } else {
                ToscaToAgaPhase1.writeLine(ident + "        value: \"" + translatedValue + "\"");
            }

        }
    }
    

}
