package WLYD.cloudMist_CS.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.game.CSGame;
import WLYD.cloudMist_CS.game.Team;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.Collection;

public class ScoreboardManager {
    private final CloudMist_CS plugin;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private final Map<UUID, String> scoreboardCache;
    private final Set<ScoreboardUpdateListener> updateListeners;
    private static final int MAX_SCOREBOARD_LINES = 16;
    private static final int MAX_NAME_LENGTH = 16;
    private static final long UPDATE_THROTTLE_MS = 50; // 限制更新频率
    private final Map<UUID, Long> lastUpdateTime;

    public ScoreboardManager(CloudMist_CS plugin) {
        this.plugin = plugin;
        this.playerScoreboards = new ConcurrentHashMap<>();
        this.scoreboardCache = new ConcurrentHashMap<>();
        this.updateListeners = new HashSet<>();
        this.lastUpdateTime = new ConcurrentHashMap<>();
    }

    public void registerUpdateListener(ScoreboardUpdateListener listener) {
        if (listener != null) {
            updateListeners.add(listener);
        }
    }

    public void unregisterUpdateListener(ScoreboardUpdateListener listener) {
        updateListeners.remove(listener);
    }

    public void updateScoreboard(Player player, CSGame game) {
        if (player == null || !player.isOnline()) {
            return;
        }

        // 使用事件系统通知更新
        ScoreboardUpdateEvent event = new ScoreboardUpdateEvent(player, game);
        plugin.getServer().getPluginManager().callEvent(event);
        
        if (event.isCancelled()) {
            return;
        }
        
        // 检查更新频率限制
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastUpdate = lastUpdateTime.get(playerId);
        if (lastUpdate != null && currentTime - lastUpdate < UPDATE_THROTTLE_MS) {
            return;
        }

        if (game == null) {
            removeScoreboard(player);
            return;
        }

        try {
            // 通知���听器
            for (ScoreboardUpdateListener listener : updateListeners) {
                try {
                    listener.onScoreboardUpdate(player, game);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "计分板更新监听器执行失败", e);
                }
            }

            String newContent = generateScoreboardContent(player, game);
            String oldContent = scoreboardCache.get(playerId);
            
            // 如果内容没有变化，跳过更新
            if (newContent.equals(oldContent)) {
                return;
            }

            Scoreboard board = playerScoreboards.computeIfAbsent(playerId, 
                k -> Bukkit.getScoreboardManager().getNewScoreboard());

            // 确保玩家在游戏中
            if (!game.getPlayers().contains(player)) {
                removeScoreboard(player);
                return;
            }

            updateScoreboardContent(player, board, game, newContent);
            scoreboardCache.put(playerId, newContent);
            lastUpdateTime.put(playerId, currentTime);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "更新玩家 " + player.getName() + " 的计分板时发生错误", e);
            resetScoreboard(player);
        }
    }

    private String generateScoreboardContent(Player player, CSGame game) {
        StringBuilder content = new StringBuilder();
        Team playerTeam = game.getPlayerTeam(player);
        
        content.append("§7================\n");
        content.append(String.format("§f队伍: %s\n", getTeamDisplay(playerTeam)));
        content.append(String.format("§f金钱: §e$%d\n", game.getEconomy().getMoney(player)));
        content.append("\n");
        
        content.append(String.format("§f回合: §e%d/%d\n", 
            game.getRoundNumber(), 
            game.getSettings().getMaxRounds()));
        content.append(String.format("§f比分: §c%d §f- §9%d\n",
            game.getTeamScore(Team.TERRORIST),
            game.getTeamScore(Team.COUNTER_TERRORIST)));
        content.append("\n");
        
        content.append("§f队伍数据:\n");
        for (Player teammate : game.getTeamPlayers(playerTeam)) {
            String playerName = formatPlayerName(teammate.getName());
            String nameColor = teammate.equals(player) ? "§e" : "§7";
            content.append(String.format("%s%s: §a%d§f/§c%d\n", 
                nameColor,
                playerName,
                game.getKills(teammate),
                game.getDeaths(teammate)));
        }
        content.append("\n");

        content.append(String.format("§f状态: %s\n", getGameStateDisplay(game)));
        content.append("§7================");
        
        return content.toString();
    }

    private void updateScoreboardContent(Player player, Scoreboard board, CSGame game, String content) {
        // 清除旧的计分板
        Objective oldObjective = board.getObjective("game");
        if (oldObjective != null) {
            oldObjective.unregister();
        }
        
        Objective objective = board.registerNewObjective("game", "dummy", "§6CloudMist CS");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        String[] lines = content.split("\n");
        int score = Math.min(lines.length, MAX_SCOREBOARD_LINES);
        
        for (String line : lines) {
            if (score <= 0) break;
            try {
                objective.getScore(line).setScore(score--);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("设置计分板行失败: " + e.getMessage());
            }
        }
        
        player.setScoreboard(board);
    }

    private String formatPlayerName(String name) {
        if (name.length() > MAX_NAME_LENGTH) {
            return name.substring(0, MAX_NAME_LENGTH - 2) + "..";
        }
        return name;
    }

    private String getGameStateDisplay(CSGame game) {
        switch (game.getState()) {
            case WARMUP:
                return "§e热身中";
            case BUY_TIME:
                return "§a购买时间";
            case IN_PROGRESS:
                return "§6进行中";
            default:
                return "§7等待中";
        }
    }

    public void removeScoreboard(Player player) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        playerScoreboards.remove(playerId);
        scoreboardCache.remove(playerId);
        lastUpdateTime.remove(playerId);
        
        try {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        } catch (Exception e) {
            plugin.getLogger().warning("移除玩家 " + player.getName() + " 的计分板时出错: " + e.getMessage());
        }
    }

    private void resetScoreboard(Player player) {
        try {
            removeScoreboard(player);
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        } catch (Exception e) {
            plugin.getLogger().severe("重置玩家 " + player.getName() + " 的计分板时出错: " + e.getMessage());
        }
    }

    public void updateTeamScoreboards(Team team) {
        for (CSGame game : plugin.getGameManager().getGames().values()) {
            for (Player player : game.getTeamPlayers(team)) {
                updateScoreboard(player, game);
            }
        }
    }

    public void updateAllScoreboards() {
        for (CSGame game : plugin.getGameManager().getGames().values()) {
            for (Player player : game.getPlayers()) {
                updateScoreboard(player, game);
            }
        }
    }

    public void cleanup() {
        playerScoreboards.clear();
        scoreboardCache.clear();
        lastUpdateTime.clear();
        updateListeners.clear();
    }

    public void batchUpdate(Collection<Player> players) {
        if (players == null || players.isEmpty()) return;
        
        for (Player player : players) {
            try {
                updateScoreboard(player, plugin.getGameManager().getPlayerGame(player));
            } catch (Exception e) {
                plugin.getLogger().warning("批量更新时，更新玩家 " + player.getName() + " 的计分板失败: " + e.getMessage());
            }
        }
    }

    public void onPlayerQuit(Player player) {
        removeScoreboard(player);
    }

    private String getTeamDisplay(Team team) {
        if (team == Team.TERRORIST) {
            return "§c恐怖分子";
        } else if (team == Team.COUNTER_TERRORIST) {
            return "§9反恐精英";
        } else {
            return "§7观察者";
        }
    }
}