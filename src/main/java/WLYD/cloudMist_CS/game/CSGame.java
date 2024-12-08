package WLYD.cloudMist_CS.game;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.weapon.WeaponManager;
import WLYD.cloudMist_CS.economy.Economy;
import WLYD.cloudMist_CS.weapon.WeaponType;
import java.util.ArrayList;
import WLYD.cloudMist_CS.stats.PlayerStats;
import WLYD.cloudMist_CS.event.GameEventListener;
import WLYD.cloudMist_CS.event.RoundEndEvent;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import WLYD.cloudMist_CS.utils.PerformanceMonitor;
import java.util.stream.Collectors;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Firework;
import org.bukkit.FireworkEffect;
import org.bukkit.Color;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;

public class CSGame {
    private final String name;
    private Location lobbyLocation;
    private Location tSpawn;    // T队出生点
    private Location ctSpawn;   // CT队出生点
    private final Set<Player> players;
    private GameState state;
    private final Map<Player, Team> playerTeams;
    private final Map<Team, Set<Player>> teamPlayers;
    private int maxPlayersPerTeam;
    private int minPlayersPerTeam;
    private static final int COUNTDOWN_SECONDS = 10;
    private int taskId = -1;
    private final WeaponManager weaponManager;
    private final Economy economy;
    private int roundNumber = 0;
    private final Map<Team, Integer> teamScores = new HashMap<>();
    private final Map<Player, Integer> playerKills = new HashMap<>();
    private final Map<Player, Integer> playerDeaths = new HashMap<>();
    private int roundTimeLeft;
    private int roundTimerTaskId = -1;
    private final GameSettings settings;
    private List<GameEventListener> eventListeners;  // 事件监听器
    private GameTimer gameTimer;  // 游戏计时器封装
    private final Queue<Runnable> updateQueue = new ConcurrentLinkedQueue<>();
    private final CloudMist_CS plugin;
    private boolean bombPlanted = false;
    private Location bombLocation = null;
    private Player bombPlanter = null;
    private BukkitTask bombTaskId;
    private final LoadingCache<UUID, PlayerStats> statsCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build(new CacheLoader<UUID, PlayerStats>() {
            @Override
            public PlayerStats load(@Nonnull UUID key) {
                return new PlayerStats();
            }
        });
    private final BombSiteManager bombSiteManager;
    private final List<Location> buyZones = new ArrayList<>();
    private TNTPrimed bombEntity;  // TNT实体
    private ArmorStand hologram;   // 全息倒计时显示
    private Location buyZoneLocation;
    private BukkitTask bombPlantingTask = null;
    private Player plantingPlayer = null;
    private Location plantingLocation = null;
    private int plantProgress = 0;
    
    public CSGame(String name, Location lobby, WeaponManager weaponManager) {
        this.plugin = CloudMist_CS.getInstance();
        this.name = name;
        this.lobbyLocation = lobby;
        this.weaponManager = weaponManager;
        this.players = new HashSet<>();
        this.playerTeams = new HashMap<>();
        this.teamPlayers = new HashMap<>();
        this.settings = new GameSettings();
        this.eventListeners = new ArrayList<>();
        this.gameTimer = new GameTimer(this);
        this.bombSiteManager = new BombSiteManager(plugin);
        
        // 使用插件实例获取管理器
        this.economy = new Economy(plugin);
        
        this.state = GameState.WAITING;
        
        // 初化队伍玩家集合
        teamPlayers.put(Team.TERRORIST, new HashSet<>());
        teamPlayers.put(Team.COUNTER_TERRORIST, new HashSet<>());
        teamPlayers.put(Team.SPECTATOR, new HashSet<>());
        
        // 初始化队伍分数
        teamScores.put(Team.TERRORIST, 0);
        teamScores.put(Team.COUNTER_TERRORIST, 0);
        teamScores.put(Team.SPECTATOR, 0);
        
        // 从配置文件读取设置
        this.maxPlayersPerTeam = plugin.getConfigManager().getGameConfig().getInt("teams.max_players", 5);
        this.minPlayersPerTeam = plugin.getConfigManager().getGameConfig().getInt("teams.min_players", 1);
        
        startUpdateTask();
    }
    
    public boolean joinTeam(Player player, Team team) {
        if (team != Team.SPECTATOR && teamPlayers.get(team).size() >= maxPlayersPerTeam) {
            player.sendMessage("§c该队伍已满!");
            return false;
        }
        
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info(String.format("玩家 %s 尝试加入 %s 队伍", player.getName(), team.name()));
        }
        
        // 如果玩家已经在某个队伍中先离开原队伍
        Team oldTeam = playerTeams.get(player);
        if (oldTeam != null) {
            teamPlayers.get(oldTeam).remove(player);
        }
        
        // 加入新队伍
        playerTeams.put(player, team);
        teamPlayers.get(team).add(player);
        
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info(String.format("玩家 %s 当前队伍: %s", 
                player.getName(), getPlayerTeam(player)));
            plugin.getLogger().info(String.format("队伍人数 - T: %d, CT: %d", 
                teamPlayers.get(Team.TERRORIST).size(),
                teamPlayers.get(Team.COUNTER_TERRORIST).size()));
        }
        
        // 发送息
        String message = String.format("§6%s §f加入了 %s §f队伍", 
            player.getName(), team.getDisplayName());
        broadcastMessage(message);
        
        updateAllScoreboards();
        return true;
    }
    
    public void addPlayer(Player player) {
        players.add(player);
        playerKills.put(player, 0);
        playerDeaths.put(player, 0);
        economy.initializePlayer(player);
        player.teleport(lobbyLocation);
        
        // 自动分配队伍
        autoAssignTeam(player);
        
        // 初始化玩家经济
        economy.initializePlayer(player);
        player.sendMessage("§a你已加入游戏 " + name);
        plugin.getScoreboardManager().updateScoreboard(player, this);
        
        // 记录日志
        plugin.getLogManager().logGame(String.format("玩家 %s 加入了游戏 %s", player.getName(), name));
        
        // 更新计分板
        plugin.getScoreboardManager().updateScoreboard(player, this);
    }
    
    private void autoAssignTeam(Player player) {
        int tCount = teamPlayers.get(Team.TERRORIST).size();
        int ctCount = teamPlayers.get(Team.COUNTER_TERRORIST).size();
        
        Team assignedTeam;
        
        // 如果一方人数明显较少，优先加入该队伍
        if (tCount < ctCount - 1) {
            assignedTeam = Team.TERRORIST;
        } else if (ctCount < tCount - 1) {
            assignedTeam = Team.COUNTER_TERRORIST;
        } else {
            // 如果人数差不多，随机分配
            assignedTeam = Math.random() < 0.5 ? Team.TERRORIST : Team.COUNTER_TERRORIST;
        }
        
        // 如果队伍已满，分配到另一个队伍
        if (teamPlayers.get(assignedTeam).size() >= maxPlayersPerTeam) {
            assignedTeam = (assignedTeam == Team.TERRORIST) ? Team.COUNTER_TERRORIST : Team.TERRORIST;
        }
        
        // 如果两个队伍都满了，分配到观察者
        if (teamPlayers.get(assignedTeam).size() >= maxPlayersPerTeam) {
            assignedTeam = Team.SPECTATOR;
            player.sendMessage("§c两个队伍都已满员，你将作为观察者加入");
        }
        
        // 加入分配的队伍
        joinTeam(player, assignedTeam);
    }
    
    public void removePlayer(Player player) {
        players.remove(player);
        Team team = playerTeams.remove(player);
        if (team != null) {
            teamPlayers.get(team).remove(player);
        }
        // 清除经济数据
        economy.removePlayer(player);
        // 清除计分板
        plugin.getScoreboardManager().removeScoreboard(player);
        
        player.sendMessage("§c你已离开游戏 " + name);
        updateAllScoreboards();
        
        // 记录日志
        plugin.getLogManager().logGame(String.format("玩家 %s 离开了游戏 %s", player.getName(), name));
        
        // 检查游戏是否需要结束
        if (players.isEmpty()) {
            stop();
        }
    }
    
    public String getName() {
        return name;
    }
    
    public Set<Player> getPlayers() {
        return new HashSet<>(players);
    }
    
    public GameState getState() {
        return state;
    }
    
    public Team getPlayerTeam(Player player) {
        return playerTeams.get(player);
    }
    
    public Set<Player> getTeamPlayers(Team team) {
        return new HashSet<>(teamPlayers.get(team));
    }
    
    private void broadcastMessage(String message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }
    
    public boolean startGame() {
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("===== 游戏启动检查 =====");
            plugin.getLogger().info("游戏状: " + state);
            plugin.getLogger().info("T队玩家列表: " + teamPlayers.get(Team.TERRORIST));
            plugin.getLogger().info("CT队玩家列表: " + teamPlayers.get(Team.COUNTER_TERRORIST));
            plugin.getLogger().info("T队人数: " + teamPlayers.get(Team.TERRORIST).size());
            plugin.getLogger().info("CT队人数: " + teamPlayers.get(Team.COUNTER_TERRORIST).size());
            plugin.getLogger().info("最小家数求: " + minPlayersPerTeam);
        }
        
        // 查游戏状态
        if (state != GameState.WAITING) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("游状态检查失败: " + state);
            }
            broadcastMessage("§c游戏已经在进行在准备!");
            return false;
        }
        
        // 检查出生点是否已设置
        if (tSpawn == null || ctSpawn == null) {
            broadcastMessage("§c无法开始游戏，出生点未设置完全!");
            return false;
        }
        
        // 检查每个队伍的玩家数量
        int tCount = teamPlayers.get(Team.TERRORIST).size();
        int ctCount = teamPlayers.get(Team.COUNTER_TERRORIST).size();
        
        if (tCount < minPlayersPerTeam || ctCount < minPlayersPerTeam) {
            broadcastMessage("§c无法开始游戏，每队至少需要 1 名玩家!");
            broadcastMessage(String.format("§c当前人数 - T队: %d, CT队: %d", tCount, ctCount));
            return false;
        }
        
        // 记录试信息
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info(String.format("尝试始游戏 - T队: %d, CT队: %d人", tCount, ctCount));
        }
        
        // 置游状态为准备中
        state = GameState.STARTING;
        startCountdown();
        
        for (Player player : getPlayers()) {
            // 检查家是否已经拥有武器
            if (!playerHasWeapon(player, WeaponType.GLOCK17.getId())) {
                giveWeapon(player, WeaponType.GLOCK17);
            }
        }
        
        return true;
    }
    
    private void startCountdown() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(CloudMist_CS.getInstance(), new Runnable() {
            private int countdown = COUNTDOWN_SECONDS;
            
            @Override
            public void run() {
                if (countdown > 0) {
                    broadcastMessage(String.format("§6游戏将在 §e%d §6秒后始!", countdown));
                    countdown--;
                } else {
                    Bukkit.getScheduler().cancelTask(taskId);
                    beginRound();
                }
            }
        }, 0L, 20L); // 20 ticks = 1 second
    }
    
    private void beginRound() {
        setState(GameState.FREEZE_TIME);
        roundNumber++;
        
        // 重置回合状态
        bombPlanted = false;
        bombLocation = null;
        bombPlanter = null;
        
        // 为所有玩家重置装备
        for (Player player : players) {
            Team team = getPlayerTeam(player);
            if (team != Team.SPECTATOR) {
                weaponManager.giveDefaultLoadout(player, team);
            }
        }
        
        // 传送玩家到出生点
        for (Player player : teamPlayers.get(Team.TERRORIST)) {
            player.teleport(tSpawn);
        }
        for (Player player : teamPlayers.get(Team.COUNTER_TERRORIST)) {
            player.teleport(ctSpawn);
        }
        
        // 分发回合开始装备
        distributeRoundStartEquipment();
        
        // 开始冻结时间
        broadcastMessage("§6=== 准备时间 ===");
        gameTimer.start(settings.getFreezeTime());
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state == GameState.FREEZE_TIME) {
                setState(GameState.BUY_TIME);
                // 保持玩家冻结状态
                for (Player player : players) {
                    freezePlayer(player);
                }
                gameTimer.start(settings.getBuyTime());
                broadcastMessage("§a购买时间开始！");
                startBuyTimer();
            }
        }, settings.getFreezeTime() * 20L);
    }
    
    private void freezePlayer(Player player) {
        player.setWalkSpeed(0);
        player.setFlySpeed(0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128, false, false));
        player.setGameMode(GameMode.ADVENTURE); // 防止破坏
    }
    
    private void unfreezePlayer(Player player) {
        player.setWalkSpeed(0.2f); // 默认行走速度
        player.setFlySpeed(0.1f);  // 默认飞行速度
        player.removePotionEffect(PotionEffectType.JUMP);
        player.setGameMode(GameMode.SURVIVAL);
    }
    
    private void distributeRoundStartEquipment() {
        for (Player player : teamPlayers.get(Team.TERRORIST)) {
            weaponManager.giveDefaultWeapons(player, Team.TERRORIST);
        }
        for (Player player : teamPlayers.get(Team.COUNTER_TERRORIST)) {
            weaponManager.giveDefaultWeapons(player, Team.COUNTER_TERRORIST);
        }
    }
    
    private void startBuyTimer() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state == GameState.BUY_TIME) {
                setState(GameState.IN_PROGRESS);
                // 解除所有玩家的冻结状态
                for (Player player : players) {
                    unfreezePlayer(player);
                }
                broadcastMessage("§c购买时间结束！");
                gameTimer.start(settings.getRoundTime());
                startRoundTimer();
            }
        }, settings.getBuyTime() * 20L);
    }
    
    private void startRoundTimer() {
        gameTimer.start(settings.getRoundTime());
    }
    
    public void endRound(Team winningTeam) {
        // 取消计时器
        if (roundTimerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(roundTimerTaskId);
            roundTimerTaskId = -1;
        }
        
        // 触发回合结事件
        RoundEndEvent event = new RoundEndEvent(this, winningTeam);
        Bukkit.getPluginManager().callEvent(event);
        
        // 添加队伍得分
        addTeamScore(winningTeam);
        
        // 广播结果
        broadcastMessage("§6=== 回合结束 ===");
        broadcastMessage(String.format("§6胜利队伍: %s", winningTeam.getDisplayName()));
        
        // 检查游戏是否结束
        if (shouldEndGame()) {
            endGame(winningTeam);
            return;
        }
        
        // 延迟开始下一回合
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state != GameState.ENDING) {
                beginRound();
            }
        }, 5 * 20L);
    }
    
    private boolean shouldEndGame() {
        int tScore = teamScores.get(Team.TERRORIST);
        int ctScore = teamScores.get(Team.COUNTER_TERRORIST);
        return tScore >= settings.getWinScore() || ctScore >= settings.getWinScore();
    }
    
    private void endGame(Team winner) {
        state = GameState.ENDING;
        
        // 广播最终比分
        broadcastMessage("§6=== 游戏结束 ===");
        broadcastMessage(String.format("§6胜利队伍: %s", winner.getDisplayName()));
        broadcastMessage(String.format("§c恐怖分子: %d", teamScores.get(Team.TERRORIST)));
        broadcastMessage(String.format("§b反恐精英: %d", teamScores.get(Team.COUNTER_TERRORIST)));
        
        // 清理游戏状态
        stop();
        
        // 重置游戏状态
        state = GameState.WAITING;
        roundNumber = 1;
        teamScores.put(Team.TERRORIST, 0);
        teamScores.put(Team.COUNTER_TERRORIST, 0);
    }
    
    public void stopGame() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        
        state = GameState.WAITING;
        broadcastMessage("§c游戏结束!");
        
        // 重置所有玩家
        for (Player player : players) {
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.teleport(lobbyLocation);
        }
    }
    
    // 设置出生点的方法
    public void setSpawnPoint(Team team, Location location) {
        if (team == Team.TERRORIST) {
            this.tSpawn = location;
        } else if (team == Team.COUNTER_TERRORIST) {
            this.ctSpawn = location;
        }
    }
    
    public void updateScoreboard(Player player) {
        plugin.getScoreboardManager().updateScoreboard(player, this);
    }
    
    public void updateAllScoreboards() {
        for (Player player : players) {
            updateScoreboard(player);
        }
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public void addKill(Player killer, Player victim) {
        // 更新击杀数
        playerKills.merge(killer, 1, Integer::sum);
        // 更新死亡数
        playerDeaths.merge(victim, 1, Integer::sum);
        
        // 给予击杀奖励
        economy.addKillReward(killer);
        
        // 记录日志
        plugin.getLogManager().logGame(String.format("%s 击杀了 %s", killer.getName(), victim.getName()));
        
        // 广播击杀信息
        broadcastMessage(String.format("§e%s §7击杀了 §e%s", killer.getName(), victim.getName()));
        
        // 更新所有玩家的计分板
        updateAllScoreboards();
        
        // 检查回合胜利条件
        checkRoundWinCondition();
    }
    
    private void checkRoundWinCondition() {
        if (state != GameState.IN_PROGRESS) {
            return;
        }
        
        // 查C4状态
        if (bombPlanted) {
            // 如果C4已安装，只有拆除爆炸才能结束回合
            return;
        }
        
        int aliveT = 0;
        int aliveCT = 0;
        
        // 计算存活人数
        for (Player player : players) {
            if (player.isDead()) continue;
            
            Team team = getPlayerTeam(player);
            if (team == Team.TERRORIST) {
                aliveT++;
            } else if (team == Team.COUNTER_TERRORIST) {
                aliveCT++;
            }
        }
        
        // 判定胜利条件
        if (aliveT == 0 && !bombPlanted) {
            // 有T亡且安C4，CT胜利
            endRound(Team.COUNTER_TERRORIST);
            broadcastMessage("§6反恐精英 胜利！");
        } else if (aliveCT == 0) {
            // 所有CT死亡，T胜利
            endRound(Team.TERRORIST);
            broadcastMessage("§6恐怖分子 胜利！");
        }
    }
    
    public void addTeamScore(Team team) {
        teamScores.put(team, teamScores.get(team) + 1);
        
        // 予回合奖励
        for (Player player : teamPlayers.get(team)) {
            economy.addRoundReward(player, true);
        }
        Team losingTeam = (team == Team.TERRORIST) ? Team.COUNTER_TERRORIST : Team.TERRORIST;
        for (Player player : teamPlayers.get(losingTeam)) {
            economy.addRoundReward(player, false);
        }
        
        updateAllScoreboards();
    }
    
    public int getRoundNumber() {
        return roundNumber;
    }
    
    public int getTeamScore(Team team) {
        return teamScores.getOrDefault(team, 0);
    }
    
    public int getKills(Player player) {
        return playerKills.getOrDefault(player, 0);
    }
    
    public int getDeaths(Player player) {
        return playerDeaths.getOrDefault(player, 0);
    }
    
    public int getRoundTimeLeft() {
        return roundTimeLeft;
    }
    
    public GameSettings getSettings() {
        return settings;
    }
    
    public void saveSettings() {
        settings.saveToConfig(name, CloudMist_CS.getInstance().getConfig());
        CloudMist_CS.getInstance().saveConfig();
    }
    
    public void loadSettings() {
        settings.loadFromConfig(name, CloudMist_CS.getInstance().getConfigManager().getGameConfig());
    }
    
    public void pauseGame() {}  // 游戏暂停
    public void resumeGame() {} // 游戏继续
    public void restartRound() {} // 重启当前回合
    
    public void queueUpdate(Runnable update) {
        if (update != null) {
            synchronized(updateQueue) {
                updateQueue.offer(update);
            }
        }
    }
    
    private void processUpdates() {
        boolean hasUpdates = false;
        List<Runnable> updates = new ArrayList<>();
        
        synchronized(updateQueue) {
            while (!updateQueue.isEmpty() && updates.size() < 100) {
                updates.add(updateQueue.poll());
                hasUpdates = true;
            }
        }
        
        if (hasUpdates) {
            PerformanceMonitor.startTiming("game_updates");
        }
        
        for (Runnable task : updates) {
            try {
                task.run();
            } catch (Exception e) {
                plugin.getLogger().severe("游戏更新处错误: " + e.getMessage());
                if (plugin.getConfig().getBoolean("debug")) {
                    e.printStackTrace();
                }
            }
        }
        
        if (hasUpdates) {
            PerformanceMonitor.endTiming("game_updates");
        }
    }
    
    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(CloudMist_CS.getInstance(), () -> {
            processUpdates();
        }, 1L, 1L);
    }
    
    // 修改GameTimer类中的broadcastTime方法
    public void broadcastTime(int seconds) {
        if ((seconds % 30 == 0 || seconds <= 10) && seconds > 0) {
            String stateMsg = "";
            switch (state) {
                case FREEZE_TIME:
                    stateMsg = "准备时间";
                    break;
                case BUY_TIME:
                    stateMsg = "购买时间";
                    break;
                case IN_PROGRESS:
                    stateMsg = "回合时间";
                    break;
                case STARTING:
                    stateMsg = "开始倒计时";
                    break;
                case WAITING:
                case ENDING:
                case WARMUP:
                default:
                    stateMsg = "等时间";
                    break;
            }
            broadcastMessage(String.format("§e%s剩余: %d 秒", stateMsg, seconds));
        }
    }
    
    // 添加玩家统计相关的方法
    public PlayerStats getPlayerStats(Player player) {
        return statsCache.getUnchecked(player.getUniqueId());
    }
    
    public void addEventListener(GameEventListener listener) {
        eventListeners.add(listener);
    }
    
    public void removeEventListener(GameEventListener listener) {
        eventListeners.remove(listener);
    }
    
    public void handleTimeUp() {
        if (state == GameState.IN_PROGRESS) {
            // 时间到，CT胜
            Team winningTeam = Team.COUNTER_TERRORIST;
            endRound(winningTeam);
        } else if (state == GameState.WARMUP) {
            startGame();
        }
    }
    
    public Location getLobbyLocation() {
        return lobbyLocation;
    }
    
    public Location getTSpawn() {
        return tSpawn;
    }
    
    public Location getCTSpawn() {
        return ctSpawn;
    }
    
    public List<Location> getBombSites() {
        return bombSiteManager.getBombSites().stream()
            .map(BombSite::getLocation)
            .collect(Collectors.toList());
    }
    
    public List<Location> getBuyZones() {
        return new ArrayList<>(buyZones);
    }
    
    public void addBombSite(Location location) {
        bombSiteManager.addBombSite(location);
    }
    
    public void addBuyZone(Location location) {
        buyZones.add(location);
    }
    
    public void onBombPlanted(Player planter, Location location) {
        plugin.getLogManager().logGame(String.format(
            "炸弹安装 - 游戏: %s, 玩家: %s, 位置: %s",
            name, planter.getName(), location.toString()
        ));
        // 检查是否在炸弹安装点范围内
        if (!bombSiteManager.isInBombSite(location)) {
            planter.sendMessage("§c你必须在炸弹安装点范围内安装C4！");
            return;
        }
        
        if (!bombPlanted) {
            bombPlanted = true;
            bombLocation = location;
            bombPlanter = planter;
            
            // 在炸弹位置上方2格创建TNT实体
            Location bombLoc = location.clone().add(0, 2, 0);
            bombEntity = location.getWorld().spawn(bombLoc, TNTPrimed.class);
            bombEntity.setFuseTicks(settings.getBombTimer() * 20); // 将秒转换为tick
            
            // 创建全息图显示倒计时
            createHologram(location.clone().add(0, 2.5, 0));
            
            // 取消回合计时器，改为炸弹计时
            cancelRoundTimer();
            
            // 开炸弹计时
            startBombTimer();
            
            // 给予安装炸弹奖励
            economy.addBonusReward(planter, Economy.RewardType.BOMB_PLANT);
            
            // 广播消息
            broadcastMessage("§c" + planter.getName() + " 安装了C4炸弹!");
        }
    }
    
    private void createHologram(Location location) {
        hologram = location.getWorld().spawn(location, ArmorStand.class);
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setCustomNameVisible(true);
        hologram.setMarker(true);
        hologram.setSmall(true); // 使全息图更小
        
        // 开始更新倒计时显示
        int bombTimer = settings.getBombTimer();
        new BukkitRunnable() {
            int timeLeft = bombTimer;
            
            @Override
            public void run() {
                if (!bombPlanted || hologram == null || hologram.isDead()) {
                    this.cancel();
                    return;
                }
                
                String timeStr = String.format("§cC4倒计时: %d:%02d", timeLeft / 60, timeLeft % 60);
                hologram.setCustomName(timeStr);
                
                timeLeft--;
                
                // 当到时，确保TNT爆炸
                if (timeLeft < 0) {
                    this.cancel();
                    if (bombEntity != null && !bombEntity.isDead()) {
                        bombEntity.setFuseTicks(0); // 立即引爆TNT
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 每秒更新一次
    }
    
    public Location getBombLocation() {
        return bombLocation;
    }
    
    public Player getBombPlanter() {
        return bombPlanter;
    }
    
    public void onBombDefused(Player defuser) {
        if (!bombSiteManager.isInDefuseRange(defuser.getLocation())) {
            defuser.sendMessage("§c你必须在炸弹附近才能拆除！");
            return;
        }
        
        if (!bombPlanted) return;
        
        bombPlanted = false;
        if (bombTaskId != null) {
            bombTaskId.cancel();
            bombTaskId = null;
        }
        
        // 移除TNT实体
        if (bombEntity != null && !bombEntity.isDead()) {
            bombEntity.remove();
            bombEntity = null;
        }
        
        // 移除全息图
        if (hologram != null && !hologram.isDead()) {
            hologram.remove();
            hologram = null;
        }
        
        // 清除炸弹位置
        bombLocation = null;
        bombPlanter = null;
        
        // 给予拆弹奖励
        int defuseReward = plugin.getConfigManager().getGameConfig().getInt("rewards.bomb_defuse", 300);
        economy.addMoney(defuser, defuseReward);
        
        endRound(Team.COUNTER_TERRORIST);
        broadcastMessage("§a" + defuser.getName() + " 成功拆除了C4");
    }
    
    private void setState(GameState newState) {
        this.state = newState;
        updateAllScoreboards();
    }
    
    public void registerBombSite(String name, Location location, double radius) {
        bombSiteManager.registerBombSite(name, location, radius);
    }
    
    public boolean canAfford(Player player, int amount) {
        return economy.getMoney(player) >= amount;
    }
    
    public void onPlayerQuit(Player player) {
        players.remove(player);
        Team team = getPlayerTeam(player);
        if (team != null) {
            teamPlayers.get(team).remove(player);
        }
        
        // 检查游戏是否应该结束
        checkGameEnd();
    }
    
    public void checkGameEnd() {
        // 检查是否有胜利条件
        if (teamScores.get(Team.TERRORIST) >= settings.getWinScore()) {
            endRound(Team.TERRORIST);
            broadcastMessage("§6恐怖分子胜利！");
        } else if (teamScores.get(Team.COUNTER_TERRORIST) >= settings.getWinScore()) {
            endRound(Team.COUNTER_TERRORIST);
            broadcastMessage("§6反恐精英胜利！");
        } else if (players.size() < settings.getMinPlayers()) {
            // 如果玩家数量少于最小要求，结束游戏
            broadcastMessage("§c游戏结束，玩家数量不足！");
            endRound(null); // null 表示没有胜利队伍
        }
    }
    
    // 添加爆炸特效方法
    private void createExplosionEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        // 生成烟花
        Firework firework = world.spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        
        // 设置烟花效果
        FireworkEffect effect = FireworkEffect.builder()
            .with(FireworkEffect.Type.BALL_LARGE)
            .withColor(Color.RED, Color.ORANGE)
            .withFade(Color.YELLOW)
            .trail(true)
            .flicker(true)
            .build();
            
        meta.addEffect(effect);
        meta.setPower(2);
        firework.setFireworkMeta(meta);
        
        // 添加粒子效果
        world.spawnParticle(Particle.EXPLOSION_HUGE, location, 1);
        world.spawnParticle(Particle.FLAME, location, 50, 1, 1, 1, 0.1);
        world.spawnParticle(Particle.SMOKE_LARGE, location, 100, 2, 2, 2, 0.1);
        
        // 播放爆炸音效
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    }
    
    public boolean isBombPlanted() {
        return bombPlanted; // 返回炸弹是否已安装
    }
    
    public int getPlayerCount() {
        return players.size(); // 返回当前玩家数量
    }
    
    // 添加炸弹计时器的方法
    private void startBombTimer() {
        bombTaskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (bombPlanted) {
                // 处理炸弹爆炸
                createExplosionEffect(bombLocation);
                
                // 清理实体
                if (bombEntity != null && !bombEntity.isDead()) {
                    bombEntity.remove();
                    bombEntity = null;
                }
                if (hologram != null && !hologram.isDead()) {
                    hologram.remove();
                    hologram = null;
                }
                
                bombPlanted = false;
                bombLocation = null;
                bombPlanter = null;
                
                endRound(Team.TERRORIST);
            }
        }, settings.getBombTimer() * 20L); // 转换为tick
    }
    
    // 在 CSGame 类中添加方法来取消回合计时器
    private void cancelRoundTimer() {
        if (roundTimerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(roundTimerTaskId);
            roundTimerTaskId = -1;
        }
        gameTimer.cancel(); // 取消游戏计时器
    }
    
    // 修改检查回合结束的方法
    public void checkRoundEnd() {
        Set<Player> aliveTerrorists = getAlivePlayers(Team.TERRORIST);
        Set<Player> aliveCounterTerrorists = getAlivePlayers(Team.COUNTER_TERRORIST);
        
        // 检查总回合胜利条件
        int maxRounds = settings.getMaxRounds();
        int tScore = teamScores.get(Team.TERRORIST);
        int ctScore = teamScores.get(Team.COUNTER_TERRORIST);
        
        // 检查游戏总胜利条件
        if (tScore > maxRounds / 2) {
            endGame(Team.TERRORIST);
            broadcastMessage("§6恐怖分子 获得了最终胜利！");
            return;
        } else if (ctScore > maxRounds / 2) {
            endGame(Team.COUNTER_TERRORIST);
            broadcastMessage("§6反恐精英 获得了最终胜利！");
            return;
        }
        
        // 如果炸弹已安装
        if (bombPlanted) {
            // 如果CT全灭，T直接获胜（因为无法拆弹）
            if (aliveCounterTerrorists.isEmpty()) {
                endRound(Team.TERRORIST);
                broadcastMessage("§6本回合：反恐精英全灭，无法拆除炸弹，恐怖分子胜利！");
            }
            // 如果炸弹已安装，即使T全灭也要继续，直到炸弹爆炸或被拆除
            return;
        }
        
        // 炸弹未安装的情况
        if (aliveTerrorists.isEmpty()) {
            // T队全灭，CT胜利
            endRound(Team.COUNTER_TERRORIST);
            broadcastMessage("§6本回合：恐怖分子全灭，反恐精英胜利！");
            return;
        } 
        
        if (aliveCounterTerrorists.isEmpty()) {
            // CT队全灭，T胜利
            endRound(Team.TERRORIST);
            broadcastMessage("§6本回合：反恐精英全灭，恐怖分子胜利！");
            return;
        }
    }
    
    // 获取存活玩家的辅助方法
    private Set<Player> getAlivePlayers(Team team) {
        return teamPlayers.get(team).stream()
            .filter(player -> !player.isDead())
            .collect(Collectors.toSet());
    }
    
    private boolean playerHasWeapon(Player player, String weaponId) {
        if (plugin.getWeaponManager().isTaczEnabled()) {
            // 对于TACZ武器，检查物品的NBT标签
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.hasItemMeta()) {
                    String nbtString = item.getItemMeta().toString();
                    if (nbtString.contains(weaponId)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            // 原版武器系统的检查逻辑
            return player.getInventory().contains(Material.DIAMOND_SWORD);
        }
    }
    
    private void giveWeapon(Player player, WeaponType type) {
        // 检查是否启用了TACZ支持
        boolean taczEnabled = plugin.getConfigManager().getModsConfig().getBoolean("tacz.enabled", true);
        
        if (taczEnabled) {
            // 从配置文件获取TACZ武器映射
            String taczWeaponId = plugin.getConfigManager().getModsConfig()
                .getString("tacz.weapons.mapping." + type.name(), null);
            
            if (taczWeaponId != null) {
                try {
                    // 使用TACZ的物品给予命令
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        "give " + player.getName() + " " + taczWeaponId);
                    return;
                } catch (Exception e) {
                    plugin.getLogManager().logError("无法给予TACZ武器: " + e.getMessage(), e);
                }
            }
        }
        
        // 如果TACZ不可用或出错，回退到默认武器
        try {
            Material weaponMaterial = Material.valueOf(type.getDefaultMaterial());
            ItemStack weapon = new ItemStack(weaponMaterial);
            player.getInventory().addItem(weapon);
        } catch (Exception e) {
            plugin.getLogManager().logError("无法给予默认武器: " + e.getMessage(), e);
        }
    }
    
    public void stop() {
        // 清理所有玩家
        for (Player player : new HashSet<>(players)) {
            removePlayer(player);
        }
        
        // 取消所有任务
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        if (roundTimerTaskId != -1) {
            Bukkit.getScheduler().cancelTask(roundTimerTaskId);
        }
        
        // 重置游戏状态
        state = GameState.WAITING;
        players.clear();
        playerTeams.clear();
        teamPlayers.get(Team.TERRORIST).clear();
        teamPlayers.get(Team.COUNTER_TERRORIST).clear();
        teamPlayers.get(Team.SPECTATOR).clear();
    }
    
    public void setLobbyLocation(Location location) {
        // 保存大厅位置
        this.lobbyLocation = location;
        // 可能还需要将位置保存到配置文件中
    }
    
    public boolean forceStop() {
        if (state == GameState.WAITING) {
            return false;
        }
        stop();
        return true;
    }
    
    public boolean forceStart() {
        if (state != GameState.WAITING) {
            return false;
        }
        return startGame();
    }
    
    public void setBuyZone(Location location) {
        this.buyZoneLocation = location;
    }
    
    public Location getBuyZone() {
        return buyZoneLocation;
    }
    
    // 获取队伍人数
    public int getTeamCount(Team team) {
        return teamPlayers.getOrDefault(team, new HashSet<>()).size();
    }
    
    public void setBombSite(String site, Location location, double radius) {
        registerBombSite(site, location, radius);
    }
    
    public boolean hasSpawnPoint(Team team) {
        return team == Team.TERRORIST ? tSpawn != null : ctSpawn != null;
    }
    
    public boolean hasBuyZone() {
        return buyZoneLocation != null;
    }
    
    public void reset() {
        state = GameState.WAITING;
        playerTeams.clear();
        for (Set<Player> players : teamPlayers.values()) {
            players.clear();
        }
        teamScores.put(Team.TERRORIST, 0);
        teamScores.put(Team.COUNTER_TERRORIST, 0);
        roundNumber = 0;
        bombPlanted = false;
        
        // 通知所有玩家
        broadcastMessage("§e游戏已被重置");
    }
    
    public void saveToConfig(ConfigurationSection section) {
        // 保存基本信息
        section.set("name", name);
        section.set("state", state.toString());
        
        // 保存位置信息
        if (lobbyLocation != null) {
            section.set("lobby", locationToString(lobbyLocation));
        }
        
        // 保存出生点
        if (tSpawn != null) {
            section.set("spawn.t", locationToString(tSpawn));
        }
        if (ctSpawn != null) {
            section.set("spawn.ct", locationToString(ctSpawn));
        }
        
        // 保存炸弹点
        // ... 其他游戏数据
    }
    
    private String locationToString(Location loc) {
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f",
            loc.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            loc.getYaw(),
            loc.getPitch());
    }
    
    // 添加一个方法来检查玩家是否可以安装炸弹
    public boolean canPlantBomb(Location location) {
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("检查是否可以安装炸弹 - 位置: " + location);
            bombSiteManager.debugPrintBombSites();
        }
        boolean canPlant = bombSiteManager.isInBombSite(location);
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("安装炸弹检查结果: " + (canPlant ? "可以" : "不可以"));
        }
        return canPlant;
    }
    
    public void startRound() {
        // 给T方随机一名玩家C4
        if (!getAlivePlayers(Team.TERRORIST).isEmpty()) {
            Player bomber = getRandomTerrorist();
            giveBomb(bomber);
        }
    }
    
    private Player getRandomTerrorist() {
        Set<Player> terrorists = getAlivePlayers(Team.TERRORIST);
        if (terrorists.isEmpty()) {
            return null;
        }
        // 将Set转换为List以便随机选择
        List<Player> terroristList = new ArrayList<>(terrorists);
        int randomIndex = (int) (Math.random() * terroristList.size());
        return terroristList.get(randomIndex);
    }
    
    private void giveBomb(Player player) {
        if (player == null) return;
        
        ItemStack bomb = new ItemStack(Material.TNT);
        ItemMeta meta = bomb.getItemMeta();
        meta.setDisplayName("§c炸弹(C4)");
        List<String> lore = new ArrayList<>();
        lore.add("§7右键点击炸弹安装点进行安装");
        lore.add("§e按住右键在炸弹点安装");
        lore.add("§c安装时请保持不动");
        meta.setLore(lore);
        bomb.setItemMeta(meta);
        player.getInventory().addItem(bomb);
        broadcastMessage("§e" + player.getName() + " 获得了C4炸弹!");
        
        // 播放获得C4的音效
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
    }
    
    // 处理玩家死亡时C4掉落
    public void handleBomberDeath(Player player) {
        if (player.getInventory().contains(Material.TNT)) {
            // 移除C4
            player.getInventory().remove(Material.TNT);
            // 在死亡位置掉落C4
            player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.TNT));
            broadcastMessage("§c" + player.getName() + " 死亡，C4已掉落！");
        }
    }
    
    // 处理C4拾取
    public void handleBombPickup(Player player) {
        if (getPlayerTeam(player) != Team.TERRORIST) {
            return;
        }
        giveBomb(player);
        broadcastMessage("§e" + player.getName() + " 捡起了C4！");
    }
    
    public void startPlantingBomb(Player player, Location location) {
        // 检查是否已经在安装
        if (plantingPlayer != null) {
            return;
        }
        
        // 检查是否在有效的炸弹点
        if (!bombSiteManager.isInBombSite(location)) {
            player.sendMessage("§c你必须在炸弹��装点安装炸弹！");
            return;
        }
        
        int plantTime = plugin.getConfigManager().getGameConfig().getInt("bomb.plant_time", 3);
        plantingPlayer = player;
        plantingLocation = location;
        plantProgress = 0;
        
        // 播放安装音效
        location.getWorld().playSound(location, Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
        
        bombPlantingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.getLocation().getBlock().equals(plantingLocation.getBlock())) {
                cancelPlanting(player);
                player.sendMessage("§c安装取消 - 你移动了位置！");
                return;
            }
            
            plantProgress++;
            
            // 显示进度条
            String progressBar = getProgressBar(plantProgress, plantTime);
            sendActionBar(player, progressBar);
            
            if (plantProgress >= plantTime) {
                bombPlantingTask.cancel();
                plantBomb(player, location);
            }
        }, 0L, 20L);
    }
    
    private void cancelPlanting(Player player) {
        if (bombPlantingTask != null) {
            bombPlantingTask.cancel();
            bombPlantingTask = null;
        }
        if (player.equals(plantingPlayer)) {
            sendActionBar(player, "§c炸弹安放已取消");
            plantingPlayer = null;
            plantingLocation = null;
            plantProgress = 0;
        }
    }
    
    private String getProgressBar(int current, int max) {
        int barLength = 20;
        int progress = (int) ((double) current / max * barLength);
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < barLength; i++) {
            if (i < progress) {
                bar.append("■");
            } else {
                bar.append("§7■");
            }
        }
        return bar.toString();
    }
    
    private void plantBomb(Player player, Location location) {
        // 清理安放状态
        plantingPlayer = null;
        plantingLocation = null;
        plantProgress = 0;
        
        bombPlanted = true;
        bombLocation = location;
        bombPlanter = player;
        
        // 创建TNT实体
        bombEntity = location.getWorld().spawn(location, TNTPrimed.class);
        bombEntity.setFuseTicks(Integer.MAX_VALUE);
        
        // 开始炸弹计时器
        startBombTimer();
        
        // 通知所有玩家
        broadcastMessage("§c炸弹已被安放！");
        
        // 给予安放奖励
        economy.addBonusReward(player, Economy.RewardType.BOMB_PLANT);
    }
    
    private void sendActionBar(Player player, String message) {
        player.sendTitle("", message, 0, 20, 0); // 用副标题作为进度条
    }
    
    public void addKill(Player player) {
        playerKills.put(player, getKills(player) + 1);
    }
    
    public void addDeath(Player player) {
        playerDeaths.put(player, getDeaths(player) + 1);
    }
} 