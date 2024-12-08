package WLYD.cloudMist_CS.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GameLogger {
    private static final Logger logger = Logger.getLogger("CloudMist_CS");
    
    public static void logPerformance(String operation, long duration) {
        if (duration > 50_000_000) { // 50ms
            logger.warning(String.format("Performance warning: %s took %dms", 
                operation, duration / 1_000_000));
        }
    }
    
    public static void logDebug(String message) {
        logger.fine(message);
    }
    
    public static void logError(String message, Throwable error) {
        logger.log(Level.SEVERE, message, error);
    }
} 