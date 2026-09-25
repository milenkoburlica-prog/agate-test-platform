package at.co.svc.tosca.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import at.co.svc.aga.transformator.ToscaLibraryMigrator;
import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.utils.MigrationReport;
import at.co.svc.aga.transformator.utils.ToscaValueTranslator;
import at.co.svc.tosca.testcases.FolderPropagationEngine;
import at.co.svc.tosca.testcases.IfPropagationEngine;
import at.co.svc.tosca.testcases.XTestStepExtend;
import at.co.svc.tosca.testcases.XTestStepJsonCompressor;
import at.co.svc.tosca.testcases.XTestStepJsonCompressorStep2;
import at.co.svc.tosca.transformation.ToscaParserPhase1;
import at.co.svc.tosca.transformation.ToscaParserPhase2;
import at.co.svc.tosca.transformation.ToscaParserPhase3;
import at.co.svc.tosca.tsu.del.TCDSheetInstanceLevel1;
import at.co.svc.tosca.tsu.del.TCDSheetRowLevel0;
import at.co.svc.tosca.tsu.del.TCDSheetRowLevel1;
import at.co.svc.tosca.tsu.dto.ToscaNode;
import at.co.svc.tosca.tsu.utils.TSUDivider;
import at.co.svc.tosca.tsu.utils.TSUObjectExtractor;
import at.co.svc.tosca.tsu.utils.TsuExtractor;
import at.co.svc.tosca.util.FileNameSanitizer;

public class MainMigrationApp {

    public static void main(String[] args) throws Exception {

        //-------------------------------------------------------------------
        String appID = "PST";
        System.setProperty("APPLICATION", appID);
        String jsonDir = "jsonOut";
        String baseFileName = "DMP_11_isDMPPatient";
        baseFileName = "MUHI_anspruchPruefen"; 
        //baseFileName = "CRS_V3";
        //baseFileName = "TI of CardtokenValidationS GF";
        String migrationDir = "migration\\\\data";
        baseFileName = "TestScheet_885_Test"; 
        baseFileName = "DMP_11_isDMPPatient";
        baseFileName = "CheckStatus_MUHI_V1";
        baseFileName = "OclMUHI_SS12_Token_Null_SVN-Eingabe_Adresse_min_GF"; 
        baseFileName = "CRS_V3";
        //baseFileName = "GINO LETZTER_KONTAKT Checkup_2";
        baseFileName = "EcSrvFotoauskunftService_SS89";
        baseFileName = "VDAS_Abfrage mit Stichtag (mit KA)";
        //baseFileName = "MUHI_V1_ss12_SYST_AUT1";
        baseFileName = "DMP_11_isDMPPatient";
        baseFileName = "DMP_11_isDMPPatient";
        baseFileName = "T_AUM_meldungenFuerSvPersonSuchen_V8_SYST_AUT1";
        baseFileName = "GF_meldungAnlegen_WithConstraint";
        baseFileName = "TBox_DB_WithConstraint";
        baseFileName = "T_AUM_meldungAnlegen_V8_SYST_AUT1";
        baseFileName = "T_VDAS_retrieveVersichertendatenPerStichtag_syst_aut1";
        baseFileName = "T_MUHI_anspruchPruefen";
        baseFileName = "AUM_meldungAnlegen_short_2";
        baseFileName = "T_AUM_meldungAnlegen_V8_SYST_AUT1";
        //baseFileName = "889-Test";
        baseFileName = "TCD_meldungAnlegen_V8_SYST_AUT1";
        baseFileName = "T_auEndeBearbeiten_V8_SYST_AUT1";
        baseFileName = "TS_absolutesBeschaeftigungsverbotEinmelden";
        baseFileName = "MUHI_SS12_Upload";
        baseFileName = "MUHI1_T_anspruchPruefen";
        baseFileName = "MUHI1_TCD_anspruchPruefen";
        baseFileName = "dmp_11_isDMPPatient";
        baseFileName = "PrKartenStatusManuell";
        baseFileName = "PST-004_Report";
        //-------------------------------------------------------------------

        start(appID, jsonDir, baseFileName, migrationDir);
                
    }
    
    public static void start(String appID, String jsonDir, String baseFileName, String migrationDir)
            throws Exception, IOException {

        MigrationArtifactAudit.Session artifactAudit =
                MigrationArtifactAudit.begin(
                        appID,
                        baseFileName,
                        migrationDir
                );

        boolean migrationSucceeded = false;

        try {

        MigrationReport.reset();
        
        String outputDir = System.getProperty("user.dir") + "\\" + jsonDir ;
        
        String tsuPath = System.getProperty("user.dir") + "\\tsu\\" + baseFileName + ".tsu";

        String tsuName = Paths.get(tsuPath).getFileName().toString().replace(".tsu", "");

        TSUDivider divider = new TSUDivider();
        Map<String, List<ToscaNode>> tsu = divider.start(tsuPath);

        Map<String, List<String>> categoryOutputs = new HashMap<>();

        exportCategory(tsu.get("testcases"), tsuPath, outputDir, "testcases", categoryOutputs);
        exportCategory(tsu.get("reusables"), tsuPath, outputDir, "reusables", categoryOutputs);
        exportCategory(tsu.get("templates"), tsuPath, outputDir, "templates", categoryOutputs);
        exportCategoryTestSheet(tsuPath, tsu.get("tcdsheets"), outputDir, "tcdsheets", categoryOutputs, baseFileName, appID);

        // =====================================================
        // STEP 7 + 8 : FINAL MERGE + PHASE2
        // =====================================================
        mergeCategory(tsuPath, "testcases", categoryOutputs, outputDir);
        mergeCategory(tsuPath, "reusables", categoryOutputs, outputDir);
        mergeCategory(tsuPath, "templates", categoryOutputs, outputDir);
//        mergeCategoryTestSheet(tsuPath, "tcdsheets", categoryOutputs, outputDir);   
        
        // Create tsu json file
        String finalFile = outputDir + "\\" + tsuName + ".json";
        String reusableFile = outputDir + "\\" + tsuName + "_reusables.json";

        TsuExtractor.extractTsuToJson(tsuPath, outputDir);

        System.out.println("✔ FINAL WRITTEN: " + finalFile);
        
        ToscaToAgaPhase1.startMigrationTestcases(appID, baseFileName, migrationDir, outputDir + File.separator);
        List<String> templeteFile = ToscaToAgaPhase1.startMigrationTemplates(appID, baseFileName, migrationDir, outputDir + File.separator);

        List<String> migModules = new ArrayList<String>();
        migModules = ToscaLibraryMigrator.extractedMain(appID, finalFile, reusableFile, migModules);
        
        List<String> usedReusable = new ArrayList<String>();
        List<String> unusedReusable = new ArrayList<String>();
        List<String> nueReusable = new ArrayList<String>();
        processReusableModules(migrationDir,appID, baseFileName, migModules, usedReusable, unusedReusable, nueReusable);
        
        modifyYamlFile(migrationDir, appID, baseFileName);
        
        for (String item : templeteFile) {
            Path path = Paths.get(item);
            String fileName = path.getFileName().toString();
            String baseName = fileName.endsWith(".yaml")
                    ? fileName.substring(0, fileName.length() - 5)
                    : fileName;

            modifyYamlFile(
                    migrationDir,
                    appID + "/template",
                    baseName
            );
        }

        MigrationReport.printSummary();

        migrationSucceeded = true;

        } finally {

            artifactAudit.finish(
                    migrationSucceeded
            );
        }
    }
    // =====================================================
    // EXPORT CATEGORY (adds files to registry)
    // =====================================================
    private static void exportCategory(
            List<ToscaNode> nodes,
            String tsuPath,
            String outputDir,
            String suffix,
            Map<String, List<String>> registry
    ) throws Exception {

        if (nodes == null) return;

        List<String> outputs = new ArrayList<>();

        for (ToscaNode node : nodes) {

            String rootSurrogate = node.surrogate;

            // STEP 1
            TSUObjectExtractor.start(tsuPath, rootSurrogate, outputDir, suffix);

            String extractedFile =
                    outputDir + "\\" + rootSurrogate + "_" + suffix + ".json";

            // STEP 2
            XTestStepExtend.extend(extractedFile, tsuPath, outputDir);
            String extendedFile = extractedFile.replace(".json", "-extended.json");

            // STEP 3
            String tempFile = XTestStepJsonCompressor.compressTemplates(extendedFile, tsuPath);
            XTestStepJsonCompressor.compress(tempFile, tsuPath);

            String compressedFile = tempFile.replace(".json", "-compressed.json");

            // STEP 4
            XTestStepJsonCompressorStep2.compressStep2(compressedFile, tsuPath);
            String step2File = compressedFile.replace(".json", "_step2.json");

            // STEP 5
            String step3File = IfPropagationEngine.processFile(step2File);

            // STEP 6
            String step4File = FolderPropagationEngine.processFile(step3File);

            outputs.add(step4File);
        }

        registry.put(suffix, outputs);
    }
    private static int findLine(List<String> lines, String surrogate) {

        String target = "\"Surrogate\" : \"" + surrogate + "\"";
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains(target)) {
                System.out.println(
                        "[TSU-DEBUG] FOUND surrogate = " + surrogate +
                        " at line = " + i
                );
                return i;
            }
        }

        System.out.println(
                "[TSU-DEBUG] NOT FOUND surrogate = " + surrogate
        );

        return Integer.MAX_VALUE;
    }
    
    
    private static List<String> loadPrettyTsuLines(String tsuPath, ObjectMapper mapper) throws Exception {

        JsonNode tsuRoot;

        try (InputStream fis = new FileInputStream(tsuPath);
             GZIPInputStream gis = new GZIPInputStream(fis)) {

            tsuRoot = mapper.readTree(gis);
        }

        // pretty print into string
        String pretty = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(tsuRoot);

        return pretty.lines().toList();
    }
    
    
    // =====================================================
    // MERGE + PHASE2
    // =====================================================
    private static void mergeCategory(
            String tsuPath,
            String category,
            Map<String, List<String>> registry,
            String outputDir
    ) throws Exception {

        List<String> files = registry.get(category);
        if (files == null || files.isEmpty()) return;

        ObjectMapper mapper = new ObjectMapper();

        // TSU lines (for ordering/debug)
        List<String> tsuLines = loadPrettyTsuLines(tsuPath, mapper);

        class Item {
            JsonNode node;
            int order;
        }

        List<Item> all = new ArrayList<>();

        for (String file : files) {

            String step1 = ToscaParserPhase1.processFile(file, tsuPath);
            String step2 = ToscaParserPhase2.processFile(step1, tsuPath);
            String step3 = ToscaParserPhase3.processFile(step2);

            JsonNode node = mapper.readTree(new File(step3));

            Item item = new Item();
            item.node = node;

            String surrogate = node.path("surrogate").asText();

            int lineIndex = findLine(tsuLines, surrogate);
            item.order = lineIndex;

            all.add(item);
        }

        // SORT BY TSU ORDER
        all.sort(Comparator.comparingInt(i -> i.order));

        // =====================================================
        // FIX: OUTPUT MUST BE ARRAY, NOT OBJECT
        // =====================================================
        ArrayNode merged = mapper.createArrayNode();

        for (Item i : all) {
            merged.add(i.node);
        }

        String tsuName =
                new File(tsuPath).getName().replace(".tsu", "");

        String outFile =
                outputDir + "\\" + tsuName + "_" + category + ".json";

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(outFile), merged);

        System.out.println("✔ FINAL MERGED (ARRAY FORMAT): " + outFile);
    }    

    private static List<String> loadTsuLines(String path) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        try (InputStream fis = new FileInputStream(path);
             GZIPInputStream gis = new GZIPInputStream(fis);
             InputStreamReader isr = new InputStreamReader(gis, java.nio.charset.StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            List<String> lines = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            return lines;
        }
    }
    
    public static void processReusableModules(
            String migrationDir,
            String appID,
            String baseFileName,
            List<String> migratedModules,
            List<String> usedReusable,
            List<String> unusedReusable,
            List<String> neueReusable) {

        String userDir = System.getProperty("user.dir");

        String sourceFolder =
                userDir + "/migration/ref/reusable";

        Set<String> usedSet = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        System.out.println();
        System.out.println("==============================================");
        System.out.println("REUSABLE DEPENDENCY ANALYSIS");
        System.out.println("==============================================");
        System.out.println("userDir          = " + userDir);
        System.out.println("migrationDir     = " + migrationDir);
        System.out.println("appID            = " + appID);
        System.out.println("baseFileName     = " + baseFileName);
        System.out.println("sourceFolder     = " + sourceFolder);
        System.out.println("migratedModules  = " + migratedModules.size());

        // ---------------------------------------------------------
        // 1. Root + template files
        // ---------------------------------------------------------

        List<File> filesToScan = new ArrayList<>();

        Path rootPath =
                Paths.get(userDir, migrationDir, appID);

        File rootYaml =
                rootPath.resolve(baseFileName + ".yaml").toFile();

        filesToScan.add(rootYaml);

        System.out.println();

        System.out.println(
                "[DEPENDENCY-DEBUG] Root YAML = "
                        + rootYaml.getAbsolutePath()
                        + " exists="
                        + rootYaml.exists()
        );

        Path templateDir = rootPath.resolve("template");

        System.out.println(
                "[DEPENDENCY-DEBUG] Template dir = "
                        + templateDir.toAbsolutePath()
                        + " exists="
                        + Files.exists(templateDir)
        );

        if (Files.exists(templateDir)) {

            File[] templates =
                    templateDir.toFile()
                            .listFiles(
                                    (dir, name) ->
                                            name.endsWith(".yaml")
                            );

            if (templates != null) {

                Collections.addAll(
                        filesToScan,
                        templates
                );

                System.out.println(
                        "[DEPENDENCY-DEBUG] Templates found = "
                                + templates.length
                );
            }
        }

        // ---------------------------------------------------------
        // 2. Initial dependencies
        // ---------------------------------------------------------

        for (File f : filesToScan) {

            System.out.println();

            System.out.println(
                    "[DEPENDENCY-DEBUG] Scanning file = "
                            + f.getAbsolutePath()
            );

            if (f.exists()) {

                for (String mod :
                        findCalledModules(
                                f,
                                migrationDir,
                                appID)) {

                    System.out.println(
                            "[DEPENDENCY-DEBUG] CALL resolved = "
                                    + mod
                    );

                    boolean migrated =
                            migratedModules.contains(mod);

                    System.out.println(
                            "[DEPENDENCY-DEBUG] migrated      = "
                                    + migrated
                    );

                    // -------------------------------------------------
                    // CLASSIFICATION TRACE
                    // -------------------------------------------------

                    System.out.println();
                    System.out.println("========== CLASSIFICATION TRACE ==========");
                    System.out.println(
                            "[CLASSIFY-TRACE] dependency = "
                                    + mod
                    );

                    System.out.println(
                            "[CLASSIFY-TRACE] migrated exact match = "
                                    + migrated
                    );

                    if (!migrated) {

                        System.out.println(
                                "[CLASSIFY-TRACE] exact match NOT found"
                        );

                        String dependencyComparable =
                                mod
                                        .replace("\\", "/")
                                        .replace("/", "")
                                        .replace(".", "")
                                        .toLowerCase();

                        System.out.println(
                                "[CLASSIFY-TRACE] comparable dependency = "
                                        + dependencyComparable
                        );

                        System.out.println(
                                "[CLASSIFY-TRACE] checking migrated candidates..."
                        );

                        boolean possibleMatchFound = false;

                        for (String migratedModule : migratedModules) {

                            String migratedComparable =
                                    migratedModule
                                            .replace("\\", "/")
                                            .replace("/", "")
                                            .replace(".", "")
                                            .toLowerCase();

                            if (dependencyComparable.equals(migratedComparable)) {

                                possibleMatchFound = true;

                                System.out.println(
                                        "[CLASSIFY-TRACE] POSSIBLE SAME MODULE = "
                                                + migratedModule
                                );
                            }
                        }

                        if (!possibleMatchFound) {

                            System.out.println(
                                    "[CLASSIFY-TRACE] no comparable migrated module found"
                            );
                        }
                    }

                    System.out.println(
                            "=========================================="
                    );

                    // -------------------------------------------------
                    // ORIGINAL LOGIC
                    // -------------------------------------------------

                    if (migrated && usedSet.add(mod)) {

                        System.out.println(
                                "[DEPENDENCY-DEBUG] -> USED / queued"
                        );

                        queue.add(mod);

                    } else if (!migrated) {

                        System.out.println(
                                "[DEPENDENCY-DEBUG] -> NEW / reference candidate"
                        );

                        if (!neueReusable.contains(mod)) {
                            neueReusable.add(mod);
                        }
                    }
                }

            } else {

                System.err.println(
                        "[DEPENDENCY-WARNING] File does not exist: "
                                + f.getAbsolutePath()
                );
            }
        }

        // ---------------------------------------------------------
        // 3. Recursive discovery
        // ---------------------------------------------------------

        System.out.println();

        System.out.println(
                "[DEPENDENCY-DEBUG] Starting recursive dependency scan..."
        );

        while (!queue.isEmpty()) {

            String currentPath = queue.poll();

            File moduleFile =
                    new File(userDir, currentPath);

            System.out.println();

            System.out.println(
                    "[DEPENDENCY-DEBUG] Recursive module = "
                            + currentPath
            );

            System.out.println(
                    "[DEPENDENCY-DEBUG] Absolute file    = "
                            + moduleFile.getAbsolutePath()
            );

            System.out.println(
                    "[DEPENDENCY-DEBUG] Exists           = "
                            + moduleFile.exists()
            );

            if (moduleFile.exists()) {

                for (String subMod :
                        findCalledModules(
                                moduleFile,
                                migrationDir,
                                appID)) {

                    boolean migrated =
                            migratedModules.contains(subMod);

                    System.out.println(
                            "[DEPENDENCY-DEBUG] nested CALL = "
                                    + subMod
                    );

                    System.out.println(
                            "[DEPENDENCY-DEBUG] migrated    = "
                                    + migrated
                    );

                    // -------------------------------------------------
                    // NESTED CLASSIFICATION TRACE
                    // -------------------------------------------------

                    if (!migrated) {

                        System.out.println();
                        System.out.println("========== NESTED CLASSIFICATION TRACE ==========");

                        System.out.println(
                                "[CLASSIFY-TRACE] nested dependency = "
                                        + subMod
                        );

                        String dependencyComparable =
                                subMod
                                        .replace("\\", "/")
                                        .replace("/", "")
                                        .replace(".", "")
                                        .toLowerCase();

                        boolean possibleMatchFound = false;

                        for (String migratedModule : migratedModules) {

                            String migratedComparable =
                                    migratedModule
                                            .replace("\\", "/")
                                            .replace("/", "")
                                            .replace(".", "")
                                            .toLowerCase();

                            if (dependencyComparable.equals(migratedComparable)) {

                                possibleMatchFound = true;

                                System.out.println(
                                        "[CLASSIFY-TRACE] POSSIBLE SAME MODULE = "
                                                + migratedModule
                                );
                            }
                        }

                        if (!possibleMatchFound) {

                            System.out.println(
                                    "[CLASSIFY-TRACE] no comparable migrated module found"
                            );
                        }

                        System.out.println(
                                "================================================="
                        );
                    }

                    if (migrated && usedSet.add(subMod)) {

                        queue.add(subMod);

                        System.out.println(
                                "[DEPENDENCY-DEBUG] -> nested module queued"
                        );
                    }
                }

            } else {

                System.err.println(
                        "[DEPENDENCY-WARNING] Migrated module missing on disk: "
                                + moduleFile.getAbsolutePath()
                );
            }
        }

        usedReusable.addAll(usedSet);

        // ---------------------------------------------------------
        // 4. Identify unused modules
        // ---------------------------------------------------------

        for (String module : migratedModules) {

            if (!usedSet.contains(module)) {
                unusedReusable.add(module);
            }
        }

        // ---------------------------------------------------------
        // 5. DEBUG SUMMARY BEFORE FILE OPERATIONS
        // ---------------------------------------------------------

        System.out.println();
        System.out.println("==============================================");
        System.out.println("REUSABLE SUMMARY");
        System.out.println("==============================================");

        System.out.println(
                "Migrated modules : "
                        + migratedModules.size()
        );

        System.out.println(
                "Used modules     : "
                        + usedReusable.size()
        );

        System.out.println(
                "Unused modules   : "
                        + unusedReusable.size()
        );

        System.out.println(
                "New modules      : "
                        + neueReusable.size()
        );

        System.out.println();

        System.out.println("USED REUSABLES:");

        for (String module : usedReusable) {
            System.out.println(
                    "  USED   -> " + module
            );
        }

        System.out.println();

        System.out.println("UNUSED REUSABLES:");

        for (String module : unusedReusable) {
            System.out.println(
                    "  UNUSED -> " + module
            );
        }

        System.out.println();

        System.out.println("NEW / NOT MIGRATED REUSABLES:");

        for (String module : neueReusable) {
            System.out.println(
                    "  NEW    -> " + module
            );
        }

        System.out.println(
                "=============================================="
        );

        // ---------------------------------------------------------
        // 6. Delete unused modules
        // ---------------------------------------------------------

        System.out.println();

        if (MigrationArtifactAudit.isManagedCleanupEnabled()) {

            System.out.println(
                    "INFO: Legacy unused-reusable deletion is disabled because "
                            + "the migration artifact audit/cleanup workflow is active."
            );

            System.out.println(
                    "INFO: Unused reusable candidates will be evaluated after the complete migration."
            );

        } else {

            System.out.println(
                    "INFO: Started deleting unused modules..."
            );

            for (String path : unusedReusable) {

                try {

                    Path deletePath =
                            Paths.get(userDir, path);

                    System.out.println(
                            "[DELETE-DEBUG] path   = "
                                    + deletePath.toAbsolutePath()
                    );

                    System.out.println(
                            "[DELETE-DEBUG] exists = "
                                    + Files.exists(deletePath)
                    );

                    boolean deleted =
                            Files.deleteIfExists(deletePath);

                    if (deleted) {

                        System.out.println(
                                "DELETED: " + path
                        );
                    }

                } catch (IOException e) {

                    System.err.println(
                            "ERROR: Failed to delete "
                                    + path
                                    + ": "
                                    + e.getMessage()
                    );
                }
            }
        }

        // ---------------------------------------------------------
        // 7. Synchronization
        // ---------------------------------------------------------

        System.out.println();

        System.out.println(
                "INFO: Synchronizing reusable modules..."
        );

        System.out.println();

        System.out.println(
                ">>> COPYING NEW REUSABLES"
        );

        processFileOperations(
                neueReusable,
                sourceFolder,
                userDir,
                "COPIED (NEW)"
        );

        System.out.println();

        System.out.println(
                ">>> REFRESHING USED REUSABLES"
        );

        processFileOperations(
                usedReusable,
                sourceFolder,
                userDir,
                "REFRESHED"
        );

        // ---------------------------------------------------------
        // 8. Synchronize reference API modules used by reference reusables
        // ---------------------------------------------------------

        System.out.println();
        System.out.println(">>> REFRESHING REFERENCE API MODULES");

        synchronizeReferenceModules(
                usedReusable,
                neueReusable,
                sourceFolder,
                userDir,
                migrationDir,
                appID
        );
    }
    
    /**
     * Helper method to parse YAML files and extract 'command' paths
     */
    private static List<String> findCalledModules(
            File file,
            String migrationDir,
            String appID) {

        List<String> found = new ArrayList<>();
        boolean insideCall = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = br.readLine()) != null) {

                String trimmed = line.trim();

                if (trimmed.startsWith("- type: CALL")) {
                    insideCall = true;

                } else if (trimmed.startsWith("- type:")
                        && !trimmed.contains("CALL")) {

                    insideCall = false;
                }

                if (insideCall && trimmed.startsWith("command:")) {

                    String cmd = trimmed
                            .substring(trimmed.indexOf(":") + 1)
                            .trim()
                            .replace("\"", "")
                            .replace("'", "");

                    if (!cmd.isEmpty()) {

                        String reusableName = cmd;

                        if (reusableName.startsWith("reusable.")) {
                            reusableName =
                                    reusableName.substring("reusable.".length());
                        }

                        reusableName =
                                FileNameSanitizer.sanitize(reusableName);

                        Path path =
                                Paths.get(
                                        migrationDir,
                                        appID,
                                        "reusable",
                                        reusableName + ".yaml"
                                );

                        String reusablePath =
                                path.toString().replace("\\", "/");

                        found.add(reusablePath);
                    }
                }
            }

        } catch (IOException e) {

            System.err.println(
                    "ERROR reading file: "
                            + file.getPath()
                            + " -> "
                            + e.getMessage()
            );
        }

        return found;
    }
    
    /**
     * Helper method to handle file copy operations
     */
    private static void processFileOperations(
            List<String> modules,
            String sourceFolder,
            String userDir,
            String logPrefix) {

        for (String relativePath : modules) {

            try {

                Path target = Paths.get(userDir, relativePath);

                Path source = Paths.get(
                        sourceFolder,
                        target.getFileName().toString()
                );

                boolean targetExists = Files.exists(target);
                boolean sourceExists = Files.exists(source);

                /*
                 * Referentni fajl postoji:
                 * kopiramo ga preko postojećeg/migriranog fajla.
                 */
                if (sourceExists) {

                    if (target.getParent() != null) {
                        Files.createDirectories(target.getParent());
                    }

                    Files.copy(
                            source,
                            target,
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    System.out.println(
                            logPrefix + ": " + target
                    );

                    continue;
                }

                /*
                 * Source ne postoji, ali target već postoji.
                 *
                 * Ovo je normalan slučaj za reusable koji je upravo
                 * migriran iz Tosce i nema odgovarajući fajl u
                 * migration/ref/reusable.
                 *
                 * Nema potrebe za WARNING-om.
                 */
                if (targetExists) {
                    continue;
                }

                /*
                 * Ni source ni target ne postoje.
                 *
                 * Ovo jeste stvarni problem i želimo ga videti.
                 */
                System.err.println(
                        "WARNING: Reusable module not found. "
                                + "Neither migrated target nor reference source exists: "
                                + relativePath
                );

            } catch (IOException e) {

                System.err.println(
                        "ERROR: Operation failed for "
                                + relativePath
                                + " -> "
                                + e.getMessage()
                );
            }
        }
    }
    
        

    /**
     * Synchronizes REST/SOAP module folders referenced by reusable YAML files
     * that come from migration/ref/reusable.
     *
     * Reference reusable + reference modules are treated as one unit.
     * Only modules actually referenced by those reference reusable YAMLs are
     * copied. Unrelated reference modules are ignored.
     */
    private static void synchronizeReferenceModules(
            List<String> usedReusable,
            List<String> neueReusable,
            String sourceReusableFolder,
            String userDir,
            String migrationDir,
            String appID) {

        Set<Path> referenceReusableFiles = new HashSet<>();

        collectReferenceReusableFiles(
                usedReusable,
                sourceReusableFolder,
                referenceReusableFiles
        );

        collectReferenceReusableFiles(
                neueReusable,
                sourceReusableFolder,
                referenceReusableFiles
        );

        if (referenceReusableFiles.isEmpty()) {
            System.out.println(
                    "INFO: No reference reusable YAML files are active. "
                            + "Reference module synchronization skipped."
            );
            return;
        }

        Path referenceModulesRoot =
                Paths.get(
                        userDir,
                        "migration",
                        "ref",
                        "modules"
                )
                .toAbsolutePath()
                .normalize();

        Path targetModulesRoot =
                Paths.get(
                        userDir,
                        migrationDir,
                        appID,
                        "modules"
                )
                .toAbsolutePath()
                .normalize();

        System.out.println(
                "[REF-MODULE-DEBUG] reference root = "
                        + referenceModulesRoot
        );

        System.out.println(
                "[REF-MODULE-DEBUG] target root    = "
                        + targetModulesRoot
        );

        Set<String> apiCommands = new HashSet<>();

        for (Path referenceReusable : referenceReusableFiles) {

            System.out.println(
                    "[REF-MODULE-DEBUG] scanning reusable = "
                            + referenceReusable
            );

            apiCommands.addAll(
                    findApiCommands(
                            referenceReusable.toFile()
                    )
            );
        }

        if (apiCommands.isEmpty()) {
            System.out.println(
                    "INFO: No REST/SOAP commands found in reference reusable YAML files."
            );
            return;
        }

        List<String> sortedCommands = new ArrayList<>(apiCommands);
        Collections.sort(sortedCommands);

        for (String command : sortedCommands) {

            Path relativeModulePath =
                    apiCommandToRelativeModulePath(command);

            if (relativeModulePath == null) {
                System.out.println(
                        "[REF-MODULE-DEBUG] unsupported command ignored = "
                                + command
                );
                continue;
            }

            Path sourceModuleDir =
                    referenceModulesRoot
                            .resolve(relativeModulePath)
                            .normalize();

            Path targetModuleDir =
                    targetModulesRoot
                            .resolve(relativeModulePath)
                            .normalize();

            if (!sourceModuleDir.startsWith(referenceModulesRoot)
                    || !targetModuleDir.startsWith(targetModulesRoot)) {

                System.err.println(
                        "WARNING: Unsafe reference module path ignored: "
                                + command
                );
                continue;
            }

            System.out.println();
            System.out.println(
                    "[REF-MODULE-DEBUG] command = " + command
            );
            System.out.println(
                    "[REF-MODULE-DEBUG] source  = " + sourceModuleDir
            );
            System.out.println(
                    "[REF-MODULE-DEBUG] target  = " + targetModuleDir
            );

            if (!Files.isDirectory(sourceModuleDir)) {
                System.out.println(
                        "[REF-MODULE-DEBUG] no reference module found; "
                                + "migrated module remains unchanged."
                );
                continue;
            }

            try {
                copyDirectoryRecursively(
                        sourceModuleDir,
                        targetModuleDir
                );

                System.out.println(
                        "REFRESHED REFERENCE MODULE: "
                                + relativeModulePath
                );

            } catch (IOException e) {
                System.err.println(
                        "ERROR: Failed to synchronize reference module "
                                + command
                                + " -> "
                                + e.getMessage()
                );
            }
        }
    }


    private static void collectReferenceReusableFiles(
            List<String> reusablePaths,
            String sourceReusableFolder,
            Set<Path> result) {

        if (reusablePaths == null || reusablePaths.isEmpty()) {
            return;
        }

        for (String relativePath : reusablePaths) {

            if (relativePath == null || relativePath.isBlank()) {
                continue;
            }

            Path targetPath = Paths.get(relativePath);
            Path fileName = targetPath.getFileName();

            if (fileName == null) {
                continue;
            }

            Path referenceFile =
                    Paths.get(
                            sourceReusableFolder,
                            fileName.toString()
                    )
                    .toAbsolutePath()
                    .normalize();

            if (Files.isRegularFile(referenceFile)) {
                result.add(referenceFile);
            }
        }
    }


    private static List<String> findApiCommands(
            File yamlFile) {

        List<String> commands = new ArrayList<>();

        if (yamlFile == null || !yamlFile.isFile()) {
            return commands;
        }

        try (BufferedReader br =
                     new BufferedReader(
                             new FileReader(yamlFile)
                     )) {

            String line;

            while ((line = br.readLine()) != null) {

                String trimmed = line.trim();

                if (!trimmed.startsWith("command:")) {
                    continue;
                }

                String command =
                        trimmed
                                .substring(trimmed.indexOf(":") + 1)
                                .trim()
                                .replace("\"", "")
                                .replace("'", "");

                if (command.startsWith("rest.")
                        || command.startsWith("soap.")) {

                    commands.add(command);
                }
            }

        } catch (IOException e) {
            System.err.println(
                    "ERROR reading reference reusable "
                            + yamlFile.getPath()
                            + " -> "
                            + e.getMessage()
            );
        }

        return commands;
    }


    private static Path apiCommandToRelativeModulePath(
            String command) {

        if (command == null || command.isBlank()) {
            return null;
        }

        String normalized = command.trim();

        if (!(normalized.startsWith("rest.")
                || normalized.startsWith("soap."))) {
            return null;
        }

        String[] parts = normalized.split("\\.");

        if (parts.length < 2) {
            return null;
        }

        Path result = Paths.get(parts[0]);

        for (int i = 1; i < parts.length; i++) {

            if (parts[i] == null || parts[i].isBlank()) {
                return null;
            }

            if (".".equals(parts[i])
                    || "..".equals(parts[i])
                    || parts[i].contains("/")
                    || parts[i].contains("\\")) {
                return null;
            }

            result = result.resolve(parts[i]);
        }

        return result.normalize();
    }


    private static void copyDirectoryRecursively(
            Path sourceDir,
            Path targetDir)
            throws IOException {

        Path normalizedSource =
                sourceDir.toAbsolutePath().normalize();

        Path normalizedTarget =
                targetDir.toAbsolutePath().normalize();

        if (!Files.isDirectory(normalizedSource)) {
            throw new IOException(
                    "Reference module directory does not exist: "
                            + normalizedSource
            );
        }

        try (java.util.stream.Stream<Path> stream =
                     Files.walk(normalizedSource)) {

            for (Path sourcePath : stream.toList()) {

                Path relativePath =
                        normalizedSource.relativize(sourcePath);

                Path targetPath =
                        normalizedTarget.resolve(relativePath);

                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                    continue;
                }

                if (targetPath.getParent() != null) {
                    Files.createDirectories(targetPath.getParent());
                }

                Files.copy(
                        sourcePath,
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        }
    }


    public static void modifyYamlFile(
            String migrationDir,
            String appID,
            String baseFileName) {

        String filePath =
                Paths.get(
                                System.getProperty("user.dir"),
                                migrationDir,
                                appID,
                                baseFileName + ".yaml"
                        )
                        .toString()
                        .replace("\\", "/");

        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println(
                    "File does not exist: " + filePath
            );
            return;
        }

        List<String> outputLines = new ArrayList<>();

        List<String> cardSlotCommands = Arrays.asList(
                "reusable.create_cardtoken_svsig_e_card",
                "reusable.create_cardtoken_svsig_o_card",
                "reusable.create_cardtoken_vpsig_o_card",
                "reusable.ru_dialog_aufbau_mit_ordid"
        );

        /*
         * dialogClose handling:
         *
         * After detecting:
         *
         *   command: 'reusable.dialogclose_auth1'
         *
         * we wait until the original "parameters:" line appears.
         *
         * IMPORTANT:
         * Lines between command and parameters, especially "condition:",
         * must be preserved.
         */
        boolean waitingForDialogcloseParams = false;

        /*
         * After replacing the parameters block, skip only the OLD parameter
         * children, not arbitrary indented lines.
         */
        boolean skipDialogcloseParamChildren = false;
        int dialogcloseParametersIndent = -1;

        boolean trackingCardSlotParams = false;
        int cardSlotInsertIndex = -1;

        try (BufferedReader br =
                     new BufferedReader(
                             new FileReader(file)
                     )) {

            String line;

            while ((line = br.readLine()) != null) {

                String trimmed = line.trim();

                int leadingSpaceCount =
                        line.length()
                                - line.stripLeading().length();

                String leadingSpaces =
                        line.substring(
                                0,
                                leadingSpaceCount
                        );

                // -------------------------------------------------
                // Normalize "- id:"
                // -------------------------------------------------

                int idIndex =
                        line.indexOf("- id:");

                if (idIndex != -1
                        && !line.contains("\"")) {

                    String prefix =
                            line.substring(
                                    0,
                                    idIndex
                            );

                    int colonIndex =
                            line.indexOf(
                                    ":",
                                    idIndex
                            );

                    String idValue =
                            line.substring(
                                            colonIndex + 1
                                    )
                                    .trim();

                    line =
                            prefix
                                    + "- id: \""
                                    + idValue
                                    + "\"";

                    trimmed =
                            line.trim();
                }

                // -------------------------------------------------
                // dialogClose:
                // skip only OLD parameter children
                // -------------------------------------------------

                if (skipDialogcloseParamChildren) {

                    int currentIndent =
                            line.length()
                                    - line.stripLeading().length();

                    /*
                     * Old parameter children are deeper indented
                     * than the "parameters:" line.
                     *
                     * Example:
                     *
                     *   parameters:               <- indent 8
                     *     VpNummer: ...           <- indent 10
                     *     OrdinationsId: ...      <- indent 10
                     *
                     * The next condition / command / step must NOT
                     * be removed.
                     */
                    if (!trimmed.isEmpty()
                            && currentIndent
                            > dialogcloseParametersIndent) {

                        continue;
                    }

                    skipDialogcloseParamChildren = false;
                    dialogcloseParametersIndent = -1;
                }

                // -------------------------------------------------
                // dialogClose:
                // wait for original parameters block
                // -------------------------------------------------

                if (waitingForDialogcloseParams) {

                    /*
                     * CONDITION MUST SURVIVE.
                     *
                     * Example:
                     *
                     * command: 'reusable.dialogclose_auth1'
                     * condition: "'searchAnfragenBasisdaten.SvNummer' != NULL"
                     * parameters:
                     */
                    if (trimmed.startsWith("condition:")) {

                        outputLines.add(line);

                        continue;
                    }

                    /*
                     * Replace existing parameters block.
                     */
                    if ("parameters:".equals(trimmed)) {

                        outputLines.add(line);

                        outputLines.add(
                                leadingSpaces
                                        + "  dialogId: '{B[dialogId]}'"
                        );

                        waitingForDialogcloseParams = false;

                        skipDialogcloseParamChildren = true;

                        dialogcloseParametersIndent =
                                leadingSpaceCount;

                        continue;
                    }

                    /*
                     * Normally dialogClose has a parameters block.
                     *
                     * If for some reason the next YAML step starts
                     * before parameters are found, create the block
                     * before that step so the YAML remains valid.
                     */
                    if (trimmed.startsWith("- type:")
                            || trimmed.startsWith("- id:")) {

                        outputLines.add(
                                leadingSpaces
                                        + "parameters:"
                        );

                        outputLines.add(
                                leadingSpaces
                                        + "  dialogId: '{B[dialogId]}'"
                        );

                        waitingForDialogcloseParams = false;
                    }
                }

                // -------------------------------------------------
                // Track parameters for cardSlot commands
                // -------------------------------------------------

                if (trackingCardSlotParams) {

                    if (!trimmed.isEmpty()
                            && trimmed.contains(":")
                            && !trimmed.startsWith("#")
                            && !trimmed.startsWith("- type:")
                            && !trimmed.startsWith("- id:")) {

                        String[] parts =
                                line.split(
                                        ":",
                                        2
                                );

                        line =
                                parts[0]
                                        + ": "
                                        + ToscaValueTranslator
                                        .translateToscaValues(
                                                parts[1].trim()
                                        );

                        outputLines.add(line);

                        cardSlotInsertIndex =
                                outputLines.size();

                        continue;

                    } else {

                        if (cardSlotInsertIndex != -1) {

                            /*
                             * cardSlot is currently generated elsewhere.
                             *
                             * If needed:
                             *
                             * outputLines.add(
                             *     cardSlotInsertIndex,
                             *     "          cardSlot: \"baseContact\""
                             * );
                             */
                        }

                        trackingCardSlotParams = false;
                        cardSlotInsertIndex = -1;
                    }
                }

                // -------------------------------------------------
                // Detect commands
                // -------------------------------------------------

                if (trimmed.startsWith("command:")) {

                    String cmdValue =
                            trimmed
                                    .substring(
                                            trimmed.indexOf(":")
                                                    + 1
                                    )
                                    .trim()
                                    .replace("\"", "")
                                    .replace("'", "");

                    // ---------------------------------------------
                    // dialogClose
                    // ---------------------------------------------

                    if ("reusable.dialogclose_auth1"
                            .equals(cmdValue)) {

                        /*
                         * Keep the command exactly as generated.
                         */
                        outputLines.add(line);

                        /*
                         * DO NOT write parameters yet.
                         *
                         * There may be:
                         *
                         *   condition: ...
                         *
                         * directly after command.
                         */
                        waitingForDialogcloseParams = true;

                        continue;
                    }

                    // ---------------------------------------------
                    // cardSlot commands
                    // ---------------------------------------------

                    if (cardSlotCommands.contains(
                            cmdValue
                    )) {

                        outputLines.add(line);

                        trackingCardSlotParams = true;

                        cardSlotInsertIndex =
                                outputLines.size();

                        continue;
                    }
                }

                // -------------------------------------------------
                // Transform normal parameters
                // -------------------------------------------------

                if (trimmed.contains(":")
                        && !trimmed.startsWith("-")
                        && !trimmed.startsWith("command:")
                        && !trimmed.startsWith("#")) {

                    int colonIndex =
                            line.indexOf(":");

                    String key =
                            line.substring(
                                    0,
                                    colonIndex
                            );

                    String value =
                            line.substring(
                                            colonIndex + 1
                                    )
                                    .trim();

                    line =
                            key
                                    + ": "
                                    + ToscaValueTranslator
                                    .translateToscaValues(
                                            value
                                    );
                }

                outputLines.add(line);
            }

            /*
             * Edge case:
             *
             * File ended directly after dialogClose command without
             * an original parameters block.
             */
            if (waitingForDialogcloseParams) {

                outputLines.add(
                        "        parameters:"
                );

                outputLines.add(
                        "          dialogId: '{B[dialogId]}'"
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error: "
                            + e.getMessage()
            );

            return;
        }

        // -------------------------------------------------
        // Write modified YAML
        // -------------------------------------------------

        try (PrintWriter pw =
                     new PrintWriter(file)) {

            for (String outputLine : outputLines) {
                pw.println(outputLine);
            }

            System.out.println(
                    "[SUCCESS] YAML file updated: "
                            + baseFileName
                            + ".yaml"
            );

        } catch (Exception e) {

            System.err.println(
                    "[ERROR] Failed to update YAML file "
                            + baseFileName
                            + ".yaml: "
                            + e.getMessage()
            );
        }
    }    
    
        
    private static void exportCategoryTestSheet(
            String tsuPath,
            List<ToscaNode> nodes,
            String outputDir,
            String suffix,
            Map<String, List<String>> registry,
            String baseFileName,
            String appName
    ) throws Exception {
        // AUTOMATION: Ensure the JSON index existss
        String originalTsuJsonPath = outputDir + "\\" + new java.io.File(tsuPath).getName().replace(".tsu", ".json");
        File jsonFile = new File(originalTsuJsonPath);
        
        if (!jsonFile.exists()) {
            System.out.println("[TCD-Pipeline] JSON index does not exist. Generating it automatically...");
            TsuExtractor.extractTsuToJson(tsuPath, outputDir); // Using your existing extractor
        }
        
        if (nodes == null) return;

        List<String> outputs = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        System.out.println("[TCD-Pipeline] Creating index of the original TSU file...");
        Map<String, JsonNode> tsuIndex = TCDSheetRowLevel0.loadTsuToMemoryIndex(tsuPath, mapper);

        for (ToscaNode node : nodes) {
            String rootSurrogate = node.surrogate;

            TSUObjectExtractor.start(tsuPath, rootSurrogate, outputDir, suffix);
            String extractedFile = outputDir + "\\" + rootSurrogate + "_" + suffix + ".json";

            XTestStepExtend.extend(extractedFile, tsuPath, outputDir);
            String extendedFile = extractedFile.replace(".json", "-extended.json");

            String rowDataLevel0File = extendedFile.replace("-extended.json", "-rowdata-level0.json");
            TCDSheetRowLevel0.generateRowDataLevel0(extendedFile, rowDataLevel0File, tsuIndex, rootSurrogate);

            String rowDataLevel1File = rowDataLevel0File.replace("-rowdata-level0.json", "-rowdata-level1.json");

            // STEP 4: Generate Level 1
            TCDSheetRowLevel1.generateRowDataLevel1(rowDataLevel0File, rowDataLevel1File, originalTsuJsonPath);
            
            // STEP 5: Validation and generation of Instance level (Level 1 -> 2 -> 3)
            String validatedTestSheetId = TCDSheetInstanceLevel1.getUniqueTestSheetId(rowDataLevel1File);

            if (validatedTestSheetId != null) {
                String level1InstancesPath = outputDir + "\\" + rootSurrogate + "_level1Istances.json";
                String level2InstancesPath = outputDir + "\\" + rootSurrogate + "_level2Istances.json";
                String level3InstancesPath = outputDir + "\\" + rootSurrogate + "_level3Istances.json";
                
                // 1. Base
                TCDSheetInstanceLevel1.generateInstancesJson(validatedTestSheetId, originalTsuJsonPath, level1InstancesPath);
                
                // 2. Add attributes
                TCDSheetInstanceLevel1.populateInstanceValues(level1InstancesPath, level2InstancesPath, originalTsuJsonPath);
                
                // 3. Add sub-parameters from TDClass
                TCDSheetInstanceLevel1.populateSubParameters(level2InstancesPath, level3InstancesPath, originalTsuJsonPath);
                
                // We add only the final one (level 3) to the registry, as it contains all information
              //  outputs.add(level3InstancesPath);
                
             // NOVA LINIJA: Level 4 Resolve
                String level4InstancesPath = outputDir + "\\" + rootSurrogate + "_level4Istances.json";
                TCDSheetInstanceLevel1.generateLevel4Instances(level3InstancesPath, level4InstancesPath);

                String level5InstancesPath = outputDir + "\\" + baseFileName + ".csv";                
                TCDSheetInstanceLevel1.generateLevel5MatrixCSV(level4InstancesPath, level5InstancesPath, appName);
                
                // Registruj Level 4 u output listu
                outputs.add(level4InstancesPath);
                
            }
            
            outputs.add(rowDataLevel1File);
        }

        registry.put(suffix, outputs);
    }

  
}