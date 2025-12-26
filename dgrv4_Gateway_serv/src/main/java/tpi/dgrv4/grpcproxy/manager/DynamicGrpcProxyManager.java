package tpi.dgrv4.grpcproxy.manager;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.grpc.HandlerRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import jakarta.annotation.PostConstruct;
import tpi.dgrv4.common.utils.CheckmarxCommUtils;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;
import tpi.dgrv4.entity.repository.DgrGrpcProxyMapDao;
import tpi.dgrv4.escape.CheckmarxUtils;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.grpcproxy.config.GrpcProxyProperties;
import tpi.dgrv4.grpcproxy.event.ProxyConfigChangedEvent;
import tpi.dgrv4.grpcproxy.handler.HostBasedHandlerRegistry;
import tpi.dgrv4.grpcproxy.security.TlsCertificateManager;

/**
 * Dynamic gRPC proxy manager, responsible for managing proxy channels and their configurations.
 * Supports dynamically adding, updating, and deleting proxy mappings.
 */
@Component
public class DynamicGrpcProxyManager {

    
    @Value("${grpc.proxy.enabled:false}")
    private boolean grpcProxyEnabled;

    private final DgrGrpcProxyMapDao dgrGrpcProxyMapDao;
    private final GrpcProxyProperties proxyProperties;
    private final TlsCertificateManager tlsCertificateManager;
    private final Map<String, ManagedChannel> proxyChannels = new ConcurrentHashMap<>();
    private final Map<String, DgrGrpcProxyMap> activeProxyMappings = new ConcurrentHashMap<>();
    private HostBasedHandlerRegistry handlerRegistry;

    // Spring event publisher
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor that injects required dependencies
     */
    @Autowired
    public DynamicGrpcProxyManager(DgrGrpcProxyMapDao dgrGrpcProxyMapDao,
                                   ApplicationEventPublisher eventPublisher,
                                   GrpcProxyProperties proxyProperties,
                                   TlsCertificateManager tlsCertificateManager) {
        this.dgrGrpcProxyMapDao = dgrGrpcProxyMapDao;
        this.eventPublisher = eventPublisher;
        this.proxyProperties = proxyProperties;
        this.tlsCertificateManager = tlsCertificateManager;
    }

    /**
     * Initialization method, executed when Spring container starts
     */
    @PostConstruct
    public void initialize() {
    	if(!grpcProxyEnabled) {
    		TPILogger.tl.info("grpc proxy enabled is false");
    		return;
    	}else {
    		TPILogger.tl.info("grpc proxy enabled is true");
    	}
    	
        TPILogger.tl.debug("Initializing dynamic gRPC proxy manager");
        try {
            refreshProxyMappings();
        } catch (Exception e) {
        	TPILogger.tl.error("Error initializing dynamic proxy manager: " + StackTraceUtil.logStackTrace(e));
            // Create an empty handler registry to ensure the application can start
            handlerRegistry = new HostBasedHandlerRegistry(new HashMap<>());
        }
    }

    /**
     * Refresh all proxy mappings from the database
     * This method is executed on application startup or when a manual refresh is triggered
     */
    public synchronized void refreshProxyMappings() {
        TPILogger.tl.debug("Refreshing proxy mappings from database");

        try {
            // Get all enabled proxy mappings
            List<DgrGrpcProxyMap> enabledMappings = dgrGrpcProxyMapDao.findByEnable("Y");
            TPILogger.tl.debug("Found " + enabledMappings.size() + " enabled proxy mappings");

            // Remove channels that are no longer enabled
            List<String> newProxyHostnames = enabledMappings.stream()
                    .map(DgrGrpcProxyMap::getProxyHostName)
                    .toList();

            // Find the hostnames to remove
            List<String> hostnamesForRemoval = proxyChannels.keySet().stream()
                    .filter(hostname -> !newProxyHostnames.contains(hostname))
                    .toList();

            // Remove channels
            for (String hostname : hostnamesForRemoval) {
                removeProxyChannel(hostname, false); // Don't send individual events
            }

            // Add or update channels
            for (DgrGrpcProxyMap mapping : enabledMappings) {
                updateProxyChannel(mapping, false); // Don't send individual events
            }

            // Create a new handler registry
            handlerRegistry = new HostBasedHandlerRegistry(proxyChannels);

            TPILogger.tl.debug("Proxy mapping refresh completed. Active channels: " + proxyChannels.size() +
                    ", hostnames: " + String.join(", ", proxyChannels.keySet()));

            // Publish refresh event to restart the server once
            eventPublisher.publishEvent(ProxyConfigChangedEvent.refreshEvent());
        } catch (Exception e) {
        	TPILogger.tl.error("Error refreshing proxy mappings: " + StackTraceUtil.logStackTrace(e));
            throw e;
        }
    }

    /**
     * Add a new proxy mapping
     *
     * @param mapping The proxy mapping to add
     */
    public synchronized void addProxyMapping(DgrGrpcProxyMap mapping) {
        TPILogger.tl.debug("Adding new proxy mapping: " + mapping.getProxyHostName());
        if ("Y".equals(mapping.getEnable())) {
            updateProxyChannel(mapping, true); // Send event
        }
    }

    /**
     * Update an existing proxy mapping
     *
     * @param mapping The proxy mapping to update
     */
    public synchronized void updateProxyMapping(DgrGrpcProxyMap mapping) {
        TPILogger.tl.debug("Updating proxy mapping: " + mapping.getProxyHostName() + ", enable status: " + mapping.getEnable());
        String proxyHostname = mapping.getProxyHostName();

        if ("Y".equals(mapping.getEnable())) {
            // If enabled, update or add the channel
            updateProxyChannel(mapping, true); // Send event
        } else if ("N".equals(mapping.getEnable()) && proxyChannels.containsKey(proxyHostname)) {
            // If disabled and the channel exists, remove the channel
            removeProxyChannel(proxyHostname, true); // Send event
        }
    }

    /**
     * Batch update the enable status of proxy mappings
     *
     * @param mappings List of proxy mappings to update
     * @param enable Whether to enable (true) or disable (false)
     * @return Number of successfully processed items
     */
    public synchronized int batchUpdateProxyStatus(List<DgrGrpcProxyMap> mappings, boolean enable) {
        if (mappings == null || mappings.isEmpty()) {
            return 0;
        }

        TPILogger.tl.debug("Batch " + (enable ? "enabling" : "disabling") + " proxy services, count: " + mappings.size());

        int successCount = 0;
        boolean needRestart = false;

        // Process each mapping
        for (DgrGrpcProxyMap mapping : mappings) {
            try {
                String proxyHostname = mapping.getProxyHostName();

                if (enable) {
                    // If enabling, update or add the channel
                    updateProxyChannel(mapping, false); // Don't send individual events
                    needRestart = true;
                } else if (!enable && proxyChannels.containsKey(proxyHostname)) {
                    // If disabling and the channel exists, remove the channel
                    removeProxyChannel(proxyHostname, false); // Don't send individual events
                    needRestart = true;
                }

                successCount++;
            } catch (Exception e) {
            	TPILogger.tl.error("Error processing proxy mapping: " + mapping.getProxyHostName()+"\n"+StackTraceUtil.logStackTrace(e));
                
                // Continue processing other mappings
            }
        }

        // If there are any changes, restart is needed
        if (needRestart && successCount > 0) {
            // Update the handler registry
            handlerRegistry = new HostBasedHandlerRegistry(proxyChannels);

            // Publish refresh event to restart the server once
            eventPublisher.publishEvent(ProxyConfigChangedEvent.refreshEvent());
        }

        TPILogger.tl.debug("Batch " + (enable ? "enabling" : "disabling") + " proxy services completed, success: " +
                successCount + ", failure: " + (mappings.size() - successCount));

        return successCount;
    }

    /**
     * Delete a proxy mapping
     *
     * @param mapping The proxy mapping to delete
     */
    public synchronized void deleteProxyMapping(DgrGrpcProxyMap mapping) {
        String proxyHostname = mapping.getProxyHostName();
        TPILogger.tl.debug("Deleting proxy mapping: " + proxyHostname);
        if (proxyChannels.containsKey(proxyHostname)) {
            removeProxyChannel(proxyHostname, true); // Send event
        }
    }

    /**
     * Delete a proxy mapping by hostname
     *
     * @param proxyHostname The proxy hostname to delete
     */
    public synchronized void deleteProxyMappingByHostname(String proxyHostname) {
        TPILogger.tl.debug("Deleting proxy mapping by hostname: " + proxyHostname);
        if (proxyChannels.containsKey(proxyHostname)) {
            removeProxyChannel(proxyHostname, true); // Send event
        }
    }

    /**
     * Detect if the target service supports TLS
     *
     * @param hostname Target hostname
     * @param port Target port
     * @return true if the target service supports TLS, false otherwise
     */
    private boolean detectTlsSupport(String hostname, int port) {
        TPILogger.tl.debug("Detecting if target service supports TLS: " + hostname + ":" + port);

        try {
            // Try to connect to the target service using SSL
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

            // Use try-with-resources to ensure proper socket closure
            try (Socket tempSocket = new Socket()) {
                // Set connection timeout
            	//checkmarx, SSRF
                CheckmarxCommUtils.sanitizeForCheckmarxConn(tempSocket, hostname, port, 5000);

                // Start SSL handshake
                try (SSLSocket socket = (SSLSocket) factory.createSocket(
                        tempSocket, hostname, port, true)) {

                    // Configure SSL parameters to avoid server sending certificate chains
                    SSLParameters params = socket.getSSLParameters();
                    params.setEndpointIdentificationAlgorithm("HTTPS");
                    socket.setSSLParameters(params);

                    // If auto-trusting all certificates, set handshake timeout shorter
                    socket.setSoTimeout(5000);

                    // Perform SSL handshake
                    socket.startHandshake();

                    TPILogger.tl.debug("Target service supports TLS: " + hostname + ":" + port);
                    return true;
                }
            }
        } catch (SSLHandshakeException e) {
            // If it's a certificate issue, the server might support TLS, but we don't trust its certificate
            TPILogger.tl.debug("Target service might support TLS, but certificate validation failed (might need to trust certificate): " + hostname + ":" + port);
            return true;
        } catch (Exception e) {
            // Other exceptions, might not support TLS
            TPILogger.tl.debug("Target service might not support TLS: " + hostname + ":" + port + ", error: " + e.getMessage());
            return false;
        }
    }

    private static final String PROXY_MAPPING_LOG_PREFIX = "Proxy mapping ";

    /**
     * Update or add a proxy channel
     *
     * @param mapping Proxy mapping configuration
     * @param publishEvent Whether to publish an event
     */
    private synchronized void updateProxyChannel(DgrGrpcProxyMap mapping, boolean publishEvent) {
        String proxyHostname = mapping.getProxyHostName();
        boolean isNew = !proxyChannels.containsKey(proxyHostname);

        if (isConfigurationUnchanged(mapping, proxyHostname)) {
            return;
        }

        closeExistingChannel(proxyHostname);

        String secureMode = getSecureMode(mapping);
        boolean useTls = determineTlsUsage(mapping, secureMode, proxyHostname);
        
        ManagedChannel newChannel = createChannel(mapping, useTls, proxyHostname);
        
        registerChannel(mapping, proxyHostname, newChannel, secureMode, useTls);
        
        publishEventIfRequired(publishEvent, isNew, proxyHostname);
    }

    private boolean isConfigurationUnchanged(DgrGrpcProxyMap mapping, String proxyHostname) {
        DgrGrpcProxyMap existingMapping = activeProxyMappings.get(proxyHostname);
        if (existingMapping == null) {
            return false;
        }
        
        boolean unchanged = existingMapping.getTargetHostName().equals(mapping.getTargetHostName()) &&
                existingMapping.getTargetPort() == mapping.getTargetPort() &&
                existingMapping.getConnectTimeoutMs() == mapping.getConnectTimeoutMs() &&
                Objects.equals(existingMapping.getSecureMode(), mapping.getSecureMode()) &&
                Objects.equals(existingMapping.getTrustedCertsContent(), mapping.getTrustedCertsContent()) &&
                Objects.equals(existingMapping.isAutoTrustUpstreamCerts(), mapping.isAutoTrustUpstreamCerts());
                
        if (unchanged) {
            TPILogger.tl.debug(PROXY_MAPPING_LOG_PREFIX + proxyHostname + " configuration unchanged, skipping update");
        }
        return unchanged;
    }

    private void closeExistingChannel(String proxyHostname) {
        if (!proxyChannels.containsKey(proxyHostname)) {
            return;
        }
        
        ManagedChannel oldChannel = proxyChannels.remove(proxyHostname);
        try {
            oldChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            TPILogger.tl.debug("Closed old channel for " + proxyHostname);
        } catch (InterruptedException e) {
        	TPILogger.tl.error("Error closing channel for " + proxyHostname +"\n"+StackTraceUtil.logStackTrace(e));
            Thread.currentThread().interrupt();
        }
    }

    private String getSecureMode(DgrGrpcProxyMap mapping) {
        String secureMode = mapping.getSecureMode();
        return (secureMode == null || secureMode.isEmpty()) ? "AUTO" : secureMode;
    }

    private boolean determineTlsUsage(DgrGrpcProxyMap mapping, String secureMode, String proxyHostname) {
        switch (secureMode) {
            case "SECURE":
                TPILogger.tl.debug(PROXY_MAPPING_LOG_PREFIX + proxyHostname + " configured to force use of TLS");
                return true;
            case "PLAINTEXT":
                TPILogger.tl.debug(PROXY_MAPPING_LOG_PREFIX + proxyHostname + " configured to not use TLS (plaintext)");
                return false;
            case "AUTO":
            default:
                boolean useTls = detectTlsSupport(mapping.getTargetHostName(), mapping.getTargetPort());
                TPILogger.tl.debug(PROXY_MAPPING_LOG_PREFIX + proxyHostname + " auto-detected TLS result: " +
                        (useTls ? "using TLS" : "using plaintext"));
                return useTls;
        }
    }

    private ManagedChannel createChannel(DgrGrpcProxyMap mapping, boolean useTls, String proxyHostname) {
    	String prefix = useTls ? "TLS channel:" : "plaintext channel:";
    	try {
            NettyChannelBuilder channelBuilder = NettyChannelBuilder
                    .forAddress(mapping.getTargetHostName(), mapping.getTargetPort())
                    .withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, mapping.getConnectTimeoutMs());
            
            if(useTls) {
	            SslContext sslContext = createSslContext(mapping, proxyHostname);
	            channelBuilder.sslContext(sslContext);
            }else {
            	channelBuilder.usePlaintext();
            }
            
            ManagedChannel channel = applyChannelSettings((ManagedChannelBuilder<?>) channelBuilder).build();
            if(useTls) {
            	TPILogger.tl.debug("Created TLS channel for " + proxyHostname + " with enhanced HTTP/2 settings");
            }else {
            	TPILogger.tl.debug("Created plaintext channel for " + proxyHostname + " with enhanced HTTP/2 settings");
            }
            
            return channel;
        } catch (Exception e) {
        	TPILogger.tl.error(prefix + StackTraceUtil.logStackTrace(e));
            return createPlaintextChannelFallback(mapping);
        }
        
    }

    private SslContext createSslContext(DgrGrpcProxyMap mapping, String proxyHostname) throws Exception {
        if ("Y".equals(mapping.isAutoTrustUpstreamCerts())) {
            TPILogger.tl.debug("Creating TLS channel with auto-trust all certificates for " + proxyHostname);
            return tlsCertificateManager.createInsecureClientSslContext();
        } else if (mapping.getTrustedCertsContent() != null && !mapping.getTrustedCertsContent().isEmpty()) {
            TPILogger.tl.debug("Creating TLS channel with custom trusted certificates for " + proxyHostname);
            return tlsCertificateManager.createClientSslContext(mapping.getTrustedCertsContent());
        } else {
            TPILogger.tl.debug("No trusted certificates provided, creating TLS channel with auto-trust all certificates for " + proxyHostname);
            return tlsCertificateManager.createInsecureClientSslContext();
        }
    }

    private ManagedChannel createPlaintextChannelFallback(DgrGrpcProxyMap mapping) {
        return ManagedChannelBuilder
                .forAddress(mapping.getTargetHostName(), mapping.getTargetPort())
                .usePlaintext()
                .keepAliveTime(60, TimeUnit.SECONDS)
                .keepAliveTimeout(20, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .maxInboundMessageSize(20 * 1024 * 1024)
                .maxRetryAttempts(0)
                .idleTimeout(5, TimeUnit.MINUTES)
                .build();
    }

    private <T extends ManagedChannelBuilder<T>> T applyChannelSettings(ManagedChannelBuilder<?> channelBuilder) {
        return (T) channelBuilder
                .keepAliveTime(60, TimeUnit.SECONDS)
                .keepAliveTimeout(20, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .maxInboundMessageSize(20 * 1024 * 1024)
                .maxRetryAttempts(0)
                .idleTimeout(5, TimeUnit.MINUTES);
    }

    private void registerChannel(DgrGrpcProxyMap mapping, String proxyHostname, ManagedChannel newChannel, String secureMode, boolean useTls) {
        proxyChannels.put(proxyHostname, newChannel);
        activeProxyMappings.put(proxyHostname, mapping);

        String securityInfo = buildSecurityInfo(mapping, useTls);
        logChannelCreation(mapping, proxyHostname, secureMode, securityInfo);

        updateHandlerRegistry();
    }

    private String buildSecurityInfo(DgrGrpcProxyMap mapping, boolean useTls) {
        if (!useTls) {
            return " (plaintext)";
        }
        
        String tlsInfo;
        if ("Y".equals(mapping.isAutoTrustUpstreamCerts())) {
            tlsInfo = ", auto-trusting all certificates";
        } else if (mapping.getTrustedCertsContent() != null) {
            tlsInfo = ", using custom trusted certificates";
        } else {
            tlsInfo = ", using system certificates";
        }
        return " (using TLS" + tlsInfo + ")";
    }

    private void logChannelCreation(DgrGrpcProxyMap mapping, String proxyHostname, String secureMode, String securityInfo) {
        TPILogger.tl.debug("Added new channel: " + proxyHostname + " -> " +
                mapping.getTargetHostName() + ":" + mapping.getTargetPort() +
                " (timeout settings: connect=" + mapping.getConnectTimeoutMs() +
                "ms, send=" + mapping.getSendTimeoutMs() +
                "ms, read=" + mapping.getReadTimeoutMs() +
                "ms, secure mode: " + secureMode + securityInfo + ")");
    }

    private void updateHandlerRegistry() {
        if (handlerRegistry != null) {
            handlerRegistry = new HostBasedHandlerRegistry(proxyChannels);
        }
    }

    private void publishEventIfRequired(boolean publishEvent, boolean isNew, String proxyHostname) {
        if (!publishEvent) {
            return;
        }
        ProxyConfigChangedEvent.ChangeType type = isNew ?
                ProxyConfigChangedEvent.ChangeType.ADDED :
                ProxyConfigChangedEvent.ChangeType.UPDATED;
        eventPublisher.publishEvent(new ProxyConfigChangedEvent(type, proxyHostname));
    }

    /**
     * Remove a proxy channel
     *
     * @param proxyHostname The proxy hostname to remove
     * @param publishEvent Whether to publish an event
     */
    private synchronized void removeProxyChannel(String proxyHostname, boolean publishEvent) {
        ManagedChannel channel = proxyChannels.remove(proxyHostname);
        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                TPILogger.tl.debug("Removed channel for hostname: " + proxyHostname);
            } catch (InterruptedException e) {
            	TPILogger.tl.error("Error closing channel for hostname: " + proxyHostname +"\n"+StackTraceUtil.logStackTrace(e));
                Thread.currentThread().interrupt();
            }

            activeProxyMappings.remove(proxyHostname);

            // Update the handler registry
            if (handlerRegistry != null) {
                handlerRegistry = new HostBasedHandlerRegistry(proxyChannels);
            }

            // Publish event to notify configuration has been removed
            if (publishEvent) {
                eventPublisher.publishEvent(
                        new ProxyConfigChangedEvent(ProxyConfigChangedEvent.ChangeType.REMOVED, proxyHostname));
            }
        }
    }

    /**
     * Get the current handler registry
     */
    public HandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
    }

    /**
     * Get the list of active proxy hostnames (for diagnostics)
     */
    public List<String> getActiveProxyHostnames() {
        return new ArrayList<>(proxyChannels.keySet());
    }

    /**
     * Get the channel configuration for a specific hostname (for diagnostics)
     */
    public DgrGrpcProxyMap getProxyMapping(String proxyHostname) {
        return activeProxyMappings.get(proxyHostname);
    }
}