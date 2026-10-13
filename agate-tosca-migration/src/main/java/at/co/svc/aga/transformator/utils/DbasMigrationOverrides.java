package at.co.svc.aga.transformator.utils;

import java.util.*;
import java.util.regex.*;

/** Explicit DBAS migration rules requested for the known reader step and SQLPlus credentials. */
public final class DbasMigrationOverrides {
    private DbasMigrationOverrides() {}
    private static final String READER_ID = "3a1aba16-7026-7f71-9bda-f208797f0511_3a1aba16-7026-be2a-dbfa-acc17502daba";
    private static final Pattern CONNECTION = Pattern.compile(
            "(" + Pattern.quote("\"{E[env.database.user]}/") + ")([^\"\\r\\n]*)("
                    + Pattern.quote("@{B[EC_DB_TNS]}\"") + ")");
    public static void reader(Map<String,Object> step) {
        if (READER_ID.equals(step.get("id")) && "SELECT".equals(step.get("op")))
            step.put("value", Map.of("label", "{E[users.gino_simu]}"));
    }
    public static String sqlplus(String command) {
        if (command == null || !command.toLowerCase(Locale.ROOT).contains("sqlplus.exe")) return command;
        Matcher match = CONNECTION.matcher(command);
        if (!match.find()) return command;
        return match.replaceFirst(Matcher.quoteReplacement(match.group(1)
                + "{E[env.database.password]}" + match.group(3)));
    }
}
