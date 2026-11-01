package com.fot.ams.tg2102;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * TG2102-only JDBC connection settings. Keeps TG2102 compatible with the
 * existing project configuration while targeting this user's XAMPP instance.
 */
public final class TG2102DatabaseConnection {
    private static final String HOST = System.getProperty("tg2102.db.host", "localhost");
    private static final int PORT = Integer.getInteger("tg2102.db.port", 4307);
    private static final String DATABASE = System.getProperty("tg2102.db.database", "faculty_academic_management");
    private static final String USERNAME = System.getProperty("tg2102.db.username", "root");
    private static final String PASSWORD = System.getProperty("tg2102.db.password", "");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC Driver is missing from the project dependencies.", e);
        }
    }

    private TG2102DatabaseConnection() { }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        try {
            return DriverManager.getConnection(url, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new SQLException("TG2102 could not connect to MySQL database '" + DATABASE
                    + "' at " + HOST + ":" + PORT + ". Check that XAMPP MySQL is running and the database exists. "
                    + e.getMessage(), e);
        }
    }
}
