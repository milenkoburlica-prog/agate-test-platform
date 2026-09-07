package at.co.svc.aga.transformator.utils;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import at.co.svc.aga.transformator.dto.StepValueDetails;
import at.co.svc.aga.transformator.dto.Module;


public class MigationPhase2Utils {
    /**     * POMOĆNA METODA - DODAJ JE UNUTAR KLASE ToscaParserPhase2
     */
    /**
     * Rekurzivno mapira JSON polja u Tosca buffere.
     * Forsira parametrizaciju čak i ako su u Tosci upisane fiksne vrednosti (poput True/False).
     */
    public static void mapJsonVariables(String path, JsonNode source, ObjectNode target, Map<String, StepValueDetails> stepValues) {
        if (source == null || !source.isObject()) return;

        Iterator<Map.Entry<String, JsonNode>> fields = source.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            String currentPath = path.isEmpty() ? key : path + "." + key;

            if (value.isObject()) {
                ObjectNode newNode = target.putObject(key);
                mapJsonVariables(currentPath, value, newNode, stepValues);
            } else if (value.isArray()) {
                target.set(key, value);
            } else {
                // --- KLJUČNA LOGIKA ZA PARAMETRIZACIJU ---

                // 1. Proveravamo da li u Tosca Step-u postoji vrednost za ovaj ključ
                String toscaValue = null;
                if (stepValues != null) {
                    for (Map.Entry<String, StepValueDetails> sv : stepValues.entrySet()) {
                        if (sv.getKey().equalsIgnoreCase(key)) {
                            toscaValue = sv.getValue().getValue();
                            break;
                        }
                    }
                }

                // 2. Određivanje šta ide u request.json
                if (toscaValue != null) {
                    if (toscaValue.startsWith("{E[")) {
                        // Ako je environment varijabla (npr. serverip), nju ostavljamo direktno u JSON-u
                        target.put(key, toscaValue);
                    } 
                    else if (toscaValue.startsWith("{B[")) {
                        // Ako je tester već u Tosci koristio buffer, samo ga prosledi
                        target.put(key, toscaValue);
                    } 
                    else {
                        // AKO JE FIKSNA VREDNOST (npr. "baseContact", "12345", "True")
                        // U request.json stavljamo referencu na buffer.
                        // Ime buffera mora biti isto kao ono koje smo ispisali u YAML-u (obično key)
                        target.put(key, "{B[" + key + "]}");
                    }
                } else {
                    // Ako polje uopšte ne postoji u Step-u, ali postoji u Modulu (source)
                    // Možemo ostaviti originalnu vrednost ili takođe staviti buffer kao fallback
                    target.put(key, "{B[" + key + "]}");
                }
            }
        }
    }
        
    /**
     * Ova metoda služi da izvuče Endpoint ako ti zatreba prava vrednost, 
     * mada u metadata.json trenutno forsiramo {B[Endpoint]} placeholder.
     */
    public static String getEndpointFromModule(Module module) {
        if (module == null) return "{B[Endpoint]}";
        
        String explicitConn = module.getAttributes().get("ExplicitConnection");
        if (explicitConn != null && !explicitConn.isEmpty()) {
            try {
                String decoded = new String(java.util.Base64.getDecoder().decode(explicitConn));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(decoded);
                
                for (JsonNode node : root) {
                    if ("Endpoint".equals(node.get("Key").asText())) {
                        return node.get("Value").asText();
                    }
                }
            } catch (Exception e) {
                // Tiho nastavi ako dekodiranje ne uspe
            }
        }
        return "{B[Endpoint]}";
    }    
    
    // Pomoćna metoda da izbegneš NullPointerException i Case probleme kod mapa
    public static String getMapValueIgnoreCase(Map<String, StepValueDetails> map, String key) {
        for (String k : map.keySet()) {
            if (k.equalsIgnoreCase(key)) {
                return map.get(k).getValue();
            }
        }
        return "";
    }

    
}
