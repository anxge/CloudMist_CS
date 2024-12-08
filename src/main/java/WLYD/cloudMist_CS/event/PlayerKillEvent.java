package WLYD.cloudMist_CS.event;

import WLYD.cloudMist_CS.game.CSGame;
import org.bukkit.entity.Player;

public class PlayerKillEvent extends GameEvent {
    private final Player killer;
    private final Player victim;
    
    public PlayerKillEvent(CSGame game, Player killer, Player victim) {
        super(game);
        this.killer = killer;
        this.victim = victim;
    }
    
    public Player getKiller() { return killer; }
    public Player getVictim() { return victim; }
} 