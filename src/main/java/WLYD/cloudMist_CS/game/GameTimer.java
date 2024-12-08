package WLYD.cloudMist_CS.game;

import org.bukkit.scheduler.BukkitRunnable;
import WLYD.cloudMist_CS.CloudMist_CS;
import WLYD.cloudMist_CS.utils.GameLogger;

public class GameTimer {
    private final CSGame game;
    private BukkitRunnable timer;
    private int timeLeft;
    private boolean isPaused;
    
    public GameTimer(CSGame game) {
        this.game = game;
        this.isPaused = false;
    }
    
    public void start(int seconds) {
        if (timer != null) {
            timer.cancel();
        }
        
        timeLeft = seconds;
        isPaused = false;
        
        timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    if (timeLeft > 0) {
                        game.broadcastTime(timeLeft);
                        timeLeft--;
                    } else {
                        cancel();
                        game.handleTimeUp();
                    }
                }
            }
        };
        
        timer.runTaskTimer(CloudMist_CS.getInstance(), 0L, 20L);
    }
    
    public void pause() {
        isPaused = true;
        GameLogger.logDebug("Game timer paused at " + timeLeft + " seconds");
    }
    
    public void resume() {
        isPaused = false;
        GameLogger.logDebug("Game timer resumed with " + timeLeft + " seconds remaining");
    }
    
    public void cancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    
    public int getTimeLeft() {
        return timeLeft;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
} 