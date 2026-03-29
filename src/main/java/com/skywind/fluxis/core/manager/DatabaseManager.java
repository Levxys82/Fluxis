package com.skywind.fluxis.core.manager;

import com.skywind.fluxis.core.Fluxis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {

    private final Fluxis core;
    private final boolean mysqlEnabled;
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseManager(Fluxis core) {
        this.core = core;

        String mode = core.getDatabaseConfig().getString("database.mode", "mysql");
        this.mysqlEnabled = "mysql".equalsIgnoreCase(mode);

        String host = core.getDatabaseConfig().getString("mysql.host", "127.0.0.1");
        int port = core.getDatabaseConfig().getInt("mysql.port", 3306);
        String database = core.getDatabaseConfig().getString("mysql.database", "fluxis");
        this.username = core.getDatabaseConfig().getString("mysql.username", "root");
        this.password = core.getDatabaseConfig().getString("mysql.password", "");
        this.jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        if (mysqlEnabled) {
            try {
                setupSchema();
                core.getLogger().info("MySQL storage initialized successfully.");
            } catch (SQLException e) {
                core.getLogger().severe("MySQL initialization failed: " + e.getMessage());
            }
        }
    }

    public boolean isMysqlEnabled() {
        return mysqlEnabled;
    }

    public void saveJson(String key, String jsonPayload) {
        if (!mysqlEnabled) return;
        String sql = "INSERT INTO fluxis_storage(storage_key, payload) VALUES(?, ?) ON DUPLICATE KEY UPDATE payload = VALUES(payload)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, jsonPayload);
            ps.executeUpdate();
        } catch (SQLException e) {
            core.getLogger().severe("Failed to save MySQL payload for key '" + key + "': " + e.getMessage());
        }
    }

    public String loadJson(String key) {
        if (!mysqlEnabled) return null;
        String sql = "SELECT payload FROM fluxis_storage WHERE storage_key = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("payload");
                }
            }
        } catch (SQLException e) {
            core.getLogger().severe("Failed to load MySQL payload for key '" + key + "': " + e.getMessage());
        }
        return null;
    }

    private void setupSchema() throws SQLException {
        String ddl = """
                CREATE TABLE IF NOT EXISTS fluxis_storage (
                    storage_key VARCHAR(64) PRIMARY KEY,
                    payload LONGTEXT NOT NULL,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(ddl)) {
            ps.execute();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}
