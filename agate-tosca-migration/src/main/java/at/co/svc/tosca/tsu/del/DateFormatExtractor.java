package at.co.svc.tosca.tsu.del;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFormatExtractor {

    public static void main(String[] args) {
        // Promeni putanju do tvog fajla
        String filePath = "C:\\work\\projects\\agate-studio\\demo-agate-server\\tsu\\Fach.txt";
        extractDateFormats(filePath);
    }

    public static void extractDateFormats(String path) {
        // Regex koji hvata bilo šta što počinje sa {DATE i završava sa }
        // Ne ograničavamo se na broj zagrada, već hvatamo sve što liči na DATE placeholder
        Pattern datePattern = Pattern.compile("\\{DATE.*?}", Pattern.CASE_INSENSITIVE);
        Set<String> foundFormats = new HashSet<>();

        try {
            // Čitamo ceo fajl kao String (radi savršeno i bez standardnih EOL-ova)
            String content = new String(Files.readAllBytes(Paths.get(path)));
            Matcher matcher = datePattern.matcher(content);

            while (matcher.find()) {
                foundFormats.add(matcher.group());
            }

            System.out.println("--- Pronadjeni DATE formati ---");
            foundFormats.stream().sorted().forEach(System.out::println);
            System.out.println("--------------------------------");
            System.out.println("Ukupno jedinstvenih formata: " + foundFormats.size());

        } catch (IOException e) {
            System.err.println("Greska pri citanju fajla: " + e.getMessage());
        }
    }
}