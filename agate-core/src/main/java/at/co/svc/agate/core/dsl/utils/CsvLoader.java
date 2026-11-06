package at.co.svc.agate.core.dsl.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to load test data from CSV files.
 * Supports comma-separated values and uses the first line as headers.
 */
public class CsvLoader {

    public static List<Map<String, String>> load(String path) throws Exception {
        List<Map<String, String>> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String headerLine = br.readLine();
            if (headerLine == null) return data;

            // Ključna promena: split po ";"
            String[] headers = headerLine.split(";");

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    if (i < values.length) {
                        // Čistimo whitespace i navodnike ako ih ima
                        row.put(headers[i].trim(), values[i].trim().replace("\"", ""));
                    }
                }
                data.add(row);
            }
        }
        return data;
    }
    
}