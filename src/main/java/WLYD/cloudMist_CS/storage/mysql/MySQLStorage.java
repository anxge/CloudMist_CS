package WLYD.cloudMist_CS.storage.mysql;

import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.storage.IStorage;
import WLYD.cloudMist_CS.storage.DatabaseManager;
import WLYD.cloudMist_CS.data.PlayerData;
import WLYD.cloudMist_CS.data.GameData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.sql.SQLException;

public class MySQLStorage implements IStorage {
    private final CloudMist_CS plugin;
    private final DatabaseManager database;
    
    public MySQLStorage(CloudMist_CS plugin) {
        this.plugin = plugin;
        this.database = new DatabaseManager(plugin);
    }
    
    @Override
    public void init() {
        createTables();
    }
    
    private void createTables() {
        String prefix = database.getTablePrefix();
        try (Connection conn = database.getConnection()) {
            // 创建玩家数据表
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS " + prefix + "players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "data TEXT NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // 创建游戏数据表
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS " + prefix + "games (" +
                "name VARCHAR(64) PRIMARY KEY," +
                "data TEXT NOT NULL," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        } catch (Exception e) {
            plugin.getLogger().severe("创建数据表失败: " + e.getMessage());
            throw new RuntimeException("数据库初始化失败", e);
        }
    }
    
    @Override
    public void close() {
        database.close();
    }
    
    @Override
    public boolean isConnected() {
        return database.isConnected();
    }
    
    @Override
    public void savePlayerData(UUID playerId, PlayerData data) {
        String sql = "INSERT INTO " + database.getTablePrefix() + "players (uuid, data) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE data = ?, last_updated = CURRENT_TIMESTAMP";
        for (int retries = 3; retries > 0; retries--) {
            try (Connection conn = database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                String jsonData = plugin.getGson().toJson(data);
                stmt.setString(1, playerId.toString());
                stmt.setString(2, jsonData);
                stmt.setString(3, jsonData);
                stmt.executeUpdate();
                return;
            } catch (SQLException e) {
                if (retries == 1) {
                    plugin.getLogger().severe("保存玩家数据失败(重试耗尽): " + playerId);
                    e.printStackTrace();
                } else {
                    plugin.getLogger().warning("保存玩家数据失败，将重试: " + playerId);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    public PlayerData loadPlayerData(UUID playerId) {
        String sql = "SELECT data FROM " + database.getTablePrefix() + "players WHERE uuid = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String jsonData = rs.getString("data");
                return plugin.getGson().fromJson(jsonData, PlayerData.class);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("加载玩家数据失败: " + playerId);
            e.printStackTrace();
        }
        return new PlayerData();
    }
    
    @Override
    public void saveGameData(String gameName, GameData data) {
        String sql = "INSERT INTO " + database.getTablePrefix() + "games (name, data) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE data = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String jsonData = plugin.getGson().toJson(data);
            stmt.setString(1, gameName);
            stmt.setString(2, jsonData);
            stmt.setString(3, jsonData);
            stmt.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().severe("保存游戏数据失败: " + gameName);
            e.printStackTrace();
        }
    }
    
    @Override
    public GameData loadGameData(String gameName) {
        String sql = "SELECT data FROM " + database.getTablePrefix() + "games WHERE name = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, gameName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String jsonData = rs.getString("data");
                return plugin.getGson().fromJson(jsonData, GameData.class);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("加载游戏数据失败: " + gameName);
            e.printStackTrace();
        }
        return null;
    }
} 