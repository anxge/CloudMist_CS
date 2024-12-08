package WLYD.cloudMist_CS.data;

import org.bukkit.configuration.file.YamlConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public class PlayerData {
    private int kills;
    private int deaths;
    private int wins;
    private int losses;
    private Map<String, Object> stats;
    private ConfigurationSection loadouts;
    private int bombPlants;
    private int bombDefuses;
    
    public PlayerData() {
        this.stats = new HashMap<>();
    }
    
    public void save(YamlConfiguration config) {
        config.set("kills", kills);
        config.set("deaths", deaths);
        config.set("wins", wins);
        config.set("losses", losses);
        config.set("stats.bomb_plants", bombPlants);
        config.set("stats.bomb_defuses", bombDefuses);
        if (loadouts != null) {
            config.set("loadouts", loadouts);
        }
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            config.set("stats." + entry.getKey(), entry.getValue());
        }
    }
    
    public static PlayerData load(YamlConfiguration config) {
        PlayerData data = new PlayerData();
        data.kills = config.getInt("kills");
        data.deaths = config.getInt("deaths");
        data.wins = config.getInt("wins");
        data.losses = config.getInt("losses");
        data.stats = config.getConfigurationSection("stats").getValues(true);
        return data;
    }
    
    public static PlayerData fromConfig(YamlConfiguration config) {
        PlayerData data = new PlayerData();
        data.kills = config.getInt("stats.kills", 0);
        data.deaths = config.getInt("stats.deaths", 0);
        data.wins = config.getInt("stats.wins", 0);
        data.losses = config.getInt("stats.losses", 0);
        data.bombPlants = config.getInt("stats.bomb_plants", 0);
        data.bombDefuses = config.getInt("stats.bomb_defuses", 0);
        data.loadouts = config.getConfigurationSection("loadouts");
        return data;
    }
    
    // Getters and Setters
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    
    public Map<String, Object> getStats() { return stats; }
    public void setStats(Map<String, Object> stats) { this.stats = stats; }
    
    public int getBombPlants() { return bombPlants; }
    public int getBombDefuses() { return bombDefuses; }
    public ConfigurationSection getLoadouts() { return loadouts; }
}