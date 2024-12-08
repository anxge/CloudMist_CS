package WLYD.cloudMist_CS.weapon;

import java.util.concurrent.atomic.AtomicInteger;

public class WeaponStats {
    private final AtomicInteger kills = new AtomicInteger(0);
    private final AtomicInteger shots = new AtomicInteger(0);
    private final AtomicInteger hits = new AtomicInteger(0);
    private final AtomicInteger headshots = new AtomicInteger(0);
    private final AtomicInteger purchases = new AtomicInteger(0);
    
    public WeaponStats() {
    }
    
    public void addKill() { kills.incrementAndGet(); }
    public void addShot() { shots.incrementAndGet(); }
    public void addHit() { hits.incrementAndGet(); }
    public void addHeadshot() { headshots.incrementAndGet(); }
    public void addPurchase() { purchases.incrementAndGet(); }
    
    public double getAccuracy() {
        int totalShots = shots.get();
        return totalShots == 0 ? 0 : (double) hits.get() / totalShots;
    }
    
    public double getHeadshotRatio() {
        int totalKills = kills.get();
        return totalKills == 0 ? 0 : (double) headshots.get() / totalKills;
    }
} 