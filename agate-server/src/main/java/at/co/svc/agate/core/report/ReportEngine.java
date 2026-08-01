package at.co.svc.agate.core.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.itextpdf.html2pdf.HtmlConverter;

import at.co.svc.agate.core.dsl.model.TestCase;

public class ReportEngine {

    private final List<TestRecord> records = new ArrayList<>();

    public static class TestRecord {
        public String suiteName; 
        public String appName;  
        public String name;
        public String description;
        public String suiteFile;
        public boolean passed;
        public long duration;
        public String error;
        public List<String> steps = new ArrayList<>();
        public List<String> performanceMetrics = new ArrayList<>();
        
    }

    public void addRecord(TestCase tc, String suiteFile, boolean passed, long duration, String error, List<String> testLogs) {
        TestRecord record = new TestRecord();
        
        String normalizedPath = suiteFile != null ? suiteFile.replace("\\", "/") : "";
        String[] parts = normalizedPath.split("/");
        
        if (parts.length >= 2) {
            record.appName = parts[parts.length - 2];
            record.suiteName = parts[parts.length - 1].replace(".yaml", "");
        } else if (parts.length == 1) {
            record.appName = "General";
            record.suiteName = parts[0].replace(".yaml", "");
        } else {
            record.appName = "General";
            record.suiteName = "UnknownSuite";
        }
        
        record.name = tc.getName() != null ? tc.getName() : "Unnamed Test";
        record.description = tc.getDescription();
        record.suiteFile = suiteFile;
        record.passed = passed;
        record.duration = duration;
        record.error = error;
        if (testLogs != null) {
            record.steps.addAll(testLogs);
        }
        records.add(record);
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        
        String escaped = text.replace("&", "&amp;")
                             .replace("<", "&lt;")
                             .replace(">", "&gt;");
        return escaped.replaceAll("\\\u001B\\[[;\\d]*[A-Za-z]", "")
                      .replaceAll("\u001B\\[[;\\d]*m", "")
                      .replaceAll("\u001B", "");
    }
    
    public void generateHtmlReport(String outputPath) throws Exception {
        
        Path path = Paths.get(outputPath);
        if (path.getParent() != null) Files.createDirectories(path.getParent());

        Map<String, List<TestRecord>> groupedRecords = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.suiteName,
                        TreeMap::new,
                        Collectors.toList()
                ));
        
        long totalDurationMs = records.stream().mapToLong(r -> r.duration).sum();
        long total = records.size();
        long passed = records.stream().filter(r -> r.passed).count();
        double successRate = (total == 0) ? 0 : (passed * 100.0 / total);

        StringBuilder suitesHtml = new StringBuilder();

        for (Map.Entry<String, List<TestRecord>> suiteEntry : groupedRecords.entrySet()) {
            List<TestRecord> tests = suiteEntry.getValue();
            long sPassed = tests.stream().filter(r -> r.passed).count();
            int sTotal = tests.size();
            int sRate = (sTotal == 0) ? 0 : (int) ((sPassed * 100.0) / sTotal);
            
            suitesHtml.append("<details><summary><div class='suite-row'>")
                      .append("<div class='suite-name'>").append(escapeHtml(suiteEntry.getKey())).append("</div>")
                      .append("<div class='suite-right'>")
                      .append("<span class='badge'>Total: ").append(sTotal).append("</span>")
                      .append("<span class='badge'>Passed: ").append(sPassed).append("</span>")
                      .append("<span class='badge'>Failed: ").append(sTotal - sPassed).append("</span>")
                      .append("<span class='badge rate'>").append(sRate).append("%</span>")
                      .append("</div></div>")
                      .append("<div class='progress'><div class='progress-bar' style='width:").append(sRate).append("%'></div></div>")
                      .append("</summary><div class='content'><table>")
                      .append("<tr><th>Test Case</th><th>Status</th><th>Runtime</th></tr>");
                      
            for (TestRecord record : tests) {
                suitesHtml.append("<tr><td>");
                
                if (record.steps != null && !record.steps.isEmpty()) {
                    suitesHtml.append("<details style='cursor:pointer;'>")
                            .append("<summary style='font-weight:bold; margin-bottom:6px;'>")
                            .append(escapeHtml(record.name))
                            .append("</summary>")
                            .append("<div style='margin-top:8px; padding:10px; background:#0f172a; border-radius:6px; font-family:monospace; font-size:12px; color:#cbd5e1;'>");

                    for (String stepLine : record.steps) {
                        String escapedLine = escapeHtml(stepLine);
                        
                        String color = "#cbd5e1";
                        String fontWeight = "normal";

                        if (escapedLine.contains("&gt;&gt;&gt;") && escapedLine.contains("DSL")) {
                            color = "#ffffff";
                        } 
                        else if (escapedLine.contains("ERROR") || escapedLine.contains("FAILED")) {
                            color = "#ef4444";
                            fontWeight = "bold";
                        } 
                        else if ((escapedLine.contains("&gt;&gt;&gt;")) || (escapedLine.contains(" &lt;&lt;&lt;"))) { 
                            color = "#228b22"; 
                        }

                        String style = String.format("color:%s; font-size:14px; font-weight:%s;", color, fontWeight);

                        suitesHtml.append("<pre style='").append(style).append(" margin:0;'>")
                                .append(escapedLine)
                                .append("</pre>");

                        if (escapedLine.contains("FINISHED") || escapedLine.contains("FAILED")) {
                            suitesHtml.append("<div style='border-bottom:1px solid #475569; margin: 5px 0 10px 0;'></div>");
                        }
                    }
                    
                    suitesHtml.append("</div></details>");
                } else {
                    suitesHtml.append("<div style='font-weight:bold;'>").append(escapeHtml(record.name)).append("</div>");
                }

                suitesHtml.append("</td>")
                        .append("<td><span class='badge ").append(record.passed ? "passed" : "failed").append("'>")
                        .append(record.passed ? "PASSED" : "FAILED").append("</span></td>")
                        .append("<td class='runtime'>").append(record.duration).append(" ms</td></tr>");
            }
            suitesHtml.append("</table></div></details>");
            
        }

        Path templatePath = Paths.get("env", "templates", "template.html");
        String html = Files.readString(templatePath);

        html = html.replace("{{RUNTIME_APP}}", System.getProperty("APPLICATION", "svc"))
                   .replace("{{RUNTIME_INSTANCE}}", System.getProperty("INSTANCE", "N/A"))
                   .replace("{{RUNTIME_USER}}", System.getProperty("PERSON", "Unknown"))
                   .replace("{{DURATION}}", (totalDurationMs / 1000) + " sec")
                   .replace("{{SUCCESS_RATE}}", String.format("%.0f", successRate))
                   .replace("{{TOTAL_TESTS}}", String.valueOf(total))
                   .replace("{{PASSED_COUNT}}", String.valueOf(passed))
                   .replace("{{FAILED_COUNT}}", String.valueOf(total - passed))
                   .replace("{{SUITE_RESULTS}}", suitesHtml.toString())
                   .replace("{{GRADIENT_STYLE}}", "conic-gradient(var(--success) 0deg " + (successRate * 3.6) + "deg, var(--failed) " + (successRate * 3.6) + "deg 360deg)");

        Files.writeString(path, html);
    }    
    
    public void generatePdfReport(String htmlPath, String pdfOutputPath) throws Exception {
        File htmlFile = new File(htmlPath);
        File pdfFile = new File(pdfOutputPath);
        
        try (FileInputStream fis = new FileInputStream(htmlFile);
             FileOutputStream fos = new FileOutputStream(pdfFile)) {
            HtmlConverter.convertToPdf(fis, fos);
        }
    }
    
    public void generateJsonReport(String outputPath) throws Exception {
        long passed = records.stream().filter(r -> r.passed).count();
        long failed = records.stream().filter(r -> !r.passed).count();
        double rate = records.isEmpty() ? 0 : (passed * 100.0 / records.size());

        String runtimeUser = System.getProperty("PERSON", "Unknown");
        String runtimeInstance = System.getProperty("INSTANCE", "Unknown");
        String runtimeApp = System.getProperty("APPLICATION", "Unknown");

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTimestamp = now.format(formatter);

        StringBuilder json = new StringBuilder();
        json.append("{\n")
            .append("  \"timestamp\": \"").append(escapeJson(formattedTimestamp)).append("\",\n")
            .append("  \"executionUser\": \"").append(escapeJson(runtimeUser)).append("\",\n")
            .append("  \"targetInstance\": \"").append(escapeJson(runtimeInstance)).append("\",\n")
            .append("  \"targetApplication\": \"").append(escapeJson(runtimeApp)).append("\",\n")
            .append("  \"metrics\": {\n")
            .append("    \"totalTestCases\": ").append(records.size()).append(",\n")
            .append("    \"passed\": ").append(passed).append(",\n")
            .append("    \"failed\": ").append(failed).append(",\n")
            .append("    \"successRate\": ").append(String.format(java.util.Locale.US, "%.1f", rate)).append("\n")
            .append("  },\n")
            .append("  \"testCases\": [\n");

        for (int i = 0; i < records.size(); i++) {
            TestRecord r = records.get(i);
            json.append("    {\n")
                .append("      \"name\": \"").append(escapeJson(r.name)).append("\",\n")
                .append("      \"suiteFile\": \"").append(escapeJson(r.suiteFile)).append("\",\n")
                .append("      \"description\": \"").append(escapeJson(r.description)).append("\",\n")
                .append("      \"passed\": ").append(r.passed).append(",\n")
                .append("      \"durationMs\": ").append(r.duration).append(",\n")
                .append("      \"error\": ").append(r.error == null ? "null" : "\"" + escapeJson(r.error) + "\"").append(",\n")
                .append("      \"steps\": [\n");

            for (int j = 0; j < r.steps.size(); j++) {
                json.append("        \"").append(escapeJson(r.steps.get(j))).append("\"");
                if (j < r.steps.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            json.append("      ]\n")
                .append("    }");
            
            if (i < records.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]\n")
            .append("}");

        java.nio.file.Path path = java.nio.file.Paths.get(outputPath);
        java.nio.file.Files.createDirectories(path.getParent());
        java.nio.file.Files.writeString(path, json.toString(), java.nio.charset.StandardCharsets.UTF_8);
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}