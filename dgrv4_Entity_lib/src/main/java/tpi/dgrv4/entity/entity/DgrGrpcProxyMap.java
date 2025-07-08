package tpi.dgrv4.entity.entity;

import jakarta.persistence.*;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

import java.util.Date;

@Entity
@Table(name = "dgr_grpcproxy_map")
public class DgrGrpcProxyMap implements DgrSequenced{
    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "grpcproxy_map_id")
    private Long grpcproxyMapId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "proxy_hostname")
    private String proxyHostName;

    @Column(name = "target_hostname")
    private String targetHostName;

    @Column(name = "target_port")
    private int targetPort;

    @Column(name = "connect_timeout_ms")
    private int connectTimeoutMs = 5000;

    @Column(name = "send_timeout_ms")
    private int sendTimeoutMs = 10000;

    @Column(name = "read_timeout_ms")
    private int readTimeoutMs = 30000;

    /**
     * Secure mode for this mapping.
     * Values: "AUTO", "SECURE", "PLAINTEXT"
     * - AUTO: Automatically detect if the target service uses TLS
     * - SECURE: Always use TLS to connect to the target service
     * - PLAINTEXT: Never use TLS to connect to the target service
     */
    @Column(name = "secure_mode")
    private String secureMode = "AUTO";

    /**
     * X509 certificate content in PEM format for server TLS (from client to proxy)
     */
    @Column(name = "server_cert_content", columnDefinition = "TEXT")
    private String serverCertContent;

    /**
     * Private key content in PEM format for server TLS
     */
    @Column(name = "server_key_content", columnDefinition = "TEXT")
    private String serverKeyContent;

    /**
     * Whether to auto-trust all upstream server certificates
     */
    @Column(name = "auto_trust_upstream_certs")
    private String autoTrustUpstreamCerts = "N";

    /**
     * Trusted CA certificates content in PEM format for client TLS (from proxy to upstream)
     */
    @Column(name = "trusted_certs_content", columnDefinition = "TEXT")
    private String trustedCertsContent;

    @Column(name = "enable")
    private String enable = "N";

    @Column(name = "create_date_time")
    private Date createDateTime = DateTimeUtil.now();

    @Column(name = "create_user")
    private String createUser = "SYSTEM";

    @Column(name = "update_date_time")
    private Date updateDateTime;

    @Column(name = "update_user")
    private String updateUser;

    @Version
    @Column(name = "version")
    private Long version = 1L;

    @Override
    public Long getPrimaryKey() {
        return grpcproxyMapId;
    }

    public Long getGrpcproxyMapId() {
        return grpcproxyMapId;
    }

    public void setGrpcproxyMapId(Long grpcproxyMapId) {
        this.grpcproxyMapId = grpcproxyMapId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProxyHostName() {
        return proxyHostName;
    }

    public void setProxyHostName(String proxyHostname) {
        this.proxyHostName = proxyHostname;
    }

    public String getTargetHostName() {
        return targetHostName;
    }

    public void setTargetHostName(String targetHostName) {
        this.targetHostName = targetHostName;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getSendTimeoutMs() {
        return sendTimeoutMs;
    }

    public void setSendTimeoutMs(int sendTimeoutMs) {
        this.sendTimeoutMs = sendTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public String getSecureMode() {
        return secureMode;
    }

    public void setSecureMode(String secureMode) {
        this.secureMode = secureMode;
    }

    public String getServerCertContent() {
        return serverCertContent;
    }

    public void setServerCertContent(String serverCertContent) {
        this.serverCertContent = serverCertContent;
    }

    public String getServerKeyContent() {
        return serverKeyContent;
    }

    public void setServerKeyContent(String serverKeyContent) {
        this.serverKeyContent = serverKeyContent;
    }

    public String isAutoTrustUpstreamCerts() {
        return autoTrustUpstreamCerts;
    }

    public void setAutoTrustUpstreamCerts(String autoTrustUpstreamCerts) {
        this.autoTrustUpstreamCerts = autoTrustUpstreamCerts;
    }

    public String getTrustedCertsContent() {
        return trustedCertsContent;
    }

    public void setTrustedCertsContent(String trustedCertsContent) {
        this.trustedCertsContent = trustedCertsContent;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public Date getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(Date createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(Date updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "DgrGrpcProxyMap{" +
                "grpcproxyMapId=" + grpcproxyMapId +
                ", serviceName='" + serviceName + '\'' +
                ", proxyHostname='" + proxyHostName + '\'' +
                ", targetHostname='" + targetHostName + '\'' +
                ", targetPort=" + targetPort +
                ", connectTimeoutMs=" + connectTimeoutMs +
                ", sendTimeoutMs=" + sendTimeoutMs +
                ", readTimeoutMs=" + readTimeoutMs +
                ", secureMode='" + secureMode + '\'' +
                ", serverCertContent='" + (serverCertContent != null ? "[PROTECTED]" : "null") + '\'' +
                ", serverKeyContent='" + (serverKeyContent != null ? "[PROTECTED]" : "null") + '\'' +
                ", autoTrustUpstreamCerts=" + autoTrustUpstreamCerts +
                ", trustedCertsContent='" + (trustedCertsContent != null ? "[PROTECTED]" : "null") + '\'' +
                ", enable='" + enable + '\'' +
                ", createDateTime=" + createDateTime +
                ", createUser='" + createUser + '\'' +
                ", updateDateTime=" + updateDateTime +
                ", updateUser='" + updateUser + '\'' +
                ", version=" + version +
                '}';
    }
}