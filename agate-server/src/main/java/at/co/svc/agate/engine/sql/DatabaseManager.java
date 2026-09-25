package at.co.svc.agate.engine.sql;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.co.svc.agate.core.env.EnvironmentManager;

public final class DatabaseManager {

    private static final Map<String, Connection> CONNECTIONS =
            new HashMap<>();

    private static final String DEFAULT_DATASOURCE = "default";
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private static final int MAX_FETCH_ROWS = 10000;

    private DatabaseManager() {
    }

    /**
     * Backward-compatible initialization of the default datasource.
     */
    public static synchronized void init() throws Exception {
        getConnection(null);
    }

    /**
     * Initializes the requested datasource if necessary.
     */
    public static synchronized void init(String datasource) throws Exception {
        getConnection(datasource);
    }

    /**
     * Backward-compatible reinitialization of the default datasource.
     */
    public static synchronized void reinit() throws Exception {
        getConnection(null);
    }

    /**
     * Reinitializes the requested datasource if necessary.
     */
    public static synchronized void reinit(String datasource) throws Exception {
        getConnection(datasource);
    }

    /**
     * Returns one cached JDBC connection per logical datasource.
     *
     * datasource == null / blank:
     *   database.connectionString
     *   database.user
     *   database.password
     *
     * datasource == "ks":
     *   ks.database.connectionString
     *   ks.database.user
     *   ks.database.password
     */
    private static synchronized Connection getConnection(
            String datasource) throws Exception {

        String connectionKey =
                normalizeDatasource(datasource);

        Connection connection =
                CONNECTIONS.get(connectionKey);

        if (connection != null
                && !connection.isClosed()) {

            return connection;
        }

        String configPrefix =
                buildConfigPrefix(datasource);

        String url =
                EnvironmentManager.getEnvValue(
                        configPrefix
                                + "database.connectionString");

        String user =
                EnvironmentManager.getEnvValue(
                        configPrefix
                                + "database.user");

        String pass =
                EnvironmentManager.getEnvValue(
                        configPrefix
                                + "database.password");

        connection =
                DriverManager.getConnection(
                        url,
                        user,
                        pass);

        CONNECTIONS.put(
                connectionKey,
                connection);

        return connection;
    }

    private static String normalizeDatasource(
            String datasource) {

        if (datasource == null
                || datasource.isBlank()) {

            return DEFAULT_DATASOURCE;
        }

        return datasource.trim().toLowerCase();
    }

    private static String buildConfigPrefix(
            String datasource) {

        if (datasource == null
                || datasource.isBlank()) {

            return "";
        }

        return datasource.trim() + ".";
    }

    /**
     * Backward-compatible SELECT using the default datasource.
     */
    public static List<Map<String, Object>> select(
            String sql) throws Exception {

        return select(
                sql,
                null);
    }

    /**
     * SELECT using an optional named datasource.
     */
    public static List<Map<String, Object>> select(
            String sql,
            String datasource) throws Exception {

        Connection dbConn =
                getConnection(datasource);

        try (Statement stmt = dbConn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta =
                    rs.getMetaData();

            int cols =
                    meta.getColumnCount();

            List<Map<String, Object>> results =
                    new ArrayList<>();

            int rowCount = 0;

            while (rs.next()
                    && rowCount < MAX_FETCH_ROWS) {

                Map<String, Object> row =
                        new LinkedHashMap<>();

                for (int c = 1;
                     c <= cols;
                     c++) {

                    String columnName =
                            meta.getColumnLabel(c);

                    row.put(
                            columnName,
                            extractValue(
                                    rs,
                                    c));
                }

                results.add(row);
                rowCount++;
            }

            return results;
        }
    }

    /**
     * Backward-compatible UPDATE/INSERT/DELETE using the default datasource.
     */
    public static int update(
            String sql) throws Exception {

        return update(
                sql,
                null);
    }

    /**
     * UPDATE/INSERT/DELETE using an optional named datasource.
     */
    public static int update(
            String sql,
            String datasource) throws Exception {

        Connection dbConn =
                getConnection(datasource);

        try (Statement stmt = dbConn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    /**
     * Central data type conversion used for SQL result tables.
     */
    private static Object extractValue(
            ResultSet rs,
            int index) throws Exception {

        Object value =
                rs.getObject(index);

        if (rs.wasNull()
                || value == null) {

            return null;
        }

        if (value instanceof java.util.Date
                || value instanceof java.sql.Timestamp) {

            return new SimpleDateFormat(
                    DATE_FORMAT)
                    .format(value);
        }

        if (value instanceof Clob clob) {
            long len = clob.length();
            return "[CLOB: " + len + " chars]";
        }

        if (value instanceof Blob blob) {
            long len = blob.length();
            return "[BLOB: " + len + " bytes]";
        }

        return value;
    }

    /**
     * Optional cleanup hook for tests / shutdown handling.
     */
    public static synchronized void closeAll() {

        for (Connection connection : CONNECTIONS.values()) {
            if (connection == null) {
                continue;
            }

            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (Exception ignored) {
                // Cleanup must not hide the original test result.
            }
        }

        CONNECTIONS.clear();
    }
}
