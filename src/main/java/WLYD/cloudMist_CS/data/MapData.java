package WLYD.cloudMist_CS.data;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import WLYD.cloudMist_CS.game.CSGame;
import java.util.List;
import java.util.ArrayList;

public class MapData {
    private final String name;
    private final Location lobbyLocation;
    private final Location tSpawn;
    private final Location ctSpawn;
    private final List<Location> bombSites;
    private final List<Location> buyZones;
    
    public MapData(CSGame game) {
        this.name = game.getName();
        this.lobbyLocation = game.getLobbyLocation();
        this.tSpawn = game.getTSpawn();
        this.ctSpawn = game.getCTSpawn();
        this.bombSites = game.getBombSites();
        this.buyZones = game.getBuyZones();
    }
    
    public MapData(String name, Location lobbyLocation, Location tSpawn, Location ctSpawn, List<Location> bombSites, List<Location> buyZones) {
        this.name = name;
        this.lobbyLocation = lobbyLocation;
        this.tSpawn = tSpawn;
        this.ctSpawn = ctSpawn;
        this.bombSites = bombSites;
        this.buyZones = buyZones;
    }
    
    public static MapData fromConfig(YamlConfiguration config) {
        @SuppressWarnings("unchecked")
        List<Location> bombSites = config.getList("bomb_sites") != null ? 
            (List<Location>) config.getList("bomb_sites") : new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        List<Location> buyZones = config.getList("buy_zones") != null ? 
            (List<Location>) config.getList("buy_zones") : new ArrayList<>();
        
        return new MapData(
            config.getString("name"),
            (Location) config.get("lobby_location"),
            (Location) config.get("t_spawn"),
            (Location) config.get("ct_spawn"),
            bombSites,
            buyZones
        );
    }
    
    // Getters
    public String getName() { return name; }
    public Location getLobbyLocation() { return lobbyLocation; }
    public Location getTSpawn() { return tSpawn; }
    public Location getCTSpawn() { return ctSpawn; }
    public List<Location> getBombSites() { return bombSites; }
    public List<Location> getBuyZones() { return buyZones; }
} 