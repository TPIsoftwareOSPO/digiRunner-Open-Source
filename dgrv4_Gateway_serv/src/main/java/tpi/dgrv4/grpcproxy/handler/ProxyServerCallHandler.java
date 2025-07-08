package tpi.dgrv4.grpcproxy.handler;

import com.google.common.io.ByteStreams;
import io.grpc.*;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.grpcproxy.adaptive.AdaptiveTimeoutManager;
import tpi.dgrv4.grpcproxy.adaptive.SmartFlowControlManager;
import tpi.dgrv4.grpcproxy.marshaller.PassthroughMarshaller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Proxy handler for gRPC services, responsible for forwarding requests to target services and returning responses to clients.
 * Features adaptive timeout and smart flow control capabilities, automatically adapting to different types of RPC calls.
 */
public class ProxyServerCallHandler implements ServerCallHandler<InputStream, InputStream> {
    private static final TPILogger logger = TPILogger.tl;
    private final ManagedChannel channel;
    private final String fullMethodName;
    private static final AtomicLong callCounter = new AtomicLong(0);
    private static final boolean ENABLE_VERBOSE_LOGGING = false; // New switch to control verbose logging

    // Resource monitoring counters
    private static final AtomicInteger activeClientStreams = new AtomicInteger(0);
    private static final AtomicInteger totalMessagesReceived = new AtomicInteger(0);
    private static final AtomicInteger activeCalls = new AtomicInteger(0);

    // Periodic log output
    static {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> logger.info("Resource usage metrics: active calls=" + activeCalls.get() +
                ", active client streams=" + activeClientStreams.get() +
                ", total messages=" + totalMessagesReceived.get()), 30, 30, TimeUnit.SECONDS);
    }

    // Injected manager instances
    private static AdaptiveTimeoutManager timeoutManager;
    private static SmartFlowControlManager flowControlManager;

    // Conservative default timeout settings (seconds)
    private static final int DEFAULT_TIMEOUT_SECONDS = 300; // Conservative default timeout of 5 minutes
    private static final int UNARY_TIMEOUT_SECONDS = 60;    // Unary calls use shorter timeout

    // Timeout settings for different RPC types
    private static final int MIN_TIMEOUT_SECONDS = 60;     // Minimum timeout (1 minute)
    
    // Constants for string literals
    private static final String ALREADY_HALF_CLOSED_MSG = "already half-closed";
    private static final String CALL_ALREADY_HALF_CLOSED_LOG = "] Call already half-closed, ignored";
    private static final String MESSAGE_PREFIX = "] Message #";
    private static final String MESSAGES_SUFFIX = " messages";
    
    // Constants for magic numbers
    private static final int MESSAGE_SIZE_LIMIT_MB = 20;
    private static final int MESSAGE_SIZE_LIMIT_BYTES = MESSAGE_SIZE_LIMIT_MB * 1024 * 1024;
    private static final int HIGH_INITIAL_REQUEST_COUNT = 20;
    private static final int MAX_DELAY_MS = 1000;
    private static final int DELAY_MULTIPLIER = 50;
    private static final int MESSAGE_COUNT_DELAY_THRESHOLD = 5;
    private static final int UUID_SUBSTRING_LENGTH = 8;

    /**
     * Static method for injecting manager instances
     */
    public static void injectManagers(AdaptiveTimeoutManager timeoutMgr, SmartFlowControlManager flowControlMgr) {
        timeoutManager = timeoutMgr;
        flowControlManager = flowControlMgr;
    }

    /**
     * Constructor, creates a new proxy handler
     *
     * @param channel Channel to the target service
     * @param fullMethodName Full method name being proxied
     */
    public ProxyServerCallHandler(ManagedChannel channel, String fullMethodName) {
        this.channel = channel;
        this.fullMethodName = fullMethodName;
        if (ENABLE_VERBOSE_LOGGING)
            logger.debug("Created ProxyServerCallHandler for method: " + fullMethodName);
    }

    /**
     * Generate unique call ID for log tracking
     */
    private String generateCallId() {
        String methodShortName = fullMethodName;
        int lastSlashIndex = fullMethodName.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < fullMethodName.length() - 1) {
            methodShortName = fullMethodName.substring(lastSlashIndex + 1);
        }

        return methodShortName + "-" +
                callCounter.incrementAndGet() + "-" +
                java.util.UUID.randomUUID().toString().substring(0, UUID_SUBSTRING_LENGTH);
    }

    /**
     * Called by gRPC when a new call is received.
     * Sets up proxy mechanism to forward messages between incoming server call and new client call.
     */
    @Override
    public ServerCall.Listener<InputStream> startCall(
            ServerCall<InputStream, InputStream> serverCall,
            Metadata headers) {

        // Increment active call count
        activeCalls.incrementAndGet();

        // Generate unique call ID for tracking
        String callId = generateCallId();

        if (ENABLE_VERBOSE_LOGGING) {
            logger.debug("[" + callId + "] Starting call for method: " + fullMethodName);
            logger.debug("[" + callId + "] Headers received: " + headers);
        }

        try {
            // Check if managers are injected
            if (timeoutManager == null || flowControlManager == null) {
                logger.warn("[" + callId + "] Adaptive managers not injected, using default settings");
                return fallbackStartCall(serverCall, headers, callId);
            }

            // Start tracking call for adaptive mechanisms
            timeoutManager.startCall(fullMethodName, callId);
            flowControlManager.initializeFlowControl(callId);

            // Get adaptive timeout for method (based on historical call patterns)
            int timeoutSeconds = getTimeoutForMethod(fullMethodName, callId);

            // Check if method name looks like Unary method (doesn't contain stream or flow)
            boolean isLikelyUnary = !isLikelyStreamingMethod(fullMethodName);
            if (isLikelyUnary)
                timeoutSeconds = Math.min(timeoutSeconds, UNARY_TIMEOUT_SECONDS);

            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] Setting deadline to " + timeoutSeconds + " seconds from now");

            // Create client call to target service
            ClientCall<InputStream, InputStream> clientCall = createClientCall(timeoutSeconds);

            // Determine if this is likely a streaming RPC
            boolean isLikelyStreaming = isLikelyStreamingMethod(fullMethodName) ||
                    flowControlManager.isShowingStreamingBehavior(callId);

            // Create adaptive client call listener
            AdaptiveClientCallListener clientCallListener =
                    new AdaptiveClientCallListener(serverCall, callId, isLikelyStreaming);

            // Start client call with original headers
            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] Starting client call");
            clientCall.start(clientCallListener, headers);

            // Set up initial flow control
            setupInitialFlowControl(clientCall, serverCall, callId, isLikelyStreaming);

            // Create and return adaptive server call listener
            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] Creating adaptive server call listener");
            return new AdaptiveServerCallListener(clientCall, serverCall, callId, isLikelyStreaming);
        } catch (Exception e) {
            logger.error("[" + callId + "] Error in startCall for method: " + fullMethodName + ": " + e.getMessage());
            // Decrement count because call failed
            activeCalls.decrementAndGet();
            throw e;
        }
    }

    /**
     * Helper method to create client call with timeout and message size limits
     */
    private ClientCall<InputStream, InputStream> createClientCall(int timeoutSeconds) {
        return channel.newCall(
                MethodDescriptor.<InputStream, InputStream>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNKNOWN)
                        .setFullMethodName(fullMethodName)
                        .setRequestMarshaller(new PassthroughMarshaller())
                        .setResponseMarshaller(new PassthroughMarshaller())
                        .build(),
                CallOptions.DEFAULT
                        .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                        .withMaxInboundMessageSize(MESSAGE_SIZE_LIMIT_BYTES)
                        .withMaxOutboundMessageSize(MESSAGE_SIZE_LIMIT_BYTES));
    }

    /**
     * Helper method to set up initial flow control requests
     */
    private void setupInitialFlowControl(ClientCall<InputStream, InputStream> clientCall, 
                                         ServerCall<InputStream, InputStream> serverCall,
                                         String callId, boolean isLikelyStreaming) {
        int initialClientRequest = flowControlManager.getInitialRequestCount(callId, isLikelyStreaming);
        int initialServerRequest = flowControlManager.getInitialRequestCount(callId, isLikelyStreaming);

        if (ENABLE_VERBOSE_LOGGING)
            logger.debug("[" + callId + "] Setting initial flow control (likely streaming: " + isLikelyStreaming +
                    ", client req: " + initialClientRequest + ", server req: " + initialServerRequest + ")");

        clientCall.request(initialClientRequest);

        if (fullMethodName.contains("ClientStream") || fullMethodName.contains("Bidirectional")) {
            serverCall.request(HIGH_INITIAL_REQUEST_COUNT);
            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] Detected streaming method, requesting " +
                        HIGH_INITIAL_REQUEST_COUNT + " messages initially");
        } else {
            serverCall.request(initialServerRequest);
        }
    }

    /**
     * Fallback method when managers are not injected
     */
    private ServerCall.Listener<InputStream> fallbackStartCall(
            ServerCall<InputStream, InputStream> serverCall,
            Metadata headers, String callId) {

        int timeoutSeconds = determineFallbackTimeout();
        ClientCall<InputStream, InputStream> clientCall = createFallbackClientCall(timeoutSeconds);
        ClientCall.Listener<InputStream> clientCallListener = createFallbackClientListener(serverCall);
        
        clientCall.start(clientCallListener, headers);
        setupFallbackFlowControl(clientCall, serverCall);
        
        return createFallbackServerListener(clientCall, serverCall, callId);
    }

    /**
     * Determine timeout for fallback mode
     */
    private int determineFallbackTimeout() {
        return isLikelyStreamingMethod(fullMethodName) ? DEFAULT_TIMEOUT_SECONDS : UNARY_TIMEOUT_SECONDS;
    }

    /**
     * Create client call for fallback mode
     */
    private ClientCall<InputStream, InputStream> createFallbackClientCall(int timeoutSeconds) {
        return channel.newCall(
                MethodDescriptor.<InputStream, InputStream>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNKNOWN)
                        .setFullMethodName(fullMethodName)
                        .setRequestMarshaller(new PassthroughMarshaller())
                        .setResponseMarshaller(new PassthroughMarshaller())
                        .build(),
                CallOptions.DEFAULT
                        .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
                        .withMaxInboundMessageSize(20 * 1024 * 1024)
                        .withMaxOutboundMessageSize(20 * 1024 * 1024));
    }

    /**
     * Create client call listener for fallback mode
     */
    private ClientCall.Listener<InputStream> createFallbackClientListener(
            ServerCall<InputStream, InputStream> serverCall) {
        return new FallbackClientListener(serverCall);
    }

    /**
     * Setup flow control for fallback mode
     */
    private void setupFallbackFlowControl(ClientCall<InputStream, InputStream> clientCall,
                                         ServerCall<InputStream, InputStream> serverCall) {
        boolean isLikelyStreaming = isLikelyStreamingMethod(fullMethodName);
        int initialRequestCount = isLikelyStreaming ? 2 : 1;

        clientCall.request(initialRequestCount);

        if (fullMethodName.contains("ClientStream") || fullMethodName.contains("Bidirectional")) {
            initialRequestCount = 10;
        }

        serverCall.request(initialRequestCount);
    }

    /**
     * Create server listener for fallback mode
     */
    private ServerCall.Listener<InputStream> createFallbackServerListener(
            ClientCall<InputStream, InputStream> clientCall,
            ServerCall<InputStream, InputStream> serverCall,
            String callId) {
        return new FallbackServerListener(clientCall, serverCall, callId);
    }

    /**
     * Simple client listener for fallback mode
     */
    private static class FallbackClientListener extends ClientCall.Listener<InputStream> {
        private final ServerCall<InputStream, InputStream> serverCall;
        private boolean headersSent = false;

        public FallbackClientListener(ServerCall<InputStream, InputStream> serverCall) {
            this.serverCall = serverCall;
        }

        @Override
        public void onMessage(InputStream message) {
            ensureHeadersSent();
            serverCall.sendMessage(message);
            serverCall.request(1);
        }

        @Override
        public void onClose(Status status, Metadata trailers) {
            ensureHeadersSent();
            serverCall.close(status, trailers);
            activeCalls.decrementAndGet();
        }

        @Override
        public void onReady() {
            // No-op
        }

        private void ensureHeadersSent() {
            if (!headersSent) {
                serverCall.sendHeaders(new Metadata());
                headersSent = true;
            }
        }
    }

    /**
     * Server listener for fallback mode
     */
    private class FallbackServerListener extends ServerCall.Listener<InputStream> {
        private final ClientCall<InputStream, InputStream> clientCall;
        private final ServerCall<InputStream, InputStream> serverCall;
        private final String callId;
        private final Set<InputStream> activeStreams = Collections.newSetFromMap(new ConcurrentHashMap<>());
        private final AtomicBoolean halfCloseProcessed = new AtomicBoolean(false);
        private final List<byte[]> bufferedMessages = new ArrayList<>();
        
        private int messageCount = 0;
        private boolean isActive = true;

        public FallbackServerListener(ClientCall<InputStream, InputStream> clientCall,
                                     ServerCall<InputStream, InputStream> serverCall,
                                     String callId) {
            this.clientCall = clientCall;
            this.serverCall = serverCall;
            this.callId = callId;
        }

        @Override
        public void onMessage(InputStream message) {
            if (!handleActiveCheck(message)) {
                return;
            }

            prepareMessage(message);

            if (tryUnaryFastPath(message)) {
                return;
            }

            processMessageStandard(message);
        }

        private boolean handleActiveCheck(InputStream message) {
            if (!isActive) {
                try {
                    message.close();
                } catch (IOException e) {
                    logger.warn("[" + callId + "] Error closing message in inactive state: " + e.getMessage());
                }
                return false;
            }
            return true;
        }

        private void prepareMessage(InputStream message) {
            messageCount++;
            activeStreams.add(message);
            totalMessagesReceived.incrementAndGet();
        }

        private boolean tryUnaryFastPath(InputStream message) {
            if (!isLikelyStreamingMethod(fullMethodName) && messageCount == 1) {
                try {
                    clientCall.sendMessage(message);
                    activeStreams.remove(message);
                    clientCall.halfClose();
                    halfCloseProcessed.set(true);
                    serverCall.request(1);
                    return true;
                } catch (Exception e) {
                    logger.error("[" + callId + "] Error in fast path, falling back: " + e.getMessage());
                }
            }
            return false;
        }

        private void processMessageStandard(InputStream message) {
            try {
                byte[] messageData = ByteStreams.toByteArray(message);
                bufferedMessages.add(messageData);
                
                if (ENABLE_VERBOSE_LOGGING)
                    logger.debug("[" + callId + MESSAGE_PREFIX + messageCount + " added to buffer");

                processBufferedMessages();
                activeStreams.remove(message);
                serverCall.request(1);
            } catch (Exception e) {
                handleMessageError(message, e);
            }
        }

        private void handleMessageError(InputStream message, Exception e) {
            logger.error("[" + callId + "] Error forwarding message: " + e.getMessage());
            
            cleanupMessageOnError(message);
            closeCallsOnError(e);
            cleanupAndDeactivate();
        }

        private void cleanupMessageOnError(InputStream message) {
            try {
                message.close();
            } catch (IOException ioe) {
                // Ignore close exception
            }
            activeStreams.remove(message);
        }

        private void closeCallsOnError(Exception e) {
            Status status = Status.INTERNAL.withDescription("Error forwarding message: " + e.getMessage());
            serverCall.close(status, new Metadata());
            clientCall.cancel("Error processing message", null);
        }

        private void cleanupAndDeactivate() {
            isActive = false;
            cleanupStreams();
            activeCalls.decrementAndGet();
        }

        private void processBufferedMessages() {
            while (!bufferedMessages.isEmpty() && isActive) {
                if (!processSingleMessage()) {
                    break;
                }
            }

            tryDelayedHalfClose();
        }

        private boolean processSingleMessage() {
            try {
                byte[] data = bufferedMessages.getFirst();
                InputStream newStream = new ByteArrayInputStream(data);
                
                clientCall.sendMessage(newStream);
                if (ENABLE_VERBOSE_LOGGING)
                    logger.debug("[" + callId + "] Sent buffered message to upstream service");

                bufferedMessages.removeFirst();
                clientCall.request(1);
                return true;
            } catch (Exception e) {
                logger.error("[" + callId + "] Error sending buffered message: " + e.getMessage());
                return false;
            }
        }

        private void tryDelayedHalfClose() {
            if (shouldExecuteDelayedHalfClose()) {
                executeDelayedHalfClose();
            }
        }

        private boolean shouldExecuteDelayedHalfClose() {
            return bufferedMessages.isEmpty() && halfCloseProcessed.get() && isActive;
        }

        private void executeDelayedHalfClose() {
            try {
                clientCall.halfClose();
                if (ENABLE_VERBOSE_LOGGING)
                    logger.debug("[" + callId + "] Sending delayed half-close");
            } catch (IllegalStateException e) {
                handleHalfCloseStateException(e);
            } catch (Exception e) {
                logger.error("[" + callId + "] Error in delayed half-close: " + e.getMessage());
            }
        }

        private void handleHalfCloseStateException(IllegalStateException e) {
            if (e.getMessage().contains(ALREADY_HALF_CLOSED_MSG)) {
                if (ENABLE_VERBOSE_LOGGING)
                    logger.debug("[" + callId + CALL_ALREADY_HALF_CLOSED_LOG);
            } else {
                logger.error("[" + callId + "] Error in delayed half-close: " + e.getMessage());
            }
        }

        @Override
        public void onHalfClose() {
            if (!isActive) return;

            try {
                if (trySetHalfCloseProcessed()) {
                    logHalfCloseStart();
                    
                    if (isUnaryCall()) {
                        handleUnaryHalfClose();
                        return;
                    }

                    handleStreamingHalfClose();
                } else {
                    logDuplicateHalfClose();
                }
            } catch (Exception e) {
                handleHalfCloseError(e);
            }
        }

        private boolean trySetHalfCloseProcessed() {
            return halfCloseProcessed.compareAndSet(false, true);
        }

        private void logHalfCloseStart() {
            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] Client stream completed, beginning half-close operation");
        }

        private boolean isUnaryCall() {
            return !isLikelyStreamingMethod(fullMethodName) && messageCount <= 1;
        }

        private void handleUnaryHalfClose() {
            processBufferedMessages();
            cleanupStreams();
            
            if (bufferedMessages.isEmpty() && isActive) {
                executeImmediateHalfClose();
            }
        }

        private void executeImmediateHalfClose() {
            try {
                clientCall.halfClose();
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("[" + callId + "] Client call half-closed successfully (no delay for Unary)");
                }
            } catch (IllegalStateException e) {
                handleHalfCloseStateException(e);
            }
        }

        private void handleStreamingHalfClose() {
            addMultiMessageDelay();
            processBufferedMessages();
            cleanupStreams();
            executeStreamingHalfClose();
        }

        private void addMultiMessageDelay() {
            if (messageCount > 2) {
                try {
                    if (ENABLE_VERBOSE_LOGGING)
                        logger.debug("[" + callId + "] Multiple messages detected, adding delay before half-close");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void executeStreamingHalfClose() {
            if (bufferedMessages.isEmpty() && isActive) {
                try {
                    clientCall.halfClose();
                    if (ENABLE_VERBOSE_LOGGING)
                        logger.debug("[" + callId + "] Client call half-closed successfully");
                } catch (IllegalStateException e) {
                    handleHalfCloseStateException(e);
                }
            } else {
                logDelayedHalfClose();
            }
        }

        private void logDelayedHalfClose() {
            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] Delaying half-close until buffer is processed");
        }

        private void logDuplicateHalfClose() {
            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] Half-close already processed, ignoring duplicate call");
        }

        private void handleHalfCloseError(Exception e) {
            logger.error("[" + callId + "] Error during half-close: " + e.getMessage());
            
            isActive = false;
            Status status = Status.INTERNAL.withDescription("Error during half-close: " + e.getMessage());
            serverCall.close(status, new Metadata());
            clientCall.cancel("Error during half-close", null);
            cleanupStreams();
            activeCalls.decrementAndGet();
        }

        @Override
        public void onCancel() {
            try {
                clientCall.cancel("Client cancelled the call", null);
            } catch (Exception e) {
                logger.error("[" + callId + "] Error cancelling client call: " + e.getMessage());
            } finally {
                isActive = false;
                cleanupStreams();
                activeCalls.decrementAndGet();
            }
        }

        @Override
        public void onComplete() {
            cleanupAndDeactivate();
        }

        @Override
        public void onReady() {
            // No-op
        }

        private void cleanupStreams() {
            for (InputStream stream : activeStreams) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // Ignore close exception
                }
            }
            activeStreams.clear();
        }
    }

    /**
     * Determine if a method is likely streaming based on its name
     */
    private boolean isLikelyStreamingMethod(String methodName) {
        String lowerName = methodName.toLowerCase();
        return lowerName.contains("stream") ||
                lowerName.contains("watch") ||
                lowerName.contains("observe") ||
                lowerName.contains("monitor") ||
                lowerName.contains("subscribe") ||
                lowerName.contains("list") ||
                lowerName.contains("chat") ||
                lowerName.contains("continuous");
    }

    /**
     * Get recommended timeout for method
     */
    private int getTimeoutForMethod(String methodName, String callId) {
        // First try to get timeout from adaptive manager
        int adaptiveTimeout = timeoutManager.getTimeout(methodName);

        if (adaptiveTimeout > 0) {
            // Use adaptive timeout, but ensure at least minimum timeout
            return Math.max(adaptiveTimeout, MIN_TIMEOUT_SECONDS);
        }

        // If adaptive manager has no suggestion, judge based on method name
        if (isLikelyStreamingMethod(methodName)) {
            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("[" + callId + "] Method name suggests streaming, using extended timeout");
            }
            return DEFAULT_TIMEOUT_SECONDS;
        }

        // Check if it's a Unary method
        if (!isLikelyStreamingMethod(methodName)) {
            return UNARY_TIMEOUT_SECONDS; // Use shorter timeout
        }

        // Otherwise use default timeout (conservative setting)
        return DEFAULT_TIMEOUT_SECONDS;
    }

    /**
     * Adaptive client call listener, handles responses received from target service
     */
    private class AdaptiveClientCallListener extends ClientCall.Listener<InputStream> {
        private final ServerCall<InputStream, InputStream> serverCall;
        private final String callId;
        private boolean headersSent = false;
        private int messageCount = 0;
        private boolean isStreaming;

        // Track resource usage
        private final Set<InputStream> activeStreams = Collections.newSetFromMap(new ConcurrentHashMap<>());

        /**
         * Create a new adaptive client call listener
         *
         * @param serverCall Server call for sending responses back to original client
         * @param callId Call ID for log tracking
         */
        public AdaptiveClientCallListener(ServerCall<InputStream, InputStream> serverCall,
                                          String callId, boolean initialIsStreaming) {
            this.serverCall = serverCall;
            this.callId = callId;
            this.isStreaming = initialIsStreaming;
            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] Created new AdaptiveClientCallListener");
        }

        /**
         * Called when message is received from target service.
         * Forward message back to original client and update flow control.
         */
        @Override
        public void onMessage(InputStream message) {
            initializeMessageProcessing(message);
            recordMessageStatistics();

            if (tryUnaryFastPath(message)) {
                return;
            }

            try {
                processMessageStandard(message);
            } catch (Exception e) {
                throw new StatusRuntimeException(Status.INTERNAL
                    .withDescription("Error processing message in client call listener: " + e.getMessage())
                    .withCause(e));
            }
        }

        /**
         * Initialize message processing - update counters and streaming detection
         */
        private void initializeMessageProcessing(InputStream message) {
            messageCount++;
            activeStreams.add(message);

            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] AdaptiveClientCallListener received message #" + messageCount);

            // Update streaming RPC detection
            if (messageCount > 1)
                isStreaming = true;
        }

        /**
         * Record message reception for statistics and adaptive mechanisms
         */
        private void recordMessageStatistics() {
            if (timeoutManager != null)
                timeoutManager.recordMessage(callId);
        }

        /**
         * Try fast path processing for Unary responses
         */
        private boolean tryUnaryFastPath(InputStream message) {
            if (messageCount == 1 && !isStreaming) {
                try {
                    ensureHeadersSent();
                    serverCall.sendMessage(message);
                    completeUnaryProcessing(message);
                    return true;
                } catch (Exception e) {
                    logger.error("[" + callId + "] Error in fast path: " + e.getMessage());
                    // Continue with standard processing logic
                }
            }
            return false;
        }

        /**
         * Complete processing for Unary messages
         */
        private void completeUnaryProcessing(InputStream message) {
            if (flowControlManager != null)
                flowControlManager.completeProcessingMessage(callId);
            activeStreams.remove(message);
        }

        /**
         * Process message using standard approach
         */
        private void processMessageStandard(InputStream message) throws StatusRuntimeException {
            try {
                ensureHeadersSentWithLogging();
                forwardMessageToClient(message);
                completeStandardProcessing(message);
                applyFlowControl();
            } catch (Exception e) {
                handleMessageForwardingError(message, e);
            }
        }

        /**
         * Ensure headers are sent
         */
        private void ensureHeadersSent() {
            if (!headersSent) {
                serverCall.sendHeaders(new Metadata());
                headersSent = true;
            }
        }

        /**
         * Ensure headers are sent with verbose logging
         */
        private void ensureHeadersSentWithLogging() {
            if (!headersSent) {
                if (ENABLE_VERBOSE_LOGGING)
                    logger.debug("[" + callId + "] Sending initial headers");
                serverCall.sendHeaders(new Metadata());
                headersSent = true;
            }
        }

        /**
         * Forward message to original client
         */
        private void forwardMessageToClient(InputStream message) {
            serverCall.sendMessage(message);
            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + MESSAGE_PREFIX + messageCount + " forwarded to server call");
        }

        /**
         * Complete standard message processing
         */
        private void completeStandardProcessing(InputStream message) {
            completeUnaryProcessing(message);
        }

        /**
         * Apply adaptive flow control
         */
        private void applyFlowControl() {
            if (flowControlManager != null)
                flowControlManager.applyFlowControl(serverCall, callId, isStreaming);
            else
                serverCall.request(1);
        }

        /**
         * Handle errors during message forwarding
         */
        private void handleMessageForwardingError(InputStream message, Exception e) throws StatusRuntimeException {
            logger.error("[" + callId + "] Error forwarding message to server call: " + e.getMessage());

            cleanupMessageOnError(message);

            if (flowControlManager != null) {
                flowControlManager.completeProcessingMessage(callId);
            }

            throw new StatusRuntimeException(Status.INTERNAL
                .withDescription("Error forwarding message to server call: " + e.getMessage())
                .withCause(e));
        }

        /**
         * Clean up message resources on error
         */
        private void cleanupMessageOnError(InputStream message) {
            try {
                message.close();
            } catch (IOException ioe) {
                // Ignore close exception
            }
            activeStreams.remove(message);
        }

        /**
         * Called when the call to the target service is closed.
         * Closes the call to the original client with the same status and trailers.
         */
        @Override
        public void onClose(Status status, Metadata trailers) {
            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] AdaptiveClientCallListener closing with status: " + status);
            try {
                // Ensure headers are sent before closing
                if (!headersSent) {
                    if (ENABLE_VERBOSE_LOGGING)
                        logger.debug("[" + callId + "] Sending headers before close");
                    serverCall.sendHeaders(new Metadata());
                    headersSent = true;
                }

                // Close the call to the original client
                if (status.isOk()) {
                    if (ENABLE_VERBOSE_LOGGING) {
                        logger.debug("[" + callId + "] Call completed successfully, received " + messageCount + MESSAGES_SUFFIX);
                    }
                } else {
                    logger.warn("[" + callId + "] Call failed with status: " + status.getCode() +
                            ", description: " + status.getDescription() +
                            ", received " + messageCount + MESSAGES_SUFFIX);
                }

                serverCall.close(status, trailers);
                if (ENABLE_VERBOSE_LOGGING)
                    logger.debug("[" + callId + "] Server call closed");

                // Clean up resources
                cleanupStreams();

                // Mark call complete and clean up resources
                if (timeoutManager != null)
                    timeoutManager.completeCall(fullMethodName, callId);
                if (flowControlManager != null)
                    flowControlManager.cleanupFlowControl(callId);

                // Update active call count
                activeCalls.decrementAndGet();
            } catch (Exception e) {
                logger.error("[" + callId + "] Error closing server call: " + e.getMessage());

                // Ensure count is decremented
                activeCalls.decrementAndGet();

                throw e;
            }
        }

        /**
         * Called when the client call is ready to send more messages.
         */
        @Override
        public void onReady() {
            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] AdaptiveClientCallListener ready");
        }

        /**
         * Clean up resources
         */
        private void cleanupStreams() {
            for (InputStream stream : activeStreams) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // Ignore close exception
                }
            }
            activeStreams.clear();
        }
    }

    /**
     * Adaptive server call listener, handles requests received from the original client
     */
    private class AdaptiveServerCallListener extends ServerCall.Listener<InputStream> {
        private final ClientCall<InputStream, InputStream> clientCall;
        private final ServerCall<InputStream, InputStream> serverCall;
        private final String callId;
        private boolean isStreaming;
        private int messageCount = 0;
        private boolean isActive = true;

        // Track resource usage
        private final Set<InputStream> activeStreams = Collections.newSetFromMap(new ConcurrentHashMap<>());

        // Buffer and synchronization
        private final List<byte[]> bufferedMessages = new ArrayList<>();
        private final Object bufferLock = new Object();
        private final AtomicBoolean halfCloseSent = new AtomicBoolean(false);
        private final AtomicInteger processedMessageCount = new AtomicInteger(0);
        private long streamHalfCloseTimeoutMs = 500; // Default half-close delay

        // Performance monitoring
        private final long startTime = System.currentTimeMillis();
        private long lastMessageTime = startTime;

        /**
         * Create a new adaptive server call listener
         *
         * @param clientCall Client call for forwarding requests to the target service
         * @param callId Call ID for log tracking
         * @param initialIsStreaming Initial streaming detection state
         */
        public AdaptiveServerCallListener(ClientCall<InputStream, InputStream> clientCall,
                                          ServerCall<InputStream, InputStream> serverCall,
                                          String callId, boolean initialIsStreaming) {
            this.clientCall = clientCall;
            this.serverCall = serverCall;
            this.callId = callId;
            this.isStreaming = initialIsStreaming;

            // Update active streaming call count
            if (initialIsStreaming) {
                activeClientStreams.incrementAndGet();
                // Streaming calls use longer delay
            }

            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("[" + callId + "] Created new AdaptiveServerCallListener (initial streaming: " + initialIsStreaming + ")");
            }
        }


        /**
         * Called when a message is received from the original client.
         * Forwards the message to the target service and updates flow control.
         */
        @Override
        public void onMessage(InputStream message) {
            totalMessagesReceived.incrementAndGet();

            if (!handleInactiveListener(message)) {
                return;
            }

            prepareMessageProcessing(message);

            if (tryFastPathProcessing(message)) {
                return;
            }

            processMessageStandard(message);
        }

        /**
         * Handle inactive listener scenario
         */
        private boolean handleInactiveListener(InputStream message) {
            if (!isActive) {
                logger.warn("[" + callId + "] Received message after listener was marked inactive");
                try {
                    message.close();
                } catch (IOException e) {
                    logger.warn("[" + callId + "] Error closing unused message stream: " + e.getMessage());
                }
                return false;
            }
            return true;
        }

        /**
         * Prepare for message processing - update counters and timing
         */
        private void prepareMessageProcessing(InputStream message) {
            activeStreams.add(message);
            messageCount++;
            long now = System.currentTimeMillis();
            long timeSinceLastMessage = now - lastMessageTime;
            lastMessageTime = now;

            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("[" + callId + "] AdaptiveServerCallListener received message #" + messageCount +
                        " after " + timeSinceLastMessage + "ms");
            }
        }

        /**
         * Try fast path processing for Unary requests
         */
        private boolean tryFastPathProcessing(InputStream message) {
            if (!isStreaming && messageCount == 1) {
                try {
                    clientCall.sendMessage(message);
                    activeStreams.remove(message);
                    recordMessageForAdaptiveMechanisms();
                    serverCall.request(1);
                    return true;
                } catch (Exception e) {
                    logger.error("[" + callId + "] Error in fast path, falling back: " + e.getMessage());
                }
            }
            return false;
        }

        /**
         * Record message processing for adaptive mechanisms
         */
        private void recordMessageForAdaptiveMechanisms() {
            if (timeoutManager != null) {
                timeoutManager.recordMessage(callId);
            }
            if (flowControlManager != null) {
                flowControlManager.startProcessingMessage(callId);
                flowControlManager.completeProcessingMessage(callId);
            }
        }

        /**
         * Process message using standard buffered approach
         */
        private void processMessageStandard(InputStream message) {
            try {
                byte[] messageData = ByteStreams.toByteArray(message);
                addMessageToBuffer(messageData);
                updateStreamingDetection();
                processBufferedMessages();
                activeStreams.remove(message);
                requestMoreMessages();
            } catch (Exception e) {
                handleMessageProcessingError(message, e);
            }
        }

        /**
         * Add message to buffer with logging
         */
        private void addMessageToBuffer(byte[] messageData) {
            synchronized (bufferLock) {
                bufferedMessages.add(messageData);
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("[" + callId + MESSAGE_PREFIX + messageCount + " added to buffer, buffer size: " +
                            bufferedMessages.size());
                }
            }
        }

        /**
         * Update streaming detection based on message count
         */
        private void updateStreamingDetection() {
            if (messageCount > 1) {
                isStreaming = true;
                if (messageCount == 2) {
                    activeClientStreams.incrementAndGet();
                    streamHalfCloseTimeoutMs = Math.max(streamHalfCloseTimeoutMs, 500);
                }
            }
        }

        /**
         * Request more messages based on current message count
         */
        private void requestMoreMessages() {
            if (messageCount < 10) {
                serverCall.request(1);
            } else {
                serverCall.request(3);
            }
        }

        /**
         * Handle errors during message processing
         */
        private void handleMessageProcessingError(InputStream message, Exception e) {
            logger.error("[" + callId + "] Error processing message: " + e.getMessage());

            cleanupMessageStream(message);
            closeCallsWithError(e);
            cleanupResources();
            isActive = false;
            completeCallTracking();
        }

        /**
         * Clean up message stream safely
         */
        private void cleanupMessageStream(InputStream message) {
            try {
                message.close();
            } catch (IOException ioe) {
                // Ignore close exception
            }
            activeStreams.remove(message);
        }

        /**
         * Close calls with error status
         */
        private void closeCallsWithError(Exception e) {
            Status status = Status.INTERNAL.withDescription("Error processing message: " + e.getMessage());
            serverCall.close(status, new Metadata());
            clientCall.cancel("Error processing message", null);
        }

        /**
         * Complete call tracking for adaptive mechanisms
         */
        private void completeCallTracking() {
            if (timeoutManager != null) {
                timeoutManager.completeCall(fullMethodName, callId);
            }
            if (flowControlManager != null) {
                flowControlManager.cleanupFlowControl(callId);
            }
        }

        /**
         * Process buffered messages
         */
        private void processBufferedMessages() {
            if (!isActive) return;

            synchronized (bufferLock) {
                processAllBufferedMessages();
                tryExecuteDelayedHalfClose();
            }
        }

        private void processAllBufferedMessages() {
            while (!bufferedMessages.isEmpty() && isActive) {
                byte[] messageData = bufferedMessages.getFirst();
                
                if (!processSingleBufferedMessage(messageData)) {
                    return; // Stop processing on error
                }
                
                completeMessageProcessing();
            }
        }

        private boolean processSingleBufferedMessage(byte[] messageData) {
            try {
                forwardBufferedMessage(messageData);
                logMessageProcessing(messageData);
                updateMessageTrackingCounters();
                return true;
            } catch (Exception e) {
                logger.error("[" + callId + "] Error forwarding buffered message: " + e.getMessage());
                return false;
            }
        }

        private void forwardBufferedMessage(byte[] messageData) {
            InputStream messageStream = new ByteArrayInputStream(messageData);
            clientCall.sendMessage(messageStream);
        }

        private void logMessageProcessing(byte[] messageData) {
            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("[" + callId + "] Processed message #" + (processedMessageCount.get() + 1) +
                        " of " + messageCount + ", size: " + messageData.length + " bytes");
            }
        }

        private void updateMessageTrackingCounters() {
            processedMessageCount.incrementAndGet();
            
            if (timeoutManager != null) {
                timeoutManager.recordMessage(callId);
            }
            if (flowControlManager != null) {
                flowControlManager.startProcessingMessage(callId);
                flowControlManager.completeProcessingMessage(callId);
            }
        }

        private void completeMessageProcessing() {
            bufferedMessages.removeFirst();
            clientCall.request(1);
        }

        private void tryExecuteDelayedHalfClose() {
            if (shouldExecuteDelayedHalfClose()) {
                executeDelayedHalfClose();
            }
        }

        private boolean shouldExecuteDelayedHalfClose() {
            return bufferedMessages.isEmpty() && halfCloseSent.get() && isActive;
        }

        private void executeDelayedHalfClose() {
            try {
                logDelayedHalfCloseStart();
                clientCall.halfClose();
                logDelayedHalfCloseSuccess();
            } catch (IllegalStateException e) {
                handleDelayedHalfCloseStateException(e);
            } catch (Exception e) {
                logger.error("[" + callId + "] Error during delayed half-close: " + e.getMessage());
            }
        }

        private void logDelayedHalfCloseStart() {
            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("[" + callId + "] All buffered messages processed, sending half-close");
            }
        }

        private void logDelayedHalfCloseSuccess() {
            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("[" + callId + "] Half-close sent to upstream service");
            }
        }

        private void handleDelayedHalfCloseStateException(IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains(ALREADY_HALF_CLOSED_MSG)) {
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("[" + callId + CALL_ALREADY_HALF_CLOSED_LOG);
                }
            } else {
                logger.error("[" + callId + "] Error during delayed half-close: " + e.getMessage());
            }
        }

        /**
         * Called when the original client has finished sending messages (half-close).
         * Performs half-close on the call to the target service.
         */
        @Override
        public void onHalfClose() {
            if (!isActive) return;

            try {
                logHalfCloseStart();
                
                if (!trySetHalfCloseSent()) {
                    return;
                }

                if (isUnaryCall()) {
                    handleUnaryHalfClose();
                    return;
                }

                handleStreamingHalfClose();
            } catch (Exception e) {
                handleHalfCloseError(e);
            }
        }

        private void logHalfCloseStart() {
            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("[" + callId + "] AdaptiveServerCallListener half-closing after receiving " +
                        messageCount + MESSAGES_SUFFIX);
            }
        }

        private boolean trySetHalfCloseSent() {
            if (!halfCloseSent.compareAndSet(false, true)) {
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("[" + callId + "] Half-close already sent, ignoring duplicate half-close");
                }
                return false;
            }
            return true;
        }

        private boolean isUnaryCall() {
            return !isStreaming && messageCount <= 1;
        }

        private void handleUnaryHalfClose() {
            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("[" + callId + "] Unary call detected, using no delay for half-close");
            }

            processBufferedMessages();
            cleanupResources();
            tryExecuteHalfClose("Client call half-closed successfully (no delay for Unary)");
        }

        private void handleStreamingHalfClose() {
            long delayMs = calculateDelayMs();
            applyHalfCloseDelay(delayMs);
            
            synchronized (bufferLock) {
                processBufferedMessages();
                executeBufferedHalfClose();
            }
        }

        private long calculateDelayMs() {
            long delayMs = streamHalfCloseTimeoutMs;
            if (messageCount > MESSAGE_COUNT_DELAY_THRESHOLD) {
                delayMs = Math.min(MAX_DELAY_MS, messageCount * DELAY_MULTIPLIER);
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("[" + callId + "] Adjusted half-close delay to " + delayMs +
                            "ms based on message count: " + messageCount);
                }
            }
            return delayMs;
        }

        private void applyHalfCloseDelay(long delayMs) {
            try {
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("[" + callId + "] Adding delay of " + delayMs +
                            "ms before processing half-close");
                }
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("[" + callId + "] Interrupted during half-close delay");
            }
        }

        private void executeBufferedHalfClose() {
            if (bufferedMessages.isEmpty() && isActive) {
                tryExecuteHalfClose("Half-close sent to upstream service");
            } else {
                logDelayedHalfClose();
            }
        }

        private void tryExecuteHalfClose(String successMessage) {
            if (!bufferedMessages.isEmpty() || !isActive) {
                return;
            }
            
            try {
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("[" + callId + "] Buffer is empty, sending half-close to upstream service");
                }
                clientCall.halfClose();
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("[" + callId + "] " + successMessage);
                }
            } catch (IllegalStateException e) {
                handleHalfCloseStateException(e);
            }
        }

        private void handleHalfCloseStateException(IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains(ALREADY_HALF_CLOSED_MSG)) {
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("[" + callId + CALL_ALREADY_HALF_CLOSED_LOG);
                }
            } else {
                throw e;
            }
        }

        private void logDelayedHalfClose() {
            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("[" + callId + "] Delaying half-close until buffer is processed, " +
                        bufferedMessages.size() + " messages remaining");
            }
        }

        private void handleHalfCloseError(Exception e) {
            logger.error("[" + callId + "] Error during half-close: " + e.getMessage());

            logStatusException(e);
            forceCloseConnections(e);
            cleanupAfterError();
        }

        private void logStatusException(Exception e) {
            if (e instanceof StatusException se) {
                logger.error("[" + callId + "] Status exception during half-close: " +
                        se.getStatus().getCode() + " - " + se.getStatus().getDescription());
            }
        }

        private void forceCloseConnections(Exception e) {
            Status status = Status.INTERNAL.withDescription("Error during half-close: " + e.getMessage());
            
            try {
                serverCall.close(status, new Metadata());
            } catch (Exception ce) {
                logger.error("[" + callId + "] Error closing server call: " + ce.getMessage());
            }

            try {
                clientCall.cancel("Error during half-close", null);
            } catch (Exception ce) {
                logger.error("[" + callId + "] Error canceling client call: " + ce.getMessage());
            }
        }

        private void cleanupAfterError() {
            cleanupResources();
            isActive = false;

            if (timeoutManager != null) {
                timeoutManager.completeCall(fullMethodName, callId);
            }
            if (flowControlManager != null) {
                flowControlManager.cleanupFlowControl(callId);
            }
        }

        /**
         * Called when the original client cancels the call.
         * Cancels the call to the target service.
         */
        @Override
        public void onCancel() {
            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("[" + callId + "] AdaptiveServerCallListener cancelled after receiving " +
                        messageCount + MESSAGES_SUFFIX);
            }

            try {
                // Cancel the call to the target service
                clientCall.cancel("Client cancelled the call", null);
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("[" + callId + "] Client call cancelled");
                }
            } catch (Exception e) {
                logger.error("[" + callId + "] Error cancelling client call: " + e.getMessage());
            } finally {
                // Update active stream count
                if (isStreaming) {
                    activeClientStreams.decrementAndGet();
                }

                // Clean up resources
                cleanupResources();
                isActive = false;

                // Mark call complete and clean up resources
                if (timeoutManager != null)
                    timeoutManager.completeCall(fullMethodName, callId);
                if (flowControlManager != null)
                    flowControlManager.cleanupFlowControl(callId);

                // Update active call count
                activeCalls.decrementAndGet();
            }
        }

        /**
         * Called when the call from the original client is completed.
         */
        @Override
        public void onComplete() {
            long totalTime = System.currentTimeMillis() - startTime;
            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("[" + callId + "] AdaptiveServerCallListener completed after receiving " +
                        messageCount + " messages, total time: " + totalTime + "ms");
            }

            try {
                // Update active stream count
                if (isStreaming) {
                    activeClientStreams.decrementAndGet();
                }

                // Clean up resources
                cleanupResources();
                isActive = false;

                // Mark call complete and clean up resources
                if (timeoutManager != null)
                    timeoutManager.completeCall(fullMethodName, callId);
                if (flowControlManager != null)
                    flowControlManager.cleanupFlowControl(callId);

                // Update active call count
                activeCalls.decrementAndGet();
            } catch (Exception e) {
                logger.error("[" + callId + "] Error during completion cleanup: " + e.getMessage());
            }
        }

        /**
         * Called when the server call is ready to receive more messages.
         */
        @Override
        public void onReady() {
            if (!isActive) return;

            if (ENABLE_VERBOSE_LOGGING)
                logger.debug("[" + callId + "] AdaptiveServerCallListener ready");

            // For streaming RPC, ensure the stream remains active
            if (isStreaming && flowControlManager != null) {
                try {
                    flowControlManager.applyFlowControl(clientCall, callId, true);
                } catch (Exception e) {
                    logger.warn("[" + callId + "] Error applying flow control on ready: " + e.getMessage());
                }
            }
        }

        /**
         * Clean up all active resources
         */
        private void cleanupResources() {
            // Close all active streams
            if (!activeStreams.isEmpty()) {
                if (ENABLE_VERBOSE_LOGGING)
                    logger.debug("[" + callId + "] Cleaning up " + activeStreams.size() + " active streams");

                for (InputStream stream : activeStreams) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        // Ignore close exception
                        if (ENABLE_VERBOSE_LOGGING)
                            logger.debug("[" + callId + "] Error closing stream: " + e.getMessage());
                    }
                }
                activeStreams.clear();
            }

            // Clear buffer
            synchronized (bufferLock) {
                bufferedMessages.clear();
            }

            // Reset other counters and state
            isActive = false;
        }
    }
}