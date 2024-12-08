package WLYD.cloudMist_CS.event;

import WLYD.cloudMist_CS.game.CSGame;
import org.bukkit.entity.Player;

public class PlayerDeathEvent extends GameEvent {
    private final Player victim;
    private final Player killer;
    
    public PlayerDeathEvent(CSGame game, Player victim, Player killer) {
        super(game);
        this.victim = victim;
        this.killer = killer;
    }
    
    public Player getVictim() { return victim; }
    public Player getKiller() { return killer; }
} 