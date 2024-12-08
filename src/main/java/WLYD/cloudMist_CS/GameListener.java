package WLYD.cloudMist_CS;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import WLYD.cloudMist_CS.game.CSGame;

public class GameListener implements Listener {
    private final CloudMist_CS plugin;
    
    public GameListener(CloudMist_CS plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // 使用plugin发送欢迎消息
        plugin.getLogger().info(event.getPlayer().getName() + " 加入了服务器");
        event.getPlayer().sendMessage(plugin.getConfig().getString("messages.welcome", "§6欢迎来到 CloudMist CS!"));
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getGameManager().leaveGame(player);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer == null) return;
        
        CSGame game = plugin.getGameManager().getPlayerGame(victim);
        if (game == null || game != plugin.getGameManager().getPlayerGame(killer)) return;
        
        // 记录击杀
        game.addKill(killer, victim);
    }
} 