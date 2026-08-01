package at.co.svc.agate.core.dsl.register;

import java.util.Map;

import at.co.svc.agate.core.dsl.model.Constraint;
import at.co.svc.agate.core.dsl.model.StepType;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;
import at.co.svc.agate.core.interfaces.TestLogger;

public class PrintDslStepContext {
    public static void logDslStepContext(TestLogger logger, TestStep step) {
        logger.log("");
        // 1. Ako imamo validan originalni YAML tekst bez grešaka, koristimo njega
        if (step.getTextYaml() != null && !step.getTextYaml().trim().isEmpty() && !step.getTextYaml().contains("Greška:")) {
            String[] lines = step.getTextYaml().split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "      " + line);
                }
            }
            return;
        }

        // 2. PAMETNI FALLBACK: Ručno sklapanje strukture u zavisnosti od tipa koraka (Engine-safe)
        try {
            // Svaki korak počinje sa svojim tipom
            logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "       - type: " + step.getType());
            
            // Prikaz uslova (condition) ako postoji i nije prazan
            if (step.getCondition() != null && !step.getCondition().trim().isEmpty()) {
                logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         condition: \"" + step.getCondition() + "\"");
            }

            // --- SPECIFIČAN ISPIS ZA 'CALL' TIP (Reusable roditelj) ---
            if (step.getType() == StepType.CALL) {
                if (step.getCommand() != null) {
                    logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         command: \"" + step.getCommand() + "\"");
                }
                if (step.getParameters() != null && !step.getParameters().isEmpty()) {
                    logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         parameters:");
                    for (Map.Entry<String, Object> param : step.getParameters().entrySet()) {
                        String formattedVal = (param.getValue() instanceof String) ? "\"" + param.getValue() + "\"" : String.valueOf(param.getValue());
                        logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "           " + param.getKey() + ": " + formattedVal);
                    }
                }
                return; // Završavamo ovde za CALL
            }

            // --- SPECIFIČAN ISPIS ZA 'BUFFER' TIP (Unutar fragmenta) ---
            if (step.getType() == StepType.BUFFER) {
                if (step.getOp() != null) {
                    logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         op: " + step.getOp());
                }
                if (step.getName() != null) {
                    logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         name: " + step.getName());
                }
                if (step.getValue() != null) {
                    String formattedVal = (step.getValue() instanceof String) ? "\"" + step.getValue() + "\"" : String.valueOf(step.getValue());
                    logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         value: " + formattedVal);
                }
                return;
            }

            // --- GENERIČKI ISPIS ZA SVE OSTALE ENGINS (REST, SOAP, SQL, CMD...) ---
            if (step.getOp() != null) logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         op: " + step.getOp());
            if (step.getCommand() != null) logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         command: \"" + step.getCommand() + "\"");
            if (step.getUrl() != null) logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         url: \"" + step.getUrl() + "\"");
            if (step.getSource() != null) logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         source: " + step.getSource());
            if (step.getPath() != null) logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         path: " + step.getPath());
            if (step.getAction() != null) logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         action: " + step.getAction());
            if (step.getExpected() != null) logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         expected: \"" + step.getExpected() + "\"");
            if (step.getResponse() != null) logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         response: \"" + step.getResponse() + "\"");
            
         // --- OVDE UBAČI BLOK ZA CONSTRAINTS ---
            if (step.getConstraints() != null && !step.getConstraints().isEmpty()) {
                logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "         constraints:");
                for (Constraint c : step.getConstraints()) {
                    logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "           - path: " + c.getPath());
                    logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "             action: " + c.getAction());
                    logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "             expected: '" + c.getExpected() + "'");
                }
            }
            
        } catch (Exception e) {
            logger.log(ConsoleColors.BLUE + ">>> DSL" + ConsoleColors.RESET + "     : [Greška pri prikazu koraka: " + e.getMessage() + "]");
        }
    }

}
