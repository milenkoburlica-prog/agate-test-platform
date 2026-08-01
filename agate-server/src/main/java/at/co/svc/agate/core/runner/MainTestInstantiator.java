package at.co.svc.agate.core.runner;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import at.co.svc.agate.core.dsl.register.YamlTestInstantiator;
import at.co.svc.agate.core.dsl.utils.ConsoleColors;

public class MainTestInstantiator {

    public static void main(String[] args) {
        String appName = "MUHI1";
//        String templateFile = "meldungAnlegen_V8_SYST_AUT1.yaml";
//        String dataFile = "T_AUM_meldungAnlegen_V8_SYST_AUT1.csv"; // Tvoj novi format

        String templateFile = "anspruchPruefen.yaml";
        String dataFile = "MUHI1_T_anspruchPruefen.csv"; // Tvoj novi format

        start(appName, templateFile, dataFile);
    }

    private static void start(String appName, String templateFile, String dataFile) {
        System.out.println("=".repeat(80));
        System.out.println("             BATCH TEST CASE INSTANTIATION");
        System.out.println("=".repeat(80));

        try {
            String appPath = "data/" + appName;
            cleanupOldInstances(appPath);

            // Pokretanje generatora (on piše fajlove tamo gde mu je definisano)
            YamlTestInstantiator instantiator = new YamlTestInstantiator();
            instantiator.instantiate(appName, templateFile, dataFile);

            // === POGODAK U METU: Čišćenje fajlova direktno u agate-serveru ===
            String agateServerPath = null; //"C:\\work\\projects\\agate-studio\\agate-server\\data\\" + appName;
            agateServerPath = System.getProperty("user.dir") + "\\data\\" + appName;
            sanitizeAgateServerInstances(agateServerPath);

            System.out.println("=".repeat(80));
            System.out.println("  STATUS : " + ConsoleColors.GREEN + "ALL CASES GENERATED & CLEANED IN AGATE-SERVER" + ConsoleColors.RESET);
        } catch (Exception e) {
            System.err.println("\n" + ConsoleColors.RED + "FATAL ERROR:" + ConsoleColors.RESET);
            e.printStackTrace();
            System.exit(1);
        }
    }
    /**
     * Pronalazi i čisti sve generisane Instance_*.yaml fajlove direktno u agate-server projektu.
     */
    private static void sanitizeAgateServerInstances(String serverPath) {
        Path path = Paths.get(serverPath);
        if (!Files.exists(path)) {
            System.out.println("[WARNING] Putanja za agate-server ne postoji: " + serverPath);
            return;
        }

        //System.out.println("[INFO] Otpočinjem čišćenje rezultujućih fajlova u agate-serveru: " + serverPath);
        
        try (var stream = Files.walk(path, 1)) {
            List<Path> instances = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith("Instance_") && p.getFileName().toString().endsWith(".yaml"))
                    .toList();

            for (Path instancePath : instances) {
                sanitizeYamlFile(instancePath); // Koristi onu istu sanitizeYamlFile metodu od malopre
            }
            System.out.println("[INFO] Agate-server instance cleanup finished successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to clean up agate-server files: " + e.getMessage());
        }
    }
    /**
     * METODA KOJA JE FALILA: Čisti 'condition:' linije od trostrukih i duplih jednostrukih navodnika.
     */
    private static void sanitizeYamlFile(Path filePath) {
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            List<String> sanitizedLines = new ArrayList<>();
            boolean fileModified = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("condition:")) {
                    int colonIndex = line.indexOf(":");
                    String prefix = line.substring(0, colonIndex + 1);
                    String value = line.substring(colonIndex + 1).trim();

                    // Ako je uslov prazan, izbaci liniju
                    if (value.isEmpty() || "\"\"".equals(value) || "''".equals(value)) {
                        fileModified = true;
                        continue; 
                    }

                    // Ako je već ispravno obmotan duplim navodnicima, preskoči
                    if (value.startsWith("\"") && value.endsWith("\"") && !value.contains("'''")) {
                        sanitizedLines.add(line);
                        continue;
                    }

                    // 1. Skidanje spoljašnjih trostrukih ili običnih navodnika
                    if (value.startsWith("'''") && value.endsWith("'''") && value.length() > 3) {
                        value = value.substring(3, value.length() - 3);
                    } else if (value.startsWith("'") && value.endsWith("'") && value.length() > 1) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    // 2. Korekcija duplih jednostrukih navodnika unutar izraza ('' -> ')
                    value = value.replace("''", "'");
                    
                    // 3. Obmotavanje u regularne duple navodnike
                    String newLine = prefix + " \"" + value + "\"";
                    line = newLine;
                    fileModified = true;
                }
                sanitizedLines.add(line);
            }

            if (fileModified) {
                Files.write(filePath, sanitizedLines, StandardCharsets.UTF_8);
                System.out.println("[SANITY] Uspešno očišćen fajl: " + filePath.getFileName());
            }
        } catch (Exception e) {
            System.err.println("[WARNING] Greška prilikom sanacije fajla " + filePath.getFileName() + ": " + e.getMessage());
        }
    }
    /**
     * Removes all existing 'instance_*.yaml' files in the application directory
     * to ensure a clean state before generating new ones.
     */
    private static void cleanupOldInstances(String appPath) throws Exception {
        Path path = Paths.get(appPath);
        if (!Files.exists(path)) return;

        System.out.println("[INFO] Cleaning up old instances in: " + appPath);
        
        Files.walk(path, 1) // Only look at the root of the app folder
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().startsWith("instance_") && p.getFileName().toString().endsWith(".yaml"))
                .map(Path::toFile)
                .forEach(File::delete);
                
        System.out.println("[INFO] Cleanup finished.");
    }
}