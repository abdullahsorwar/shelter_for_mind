package com.the_pathfinders.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class DB {
    private static HikariDataSource ds;

    private DB() {}

    /** Initialize the pool with a full JDBC URL (includes user/password/ssl). */
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