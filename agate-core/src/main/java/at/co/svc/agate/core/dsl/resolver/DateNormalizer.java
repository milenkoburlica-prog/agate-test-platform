package at.co.svc.agate.core.dsl.resolver;

public class DateNormalizer {
    public static String normalize(String input) {
        if (input == null) return null;
        
        String s = input;
        
        // 1. Ujednači naziv (Date, DATETIME, date -> DATE)
        s = s.replaceAll("(?i)\\{DATETIME", "{DATE");
        s = s.replaceAll("(?i)\\{Date", "{DATE");
        
        // 2. Pretvaranje formata {DATE+141d} u {DATE[][+141d][]}
        // Traži sve što je {DATE +/-(broj)(jedinica)}
        s = s.replaceAll("\\{DATE([+-]\\d+[dMyHms])\\}", "{DATE[][$1][]}");
        
        // 3. Pretvaranje formata {DATE} u {DATE[][][]}
        s = s.replaceAll("\\{DATE\\}", "{DATE[][][]}");
        
        return s;
    }
}