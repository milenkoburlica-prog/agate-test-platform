package at.co.svc.aga.transformator.utils;

import java.util.Map;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.CleanTestCase;
import at.co.svc.aga.transformator.dto.StepValueDetails;



public class ProcessVariablesBlock {
    /**
     * Glavna metoda za generisanje variables bloka
     */
    public static void processVariablesBlock(CleanTestCase tc) {
        ToscaToAgaPhase1.writeLine("    variables:");
        boolean hasVariables = false;
        java.util.Set<String> addedVarsLow = new java.util.HashSet<>();

        for (CleanStep step : tc.getSteps()) {
            // Koristimo getValuesAsMap() kako bismo dobili Map<String, StepValueDetails> 
            // koju tvoja metoda processValueMap očekuje
            Map<String, StepValueDetails> valueMap = step.getValuesAsMap();
            
            if ("TBox Set Buffer".equalsIgnoreCase(step.getModule())) {
                if (processValueMap(valueMap, addedVarsLow)) hasVariables = true;
            }
            else if ("ApiModule".equalsIgnoreCase(step.getModuleClass())) {
                if (processValueMap(valueMap, addedVarsLow)) {
                    hasVariables = true;
                }
            }
        }
        
        if (!hasVariables) {
            ToscaToAgaPhase1.writeLine("      {}");
        }
    }
    
    /**
     * POMOĆNA METODA: Ovde je ubaci
     * Prolazi kroz mapu vrednosti i ispisuje ih kao YAML varijable ako su tipa Input
     */
    private static boolean processValueMap(Map<String, StepValueDetails> values, java.util.Set<String> addedVarsLow) {
        if (values == null) return false;
        boolean found = false;
        
        for (Map.Entry<String, StepValueDetails> entry : values.entrySet()) {
            String cleanKey = entry.getKey().trim().replaceAll("\\s+", "_");
            StepValueDetails details = entry.getValue();
            String mode = translateActionMode(details.getActionMode());

            if ("Input".equalsIgnoreCase(mode) && !addedVarsLow.contains(cleanKey)) {
                if (cleanKey.equals("endpoint") || cleanKey.equals("resource")) continue;

                String val = (details.getValue() == null) ? "" : details.getValue();
                val = val.replace("\r", " ").replace("\n", " ").replaceAll("\\s+", " ").trim();
                val = SVCToscaTranslator.translateToscaValues(val);

                ToscaToAgaPhase1.writeLine("      " + cleanKey + ": \"" + val + "\"");
                addedVarsLow.add(cleanKey);
                found = true;
            }
        }
        return found;
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
