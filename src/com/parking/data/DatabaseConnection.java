package com.parking.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages JDBC connection lifecycle, credential configuration, and driver loading
 * for MySQL database connectivity.
 */
public class DatabaseConnection {

    private static final String DEFAULT_URL = 
            "jdbc:mysql://localhost:3306/parking_management_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static String dbUrl = DEFAULT_URL;
    private static String dbUser = DEFAULT_USER;
    private static String dbPassword = DEFAULT_PASSWORD;
    private static boolean driverLoaded = false;

    static {
        loadConfiguration();
        loadDriver();
    }

    /**
     * Attempts to load credentials from db.properties if present.
     */
    private static void loadConfiguration() {
        Properties props = new Properties();

        // 1. Try loading from working directory file
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                props.load(fis);
            } catch (Exception e) {
                System.err.println("Could not load db.properties from root: " + e.getMessage());
            }
        }

        // 2. Try loading from classpath
        if (props.isEmpty()) {
            try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                }
            } catch (Exception ignored) {}
        }

        if (!props.isEmpty()) {
            dbUrl = props.getProperty("db.url", DEFAULT_URL).trim();
            dbUser = props.getProperty("db.user", DEFAULT_USER).trim();
            dbPassword = props.getProperty("db.password", DEFAULT_PASSWORD).trim();
        }
    }

    private static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e1) {
            try {
                // Fallback for older MySQL connector versions
                Class.forName("com.mysql.jdbc.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException e2) {
                driverLoaded = false;
                System.err.println("Note: MySQL JDBC Driver not found on classpath. Ensure mysql-connector-j is added to project libraries.");
            }
        }
    }

    /**
     * Obtains an active connection to the MySQL database.
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Tests if connection to the MySQL database succeeds.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Allows dynamic configuration of database credentials at runtime.
     */
    public static void setCredentials(String url, String user, String password) {
        dbUrl = url;
        dbUser = user;
        dbPassword = password;
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static boolean isDriverLoaded() {
        return driverLoaded;
    }

    /**
     * Safe utility to close JDBC statements, result sets, or connections without throwing checked exceptions.
     */
    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {}
            }
        }
    }
}
