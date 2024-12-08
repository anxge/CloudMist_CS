package WLYD.cloudMist_CS.economy;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.game.CSGame;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.concurrent.ConcurrentHashMap;

public class Economy {
    public enum RewardType {
        BOMB_PLANT,
        BOMB_DEFUSE,
        HEADSHOT
    }
    
    private final CloudMist_CS plugin;
    private final Map<UUID, Integer> playerMoney;
    private Map<UUID, List<Transaction>> transactionHistory;
    private EconomyConfig config;
    private final Map<UUID, Integer> consecutiveLosses = new HashMap<>();
    
    public Economy(CloudMist_CS plugin) {
        this.plugin = plugin;
        this.playerMoney = new ConcurrentHashMap<>();
        this.config = new EconomyConfig();
        this.transactionHistory = new HashMap<>();
    }
    
    public void initializePlayer(Player player) {
        int startMoney = plugin.getConfigManager().getEconomyConfig().getInt("start_money", 800);
        setMoney(player, startMoney);
    }
    
    public void removePlayer(Player player) {
        playerMoney.remove(player.getUniqueId());
    }
    
    public int getMoney(Player player) {
        return playerMoney.getOrDefault(player.getUniqueId(), 0);
    }
    
    public void setMoney(Player player, int amount) {
        playerMoney.put(player.getUniqueId(), Math.max(0, amount));
        updateScoreboard(player);
    }
    
    public boolean addMoney(Player player, int amount) {
        int maxMoney = plugin.getConfigManager().getEconomyConfig().getInt("max_money", 16000);
        int currentMoney = getMoney(player);
        int newAmount = Math.min(currentMoney + amount, maxMoney);
        playerMoney.put(player.getUniqueId(), newAmount);
        updateScoreboard(player);
        return true;
    }
    
    public boolean removeMoney(Player player, int amount) {
        int currentMoney = getMoney(player);
        if (currentMoney >= amount) {
            playerMoney.put(player.getUniqueId(), currentMoney - amount);
            updateScoreboard(player);
            return true;
        }
        return false;
    }
    
    public void addKillReward(Player player) {
        addMoney(player, config.getKillReward());
        player.sendMessage(String.format("§a+$%d §7(击杀奖励)", config.getKillReward()));
    }
    
    public void addRoundReward(Player player, boolean isWinner) {
        UUID id = player.getUniqueId();
        int lossCount = consecutiveLosses.getOrDefault(id, 0);
        
        FileConfiguration config = plugin.getConfigManager().getEconomyConfig();
        if (isWinner) {
            addMoney(player, config.getInt("rewards.win_round", 3000));
            consecutiveLosses.put(id, 0);
        } else {
            int baseLoss = config.getInt("rewards.lose_bonus.base", 1400);
            int increment = config.getInt("rewards.lose_bonus.increment", 500);
            int maxLoss = config.getInt("rewards.lose_bonus.max", 3400);
            int lossBonus = baseLoss + (lossCount * increment);
            lossBonus = Math.min(lossBonus, maxLoss);
            addMoney(player, lossBonus);
            consecutiveLosses.put(id, lossCount + 1);
        }
    }
    
    private void updateScoreboard(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        CSGame game = plugin.getGameManager().getPlayerGame(player);
        if (game != null) {
            plugin.getScoreboardManager().updateScoreboard(player, game);
        }
    }
    
    public boolean canAfford(Player player, int amount) {
        return getMoney(player) >= amount;
    }
    
    public void refundLastPurchase(Player player) {
        // 实现退款逻辑
    }
    
    public List<Transaction> getTransactionHistory(Player player) {
        return transactionHistory.getOrDefault(player.getUniqueId(), List.of());
    }
    
    public void addBonusReward(Player player, RewardType type) {
        // 从配置文件读取奖励值
        int bombPlantReward = plugin.getConfigManager().getEconomyConfig().getInt("rewards.bomb_plant", 300);
        int bombDefuseReward = plugin.getConfigManager().getEconomyConfig().getInt("rewards.bomb_defuse", 300);
        int headshotReward = plugin.getConfigManager().getEconomyConfig().getInt("rewards.headshot", 100);
        
        switch (type) {
            case BOMB_PLANT:
                addMoney(player, bombPlantReward);
                break;
            case BOMB_DEFUSE:
                addMoney(player, bombDefuseReward);
                break;
            case HEADSHOT:
                addMoney(player, headshotReward);
                break;
        }
    }
} 