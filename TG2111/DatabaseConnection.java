package com.fot.ams.database;

import com.fot.ams.config.DatabaseConfig;
import com.fot.ams.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages database connectivity to MySQL through JDBC.
 * Singleton-based connection manager ensuring controlled access.
 */
public class DatabaseConnection {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("MySQL JDBC Driver not found on classpath.", e);
        }
    }

    private DatabaseConnection() {
        // Prevent direct instantiation
    }

    /**
     * Obtains a new connection to the MySQL database.
     *
     * @return active java.sql.Connection
     * @throws DatabaseException if connection fails
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                DatabaseConfig.getJdbcUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword()
            );
        } catch (SQLException e) {
            throw new DatabaseException(
                "Failed to connect to MySQL database (" + DatabaseConfig.getDatabase() +
                ") on " + DatabaseConfig.getHost() + ":" + DatabaseConfig.getPort() +
                ". Ensure XAMPP MySQL is running. Error: " + e.getMessage(), e
            );
        }
    }

    /**
     * Safely closes an open database connection.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("[DatabaseConnection] Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Safely closes arbitrary JDBC resources (Statements, ResultSets, Connections).
     */
    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Tests database connectivity.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}
