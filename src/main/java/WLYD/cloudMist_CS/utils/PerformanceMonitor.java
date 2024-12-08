package WLYD.cloudMist_CS.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import WLYD.cloudMist_CS.CloudMist_CS;

public class PerformanceMonitor {
    private static final ConcurrentHashMap<String, Long> timings = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> lastOutput = new ConcurrentHashMap<>();
    private static final Logger logger = CloudMist_CS.getInstance().getLogger();
    private static final long OUTPUT_INTERVAL = 60000; // 每分钟输出一次
    
    public static void startTiming(String operation) {
        timings.put(operation, System.nanoTime());
    }
    
    public static void endTiming(String operation) {
        Long start = timings.remove(operation);
        if (start != null) {
            long duration = System.nanoTime() - start;
            long currentTime = System.currentTimeMillis();
            Long lastOutputTime = lastOutput.get(operation);
            
            // 只有当距离上次输出超过指定间隔，且执行时间超过1ms时才输出
            if ((lastOutputTime == null || currentTime - lastOutputTime >= OUTPUT_INTERVAL) 
                && duration > 1_000_000) { // 1ms = 1,000,000ns
                logger.info(String.format("操作 %s 耗时: %.2fms", operation, duration / 1e6));
                lastOutput.put(operation, currentTime);
            }
        }
    }
    
    public static void clearTimings() {
        timings.clear();
        lastOutput.clear();
    }
} 