package WLYD.cloudMist_CS.utils;

import org.bukkit.entity.Player;

public class GameException extends RuntimeException {
    private final Player player;
    
    public GameException(String message) {
        this(message, null);
    }
    
    public GameException(String message, Player player) {
        super(message);
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
} 