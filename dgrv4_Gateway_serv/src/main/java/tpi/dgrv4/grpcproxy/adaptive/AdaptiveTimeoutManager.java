package tpi.dgrv4.grpcproxy.adaptive;

import org.springframework.stereotype.Component;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptive timeout manager
 * Automatically adjusts RPC timeout settings based on historical call patterns
 */
@Component
public class AdaptiveTimeoutManager {
    private static final TPILogger logger = TPILogger.tl;

    // Timeout settings (seconds)
    private static final int DEFAULT_TIMEOUT = 300; // Default conservative 5-minute timeout
    private static final int MIN_TIMEOUT = 60;      // Minimum timeout (1 minute)
    private static final int MAX_TIMEOUT = 600;     // Maximum timeout (10 minutes)

    // Method call statistics
    private static class MethodStats {
        // Call count
        AtomicInteger callCount = new AtomicInteger(0);

        // Total duration (milliseconds)
        AtomicLong totalDuration = new AtomicLong(0);

        // Maximum duration (milliseconds)
        AtomicLong maxDuration = new AtomicLong(0);

        // Maximum messages
        AtomicInteger maxMessages = new AtomicInteger(0);

        // Total messages
        AtomicInteger totalMessages = new AtomicInteger(0);

        // Current timeout (seconds)
        AtomicInteger currentTimeout = new AtomicInteger(DEFAULT_TIMEOUT);

        // Whether streaming behavior is observed
        AtomicBoolean streamingBehaviorObserved = new AtomicBoolean(false);

        // Last update time
        AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());
    }

    // Method statistics cache
    private final Map<String, MethodStats> methodStats = new ConcurrentHashMap<>();

    // Active call tracking
    private final Map<String, CallTracker> activeTrackers = new ConcurrentHashMap<>();

    // Call tracker
    private static class CallTracker {
        final long startTime = System.currentTimeMillis();
        final AtomicInteger messageCount = new AtomicInteger(0);
        final String methodName;

        CallTracker(String methodName) {
            this.methodName = methodName;
        }
    }

    /**
     * Start tracking a new RPC call
     */
    public void startCall(String methodName, String callId) {
        // Create method statistics (if not exists)
        methodStats.computeIfAbsent(methodName, k -> new MethodStats());

        // Create and store call tracker
        activeTrackers.put(callId, new CallTracker(methodName));

        logger.debug("[" + callId + "] Started tracking call for method: " + methodName);
    }

    /**
     * Record message reception
     */
    public void recordMessage(String callId) {
        CallTracker tracker = activeTrackers.get(callId);
        if (tracker != null) {
            int count = tracker.messageCount.incrementAndGet();

            // Detect streaming behavior
            if (count > 1) {
                MethodStats stats = methodStats.get(tracker.methodName);
                if (stats != null) {
                    stats.streamingBehaviorObserved.set(true);
                }
            }
        }
    }

    /**
     * Complete call
     */
    public void completeCall(String methodName, String callId) {
        CallTracker tracker = activeTrackers.remove(callId);
        if (tracker == null) {
            return;
        }

        long duration = System.currentTimeMillis() - tracker.startTime;
        int messageCount = tracker.messageCount.get();

        // Update method statistics
        MethodStats stats = methodStats.get(methodName);
        if (stats != null) {
            // Update call count
            stats.callCount.incrementAndGet();

            // Update duration statistics
            stats.totalDuration.addAndGet(duration);
            stats.maxDuration.updateAndGet(current -> Math.max(current, duration));

            // Update message count statistics
            stats.totalMessages.addAndGet(messageCount);
            stats.maxMessages.updateAndGet(current -> Math.max(current, messageCount));

            // If message count > 1, mark as streaming
            if (messageCount > 1) {
                stats.streamingBehaviorObserved.set(true);
            }

            // Update timeout settings
            updateTimeoutForMethod(methodName, stats);

            // Update last update time
            stats.lastUpdateTime.set(System.currentTimeMillis());

            logger.debug("[" + callId + "] Completed call for method: " + methodName +
                    ", duration: " + duration + "ms, messages: " + messageCount);
        }
    }

    /**
     * Update timeout settings based on method statistics
     */
    private void updateTimeoutForMethod(String methodName, MethodStats stats) {
        // Only adjust timeout when there are enough samples
        if (stats.callCount.get() < 5) {
            return;
        }

        // Calculate average duration (milliseconds)
        long avgDuration = stats.callCount.get() > 0
                ? stats.totalDuration.get() / stats.callCount.get()
                : 0;

        // Calculate average message count
        double avgMessages = stats.callCount.get() > 0
                ? (double) stats.totalMessages.get() / stats.callCount.get()
                : 0;

        int newTimeout;

        if (stats.streamingBehaviorObserved.get()) {
            // Calculate timeout for streaming RPC: 2x max duration, but not exceeding max timeout
            long suggestedTimeout = Math.min((stats.maxDuration.get() * 2) / 1000, MAX_TIMEOUT);
            newTimeout = (int) Math.max(suggestedTimeout, DEFAULT_TIMEOUT);
        } else {
            // Timeout for single RPC: 1.5x max duration
            long suggestedTimeout = Math.min((stats.maxDuration.get() * 3 / 2) / 1000, DEFAULT_TIMEOUT);
            newTimeout = (int) Math.max(suggestedTimeout, MIN_TIMEOUT);
        }

        // Update timeout settings (if changed)
        int oldTimeout = stats.currentTimeout.getAndSet(newTimeout);
        if (oldTimeout != newTimeout) {
            logger.info("Adaptive timeout for method " + methodName + " updated: " +
                    oldTimeout + "s -> " + newTimeout + "s (streaming: " +
                    stats.streamingBehaviorObserved.get() + ", avg duration: " +
                    avgDuration + "ms, max duration: " + stats.maxDuration.get() +
                    "ms, avg messages: " + avgMessages + ")");
        }
    }

    /**
     * Get current timeout setting for method
     */
    public int getTimeout(String methodName) {
        MethodStats stats = methodStats.get(methodName);
        if (stats != null) {
            return stats.currentTimeout.get();
        }

        // Infer if method is likely streaming based on method name
        if (isLikelyStreamingMethod(methodName)) {
            return DEFAULT_TIMEOUT;
        }

        // Return conservative timeout setting by default
        return DEFAULT_TIMEOUT;
    }

    /**
     * Determine if method is likely streaming based on method name
     */
    private boolean isLikelyStreamingMethod(String methodName) {
    	return true;
        /*String lowerName = methodName.toLowerCase();
        return lowerName.contains("stream") ||
                lowerName.contains("watch") ||
                lowerName.contains("observe") ||
                lowerName.contains("monitor") ||
                lowerName.contains("subscribe") ||
                lowerName.contains("list") ||
                lowerName.contains("upload") ||
                lowerName.contains("download") ||
                lowerName.contains("chat");*/
    }

    /**
     * Clean up stale method statistics data
     */
    public void cleanupStaleMethodStats() {
        long now = System.currentTimeMillis();
        long staleThreshold = 24L * 60 * 60 * 1000; // 24 hours

        int removed = 0;
        for (Map.Entry<String, MethodStats> entry : methodStats.entrySet()) {
            if (now - entry.getValue().lastUpdateTime.get() > staleThreshold) {
                methodStats.remove(entry.getKey());
                removed++;
            }
        }

        if (removed > 0) {
            logger.info("Cleaned up " + removed + " stale method statistics");
        }
    }
}