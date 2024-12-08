package WLYD.cloudMist_CS.game;

public enum GamePreset {
    COMPETITIVE("竞技模式", new GameSettings()
        .setPreset(120, 800, 5, 5, 16, 300, 3000, 1400, 5, false)),
        
    CASUAL("休闲模式", new GameSettings()
        .setPreset(180, 1000, 1, 10, 8, 600, 3500, 2000, 10, true)),
        
    PRACTICE("练习模式", new GameSettings()
        .setPreset(300, 16000, 1, 5, 30, 0, 0, 0, 0, true)),
        
    QUICK("快速模式", new GameSettings()
        .setPreset(60, 2000, 1, 3, 8, 500, 2000, 1000, 3, false));
    
    private final String displayName;
    private final GameSettings settings;
    
    GamePreset(String displayName, GameSettings settings) {
        this.displayName = displayName;
        this.settings = settings;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public GameSettings getSettings() {
        return settings;
    }
} 