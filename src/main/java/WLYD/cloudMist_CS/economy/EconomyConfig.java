package WLYD.cloudMist_CS.economy;

public class EconomyConfig {
    private int startingMoney = 800;
    private int killReward = 300;
    private int winReward = 3000;
    private int loseReward = 1400;
    
    public int getStartingMoney() { return startingMoney; }
    public int getKillReward() { return killReward; }
    public int getWinReward() { return winReward; }
    public int getLoseReward() { return loseReward; }
    
    public void setStartingMoney(int amount) { this.startingMoney = amount; }
    public void setKillReward(int amount) { this.killReward = amount; }
    public void setWinReward(int amount) { this.winReward = amount; }
    public void setLoseReward(int amount) { this.loseReward = amount; }
} 