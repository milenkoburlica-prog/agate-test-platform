package at.co.svc.agate.core.dsl.resolver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataValueParser {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static String parse(String value) {
        if (value == null || value.isBlank()) return value;
        String res = value;

        // 1. ZAMENE (Pattern-i koji rade svuda)
        res = replacePattern(res, "(?i)STRING:NULL", (m) -> "");
        res = replacePattern(res, "(?i)BOOLEAN:true", (m) -> "true");
        res = replacePattern(res, "(?i)BOOLEAN:false", (m) -> "false");

        res = replacePattern(res, "STRING:FIX:(\\d+)", (m) -> "A".repeat(Integer.parseInt(m.group(1))));
        res = replacePattern(res, "STRING:EMPTY", (m) -> "");
        res = replacePattern(res, "STRING:(\\d+):EMPTY", (m) -> " ".repeat(Integer.parseInt(m.group(1))));

        // 2. DATETIME (PRVI I JEDINI - hvata i DATETIME i DATETIME:offset)
        // Regex ne dozvoljava da ga DATE 'ukrade' jer gađamo celu reč DATETIME
     // 1. ZASEBNO ZA DATETIME (Isključivo velika slova)
//        res = replacePattern(res, "DATETIME(:([+-]?[\\d\\w]+))?", (m) -> {
//            return resolveDateWithOffset("DATETIME", m.group(2));
//        });

        // 2. ZASEBNO ZA DATE/DATUM (Sa negativnim lookahead-om)
        // (?!TIME) osigurava da DATE ne može biti deo reči DATETIME
//        res = replacePattern(res, "(DATE|DATUM)(?!TIME)(:([+-]?[\\d\\w]+))?", (m) -> {
//            return resolveDateWithOffset(m.group(1).toUpperCase(), m.group(3));
//        });
        
        
        return res;
    }    
    
    // Pomoćna metoda za regex zamenu
    private static String replacePattern(String input, String regex, java.util.function.Function<Matcher, String> replacer) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(replacer.apply(m)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static LocalDate applyDateOffset(LocalDate date, String sign, String amount, String unit) {
        int val = Integer.parseInt(amount) * (sign.equals("-") ? -1 : 1);
        switch (unit.toLowerCase()) {
            case "y": return date.plusYears(val);
            case "m": return date.plusMonths(val);
            default: return date.plusDays(val);
        }
    }

    private static LocalDateTime applyDateTimeOffset(LocalDateTime ldt, String sign, String amount, String unit) {
        int val = Integer.parseInt(amount) * (sign.equals("-") ? -1 : 1);
        switch (unit.toLowerCase()) {
            case "y": return ldt.plusYears(val);
            case "m": return ldt.plusMonths(val);
            case "d": return ldt.plusDays(val);
            case "h": return ldt.plusHours(val);
            case "s": return ldt.plusSeconds(val);
            default: return ldt.plusMinutes(val);
        }
    }
    
    /**
     * Primenjuje kompleksni ofset na LocalDateTime.
     * Podržava jedinice: d, M, y, h, m, s
     */
    private static LocalDateTime applyComplexOffset(LocalDateTime ldt, String offset) {
        Pattern p = Pattern.compile("([+-])(\\d+)([yMdHms])"); // Dodato H za sate
        Matcher m = p.matcher(offset);
        
        while (m.find()) {
            int val = Integer.parseInt(m.group(2));
            if ("-".equals(m.group(1))) val = -val;
            String unit = m.group(3); // Ovde ne koristimo toLowerCase() da bi sačuvali razliku M/m
            
            switch (unit) {
                case "y": ldt = ldt.plusYears(val); break;
                case "M": ldt = ldt.plusMonths(val); break; // Veliko M za mesece
                case "d": ldt = ldt.plusDays(val); break;
                case "H": ldt = ldt.plusHours(val); break; // Veliko H za sate
                case "m": ldt = ldt.plusMinutes(val); break; // Malo m za minute
                case "s": ldt = ldt.plusSeconds(val); break;
            }
        }
        return ldt;
    }
    
    
    /**
     * Primenjuje kompleksni ofset na LocalDate.
     * Podržava jedinice: d, M, y
     */
    private static LocalDate applyComplexOffset(LocalDate ld, String offset) {
        Pattern p = Pattern.compile("([+-])(\\d+)([dMyHms])");
        Matcher m = p.matcher(offset);
        
        while (m.find()) {
            String sign = m.group(1);
            int val = Integer.parseInt(m.group(2)) * (sign.equals("-") ? -1 : 1);
            String unit = m.group(3).toLowerCase();
            
            switch (unit) {
                case "y": ld = ld.plusYears(val); break;
                case "m": ld = ld.plusMonths(val); break;
                case "d": ld = ld.plusDays(val); break;
                // h, m, s se ignorišu za LocalDate jer on nema vremensku komponentu
            }
        }
        return ld;
    }
    private static String resolveDateWithOffset(String type, String offset) {
        try {
            if (type.equals("DATETIME")) {
                LocalDateTime ldt = LocalDateTime.now();
                if (offset != null) {
                    // Ovde prolazimo kroz ofset (npr. -10H+15m)
                    Pattern p = Pattern.compile("([+-]\\d+[yYjJMdDHhms])");
                    Matcher m = p.matcher(offset);
                    while (m.find()) {
                        String part = m.group(1);
                        int val = Integer.parseInt(part.substring(1, part.length() - 1));
                        if (part.startsWith("-")) val = -val;
                        char unit = part.charAt(part.length() - 1);
                        
                        if (unit == 'y') ldt = ldt.plusYears(val);
                        else if (unit == 'M') ldt = ldt.plusMonths(val);
                        else if (unit == 'T') ldt = ldt.plusDays(val);
                        else if (unit == 't') ldt = ldt.plusDays(val);
                        else if (unit == 'D') ldt = ldt.plusDays(val);
                        else if (unit == 'd') ldt = ldt.plusDays(val);
                        else if (unit == 'h') ldt = ldt.plusHours(val);
                        else if (unit == 'H') ldt = ldt.plusHours(val);
                        else if (unit == 'm') ldt = ldt.plusMinutes(val);
                        else if (unit == 's') ldt = ldt.plusSeconds(val);
                        else if (unit == 'y') ldt = ldt.plusYears(val);
                        else if (unit == 'Y') ldt = ldt.plusHours(val);
                        else if (unit == 'j') ldt = ldt.plusYears(val);
                        else if (unit == 'J') ldt = ldt.plusHours(val);
                    }
                }
                return ldt.format(DATETIME_FORMAT);
            } else {
                // Logika za DATE i DATUM (LocalDate nema H, m, s)
                LocalDate ld = LocalDate.now();
                if (offset != null) {
                    Pattern p = Pattern.compile("([+-])(\\d+)([yMdDmYJj])");
                    Matcher m = p.matcher(offset);

                    while (m.find()) {
                        // Ovde sada imamo m.group(1) za znak, group(2) za broj, group(3) za jedinicu
                        int val = Integer.parseInt(m.group(2));
                        if ("-".equals(m.group(1))) {
                            val = -val;
                        }
                        
                        char unit = m.group(3).charAt(0);
                        
                        // Primenjujemo operaciju
                        if (unit == 'y') ld = ld.plusYears(val);
                        else if (unit == 'm') ld = ld.plusMonths(val);
                        else if (unit == 'M') ld = ld.plusMonths(val);
                        else if (unit == 'd') ld = ld.plusDays(val);
                        else if (unit == 'D') ld = ld.plusDays(val);
                        else if (unit == 'y') ld = ld.plusYears(val);
                        else if (unit == 'Y') ld = ld.plusYears(val);
                        else if (unit == 'j') ld = ld.plusYears(val);
                        else if (unit == 'J') ld = ld.plusYears(val);
                    }
                    
                }
                return ld.format(DATE_FORMAT);
            }
        } catch (Exception e) {
            // Ako nešto pukne, vrati ulazni format kao tekst
            return type + (offset != null ? ":" + offset : "");
        }
    }
    
    
}
