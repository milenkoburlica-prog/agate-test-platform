package at.co.svc.agate.core.dsl.resolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.dsl.utils.CsvLoader;
import at.co.svc.agate.core.env.EnvironmentManager;

/**
 * Resolves placeholders within YAML test definitions.
 * Supports:
 * {B[var]} - Base/Local variables
 * {T[table.column]} - Table-driven data from CSV files
 * {E[path]} - Environment/System configurations
 * {R[var]} - Reusable parameters (from CALL step)
 * {DATE[][][format]} - Tosca compatible dynamic dates
 */
public class YamlPlaceholderResolver {

    private static final Pattern B_PATTERN = Pattern.compile("\\{B\\[(.+?)]}");
    private static final Pattern T_PATTERN = Pattern.compile("\\{T\\[(.+?)]}");
    private static final Pattern E_PATTERN = Pattern.compile("\\{E\\[(.+?)]}");
    private static final Pattern R_PATTERN = Pattern.compile("\\{R\\[(.+?)]}");
    private final Map<String, Set<String>> warnedPlaceholders = new HashMap<>();

    /**
     * PROŠIRENA METODA: Sada prima i TestStep kako bi mogla da dohvati R varijable.
     */
    public String resolve(TestCase tc,
                          String value,
                          Map<String, Object> variables,
                          String yamlPath,
                          int stepIndex,
                          String originalAction,
                          TestStep currentStep) {

        if (value == null) return null;

        String result;
        try {
            Map<String, String> vars = new HashMap<>();
            if (variables != null) {
                variables.forEach((k, v) -> {
                    if (k != null && v != null) {
                        vars.put(k, v.toString());
                    }
                });
            }

            result = value;
            if ((result != null) && ("".equals(result))) {
                return result;
            }

            // Redosled je bitan: prvo R, pa onda ostali
            result = resolveR(result, currentStep);
            result = resolveB(result, vars);
            result = resolveT(tc, result, yamlPath, stepIndex, originalAction);
            result = resolveE(tc, result, stepIndex, originalAction);
            //result = resolveD(result);

            
            // 2. >>> POPRAVKA: Ako je R parametar u sebi krio B, T ili E tagove, 
            // propuštamo ih ponovo kroz rezoluciju da se i oni zamene prave vrednosti
            if (result != null && result.contains("{")) {
                result = resolveB(result, vars);
                result = resolveT(tc, result, yamlPath, stepIndex, originalAction);
                result = resolveE(tc, result, stepIndex, originalAction);
            }        
//            if ((value!=null) && (value.contains("{DATE"))) {
//                    result = DateNormalizer.normalize(value);                
//            }
            result = resolveToscaDate(result, tc, stepIndex, originalAction); // DODATO ZA TOSCA DATE
            result = resolveD(result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new RuntimeException();
        }

        return result;
    }

    /**
     * Overload metode radi kompatibilnosti sa starim pozivima koji nemaju currentStep.
     */
    public String resolve(TestCase tc, String value, Map<String, Object> variables, 
                          String yamlPath, int stepIndex, String originalAction) {
        return resolve(tc, value, variables, yamlPath, stepIndex, originalAction, null);
    }

    /**
     * Resolves Tosca dynamic dates like {DATE[][][yyyy-MM-ddTHH:mm:ss]}
     */
    /**
     * Resolves Tosca dynamic dates like {DATE[][][yyyy-MM-ddTHH:mm:ss]}
     */
    private String resolveToscaDate(String value, TestCase tc, int stepIndex, String originalAction) {
        if (value == null || !value.toUpperCase().contains("{DATE")) {
            return value;
        }
        // Pozivamo našu novu pomoćnu klasu
        return ToscaDateResolver.resolve(value);
        
    }

    
    private String resolveR(String value, TestStep currentStep) {
        if (currentStep == null || currentStep.getParameters() == null) {
            return value;
        }
        Matcher matcher = R_PATTERN.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object paramValue = currentStep.getParameters().get(paramName);
            if (paramValue != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(paramValue.toString()));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveB(String value, Map<String, String> variables) {
        Matcher matcher = B_PATTERN.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object varObj = variables.get(varName);
            if (varObj != null) {
                String varValue = varObj.toString();
                matcher.appendReplacement(sb, Matcher.quoteReplacement(varValue));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveT(TestCase tc, String value, String yamlPath, int stepIndex, String originalAction) {
        Matcher matcher = T_PATTERN.matcher(value);
        StringBuilder sb = new StringBuilder();
        String baseDir = "";
        if (yamlPath != null) {
            int lastSlash = Math.max(yamlPath.lastIndexOf("/"), yamlPath.lastIndexOf("\\"));
            if (lastSlash != -1) {
                baseDir = yamlPath.substring(0, lastSlash);
            }
        }
        while (matcher.find()) {
            String fullPath = matcher.group(1);
            String replacement = null;
            try {
                String[] parts = fullPath.split("\\.");
                if (parts.length == 2) {
                    String tableName = parts[0];
                    String columnName = parts[1];
                    Object tcIdObj = tc.getVariables().get("TC_ID");
                    if (tcIdObj != null) {
                        String tcId = tcIdObj.toString();
                        String csvPath = baseDir + "/variables/" + tableName + ".csv";
                        List<Map<String, String>> dataRows = CsvLoader.load(csvPath);
                        if (dataRows != null) {
                            replacement = dataRows.stream()
                                    .filter(row -> {
                                        String idValueInRow = row.entrySet().stream()
                                                .filter(entry -> entry.getKey().equalsIgnoreCase("id") 
                                                              || entry.getKey().equalsIgnoreCase("TC_ID"))
                                                .map(Map.Entry::getValue)
                                                .findFirst()
                                                .orElse(null);
                                        return tcId.equalsIgnoreCase(idValueInRow);
                                    })
                                    .map(row -> row.entrySet().stream()
                                                .filter(entry -> entry.getKey().equalsIgnoreCase(columnName))
                                                .map(Map.Entry::getValue)
                                                .findFirst()
                                                .orElse(null))
                                    .findFirst()
                                    .orElse(null);
                        }
                    }
                }
            } catch (Exception e) { }
            if (replacement != null) {
                replacement = resolveD(replacement);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } else {
                logWarning(tc.getName(), stepIndex, "{T[" + fullPath + "]}", originalAction);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveE(TestCase tc, String value, int stepIndex, String originalAction) {
        Matcher matcher = E_PATTERN.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String path = matcher.group(1);
            Object replacement = null;
            if (path.startsWith("env.")) {
                replacement = EnvironmentManager.getEnvValue(path.substring(4));
            } else if (path.startsWith("users.")) {
                replacement = EnvironmentManager.getReaderValue(path.substring(6));
            }
            if (replacement != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String resolveD(String value) {
        if (value == null) return null;
        if (value.contains("{D")) return value;

        // 1. Obrada {RND[n]} direktiva pomoću RegEx-a
        // RegEx hvata "{RND[" pa grupu od jedne ili više cifara (\\d+) pa "]}"
        Pattern rndPattern = Pattern.compile("\\{RND\\[(\\d+)\\]\\}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = rndPattern.matcher(value);
        
        // StringBuffer koristimo jer Matcher zahteva njega za appendReplacement
        StringBuilder resultSb = new StringBuilder(); 
        Random random = new Random();

        while (matcher.find()) {
            // Izvlačimo broj 'n' (broj cifara) iz prve zagrade u RegEx-u
            int length = Integer.parseInt(matcher.group(1));
            
            if (length > 0) {
                StringBuilder randomNumberStr = new StringBuilder();
                
                // Prva cifra ne sme biti 0 da bi broj zadržao tačnu dužinu (npr. da ne bude 034 umesto 3 cifre)
                randomNumberStr.append(random.nextInt(9) + 1); 
                
                // Generišemo ostale cifre (od 0 do 9)
                for (int i = 1; i < length; i++) {
                    randomNumberStr.append(random.nextInt(10));
                }
                
                // Menjamo pronađeni {RND[n]} sa generisanim brojem
                matcher.appendReplacement(resultSb, randomNumberStr.toString());
            } else {
                // Ako je prosleđeno {RND[0]}, menjamo ga praznim stringom
                matcher.appendReplacement(resultSb, "");
            }
        }
        matcher.appendTail(resultSb);
        value = resultSb.toString();

        // 2. Pozivamo tvoj parser za preostale direktive
        Object parsedValue = DataValueParser.parse(value);
        
        return parsedValue != null ? parsedValue.toString() : "";
    }
    
    

    private void logWarning(String testCaseName, int stepIndex, String placeholder, String originalAction) {
        String key = testCaseName + "_STEP_" + stepIndex;
        warnedPlaceholders.computeIfAbsent(key, k -> new HashSet<>());
        Set<String> warned = warnedPlaceholders.get(key);
        if (!warned.contains(placeholder)) {
            System.out.printf("[WARN] TC=%s Step %03d -> Placeholder %s not resolved in action: %s%n",
                    testCaseName, stepIndex, placeholder, originalAction);
            warned.add(placeholder);
        }
    }
}