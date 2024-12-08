package WLYD.cloudMist_CS.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import WLYD.cloudMist_CS.CloudMist_CS;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.Map;

public class ConfigManager {
    private final CloudMist_CS plugin;
    private final Logger logger;
    private final ConcurrentHashMap<String, FileConfiguration> configCache;
    private final Map<String, File> configFiles;
    
    // 配置文件引用
    private FileConfiguration gameConfig;
    private FileConfiguration economyConfig;
    private FileConfiguration weaponsConfig;
    private FileConfiguration modsConfig;
    
    // 使用枚举管理配置文件
    private enum ConfigType {
        GAME("game"),
        ECONOMY("economy"),
        WEAPONS("weapons"),
        MODS("mods");
        
        private final String fileName;
        ConfigType(String fileName) {
            this.fileName = fileName;
        }
    }
    
    public ConfigManager(CloudMist_CS plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.configCache = new ConcurrentHashMap<>();
        this.configFiles = new ConcurrentHashMap<>();
        
        // 初始化配置
        initializeConfigs();
        validateConfig();
    }
    
    private void initializeConfigs() {
        // 确保所有必需的目录存在
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        File configDir = new File(plugin.getDataFolder(), "configs");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // 先加载主配置文件
        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.saveResource("config.yml", false);
        }
        plugin.reloadConfig();
        
        // 确保所有配置文件都被加载
        loadConfig("game");
        loadConfig("economy");
        loadConfig("weapons");
        loadConfig("mods");
        
        // 验证所有配置
        validateConfig();
    }
    
    private FileConfiguration loadConfig(String name) {
        File configFile = new File(plugin.getDataFolder(), "configs/" + name + ".yml");
        if (!configFile.exists()) {
            plugin.getLogger().warning("配置文件不存在: " + configFile.getPath());
            // 如果文件不存在，创建默认配置
            FileConfiguration defaultConfig = new YamlConfiguration();
            switch (name) {
                case "game":
                    gameConfig = defaultConfig;
                    validateGameConfig(defaultConfig);
                    break;
                case "economy":
                    economyConfig = defaultConfig;
                    validateEconomyConfig(defaultConfig);
                    break;
                case "weapons":
                    weaponsConfig = defaultConfig;
                    validateWeaponsConfig(defaultConfig);
                    break;
                case "mods":
                    modsConfig = defaultConfig;
                    validateModsConfig(defaultConfig);
                    break;
            }
            try {
                defaultConfig.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().severe("无法保存默认配置文件: " + e.getMessage());
            }
            return defaultConfig;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        // 将加载的配置分配给相应的变量
        switch (name) {
            case "game":
                gameConfig = config;
                break;
            case "economy":
                economyConfig = config;
                break;
            case "weapons":
                weaponsConfig = config;
                break;
            case "mods":
                modsConfig = config;
                break;
        }
        return config;
    }
    
    private void loadConfigs() {
        // 确保配置目录存在
        File configDir = new File(plugin.getDataFolder(), "configs");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // 保存所有默认配置文件
        for (ConfigType type : ConfigType.values()) {
            String path = "configs/" + type.fileName + ".yml";
            if (!new File(plugin.getDataFolder(), path).exists()) {
                plugin.saveResource(path, false);
            }
        }
        
        try {
            // 加载配置文件
            gameConfig = loadConfig("game");
            economyConfig = loadConfig("economy");
            weaponsConfig = loadConfig("weapons");
            modsConfig = loadConfig("mods");
            
            // 缓存配置
            configCache.put("game", gameConfig);
            configCache.put("economy", economyConfig);
            configCache.put("weapons", weaponsConfig);
            configCache.put("mods", modsConfig);
        } catch (Exception e) {
            logger.severe("加载配置文件时发生错误: " + e.getMessage());
            if (plugin.getConfig().getBoolean("debug", false)) {
                e.printStackTrace();
            }
            throw new RuntimeException("配置加载失败", e);
        }
    }
    
    public void saveConfig(String name) {
        FileConfiguration config = configCache.get(name);
        File file = configFiles.get(name);
        if (config != null && file != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                logger.severe("保存配置文件失败 " + name + ": " + e.getMessage());
            }
        }
    }
    
    public void reloadConfigs() {
        loadConfigs();
        validateConfig();
    }
    
    // 游戏设置相关方法
    public void setStartMoney(int amount) {
        gameConfig.set("game.start_money", amount);
        saveConfig("game");
    }
    
    public void setRoundTime(int seconds) {
        gameConfig.set("round.time", seconds);
        saveConfig("game");
    }
    
    public void setWinScore(int score) {
        gameConfig.set("game.win_score", score);
        saveConfig("game");
    }
    
    // 配置获取方法
    public FileConfiguration getConfig(String name) {
        return configCache.get(name);
    }
    
    public FileConfiguration getGameConfig() {
        return gameConfig;
    }
    
    public FileConfiguration getEconomyConfig() {
        return economyConfig;
    }
    
    public FileConfiguration getWeaponsConfig() {
        return weaponsConfig;
    }
    
    public FileConfiguration getModsConfig() {
        return modsConfig;
    }
    
    // 配置验证方法
    public void validateConfig() {
        boolean needsSave = false;
        
        // 验证游戏配置
        needsSave |= validateGameConfig(gameConfig);
        
        // 验证经济配置
        needsSave |= validateEconomyConfig(economyConfig);
        
        // 验证武器配置
        needsSave |= validateWeaponsConfig(weaponsConfig);
        
        // 验证模组配置
        needsSave |= validateModsConfig(modsConfig);
        
        // 如果有修改，保存配置
        if (needsSave) {
            saveAllConfigs();
        }
    }
    
    private boolean validateGameConfig(FileConfiguration config) {
        boolean changed = false;
        
        // 游戏基础设置
        Map<String, Object> gameSettings = Map.of(
            "max_players", 10,
            "min_players", 2,
            "win_score", 16,
            "auto_start", true,
            "spawn_protection", true,
            "spawn_protection_time", 5
        );
        
        changed |= validateSection(config, "game", gameSettings);
        
        // 回合设置
        Map<String, Object> roundSettings = Map.of(
            "time", 120,
            "freeze_time", 5,
            "buy_time", 20,
            "warmup_time", 60,
            "overtime", false,
            "overtime_rounds", 6
        );
        
        changed |= validateSection(config, "round", roundSettings);
        
        // 队伍设置
        Map<String, Object> teamSettings = Map.of(
            "min_players", 1,
            "max_players", 5,
            "friendly_fire", false,
            "auto_balance", true,
            "switch_after_round", true
        );
        
        changed |= validateSection(config, "teams", teamSettings);
        
        // 炸弹设置
        Map<String, Object> bombSettings = Map.of(
            "timer", 45,
            "defuse_time", 10,
            "plant_range", 2,
            "defuse_range", 2,
            "plant_time", 3
        );
        
        changed |= validateSection(config, "bomb", bombSettings);
        
        return changed;
    }
    
    private boolean validateEconomyConfig(FileConfiguration config) {
        boolean changed = false;
        
        // 经济基础设置
        Map<String, Object> economySettings = Map.of(
            "max_money", 16000,
            "kill_interest", 0.5,
            "round_loss_increment", true,
            "starting_money", 800,
            "min_money", 0
        );
        
        changed |= validateSection(config, "economy", economySettings);
        
        // 奖励设置
        Map<String, Object> rewardSettings = Map.of(
            "kill", 300,
            "headshot", 100,
            "win_round", 3000,
            "lose_round", 1400,
            "bomb_plant", 300,
            "bomb_defuse", 300,
            "assist", 150
        );
        
        // 失败奖励设置
        Map<String, Object> loseBonusSettings = Map.of(
            "base", 1400,
            "increment", 500,
            "max", 3400
        );
        
        changed |= validateSection(config, "rewards.lose_bonus", loseBonusSettings);
        changed |= validateSection(config, "rewards", rewardSettings);
        
        return changed;
    }
    
    private boolean validateWeaponsConfig(FileConfiguration config) {
        boolean changed = false;
        
        // 武器基础设置
        Map<String, Object> weaponSettings = Map.of(
            "enabled", true,
            "damage_multiplier", 1.0,
            "headshot_multiplier", 2.0
        );
        
        changed |= validateSection(config, "weapons", weaponSettings);
        
        return changed;
    }
    
    private boolean validateModsConfig(FileConfiguration config) {
        boolean changed = false;
        
        Map<String, Object> modSettings = Map.of(
            "enabled", true,
            "tacz_support", true,
            "custom_models", false
        );
        
        changed |= validateSection(config, "mods", modSettings);
        
        return changed;
    }
    
    // 优化配置验证逻辑
    private boolean validateSection(FileConfiguration config, String section, Map<String, Object> defaults) {
        if (config == null) {
            logger.warning("配置文件为空，正在创建默认配置: " + section);
            FileConfiguration newConfig = new YamlConfiguration();
            defaults.forEach((key, value) -> newConfig.set(section + "." + key, value));
            return validateSection(newConfig, section, defaults);
        }

        if (!config.contains(section)) {
            config.createSection(section);
        }

        return defaults.entrySet().stream()
            .filter(entry -> !config.contains(section + "." + entry.getKey()))
            .map(entry -> {
                config.set(section + "." + entry.getKey(), entry.getValue());
                return true;
            })
            .findAny()
            .orElse(false);
    }
    
    private void saveAllConfigs() {
        for (String configName : configCache.keySet()) {
            saveConfig(configName);
        }
    }
    
    public void validateConfigs() {
        validateGameConfig(gameConfig);
        validateWeaponsConfig(weaponsConfig);
        validateModsConfig(modsConfig);
    }
} 