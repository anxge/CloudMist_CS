package WLYD.cloudMist_CS.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Location;
import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.game.CSGame;
import WLYD.cloudMist_CS.game.GameState;

public class FreezeTimeListener implements Listener {
    private final CloudMist_CS plugin;
    
    public FreezeTimeListener(CloudMist_CS plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        CSGame game = plugin.getGameManager().getPlayerGame(event.getPlayer());
        if (game == null) return;
        
        GameState state = game.getState();
        if (state == GameState.FREEZE_TIME || state == GameState.BUY_TIME) {
            Location from = event.getFrom();
            Location to = event.getTo();
            
            // 只允许玩家转动视角，不允许移动位置
            if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
                event.setTo(new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch()));
            }
        }
    }
} 