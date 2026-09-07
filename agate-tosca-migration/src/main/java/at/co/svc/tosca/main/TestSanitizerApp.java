package at.co.svc.tosca.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.co.svc.aga.transformator.utils.MigrationLog;

public class TestSanitizerApp {

    public static void main(String[] args) {
        if (args.length < 3) {
            MigrationLog.info("====== PROGRAM USAGE ======");
            MigrationLog.info("Required arguments: <app> <template.yaml> <template.csv>");
            MigrationLog.info("Example: MUHI absolutesBeschaeftigungsverbotEinmelden.yaml TS_absolutesBeschaeftigungsverbotEinmelden.csv");
            MigrationLog.info("===============================");
            System.exit(1);
        }

        String app = args[0];
        String yamlFileName = args[1];
        String csvFileName = args[2];

        String userDir = System.getProperty("user.dir");

        Path basePath = Paths.get(userDir, "migration", "data", app, "template");
        
        Path yamlPath = basePath.resolve(yamlFileName);
        Path csvPath = basePath.resolve(csvFileName);

        MigrationLog.info("======================================================================");
        MigrationLog.info("[SANITIZER] Starting file sanitization for application: " + app);
        MigrationLog.info("[SANITIZER] Project working directory: " + userDir);
        MigrationLog.info("[SANITIZER] CSV file:  " + csvPath.toAbsolutePath());
        MigrationLog.info("[SANITIZER] YAML file: " + yamlPath.toAbsolutePath());
        MigrationLog.info("======================================================================");
        
        try {
            if (!Files.exists(csvPath)) {
                MigrationLog.error("CSV file does not exist at the specified path!");
                System.exit(1);
            }
            if (!Files.exists(yamlPath)) {
                MigrationLog.error("YAML template does not exist at the specified path!");
                System.exit(1);
            }

            sanitizeTestFiles(csvPath, yamlPath);
            sanitizeTestFiles2(yamlPath);

            MigrationLog.success("Sanitization completed successfully for " + app + "!");
        } catch (Exception e) {
            MigrationLog.error("An error occurred during sanitization: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String sanitizeKey(String key) {
        if (key == null) return null;

        String clean = key;

        clean = clean.replace("ä", "ae").replace("ö", "oe").replace("ü", "ue")
                     .replace("Ä", "Ae").replace("Ö", "Oe").replace("Ü", "Ue")
                     .replace("ß", "ss");

        clean = clean.replace("-", "").replace("/", "").replace("(", "").replace(")", "");
        clean = clean.replaceAll("\\s+", "");

        return clean;
    }

    public static void sanitizeTestFiles(Path csvPath, Path yamlTemplatePath) throws IOException {
        // --- PHASE 1: Read the CSV and build the replacement map ---
        List<String> csvLines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        if (csvLines.isEmpty()) return;

        Map<String, String> replacementMap = new LinkedHashMap<>();
        List<String> updatedCsvLines = new ArrayList<>();
        
        for (String line : csvLines) {
            if (line.trim().isEmpty()) {
                updatedCsvLines.add(line);
                continue;
            }
            String[] parts = line.split(";", -1); 
            String originalKey = parts[0].trim();
            if (!originalKey.isEmpty() && !originalKey.startsWith("#")) {
                String cleanKey = sanitizeKey(originalKey);
                replacementMap.put(originalKey, cleanKey);
                parts[0] = cleanKey;
            }
            updatedCsvLines.add(String.join(";", parts));
        }

        List<Map.Entry<String, String>> sortedReplacements = new ArrayList<>(replacementMap.entrySet());
        sortedReplacements.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));

        Files.write(csvPath, updatedCsvLines, StandardCharsets.UTF_8);
        MigrationLog.info("CSV file sanitized successfully.");

        // --- PHASE 2: Read the YAML template as plain text and replace keys ---
        String yamlContent = Files.readString(yamlTemplatePath, StandardCharsets.UTF_8);

        int changedKeysCount = 0;
        for (Map.Entry<String, String> entry : sortedReplacements) {
            String oldKey = entry.getKey();
            String newKey = entry.getValue();
            if (oldKey.equals(newKey)) continue;
            if (yamlContent.contains(oldKey)) {
                yamlContent = yamlContent.replace(oldKey, newKey);
                changedKeysCount++;
            }
        }

        // --- PHASE 3: Normalize condition lines ---
        String[] lines = yamlContent.split("\\r?\\n");
        List<String> finalizedLines = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("condition:") || trimmed.startsWith("- condition:")) {
                int colonIndex = line.indexOf(":");
                String prefix = line.substring(0, colonIndex + 1);
                String value = line.substring(colonIndex + 1).trim();

                if (!value.isEmpty() && !"\"\"".equals(value) && !"''".equals(value)) {
                    // Remove surrounding quotes if present
                    if (value.startsWith("\"") || value.startsWith("'")) value = value.substring(1);
                    if (value.endsWith("\"") || value.endsWith("'")) value = value.substring(0, value.length() - 1);

                    // Wrap the value in single quotes for the second sanitization pass
                    line = prefix + " '" + value + "'";
                }
            }
            finalizedLines.add(line);
        }

        Files.writeString(yamlTemplatePath, String.join("\n", finalizedLines), StandardCharsets.UTF_8);
        MigrationLog.info("First sanitization phase completed. Changed keys: " + changedKeysCount);
    }
    public static void sanitizeTestFiles2(Path yamlTemplatePath) throws IOException {
        List<String> lines = Files.readAllLines(yamlTemplatePath, StandardCharsets.UTF_8);
        List<String> correctedLines = new ArrayList<>();
        boolean secondaryModificationDone = false;

        for (String line : lines) {
            String trimmed = line.trim();

            // --- STEP 0: Remove hidden NBSP characters (\u00A0) from the entire line ---
            if (line.contains("\u00A0")) {
                line = line.replace("\u00A0", " ");
                secondaryModificationDone = true;
            }
            
            if (trimmed.startsWith("condition:") || trimmed.startsWith("- condition:")) {
                int colonIndex = line.indexOf(":");
                String prefix = line.substring(0, colonIndex + 1);
                String value = line.substring(colonIndex + 1).trim();

                // --- Remove completely empty conditions ---
                // If the condition is empty, '', "", or {}, remove the entire line
                if (value.isEmpty() || "''".equals(value) || "\"\"".equals(value) || "{}".equals(value)) {
                    MigrationLog.debug("Removed empty condition: " + trimmed);
                    secondaryModificationDone = true;
                    continue; // Skip this line so it is removed from correctedLines
                }

                // 1. Sicheres Entpacken: NUR die äußeren umschließenden Paare entfernen
                if (value.startsWith("'") && value.endsWith("'") && value.length() > 1) {
                    value = value.substring(1, value.length() - 1).trim();
                }
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
                    value = value.substring(1, value.length() - 1).trim();
                }
                
                // Falls durch doppelte Ausführung noch ein Paar vorhanden ist
                if (value.startsWith("'") && value.endsWith("'") && value.length() > 1) {
                    value = value.substring(1, value.length() - 1).trim();
                }

                // Check again in case removing the quotes leaves an empty value
                if (value.isEmpty()) {
                    MigrationLog.debug("Removed empty condition after stripping quotes: " + trimmed);
                    secondaryModificationDone = true;
                    continue;
                }

                // 2. Gezielte Säuberung NUR für CardToken-Variablen (falls vorhanden)
                if (value.contains("CardToken")) {
                    value = value.replace("''CardToken.", "CardToken.");
                    value = value.replace("'CardToken.", "CardToken.");
                    value = value.replace(".SVNRKarte'", ".SVNRKarte");
                    value = value.replace(".VPNRKarte'", ".VPNRKarte");
                    value = value.replace(".TokenWert'", ".TokenWert");
                    
                    // Allgemeine Glättung für den CardToken-Ausdruck
                    value = value.replace("'''", "");
                    value = value.replace("''", "");
                }

                // 3. Problem mit unvollständigen oder verunstalteten {NULL}-Ausdrücken beheben
                value = value.replaceAll("==\\s*\"*\\{NULL\\}\"*", "== NULL");
                value = value.replaceAll("=\\s*\"*\\{NULL\\}\"*", "== NULL");

                // --- STEP 3.5: Escape inner quotes (fix for 'VPVP') ---
                if (value.contains("'")) {
                    value = value.replace("''", "##DOUBLE_APOSTROPHE##");
                    value = value.replace("'", "''");
                    value = value.replace("##DOUBLE_APOSTROPHE##", "''");
                }

                // 4. Den bereinigten Ausdruck sicher in EINFACHE äußere Anführungszeichen einschließen
                String newLine = prefix + " '" + value.trim() + "'";
                
                if (!line.equals(newLine)) {
                    secondaryModificationDone = true;
                    line = newLine;
                }
            }
            correctedLines.add(line);
        }

        if (secondaryModificationDone) {
            Files.write(yamlTemplatePath, correctedLines, StandardCharsets.UTF_8);
            MigrationLog.info("Second sanitization phase (sanitizeTestFiles2) completed successfully.");
        }
    } 

}