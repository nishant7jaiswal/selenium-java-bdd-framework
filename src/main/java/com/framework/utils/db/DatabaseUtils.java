package com.framework.utils.db;

import com.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * JDBC-based database utility for backend validation.
 *
 * FAANG pattern: After a UI action (e.g. form submit), verify
 * the correct record exists in the database — dual assertion strategy.
 *
 * Supports: MySQL, PostgreSQL, Oracle, SQL Server (driver on classpath)
 *
 * Usage:
 *   try (DatabaseUtils db = DatabaseUtils.connect()) {
 *       List<Map<String,Object>> rows = db.query("SELECT * FROM users WHERE email = ?", email);
 *       assertThat(rows).hasSize(1);
 *       assertThat(rows.get(0).get("status")).isEqualTo("ACTIVE");
 *   }
 */
public class DatabaseUtils implements AutoCloseable {

    private static final Logger log = LogManager.getLogger(DatabaseUtils.class);
    private final Connection connection;

    // ── Connection ───────────────────────────────────────────────────────

    private DatabaseUtils(Connection connection) {
        this.connection = connection;
    }

    /**
     * Open a connection using config.properties credentials.
     */
    public static DatabaseUtils connect() {
        try {
            String url      = ConfigManager.get().dbUrl();
            String username = ConfigManager.get().dbUsername();
            String password = ConfigManager.get().dbPassword();
            Connection conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);
            log.info("Database connection established: {}", url);
            return new DatabaseUtils(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    /**
     * Open a connection with explicit credentials.
     */
    public static DatabaseUtils connect(String url, String username, String password) {
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);
            log.info("Database connection established: {}", url);
            return new DatabaseUtils(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + url, e);
        }
    }

    // ── Query ────────────────────────────────────────────────────────────

    /**
     * Execute a SELECT query and return results as List of Maps.
     * Each Map represents one row: column name → value.
     *
     * Usage: db.query("SELECT id, name FROM users WHERE status = ?", "ACTIVE")
     */
    public List<Map<String, Object>> query(String sql, Object... params) {
        log.debug("Executing query: {} | Params: {}", sql, Arrays.toString(params));
        List<Map<String, Object>> results = new ArrayList<>();

        try (PreparedStatement stmt = prepareStatement(sql, params);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnLabel(i).toLowerCase(), rs.getObject(i));
                }
                results.add(row);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + sql, e);
        }

        log.debug("Query returned {} row(s)", results.size());
        return results;
    }

    /**
     * Return a single value from a query — useful for COUNT, SUM, etc.
     *
     * Usage: long count = db.queryScalar("SELECT COUNT(*) FROM orders WHERE user_id = ?", userId);
     */
    public <T> T queryScalar(String sql, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        if (rows.isEmpty()) return null;
        @SuppressWarnings("unchecked")
        T value = (T) rows.get(0).values().iterator().next();
        return value;
    }

    /**
     * Return a single row as a Map. Throws if 0 or >1 rows returned.
     */
    public Map<String, Object> querySingleRow(String sql, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        if (rows.isEmpty()) throw new RuntimeException("No rows returned for: " + sql);
        if (rows.size() > 1)  throw new RuntimeException("Expected 1 row but got " + rows.size() + " for: " + sql);
        return rows.get(0);
    }

    // ── Execute ──────────────────────────────────────────────────────────

    /**
     * Execute an INSERT / UPDATE / DELETE statement.
     * Returns the number of affected rows.
     */
    public int execute(String sql, Object... params) {
        log.debug("Executing DML: {} | Params: {}", sql, Arrays.toString(params));
        try (PreparedStatement stmt = prepareStatement(sql, params)) {
            int affected = stmt.executeUpdate();
            connection.commit();
            log.info("DML affected {} row(s): {}", affected, sql);
            return affected;
        } catch (SQLException e) {
            rollback();
            throw new RuntimeException("DML failed: " + sql, e);
        }
    }

    /**
     * Execute multiple statements in a single transaction.
     * All succeed or all roll back.
     */
    public void executeInTransaction(List<String> statements) {
        try {
            for (String sql : statements) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate(sql);
                    log.debug("Executed: {}", sql);
                }
            }
            connection.commit();
            log.info("Transaction committed — {} statement(s)", statements.size());
        } catch (SQLException e) {
            rollback();
            throw new RuntimeException("Transaction failed", e);
        }
    }

    // ── Validation Helpers ───────────────────────────────────────────────

    /**
     * Assert that a record exists matching the given condition.
     */
    public boolean recordExists(String table, String whereClause, Object... params) {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + whereClause;
        Number count = queryScalar(sql, params);
        return count != null && count.longValue() > 0;
    }

    /**
     * Get count of rows matching a condition.
     */
    public long countRows(String table, String whereClause, Object... params) {
        String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + whereClause;
        Number count = queryScalar(sql, params);
        return count == null ? 0 : count.longValue();
    }

    /**
     * Get a specific field value from a single matching row.
     */
    public Object getFieldValue(String table, String field, String whereClause, Object... params) {
        String sql = "SELECT " + field + " FROM " + table + " WHERE " + whereClause + " LIMIT 1";
        List<Map<String, Object>> rows = query(sql, params);
        if (rows.isEmpty()) return null;
        return rows.get(0).get(field.toLowerCase());
    }

    /**
     * Clean up test data by deleting records (test teardown).
     */
    public int deleteWhere(String table, String whereClause, Object... params) {
        return execute("DELETE FROM " + table + " WHERE " + whereClause, params);
    }

    // ── Private Helpers ──────────────────────────────────────────────────

    private PreparedStatement prepareStatement(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }

    private void rollback() {
        try {
            connection.rollback();
            log.warn("Transaction rolled back");
        } catch (SQLException e) {
            log.error("Rollback failed: {}", e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log.info("Database connection closed");
            }
        } catch (SQLException e) {
            log.error("Error closing DB connection: {}", e.getMessage());
        }
    }
}
