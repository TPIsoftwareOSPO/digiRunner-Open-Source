package tpi.dgrv4.grpcproxy.server;

import io.grpc.*;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;
import tpi.dgrv4.entity.repository.DgrGrpcProxyMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.grpcproxy.config.GrpcProxyProperties;
import tpi.dgrv4.grpcproxy.event.ProxyConfigChangedEvent;
import tpi.dgrv4.grpcproxy.event.UpstreamHealthCheckEvent;
import tpi.dgrv4.grpcproxy.handler.HostBasedHandlerRegistry;
import tpi.dgrv4.grpcproxy.manager.DynamicGrpcProxyManager;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class GrpcProxyServer {
    private final TPILogger logger = TPILogger.tl;

    private final GrpcProxyProperties proxyProperties;
    private final DynamicGrpcProxyManager proxyManager;
    private final SslContext sslContext;
    private final DgrGrpcProxyMapDao dgrGrpcProxyMapDao;
    private final ApplicationEventPublisher eventPublisher;

    private final ScheduledExecutorService healthCheckExecutor = Executors.newScheduledThreadPool(2, 
    r -> {
        Thread thread = new Thread(r, "dgRv4_grcp_proxy_healthCheckExecutor-" + System.currentTimeMillis());
        thread.setDaemon(true);
        return thread;
    });
    private final Map<String, UpstreamServiceStatus> upstreamStatusMap = new ConcurrentHashMap<>();

    private final AtomicInteger totalCallsProcessed = new AtomicInteger(0);
    private final AtomicInteger failedCallsCount = new AtomicInteger(0);
    private final AtomicInteger tlsErrorsCount = new AtomicInteger(0);
    private final Map<String, AtomicInteger> errorsByHostname = new ConcurrentHashMap<>();

    private Server server;
    private final ReentrantLock serverLock = new ReentrantLock();
    private final int serverPort;
    private boolean healthCheckEnabled = true;

    @Autowired
    public GrpcProxyServer(GrpcProxyProperties proxyProperties,
                           DynamicGrpcProxyManager proxyManager,
                           DgrGrpcProxyMapDao dgrGrpcProxyMapDao,
                           ApplicationEventPublisher eventPublisher,
                           @Autowired(required = false) SslContext sslContext) {
        this.proxyProperties = proxyProperties;
        this.proxyManager = proxyManager;
        this.dgrGrpcProxyMapDao = dgrGrpcProxyMapDao;
        this.eventPublisher = eventPublisher;
        this.sslContext = sslContext;
        this.serverPort = proxyProperties.getServer().getPort();

        // 初始化日誌將在啟動時一起顯示，此處只記錄簡單信息
        logger.debug("Constructing gRPC proxy server for port: " + serverPort);
    }

    /**
     * Start the gRPC proxy server
     */
    @PostConstruct
    public void start() throws IOException {
        serverLock.lock();
        try {
            // 啟動配置信息將整合到最終的banner中

            ServerBuilder<?> serverBuilder;

            if (sslContext != null && proxyProperties.getTls().isEnabled()) {
                serverBuilder = NettyServerBuilder.forAddress(new InetSocketAddress("0.0.0.0", serverPort))
                        .intercept(createLoggingInterceptor())
                        .sslContext(sslContext)
                        .directExecutor()
                        .keepAliveTime(60, TimeUnit.SECONDS)
                        .keepAliveTimeout(20, TimeUnit.SECONDS)
                        .permitKeepAliveWithoutCalls(true)
                        .permitKeepAliveTime(30, TimeUnit.SECONDS)
                        .maxConnectionAge(300, TimeUnit.SECONDS)
                        .maxConnectionAgeGrace(60, TimeUnit.SECONDS)
                        .maxInboundMessageSize(20 * 1024 * 1024)
                        .maxInboundMetadataSize(16 * 1024);
            } else {
                serverBuilder = ServerBuilder.forPort(serverPort)
                        .intercept(createLoggingInterceptor())
                        .directExecutor()
                        .maxInboundMessageSize(20 * 1024 * 1024)
                        .maxInboundMetadataSize(16 * 1024);
            }

            var registry = proxyManager.getHandlerRegistry();

            if (registry instanceof HostBasedHandlerRegistry) {
                HostBasedHandlerRegistry hostRegistry = (HostBasedHandlerRegistry) registry;
                Set<String> hostnames = hostRegistry.getHostnames();

                // 處理上游服務註冊，信息將整合到最終banner中
                for (String hostname : hostnames) {
                    DgrGrpcProxyMap mapping = proxyManager.getProxyMapping(hostname);
                    if (mapping != null) {
                        upstreamStatusMap.put(hostname, new UpstreamServiceStatus(
                                hostname,
                                mapping.getTargetHostName(),
                                mapping.getTargetPort(),
                                mapping.getSecureMode()
                        ));
                    }
                }
                
                if (!hostnames.isEmpty()) {
                    scheduleHealthChecks();
                }
            }

            serverBuilder.fallbackHandlerRegistry(registry);

            server = serverBuilder.build().start();

            // 記錄服務器啟動成功 - 使用ASCII art banner風格
            String threadName = Thread.currentThread().getName();
            String version = getClass().getPackage().getImplementationVersion();
            if (version == null) {
                version = "dev-snapshot";
            }
            
            Map<String, String> serverInfo = createServerInfoMap();
            
            // 添加伺服器操作配置信息
            serverInfo.put("Server Operation", "STARTING -> ACTIVE");
            serverInfo.put("HTTP/2 Settings", "Enhanced");
            serverInfo.put("Message Limits", "Inbound: 20MB, Metadata: 16KB");
            
            boolean tlsEnabled = sslContext != null && proxyProperties.getTls().isEnabled();
            if (tlsEnabled) {
                serverInfo.put("TLS Configuration", "Enabled with SSL context");
                serverInfo.put("Keep-Alive Settings", "Time: 60s, Timeout: 20s");
                serverInfo.put("Connection Age", "Max: 300s, Grace: 60s");
            } else {
                serverInfo.put("TLS Configuration", "Disabled");
                String sslContextStatus = sslContext != null ? "provided but not used" : "not provided";
                serverInfo.put("SSL Context", sslContextStatus);
            }
            
            // 添加上游服務狀態
            if (upstreamStatusMap.isEmpty()) {
                serverInfo.put("Upstream Services", "No services registered - proxy will start but won't forward requests");
            } else {
                serverInfo.put("Upstream Services", upstreamStatusMap.size() + " services registered");
            }
            
            // 添加初始化和啟動信息
            serverInfo.put("Initialization", "COMPLETED");
            serverInfo.put("Configuration", "LOADED");
            long startTime = System.currentTimeMillis();
            serverInfo.put("Server Started at", new java.util.Date(startTime).toString());
            serverInfo.put("Server Status", "ACTIVE");
            
            // 添加上游服務詳細信息到serverInfo中
            if (!upstreamStatusMap.isEmpty()) {
                int serviceCount = 1;
                for (UpstreamServiceStatus status : upstreamStatusMap.values()) {
                    DgrGrpcProxyMap mapping = proxyManager.getProxyMapping(status.getProxyHostname());
                    if (mapping != null) {
                        String serviceKey = "Upstream Service " + serviceCount;
                        String serviceValue = status.getProxyHostname() + " -> " + 
                                            status.getTargetHostname() + ":" + status.getTargetPort() + 
                                            " (TLS: " + (status.getSecureMode() != null ? status.getSecureMode() : "AUTO") + 
                                            ", Status: " + ("Y".equals(mapping.getEnable()) ? "ENABLED" : "DISABLED") + ")";
                        serverInfo.put(serviceKey, serviceValue);
                        
                        // 添加超時配置
                        String timeoutKey = "Service " + serviceCount + " Timeouts";
                        String timeoutValue = "connect:" + mapping.getConnectTimeoutMs() + "ms, " +
                                             "send:" + mapping.getSendTimeoutMs() + "ms, " +
                                             "read:" + mapping.getReadTimeoutMs() + "ms";
                        serverInfo.put(timeoutKey, timeoutValue);
                        serviceCount++;
                    }
                }
            }
            
            logBannerStyle(threadName, version, serverInfo);

        } catch (IOException e) {
            logSimpleBanner("dgRv4 gRPC proxy startup error", 
                    "Failed to start gRPC proxy server on port " + serverPort + ": " + e.getMessage());
            throw new IOException("Failed to start gRPC proxy server: " + e.getMessage(), e);
        } catch (Exception e) {
            logSimpleBanner("dgRv4 gRPC proxy startup error", 
                    "Unexpected error starting gRPC proxy server: " + e.getMessage());
            throw new IOException("Unexpected error starting gRPC proxy server: " + e.getMessage(), e);
        } finally {
            serverLock.unlock();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.debug("Shutting down gRPC server via shutdown hook");
            try {
                GrpcProxyServer.this.stop();
            } catch (Exception e) {
                logger.debug("Error during server shutdown from hook: " + e.getMessage());
            }
        }));
    }



    /**
     * Perform connection test to upstream service
     *
     * @param mapping The upstream service mapping to test
     * @return TestResult containing success status and message
     */
    private TestResult testUpstreamConnection(DgrGrpcProxyMap mapping) {
        String hostname = mapping.getTargetHostName();
        int port = mapping.getTargetPort();
        int connectTimeout = mapping.getConnectTimeoutMs();
        String secureMode = mapping.getSecureMode();
        if (secureMode == null || secureMode.isEmpty()) {
            secureMode = "AUTO";
        }

        StringBuilder banner = new StringBuilder();
        banner.append("---\n");
        banner.append("    ").append(Thread.currentThread().getName()).append("::\n");
        banner.append("========== dgRv4 gRPC proxy connection test ============\n");
        banner.append(" ...Test timestamp = ").append(new java.util.Date()).append("\n");
        banner.append(" ...Target service = ").append(hostname).append(":").append(port).append("\n");
        banner.append(" ...TLS mode = ").append(secureMode).append("\n");
        banner.append(" ...Connect timeout = ").append(connectTimeout).append("ms\n");

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, port), connectTimeout);
            banner.append(" ...Basic connection = SUCCESS\n");

            if ("AUTO".equals(secureMode) || "SECURE".equals(secureMode)) {
                banner.append(" ...TLS test = REQUIRED, testing...\n");
                TestResult tlsResult = testTlsConnection(mapping);
                
                if (tlsResult.isSuccess()) {
                    banner.append(" ...TLS connection = SUCCESS\n");
                    banner.append(" ...Final result = CONNECTION SUCCESSFUL (TLS)\n");
                } else {
                    banner.append(" ...TLS connection = FAILED (").append(tlsResult.getMessage()).append(")\n");
                    banner.append(" ...Final result = CONNECTION FAILED\n");
                }
                banner.append("_____________________________________________\n");
                
                if (tlsResult.isSuccess()) {
                    logger.debug(banner.toString());
                } else {
                    logger.debug(banner.toString());
                }
                
                return tlsResult;
            } else {
                banner.append(" ...TLS test = NOT REQUIRED\n");
                banner.append(" ...Final result = CONNECTION SUCCESSFUL (PLAINTEXT)\n");
                banner.append("_____________________________________________\n");
                logger.debug(banner.toString());
                return new TestResult(true, "Connection successful (plaintext)");
            }
            
        } catch (SocketTimeoutException e) {
            String errorMsg = "Connection timeout (timeout: " + connectTimeout + "ms)";
            banner.append(" ...Basic connection = TIMEOUT\n");
            banner.append(" ...Error details = ").append(errorMsg).append("\n");
            banner.append(" ...Final result = CONNECTION FAILED\n");
            banner.append("_____________________________________________\n");
            logger.debug(banner.toString());
            return new TestResult(false, errorMsg);
        } catch (IOException e) {
            String errorMsg = "Connection failed: " + e.getMessage();
            banner.append(" ...Basic connection = FAILED\n");
            banner.append(" ...Error details = ").append(errorMsg).append("\n");
            banner.append(" ...Final result = CONNECTION FAILED\n");
            banner.append("_____________________________________________\n");
            logger.debug(banner.toString());
            return new TestResult(false, errorMsg);
        }
    }

    /**
     * Test TLS connection to upstream service, with default trust for self-signed certificates
     *
     * @param mapping The upstream service mapping to test
     * @return TestResult containing success status and message
     */
    private TestResult testTlsConnection(DgrGrpcProxyMap mapping) {
        String hostname = mapping.getTargetHostName();
        int port = mapping.getTargetPort();
        int connectTimeout = mapping.getConnectTimeoutMs();
        boolean autoTrustEnabled = "Y".equals(mapping.isAutoTrustUpstreamCerts());

        if (mapping.isAutoTrustUpstreamCerts() == null) {
            autoTrustEnabled = true;
        }

        if (autoTrustEnabled) {
            return testTlsConnectionWithAutoTrust(hostname, port, connectTimeout);
        } else {
            return testTlsConnectionWithValidation(mapping);
        }
    }

    /**
     * Test TLS connection with auto-trust enabled (for internal services with self-signed certificates)
     */
    private TestResult testTlsConnectionWithAutoTrust(String hostname, int port, int connectTimeout) {
        try {
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, new javax.net.ssl.TrustManager[] {
                    new javax.net.ssl.X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }
                        @SuppressWarnings("java:S4830")
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                            // Auto-trust mode: skipping client certificate validation
                        }
                        @SuppressWarnings("java:S4830")
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                            // Auto-trust mode: accepting server certificate
                        }
                    }
            }, new java.security.SecureRandom());

            javax.net.ssl.SSLSocketFactory factory = sslContext.getSocketFactory();
            javax.net.ssl.SSLSocket socket = null;

            try (Socket tempSocket = new Socket()) {
                tempSocket.connect(new InetSocketAddress(hostname, port), connectTimeout);

                socket = (javax.net.ssl.SSLSocket) factory.createSocket(
                        tempSocket, hostname, port, true);
                socket.setSoTimeout(connectTimeout);

                socket.startHandshake();

                return new TestResult(true, "TLS connection successful (auto-trust enabled)");
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = "TLS connection failed with upstream service (auto-trust): " +
                    hostname + ":" + port + ", error: " + e.getMessage();
            // TLS錯誤信息將在健康檢查報告中統一顯示，此處只記錄debug
            logger.debug(errorMsg);
            return new TestResult(false, errorMsg);
        }
    }

    /**
     * Test TLS connection with standard certificate validation
     */
    private TestResult testTlsConnectionWithValidation(DgrGrpcProxyMap mapping) {
        String hostname = mapping.getTargetHostName();
        int port = mapping.getTargetPort();
        int connectTimeout = mapping.getConnectTimeoutMs();
        String trustedCerts = mapping.getTrustedCertsContent();

        if (trustedCerts != null && !trustedCerts.isEmpty()) {
            try {
                java.security.KeyStore keyStore = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType());
                keyStore.load(null, null);

                java.io.ByteArrayInputStream certStream = new java.io.ByteArrayInputStream(
                        trustedCerts.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
                int certCount = 0;

                while (certStream.available() > 0) {
                    java.security.cert.Certificate cert = cf.generateCertificate(certStream);
                    keyStore.setCertificateEntry("cert-" + certCount++, cert);
                }

                javax.net.ssl.TrustManagerFactory tmf = javax.net.ssl.TrustManagerFactory.getInstance(
                        javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keyStore);

                javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);

                return testTlsConnectionWithContext(sslContext, hostname, port, connectTimeout);
            } catch (Exception e) {
                String errorMsg = "Error setting up custom certificate validation: " + e.getMessage();
                return new TestResult(false, errorMsg);
            }
        } else {
            try {
                javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getDefault();
                return testTlsConnectionWithContext(sslContext, hostname, port, connectTimeout);
            } catch (Exception e) {
                String errorMsg = "TLS connection failed with default trust store: " + e.getMessage();
                return new TestResult(false, errorMsg);
            }
        }
    }

    /**
     * Test TLS connection using a specific SSLContext
     */
    private TestResult testTlsConnectionWithContext(javax.net.ssl.SSLContext sslContext,
                                                    String hostname, int port, int timeout) {
        javax.net.ssl.SSLSocket socket = null;

        try {
            javax.net.ssl.SSLSocketFactory factory = sslContext.getSocketFactory();

            try (Socket tempSocket = new Socket()) {
                tempSocket.connect(new InetSocketAddress(hostname, port), timeout);

                socket = (javax.net.ssl.SSLSocket) factory.createSocket(
                        tempSocket, hostname, port, true);
                socket.setSoTimeout(timeout);
            }

            socket.startHandshake();

            return new TestResult(true, "TLS connection successful with certificate validation");
        } catch (SSLHandshakeException e) {
            String errorMsg = "TLS handshake failed: " + e.getMessage();
            return new TestResult(false, errorMsg);
        } catch (Exception e) {
            String errorMsg = "TLS connection failed: " + e.getMessage();
            return new TestResult(false, errorMsg);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Schedule periodic health checks for all registered upstream services
     */
    private void scheduleHealthChecks() {
        StringBuilder banner = new StringBuilder();
        banner.append("---\n");
        banner.append("    ").append(Thread.currentThread().getName()).append("::\n");
        banner.append("========== dgRv4 gRPC proxy scheduler setup ============\n");
        
        if (!healthCheckEnabled) {
            banner.append(" ...Health check scheduler = DISABLED\n");
            banner.append(" ...Statistics scheduler = NOT STARTED\n");
            banner.append(" ...Reason = Health checks disabled in configuration\n");
            banner.append("_____________________________________________\n");
            logger.warn(banner.toString());
            return;
        }

        banner.append(" ...Health check scheduler = ENABLED\n");
        banner.append(" ...Health check interval = 5 seconds\n");
        banner.append(" ...Statistics report interval = 20 seconds\n");
        banner.append(" ...Scheduler thread pool = 2 threads\n");
        banner.append(" ...Services to monitor = ").append(upstreamStatusMap.size()).append("\n");
        
        healthCheckExecutor.scheduleAtFixedRate(() -> {
            try {
                performHealthChecks();
            } catch (Exception e) {
                logger.error("Critical error during scheduled health check: " + e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);

        healthCheckExecutor.scheduleAtFixedRate(() -> {
            try {
                reportStatistics();
            } catch (Exception e) {
                logger.error("Critical error during statistics reporting: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.MINUTES);
        
        banner.append(" ...Scheduler status = ACTIVE\n");
        banner.append("_____________________________________________\n");
        logger.info(banner.toString());
    }

    /**
     * Perform health checks on all registered upstream services
     */
    private void performHealthChecks() {
        if (upstreamStatusMap.isEmpty()) {
            return;
        }

        StringBuilder banner = new StringBuilder();
        banner.append("---\n");
        banner.append("    ").append(Thread.currentThread().getName()).append("::\n");
        banner.append("========== dgRv4 gRPC proxy health check report ============\n");
        banner.append(" ...Check timestamp = ").append(new java.util.Date()).append("\n");
        banner.append(" ...Total services = ").append(upstreamStatusMap.size()).append("\n");

        int healthyCount = 0;
        int unhealthyCount = 0;
        int statusChangedCount = 0;
        
        for (UpstreamServiceStatus status : upstreamStatusMap.values()) {
            String proxyHostname = status.getProxyHostname();

            DgrGrpcProxyMap mapping = proxyManager.getProxyMapping(proxyHostname);
            if (mapping == null) {
                banner.append(" ...").append(proxyHostname).append(" = SKIPPED (mapping not found)\n");
                continue;
            }

            if (!"Y".equals(mapping.getEnable())) {
                banner.append(" ...").append(proxyHostname).append(" = SKIPPED (service disabled)\n");
                continue;
            }

            TestResult result = testUpstreamConnection(mapping);
            boolean previousStatus = status.isHealthy();
            
            status.setHealthy(result.isSuccess());
            status.setLastChecked(System.currentTimeMillis());
            status.setLastMessage(result.getMessage());

            String statusText = result.isSuccess() ? "HEALTHY" : "UNHEALTHY";
            if (result.isSuccess()) {
                healthyCount++;
            } else {
                unhealthyCount++;
            }

            banner.append(" ...").append(proxyHostname)
                  .append(" -> ").append(status.getTargetHostname())
                  .append(":").append(status.getTargetPort())
                  .append(" = ").append(statusText);

            if (previousStatus != result.isSuccess()) {
                statusChangedCount++;
                String changeType = result.isSuccess() ? "RECOVERED" : "FAILED";
                banner.append(" [").append(changeType).append("]");
                
                eventPublisher.publishEvent(new UpstreamHealthCheckEvent(
                        proxyHostname, status.getTargetHostname(), status.getTargetPort(), 
                        result.isSuccess(), result.getMessage()));
            }

            if (!result.isSuccess() && result.getMessage() != null) {
                banner.append(" - ").append(result.getMessage());
            }
            banner.append("\n");
        }

        banner.append(" ...Health summary = ").append(healthyCount).append(" healthy, ")
              .append(unhealthyCount).append(" unhealthy");
        if (statusChangedCount > 0) {
            banner.append(", ").append(statusChangedCount).append(" status changed");
        }
        banner.append("\n");
        banner.append("_____________________________________________\n");

        // 根據結果設置日誌級別
        if (unhealthyCount > 0 || statusChangedCount > 0) {
            logger.warn(banner.toString());
        } else {
            logger.info(banner.toString());
        }
    }

    /**
     * Report proxy server statistics
     */
    private void reportStatistics() {
        StringBuilder banner = new StringBuilder();
        banner.append("---\n");
        banner.append("========== dgRv4 gRPC proxy statistics report ============\n");
        banner.append(" ...Report timestamp = ").append(new java.util.Date()).append("\n");
        
        // 基本統計
        banner.append(" ...Total calls processed = ").append(totalCallsProcessed.get()).append("\n");
        banner.append(" ...Failed calls = ").append(failedCallsCount.get());
        if (totalCallsProcessed.get() > 0) {
            double failRate = (double) failedCallsCount.get() / totalCallsProcessed.get() * 100;
            banner.append(" (").append(String.format("%.2f", failRate)).append("%)");
        }
        banner.append("\n");
        banner.append(" ...TLS errors = ").append(tlsErrorsCount.get()).append("\n");
        
        // 系統資源
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        banner.append(" ...Memory(used/total/Max) = ")
              .append(usedMemory / (1024 * 1024)).append("MB / ")
              .append(runtime.totalMemory() / (1024 * 1024)).append("MB / ")
              .append(runtime.maxMemory() / (1024 * 1024)).append("MB\n");
        banner.append(" ...CPU runtime core = ").append(runtime.availableProcessors()).append("\n");
        
        // 服務器運行時間
        long uptimeMs = System.currentTimeMillis() - server.getListenSockets().hashCode();
        long uptimeSec = uptimeMs / 1000;
        long days = uptimeSec / 86400;
        long hours = (uptimeSec % 86400) / 3600;
        long minutes = (uptimeSec % 3600) / 60;
        long seconds = uptimeSec % 60;

        StringBuilder uptimeStr = new StringBuilder();
        if (days > 0) uptimeStr.append(days).append("d ");
        if (hours > 0 || days > 0) uptimeStr.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) uptimeStr.append(minutes).append("m ");
        uptimeStr.append(seconds).append("s");
        
        banner.append(" ...Server uptime = ").append(uptimeStr.toString()).append("\n");
        
        // 上游服務健康狀況
        if (!upstreamStatusMap.isEmpty()) {
            banner.append(" ...Upstream services health:\n");
            int healthyServices = 0;
            int unhealthyServices = 0;
            
            for (UpstreamServiceStatus status : upstreamStatusMap.values()) {
                boolean isHealthy = status.isHealthy();
                if (isHealthy) {
                    healthyServices++;
                } else {
                    unhealthyServices++;
                }
                
                banner.append("    ...").append(status.getProxyHostname())
                      .append(" -> ").append(status.getTargetHostname())
                      .append(":").append(status.getTargetPort())
                      .append(" = ").append(isHealthy ? "HEALTHY" : "UNHEALTHY");
                
                AtomicInteger errorCount = errorsByHostname.get(status.getProxyHostname());
                if (errorCount != null && errorCount.get() > 0) {
                    banner.append(" [errors: ").append(errorCount.get()).append("]");
                }
                
                if (!isHealthy && status.getLastMessage() != null) {
                    banner.append(" - ").append(status.getLastMessage());
                }
                banner.append("\n");
            }
            
            banner.append(" ...Health summary = ").append(healthyServices).append(" healthy, ")
                  .append(unhealthyServices).append(" unhealthy\n");
        } else {
            banner.append(" ...Upstream services = No services registered\n");
        }
        
        banner.append("_____________________________________________\n");
        
        // 根據服務健康狀況決定日誌級別
        boolean hasUnhealthyServices = upstreamStatusMap.values().stream().anyMatch(s -> !s.isHealthy());
        if (hasUnhealthyServices || failedCallsCount.get() > 0) {
            logger.warn(banner.toString());
        } else {
            logger.info(banner.toString());
        }
    }

    // logSystemResources 方法已被移除，其功能已整合到各個相關方法的 banner 中

    /**
     * Listen for proxy configuration change events
     * When the configuration changes, restart the gRPC server to apply the new configuration
     *
     * @param event Configuration change event
     */
    @EventListener
    public void handleProxyConfigChangedEvent(ProxyConfigChangedEvent event) {
        ProxyConfigChangedEvent.ChangeType changeType = event.getChangeType();
        String proxyHostname = event.getProxyHostname();

        switch (changeType) {
            case ADDED:
                DgrGrpcProxyMap addedMapping = proxyManager.getProxyMapping(proxyHostname);
                if (addedMapping != null) {
                    logConfigChangeBanner("ADDED", proxyHostname, addedMapping);

                    TestResult testResult = testUpstreamConnection(addedMapping);
                    if (testResult.isSuccess()) {
                        logSimpleBanner("dgRv4 gRPC proxy connection test", 
                                "Connection test successful to new upstream service: " +
                                proxyHostname + " -> " + addedMapping.getTargetHostName() +
                                ":" + addedMapping.getTargetPort());
                    } else {
                        logSimpleBanner("dgRv4 gRPC proxy connection test", 
                                "Connection test failed for new upstream service: " +
                                proxyHostname + " -> " + addedMapping.getTargetHostName() +
                                ":" + addedMapping.getTargetPort() +
                                ", reason: " + testResult.getMessage());
                    }

                    upstreamStatusMap.put(proxyHostname, new UpstreamServiceStatus(
                            proxyHostname,
                            addedMapping.getTargetHostName(),
                            addedMapping.getTargetPort(),
                            addedMapping.getSecureMode()
                    ));
                } else {
                    logSimpleBanner("dgRv4 gRPC proxy config error", 
                            "Added upstream service but details not found: " + proxyHostname);
                }
                break;

            case UPDATED:
                DgrGrpcProxyMap updatedMapping = proxyManager.getProxyMapping(proxyHostname);
                if (updatedMapping != null) {
                    logConfigChangeBanner("UPDATED", proxyHostname, updatedMapping);

                    if ("Y".equals(updatedMapping.getEnable())) {
                        TestResult testResult = testUpstreamConnection(updatedMapping);
                        if (testResult.isSuccess()) {
                            logSimpleBanner("dgRv4 gRPC proxy connection test", 
                                    "Connection test successful to updated upstream service: " +
                                    proxyHostname + " -> " + updatedMapping.getTargetHostName() +
                                    ":" + updatedMapping.getTargetPort());
                        } else {
                            logSimpleBanner("dgRv4 gRPC proxy connection test", 
                                    "Connection test failed for updated upstream service: " +
                                    proxyHostname + " -> " + updatedMapping.getTargetHostName() +
                                    ":" + updatedMapping.getTargetPort() +
                                    ", reason: " + testResult.getMessage());
                        }
                    }

                    UpstreamServiceStatus status = upstreamStatusMap.get(proxyHostname);
                    if (status != null) {
                        status.setTargetHostname(updatedMapping.getTargetHostName());
                        status.setTargetPort(updatedMapping.getTargetPort());
                        status.setSecureMode(updatedMapping.getSecureMode());
                    } else {
                        upstreamStatusMap.put(proxyHostname, new UpstreamServiceStatus(
                                proxyHostname,
                                updatedMapping.getTargetHostName(),
                                updatedMapping.getTargetPort(),
                                updatedMapping.getSecureMode()
                        ));
                    }
                } else {
                    logSimpleBanner("dgRv4 gRPC proxy config error", 
                            "Updated upstream service but details not found: " + proxyHostname);
                }
                break;

            case REMOVED:
                logConfigChangeBanner("REMOVED", proxyHostname, null);
                upstreamStatusMap.remove(proxyHostname);
                break;

            case REFRESHED:
                logSimpleBanner("dgRv4 gRPC proxy config refresh", 
                        "Refreshing all upstream services configuration");
                break;
        }

        restart();
    }

    /**
     * Handle upstream service health check events
     *
     * @param event Health check event
     */
    @EventListener
    public void handleUpstreamHealthCheckEvent(UpstreamHealthCheckEvent event) {
        // 健康檢查事件將在健康檢查報告和統計報告中統一顯示，此處只記錄debug信息
        logger.debug("Upstream health check event: " + event.getProxyHostname() +
                " (" + event.getTargetHostname() + ":" + event.getTargetPort() + ")" +
                " status=" + (event.isHealthy() ? "HEALTHY" : "UNHEALTHY") +
                (event.getMessage() != null ? ", message=" + event.getMessage() : ""));
    }

    /**
     * Record a call failure with the upstream service
     *
     * @param proxyHostname The proxy hostname
     * @param targetHostname The target hostname
     * @param targetPort The target port
     * @param method The method being called
     * @param errorType The type of error
     * @param errorMessage The error message
     */
    public void recordCallFailure(String proxyHostname, String targetHostname, int targetPort,
                                  String method, String errorType, String errorMessage) {
        failedCallsCount.incrementAndGet();

        errorsByHostname.computeIfAbsent(proxyHostname, k -> new AtomicInteger(0)).incrementAndGet();

        if ("TLS_ERROR".equals(errorType) || errorMessage.contains("SSL") || errorMessage.contains("TLS")) {
            tlsErrorsCount.incrementAndGet();
        }

        // 記錄調用失敗，但不立即輸出，等待統計報告時統一顯示
        logger.debug("Call failure recorded: " + proxyHostname + " -> " + targetHostname + ":" + targetPort + 
                    ", method: " + method + ", error: " + errorType + ", message: " + errorMessage);

        if ("CONNECTION_REFUSED".equals(errorType) || "CONNECTION_TIMEOUT".equals(errorType)) {
            UpstreamServiceStatus status = upstreamStatusMap.get(proxyHostname);
            if (status != null) {
                status.setHealthy(false);
                status.setLastMessage(errorMessage);
                status.setLastChecked(System.currentTimeMillis());

                if (status.getConsecutiveFailures() == 0) {
                    logger.debug("Scheduling immediate health check for " + proxyHostname + " due to first failure");
                    scheduleImmediateHealthCheck(proxyHostname);
                }

                status.incrementConsecutiveFailures();
            }
        }
    }

    /**
     * Schedule an immediate health check for a specific upstream service
     *
     * @param proxyHostname The hostname to check
     */
    private void scheduleImmediateHealthCheck(String proxyHostname) {
        if (!healthCheckEnabled) {
            return;
        }

        healthCheckExecutor.schedule(() -> {
            try {
                logger.debug("Running immediate health check for upstream service: " + proxyHostname);

                DgrGrpcProxyMap mapping = proxyManager.getProxyMapping(proxyHostname);
                if (mapping == null) {
                    logger.debug("Cannot perform immediate health check - mapping not found for: " + proxyHostname);
                    return;
                }

                TestResult result = testUpstreamConnection(mapping);

                UpstreamServiceStatus status = upstreamStatusMap.get(proxyHostname);
                if (status != null) {
                    boolean previousStatus = status.isHealthy();
                    status.setHealthy(result.isSuccess());
                    status.setLastChecked(System.currentTimeMillis());
                    status.setLastMessage(result.getMessage());

                    if (result.isSuccess()) {
                        status.resetConsecutiveFailures();
                    }

                    if (previousStatus != result.isSuccess()) {
                        String targetHostname = mapping.getTargetHostName();
                        int targetPort = mapping.getTargetPort();

                        // 狀態變化將在下次健康檢查報告中統一顯示
                        logger.debug("Upstream service status changed: " + proxyHostname + 
                                   " (" + targetHostname + ":" + targetPort + ") is now " + 
                                   (result.isSuccess() ? "HEALTHY" : "UNHEALTHY"));

                        eventPublisher.publishEvent(new UpstreamHealthCheckEvent(
                                proxyHostname, targetHostname, targetPort, result.isSuccess(), result.getMessage()));
                    }
                }
            } catch (Exception e) {
                // 立即健康檢查錯誤將在下次健康檢查報告中統一顯示
                logger.debug("Error during immediate health check for " + proxyHostname + ": " + e.getMessage());
            }
        }, 2, TimeUnit.SECONDS);
    }

    /**
     * Record a successful call to the upstream service
     *
     * @param proxyHostname The proxy hostname
     */
    public void recordSuccessfulCall(String proxyHostname) {
        totalCallsProcessed.incrementAndGet();

        UpstreamServiceStatus status = upstreamStatusMap.get(proxyHostname);
        if (status != null && !status.isHealthy()) {
            logger.debug("Successful call to previously unhealthy upstream service: " + proxyHostname +
                    ", scheduling immediate health check");
            scheduleImmediateHealthCheck(proxyHostname);
        }
    }

    /**
     * Restart the gRPC server, applying the new handler registry
     */
    public void restart() {
        serverLock.lock();
        try {
            StringBuilder banner = new StringBuilder();
            banner.append("---\n");
            banner.append("    ").append(Thread.currentThread().getName()).append("::\n");
            banner.append("========== dgRv4 gRPC proxy server restart ============\n");
            banner.append(" ...Restart timestamp = ").append(new java.util.Date()).append("\n");
            
            if (server != null) {
                banner.append(" ...Current server = ACTIVE, preparing shutdown\n");

                try {
                    boolean shutdownSuccessful = server.shutdown().awaitTermination(5, TimeUnit.SECONDS);

                    if (!shutdownSuccessful) {
                        banner.append(" ...Graceful shutdown = TIMEOUT, forcing shutdown\n");
                        server.shutdownNow();
                    } else {
                        banner.append(" ...Graceful shutdown = SUCCESS\n");
                    }

                } catch (InterruptedException e) {
                    banner.append(" ...Shutdown error = INTERRUPTED - ").append(e.getMessage()).append("\n");
                    Thread.currentThread().interrupt();
                    logger.error(banner.toString());
                    return;
                } catch (Exception e) {
                    banner.append(" ...Shutdown error = UNEXPECTED - ").append(e.getMessage()).append("\n");
                    logger.error(banner.toString());
                    return;
                }

                try {
                    var registry = proxyManager.getHandlerRegistry();
                    int hostnameCount = 0;
                    
                    if (registry instanceof HostBasedHandlerRegistry) {
                        HostBasedHandlerRegistry hostRegistry = (HostBasedHandlerRegistry) registry;
                        Set<String> hostnames = hostRegistry.getHostnames();
                        hostnameCount = hostnames.size();

                        if (hostnames.isEmpty()) {
                            banner.append(" ...Handler registry = EMPTY (no request forwarding)\n");
                        } else {
                            banner.append(" ...Handler registry = UPDATED (").append(hostnameCount).append(" hostnames)\n");
                        }
                    }

                    ServerBuilder<?> serverBuilder;
                    boolean tlsEnabled = sslContext != null && proxyProperties.getTls().isEnabled();

                    if (tlsEnabled) {
                        serverBuilder = NettyServerBuilder.forPort(serverPort)
                                .sslContext(sslContext);
                        banner.append(" ...Server configuration = TLS ENABLED on port ").append(serverPort).append("\n");
                    } else {
                        serverBuilder = ServerBuilder.forPort(serverPort);
                        banner.append(" ...Server configuration = NO TLS on port ").append(serverPort).append("\n");
                    }

                    serverBuilder
                            .maxConnectionAge(60, TimeUnit.SECONDS)
                            .maxConnectionAgeGrace(30, TimeUnit.SECONDS)
                            .maxInboundMessageSize(20 * 1024 * 1024)
                            .maxInboundMetadataSize(16 * 1024)
                            .fallbackHandlerRegistry(registry);
                    
                    banner.append(" ...Connection limits = Age: 60s, Grace: 30s\n");
                    banner.append(" ...Message limits = Inbound: 20MB, Metadata: 16KB\n");

                    try {
                        server = serverBuilder.build().start();
                        banner.append(" ...Server restart = SUCCESS\n");
                        banner.append(" ...Post-restart actions = System check + Health check\n");

                        // 系統資源檢查已整合到banner中
                        Runtime runtime = Runtime.getRuntime();
                        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                        banner.append(" ...Memory usage = ").append(usedMemory / (1024 * 1024))
                              .append("MB / ").append(runtime.totalMemory() / (1024 * 1024))
                              .append("MB / ").append(runtime.maxMemory() / (1024 * 1024)).append("MB\n");
                        banner.append(" ...CPU cores = ").append(runtime.availableProcessors()).append("\n");

                        banner.append("_____________________________________________\n");
                        logger.info(banner.toString());
                        
                        // 立即執行一次健康檢查
                        performHealthChecks();

                    } catch (java.net.BindException e) {
                        banner.append(" ...Server restart = FAILED (Port ").append(serverPort).append(" in use)\n");
                        banner.append("_____________________________________________\n");
                        logger.error(banner.toString());
                        throw e;
                    } catch (java.io.IOException e) {
                        banner.append(" ...Server restart = FAILED (IO Error: ").append(e.getMessage()).append(")\n");
                        banner.append("_____________________________________________\n");
                        logger.error(banner.toString());
                        throw e;
                    } catch (Exception e) {
                        banner.append(" ...Server restart = FAILED (Unexpected: ").append(e.getMessage()).append(")\n");
                        banner.append("_____________________________________________\n");
                        logger.error(banner.toString());
                        throw e;
                    }
                } catch (Exception e) {
                    banner.append(" ...Configuration error = ").append(e.getMessage()).append("\n");
                    banner.append("_____________________________________________\n");
                    logger.error(banner.toString());
                }
            } else {
                banner.append(" ...Current server = NULL (nothing to restart)\n");
                banner.append(" ...Action = SKIPPED\n");
                banner.append("_____________________________________________\n");
                logger.warn(banner.toString());
            }
        } finally {
            serverLock.unlock();
        }
    }

    @PreDestroy
    public void stop() {
        serverLock.lock();
        try {
            StringBuilder banner = new StringBuilder();
            banner.append("---\n");
            banner.append("    ").append(Thread.currentThread().getName()).append("::\n");
            banner.append("========== dgRv4 gRPC proxy server shutdown ============\n");
            banner.append(" ...Shutdown timestamp = ").append(new java.util.Date()).append("\n");

            // 關閉健康檢查執行器
            if (!healthCheckExecutor.isShutdown()) {
                banner.append(" ...Health check executor = ACTIVE, shutting down\n");
                try {
                    healthCheckExecutor.shutdown();
                    if (!healthCheckExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        banner.append(" ...Health check executor = TIMEOUT, forcing shutdown\n");
                        healthCheckExecutor.shutdownNow();
                    } else {
                        banner.append(" ...Health check executor = SHUTDOWN SUCCESS\n");
                    }
                } catch (InterruptedException e) {
                    banner.append(" ...Health check executor = INTERRUPTED during shutdown\n");
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    banner.append(" ...Health check executor = ERROR during shutdown: ").append(e.getMessage()).append("\n");
                }
            } else {
                banner.append(" ...Health check executor = ALREADY SHUTDOWN\n");
            }

            // 關閉主服務器
            if (server != null) {
                banner.append(" ...gRPC server = ACTIVE, preparing shutdown\n");
                try {
                    // 生成最終統計報告
                    banner.append(" ...Final statistics = GENERATING\n");
                    reportStatistics();

                    boolean shutdownSuccessful = server.shutdown().awaitTermination(5, TimeUnit.SECONDS);

                    if (!shutdownSuccessful) {
                        banner.append(" ...gRPC server = TIMEOUT, forcing shutdown\n");
                        server.shutdownNow();
                    } else {
                        banner.append(" ...gRPC server = SHUTDOWN SUCCESS\n");
                    }

                    // 最終狀態總結
                    banner.append(" ...Shutdown status = COMPLETED\n");
                    banner.append(" ...Processed calls = ").append(totalCallsProcessed.get()).append("\n");
                    banner.append(" ...Failed calls = ").append(failedCallsCount.get()).append("\n");
                    banner.append(" ...Upstream services = ").append(upstreamStatusMap.size()).append(" monitored\n");
                    
                } catch (InterruptedException e) {
                    banner.append(" ...gRPC server = INTERRUPTED during shutdown\n");
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    banner.append(" ...gRPC server = ERROR during shutdown: ").append(e.getMessage()).append("\n");
                }
            } else {
                banner.append(" ...gRPC server = NOT RUNNING (nothing to shutdown)\n");
            }
            
            banner.append("_____________________________________________\n");
            logger.info(banner.toString());
            
        } finally {
            serverLock.unlock();
        }
    }

    /**
     * Create logging interceptor for gRPC calls
     */
    private ServerInterceptor createLoggingInterceptor() {
        return new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                String methodName = call.getMethodDescriptor().getFullMethodName();
                String peerAddress = String.valueOf(call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR));

                // gRPC調用統計將在統計報告中統一顯示，此處只記錄debug信息
                logger.debug("gRPC call received: " + methodName + " from " + peerAddress);

                return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                        next.startCall(call, headers)) {
                    @Override
                    public void onMessage(ReqT message) {
                        logger.debug("gRPC message: " + methodName + " - " + message);
                        super.onMessage(message);
                    }

                    @Override
                    public void onCancel() {
                        logger.debug("gRPC call cancelled: " + methodName + " from " + peerAddress);
                        super.onCancel();
                    }

                    @Override
                    public void onComplete() {
                        logger.debug("gRPC call completed: " + methodName + " from " + peerAddress);
                        super.onComplete();
                    }
                };
            }
        };
    }

    /**
     * TestResult class to hold connection test results
     */
    private static class TestResult {
        private final boolean success;
        private final String message;

        public TestResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * UpstreamServiceStatus class to track upstream service health
     */
    private static class UpstreamServiceStatus {
        private final String proxyHostname;
        private String targetHostname;
        private int targetPort;
        private String secureMode;
        private boolean healthy = false;
        private long lastChecked = 0;
        private String lastMessage = null;
        private int consecutiveFailures = 0;

        public UpstreamServiceStatus(String proxyHostname, String targetHostname, int targetPort, String secureMode) {
            this.proxyHostname = proxyHostname;
            this.targetHostname = targetHostname;
            this.targetPort = targetPort;
            this.secureMode = secureMode;
        }

        public String getProxyHostname() {
            return proxyHostname;
        }

        public String getTargetHostname() {
            return targetHostname;
        }

        public void setTargetHostname(String targetHostname) {
            this.targetHostname = targetHostname;
        }

        public int getTargetPort() {
            return targetPort;
        }

        public void setTargetPort(int targetPort) {
            this.targetPort = targetPort;
        }

        public String getSecureMode() {
            return secureMode;
        }

        public void setSecureMode(String secureMode) {
            this.secureMode = secureMode;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public long getLastChecked() {
            return lastChecked;
        }

        public void setLastChecked(long lastChecked) {
            this.lastChecked = lastChecked;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public void setLastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
        }

        public int getConsecutiveFailures() {
            return consecutiveFailures;
        }

        public void incrementConsecutiveFailures() {
            this.consecutiveFailures++;
        }

        public void resetConsecutiveFailures() {
            this.consecutiveFailures = 0;
        }
    }


    

    

    
    /**
     * 創建ASCII art banner風格的日誌
     */
    private void logBannerStyle(String threadName, String version, Map<String, String> serverInfo) {
        StringBuilder banner = new StringBuilder();
        banner.append("---\n");
        banner.append("    ").append(threadName).append("::\n");
        banner.append("╔══════════════════════════════════════════════════════════════╗\n");
        banner.append("║                                                              ║\n");
        banner.append("║   ██████╗  ██████╗ ██████╗ ██╗   ██╗██╗  ██╗                ║\n");
        banner.append("║   ██╔══██╗██╔════╝ ██╔══██╗██║   ██║██║  ██║                ║\n");
        banner.append("║   ██║  ██║██║  ███╗██████╔╝██║   ██║███████║                ║\n");
        banner.append("║   ██║  ██║██║   ██║██╔══██╗╚██╗ ██╔╝╚════██║                ║\n");
        banner.append("║   ██████╔╝╚██████╔╝██║  ██║ ╚████╔╝      ██║                ║\n");
        banner.append("║   ╚═════╝  ╚═════╝ ╚═╝  ╚═╝  ╚═══╝       ╚═╝                ║\n");
        banner.append("║                                                              ║\n");
        banner.append("║            ██████╗ ██████╗ ██████╗  ██████╗                 ║\n");
        banner.append("║           ██╔════╝ ██╔══██╗██╔══██╗██╔════╝                 ║\n");
        banner.append("║           ██║  ███╗██████╔╝██████╔╝██║                      ║\n");
        banner.append("║           ██║   ██║██╔══██╗██╔═══╝ ██║                      ║\n");
        banner.append("║           ╚██████╔╝██║  ██║██║     ╚██████╗                 ║\n");
        banner.append("║            ╚═════╝ ╚═╝  ╚═╝╚═╝      ╚═════╝                 ║\n");
        banner.append("║                                                              ║\n");
        banner.append("║                    ██████╗ ██████╗  ██████╗ ██╗  ██╗██╗   ██╗║\n");
        banner.append("║                    ██╔══██╗██╔══██╗██╔═══██╗╚██╗██╔╝╚██╗ ██╔╝║\n");
        banner.append("║                    ██████╔╝██████╔╝██║   ██║ ╚███╔╝  ╚████╔╝ ║\n");
        banner.append("║                    ██╔═══╝ ██╔══██╗██║   ██║ ██╔██╗   ╚██╔╝  ║\n");
        banner.append("║                    ██║     ██║  ██║╚██████╔╝██╔╝ ██╗   ██║   ║\n");
        banner.append("║                    ╚═╝     ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ║\n");
        banner.append("║                                                              ║\n");
        banner.append("╚══════════════════════════════════════════════════════════════╝\n");
                banner.append("========== dgRv4 gRPC proxy server info ============\n");
        banner.append(" ...dgR VERSION = ").append(version).append("\n");
        
        for (Map.Entry<String, String> entry : serverInfo.entrySet()) {
            banner.append(" ...").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        
        banner.append("_____________________________________________\n");
        
        logger.info(banner.toString());
    }
    
    /**
     * 創建服務器信息Map
     */
    private Map<String, String> createServerInfoMap() {
        Map<String, String> serverInfo = new LinkedHashMap<>();
        
        try {
            // 基本服務器信息
            serverInfo.put("gRPC Port", String.valueOf(serverPort));
            serverInfo.put("spring.profiles.active", System.getProperty("spring.profiles.active", "default"));
            
            String nodeName = "grpc-proxy";
            try {
                nodeName = "grpc-proxy-" + java.net.InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                nodeName = "grpc-proxy-unknown";
            }
            serverInfo.put("NodeName", nodeName);
            
            // 基本健康檢查配置
            serverInfo.put("Health Check Enabled", String.valueOf(healthCheckEnabled));
            
            // 系統資源
            Runtime runtime = Runtime.getRuntime();
            serverInfo.put("CPU runtime core", String.valueOf(runtime.availableProcessors()));
            serverInfo.put("Memory(free/total/Max)", 
                    (runtime.freeMemory() / (1024 * 1024)) + "MB / " + 
                    (runtime.totalMemory() / (1024 * 1024)) + "MB / " + 
                    (runtime.maxMemory() / (1024 * 1024)) + "MB");
            
            // Java版本信息
            serverInfo.put("Java Version", System.getProperty("java.version"));
            serverInfo.put("Java VM", System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version"));
            
        } catch (Exception e) {
            serverInfo.put("Error", "Failed to collect server info: " + e.getMessage());
        }
        
        return serverInfo;
    }
    
    /**
     * 創建簡單的banner風格日誌
     */
    private void logSimpleBanner(String title, String message) {
        StringBuilder banner = new StringBuilder();
        banner.append("---\n");
        banner.append("    ").append(Thread.currentThread().getName()).append("::\n");
        banner.append("========== ").append(title).append(" ============\n");
        banner.append(" ...").append(message).append("\n");
        banner.append("_____________________________________________\n");
        
        logger.info(banner.toString());
    }
    
    /**
     * 創建配置變更的banner日誌
     */
    private void logConfigChangeBanner(String changeType, String proxyHostname, DgrGrpcProxyMap mapping) {
        StringBuilder banner = new StringBuilder();
        banner.append("---\n");
        banner.append("    ").append(Thread.currentThread().getName()).append("::\n");
        banner.append("========== dgRv4 gRPC proxy config change ============\n");
        banner.append(" ...Change type = ").append(changeType).append("\n");
        banner.append(" ...Proxy hostname = ").append(proxyHostname).append("\n");
        
        if (mapping != null) {
            banner.append(" ...Target = ").append(mapping.getTargetHostName()).append(":").append(mapping.getTargetPort()).append("\n");
            banner.append(" ...TLS mode = ").append(mapping.getSecureMode() != null ? mapping.getSecureMode() : "AUTO").append("\n");
            banner.append(" ...Status = ").append("Y".equals(mapping.getEnable()) ? "ENABLED" : "DISABLED").append("\n");
            banner.append(" ...Timeouts = connect:").append(mapping.getConnectTimeoutMs())
                  .append("ms, send:").append(mapping.getSendTimeoutMs())
                  .append("ms, read:").append(mapping.getReadTimeoutMs()).append("ms\n");
        }
        
        banner.append("_____________________________________________\n");
        
        logger.info(banner.toString());
    }
}