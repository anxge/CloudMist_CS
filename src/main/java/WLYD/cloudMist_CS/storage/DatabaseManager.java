package WLYD.cloudMist_CS.storage;

import WLYD.cloudMist_CS.CloudMist_CS;
import org.bukkit.configuration.file.FileConfiguration;
import java.sql.Connection;
import java.sql.SQLException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {
    private final CloudMist_CS plugin;
    private HikariDataSource dataSource;
    private final String tablePrefix;
    
    public DatabaseManager(CloudMist_CS plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.tablePrefix = config.getString("storage.mysql.table_prefix", "cs_");
        setupPool();
    }
    
    private void setupPool() {
        FileConfiguration config = plugin.getConfig();
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s",
            config.getString("storage.mysql.host", "localhost"),
            config.getInt("storage.mysql.port", 3306),
            config.getString("storage.mysql.database", "cloudmist_cs")));
        hikariConfig.setUsername(config.getString("storage.mysql.username", "root"));
        hikariConfig.setPassword(config.getString("storage.mysql.password", ""));
        
        // 连接池设置
        hikariConfig.setMinimumIdle(config.getInt("storage.mysql.pool.min_connections", 3));
        hikariConfig.setMaximumPoolSize(config.getInt("storage.mysql.pool.max_connections", 10));
        hikariConfig.setConnectionTimeout(config.getLong("storage.mysql.pool.timeout", 30000));
        hikariConfig.setKeepaliveTime(300000); // 5分钟
        
        // 添加更多连接池优化配置
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.setAutoCommit(true);
        hikariConfig.setPoolName("CloudMist-Pool");
        
        // 添加连接测试配置
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setValidationTimeout(3000);
        
        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            plugin.getLogger().severe("无法初始化数据库连接池: " + e.getMessage());
            throw new RuntimeException("数据库连接失败", e);
        }
    }
    
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    public String getTablePrefix() {
        return tablePrefix;
    }
    
    public boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
} 