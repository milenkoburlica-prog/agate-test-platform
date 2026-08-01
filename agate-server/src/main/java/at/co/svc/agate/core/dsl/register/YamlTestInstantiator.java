package at.co.svc.agate.core.dsl.register;

import java.io.BufferedReader;

import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.Paths;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Set;

import java.util.TreeSet;

import java.util.regex.Pattern;

import org.apache.commons.jexl3.JexlBuilder;

import org.apache.commons.jexl3.JexlContext;

import org.apache.commons.jexl3.JexlEngine;

import org.apache.commons.jexl3.JexlExpression;

import org.apache.commons.jexl3.MapContext;

import org.yaml.snakeyaml.DumperOptions;

import org.yaml.snakeyaml.Yaml;

public class YamlTestInstantiator {

    private static class TCObject {

        private final String testId;

        private final Map<String, String> variables = new HashMap<>();

        public TCObject(String testId) {
            this.testId = testId;
        }

        public void addVariable(String key, String value) {
            variables.put(key, value);
        }

        public String getTestId() {
            return testId;
        }

        public Map<String, String> getVariables() {
            return variables;
        }

    }

    public void instantiate(String appName, String templateFileName, String csvFileName) throws Exception {

        String appPath = "data/" + appName;

        String templatePath = appPath + "/template/" + templateFileName;

        String csvPath = System.getProperty("user.dir") + "/" + appPath + "/template/" + csvFileName;

        List<List<String>> table = new ArrayList<>();

        int expectedColumnCount = -1;

        try (BufferedReader br = Files.newBufferedReader(Paths.get(csvPath), java.nio.charset.StandardCharsets.UTF_8)) {

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty())
                    continue;

                String[] values = line.split(";", -1);

                List<String> row = new ArrayList<>();

                for (String val : values) {

                    String cleanedVal = val;

                    // 1. Zameni duple dvostruke navodnike sa jednim jednostrukim

                    cleanedVal = cleanedVal.replace("\"\"", "\"");

                    // 2. Ukloni preostale vodeće i prateće navodnike ako ih je ostalo nakon Excel
                    // izvoza

                    cleanedVal = cleanedVal.replaceAll("^\"|\"$", "");

                    row.add(cleanedVal);

                }

                if (expectedColumnCount == -1)
                    expectedColumnCount = row.size();

                while (row.size() < expectedColumnCount)
                    row.add("");

                table.add(row);

            }

        }

        // Broj kolona definisan prvim redom

        int expectedColumnCountX = table.get(0).size();

        // Lista indeksa kolona koje treba obrisati

        List<Integer> columnsToRemove = new ArrayList<>();

        List<Integer> rowsToRemove = new ArrayList<>();

        // Prolazimo kroz svaku kolonu

        for (int col = 0; col < table.get(0).size(); col++) {

            int junkRow = -1;

            boolean hasJunk = false;

            for (int row = 0; row < table.size(); row++) {

                String val = table.get(row).get(col);

                if (val.equals("-\\??=[]{}@áä")) {

                    hasJunk = true;

                    junkRow = row;

                    break;

                }

            }

            if (hasJunk && table.get(junkRow).size() > expectedColumnCountX) {

                columnsToRemove.add(col);

                rowsToRemove.add(junkRow);

            }

        }

        for (int i = 0; i < columnsToRemove.size(); i++) {

            int col = columnsToRemove.get(i);

            int rw = rowsToRemove.get(i);

            for (int idx = 0; idx < table.size(); idx++) {

                if (idx == rw) {

                    table.get(idx).remove(col);

                }

            }

        }

        List<TCObject> tcs = new ArrayList<>();

        for (int i = 1; i < table.get(0).size(); i++) {

            TCObject tc = new TCObject(table.get(0).get(i));

            for (int j = 1; j < table.size(); j++) {

                String key = table.get(j).get(0);

                String rawVal = table.get(j).get(i);

                String sanitizedVal = sanitizeValue(rawVal);

                tc.addVariable(key, sanitizedVal);

            }

            tcs.add(tc);

        }

        Yaml yaml = new Yaml();

        Object loaded = yaml.load(Files.newInputStream(Paths.get(templatePath)));

        List<Map<String, Object>> templateTestCases;

        if (loaded instanceof Map) {

            templateTestCases = (List<Map<String, Object>>) ((Map<String, Object>) loaded).get("testCases");

        } else {

            templateTestCases = (List<Map<String, Object>>) loaded;

        }

        Map<String, Object> templateTestCase = templateTestCases.get(0);

        Set<String> globalMissingKeys = new TreeSet<>();

        List<Map<String, Object>> finalTestCasesList = new ArrayList<>();

        for (TCObject tc : tcs) {

            String tempString = yaml.dump(templateTestCase);

            Map<String, Object> newTestCase = yaml.load(tempString);

            newTestCase.put("id", tc.getTestId());

            newTestCase.put("stage", "*");

            Set<String> tcMissingKeys = new TreeSet<>();

            replaceXLBuffer(newTestCase, tc, tcMissingKeys);

            processStepsAfterInstanciation(newTestCase, tc, tcMissingKeys);

            if (newTestCase.containsKey("parameters")) {

                Map<String, Object> params = (Map<String, Object>) newTestCase.get("parameters");

                for (String key : tc.getVariables().keySet()) {

                    if (!params.containsKey(key)) {

                        params.put(key, "{NULL}");

                    }

                }

            }

            finalTestCasesList.add(newTestCase);

        }

        Map<String, Object> finalYamlOutput = new LinkedHashMap<>();

        finalYamlOutput.put("testCases", finalTestCasesList);

        DumperOptions options = new DumperOptions();

        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        options.setExplicitStart(false);

        options.setWidth(Integer.MAX_VALUE);

        options.setSplitLines(false);

        Yaml outputYaml = new Yaml(options);

        String rawYaml = outputYaml.dump(finalYamlOutput);

        String formattedYaml = rawYaml.replaceAll("(?m)(  - type:)|(^- id:)", "\n$1$2");

        formattedYaml = formattedYaml.replaceFirst("^\n", "");

        String templateNameOnly = templateFileName.contains(".")

                ? templateFileName.substring(0, templateFileName.lastIndexOf('.'))

                : templateFileName;

        String newFileName = "Instance_" + templateNameOnly + ".yaml";

        Path outputPath = Paths.get(appPath, newFileName);

        Files.writeString(outputPath, formattedYaml);

        System.out.println("[SUCCESS] Created file: " + outputPath);

    }

    private String sanitizeValue(String val) {

        if (val == null)
            return "";

        if ((val.contains("&")) || (val.contains("<")) || (val.contains(">")) || (val.contains("\""))
                || (val.contains("'"))) {

            return val;

        }

        if ((val.contains("?") || val.contains("[]") || val.contains("{}") || val.contains("@"))
                && (!val.contains("DATE"))) {

            return val;

        }

        return val;

    }

    private String xmlEscaping(String val) {

        if (val == null)
            return "";

        return val;

    }

    private void processStepsAfterInstanciation(Map<String, Object> testCase, TCObject tc, Set<String> tcMissingKeys) {

        Boolean CONSOLE_PRINT = true;

        if (!testCase.containsKey("steps"))
            return;

        List<Map<String, Object>> steps = (List<Map<String, Object>>) testCase.get("steps");

        for (int i = steps.size() - 1; i >= 0; i--) {

            Map<String, Object> step = steps.get(i);

            String type = (String) step.getOrDefault("type", "UNKNOWN");

            String op = (String) step.getOrDefault("op", "UNKNOWN");

            if (type.equals("ASSERT") || "ASSERT".equals(op)) {

                boolean shouldRemove = false;

                String stepString = new Yaml().dump(step);

                String reason = "";

                // 1. Check if an explicit missing key is mentioned inside this step

                for (String missingKey : tcMissingKeys) {

                    // Use word boundaries or exact checks so 'AccessException.ExceptionCode'

                    // doesn't accidentally trigger a match on just 'AccessException'

                    java.util.regex.Pattern exactPattern = java.util.regex.Pattern
                            .compile("\\b" + java.util.regex.Pattern.quote(missingKey) + "\\b");

                    if (exactPattern.matcher(stepString).find()) {

                        shouldRemove = true;

                        reason = "Missing or empty CSV reference column: [" + missingKey + "]";

                        break;

                    }

                }

                // 2. Check if the expected value itself resolved to {NULL}

                if (!shouldRemove && step.containsKey("expected")) {

                    Object expectedVal = step.get("expected");

                    if (expectedVal instanceof String && "{NULL}".equals(((String) expectedVal).trim())) {

                        shouldRemove = true;

                        reason = "Expected value resolved to {NULL} from data provider.";

                    }

                }

                // 3. Check if the condition explicitly contains a missing key

                if (!shouldRemove && step.containsKey("condition")) {

                    String cond = (String) step.get("condition");

                    if (cond != null) {

                        for (String missingKey : tcMissingKeys) {

                            java.util.regex.Pattern exactPattern = java.util.regex.Pattern
                                    .compile("\\b" + java.util.regex.Pattern.quote(missingKey) + "\\b");

                            if (exactPattern.matcher(cond).find()) {

                                shouldRemove = true;

                                reason = "Condition relies on missing or empty CSV column: [" + missingKey + "]";

                                break;

                            }

                        }

                    }

                }

                if (shouldRemove) {

                    if (CONSOLE_PRINT) {

                        System.out.println(
                                "[INFO] Removing ASSERT step " + (i + 1) + " from '" + testCase.get("id") + "'");

                        System.out.println("[REASON] " + reason);

                        System.out.println("[REASON] Affected step condition was: " + (String) step.get("condition"));

                    }

                    steps.remove(i);

                    continue;

                }

            }

            // --- PRILAGOĐENA LOGIKA ZA SOAP PARAMETRE SA USLOVIMA ---

            if (type.equals("SOAP") && "EXEC".equals(op)) {

                if (step.containsKey("parameters") && step.get("parameters") instanceof Map) {

                    @SuppressWarnings("unchecked")

                    Map<String, Object> params = (Map<String, Object>) step.get("parameters");

                    // Koristimo pomoćnu mapu da izbegnemo ConcurrentModificationException tokom
                    // iteracije

                    Map<String, Object> updatedParams = new LinkedHashMap<>();

                    for (Map.Entry<String, Object> entry : params.entrySet()) {

                        String key = entry.getKey();

                        Object val = entry.getValue();

                        // Slučaj kada imamo listu uslova pod jednim parametrom (npr. cardToken)

                     // Slučaj kada imamo listu uslova pod jednim parametrom (npr. cardToken)
                        if (val instanceof List) {
                            List<?> conditionList = (List<?>) val;
                            Object resolvedValue = "{NULL}"; // Podrazumevana vrednost ako nijedan uslov ne prođe

                            System.out.println("\n[DEBUG-Klip] Provera liste uslova za parametar: " + key);

                            for (Object condObj : conditionList) {
                                if (condObj instanceof Map) {
                                    Map<?, ?> condMap = (Map<?, ?>) condObj;
                                    String condStr = (String) condMap.get("condition");
                                    Object rawValue = condMap.get("value");

                                    System.out.println("  -> Evaluiram uslov: [" + condStr + "] | Trenutna vrednost 'value' u šablonu: [" + rawValue + "]");

                                    // Evaluacija uslova preko JEXL-a pomoću tvoje isConditionSatisfied metode
                                    if (isConditionSatisfied(condStr, tc, step, CONSOLE_PRINT)) {
                                        resolvedValue = rawValue;
                                        System.out.println("  ✔ USLOV PROŠAO! Izabrana vrednost za " + key + " je: [" + resolvedValue + "]");
                                        break;
                                    } else {
                                        System.out.println("  ❌ Uslov nije ispunjen.");
                                    }
                                }
                            }

                            System.out.println("[DEBUG-Klip] Finalno dodeljen " + key + " u updatedParams: [" + resolvedValue + "]\n");
                            updatedParams.put(key, resolvedValue);
                        }
                        

                        // Standardno procesiranje za obične tekstualne parametre

                        else if (val instanceof String) {

                            String originalValue = (String) val;

                            String escapedValue = xmlEscaping(originalValue);

                            updatedParams.put(key, escapedValue);

                        } else {

                            updatedParams.put(key, val);

                        }

                    }

                    // Pregazimo stare parametre sa novim, očišćenim i evaluiranim vrednostima

                    step.put("parameters", updatedParams);

                }

            }

            String condition = (String) step.get("condition");

            if (CONSOLE_PRINT) {

                System.out.println("condition=" + condition);

            }

            if (condition != null && !condition.trim().isEmpty()) {

                if (!isConditionSatisfied(condition, tc, step, CONSOLE_PRINT)) {

                    steps.remove(i);

                } else {

                    // Ako je uslov uspešno evaluiran kao TAČAN, brišemo ga iz koraka

                    step.remove("condition");

                }

            }

        }

    }

//    private boolean isConditionSatisfied(String condition, TCObject tc, Map<String, Object> step) {

//        if (condition == null || condition.trim().isEmpty()) return true;

//

//        condition = condition.replaceAll("'''([a-zA-Z0-9_.-]+)''", "$1");

//        condition = condition.replaceAll("''([a-zA-Z0-9_.-]+)''", "$1");

//        condition = condition.replaceAll("'([a-zA-Z0-9_.-]+)'", "$1");

//

//        String command = (String) step.get("command");

//        boolean debugThisStep = "reusable.create_cardtoken_svsig_e_card".equals(command);

//

//        if (debugThisStep) {

//            System.out.println("\n======================================================================");

//            System.out.println("[DEBUG CONDITION] Korak: reusable.create_cardtoken_svsig_e_card");

//            System.out.println("[DEBUG CONDITION] Originalni uslov: " + condition);

//        }

//

//        JexlContext context = new MapContext();

//        context.set("null", null);

//        context.set("NULL", null);

//        context.set("true", Boolean.TRUE);

//        context.set("TRUE", Boolean.TRUE);

//        context.set("false", Boolean.FALSE);

//        context.set("FALSE", Boolean.FALSE);

//

//        String processedCondition = condition

//                .replaceAll("(?i)\\s+AND\\s+", " && ")

//                .replaceAll("(?i)\\s+OR\\s+", " || ")

//                .replaceAll("(?i)\\bNULL\\b", "null")

//                .replaceAll("(?i)\\bTRUE\\b", "true")

//                .replaceAll("(?i)\\bFALSE\\b", "false");

//

//        List<String> keys = new ArrayList<>(tc.getVariables().keySet());

//        keys.sort((a, b) -> Integer.compare(b.length(), a.length()));

//

//        Map<String, Object> mappedVariablesForLog = new HashMap<>();

//        int varCounter = 0;

//        

//        for (String originalKey : keys) {

//            String safeJexlVar = "VAR_" + varCounter++;

//            String val = tc.getVariables().get(originalKey);

//            

//            Object contextValue = (val == null || val.trim().isEmpty() || val.equalsIgnoreCase("NULL") || val.equalsIgnoreCase("{NULL}")) ? null : val;

//            context.set(safeJexlVar, contextValue);

//

//            if (processedCondition.contains(originalKey)) {

//                mappedVariablesForLog.put(originalKey + " (" + safeJexlVar + ")", contextValue);

//            }

//            

//            String escapedKey = Pattern.quote(originalKey);

//            processedCondition = processedCondition.replaceAll("(?<![\\w.-])" + escapedKey + "(?![\\w.-])", safeJexlVar);

//        }

//

//        if (debugThisStep) {

//            System.out.println("[DEBUG CONDITION] Prerađeni uslov za JEXL: " + processedCondition);

//            System.out.println("[DEBUG CONDITION] Vrednosti promenljivih iz CSV-a koje se koriste u uslovu:");

//            if (mappedVariablesForLog.isEmpty()) {

//                System.out.println("  -> Nijedan ključ iz CSV-a se ne poklapa sa promenljivama u uslovu!");

//            } else {

//                for (Map.Entry<String, Object> entry : mappedVariablesForLog.entrySet()) {

//                    System.out.println("  • " + entry.getKey() + " = " + (entry.getValue() == null ? "null" : "'" + entry.getValue() + "'"));

//                }

//            }

//        }

//

//        try {

//            JexlEngine jexl = new JexlBuilder().silent(true).strict(false).create();

//            JexlExpression expression = jexl.createExpression(processedCondition);

//            Object result = expression.evaluate(context);

//            

//            boolean finalResult = (result instanceof Boolean) ? (Boolean) result : (result != null);

//            

//            if (debugThisStep) {

//                System.out.println("[DEBUG CONDITION] Rezultat JEXL evaluacije: " + finalResult);

//                System.out.println("======================================================================\n");

//            }

//            

//            return finalResult;

//        } catch (Exception e) {

//            if (debugThisStep) {

//                System.out.println("[DEBUG CONDITION] DOŠLO JE DO GREŠKE: " + e.getMessage());

//                System.out.println("======================================================================\n");

//            }

//            System.err.println("[ERROR] JEXL greška na uslovu [" + condition + "] -> Greška: " + e.getMessage());

//            return false;

//        }

//    }    

    private boolean isConditionSatisfied(String condition, TCObject tc, Map<String, Object> step,
            Boolean CONSOLE_PRINT) {

        if (condition == null || condition.trim().isEmpty())
            return true;

        // 1. Strip external block quotes if they exist

        condition = condition.trim();

        if (condition.startsWith("'''") && condition.endsWith("'''") && condition.length() > 3) {

            condition = condition.substring(3, condition.length() - 3);

        } else if (condition.startsWith("'") && condition.endsWith("'") && condition.length() > 1) {

            condition = condition.substring(1, condition.length() - 1);

        } else if (condition.startsWith("\"") && condition.endsWith("\"") && condition.length() > 1) {

            condition = condition.substring(1, condition.length() - 1);

        }

        // 1. Popravi deformisane ili nezatvorene {NULL oznake iz šablona (npr. '{NULL )

        condition = condition.replaceAll("(?i)'\\{NULL\\b", "null");

        condition = condition.replaceAll("(?i)\\{NULL\\b", "null");

        // 2. Očisti sve zalutale jednostruke navodnike oko naziva promenljivih

        condition = condition.replaceAll("'([a-zA-Z0-9_.-]+)'", "$1");

        condition = condition.replaceAll("([a-zA-Z0-9_.-]+)'", "$1"); // Zaostali desni navodnici

        condition = condition.replaceAll("'([a-zA-Z0-9_.-]+)", "$1"); // Zaostali levi navodnici

        String command = (String) step.get("command");

        boolean debugThisStep = true;

        // 3. Unifikacija svih mogućih varijacija NULL-a i operatora u čist JEXL format

        String processedCondition = condition

                .replaceAll("(?i)'\\{NULL\\}'", "null")

                .replaceAll("(?i)\"\\{NULL\\}\"", "null")

                .replaceAll("(?i)\\{NULL\\}", "null")

                .replaceAll("(?i)\\bNULL\\b", "null")

                .replaceAll("(?i)\\bTRUE\\b", "true")

                .replaceAll("(?i)\\bFALSE\\b", "false")

                .replaceAll("(?i)\\s+AND\\s+", " && ")

                .replaceAll("(?i)\\s+OR\\s+", " || ");

        // 4. UKLANJANJE ZALUTALIH ZAGRADA I NAVODNIKA SA KRAJA IZRAZA

        processedCondition = processedCondition.trim();

        // Ako se izraz završava sa }, a to nije deo regularne promenljive, obriši je

        if (processedCondition.endsWith("}")) {

            processedCondition = processedCondition.substring(0, processedCondition.length() - 1).trim();

        }

        // Ako je na kraju ostao usamljeni navodnik, obriši i njega

        if (processedCondition.endsWith("'") && !processedCondition.startsWith("'")) {

            processedCondition = processedCondition.substring(0, processedCondition.length() - 1).trim();

        }

        JexlContext context = new MapContext();

        context.set("null", null);

        context.set("true", Boolean.TRUE);

        context.set("false", Boolean.FALSE);

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[a-zA-Z0-9_.-]+")
                .matcher(processedCondition);

        StringBuilder finalJexlExpression = new StringBuilder();

        int lastEnd = 0;

        int varCounter = 0;

        Map<String, Object> mappedVariablesForLog = new HashMap<>();

        Set<String> csvKeys = tc.getVariables().keySet();

        while (matcher.find()) {

            finalJexlExpression.append(processedCondition, lastEnd, matcher.start());

            String token = matcher.group();

            if (token.equals("null") || token.equals("true") || token.equals("false") || token.matches("\\d+")) {

                finalJexlExpression.append(token);

            }

            else if (csvKeys.contains(token)) {

                String safeJexlVar = "VAR_" + varCounter++;

                String val = tc.getVariables().get(token);

                Object contextValue;

                if (val == null || val.trim().isEmpty() || val.equalsIgnoreCase("NULL")
                        || val.equalsIgnoreCase("{NULL}")) {

                    contextValue = null;

                } else if (val.equalsIgnoreCase("{EMPTY}")) {

                    contextValue = "";

                } else {

                    contextValue = val;

                }

                context.set(safeJexlVar, contextValue);

                finalJexlExpression.append(safeJexlVar);

                mappedVariablesForLog.put(token + " (" + safeJexlVar + ")", contextValue);

            }

            else {

                // Safely encapsulate explicit text constants as strings for JEXL

                finalJexlExpression.append("'").append(token).append("'");

            }

            lastEnd = matcher.end();

        }

        finalJexlExpression.append(processedCondition.substring(lastEnd));

        String finalExpressionStr = finalJexlExpression.toString();

        if (debugThisStep) {

            if (CONSOLE_PRINT) {

                System.out.println("\n--- [CONDITION EVALUATION DETAIL] ---");

                System.out.println("  [RAW TEMPLATE COND] : " + condition);

                System.out.println("  [JEXL ENGINE EXEC]  : " + finalExpressionStr);

            }

        }

        try {

            JexlEngine jexl = new JexlBuilder().silent(true).strict(false).create();

            JexlExpression expression = jexl.createExpression(finalExpressionStr);

            Object result = expression.evaluate(context);

            boolean finalResult = (result instanceof Boolean) ? (Boolean) result : (result != null);

            if (debugThisStep) {

                if (CONSOLE_PRINT) {

                    System.out.println("  [EVALUATION RESULT] : "
                            + (finalResult ? "TRUE (Step will be KEPT)" : "FALSE (Step will be REMOVED)"));

                    System.out.println("-------------------------------------\n");

                }

            }

            return finalResult;

        } catch (Exception e) {

            if (debugThisStep) {

                if (CONSOLE_PRINT) {

                    System.err.println("  [EVALUATION ERROR]  : " + e.getMessage());

                    System.err.println("-------------------------------------\n");

                }

            }

            return false;

        }

    }

    private void replaceXLBuffer(Object obj, TCObject tc, Set<String> missingKeys) {

        if (obj instanceof Map) {

            Map<String, Object> map = (Map<String, Object>) obj;

            for (Map.Entry<String, Object> entry : map.entrySet()) {

                String key = entry.getKey();

                Object val = entry.getValue();

                if (val instanceof String) {

                    String strVal = (String) val;

                    if (strVal.contains("{XL[")) {

                        entry.setValue(processString(strVal, tc, missingKeys));

                    }

                    else if (tc.getVariables().containsKey(key)) {

                        String csvVal = tc.getVariables().get(key);

                        entry.setValue(processValue(csvVal));

                    }

                } else {

                    replaceXLBuffer(val, tc, missingKeys);

                }

            }

        } else if (obj instanceof List) {

            for (Object item : (List<?>) obj)
                replaceXLBuffer(item, tc, missingKeys);

        }

    }

    private String processString(String str, TCObject tc, Set<String> missingKeys) {

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{XL\\[(.*?)\\]\\}");

        java.util.regex.Matcher matcher = pattern.matcher(str);

        StringBuilder sb = new StringBuilder();

        int lastEnd = 0;

        while (matcher.find()) {

            sb.append(str, lastEnd, matcher.start());

            String key = matcher.group(1);

            if (!tc.getVariables().containsKey(key)) {

                missingKeys.add(key);

            }

            String val = tc.getVariables().get(key);

            if (val == null || val.isEmpty() || val.equalsIgnoreCase("NULL")) {

                sb.append("{NULL}");

            } else if (val.equalsIgnoreCase("{EMPTY}")) {

                sb.append("{EMPTY}");

            } else {

                sb.append(val);

            }

            lastEnd = matcher.end();

        }

        sb.append(str.substring(lastEnd));

        return sb.length() == 0 ? "{NULL}" : sb.toString();

    }

    private String processValue(String val) {

        if (val == null || val.isEmpty() || val.equalsIgnoreCase("NULL")) {

            return "{NULL}";

        }

        if (val.equalsIgnoreCase("{EMPTY}")) {

            return "{EMPTY}";

        }

        return val;

    }

}