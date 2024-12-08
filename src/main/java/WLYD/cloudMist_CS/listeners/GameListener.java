package WLYD.cloudMist_CS.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import WLYD.cloudMist_CS.game.CSGame;
import WLYD.cloudMist_CS.game.Team;
import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.game.GameState;

public class GameListener implements Listener {
    private final CloudMist_CS plugin;

    public GameListener(CloudMist_CS plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = null;
        
        // 处理TACZ的伤害来源
        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE 
                || event.getCause() == EntityDamageEvent.DamageCause.CUSTOM) {
            // TACZ的子弹伤害通常会被标记为PROJECTILE或CUSTOM
            String damageCauseName = event.getDamager().getClass().getName().toLowerCase();
            if (damageCauseName.contains("bullet") || damageCauseName.contains("tacz")) {
                // 尝试从元数据中获取射击者
                if (event.getDamager().hasMetadata("shooter")) {
                    Object shooterObj = event.getDamager().getMetadata("shooter").get(0).value();
                    if (shooterObj instanceof Player) {
                        attacker = (Player) shooterObj;
                    }
                }
            }
        }

        if (attacker == null) {
            return;
        }

        CSGame game = plugin.getGameManager().getPlayerGame(victim);
        
        // 如果玩家不在游戏中，禁止所有伤害
        if (game == null) {
            event.setCancelled(true);
            return;
        }
        
        // 如果游戏未开始，禁止所有伤害
        if (game.getState() != GameState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        // 检查是否为队友伤害
        Team victimTeam = game.getPlayerTeam(victim);
        Team attackerTeam = game.getPlayerTeam(attacker);
        
        if (victimTeam == attackerTeam) {
            event.setCancelled(true);
            attacker.sendMessage("§c禁止伤害队友！");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        CSGame game = plugin.getGameManager().getPlayerGame(victim);
        if (game == null) return;
        
        // 更新死亡统计
        game.addDeath(victim);
        
        // 如果是被玩家击杀
        if (killer != null) {
            game.addKill(killer);
        }
        
        // 更新计分板
        plugin.getScoreboardManager().updateScoreboard(victim, game);
        if (killer != null) {
            plugin.getScoreboardManager().updateScoreboard(killer, game);
        }
        
        // 检查是否需要结束回合
        game.checkRoundEnd();
        
        // 如果死亡的玩家是T并且携带着炸弹
        if (game.getPlayerTeam(victim) == Team.TERRORIST) {
            game.handleBomberDeath(victim);
        }
    }
} 