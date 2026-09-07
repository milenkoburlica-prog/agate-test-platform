package at.co.svc.aga.transformator.utils;

public final class MigrationLog {

    /*
     * 0 = OFF
     * 1 = NORMAL
     * 2 = DEBUG
     */
    private static int level = 1;

    private MigrationLog() {
    }

    public static void setLevel(int newLevel) {

        if (newLevel < 0 || newLevel > 2) {
            throw new IllegalArgumentException(
                    "Invalid log level: " + newLevel
                            + ". Allowed values: 0, 1, 2."
            );
        }

        level = newLevel;
    }

    public static int getLevel() {
        return level;
    }

    public static boolean isEnabled() {
        return level > 0;
    }

    public static boolean isDebugEnabled() {
        return level >= 2;
    }

    // ---------------------------------------------------------
    // LEVEL 1 - NORMAL
    // ---------------------------------------------------------

    public static void info(String message) {
        if (level >= 1) {
            System.out.println(message);
        }
    }

    public static void warn(String message) {
        if (level >= 1) {
            System.err.println("[WARNING] " + message);
        }
    }

    public static void error(String message) {
        if (level >= 1) {
            System.err.println("[ERROR] " + message);
        }
    }

    public static void success(String message) {
        if (level >= 1) {
            System.out.println("[SUCCESS] " + message);
        }
    }

    public static void section(String title) {
        if (level >= 1) {
            System.out.println();
            System.out.println("==============================================");
            System.out.println(title);
            System.out.println("==============================================");
        }
    }

    // ---------------------------------------------------------
    // LEVEL 2 - DEBUG
    // ---------------------------------------------------------

    public static void debug(String message) {
        if (level >= 2) {
            System.out.println("[DEBUG] " + message);
        }
    }

    public static void debugSection(String title) {
        if (level >= 2) {
            System.out.println();
            System.out.println("---------- DEBUG: " + title + " ----------");
        }
    }

    // ---------------------------------------------------------
    // EMPTY LINE
    // ---------------------------------------------------------

    public static void emptyLine() {
        if (level >= 1) {
            System.out.println();
        }
    }
}