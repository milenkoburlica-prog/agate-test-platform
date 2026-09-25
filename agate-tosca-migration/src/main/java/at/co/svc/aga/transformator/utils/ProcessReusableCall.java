package at.co.svc.aga.transformator.utils;

import java.util.Map;

import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;
import at.co.svc.tosca.util.FileNameSanitizer;



public class ProcessReusableCall {
    public static void processReusableCall(CleanStep step) {
        
        System.err.println(
                "[REUSABLE-CALL-TRACE] name=["
                        + step.getName()
                        + "] reusable=["
                        + step.getReusableName()
                        + "] condition=["
                        + step.getCondition()
                        + "]"
        );
        
        
        // First try to apply a special-case handler
        if (izuzetakZaSSHNOVerify(step)) {
            return; // If it was applied, stop processing this step
        }
     // 2. Check: SSH with buffer
        if (izuzetakZaSSHBuffer(step)) {
            return;
        }
     // 2. SCP special case
        if (izuzetakZaSCPTransfer(step))
            return;
        
        
        // If no special case applies, use the standard CALL logic
        String libName = step.getReusableName() != null ? step.getReusableName() : "unknown_block";
        
        MigrationLog.debug(
                "Reusable CALL: step='"
                        + step.getName()
                        + "', rawName='"
                        + libName
                        + "'"
        );

        String cleanName =
                FileNameSanitizer.sanitize(libName);

        String agatePath =
                "reusable." + cleanName;
        
        if (agatePath.endsWith("_")) {
            agatePath = agatePath.substring(0, agatePath.length()-1);
        }
        
        MigrationLog.debug(
                "Reusable CALL generated command: '"
                        + agatePath
                        + "'"
        );

        if (agatePath.equalsIgnoreCase("reusable.dialogaufbaubyordid_auth_1")) {
            agatePath = "reusable.ru_dialog_aufbau_mit_ordid";
        }
        if (agatePath.equalsIgnoreCase("reusable.dialogaufbaubyordid_buffered_auth_1")) {
            agatePath = "reusable.ru_dialog_aufbau_mit_ordid";
        }

        ToscaToAgaPhase1.writeLine(""); //reusable.create_cardtoken_vpsig_o_card
        ToscaToAgaPhase1.writeLine("      - type: CALL");
        ToscaToAgaPhase1.writeLine("        command: '" + agatePath + "'");
        
        System.err.println(
                "[CALL-CONDITION-TRACE] reusable=["
                        + step.getReusableName()
                        + "] name=["
                        + step.getName()
                        + "] rawCondition=["
                        + step.getCondition()
                        + "]"
        );
        
        writeCondition(step);
        

     // 1. Read the parameter map once
        Map<String, StepValueDetails> params = step.getValuesAsMap();

        // 2. Apply key normalization
        if (agatePath.equals("reusable.ru_dialog_aufbau_mit_ordid")) {
            if (params.containsKey("VpNummer")) {
                StepValueDetails details = params.remove("VpNummer");
                params.put("vpNummer", details);
            }
        }

        // 3. Check whether the map contains values and reuse the existing params variable
        if (params != null && !params.isEmpty()) {
            ToscaToAgaPhase1.writeLine("        parameters:");
            
            // 4. Iterate over the normalized params map instead of calling step.getValuesAsMap() again
            params.forEach((k, v) -> {
                String vValue = v.getValue();
                vValue = ToscaValueTranslator.translateToscaValues(vValue);
                ToscaToAgaPhase1.writeLine("          " + k + ": '" + vValue + "'");
            });
        }
        
        if (agatePath.toLowerCase().startsWith("reusable.ru_dialog_aufbau_mit_ordid")) {
            ToscaToAgaPhase1.writeLine("          cardSlot: 'baseContact'");
        }
        if (agatePath.toLowerCase().startsWith("reusable.create_cardtoken")) {
//            ToscaToAgaPhase1.writeLine("        parameters: ");
//            ToscaToAgaPhase1.writeLine("          ORD_ID: '{B[B_OrdinationsId]}'");
//            ToscaToAgaPhase1.writeLine("          vpNummer: '{B[B_Karte]}'");
            ToscaToAgaPhase1.writeLine("          cardSlot: 'baseContact'");
        }

    }

    private static void writeCondition(CleanStep step) {

        if (step == null
                || step.getCondition() == null
                || step.getCondition().isBlank()) {
            return;
        }

        String condition =
                ToscaValueTranslator.translateToscaValues(
                        step.getCondition()
                );

        String yamlCondition;

        if (condition.contains("\"")
                && !condition.contains("'")) {

            // Condition contains double quotes only.
            // Use YAML single quotes outside.
            yamlCondition =
                    "'"
                            + condition
                            + "'";

        } else {

            // Use YAML double quotes outside.
            // Escape only embedded double quotes.
            yamlCondition =
                    "\""
                            + condition.replace(
                                    "\\",
                                    "\\\\"
                            ).replace(
                                    "\"",
                                    "\\\""
                            )
                            + "\"";
        }

        ToscaToAgaPhase1.writeLine(
                "        condition: "
                        + yamlCondition
        );
    }
    

    public static void normalizeDialogaufbau(CleanStep step) {
        if (step != null && step.getValues() != null) {
            // Use the map to manipulate parameter keys
            Map<String, StepValueDetails> valuesMap = step.getValuesAsMap();
            
            StepValueDetails details = valuesMap.remove("VpNummer");
            if (details != null) {
                valuesMap.put("vpNummer", details);
                // Important: getValuesAsMap returns a new map, so 
                // write it back to the list if the change must be preserved.
                step.setValues(new java.util.ArrayList<>(valuesMap.values()));
            }
        }
    }
    public static void normalizeCardtoken_svsig_e_card(CleanStep step) {
        if (step != null && step.getValues() != null) {
            // Use the map to manipulate parameter keys
            Map<String, StepValueDetails> valuesMap = step.getValuesAsMap();
            
            StepValueDetails details = valuesMap.remove("VpNummer");
            if (details != null) {
                valuesMap.put("vpNummer", details);
                // Important: getValuesAsMap returns a new map, so 
                // write it back to the list if the change must be preserved.
                step.setValues(new java.util.ArrayList<>(valuesMap.values()));
            }
        }
    }
    
    private static boolean izuzetakZaSSHNOVerify(CleanStep step) {
        String libName = step.getReusableName();
        Map<String, StepValueDetails> values = step.getValuesAsMap(); // change
        
        if ("ssh_execute_unix_command_utf_8".equalsIgnoreCase(libName) && values != null) {
            StepValueDetails optionDetail = values.get("Option");
            
            if (optionDetail != null && "verify-no".equalsIgnoreCase(optionDetail.getValue())) {
                String command = values.containsKey("Command") ? values.get("Command").getValue() : "";
                
                // Write the step name without additional numbering
                ToscaToAgaPhase1.writeLine("      # " + (step.getName() != null ? step.getName() : "SSH Command (No Verify)"));
                ToscaToAgaPhase1.writeLine("      - type: OC");
                ToscaToAgaPhase1.writeLine("        op: EXEC");
                writeCondition(step);
                ToscaToAgaPhase1.writeLine("        pod: \"{B[chartname]}\"");
                ToscaToAgaPhase1.writeLine("        command: \"" + command + "\"");
                ToscaToAgaPhase1.writeLine("        response: response_no_verify");
                
                return true; 
            }
        }
        return false;
    }

    private static boolean izuzetakZaSSHBuffer(CleanStep step) {
        String libName = step.getReusableName();
        Map<String, StepValueDetails> values = step.getValuesAsMap();

        if ("ssh_execute_unix_command_utf_8".equalsIgnoreCase(libName) && values != null) {
            StepValueDetails optionDetail = values.get("Option");
            
            if (optionDetail != null && "buffer".equalsIgnoreCase(optionDetail.getValue())) {
                String command = values.containsKey("Command") ? values.get("Command").getValue() : "";
                String bufferName = values.containsKey("Verify") ? values.get("Verify").getValue() : "UNKNOWN_BUFFER";
                String stepName = (step.getName() != null ? step.getName() : "SSH Command");

                // --- First block: EXEC ---
                // Use [Action] instead of a fixed number
                ToscaToAgaPhase1.writeLine("      # [Action] " + stepName);
                ToscaToAgaPhase1.writeLine("      - type: OC");
                ToscaToAgaPhase1.writeLine("        op: EXEC");
                writeCondition(step);
                ToscaToAgaPhase1.writeLine("        pod: \"{B[chartname]}\"");
                ToscaToAgaPhase1.writeLine("        command: \"" + command + "\"");
                ToscaToAgaPhase1.writeLine("        response: oc_buffer");
                // 2. Add an empty line between the two blocks of the same step
                ToscaToAgaPhase1.writeLine("");
                // --- Second block: BUFFER ---
                // Use [Extract] instead of a fixed number
                ToscaToAgaPhase1.writeLine("      # [Extract] " + stepName);
                ToscaToAgaPhase1.writeLine("      - type: OC");
                ToscaToAgaPhase1.writeLine("        op: BUFFER");
                writeCondition(step);
                ToscaToAgaPhase1.writeLine("        response: oc_buffer");
                ToscaToAgaPhase1.writeLine("        action: TEXT");
                ToscaToAgaPhase1.writeLine("        name: \"" + bufferName + "\"");
                
                return true;
            }
        }
        return false;
    }


    private static boolean izuzetakZaSCPTransfer(CleanStep step) {
        // Read the reusable name (for example, "scp_transfer_files_to_from_unix_servers___alt")
        String libName = step.getReusableName();
        if (libName == null) return false;

        Map<String, StepValueDetails> values = step.getValuesAsMap();;

        // Use contains() so variants with suffixes are handled as well
        if (libName.toLowerCase().contains("scp_transfer_files") && values != null) {
            
            // Use case-insensitive lookup because Tosca parameter key casing may vary
            String option = MigationPhase2Utils.getMapValueIgnoreCase(values, "Option");
            String winPath = MigationPhase2Utils.getMapValueIgnoreCase(values, "WindowsFilePath");
            String unixPath = MigationPhase2Utils.getMapValueIgnoreCase(values, "UnixFilePath");
            String stepName = (step.getName() != null ? step.getName() : "File Transfer");

            if ("put".equalsIgnoreCase(option)) {
                ToscaToAgaPhase1.writeLine("      # [SCP-PUT] " + stepName);
                ToscaToAgaPhase1.writeLine("      - type: OC");
                ToscaToAgaPhase1.writeLine("        op: PUT");
                writeCondition(step);
                ToscaToAgaPhase1.writeLine("        pod: \"{B[chartname]}\"");
                ToscaToAgaPhase1.writeLine("        from: \"" + winPath + "\"");
                ToscaToAgaPhase1.writeLine("        to: \"" + unixPath + "\"");
                ToscaToAgaPhase1.writeLine("        response: oc_put_res");
                return true;
            } 
            else if ("get".equalsIgnoreCase(option)) {
                ToscaToAgaPhase1.writeLine("      # [SCP-GET] " + stepName);
                ToscaToAgaPhase1.writeLine("      - type: OC");
                ToscaToAgaPhase1.writeLine("        op: GET");
                writeCondition(step);
                ToscaToAgaPhase1.writeLine("        pod: \"{B[chartname]}\"");
                ToscaToAgaPhase1.writeLine("        from: \"" + unixPath + "\"");
                ToscaToAgaPhase1.writeLine("        to: \"" + winPath + "\"");
                ToscaToAgaPhase1.writeLine("        response: oc_get_res");
                return true;
            }
        }
        return false;
    }

}
