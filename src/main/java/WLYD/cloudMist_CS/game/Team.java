package WLYD.cloudMist_CS.game;

public enum Team {
    TERRORIST("§c恐怖分子", "§c[T]"),
    COUNTER_TERRORIST("§9反恐精英", "§9[CT]"),
    SPECTATOR("§7观察者", "§7[观察]"),
    NONE("§7未选择", "");
    
    private final String displayName;
    private final String prefix;
    
    Team(String displayName, String prefix) {
        this.displayName = displayName;
        this.prefix = prefix;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getPrefix() {
        return prefix;
    }
} 