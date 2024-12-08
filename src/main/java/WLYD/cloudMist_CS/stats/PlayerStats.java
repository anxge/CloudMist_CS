package WLYD.cloudMist_CS.stats;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerStats {
    private final UUID playerId;
    private final AtomicInteger kills = new AtomicInteger(0);
    private final AtomicInteger deaths = new AtomicInteger(0);
    private final AtomicInteger assists = new AtomicInteger(0);
    private final AtomicInteger roundsWon = new AtomicInteger(0);
    private final AtomicInteger roundsLost = new AtomicInteger(0);
    private final AtomicInteger moneyEarned = new AtomicInteger(0);
    private final AtomicInteger moneySpent = new AtomicInteger(0);
    
    public PlayerStats(UUID playerId) {
        this.playerId = playerId;
    }
    
    public PlayerStats() {
        this.playerId = UUID.randomUUID();
    }
    
    // Getters
    public int getKills() { return kills.get(); }
    public int getDeaths() { return deaths.get(); }
    public int getAssists() { return assists.get(); }
    public int getRoundsWon() { return roundsWon.get(); }
    public int getRoundsLost() { return roundsLost.get(); }
    public int getMoneyEarned() { return moneyEarned.get(); }
    public int getMoneySpent() { return moneySpent.get(); }
    public double getKDRatio() {
        return deaths.get() == 0 ? kills.get() : (double) kills.get() / deaths.get();
    }
    public UUID getPlayerId() { return playerId; }
    
    // 使用原子操作确保线程安全
    public void addKill() { kills.incrementAndGet(); }
    public void addDeath() { deaths.incrementAndGet(); }
    public void addAssist() { assists.incrementAndGet(); }
    public void addRoundWon() { roundsWon.incrementAndGet(); }
    public void addRoundLost() { roundsLost.incrementAndGet(); }
    public void addMoneyEarned(int amount) { moneyEarned.addAndGet(amount); }
    public void addMoneySpent(int amount) { moneySpent.addAndGet(amount); }
} 