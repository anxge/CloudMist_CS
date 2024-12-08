package WLYD.cloudMist_CS.game;

import org.bukkit.configuration.file.FileConfiguration;

public class GameSettings {
    private int roundTime = 120;              // 回合时间（秒）
    private int startMoney = 800;             // 初始金钱
    private int minPlayersPerTeam = 1;        // 每队最少玩家数
    private int maxPlayersPerTeam = 5;        // 每队最多玩家数
    private int winScore = 16;                // 胜利需要的分数
    private int killReward = 300;             // 击杀奖励
    private int winRoundReward = 3000;        // 赢回合奖励
    private int loseRoundReward = 1400;       // 输回合奖励
    private int freezeTime = 15;               // 回合开始前的冻结时间（秒）
    private boolean friendlyFire = false;     // 是否允许友军伤害
    private int bombTimer = 40;               // C4爆炸时间(秒)
    private int buyTime = 20;                 // 购买时间(秒)
    private int defuseTime = 5;               // 拆弹时间(秒)
    private boolean allowSpectators = true;    // 默认允许观战
    private int warmupTime = 60;              // 热身时间(秒)
    private int maxRounds = 30;                // 最大回合数
    private int overtimeMaxRounds = 6;          // 加时赛最大回合数
    
    // Getters
    public int getRoundTime() { return roundTime; }
    public int getStartMoney() { return startMoney; }
    public int getMinPlayersPerTeam() { return minPlayersPerTeam; }
    public int getMaxPlayersPerTeam() { return maxPlayersPerTeam; }
    public int getWinScore() { return winScore; }
    public int getKillReward() { return killReward; }
    public int getWinRoundReward() { return winRoundReward; }
    public int getLoseRoundReward() { return loseRoundReward; }
    public int getFreezeTime() { return freezeTime; }
    public boolean isFriendlyFire() { return friendlyFire; }
    public int getBombTimer() { return bombTimer; }
    public int getBuyTime() { return buyTime; }
    public int getDefuseTime() { return defuseTime; }
    public boolean isAllowSpectators() { return allowSpectators; }
    public int getWarmupTime() { return warmupTime; }
    public int getMaxRounds() { return maxRounds; }
    public int getOvertimeMaxRounds() { return overtimeMaxRounds; }
    
    // Setters with validation
    public void setRoundTime(int roundTime) {
        if (roundTime >= 60 && roundTime <= 300) {
            this.roundTime = roundTime;
        }
    }
    
    public void setStartMoney(int startMoney) {
        if (startMoney >= 0 && startMoney <= 16000) {
            this.startMoney = startMoney;
        }
    }
    
    public void setMinPlayersPerTeam(int minPlayers) {
        if (minPlayers >= 1 && minPlayers <= maxPlayersPerTeam) {
            this.minPlayersPerTeam = minPlayers;
        }
    }
    
    public void setMaxPlayersPerTeam(int maxPlayers) {
        if (maxPlayers >= minPlayersPerTeam && maxPlayers <= 10) {
            this.maxPlayersPerTeam = maxPlayers;
        }
    }
    
    public void setWinScore(int winScore) {
        if (winScore >= 8 && winScore <= 30) {
            this.winScore = winScore;
        }
    }
    
    public void setKillReward(int killReward) {
        if (killReward >= 0 && killReward <= 3000) {
            this.killReward = killReward;
        }
    }
    
    public void setWinRoundReward(int reward) {
        if (reward >= 1000 && reward <= 5000) {
            this.winRoundReward = reward;
        }
    }
    
    public void setLoseRoundReward(int reward) {
        if (reward >= 1000 && reward <= 3000) {
            this.loseRoundReward = reward;
        }
    }
    
    public void setFreezeTime(int freezeTime) {
        if (freezeTime >= 0 && freezeTime <= 15) {
            this.freezeTime = freezeTime;
        }
    }
    
    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }
    
    public void setBombTimer(int bombTimer) {
        if (bombTimer >= 0 && bombTimer <= 300) {
            this.bombTimer = bombTimer;
        }
    }
    
    public void setBuyTime(int buyTime) {
        if (buyTime >= 0 && buyTime <= 300) {
            this.buyTime = buyTime;
        }
    }
    
    public void setDefuseTime(int defuseTime) {
        if (defuseTime >= 0 && defuseTime <= 300) {
            this.defuseTime = defuseTime;
        }
    }
    
    public void setAllowSpectators(boolean allowSpectators) {
        this.allowSpectators = allowSpectators;
    }
    
    public void setWarmupTime(int warmupTime) {
        if (warmupTime >= 0 && warmupTime <= 300) {
            this.warmupTime = warmupTime;
        }
    }
    
    public void setMaxRounds(int maxRounds) {
        if (maxRounds >= 1 && maxRounds <= 30) {
            this.maxRounds = maxRounds;
        }
    }
    
    public void setOvertimeMaxRounds(int overtimeMaxRounds) {
        if (overtimeMaxRounds >= 1 && overtimeMaxRounds <= 6) {
            this.overtimeMaxRounds = overtimeMaxRounds;
        }
    }
    
    // 添加新的方法来保存和加载设置
    public void saveToConfig(String gameName, FileConfiguration config) {
        String path = "games." + gameName + ".settings.";
        config.set(path + "roundTime", roundTime);
        config.set(path + "startMoney", startMoney);
        config.set(path + "minPlayersPerTeam", minPlayersPerTeam);
        config.set(path + "maxPlayersPerTeam", maxPlayersPerTeam);
        config.set(path + "winScore", winScore);
        config.set(path + "killReward", killReward);
        config.set(path + "winRoundReward", winRoundReward);
        config.set(path + "loseRoundReward", loseRoundReward);
        config.set(path + "freezeTime", freezeTime);
        config.set(path + "friendlyFire", friendlyFire);
        config.set(path + "bombTimer", bombTimer);
        config.set(path + "buyTime", buyTime);
        config.set(path + "defuseTime", defuseTime);
        config.set(path + "allowSpectators", allowSpectators);
        config.set(path + "warmupTime", warmupTime);
        config.set(path + "maxRounds", maxRounds);
        config.set(path + "overtimeMaxRounds", overtimeMaxRounds);
    }
    
    public void loadFromConfig(String gameName, FileConfiguration config) {
        String path = "games." + gameName + ".settings.";
        if (config.contains(path)) {
            setRoundTime(config.getInt(path + "roundTime", 120));
            setStartMoney(config.getInt(path + "startMoney", 800));
            setMinPlayersPerTeam(config.getInt(path + "minPlayersPerTeam", 1));
            setMaxPlayersPerTeam(config.getInt(path + "maxPlayersPerTeam", 5));
            setWinScore(config.getInt(path + "winScore", 16));
            setKillReward(config.getInt(path + "killReward", 300));
            setWinRoundReward(config.getInt(path + "winRoundReward", 3000));
            setLoseRoundReward(config.getInt(path + "loseRoundReward", 1400));
            setFreezeTime(config.getInt(path + "freezeTime", 15));
            setFriendlyFire(config.getBoolean(path + "friendlyFire", false));
            setBombTimer(config.getInt(path + "bombTimer", 0));
            setBuyTime(config.getInt(path + "buyTime", 0));
            setDefuseTime(config.getInt(path + "defuseTime", 0));
            setAllowSpectators(config.getBoolean(path + "allowSpectators", false));
            setWarmupTime(config.getInt(path + "warmupTime", 0));
            setMaxRounds(config.getInt(path + "maxRounds", 30));
            setOvertimeMaxRounds(config.getInt(path + "overtimeMaxRounds", 6));
        }
    }
    
    // 添加预设设置的方法
    public GameSettings setPreset(int roundTime, int startMoney, int minPlayers, 
                                int maxPlayers, int winScore, int killReward,
                                int winReward, int loseReward, int freezeTime, 
                                boolean friendlyFire) {
        setRoundTime(roundTime);
        setStartMoney(startMoney);
        setMinPlayersPerTeam(minPlayers);
        setMaxPlayersPerTeam(maxPlayers);
        setWinScore(winScore);
        setKillReward(killReward);
        setWinRoundReward(winReward);
        setLoseRoundReward(loseReward);
        setFreezeTime(freezeTime);
        setFriendlyFire(friendlyFire);
        return this;
    }
    
    public void applyPreset(GamePreset preset) {
        GameSettings presetSettings = preset.getSettings();
        setPreset(
            presetSettings.getRoundTime(),
            presetSettings.getStartMoney(),
            presetSettings.getMinPlayersPerTeam(),
            presetSettings.getMaxPlayersPerTeam(),
            presetSettings.getWinScore(),
            presetSettings.getKillReward(),
            presetSettings.getWinRoundReward(),
            presetSettings.getLoseRoundReward(),
            presetSettings.getFreezeTime(),
            presetSettings.isFriendlyFire()
        );
    }
    
    public GameSettings() {
        // 默认值
        this.minPlayersPerTeam = 1; // 设置最小玩家数
        this.maxPlayersPerTeam = 5; // 设置最大玩家数
        this.winScore = 16;  // 设置胜利分数
        this.roundTime = 120; // 设置回合时间
        this.freezeTime = 5; // 设置冻结时间
        this.buyTime = 20;   // 设置购买时间
        this.warmupTime = 60; // 设置热身时间
    }
    
    public int getMinPlayers() {
        return minPlayersPerTeam; // 返回最小玩家数
    }
} 