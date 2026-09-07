package at.co.svc.aga.transformator.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SVCToscaTranslator {

    private static final Map<String, String> REPLACEMENTS = new HashMap<>();

    // Pattern za {B[vrednost]} -> {E[mapirana_vrednost]}
    private static final Pattern B_PATTERN = Pattern.compile("\\{B\\[([^\\]]+)\\]\\}");
    
    // NOVO: Pattern za {PL[bilo_sta]} -> {R[bilo_sta]}
    private static final Pattern PL_PATTERN = Pattern.compile("\\{PL\\[([^\\]]+)\\]\\}");

    static {
        // --- ENV PREFIKS ---
        REPLACEMENTS.put("EC_DB_PASSWORD", "env.database.password");
        REPLACEMENTS.put("G_EC_DB_PASSWORD", "env.database.password");
        REPLACEMENTS.put("EC_DB_SCHEMA", "env.database.user");
        REPLACEMENTS.put("G_EC_DB_USER", "env.database.user");
        REPLACEMENTS.put("EC_Instanz_GR", "env.instanzGr");
        REPLACEMENTS.put("G_EC_Instanz_GR", "env.instanzGr");
        REPLACEMENTS.put("EC_Instanz_KL", "env.instanzKl");
        REPLACEMENTS.put("G_EC_Instanz_KL", "env.instanzKl");
        REPLACEMENTS.put("B_OpenShift_Loginserver", "env.openShift.loginServer");
        REPLACEMENTS.put("G_EC_OpenShift_Loginserver", "env.openShift.loginServer");
        REPLACEMENTS.put("OpenShift_SERVER", "env.openShift.loginServer");
        REPLACEMENTS.put("B_OpenShift_Namespace", "env.openShift.namespace");
        REPLACEMENTS.put("G_EC_OpenShift_Namespace", "env.openShift.namespace");
        REPLACEMENTS.put("B_OpenShift_Password", "env.openShift.password");
        REPLACEMENTS.put("B_Password_OpenShift", "env.openShift.password");
        REPLACEMENTS.put("G_EC_OpenShift_Password", "env.openShift.password");
        REPLACEMENTS.put("B_OpenShift_Username", "env.openShift.username");
        REPLACEMENTS.put("B_User_OpenShift", "env.openShift.username");
        REPLACEMENTS.put("G_EC_OpenShift_Username", "env.openShift.username");
        REPLACEMENTS.put("B_URL_DE_HTTPS_HIGH", "env.ecard.service");
        REPLACEMENTS.put("B_URL_HIGH", "env.ecard.service");
        REPLACEMENTS.put("X-SVC-CLIENT-IP", "env.X-SVC-CLIENT-IP");
        
        

        // --- USERS PREFIKS ---
        REPLACEMENTS.put("B_KartenleserIP", "users.ginoip");
        REPLACEMENTS.put("B_Kartenlesersimulator", "users.serverip");
        REPLACEMENTS.put("B_Seriennummer", "users.seriennummer");
        REPLACEMENTS.put("B_Kartenlesername", "users.seriennummer");
        REPLACEMENTS.put("B_CardReaderSimulatorSlot", "users.card.slot.default");


    }

    /**
     * Glavna metoda koja radi sve transformacije vrednosti.
     */
    public static String translateToscaValues(String input) {
        if (input == null || input.isEmpty()) return input;

        // 1. Direktna zamena za %logcheckcloud% varijable (pošto nisu uokvirene sa {B[...]})
        if (input.contains("%logcheckcloud%")) {
            input = input.replace("%logcheckcloud%\\\\%LogCheckCloud%", "{E[env.openshift_logcheck_dir]}")
                         .replace("%logcheckcloud%\\%LogCheckCloud%", "{E[env.openshift_logcheck_dir]}");
        }
        
        // 2. Preostale bazične varijable menjamo u putanju
        if (input.contains("%logcheckcloud%")) {
            input = input.replace("%logcheckcloud%", "{E[env.openshift_logcheck_dir]}");
        }

        // 3. ROBUSTNA SIGURNOST: Sređivanje kosih crta (Čišćenje i dupliranje)
        if (input.contains("{E[env.openshift_logcheck_dir]}")) {
            // Ako je slučajno ispalo tri kose crte, svodi na jednu
            input = input.replace("{E[env.openshift_logcheck_dir]}\\\\\\\\", "{E[env.openshift_logcheck_dir]}\\");
            
            // Ako je od ranije bilo dve kose crte, svodi na jednu
            input = input.replace("{E[env.openshift_logcheck_dir]}\\\\", "{E[env.openshift_logcheck_dir]}\\");
            
            // Sada kada smo 100% sigurni da imamo samo JEDNU kosu crtu, menjamo je u DVE za YAML
            input = input.replace("{E[env.openshift_logcheck_dir]}\\", "{E[env.openshift_logcheck_dir]}\\\\\\");
        }
        
        // 4. Prvo rešavamo {B[...] } -> {E[...] } na osnovu mape
        String result = translateBtoE(input);

        // 5. Zatim rešavamo {PL[...] } -> {R[...] } generički
        result = translatePLtoR(result);

        result = result.replace("\\\\\\", "\\\\") ;
        
        return result;
    }
    

    private static String translateBtoE(String input) {
        Matcher matcher = B_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            sb.append(input, lastEnd, matcher.start());
            String oldKey = matcher.group(1);
            
            if (REPLACEMENTS.containsKey(oldKey)) {
                sb.append("{E[").append(REPLACEMENTS.get(oldKey)).append("]}");
            } else {
                sb.append(matcher.group(0)); // Ostavi kako jeste ako nema mape
            }
            lastEnd = matcher.end();
        }
        sb.append(input.substring(lastEnd));
        return sb.toString();
    }

    private static String translatePLtoR(String input) {
        Matcher matcher = PL_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            sb.append(input, lastEnd, matcher.start());
            String paramName = matcher.group(1);
            
            // Direktna zamena prefiksa PL u R, ime parametra ostaje isto
            sb.append("{R[").append(paramName).append("]}");
            
            lastEnd = matcher.end();
        }
        sb.append(input.substring(lastEnd));
        return sb.toString();
    }
}