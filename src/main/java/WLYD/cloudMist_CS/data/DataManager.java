package WLYD.cloudMist_CS.data;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.UUID;
import WLYD.cloudMist_CS.CloudMist_CS;

public class DataManager {
    private final CloudMist_CS plugin;
    private final File dataFolder;
    
    public DataManager(CloudMist_CS plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new RuntimeException("无法创建数据目录: " + dataFolder.getPath());
        }
        
        // 创建子目录
        createSubDirectory("maps");
        createSubDirectory("players");
    }
    
    private void createSubDirectory(String name) {
        File dir = new File(dataFolder, name);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("无法创建目录: " + dir.getPath());
        }
    }
    
    // 保存地图数据
    public void saveMapData(String mapName, MapData data) {
        File file = new File(dataFolder, "maps/" + mapName + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("name", data.getName());
        config.set("t_spawn", data.getTSpawn());
        config.set("ct_spawn", data.getCTSpawn());
        config.set("bomb_sites", data.getBombSites());
        config.set("buy_zones", data.getBuyZones());
        
        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("保存地图数据失败: " + mapName);
            e.printStackTrace();
        }
    }
    
    // 保存玩家数据
    public void savePlayerData(UUID playerId, PlayerData data) {
        File file = new File(dataFolder, "players/" + playerId.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("stats.kills", data.getKills());
        config.set("stats.deaths", data.getDeaths());
        config.set("stats.wins", data.getWins());
        config.set("stats.losses", data.getLosses());
        config.set("stats.bomb_plants", data.getBombPlants());
        config.set("stats.bomb_defuses", data.getBombDefuses());
        config.set("loadouts", data.getLoadouts());
        
        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("保存玩家数据失败: " + playerId);
            e.printStackTrace();
        }
    }
    
    // 加载地图数据
    public MapData loadMapData(String mapName) {
        File file = new File(dataFolder, "maps/" + mapName + ".yml");
        if (!file.exists()) {
            return null;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return MapData.fromConfig(config);
    }
    
    // 加载玩家数据
    public PlayerData loadPlayerData(UUID playerId) {
        File file = new File(dataFolder, "players/" + playerId.toString() + ".yml");
        if (!file.exists()) {
            return new PlayerData(); // 返回新的空数据
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return PlayerData.fromConfig(config);
    }
} 