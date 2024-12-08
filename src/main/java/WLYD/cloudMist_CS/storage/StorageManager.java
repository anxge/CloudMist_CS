package WLYD.cloudMist_CS.storage;

import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.data.PlayerData;
import WLYD.cloudMist_CS.data.GameData;
import WLYD.cloudMist_CS.storage.yaml.YamlStorage;
import WLYD.cloudMist_CS.storage.mysql.MySQLStorage;
import java.io.File;
import java.util.UUID;

public class StorageManager {
    private final CloudMist_CS plugin;
    private IStorage storage;
    
    public StorageManager(CloudMist_CS plugin) {
        this.plugin = plugin;
        initStorage();
    }
    
    private void initStorage() {
        String type = plugin.getConfig().getString("storage.type", "yaml");
        boolean mysqlEnabled = plugin.getConfig().getBoolean("storage.mysql.enabled", false);
        
        if (type.equalsIgnoreCase("mysql") && mysqlEnabled) {
            storage = new MySQLStorage(plugin);
        } else {
            storage = new YamlStorage(plugin);
        }
        
        storage.init();
    }
    
    private IStorage createStorage(StorageType type) {
        switch (type) {
            case MYSQL:
                return new MySQLStorage(plugin);
            case YAML:
            default:
                return new YamlStorage(plugin);
        }
    }
    
    public void migrateStorage(StorageType from, StorageType to) {
        IStorage sourceStorage = createStorage(from);
        IStorage targetStorage = createStorage(to);
        
        // 迁移玩家数据
        File playerDir = new File(plugin.getDataFolder(), "data/players");
        if (playerDir.exists()) {
            for (File file : playerDir.listFiles()) {
                try {
                    UUID playerId = UUID.fromString(file.getName().replace(".yml", ""));
                    PlayerData data = sourceStorage.loadPlayerData(playerId);
                    if (data != null) {
                        targetStorage.savePlayerData(playerId, data);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("迁移玩家数据失败: " + file.getName());
                }
            }
        }
        
        // 迁移游戏数据
        File gameDir = new File(plugin.getDataFolder(), "data/games");
        if (gameDir.exists()) {
            for (File file : gameDir.listFiles()) {
                try {
                    String gameName = file.getName().replace(".yml", "");
                    GameData data = sourceStorage.loadGameData(gameName);
                    if (data != null) {
                        targetStorage.saveGameData(gameName, data);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("迁移游戏数据失败: " + file.getName());
                }
            }
        }
    }
    
    public IStorage getStorage() {
        return storage;
    }
} 