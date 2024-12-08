package WLYD.cloudMist_CS.data;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.Map;

public class GameData {
    private String name;
    private Location lobbyLocation;
    private Location tSpawn;
    private Location ctSpawn;
    private Map<String, Object> settings;
    
    public GameData() {
        this.settings = new HashMap<>();
    }
    
    public void save(YamlConfiguration config) {
        config.set("name", name);
        config.set("lobbyLocation", lobbyLocation);
        config.set("tSpawn", tSpawn);
        config.set("ctSpawn", ctSpawn);
        config.set("settings", settings);
    }
    
    public static GameData load(YamlConfiguration config) {
        GameData data = new GameData();
        data.name = config.getString("name", "");
        data.lobbyLocation = config.getLocation("lobbyLocation");
        data.tSpawn = config.getLocation("tSpawn");
        data.ctSpawn = config.getLocation("ctSpawn");
        
        ConfigurationSection section = config.getConfigurationSection("settings");
        data.settings = section != null ? section.getValues(true) : new HashMap<>();
        
        return data;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Location getLobbyLocation() { return lobbyLocation; }
    public void setLobbyLocation(Location location) { this.lobbyLocation = location; }
    
    public Location getTSpawn() { return tSpawn; }
    public void setTSpawn(Location location) { this.tSpawn = location; }
    
    public Location getCTSpawn() { return ctSpawn; }
    public void setCTSpawn(Location location) { this.ctSpawn = location; }
    
    public Map<String, Object> getSettings() { return settings; }
    public void setSettings(Map<String, Object> settings) { this.settings = settings; }
} 