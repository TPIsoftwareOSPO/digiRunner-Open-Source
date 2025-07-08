package tpi.dgrv4.dpaa.vo;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

import java.util.Arrays;
import java.util.List;

/**
 * 創建 gRPC 代理的請求類
 * 包含基本代理設定和 TLS 安全設定
 */
public class DPB0290Req extends ReqValidator {
    private String serviceName;          // 服務名稱
    private String proxyHostName;        // 代理主機名
    private String targetHostName;       // 目標主機名
    private String targetPort;           // 目標端口
    private String connectTimeoutMs;     // 連接超時時間（毫秒）
    private String sendTimeoutMs;        // 發送超時時間（毫秒）
    private String readTimeoutMs;        // 讀取超時時間（毫秒）
    private String secureMode;           // 安全模式 (AUTO, SECURE, PLAINTEXT)
    private String serverCertContent;    // 服務器 X509 憑證內容 (PEM 格式)
    private String serverKeyContent;     // 服務器私鑰內容 (PEM 格式)
    private String autoTrustUpstreamCerts; // 是否自動信任上游證書 (Y/N)
    private String trustedCertsContent;    // 信任的 CA 證書內容 (PEM 格式)
    private String enable;               // 是否啟用 (Y/N)

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

    @Override
    public String toString() {
        return "DPB0290Req{" +
                "serviceName='" + serviceName + '\'' +
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