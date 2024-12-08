package WLYD.cloudMist_CS.event;

import WLYD.cloudMist_CS.game.CSGame;

public class RoundStartEvent extends GameEvent {
    private final int roundNumber;
    
    public RoundStartEvent(CSGame game, int roundNumber) {
        super(game);
        this.roundNumber = roundNumber;
    }
    
    public int getRoundNumber() {
        return roundNumber;
    }
}

// 其他事件类也类似实现 