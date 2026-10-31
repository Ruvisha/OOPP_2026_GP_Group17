package com.fot.ams.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and provides database configuration settings from config.properties.
 */
public class DatabaseConfig {
    private static final Properties properties = new Properties();

    private static String host = "localhost";
    private static int port = 3306;
    private static String database = "faculty_academic_management";
    private static String username = "root";
    private static String password = "";
    private static boolean useSSL = false;
    private static String serverTimezone = "UTC";
    private static boolean allowPublicKeyRetrieval = true;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
                host = properties.getProperty("db.host", host);
                try {
                    port = Integer.parseInt(properties.getProperty("db.port", String.valueOf(port)));
                } catch (NumberFormatException ignored) {}
                database = properties.getProperty("db.database", database);
                username = properties.getProperty("db.username", username);
                password = properties.getProperty("db.password", password);
                useSSL = Boolean.parseBoolean(properties.getProperty("db.useSSL", String.valueOf(useSSL)));
                serverTimezone = properties.getProperty("db.serverTimezone", serverTimezone);
                allowPublicKeyRetrieval = Boolean.parseBoolean(properties.getProperty("db.allowPublicKeyRetrieval", String.valueOf(allowPublicKeyRetrieval)));
            } else {
                System.out.println("[DatabaseConfig] config.properties not found in classpath. Using default XAMPP credentials.");
            }
        } catch (IOException e) {
            System.err.println("[DatabaseConfig] Error loading config.properties: " + e.getMessage());
        }
    }

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

    public static String getDatabase() {
        return database;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getJdbcUrl() {
        return String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=%b&serverTimezone=%s&allowPublicKeyRetrieval=%b",
            host, port, database, useSSL, serverTimezone, allowPublicKeyRetrieval
        );
    }
}
