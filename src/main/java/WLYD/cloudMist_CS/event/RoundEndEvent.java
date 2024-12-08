package WLYD.cloudMist_CS.event;

import WLYD.cloudMist_CS.game.CSGame;
import WLYD.cloudMist_CS.game.Team;

public class RoundEndEvent extends GameEvent {
    private final Team winningTeam;
    
    public RoundEndEvent(CSGame game, Team winningTeam) {
        super(game);
        this.winningTeam = winningTeam;
        this.type = GameEventType.ROUND_END;
    }
    
    public Team getWinningTeam() {
        return winningTeam;
    }
} 