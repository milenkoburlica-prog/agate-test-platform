package at.co.svc.agate.core.dsl.resolver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToscaDateResolver {

    // Prioritet: DATETIME ide PRVI, DATE ide DRUGI.
    // Dodali smo (?!...) negativni lookahead da sprečimo da DATE "pojede" deo
    // DATETIME-a
    private static final Pattern TOSCA_DATETIME_PATTERN = Pattern.compile("\\{(DATETIME)\\[(.*?)]\\[(.*?)]\\[(.*?)]}");
    private static final Pattern TOSCA_DATE_PATTERN = Pattern
            .compile("\\{(DATE)(?!\\[TIME)\\[(.*?)]\\[(.*?)]\\[(.*?)]}");

    // Isto za kratke formate
    private static final Pattern TOSCA_DATETIME_SHORT_PATTERN = Pattern.compile("\\{(DATETIME)\\[(.*?)]\\[(.*?)]}");
    private static final Pattern TOSCA_DATE_SHORT_PATTERN = Pattern.compile("\\{(DATE)(?!\\[TIME)\\[(.*?)]\\[(.*?)]}");

    private static final Pattern TOSCA_3_PART_DATETIME_PATTERN = Pattern
            .compile("\\{(DATETIME|DATE)\\[(.*?)]\\[(.*?)]\\[(.*?)]}");

    public static String resolve(String input) {
        if (input == null || !input.toUpperCase().contains("DATE"))
            return input;

        String result = input;

        // 1. NOVO: Prvo pokrivamo vaš novi slučaj sa 3 para zagrada (baza, offset,
        // format)
        result = process3Part(result, TOSCA_3_PART_DATETIME_PATTERN);

        // 2. Postojeća obrada ostaje netaknuta
        result = process(result, TOSCA_DATE_PATTERN);
        result = process(result, TOSCA_DATE_SHORT_PATTERN);

        // Finalni fallback
        return result.replace("{DATE}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .replace("{DATETIME}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
    }

    private static String process(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String type = matcher.group(1);

            // Ovde sada imamo 4 grupe u patternu: {DATE[base][offset][format]}
            // group(1)=DATE, group(2)=base, group(3)=offset, group(4)=format
            String base = matcher.group(2);
            String offset = matcher.group(3);
            String format = matcher.group(4);

            // Sada zovemo metodu sa 4 argumenta
            matcher.appendReplacement(sb, Matcher.quoteReplacement(calculate(type, base, offset, format)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String calculate(String type, String base, String offset, String format) {
        LocalDateTime ldt;

        // 1. Parsiranje baze (startna tačka)
        if (base == null || base.isEmpty()) {
            ldt = LocalDateTime.now();
        } else {
            try {
                DateTimeFormatter baseFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                ldt = java.time.LocalDate.parse(base, baseFormatter).atStartOfDay();
            } catch (Exception e) {
                ldt = LocalDateTime.now();
            }
        }

        // 2. Primena ofseta (sa fleksibilnim Regex-om)
        if (offset != null && !offset.isEmpty()) {
            // [+-]? znači da je znak opcioni, \s* dozvoljava razmak ako ga ima
            Pattern p = Pattern.compile("([+-]?)\\s*(\\d+)\\s*([dMyHms])");
            Matcher m = p.matcher(offset);

            while (m.find()) {
                String sign = m.group(1); // Može biti "+", "-" ili "" (prazan string)
                int val = Integer.parseInt(m.group(2));

                // Logika: ako je "-", oduzmi, inače dodaj (tretira "" i "+" kao pozitivan broj)
                if ("-".equals(sign)) {
                    val = -val;
                }

                switch (m.group(3)) {
                case "d" -> ldt = ldt.plusDays(val);
                case "M" -> ldt = ldt.plusMonths(val);
                case "y" -> ldt = ldt.plusYears(val);
                case "H" -> ldt = ldt.plusHours(val);
                case "m" -> ldt = ldt.plusMinutes(val);
                case "s" -> ldt = ldt.plusSeconds(val);
                }
            }
        }

        // 3. Formatiranje (prilagođavanje Tosca -> Java formatu)
        if (format == null || format.isEmpty()) {
            format = type.equals("DATETIME") ? "dd.MM.yyyy HH:mm:ss" : "dd.MM.yyyy";
        }
        String javaFormat = format.replace("f", "S").replace("T", "'T'");

        return ldt.format(DateTimeFormatter.ofPattern(javaFormat));
    }

    private static String process3Part(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String type = matcher.group(1); 
            String base = matcher.group(2);
            String offset = matcher.group(3);
            String format = matcher.group(4);

            matcher.appendReplacement(sb, Matcher.quoteReplacement(calculate(type, base, offset, format)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}