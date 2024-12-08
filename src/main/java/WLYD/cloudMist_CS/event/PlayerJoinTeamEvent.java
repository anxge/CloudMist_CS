package WLYD.cloudMist_CS.event;

import WLYD.cloudMist_CS.game.CSGame;
import WLYD.cloudMist_CS.game.Team;
import org.bukkit.entity.Player;

public class PlayerJoinTeamEvent extends GameEvent {
    private final Player player;
    private final Team team;
    
    public PlayerJoinTeamEvent(CSGame game, Player player, Team team) {
        super(game);
        this.player = player;
        this.team = team;
    }
    
    public Player getPlayer() { return player; }
    public Team getTeam() { return team; }
} 