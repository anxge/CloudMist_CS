package WLYD.cloudMist_CS.game;

import org.bukkit.Location;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import WLYD.cloudMist_CS.CloudMist_CS;
import java.util.Optional;

public class BombSiteManager {
    private final CloudMist_CS plugin;
    private final Set<BombSite> bombSites = new HashSet<>();
    private BombSite activeBombSite = null;
    
    public BombSiteManager(CloudMist_CS plugin) {
        this.plugin = plugin;
    }
    
    public void addBombSite(Location location) {
        bombSites.add(new BombSite("site" + (bombSites.size() + 1), location, 5.0));
    }
    
    public void registerBombSite(String name, Location location, double radius) {
        bombSites.add(new BombSite(name, location, radius));
    }
    
    public boolean isInBombSite(Location location) {
        for (BombSite site : bombSites) {
            if (site.isInRange(location)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean canPlantBomb(Location location) {
        return bombSites.stream().anyMatch(site -> site.isInRange(location));
    }
    
    public boolean plantBomb(Location location) {
        Optional<BombSite> site = bombSites.stream()
            .filter(s -> s.isInRange(location))
            .findFirst();
            
        if (site.isPresent()) {
            activeBombSite = site.get();
            activeBombSite.plant(location);
            return true;
        }
        return false;
    }
    
    public boolean canDefuseBomb(Location location) {
        return activeBombSite != null && 
               activeBombSite.isPlanted() && 
               activeBombSite.getPlantedLocation().distance(location) <= 
                   plugin.getConfigManager().getGameConfig().getDouble("bomb.defuse_range", 2.0);
    }
    
    public BombSite getNearestBombSite(Location location) {
        return bombSites.stream()
            .min((a, b) -> Double.compare(
                a.getCenter().distance(location),
                b.getCenter().distance(location)))
            .orElse(null);
    }
    
    public Set<BombSite> getBombSites() {
        return new HashSet<>(bombSites);
    }
    
    public void removeBombSite(String name) {
        bombSites.removeIf(site -> site.getName().equals(name));
    }
    
    public BombSite getBombSite(String name) {
        return bombSites.stream()
            .filter(site -> site.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
    
    public List<Location> getBombSiteLocations() {
        List<Location> locations = new ArrayList<>();
        for (BombSite site : bombSites) {
            locations.add(site.getLocation());
        }
        return locations;
    }
    
    public boolean isInDefuseRange(Location location) {
        if (location == null) {
            return false;
        }
        double defuseRange = plugin.getConfigManager().getGameConfig()
            .getDouble("bomb.defuse_range", 2.0);
        for (BombSite site : bombSites) {
            if (site.getLocation().distance(location) <= defuseRange) {
                return true;
            }
        }
        return false;
    }
    
    public void debugPrintBombSites() {
        if (bombSites.isEmpty()) {
            plugin.getLogger().info("当前没有任何炸弹安装点");
            return;
        }
        plugin.getLogger().info("当前炸弹安装点列表：");
        for (BombSite site : bombSites) {
            plugin.getLogger().info(String.format("- %s: 位置=%s, 半径=%f",
                site.getName(),
                site.getLocation().toString(),
                site.getRadius()));
        }
    }
} 