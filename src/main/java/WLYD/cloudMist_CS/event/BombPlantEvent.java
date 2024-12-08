package WLYD.cloudMist_CS.event;

import WLYD.cloudMist_CS.game.CSGame;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class BombPlantEvent extends GameEvent {
    private final Player planter;
    private final Location location;
    
    public BombPlantEvent(CSGame game, Player planter, Location location) {
        super(game);
        this.planter = planter;
        this.location = location;
        this.type = GameEventType.BOMB_PLANTED;
    }
    
    public Player getPlanter() { return planter; }
    public Location getLocation() { return location; }
} 