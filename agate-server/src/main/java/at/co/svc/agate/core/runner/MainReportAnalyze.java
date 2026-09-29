package at.co.svc.agate.core.runner;

import at.co.svc.agate.core.report.analysis.AnalysisReportWriter;
import at.co.svc.agate.core.report.analysis.AnalysisResult;
import at.co.svc.agate.core.report.analysis.JsonReportAnalyzer;

public class MainReportAnalyze {

    public static void main(
            String[] args) {

        if (args.length < 1) {

            System.err.println(
                    "Usage: analyze <jsonReport>"
            );

            System.exit(1);
        }

        String reportFile =
                args[0];

        try {

            JsonReportAnalyzer analyzer =
                    new JsonReportAnalyzer();

            AnalysisResult result =
                    analyzer.analyze(
                            reportFile
                    );

            AnalysisReportWriter writer =
                    new AnalysisReportWriter();

            writer.printConsole(
                    result
            );

            String analysisFile =
                    buildAnalysisFileName(
                            reportFile
                    );

            writer.writeJson(
                    result,
                    analysisFile
            );

            System.out.println();
            System.out.println(
                    ">>> Analysis JSON generated at: "
                            + analysisFile
            );

        } catch (Exception e) {

            System.err.println(
                    "Report analysis failed: "
                            + e.getMessage()
            );

            if (Boolean.getBoolean(
                    "agate.debug"
            )) {

                e.printStackTrace();
            }

            
            System.exit(1);
        }
    }

    private static String buildAnalysisFileName(
            String reportFile) {

        if (reportFile.endsWith(
                ".json"
        )) {

            return reportFile.substring(
                    0,
                    reportFile.length() - 5
            )
                    + "_analysis.json";
        }

        return reportFile
                + "_analysis.json";
    }
}