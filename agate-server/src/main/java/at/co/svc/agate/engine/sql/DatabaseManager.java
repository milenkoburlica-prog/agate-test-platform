package at.co.svc.agate.engine.sql;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.co.svc.agate.core.env.EnvironmentManager;

public final class DatabaseManager {

    private static Connection dbConn;
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private static final int MAX_FETCH_ROWS = 10000; // Sigurnosni limit za memoriju

    private DatabaseManager() {}

    public static synchronized void init() throws Exception {
        if (dbConn != null && !dbConn.isClosed()) {
            return;
        }
        String url = EnvironmentManager.getEnvValue("database.connectionString");
        String user = EnvironmentManager.getEnvValue("database.user");
        String pass = EnvironmentManager.getEnvValue("database.password");

        dbConn = DriverManager.getConnection(url, user, pass);
        
    }

    public static synchronized void reinit() throws Exception {
        if (dbConn == null || dbConn.isClosed()) {
            init();
        }
    }

    public static List<Map<String, Object>> select(String sql) throws Exception {
        reinit();

        try (Statement stmt = dbConn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            List<Map<String, Object>> results = new ArrayList<>();

            int rowCount = 0;
            while (rs.next() && rowCount < MAX_FETCH_ROWS) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int c = 1; c <= cols; c++) {
                    String columnName = meta.getColumnLabel(c);
                    row.put(columnName, extractValue(rs, c));
                }
                results.add(row);
                rowCount++;
            }
            return results;
        }
    }

    /**
     * Centralna logika za obradu tipova podataka
     */
    private static Object extractValue(ResultSet rs, int index) throws Exception {
        Object value = rs.getObject(index);

        // 1. Rukovanje NULL vrednostima
        if (rs.wasNull() || value == null) {
            return null; // Čuvamo pravi null (SqlTablePrinter će ga ispisati kao "null")
        }

        // 2. Formatiranje Datuma i Timestapa (dd.MM.yyyy HH:mm:ss)
        if (value instanceof java.util.Date || value instanceof java.sql.Timestamp) {
            return new SimpleDateFormat(DATE_FORMAT).format(value);
        }

        // 3. Rukovanje CLOB poljima (Dugački tekstovi)
        if (value instanceof java.sql.Clob clob) {
            long len = clob.length();
            return "[CLOB: " + len + " chars]";
        }

        // 4. Rukovanje BLOB poljima (Binarni podaci)
        if (value instanceof java.sql.Blob blob) {
            long len = blob.length();
            return "[BLOB: " + len + " bytes]";
        }

        return value;
    }

    public static int update(String sql) throws Exception {
        reinit();
        try (Statement stmt = dbConn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }
}