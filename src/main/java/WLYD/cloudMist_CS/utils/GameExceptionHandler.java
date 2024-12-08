package WLYD.cloudMist_CS.utils;

import java.util.logging.Logger;
import WLYD.cloudMist_CS.CloudMist_CS;
import org.bukkit.entity.Player;

public class GameExceptionHandler {
    private static final Logger LOGGER = Logger.getLogger("CloudMist_CS");
    
    public static void handle(Exception e, String context) {
        LOGGER.severe(String.format("错误发生在 %s: %s", context, e.getMessage()));
        if (e instanceof GameException) {
            handleGameException((GameException) e);
        } else {
            handleGenericException(e);
        }
    }
    
    private static void handleGameException(GameException e) {
        LOGGER.warning("游戏异常: " + e.getMessage());
        Player player = e.getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage("§c发生错误: " + e.getMessage());
        }
    }
    
    private static void handleGenericException(Exception e) {
        LOGGER.severe("系统异常: " + e.getMessage());
        e.printStackTrace();
        
        // 通知管理员
        CloudMist_CS.getInstance().getServer().getOnlinePlayers().stream()
            .filter(p -> p.hasPermission("cloudmist.cs.admin"))
            .forEach(p -> p.sendMessage("§c系统错误，请查看控制台!"));
    }
} 