package com.timeofftracker.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class DatabaseManager {
    private final String jdbcUrl;

    public DatabaseManager() {
        this(defaultDatabasePath());
    }

    public DatabaseManager(Path path) {
        try {
            Files.createDirectories(path.getParent());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create data directory", e);
        }
        jdbcUrl = "jdbc:sqlite:" + path.toAbsolutePath();
        initialize();
    }

    public static Path defaultDatabasePath() {
        String home = System.getProperty("user.home", ".");
        return Paths.get(home, ".timeofftracker", "timeoff.db");
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private void initialize() {
        try (Connection connection = getConnection(); Statement st = connection.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS time_off_years (
                        year INTEGER PRIMARY KEY,
                        vacation_allowance REAL NOT NULL CHECK(vacation_allowance >= 0),
                        eto_allowance REAL NOT NULL CHECK(eto_allowance >= 0),
                        standard_workday REAL NOT NULL CHECK(standard_workday > 0)
                    )
                    """);

            createEntriesTableIfMissing(st);
            migrateEntryTypesIfNeeded(connection);
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_entries_date ON time_off_entries(entry_date)");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize Time Off Tracker database", e);
        }
    }

    private void createEntriesTableIfMissing(Statement st) throws SQLException {
        st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS time_off_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    entry_date TEXT NOT NULL,
                    type TEXT NOT NULL CHECK(type IN ('VACATION','ETO','HOLIDAY','LIMITED_SERVICE','WORKING_HOLIDAY')),
                    status TEXT NOT NULL CHECK(status IN ('SCHEDULED','TAKEN')),
                    hours REAL NOT NULL CHECK(hours > 0),
                    notes TEXT NOT NULL DEFAULT '',
                    UNIQUE(entry_date)
                )
                """);
    }

    private void migrateEntryTypesIfNeeded(Connection connection) throws SQLException {
        String tableSql = null;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT sql FROM sqlite_master WHERE type='table' AND name='time_off_entries'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) tableSql = rs.getString(1);
        }

        if (tableSql == null || tableSql.contains("WORKING_HOLIDAY")) return;

        boolean oldAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("ALTER TABLE time_off_entries RENAME TO time_off_entries_old");
            st.executeUpdate("""
                    CREATE TABLE time_off_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        entry_date TEXT NOT NULL,
                        type TEXT NOT NULL CHECK(type IN ('VACATION','ETO','HOLIDAY','LIMITED_SERVICE','WORKING_HOLIDAY')),
                        status TEXT NOT NULL CHECK(status IN ('SCHEDULED','TAKEN')),
                        hours REAL NOT NULL CHECK(hours > 0),
                        notes TEXT NOT NULL DEFAULT '',
                        UNIQUE(entry_date)
                    )
                    """);
            st.executeUpdate("""
                    INSERT INTO time_off_entries(id, entry_date, type, status, hours, notes)
                    SELECT id, entry_date, type, status, hours, notes FROM time_off_entries_old
                    """);
            st.executeUpdate("DROP TABLE time_off_entries_old");
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }
}
