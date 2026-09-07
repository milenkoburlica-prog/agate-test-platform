package at.co.svc.tosca.main;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import at.co.svc.tosca.tsu.cleaner.ReusableDependencyAnalyzer;



public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            return;
        }

        String jsonDir = "jsonOut";
        String migrationDir = "migration\\data";
        String command = args[0];

        switch (command.toLowerCase()) {
            case "migrate":
                if (args.length < 3) {
                    System.err.println("[Error] Missing arguments for 'migrate'. Expected: migrate <appID> <baseFileName>");
                    System.exit(1);
                }
                // Kept exactly as it was originally
                MainMigrationApp.start(args[1], jsonDir, args[2], migrationDir);
                break;

            case "clean":
                if (args.length < 4) {
                    System.err.println("[Error] Missing arguments for 'clean'. Expected: clean <appName> <template.yaml> <template.csv>");
                    System.out.println("Example: clean MUHI absolutesBeschaeftigungsverbotEinmelden.yaml TS_absolutesBeschaeftigungsverbotEinmelden.csv");
                    System.exit(1);
                }

                String appName = args[1];
                String yamlFileName = args[2];
                String csvFileName = args[3];

                System.out.println("\n>>> PHASE 1: Running TestSanitizerApp (Sanitizing CSV keys and YAML conditions)...");
                String userDir = System.getProperty("user.dir");
                Path basePath = Paths.get(userDir, "migration", "data", appName, "template");
                Path yamlPath = basePath.resolve(yamlFileName);
                Path csvPath = basePath.resolve(csvFileName);
                
                // Directly calling the static method from TestSanitizerApp
                TestSanitizerApp.sanitizeTestFiles(csvPath, yamlPath);
                TestSanitizerApp.sanitizeTestFiles2(yamlPath);

                System.out.println("\n>>> PHASE 2: Running ReusableDependencyAnalyzer (Cleaning up unused files/folders from disk)...");
                String analyzerPath = userDir + File.separator + "migration" + File.separator + "data" + File.separator + appName;
                
                // Directly initializing and invoking the dependency analyzer
                ReusableDependencyAnalyzer analyzer = new ReusableDependencyAnalyzer(analyzerPath);
                analyzer.analyze();
                break;

            default:
                System.out.println("[Error] Unknown command: " + command);
                printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java -jar <jarName> migrate <appID> <baseFileName>");
        System.out.println("  java -jar <jarName> clean <appName> <template.yaml> <template.csv>");
    }
}
