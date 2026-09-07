package at.co.svc.tosca.tsu.del;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomFormatExtractor {

    public static void main(String[] args) {
        // Putanja do tvog fajla
        String filePath = "C:\\work\\projects\\playwright\\tosca_2025\\FACH-Set.json";
        extractRandomFormats(filePath);
    }

    public static void extractRandomFormats(String path) {
        // IZMENJEN REGEX: Sada hvata bilo šta što počinje sa {RND i završava se sa }
        // Pattern.CASE_INSENSITIVE obezbeđuje da radi i za {rnd...} i za {RND...}
        Pattern rndPattern = Pattern.compile("\\{RND.*?}", Pattern.CASE_INSENSITIVE);
        Set<String> foundFormats = new HashSet<>();

        try {
            // Čitamo ceo fajl kao String
            String content = new String(Files.readAllBytes(Paths.get(path)));
            Matcher matcher = rndPattern.matcher(content);

            while (matcher.find()) {
                foundFormats.add(matcher.group());
            }

            System.out.println("--- Pronadjeni RND formati ---");
            foundFormats.stream().sorted().forEach(System.out::println);
            System.out.println("--------------------------------");
            System.out.println("Ukupno jedinstvenih RND formata: " + foundFormats.size());

        } catch (IOException e) {
            System.err.println("Greska pri citanju fajla: " + e.getMessage());
        }
    }
}