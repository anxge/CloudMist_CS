package WLYD.cloudMist_CS.log;

public enum LogLevel {
    DEBUG(0), INFO(1), WARNING(2), ERROR(3);
    
    private final int level;
    
    LogLevel(int level) {
        this.level = level;
    }
    
    public boolean isEnabled(LogLevel minLevel) {
        return this.level >= minLevel.level;
    }
} 