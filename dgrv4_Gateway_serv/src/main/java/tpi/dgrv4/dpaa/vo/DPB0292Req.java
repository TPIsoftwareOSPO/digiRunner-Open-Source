package tpi.dgrv4.dpaa.vo;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

import java.util.Arrays;
import java.util.List;

/**
 * 更新 gRPC 代理映射的請求類
 */
public class DPB0292Req extends ReqValidator {
    private String gRPCProxyMapId;        // 代理映射 ID
    private String serviceName;           // 服務名稱
    private String proxyHostName;         // 代理主機名
    private String targetHostName;        // 目標主機名
    private String targetPort;            // 目標端口
    private String connectTimeoutMs;      // 連接超時時間（毫秒）
    private String sendTimeoutMs;         // 發送超時時間（毫秒）
    private String readTimeoutMs;         // 讀取超時時間（毫秒）
    private String secureMode;            // 安全模式 (AUTO, SECURE, PLAINTEXT)
    private String serverCertContent;     // 服務器 X509 憑證內容 (PEM 格式)
    private String serverKeyContent;      // 服務器私鑰內容 (PEM 格式)
    private String autoTrustUpstreamCerts; // 是否自動信任上游證書 (Y/N)
    private String trustedCertsContent;    // 信任的 CA 證書內容 (PEM 格式)
    private String enable;                 // 是否啟用 (Y/N)

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
     * 可選值：AUTO（自動檢測）、SECURE（強制TLS）、PLAINTEXT（明文）
     * @return 安全模式
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
     * 獲取服務器證書內容（PEM格式）
     * @return 服務器證書內容
     */
    public String getServerCertContent() {
        return serverCertContent;
    }

    /**
     * 設置服務器證書內容
     * @param serverCertContent 服務器證書內容（X.509 PEM格式）
     */
    public void setServerCertContent(String serverCertContent) {
        this.serverCertContent = serverCertContent;
    }

    /**
     * 獲取服務器私鑰內容（PEM格式）
     * @return 服務器私鑰內容
     */
    public String getServerKeyContent() {
        return serverKeyContent;
    }

    /**
     * 設置服務器私鑰內容
     * @param serverKeyContent 服務器私鑰內容（PEM格式）
     */
    public void setServerKeyContent(String serverKeyContent) {
        this.serverKeyContent = serverKeyContent;
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
     * 獲取信任的CA證書內容
     * @return CA證書內容（PEM格式）
     */
    public String getTrustedCertsContent() {
        return trustedCertsContent;
    }

    /**
     * 設置信任的CA證書內容
     * @param trustedCertsContent CA證書內容（PEM格式）
     */
    public void setTrustedCertsContent(String trustedCertsContent) {
        this.trustedCertsContent = trustedCertsContent;
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

    @Override
    public String toString() {
        return "DPB0292Req{" +
                "gRPCProxyMapId=" + gRPCProxyMapId +
                ", serviceName='" + serviceName + '\'' +
                ", proxyHostName='" + proxyHostName + '\'' +
                ", targetHostName='" + targetHostName + '\'' +
                ", targetPort='" + targetPort + '\'' +
                ", connectTimeoutMs='" + connectTimeoutMs + '\'' +
                ", sendTimeoutMs='" + sendTimeoutMs + '\'' +
                ", readTimeoutMs='" + readTimeoutMs + '\'' +
                ", secureMode='" + secureMode + '\'' +
                ", serverCertContent='" + (serverCertContent != null ? "[PROTECTED]" : "null") + '\'' +
                ", serverKeyContent='" + (serverKeyContent != null ? "[PROTECTED]" : "null") + '\'' +
                ", autoTrustUpstreamCerts='" + autoTrustUpstreamCerts + '\'' +
                ", trustedCertsContent='" + (trustedCertsContent != null ? "[PROTECTED]" : "null") + '\'' +
                ", enable='" + enable + '\'' +
                '}';
    }

    @Override
    protected List<BeforeControllerRespItem> provideConstraints(String locale) {
        return Arrays.asList(new BeforeControllerRespItem[] { //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("gRPCProxyMapId") //
                        .isRequired() //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("serviceName") //
                        .isRequired() //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("proxyHostName") //
                        .isRequired() //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("targetHostName") //
                        .isRequired() //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("targetPort") //
                        .isRequired() //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("connectTimeoutMs") //
                        .isRequired() //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("sendTimeoutMs") //
                        .isRequired() //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("readTimeoutMs") //
                        .isRequired() //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("secureMode") //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("enable") //
                        .isRequired() //
                        .pattern(RegexpConstant.Y_OR_N, TsmpDpAaRtnCode._2007.getCode(), null) //
                        .build() //
        });

    }
}