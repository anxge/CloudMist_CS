package WLYD.cloudMist_CS.event;

import WLYD.cloudMist_CS.game.CSGame;
import org.bukkit.entity.Player;

public class BombDefuseEvent extends GameEvent {
    private final Player defuser;
    
    public BombDefuseEvent(CSGame game, Player defuser) {
        super(game);
        this.defuser = defuser;
        this.type = GameEventType.BOMB_DEFUSED;
    }
    
    public Player getDefuser() { return defuser; }
} 