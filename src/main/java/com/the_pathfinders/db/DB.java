package com.the_pathfinders.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class DB {
    private static HikariDataSource ds;

    // Database configuration constants
    private static final String DB_HOST = "ep-silent-pond-a1hw326v-pooler.ap-southeast-1.aws.neon.tech";
    private static final String DB_NAME = "neondb";
    private static final String DB_USER = "neondb_owner";
    private static final String DB_PASSWORD = "npg_LuaRU0wpTn1K";
    // New JDBC URL: jdbc:postgresql://ep-silent-pond-a1hw326v-pooler.ap-southeast-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_LuaRU0wpTn1K&sslmode=require&channelBinding=require
    private static final String JDBC_URL = String.format(
        "jdbc:postgresql://%s/%s?user=%s&password=%s&sslmode=require&channelBinding=require",
        DB_HOST, DB_NAME, DB_USER, DB_PASSWORD
    );

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
        cfg.setConnectionTimeout(10_000);
        // Extra explicitness for SSL (your URL already has sslmode=require)
        cfg.addDataSourceProperty("ssl", "true");
        cfg.addDataSourceProperty("sslmode", "require");
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