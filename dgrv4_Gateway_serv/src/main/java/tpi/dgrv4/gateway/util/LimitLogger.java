package tpi.dgrv4.gateway.util;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import tpi.dgrv4.gateway.keeper.TPILogger;

public class LimitLogger {
    // 使用ConcurrentHashMap來存儲不同消息標識符的最後輸出時間
    private static final ConcurrentHashMap<String, Instant> lastLogTimeMap = new ConcurrentHashMap<>();
    
    /**
     * 以限流方式輸出訊息，確保相同標識符的訊息至少間隔指定的秒數
     * 
     * @param message 要輸出的訊息
     * @param identifier 訊息的唯一標識符，用於區分不同的訊息流
     * @param intervalSeconds 最小間隔秒數
     * @return 訊息是否被實際輸出
     */
    public static boolean logWithThrottle(String message, String identifier, long intervalSeconds) {
        Instant now = Instant.now();
        Instant lastLogTime = lastLogTimeMap.getOrDefault(identifier, Instant.EPOCH);
        
        // 檢查是否已經過了足夠的時間間隔
        if (now.isAfter(lastLogTime.plusSeconds(intervalSeconds))) {
        	TPILogger.tl.info(message);
            lastLogTimeMap.put(identifier, now);
            return true;
        }
        
        return false;
    }
    
    /**
     * 以10秒為默認間隔的限流日誌方法
     * 
     * @param message 要輸出的訊息
     * @param identifier 訊息的唯一標識符
     * @return 訊息是否被實際輸出
     */
    public static boolean logWithThrottle(String message, String identifier) {
        return logWithThrottle(message, identifier, 10);
    }
}