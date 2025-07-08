package tpi.dgrv4.dpaa.vo;

public class DPB0294RespItem {
    private String gRPCProxyMapId;
    private String serviceName;
    private String proxyHostName;
    private String targetHostName;
    private String targetPort;
    private String connectTimeoutMs;
    private String readTimeoutMs;
    private String sendTimeoutMs;
    private String secureMode;            // 安全模式 (AUTO, SECURE, PLAINTEXT)
    private String serverCertContent;     // 服務器 X509 憑證內容 (PEM 格式)
    private String serverKeyContent;      // 服務器私鑰內容 (PEM 格式)
    private String autoTrustUpstreamCerts; // 是否自動信任上游證書 (Y/N)
    private String trustedCertsContent;    // 信任的 CA 證書內容 (PEM 格式)
    private String enable;
    private String createDateTime;
    private String createUser;
    private String updateDateTime;
    private String updateUser;

    public String getgRPCProxyMapId() {
        return gRPCProxyMapId;
    }

    public void setgRPCProxyMapId(String gRPCProxyMapId) {
        this.gRPCProxyMapId = gRPCProxyMapId;
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

    public void setProxyHostName(String proxyHostName) {
        this.proxyHostName = proxyHostName;
    }

    public String getTargetHostName() {
        return targetHostName;
    }

    public void setTargetHostName(String targetHostName) {
        this.targetHostName = targetHostName;
    }

    public String getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(String targetPort) {
        this.targetPort = targetPort;
    }

    public String getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(String connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public String getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(String readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public String getSendTimeoutMs() {
        return sendTimeoutMs;
    }

    public void setSendTimeoutMs(String sendTimeoutMs) {
        this.sendTimeoutMs = sendTimeoutMs;
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

    public String getAutoTrustUpstreamCerts() {
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

    public String getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(String updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }
}
