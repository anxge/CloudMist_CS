package WLYD.cloudMist_CS.game;

import org.bukkit.Location;

public class BombSite {
    private final String name;
    private final Location center;
    private final double radius;
    private boolean isPlanted = false;
    private Location plantedLocation = null;
    
    public BombSite(String name, Location center, double radius) {
        this.name = name;
        this.center = center;
        this.radius = radius;
    }
    
    public String getName() {
        return name;
    }
    
    public Location getLocation() {
        return center;
    }
    
    public double getRadius() {
        return radius;
    }
    
    public boolean isInRange(Location location) {
        if (location == null || location.getWorld() != center.getWorld()) {
            return false;
        }
        return center.distance(location) <= radius;
    }
    
    public Location getCenter() {
        return center;
    }
    
    public void plant(Location location) {
        this.isPlanted = true;
        this.plantedLocation = location;
    }
    
    public void defuse() {
        this.isPlanted = false;
        this.plantedLocation = null;
    }
    
    public boolean isPlanted() {
        return isPlanted;
    }
    
    public Location getPlantedLocation() {
        return plantedLocation;
    }
} 