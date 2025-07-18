package tpi.dgrv4.dpaa.vo;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.BeanUtils;

import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;

/**
 * DTO for DgrGrpcProxyMap entity
 * Compatible with BeanUtils.copyProperties for property copying
 */
public class DgrGrpcProxyMapDto {

    private String grpcproxyMapId;
    private String serviceName;
    private String proxyHostName;
    private String targetHostName;
    private String targetPort;
    private String connectTimeoutMs = "5000";
    private String sendTimeoutMs = "10000";
    private String readTimeoutMs = "30000";

    /**
     * Secure mode for this mapping.
     * Values: "AUTO", "SECURE", "PLAINTEXT"
     * - AUTO: Automatically detect if the target service uses TLS
     * - SECURE: Always use TLS to connect to the target service
     * - PLAINTEXT: Never use TLS to connect to the target service
     */
    private String secureMode = "AUTO";

    /**
     * X509 certificate content in PEM format for server TLS (from client to proxy)
     */
    private String serverCertContent;

    /**
     * Private key content in PEM format for server TLS
     */
    private String serverKeyContent;

    /**
     * Whether to auto-trust all upstream server certificates
     */
    private String autoTrustUpstreamCerts = "N";

    /**
     * Trusted CA certificates content in PEM format for client TLS (from proxy to
     * upstream)
     */
    private String trustedCertsContent;

    private String enable = "N";
    private String createDateTime;
    private String createUser = "SYSTEM";
    private String updateDateTime;
    private String updateUser;
    private String version = "1";

    private boolean create = false;

    private boolean update = false;

    private Boolean success = null;

    private String errorMessage = null;

    private String note = null;

    private String uuid = UUID.randomUUID().toString();

    // Default constructor
    public DgrGrpcProxyMapDto() {
    }

    public DgrGrpcProxyMapDto(DgrGrpcProxyMap dgrGrpcProxyMap) {
        if (dgrGrpcProxyMap != null) {
            // 處理 ID 型別轉換 (Long -> String)
            if (dgrGrpcProxyMap.getGrpcproxyMapId() != null) {
                this.grpcproxyMapId = dgrGrpcProxyMap.getGrpcproxyMapId().toString();
            }

            // 複製其他基本欄位
            this.serviceName = dgrGrpcProxyMap.getServiceName();
            this.proxyHostName = dgrGrpcProxyMap.getProxyHostName();
            this.targetHostName = dgrGrpcProxyMap.getTargetHostName();
            this.targetPort = String.valueOf(dgrGrpcProxyMap.getTargetPort());
            this.connectTimeoutMs = String.valueOf(dgrGrpcProxyMap.getConnectTimeoutMs());
            this.sendTimeoutMs = String.valueOf(dgrGrpcProxyMap.getSendTimeoutMs());
            this.readTimeoutMs = String.valueOf(dgrGrpcProxyMap.getReadTimeoutMs());
            this.secureMode = dgrGrpcProxyMap.getSecureMode();
            this.serverCertContent = dgrGrpcProxyMap.getServerCertContent();
            this.serverKeyContent = dgrGrpcProxyMap.getServerKeyContent();

            this.autoTrustUpstreamCerts = dgrGrpcProxyMap.isAutoTrustUpstreamCerts();

            this.trustedCertsContent = dgrGrpcProxyMap.getTrustedCertsContent();
            this.enable = dgrGrpcProxyMap.getEnable();
            this.createDateTime = dgrGrpcProxyMap.getCreateDateTime() != null ? dgrGrpcProxyMap.getCreateDateTime().toString() : null;
            this.createUser = dgrGrpcProxyMap.getCreateUser();
            this.updateDateTime = dgrGrpcProxyMap.getUpdateDateTime() != null ? dgrGrpcProxyMap.getUpdateDateTime().toString() : null;
            this.updateUser = dgrGrpcProxyMap.getUpdateUser();
            this.version = String.valueOf(dgrGrpcProxyMap.getVersion());

            // DTO 特有的欄位保持預設值
            // create, update, success, errorMessage, note ,uuid 已在欄位宣告時設定預設值
        }
    }

    /**
     * 將 DTO 轉換為 DPB0290Req 物件
     * 忽略 DPB0290Req 中不存在的屬性
     * 
     * @return DPB0290Req 物件
     */
    public DPB0290Req toDPB0290Req() {
        DPB0290Req req = new DPB0290Req();

        // 直接複製字串屬性
        req.setServiceName(this.serviceName);
        req.setProxyHostName(this.proxyHostName);
        req.setTargetHostName(this.targetHostName);
        req.setSecureMode(this.secureMode);
        req.setServerCertContent(this.serverCertContent);
        req.setServerKeyContent(this.serverKeyContent);
        req.setAutoTrustUpstreamCerts(this.autoTrustUpstreamCerts);
        req.setTrustedCertsContent(this.trustedCertsContent);
        req.setEnable(this.enable);

        // 數值型別轉換為字串
        req.setTargetPort(this.targetPort);
        req.setConnectTimeoutMs(this.connectTimeoutMs);
        req.setSendTimeoutMs(this.sendTimeoutMs);
        req.setReadTimeoutMs(this.readTimeoutMs);

        return req;
    }

    /**
     * 將 DTO 轉換為 DPB0292Req 物件
     * 忽略 DPB0292Req 中不存在的屬性
     * 
     * @return DPB0292Req 物件
     */
    public DPB0292Req toDPB0292Req() {
        DPB0292Req req = new DPB0292Req();

        // 特殊屬性名稱對應
        req.setgRPCProxyMapId(this.grpcproxyMapId);

        // 直接複製字串屬性
        req.setServiceName(this.serviceName);
        req.setProxyHostName(this.proxyHostName);
        req.setTargetHostName(this.targetHostName);
        req.setTargetPort(this.targetPort);
        req.setConnectTimeoutMs(this.connectTimeoutMs);
        req.setSendTimeoutMs(this.sendTimeoutMs);
        req.setReadTimeoutMs(this.readTimeoutMs);
        req.setSecureMode(this.secureMode);
        req.setServerCertContent(this.serverCertContent);
        req.setServerKeyContent(this.serverKeyContent);
        req.setAutoTrustUpstreamCerts(this.autoTrustUpstreamCerts);
        req.setTrustedCertsContent(this.trustedCertsContent);
        req.setEnable(this.enable);

        return req;
    }

    // Getters and Setters
    public String getGrpcproxyMapId() {
        return grpcproxyMapId;
    }

    public void setGrpcproxyMapId(String grpcproxyMapId) {
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

    public String getSendTimeoutMs() {
        return sendTimeoutMs;
    }

    public void setSendTimeoutMs(String sendTimeoutMs) {
        this.sendTimeoutMs = sendTimeoutMs;
    }

    public String getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(String readTimeoutMs) {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "DgrGrpcProxyMapDto{" +
                "grpcproxyMapId=" + grpcproxyMapId +
                ", serviceName='" + serviceName + '\'' +
                ", proxyHostName='" + proxyHostName + '\'' +
                ", targetHostName='" + targetHostName + '\'' +
                ", targetPort=" + targetPort +
                ", connectTimeoutMs=" + connectTimeoutMs +
                ", sendTimeoutMs=" + sendTimeoutMs +
                ", readTimeoutMs=" + readTimeoutMs +
                ", secureMode='" + secureMode + '\'' +
                ", serverCertContent='" + (serverCertContent != null ? "[PROTECTED]" : "null") + '\'' +
                ", serverKeyContent='" + (serverKeyContent != null ? "[PROTECTED]" : "null") + '\'' +
                ", autoTrustUpstreamCerts='" + autoTrustUpstreamCerts + '\'' +
                ", trustedCertsContent='" + (trustedCertsContent != null ? "[PROTECTED]" : "null") + '\'' +
                ", enable='" + enable + '\'' +
                ", createDateTime=" + createDateTime +
                ", createUser='" + createUser + '\'' +
                ", updateDateTime=" + updateDateTime +
                ", updateUser='" + updateUser + '\'' +
                ", version=" + version +
                ", create=" + create +
                ", update=" + update +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                ", note='" + note + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}