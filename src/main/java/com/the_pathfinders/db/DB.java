package com.the_pathfinders.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.the_pathfinders.util.EncryptedConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DB {
    private static HikariDataSource ds;

    // Database configuration loaded from encrypted config
    private static String DB_HOST;
    private static String DB_NAME;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_SSL_MODE;
    private static String DB_SSL_CHANNEL_BINDING;
    private static String JDBC_URL;
    
    static {
        // Load configuration on class initialization
        try {
            loadConfiguration();
        } catch (Exception e) {
            System.err.println("Failed to load database configuration: " + e.getMessage());
            throw new RuntimeException("Database configuration error", e);
        }
    }
    
    /**
     * Load database configuration from encrypted config file
     */
    private static void loadConfiguration() {
        Properties props = EncryptedConfig.loadDatabaseConfig();
        
        DB_HOST = props.getProperty("db.host");
        DB_NAME = props.getProperty("db.name");
        DB_USER = props.getProperty("db.user");
        DB_PASSWORD = props.getProperty("db.password");
        DB_SSL_MODE = props.getProperty("db.ssl.mode", "require");
        DB_SSL_CHANNEL_BINDING = props.getProperty("db.ssl.channel.binding", "require");
        
        // Validate required properties
        if (DB_HOST == null || DB_NAME == null || DB_USER == null || DB_PASSWORD == null) {
            throw new IllegalStateException("Missing required database configuration properties");
        }
        
        // Build JDBC URL
        JDBC_URL = String.format(
            "jdbc:postgresql://%s/%s?user=%s&password=%s&sslmode=%s&channelBinding=%s",
            DB_HOST, DB_NAME, DB_USER, DB_PASSWORD, DB_SSL_MODE, DB_SSL_CHANNEL_BINDING
        );
        
        System.out.println("✓ Database configuration loaded successfully");
    }

    private DB() {}

    /** Initialize the connection pool with default settings */
    public static void init() {
        init(JDBC_URL);
    }

    /** Initialize the pool with custom JDBC URL (includes user/password/ssl) - for testing/development */
    public static void init(String jdbcUrl) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setMaximumPoolSize(5);
        cfg.setMinimumIdle(0);
        cfg.setIdleTimeout(30_000);
        cfg.setConnectionTimeout(30_000); // Increased to 30s for Neon wake-up
        cfg.setValidationTimeout(5_000);
        cfg.setInitializationFailTimeout(-1); // Don't fail fast on startup
        // Extra explicitness for SSL (your URL already has sslmode=require)
        cfg.addDataSourceProperty("ssl", "true");
        cfg.addDataSourceProperty("sslmode", "require");
        cfg.addDataSourceProperty("connectTimeout", "30");
        ds = new HikariDataSource(cfg);
    }

    public static Connection getConnection() throws SQLException {
        if (ds == null) throw new IllegalStateException("DB.init() not called");
        return ds.getConnection();
    }

    public static void shutdown() {
        if (ds != null) ds.close();
    }
}