package tpi.dgrv4.dpaa.vo;

/**
 * 更新 gRPC 代理映射的響應類
 */
public class DPB0292Resp {
    private String gRPCProxyMapId;        // 代理映射 ID
    private String serviceName;           // 服務名稱
    private String proxyHostName;         // 代理主機名
    private String targetHostName;        // 目標主機名
    private String targetPort;            // 目標端口
    private String connectTimeoutMs;      // 連接超時時間（毫秒）
    private String sendTimeoutMs;         // 發送超時時間（毫秒）
    private String readTimeoutMs;         // 讀取超時時間（毫秒）
    private String secureMode;            // 安全模式 (AUTO, SECURE, PLAINTEXT)
    private String autoTrustUpstreamCerts; // 是否自動信任上游證書 (Y/N)
    private String enable;                // 是否啟用 (Y/N)
    private String updateDateTime;        // 更新時間
    private String updateUser;            // 更新用戶

    /**
     * 獲取代理映射 ID
     * @return 代理映射 ID
     */
    public String getgRPCProxyMapId() {
        return gRPCProxyMapId;
    }

    /**
     * 設置代理映射 ID
     * @param gRPCProxyMapId 代理映射 ID
     */
    public void setgRPCProxyMapId(String gRPCProxyMapId) {
        this.gRPCProxyMapId = gRPCProxyMapId;
    }

    /**
     * 獲取服務名稱
     * @return 服務名稱
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 設置服務名稱
     * @param serviceName 服務名稱
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * 獲取代理主機名
     * @return 代理主機名
     */
    public String getProxyHostName() {
        return proxyHostName;
    }

    /**
     * 設置代理主機名
     * @param proxyHostName 代理主機名
     */
    public void setProxyHostName(String proxyHostName) {
        this.proxyHostName = proxyHostName;
    }

    /**
     * 獲取目標主機名
     * @return 目標主機名
     */
    public String getTargetHostName() {
        return targetHostName;
    }

    /**
     * 設置目標主機名
     * @param targetHostName 目標主機名
     */
    public void setTargetHostName(String targetHostName) {
        this.targetHostName = targetHostName;
    }

    /**
     * 獲取目標端口
     * @return 目標端口
     */
    public String getTargetPort() {
        return targetPort;
    }

    /**
     * 設置目標端口
     * @param targetPort 目標端口
     */
    public void setTargetPort(String targetPort) {
        this.targetPort = targetPort;
    }

    /**
     * 獲取連接超時時間（毫秒）
     * @return 連接超時時間
     */
    public String getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    /**
     * 設置連接超時時間（毫秒）
     * @param connectTimeoutMs 連接超時時間
     */
    public void setConnectTimeoutMs(String connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    /**
     * 獲取發送超時時間（毫秒）
     * @return 發送超時時間
     */
    public String getSendTimeoutMs() {
        return sendTimeoutMs;
    }

    /**
     * 設置發送超時時間（毫秒）
     * @param sendTimeoutMs 發送超時時間
     */
    public void setSendTimeoutMs(String sendTimeoutMs) {
        this.sendTimeoutMs = sendTimeoutMs;
    }

    /**
     * 獲取讀取超時時間（毫秒）
     * @return 讀取超時時間
     */
    public String getReadTimeoutMs() {
        return readTimeoutMs;
    }

    /**
     * 設置讀取超時時間（毫秒）
     * @param readTimeoutMs 讀取超時時間
     */
    public void setReadTimeoutMs(String readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    /**
     * 獲取安全模式
     * @return 安全模式 (AUTO, SECURE, PLAINTEXT)
     */
    public String getSecureMode() {
        return secureMode;
    }

    /**
     * 設置安全模式
     * @param secureMode 安全模式
     */
    public void setSecureMode(String secureMode) {
        this.secureMode = secureMode;
    }

    /**
     * 獲取是否自動信任上游證書
     * @return Y表示自動信任，N表示不自動信任
     */
    public String getAutoTrustUpstreamCerts() {
        return autoTrustUpstreamCerts;
    }

    /**
     * 設置是否自動信任上游證書
     * @param autoTrustUpstreamCerts Y表示自動信任，N表示不自動信任
     */
    public void setAutoTrustUpstreamCerts(String autoTrustUpstreamCerts) {
        this.autoTrustUpstreamCerts = autoTrustUpstreamCerts;
    }

    /**
     * 獲取是否啟用
     * @return Y表示啟用，N表示不啟用
     */
    public String getEnable() {
        return enable;
    }

    /**
     * 設置是否啟用
     * @param enable Y表示啟用，N表示不啟用
     */
    public void setEnable(String enable) {
        this.enable = enable;
    }

    /**
     * 獲取更新時間
     * @return 更新時間
     */
    public String getUpdateDateTime() {
        return updateDateTime;
    }

    /**
     * 設置更新時間
     * @param updateDateTime 更新時間
     */
    public void setUpdateDateTime(String updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    /**
     * 獲取更新用戶
     * @return 更新用戶
     */
    public String getUpdateUser() {
        return updateUser;
    }

    /**
     * 設置更新用戶
     * @param updateUser 更新用戶
     */
    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    @Override
    public String toString() {
        return "DPB0292Resp{" +
                "gRPCProxyMapId='" + gRPCProxyMapId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", proxyHostName='" + proxyHostName + '\'' +
                ", targetHostName='" + targetHostName + '\'' +
                ", targetPort='" + targetPort + '\'' +
                ", connectTimeoutMs='" + connectTimeoutMs + '\'' +
                ", sendTimeoutMs='" + sendTimeoutMs + '\'' +
                ", readTimeoutMs='" + readTimeoutMs + '\'' +
                ", secureMode='" + secureMode + '\'' +
                ", autoTrustUpstreamCerts='" + autoTrustUpstreamCerts + '\'' +
                ", enable='" + enable + '\'' +
                ", updateDateTime='" + updateDateTime + '\'' +
                ", updateUser='" + updateUser + '\'' +
                '}';
    }
}