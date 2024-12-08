package WLYD.cloudMist_CS.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import WLYD.cloudMist_CS.game.CSGame;

public class ScoreboardUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final CSGame game;
    private boolean cancelled;

    public ScoreboardUpdateEvent(Player player, CSGame game) {
        this.player = player;
        this.game = game;
    }

    public Player getPlayer() {
        return player;
    }

    public CSGame getGame() {
        return game;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
} 