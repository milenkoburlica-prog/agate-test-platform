package at.co.svc.tosca.tsu.del;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DateKeywordExtractor {

    public static void main(String[] args) {
        String pathToTsocaJSON = "C:\\work\\projects\\playwright\\tosca_2025\\FACH-Set.json"; // Ovde stavi putanju
        extractDateKeywords(pathToTsocaJSON);
    }

    public static void extractDateKeywords(String filePath) {
        // Pattern: traži '{' posle čega ide 'DAT' i sve do '}'
        Pattern pattern = Pattern.compile("\\{DAT[^\\}]*\\}");
        
        // TreeSet automatski uklanja duplikate i sortira rezultate
        Set<String> foundKeywords = new TreeSet<>();

        // Files.lines() je memorijski efikasan za velike fajlove
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.forEach(line -> {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    foundKeywords.add(matcher.group());
                }
            });

            // Ispis rezultata na konzolu
            System.out.println("Pronađeni jedinstveni DAT ključevi:");
            foundKeywords.forEach(System.out::println);

        } catch (IOException e) {
            System.err.println("Greška pri čitanju fajla: " + e.getMessage());
        }
    }
}

