package WLYD.cloudMist_CS.game;

import WLYD.cloudMist_CS.weapon.WeaponManager;
import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.data.MapData;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    private final ConcurrentHashMap<String, CSGame> games = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, CSGame> playerGame = new ConcurrentHashMap<>();
    private final WeaponManager weaponManager;
    private final CloudMist_CS plugin;
    
    public GameManager(WeaponManager weaponManager) {
        this.plugin = CloudMist_CS.getInstance();
        this.weaponManager = weaponManager;
    }
    
    public boolean createGame(String name, Location lobby) {
        if (games.containsKey(name)) {
            return false;
        }
        games.put(name, new CSGame(name, lobby, weaponManager));
        return true;
    }
    
    public boolean joinGame(Player player, String gameName) {
        CSGame game = games.get(gameName);
        if (game == null || playerGame.containsKey(player)) {
            return false;
        }
        
        game.addPlayer(player);
        playerGame.put(player, game);
        return true;
    }
    
    public boolean leaveGame(Player player) {
        CSGame game = playerGame.get(player);
        if (game == null) {
            return false;
        }
        
        game.removePlayer(player);
        playerGame.remove(player);
        return true;
    }
    
    public CSGame getPlayerGame(Player player) {
        return playerGame.get(player);
    }
    
    public CSGame getGameByLocation(Location location) {
        for (CSGame game : games.values()) {
            Location lobby = game.getLobbyLocation();
            if (lobby.getWorld().equals(location.getWorld()) && 
                lobby.distance(location) <= 100) {
                return game;
            }
        }
        return null;
    }
    
    public void saveAllGames() {
        for (CSGame game : games.values()) {
            plugin.getDataManager().saveMapData(game.getName(), new MapData(game));
        }
    }
    
    public Map<String, CSGame> getGames() {
        return games;
    }
    
    public void deleteGame(String name) {
        CSGame game = games.remove(name);
        if (game != null) {
            game.stop();
        }
    }
    
    public void listGames(CommandSender sender) {
        if (games.isEmpty()) {
            sender.sendMessage("§e当前没有任何游戏");
            return;
        }
        
        sender.sendMessage("§6=== 游戏列表 ===");
        games.forEach((name, game) -> {
            sender.sendMessage(String.format("§e- %s §7(玩家数: %d, 状态: %s)", 
                name, game.getPlayerCount(), game.getState()));
        });
    }
    
    public void reloadGames() {
        // 保存当前游戏
        saveAllGames();
        
        // 清除现有游戏
        games.clear();
        playerGame.clear();
        
        // 重新加载游戏
        loadGames();
    }
    
    private void loadGames() {
        File gamesDir = new File(plugin.getDataFolder(), "games");
        if (!gamesDir.exists()) {
            gamesDir.mkdirs();
            return;
        }

        Optional.ofNullable(gamesDir.listFiles((dir, name) -> name.endsWith(".yml")))
            .ifPresent(files -> Arrays.stream(files)
                .parallel()
                .forEach(this::loadGame));
    }

    private void loadGame(File file) {
        try {
            String gameName = file.getName().replace(".yml", "");
            MapData mapData = plugin.getDataManager().loadMapData(gameName);
            if (mapData != null) {
                games.put(mapData.getName(), 
                    new CSGame(mapData.getName(), mapData.getLobbyLocation(), weaponManager));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("加载游戏失败: " + file.getName());
        }
    }
} 