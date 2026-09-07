package at.co.svc.tosca.tsu.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import at.co.svc.tosca.tsu.dto.ToscaNode;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

public class TSUExporter {

    private final Gson gson =
            new GsonBuilder().setPrettyPrinting().create();

    public void export(
            Map<String, List<ToscaNode>> inventory,
            String outputDir
    ) throws Exception {

        exportCategory(inventory.get("testcases"), outputDir, "testcases");
        exportCategory(inventory.get("reusables"), outputDir, "reusables");
        exportCategory(inventory.get("templates"), outputDir, "templates");
        exportCategory(inventory.get("tcdsheets"), outputDir, "tcdsheets");
    }

    private void exportCategory(
            List<ToscaNode> nodes,
            String outputDir,
            String suffix
    ) throws Exception {

        if (nodes == null) return;

        File dir = new File(outputDir);
        if (!dir.exists()) dir.mkdirs();

        for (ToscaNode node : nodes) {

            String fileName =
                    node.surrogate + "_" + suffix + ".json";

            File file = new File(dir, fileName);

            try (FileWriter fw = new FileWriter(file)) {
                gson.toJson(node, fw);
            }
        }
    }
}