package WLYD.cloudMist_CS.event;

import WLYD.cloudMist_CS.game.CSGame;
import WLYD.cloudMist_CS.game.GameState;

public class GameStateChangeEvent extends GameEvent {
    private final GameState oldState;
    private final GameState newState;
    
    public GameStateChangeEvent(CSGame game, GameState oldState, GameState newState) {
        super(game);
        this.oldState = oldState;
        this.newState = newState;
    }
    
    public GameState getOldState() { return oldState; }
    public GameState getNewState() { return newState; }
} 