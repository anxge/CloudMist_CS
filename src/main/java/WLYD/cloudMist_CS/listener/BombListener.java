package WLYD.cloudMist_CS.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.game.CSGame;
import WLYD.cloudMist_CS.game.Team;
import java.util.HashMap;
import java.util.Map;

public class BombListener implements Listener {
    private final CloudMist_CS plugin;
    private final Map<Player, BukkitTask> defusingPlayers = new HashMap<>();
    
    public BombListener(CloudMist_CS plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        CSGame game = plugin.getGameManager().getPlayerGame(player);
        if (game == null) return;
        
        // 处理T队安装炸弹
        if (game.getPlayerTeam(player) == Team.TERRORIST && 
            event.getAction() == Action.RIGHT_CLICK_BLOCK && 
            event.getItem() != null && 
            event.getItem().getType() == Material.TNT) {
            
            handleBombPlant(event, game, player);
        }
        
        // 处理CT队拆除炸弹
        if (game.getPlayerTeam(player) == Team.COUNTER_TERRORIST && 
            event.getAction() == Action.RIGHT_CLICK_BLOCK && 
            game.isBombPlanted()) {
            
            handleBombDefuse(event, game, player);
        }
    }

    private void handleBombPlant(PlayerInteractEvent event, CSGame game, Player player) {
        event.setCancelled(true);
        Location location = event.getClickedBlock().getLocation();
        
        // 检查是否在炸弹安装点范围内
        if (!game.canPlantBomb(location)) {
            player.sendMessage("§c你必须在炸弹安装点安装炸弹！");
            return;
        }
        
        // 开始安装炸弹
        game.startPlantingBomb(player, location);
        
        // 移除TNT物品
        ItemStack tnt = event.getItem();
        if (tnt != null && tnt.getAmount() > 1) {
            tnt.setAmount(tnt.getAmount() - 1);
        } else {
            player.getInventory().remove(tnt);
        }
    }
    
    private void handleBombDefuse(PlayerInteractEvent event, CSGame game, Player player) {
        event.setCancelled(true);
        Location bombLoc = game.getBombLocation();
        Location clickedLoc = event.getClickedBlock().getLocation();
        
        // 检查点击的方块是否是炸弹位置
        if (!bombLoc.getBlock().equals(clickedLoc.getBlock())) {
            return;
        }
        
        // 如果玩家已经在拆弹，不要重复开始
        if (defusingPlayers.containsKey(player)) {
            return;
        }
        
        // 获取拆弹时间
        int defuseTime = game.getSettings().getDefuseTime();
        
        // 创建拆弹倒计时任务
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            private int timeLeft = defuseTime;
            
            @Override
            public void run() {
                if (!player.isOnline() || 
                    !game.isBombPlanted() || 
                    player.getLocation().distance(clickedLoc) > 3) {
                    // 取消拆弹
                    cancelDefusing(player);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        new TextComponent("§c拆弹失败！你移动了位置或离开了拆弹区域。"));
                    return;
                }
                
                if (timeLeft > 0) {
                    // 显示拆弹进度条
                    StringBuilder progressBar = new StringBuilder("§e拆弹中... §7[");
                    int totalBars = 20;
                    int filledBars = (int) ((defuseTime - timeLeft) * totalBars / defuseTime);
                    progressBar.append("§a").append("=".repeat(filledBars))
                             .append("§7").append("-".repeat(totalBars - filledBars))
                             .append("§7] §f").append(timeLeft).append("秒");
                    
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        new TextComponent(progressBar.toString()));
                    timeLeft--;
                } else {
                    // 完成拆弹
                    game.onBombDefused(player);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        new TextComponent("§a成功拆除炸弹！"));
                    cancelDefusing(player);
                }
            }
        }, 0L, 20L);
        
        defusingPlayers.put(player, task);
    }
    
    private void cancelDefusing(Player player) {
        BukkitTask task = defusingPlayers.remove(player);
        if (task != null) {
            task.cancel();
        }
    }
} 