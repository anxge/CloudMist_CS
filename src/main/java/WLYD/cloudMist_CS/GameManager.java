package WLYD.cloudMist_CS;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import WLYD.cloudMist_CS.game.CSGame;
import WLYD.cloudMist_CS.game.Team;
import WLYD.cloudMist_CS.data.DataManager;
import WLYD.cloudMist_CS.data.MapData;

public class GameManager {
    private final Map<String, CSGame> games = new HashMap<String, CSGame>();
    private final Map<Player, CSGame> playerGames = new HashMap<Player, CSGame>();
    private final CloudMist_CS plugin;
    private final DataManager dataManager;

    public GameManager(CloudMist_CS plugin) {
        this.plugin = plugin;
        this.dataManager = new DataManager(plugin);
        loadGames();
    }

    private void loadGames() {
        File gamesDir = new File(plugin.getDataFolder(), "games");
        if (!gamesDir.exists()) {
            gamesDir.mkdirs();
            return;
        }

        File[] gameFiles = gamesDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (gameFiles != null) {
            for (File file : gameFiles) {
                try {
                    String gameName = file.getName().replace(".yml", "");
                    MapData mapData = dataManager.loadMapData(gameName);
                    if (mapData != null) {
                        CSGame game = new CSGame(
                            mapData.getName(),
                            mapData.getLobbyLocation(),
                            plugin.getWeaponManager()
                        );
                        game.setSpawnPoint(Team.TERRORIST, mapData.getTSpawn());
                        game.setSpawnPoint(Team.COUNTER_TERRORIST, mapData.getCTSpawn());
                        mapData.getBombSites().forEach(game::addBombSite);
                        mapData.getBuyZones().forEach(game::addBuyZone);
                        games.put(mapData.getName(), game);
                        plugin.getLogger().info("已加载游戏: " + mapData.getName() + 
                            " (大厅: " + mapData.getLobbyLocation() + 
                            ", T出生点: " + mapData.getTSpawn() + 
                            ", CT出生点: " + mapData.getCTSpawn() + ")");
                    } else {
                        plugin.getLogger().warning("无法加载游戏数据: " + gameName);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("加载游戏失败: " + file.getName() + " - " + e.getMessage());
                    if (plugin.getConfig().getBoolean("debug", false)) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Map<String, CSGame> getGames() {
        return games;
    }

    public void saveGame(CSGame game) {
        try {
            MapData mapData = new MapData(game);
            dataManager.saveMapData(game.getName(), mapData);
            plugin.getLogger().info("已保存游戏: " + game.getName() + 
                " (大厅: " + game.getLobbyLocation() + 
                ", T出生点: " + game.getTSpawn() + 
                ", CT出生点: " + game.getCTSpawn() + ")");
        } catch (Exception e) {
            plugin.getLogger().warning("保存游戏失败: " + game.getName() + " - " + e.getMessage());
            if (plugin.getConfig().getBoolean("debug", false)) {
                e.printStackTrace();
            }
        }
    }

    public boolean createGame(String name, Location lobby) {
        if (games.containsKey(name)) {
            plugin.getLogManager().logGame("创建游戏失败: 游戏 " + name + " 已存在");
            return false;
        }
        CSGame game = new CSGame(name, lobby, plugin.getWeaponManager());
        games.put(name, game);
        saveGame(game);
        plugin.getLogManager().logGame("成功创建游戏: " + name + " (大厅位置: " + lobby + ")");
        return true;
    }

    public CSGame getPlayerGame(Player player) {
        return playerGames.get(player);
    }

    public void setPlayerGame(Player player, CSGame game) {
        if (game == null) {
            playerGames.remove(player);
        } else {
            playerGames.put(player, game);
        }
    }

    public boolean joinGame(Player player, String gameName) {
        CSGame game = games.get(gameName);
        if (game == null || playerGames.containsKey(player)) {
            plugin.getLogManager().logGame("玩家 " + player.getName() + " 加入游戏 " + gameName + " 失败");
            return false;
        }
        
        game.addPlayer(player);
        playerGames.put(player, game);
        plugin.getLogManager().logGame("玩家 " + player.getName() + " 成功加入游戏 " + gameName);
        return true;
    }
} 