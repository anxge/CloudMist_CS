package WLYD.cloudMist_CS.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

public class CacheManager {
    private static final Cache<String, Object> gameCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();
        
    private static final Cache<String, Object> playerCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .build();
        
    public static void cacheGameData(String key, Object data) {
        gameCache.put(key, data);
    }
    
    public static Object getGameData(String key) {
        return gameCache.getIfPresent(key);
    }
    
    public static void cachePlayerData(String key, Object data) {
        playerCache.put(key, data);
    }
    
    public static Object getPlayerData(String key) {
        return playerCache.getIfPresent(key);
    }
} 