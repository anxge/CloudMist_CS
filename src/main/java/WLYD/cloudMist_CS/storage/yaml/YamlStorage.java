package WLYD.cloudMist_CS.storage.yaml;

import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.storage.IStorage;
import WLYD.cloudMist_CS.data.PlayerData;
import WLYD.cloudMist_CS.data.GameData;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YamlStorage implements IStorage {
    private final CloudMist_CS plugin;
    private final File dataFolder;
    
    public YamlStorage(CloudMist_CS plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
    }
    
    @Override
    public void init() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new RuntimeException("无法创建数据目录");
        }
    }
    
    @Override
    public void close() {
        // YAML不需要关闭连接
    }
    
    @Override
    public boolean isConnected() {
        return true; // YAML总是可用
    }
    
    @Override
    public void savePlayerData(UUID playerId, PlayerData data) {
        File playerDir = new File(dataFolder, "players");
        if (!playerDir.exists() && !playerDir.mkdirs()) {
            plugin.getLogger().severe("无法创建玩家数据目录");
            return;
        }
        
        File file = new File(playerDir, playerId + ".yml");
        File tempFile = new File(playerDir, playerId + ".yml.tmp");
        
        YamlConfiguration config = new YamlConfiguration();
        data.save(config);
        try {
            // 先写入临时文件
            config.save(tempFile);
            // 成功后再重命名
            if (!tempFile.renameTo(file)) {
                throw new IOException("无法重命名临时文件");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("保存玩家数据失败: " + playerId);
            e.printStackTrace();
            tempFile.delete();
        }
    }
    
    @Override
    public PlayerData loadPlayerData(UUID playerId) {
        File file = new File(dataFolder, "players/" + playerId + ".yml");
        if (!file.exists()) {
            return new PlayerData();
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return PlayerData.load(config);
    }
    
    @Override
    public void saveGameData(String gameName, GameData data) {
        File file = new File(dataFolder, "games/" + gameName + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        data.save(config);
        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("保存游戏数据失败: " + gameName);
            e.printStackTrace();
        }
    }
    
    @Override
    public GameData loadGameData(String gameName) {
        File file = new File(dataFolder, "games/" + gameName + ".yml");
        if (!file.exists()) {
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return GameData.load(config);
    }
} 