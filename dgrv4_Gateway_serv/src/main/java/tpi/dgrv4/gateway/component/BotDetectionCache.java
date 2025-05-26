package tpi.dgrv4.gateway.component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class BotDetectionCache {
    private final Cache<String, Boolean> userAgentCache;
    
    public BotDetectionCache() {
        userAgentCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }
    
    public Boolean get(String userAgent) {
        return userAgentCache.getIfPresent(userAgent);
    }
    
    public void put(String userAgent, Boolean result) {
        userAgentCache.put(userAgent, result);
    }
    
    public void invalidate(String userAgent) {
        userAgentCache.invalidate(userAgent);
    }
    
    public void invalidateAll() {
        userAgentCache.invalidateAll();
    }
}