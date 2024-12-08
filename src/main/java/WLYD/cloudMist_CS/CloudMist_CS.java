package WLYD.cloudMist_CS;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import WLYD.cloudMist_CS.game.GameManager;
import WLYD.cloudMist_CS.game.CSGame;
import WLYD.cloudMist_CS.weapon.WeaponManager;
import WLYD.cloudMist_CS.data.DataManager;
import WLYD.cloudMist_CS.data.PlayerData;
import WLYD.cloudMist_CS.config.ConfigManager;
import WLYD.cloudMist_CS.listener.BombListener;
import WLYD.cloudMist_CS.listener.FreezeTimeListener;
import WLYD.cloudMist_CS.log.LogManager;
import WLYD.cloudMist_CS.commands.CommandManager;
import WLYD.cloudMist_CS.event.GameEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import WLYD.cloudMist_CS.scoreboard.ScoreboardManager;

public final class CloudMist_CS extends JavaPlugin {
    private static volatile CloudMist_CS instance;
    private ConfigManager configManager;
    private GameManager gameManager;
    private WeaponManager weaponManager;
    private DataManager dataManager;
    private LogManager logManager;
    private BombListener bombListener;
    private ScoreboardManager scoreboardManager;
    private final String mainLogo = String.join("\n", new String[]{
        "§b╔════════════════════════════════════════╗",
        "§b║            CloudMist CS                ║",
        "§b              [云雾CS插件]                  ",
        "§b║            [雾里云端开发]                ║",
        "§b║        Version: 1.0.0-BETA             ║",
        "§b╚════════════════════════════════════════╝"
    });
    private final String pixelLogo = String.join("\n", new String[]{
        "§d雾里云端开发组祝您游玩愉快",
        "§e██╗    ██╗   §b██╗      ╔██╗    §a ██████╗  §c███████╗",
        "§e██║    ██║   §b╚██╗    ╔██╝     §a██╔════╝  §c██╔════╝",
        "§e██║ █╗ ██║    §b╚██╗══╔██╝      §a██║       §c███████╗",
        "§e██║███╗██║       §b║██║         §a██║       §c╚════██║",
        "§e╚███╔███╔╝       §b║██║         §a╚██████╗  §c███████║",
        "§e ╚══╝╚══╝        §b╚══╝         §a ╚═════╝  §c╚══════╝"
    });
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    
    public CloudMist_CS() {
        // 使用Builder模式优化Gson实例创建
    }
    
    public Gson getGson() {
        return GSON;
    }
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        
        // 初始化日志管理器
        logManager = LogManager.getInstance(this);
        
        // 保存默认配置文件
        saveDefaultConfig();
        
        // 显示主LOGO
        getLogger().info("\n" + mainLogo);
        
        // 2. 初始化配置管理器
        configManager = new ConfigManager(this);
        
        // 3. 初始化数据管理器
        dataManager = new DataManager(this);
        
        // 4. 初始化武器管理器
        weaponManager = new WeaponManager(this, true);
        
        // 5. 初始化游戏管理器
        gameManager = new GameManager(weaponManager);
        
        // 6. 初始化日志管理器
        logManager = LogManager.getInstance(this);
        
        // 7. 初始化炸弹监听器
        bombListener = new BombListener(this);
        
        // 8. 注册命令
        CommandManager commandManager = new CommandManager(this);
        getCommand("cmcs").setExecutor(commandManager.getPlayerCommand());
        getCommand("cmcsadmin").setExecutor(commandManager.getAdminCommand());
        
        // 9. 注册Tab补全
        getCommand("cmcs").setTabCompleter(commandManager.getPlayerCommand());
        getCommand("cmcsadmin").setTabCompleter(commandManager.getAdminCommand());
        
        // 10. 注册事件监听器
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BombListener(this), this);
        pm.registerEvents(new FreezeTimeListener(this), this);
        pm.registerEvents(new GameEventListener(this), this);
        
        // 11. 输出启动信息
        getLogger().info("CloudMist CS 插件已成功启动! 可用命令: /cmcs, /cmcsadmin");
        
        // 12. 延迟10秒后打印LOGO和欢迎信息
        Bukkit.getScheduler().runTaskLater(this, () -> {
            getLogger().info(pixelLogo);
            Bukkit.broadcastMessage("§6CloudMist CS §b=》》》 §a感谢您使用雾里云端插件，祝您生活愉快！");
        }, 20L * 10);
        
        // 清理旧日志
        int keepDays = getConfig().getInt("log.keep_days", 7);
        logManager.cleanOldLogs(keepDays);
        
        // 初始化计分板管理器
        scoreboardManager = new ScoreboardManager(this);
        
        LogManager logManager = LogManager.getInstance(this);
        logManager.scheduleLogCleanup();
    }

    @Override
    public void onDisable() {
        // 打印 LOGO
        getLogger().info(pixelLogo);
        
        // 关闭日志系统
        if (logManager != null) {
            logManager.close();
        }
        
        // 保存所有游戏数据
        if (gameManager != null) {
            gameManager.saveAllGames();
        }
        
        // 保存所有玩家数据
        if (dataManager != null) {
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                PlayerData data = dataManager.loadPlayerData(player.getUniqueId());
                dataManager.savePlayerData(player.getUniqueId(), data);
            }
        }
        
        saveConfig();
        getLogger().info("CloudMist CS 插件已关闭!");
    }
    
    // 获取插件实的静态方法
    public static CloudMist_CS getInstance() {
        return instance;
    }
    
    // 获取置件
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public WeaponManager getWeaponManager() {
        return weaponManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public LogManager getLogManager() {
        return logManager;
    }
    
    public BombListener getBombListener() {
        return bombListener;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    // 备份游戏数据
    public void backupGames() {
        try {
            // 创建备份文件夹
            File backupDir = new File(getDataFolder(), "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            // 创建带时间戳的备份文件
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File backupFile = new File(backupDir, "games_" + timestamp + ".yml");
            
            // 创建配置文件
            YamlConfiguration config = new YamlConfiguration();
            
            // 保存每个游戏的数据
            for (Map.Entry<String, CSGame> entry : gameManager.getGames().entrySet()) {
                String gameName = entry.getKey();
                CSGame game = entry.getValue();
                
                ConfigurationSection gameSection = config.createSection("games." + gameName);
                // 保存游戏数据
                game.saveToConfig(gameSection);
            }
            
            // 保存配置到文件
            config.save(backupFile);
            getLogger().info("游戏数据已备份到: " + backupFile.getName());
            
        } catch (Exception e) {
            getLogger().severe("备份游戏数据时发生错误: " + e.getMessage());
            if (getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
    }
}

