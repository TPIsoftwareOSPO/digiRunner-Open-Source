package tpi.dgrv4.grpcproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

/**
 * Configuration properties class for the gRPC proxy service.
 * Properties are read from the configuration file with the prefix "grpc.proxy".
 */
@ConfigurationProperties("grpc.proxy")
public class GrpcProxyProperties {
    /**
     * List of proxy mappings defining how hostnames are mapped to target services.
     */
    private List<ProxyMapping> mappings;

    /**
     * Server configuration properties.
     */
    private ServerConfig server;

    /**
     * Timeout configuration properties.
     */
    private TimeoutConfig timeout = new TimeoutConfig();

    /**
     * TLS configuration properties.
     */
    private TlsConfig tls = new TlsConfig();

    /**
     * Returns the list of proxy mappings.
     *
     * @return List of proxy mappings
     */
    public List<ProxyMapping> getMappings() {
        return mappings;
    }

    /**
     * Sets the list of proxy mappings.
     *
     * @param mappings List of proxy mappings
     */
    public void setMappings(List<ProxyMapping> mappings) {
        this.mappings = mappings;
    }

    /**
     * Returns the server configuration.
     *
     * @return ServerConfig object containing server properties
     */
    public ServerConfig getServer() {
        return server;
    }

    /**
     * Sets the server configuration.
     *
     * @param server ServerConfig object containing server properties
     */
    public void setServer(ServerConfig server) {
        this.server = server;
    }

    /**
     * Returns the timeout configuration.
     *
     * @return TimeoutConfig object containing timeout properties
     */
    public TimeoutConfig getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout configuration.
     *
     * @param timeout TimeoutConfig object containing timeout properties
     */
    public void setTimeout(TimeoutConfig timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the TLS configuration.
     *
     * @return TlsConfig object containing TLS properties
     */
    public TlsConfig getTls() {
        return tls;
    }

    /**
     * Sets the TLS configuration.
     *
     * @param tls TlsConfig object containing TLS properties
     */
    public void setTls(TlsConfig tls) {
        this.tls = tls;
    }

    /**
     * Proxy mapping configuration class.
     * Defines the hostname to proxy and the target service to forward to.
     */
    public static class ProxyMapping {
        /**
         * The hostname that will be proxied (what clients connect to).
         */
        private String proxyHostname;

        /**
         * The target hostname where requests will be forwarded to.
         */
        private String targetHostname;

        /**
         * The port number of the target service.
         */
        private int targetPort;

        /**
         * Indicates whether to use TLS to connect to the target service.
         * Values: "AUTO", "SECURE", "PLAINTEXT"
         * - AUTO: Automatically detect if the target service uses TLS
         * - SECURE: Always use TLS to connect to the target service
         * - PLAINTEXT: Never use TLS to connect to the target service
         * Default is "AUTO".
         */
        private String secureMode = "AUTO";

        /**
         * Timeout configuration for this specific proxy mapping.
         * If not specified, the global timeout configuration will be used.
         */
        private TimeoutConfig timeout;

        /**
         * Returns the proxy hostname.
         *
         * @return Proxy hostname string
         */
        public String getProxyHostname() {
            return proxyHostname;
        }

        /**
         * Sets the proxy hostname.
         *
         * @param proxyHostname Proxy hostname string
         */
        public void setProxyHostname(String proxyHostname) {
            this.proxyHostname = proxyHostname;
        }

        /**
         * Returns the target hostname.
         *
         * @return Target hostname string
         */
        public String getTargetHostname() {
            return targetHostname;
        }

        /**
         * Sets the target hostname.
         *
         * @param targetHostname Target hostname string
         */
        public void setTargetHostname(String targetHostname) {
            this.targetHostname = targetHostname;
        }

        /**
         * Returns the target port.
         *
         * @return Target port number
         */
        public int getTargetPort() {
            return targetPort;
        }

        /**
         * Sets the target port.
         *
         * @param targetPort Target port number
         */
        public void setTargetPort(int targetPort) {
            this.targetPort = targetPort;
        }

        /**
         * Returns the secure mode.
         *
         * @return Secure mode string (AUTO, SECURE, or PLAINTEXT)
         */
        public String getSecureMode() {
            return secureMode;
        }

        /**
         * Sets the secure mode.
         *
         * @param secureMode Secure mode string (AUTO, SECURE, or PLAINTEXT)
         */
        public void setSecureMode(String secureMode) {
            this.secureMode = secureMode;
        }

        /**
         * Returns the timeout configuration for this proxy mapping.
         *
         * @return TimeoutConfig object or null if not specified
         */
        public TimeoutConfig getTimeout() {
            return timeout;
        }

        /**
         * Sets the timeout configuration for this proxy mapping.
         *
         * @param timeout TimeoutConfig object
         */
        public void setTimeout(TimeoutConfig timeout) {
            this.timeout = timeout;
        }
    }

    /**
     * Server configuration class.
     * Defines properties for the gRPC proxy server.
     */
    public static class ServerConfig {
        /**
         * The port number on which the gRPC proxy server will listen.
         * Defaults to 8080 if not specified.
         */
        private int port = 8080; // default to 8080

        /**
         * Returns the server port.
         *
         * @return Server port number
         */
        public int getPort() {
            return port;
        }

        /**
         * Sets the server port.
         *
         * @param port Server port number
         */
        public void setPort(int port) {
            this.port = port;
        }
    }

    /**
     * Timeout configuration class.
     * Defines timeout settings for the gRPC connections.
     */
    public static class TimeoutConfig {
        /**
         * Connection timeout in seconds. Default is 10 seconds.
         */
        private int connectTimeoutSeconds = 10;

        /**
         * Send timeout in seconds. Default is 5 seconds.
         */
        private int sendTimeoutSeconds = 5;

        /**
         * Read timeout in seconds. Default is 30 seconds.
         */
        private int readTimeoutSeconds = 30;

        /**
         * Returns the connection timeout in seconds.
         *
         * @return Connection timeout in seconds
         */
        public int getConnectTimeoutSeconds() {
            return connectTimeoutSeconds;
        }

        /**
         * Sets the connection timeout in seconds.
         *
         * @param connectTimeoutSeconds Connection timeout in seconds
         */
        public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
            this.connectTimeoutSeconds = connectTimeoutSeconds;
        }

        /**
         * Returns the send timeout in seconds.
         *
         * @return Send timeout in seconds
         */
        public int getSendTimeoutSeconds() {
            return sendTimeoutSeconds;
        }

        /**
         * Sets the send timeout in seconds.
         *
         * @param sendTimeoutSeconds Send timeout in seconds
         */
        public void setSendTimeoutSeconds(int sendTimeoutSeconds) {
            this.sendTimeoutSeconds = sendTimeoutSeconds;
        }

        /**
         * Returns the read timeout in seconds.
         *
         * @return Read timeout in seconds
         */
        public int getReadTimeoutSeconds() {
            return readTimeoutSeconds;
        }

        /**
         * Sets the read timeout in seconds.
         *
         * @param readTimeoutSeconds Read timeout in seconds
         */
        public void setReadTimeoutSeconds(int readTimeoutSeconds) {
            this.readTimeoutSeconds = readTimeoutSeconds;
        }
    }

    /**
     * TLS configuration class.
     * Defines TLS settings for the gRPC connections.
     */
    public static class TlsConfig {
        /**
         * Whether to enable TLS for the gRPC proxy server.
         * Default is false.
         */
        private boolean enabled = false;

        /**
         * X509 certificate string in PEM format.
         */
        private String certContent;

        /**
         * Private key string in PEM format.
         */
        private String keyContent;

        /**
         * Whether to auto-trust all server certificates when connecting to upstream services.
         * This is insecure and should only be used for testing.
         * Default is false.
         */
        private boolean autoTrustUpstreamCertificates = false;

        /**
         * Trusted CA certificates string in PEM format.
         */
        private String trustCertsContent;

        /**
         * Returns whether TLS is enabled.
         *
         * @return true if TLS is enabled, false otherwise
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets whether TLS is enabled.
         *
         * @param enabled true to enable TLS, false to disable
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Returns the X509 certificate content.
         *
         * @return Certificate content in PEM format
         */
        public String getCertContent() {
            return certContent;
        }

        /**
         * Sets the X509 certificate content.
         *
         * @param certContent Certificate content in PEM format
         */
        public void setCertContent(String certContent) {
            this.certContent = certContent;
        }

        /**
         * Returns the private key content.
         *
         * @return Private key content in PEM format
         */
        public String getKeyContent() {
            return keyContent;
        }

        /**
         * Sets the private key content.
         *
         * @param keyContent Private key content in PEM format
         */
        public void setKeyContent(String keyContent) {
            this.keyContent = keyContent;
        }

        /**
         * Returns whether to auto-trust all upstream server certificates.
         *
         * @return true if auto-trust is enabled, false otherwise
         */
        public boolean isAutoTrustUpstreamCertificates() {
            return autoTrustUpstreamCertificates;
        }

        /**
         * Sets whether to auto-trust all upstream server certificates.
         *
         * @param autoTrustUpstreamCertificates true to enable auto-trust, false otherwise
         */
        public void setAutoTrustUpstreamCertificates(boolean autoTrustUpstreamCertificates) {
            this.autoTrustUpstreamCertificates = autoTrustUpstreamCertificates;
        }

        /**
         * Returns the trusted CA certificates content.
         *
         * @return Trusted CA certificates content in PEM format
         */
        public String getTrustCertsContent() {
            return trustCertsContent;
        }

        /**
         * Sets the trusted CA certificates content.
         *
         * @param trustCertsContent Trusted CA certificates content in PEM format
         */
        public void setTrustCertsContent(String trustCertsContent) {
            this.trustCertsContent = trustCertsContent;
        }
    }
}