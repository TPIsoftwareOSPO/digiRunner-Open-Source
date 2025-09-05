package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0290Req;
import tpi.dgrv4.dpaa.vo.DPB0290Resp;
import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;
import tpi.dgrv4.entity.repository.DgrGrpcProxyMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.grpcproxy.manager.DynamicGrpcProxyManager;
import tpi.dgrv4.grpcproxy.security.TlsCertificateManager;

@Service
public class DPB0290Service {

    private final DgrGrpcProxyMapDao dgrGrpcProxyMapDao;
    private final DynamicGrpcProxyManager dynamicGrpcProxyManager;
    private final TlsCertificateManager tlsCertificateManager;
    
    private static final String TARGET_PORT = "targetPort";

    @Autowired
    public DPB0290Service(DgrGrpcProxyMapDao dgrGrpcProxyMapDao,
                          DynamicGrpcProxyManager dynamicGrpcProxyManager,
                          TlsCertificateManager tlsCertificateManager) {
        super();
        this.dgrGrpcProxyMapDao = dgrGrpcProxyMapDao;
        this.dynamicGrpcProxyManager = dynamicGrpcProxyManager;
        this.tlsCertificateManager = tlsCertificateManager;
    }

    @Transactional
    public DgrGrpcProxyMap createGrpcProxyWithImport(TsmpAuthorization authorization, DPB0290Req req) {
        try {
            // 檢查基本參數
            checkParam(req);

            // 驗證 TLS 證書（如果有提供）
            validateTlsCertificates(req);

            // 創建代理映射並儲存到資料庫
            DgrGrpcProxyMap dgrGrpcProxyMap = createGrpcProxyMap(authorization.getUserName(), req);

            // 註冊到動態代理管理器
            // 這將發布事件，觸發 GrpcProxyServer 重啟
            dynamicGrpcProxyManager.addProxyMapping(dgrGrpcProxyMap);

            return dgrGrpcProxyMap;
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    /**
     * 創建新的 gRPC 代理映射
     *
     * @param authorization 授權信息
     * @param req 請求對象
     * @return 響應對象
     */
    @Transactional
    public DPB0290Resp createGrpcProxy(TsmpAuthorization authorization, DPB0290Req req) {
        try {
            // 檢查基本參數
            checkParam(req);

            // 驗證 TLS 證書（如果有提供）
            validateTlsCertificates(req);

            // 創建代理映射並儲存到資料庫
            DgrGrpcProxyMap dgrGrpcProxyMap = createGrpcProxyMap(authorization.getUserName(), req);

            // 註冊到動態代理管理器
            // 這將發布事件，觸發 GrpcProxyServer 重啟
            dynamicGrpcProxyManager.addProxyMapping(dgrGrpcProxyMap);

            return getResp(dgrGrpcProxyMap);
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    /**
     * 驗證 TLS 相關的證書內容
     *
     * @param req 請求對象
     * @throws TsmpDpAaException 如果證書無效或缺少必需的配置
     */
    protected void validateTlsCertificates(DPB0290Req req) {
        // 檢查如果有提供服務器證書，那麼必須同時提供私鑰
        if ((req.getServerCertContent() != null && !req.getServerCertContent().isEmpty()) &&
                (req.getServerKeyContent() == null || req.getServerKeyContent().isEmpty())) {
            throw TsmpDpAaRtnCode._1559.throwing("Server private key is required when server certificate is provided");
        }

        // 檢查如果有提供私鑰，那麼必須同時提供服務器證書
        if ((req.getServerKeyContent() != null && !req.getServerKeyContent().isEmpty()) &&
                (req.getServerCertContent() == null || req.getServerCertContent().isEmpty())) {
            throw TsmpDpAaRtnCode._1559.throwing("Server certificate is required when server private key is provided");
        }

        // 如果有提供服務器證書，驗證其有效性
        if (req.getServerCertContent() != null && !req.getServerCertContent().isEmpty()) {
            boolean isValid = tlsCertificateManager.validateCertificate(req.getServerCertContent());
            if (!isValid) {
                throw TsmpDpAaRtnCode._1352.throwing("Server certificate");
            }
        }

        // 檢查安全模式是否有效
        String secureMode = req.getSecureMode();
        if (secureMode != null && !secureMode.isEmpty() &&
                !secureMode.equals("AUTO") && !secureMode.equals("SECURE") && !secureMode.equals("PLAINTEXT")) {
            throw TsmpDpAaRtnCode._1559.throwing("Invalid secure mode. Must be AUTO, SECURE or PLAINTEXT");
        }

        // 安全模式啟用時檢查證書是否存在
        String trustedCertsContent = req.getTrustedCertsContent();
        if("SECURE".equals(secureMode) && "N".equals(req.getAutoTrustUpstreamCerts())) {
            if(!StringUtils.hasText(trustedCertsContent)) {
            	throw TsmpDpAaRtnCode._1350.throwing("{{trustedCertsContent}}");
            }else {
            	boolean isValid = tlsCertificateManager.validateCertificate(trustedCertsContent);
                if (!isValid) {
                    throw TsmpDpAaRtnCode._1352.throwing("{{trustedCertsContent}}");
                }
            }
        }
    }

    /**
     * 檢查請求參數是否有效
     *
     * @param req 請求對象
     * @throws TsmpDpAaException 如果參數無效
     */
    public void checkParam(DPB0290Req req) {
        String serviceName = req.getServiceName();
        String proxyHostName = req.getProxyHostName();
        String targetHostName = req.getTargetHostName();
        String targetPort = req.getTargetPort();
        String connectTimeout = req.getConnectTimeoutMs();
        String readTimeout = req.getReadTimeoutMs();
        String sendTimeout = req.getSendTimeoutMs();
        String enable = req.getEnable();

        if (serviceName == null || serviceName.isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing("serviceName");
        }

        if (proxyHostName == null || proxyHostName.isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing("proxyHostName");
        }

        if (targetHostName == null || targetHostName.isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing("targetHostName");
        }

        if (targetPort == null || targetPort.isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing(TARGET_PORT);
        }

        if (connectTimeout == null || connectTimeout.isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing("connectTimeoutMs");
        }

        if (readTimeout == null || readTimeout.isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing("readTimeoutMs");
        }

        if (sendTimeout == null || sendTimeout.isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing("sendTimeoutMs");
        }

        if (enable == null || enable.isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing("enable");
        }

        // 檢查數值型參數是否有效
        try {
            int portNum = Integer.parseInt(targetPort);
            if (portNum <= 0) {
                throw TsmpDpAaRtnCode._1355.throwing(TARGET_PORT, "1", String.valueOf(portNum));
            }
            if (portNum > 65535) {
                throw TsmpDpAaRtnCode._1356.throwing(TARGET_PORT, "65535", String.valueOf(portNum));
            }

            int connectTimeoutMs = Integer.parseInt(connectTimeout);
            if (connectTimeoutMs <= 0) {
                throw TsmpDpAaRtnCode._1355.throwing("connectTimeoutMs", "1", String.valueOf(connectTimeoutMs));
            }

            int readTimeoutMs = Integer.parseInt(readTimeout);
            if (readTimeoutMs <= 0) {
                throw TsmpDpAaRtnCode._1355.throwing("readTimeoutMs", "1", String.valueOf(readTimeoutMs));
            }

            int sendTimeoutMs = Integer.parseInt(sendTimeout);
            if (sendTimeoutMs <= 0) {
                throw TsmpDpAaRtnCode._1355.throwing("sendTimeoutMs", "1", String.valueOf(sendTimeoutMs));
            }
        } catch (NumberFormatException e) {
            throw TsmpDpAaRtnCode._2021.throwing();
        }

        // 檢查是否已存在相同的映射
        DgrGrpcProxyMap dgrGrpcProxyMap = getDgrGrpcProxyMapDao().findFirstByProxyHostName(proxyHostName);
        if (dgrGrpcProxyMap != null) {
        	//因為匯入也會呼叫,所以不做{{proxyHostName}}
            throw TsmpDpAaRtnCode._1353.throwing("Proxy hostname/domain name", proxyHostName);
        }
    }

    /**
     * 創建代理映射實體並保存到資料庫
     *
     * @param username 創建用戶名
     * @param req 請求對象
     * @return 創建的代理映射實體
     */
    public DgrGrpcProxyMap createGrpcProxyMap(String username, DPB0290Req req) {
        String serviceName = req.getServiceName();
        String proxyHostName = req.getProxyHostName();
        String targetHostName = req.getTargetHostName();
        String targetPort = req.getTargetPort();
        String connectTimeout = req.getConnectTimeoutMs();
        String readTimeout = req.getReadTimeoutMs();
        String sendTimeout = req.getSendTimeoutMs();
        String secureMode = req.getSecureMode();
        String enable = req.getEnable();
        String serverCertContent = req.getServerCertContent();
        String serverKeyContent = req.getServerKeyContent();
        String autoTrustUpstreamCerts = req.getAutoTrustUpstreamCerts();
        String trustedCertsContent = req.getTrustedCertsContent();

        DgrGrpcProxyMap dgrGrpcProxyMap = new DgrGrpcProxyMap();
        dgrGrpcProxyMap.setServiceName(serviceName);
        dgrGrpcProxyMap.setProxyHostName(proxyHostName);
        dgrGrpcProxyMap.setTargetHostName(targetHostName);
        dgrGrpcProxyMap.setTargetPort(Integer.parseInt(targetPort));
        dgrGrpcProxyMap.setConnectTimeoutMs(Integer.parseInt(connectTimeout));
        dgrGrpcProxyMap.setReadTimeoutMs(Integer.parseInt(readTimeout));
        dgrGrpcProxyMap.setSendTimeoutMs(Integer.parseInt(sendTimeout));

        // 設置安全模式，默認為 AUTO
        if (secureMode != null && !secureMode.isEmpty()) {
            dgrGrpcProxyMap.setSecureMode(secureMode);
        } else {
            dgrGrpcProxyMap.setSecureMode("AUTO");
        }

        // 設置 TLS 相關內容
        if (serverCertContent != null && !serverCertContent.isEmpty()) {
            dgrGrpcProxyMap.setServerCertContent(serverCertContent);
        }

        if (serverKeyContent != null && !serverKeyContent.isEmpty()) {
            dgrGrpcProxyMap.setServerKeyContent(serverKeyContent);
        }

        // 設置是否自動信任上游證書
        if ("Y".equals(autoTrustUpstreamCerts)) {
            dgrGrpcProxyMap.setAutoTrustUpstreamCerts("Y");
        } else {
            dgrGrpcProxyMap.setAutoTrustUpstreamCerts("N");
        }

        // 設置信任的 CA 證書內容
        if (trustedCertsContent != null && !trustedCertsContent.isEmpty()) {
            dgrGrpcProxyMap.setTrustedCertsContent(trustedCertsContent);
        }

        dgrGrpcProxyMap.setCreateUser(username);
        dgrGrpcProxyMap.setEnable(enable);

        getDgrGrpcProxyMapDao().save(dgrGrpcProxyMap);
        return dgrGrpcProxyMap;
    }

    /**
     * 刷新所有代理映射
     */
    public void refreshProxies() {
        dynamicGrpcProxyManager.refreshProxyMappings();
    }

    /**
     * 從代理映射實體創建響應對象
     *
     * @param dgrGrpcProxyMap 代理映射實體
     * @return 響應對象
     */
    public DPB0290Resp getResp(DgrGrpcProxyMap dgrGrpcProxyMap) {
        DPB0290Resp resp = new DPB0290Resp();
        resp.setgRPCProxyMapId(String.valueOf(dgrGrpcProxyMap.getGrpcproxyMapId()));
        resp.setCreateUser(dgrGrpcProxyMap.getCreateUser());
        resp.setCreateDateTime(String.valueOf(dgrGrpcProxyMap.getCreateDateTime().getTime()));
        resp.setUpdateUser(dgrGrpcProxyMap.getUpdateUser());
        resp.setUpdateDateTime(null);
        resp.setServiceName(dgrGrpcProxyMap.getServiceName());
        resp.setProxyHostName(dgrGrpcProxyMap.getProxyHostName());
        resp.setTargetHostName(dgrGrpcProxyMap.getTargetHostName());
        resp.setTargetPort(String.valueOf(dgrGrpcProxyMap.getTargetPort()));
        resp.setSecureMode(dgrGrpcProxyMap.getSecureMode());
        return resp;
    }

    /**
     * 獲取 gRPC 代理映射 DAO
     *
     * @return gRPC 代理映射 DAO
     */
    protected DgrGrpcProxyMapDao getDgrGrpcProxyMapDao() {
        return this.dgrGrpcProxyMapDao;
    }
}