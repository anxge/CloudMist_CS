package WLYD.cloudMist_CS.log;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

public class LogConfig {
    private boolean enabled;
    private int keepDays;
    private boolean writeToFile;
    private LogLevel logLevel;
    private Map<LogCategory, Boolean> categoryStatus;

    // 默认构造函数
    public LogConfig() {
        this.enabled = true;
        this.keepDays = 7;
        this.writeToFile = true;
        this.logLevel = LogLevel.INFO;
        this.categoryStatus = new EnumMap<>(LogCategory.class);
        
        // 默认所有类别都启用
        for (LogCategory category : LogCategory.values()) {
            this.categoryStatus.put(category, true);
        }
    }

    // Getter 和 Setter 方法
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getKeepDays() {
        return keepDays;
    }

    public void setKeepDays(int keepDays) {
        this.keepDays = keepDays;
    }

    public boolean isWriteToFile() {
        return writeToFile;
    }

    public void setWriteToFile(boolean writeToFile) {
        this.writeToFile = writeToFile;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public Map<LogCategory, Boolean> getCategoryStatus() {
        return categoryStatus;
    }

    public void setCategoryStatus(Map<LogCategory, Boolean> categoryStatus) {
        this.categoryStatus = categoryStatus;
    }

    // 检查特定类别是否启用
    public boolean isCategoryEnabled(LogCategory category) {
        return categoryStatus.getOrDefault(category, true);
    }

    // 从配置文件加载日志配置的静态方法
    public static LogConfig loadFromConfig(FileConfiguration config) {
        LogConfig logConfig = new LogConfig();
        logConfig.setEnabled(config.getBoolean("log.enabled", true));
        logConfig.setKeepDays(config.getInt("log.keep_days", 7));
        logConfig.setWriteToFile(config.getBoolean("log.write_to_file", true));
        logConfig.setLogLevel(LogLevel.valueOf(config.getString("log.log_level", "INFO")));
        
        Map<LogCategory, Boolean> categoryStatus = new EnumMap<>(LogCategory.class);
        for (LogCategory category : LogCategory.values()) {
            categoryStatus.put(category, 
                config.getBoolean("log.categories." + category.getCategoryName(), true));
        }
        logConfig.setCategoryStatus(categoryStatus);
        
        return logConfig;
    }
}
