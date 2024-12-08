package WLYD.cloudMist_CS.log;

import WLYD.cloudMist_CS.CloudMist_CS;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.StringWriter;
import java.util.EnumMap;
import java.util.Map;

public class LogManager {
    private static LogManager INSTANCE;
    private final CloudMist_CS plugin;
    private LogConfig logConfig;
    private Map<LogCategory, PrintWriter> categoryWriters;

    private LogManager(CloudMist_CS plugin) {
        this.plugin = plugin;
        this.categoryWriters = new EnumMap<>(LogCategory.class);
        initializeLogSystem();
    }

    public static synchronized LogManager getInstance(CloudMist_CS plugin) {
        if (INSTANCE == null) {
            INSTANCE = new LogManager(plugin);
        }
        return INSTANCE;
    }

    private void initializeLogSystem() {
        // 加载日志配置
        this.logConfig = LogConfig.loadFromConfig(plugin.getConfig());
        
        // 初始化日志文件
        if (logConfig.isWriteToFile()) {
            initLogFiles();
        }
    }

    private void initLogFiles() {
        File logDir = new File(plugin.getDataFolder(), "logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        
        for (LogCategory category : LogCategory.values()) {
            if (logConfig.isCategoryEnabled(category)) {
                try {
                    File logFile = new File(logDir, category.getCategoryName() + "-" + date + ".log");
                    categoryWriters.put(category, new PrintWriter(new FileWriter(logFile, true)));
                } catch (IOException e) {
                    plugin.getLogger().severe("无法创建日志文件: " + category);
                }
            }
        }
    }

    public void log(LogCategory category, LogLevel level, String message) {
        // 检查日志级别和类别是否启用
        if (!logConfig.isEnabled() || 
            !logConfig.isCategoryEnabled(category) || 
            level.ordinal() < logConfig.getLogLevel().ordinal()) {
            return;
        }

        // 控制台日志
        switch (level) {
            case DEBUG:
                plugin.getLogger().info("[DEBUG] " + message);
                break;
            case WARNING:
                plugin.getLogger().warning(message);
                break;
            case ERROR:
                plugin.getLogger().severe(message);
                break;
            default:
                plugin.getLogger().info(message);
        }

        // 文件日志
        if (logConfig.isWriteToFile()) {
            PrintWriter writer = categoryWriters.get(category);
            if (writer != null) {
                writer.println(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + 
                    " [" + level + "] " + message
                );
                writer.flush();
            }
        }
    }

    // 便捷方法
    public void logGame(String message) {
        log(LogCategory.GAME, LogLevel.INFO, message);
    }

    public void logEconomy(String message) {
        log(LogCategory.ECONOMY, LogLevel.INFO, message);
    }

    public void logError(String message, Throwable e) {
        log(LogCategory.ERROR, LogLevel.ERROR, message);
        if (e != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log(LogCategory.ERROR, LogLevel.ERROR, sw.toString());
        }
    }

    // 资源清理方法
    public void close() {
        for (PrintWriter writer : categoryWriters.values()) {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void cleanOldLogs(int keepDays) {
        File logDir = new File(plugin.getDataFolder(), "logs");
        if (!logDir.exists()) return;
        
        long cutoffTime = System.currentTimeMillis() - (keepDays * 24L * 60L * 60L * 1000L);
        File[] files = logDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() < cutoffTime) {
                    file.delete();
                }
            }
        }
    }

    public void scheduleLogCleanup() {
        int keepDays = plugin.getConfig().getInt("log.keep_days", 7);
        // 每天执行一次清理
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            cleanOldLogs(keepDays);
        }, 20L * 60 * 60 * 24, 20L * 60 * 60 * 24); // 24小时执行一次
    }
} 