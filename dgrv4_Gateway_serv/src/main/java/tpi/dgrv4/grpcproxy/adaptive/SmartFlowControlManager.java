package tpi.dgrv4.grpcproxy.adaptive;

import org.springframework.stereotype.Component;
import io.grpc.ClientCall;
import io.grpc.ServerCall;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Smart Flow Control Manager
 * Automatically adjusts flow control parameters based on call patterns
 */
@Component
public class SmartFlowControlManager {
    private static final TPILogger logger = TPILogger.tl;

    // Flow control settings
    private static final int DEFAULT_REQUEST_COUNT = 1;  // Default request one message
    private static final int MIN_STREAMING_REQUEST = 2;  // Minimum streaming request count
    private static final int MAX_REQUEST_COUNT = 8;      // Maximum request count

    // Flow control statistics
    private static class FlowStats {
        // Current request count
        AtomicInteger currentRequestCount = new AtomicInteger(DEFAULT_REQUEST_COUNT);

        // Pending message count
        AtomicInteger pendingCount = new AtomicInteger(0);

        // Processing time tracking
        AtomicLong lastProcessStart = new AtomicLong(0);
        AtomicLong lastProcessEnd = new AtomicLong(0);

        // Processing rate (messages/second)
        double processingRate = 1.0;

        // Total processed messages
        AtomicInteger totalProcessed = new AtomicInteger(0);

        // Whether is streaming
        AtomicBoolean isStreaming = new AtomicBoolean(false);
    }

    // Active call flow control state
    private final Map<String, FlowStats> activeFlowStats = new ConcurrentHashMap<>();

    /**
     * Initialize flow control for a call
     */
    public void initializeFlowControl(String callId) {
        FlowStats stats = new FlowStats();
        activeFlowStats.put(callId, stats);
    }

    /**
     * Get initial request message count
     */
    public int getInitialRequestCount(String callId, boolean isStreaming) {
        FlowStats stats = activeFlowStats.get(callId);
        if (stats == null) {
            return isStreaming ? MIN_STREAMING_REQUEST : DEFAULT_REQUEST_COUNT;
        }

        // If streaming RPC, use previous request count or higher initial value
        if (isStreaming) {
            stats.isStreaming.set(true);
            return Math.max(stats.currentRequestCount.get(), MIN_STREAMING_REQUEST);
        } else {
            return DEFAULT_REQUEST_COUNT;
        }
    }

    /**
     * Mark start of message processing
     */
    public void startProcessingMessage(String callId) {
        FlowStats stats = activeFlowStats.get(callId);
        if (stats != null) {
            stats.pendingCount.incrementAndGet();
            stats.lastProcessStart.set(System.currentTimeMillis());
        }
    }

    /**
     * Mark completion of message processing
     */
    public void completeProcessingMessage(String callId) {
        FlowStats stats = activeFlowStats.get(callId);
        if (stats != null) {
            stats.pendingCount.decrementAndGet();
            stats.totalProcessed.incrementAndGet();
            stats.lastProcessEnd.set(System.currentTimeMillis());

            // Update processing rate
            long startTime = stats.lastProcessStart.get();
            if (startTime > 0) {
                long processingTime = Math.max(System.currentTimeMillis() - startTime, 1); // Avoid division by zero
                // messages/second
                stats.processingRate = 1000.0 / processingTime; // Update processing rate
            }

            // If processed multiple messages, mark as streaming
            if (stats.totalProcessed.get() > 1) {
                stats.isStreaming.set(true);
            }
        }
    }

    /**
     * Calculate number of messages to request
     */
    public int calculateRequestCount(String callId, boolean isStreaming) {
        if (!isStreaming) {
            return DEFAULT_REQUEST_COUNT;
        }

        FlowStats stats = activeFlowStats.get(callId);
        if (stats == null) {
            return MIN_STREAMING_REQUEST;
        }

        // Current pending message count
        int pending = stats.pendingCount.get();
        int current = stats.currentRequestCount.get();

        // Adjust request count based on processing rate and pending messages
        if (pending == 0) {
            // No pending messages, can increase request count
            int newCount = Math.min(current * 2, MAX_REQUEST_COUNT);
            stats.currentRequestCount.set(newCount);
            return newCount;
        } else if (pending < current / 2) {
            // Few pending messages, moderately increase request count
            int newCount = Math.min(current + 1, MAX_REQUEST_COUNT);
            stats.currentRequestCount.set(newCount);
            return newCount;
        } else if (pending > current) {
            // Many pending messages, decrease request count
            int newCount = Math.max(current / 2, MIN_STREAMING_REQUEST);
            stats.currentRequestCount.set(newCount);
            return Math.max(1, newCount - pending); // Ensure at least request one
        }

        // Maintain current request count
        return Math.max(1, current - pending);
    }

    /**
     * Apply flow control to client call
     */
    public void applyFlowControl(ClientCall<?, ?> clientCall, String callId, boolean isStreaming) {
        int requestCount = calculateRequestCount(callId, isStreaming);

        if (requestCount > 0) {
            logger.debug("[" + callId + "] Requesting " + requestCount + " messages from client call");
            clientCall.request(requestCount);
        }
    }

    /**
     * Apply flow control to server call
     */
    public void applyFlowControl(ServerCall<?, ?> serverCall, String callId, boolean isStreaming) {
        int requestCount = calculateRequestCount(callId, isStreaming);

        if (requestCount > 0) {
            logger.debug("[" + callId + "] Requesting " + requestCount + " messages from server call");
            serverCall.request(requestCount);
        }
    }

    /**
     * Clean up flow control state for a call
     */
    public void cleanupFlowControl(String callId) {
        activeFlowStats.remove(callId);
    }

    /**
     * Determine if call is showing streaming behavior
     */
    public boolean isShowingStreamingBehavior(String callId) {
        FlowStats stats = activeFlowStats.get(callId);
        return stats != null && (stats.isStreaming.get() || stats.totalProcessed.get() > 1);
    }
}