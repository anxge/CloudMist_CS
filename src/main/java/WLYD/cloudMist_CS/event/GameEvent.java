package WLYD.cloudMist_CS.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import WLYD.cloudMist_CS.game.CSGame;
import WLYD.cloudMist_CS.game.GameState;

public class GameEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected final CSGame game;
    protected GameState state;
    protected GameEventType type;
    
    // 基础构造函数
    public GameEvent(CSGame game) {
        this.game = game;
        this.state = game.getState();
        this.type = GameEventType.GAME_START; // 默认类型
    }
    
    // 完整构造函数
    public GameEvent(CSGame game, GameState state, GameEventType type) {
        this.game = game;
        this.state = state;
        this.type = type;
    }
    
    public CSGame getGame() {
        return game;
    }
    
    public GameState getState() {
        return state;
    }
    
    public GameEventType getType() {
        return type;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
} 