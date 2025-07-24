package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0292Req;
import tpi.dgrv4.dpaa.vo.DPB0292Resp;
import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;
import tpi.dgrv4.entity.repository.DgrGrpcProxyMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.grpcproxy.manager.DynamicGrpcProxyManager;
import tpi.dgrv4.grpcproxy.security.TlsCertificateManager;

import java.util.Optional;

/**
 * 更新 gRPC 代理映射的服務類
 */
@Service
public class DPB0292Service {
    private final TPILogger logger = TPILogger.tl;
    private final DgrGrpcProxyMapDao dgrGrpcProxyMapDao;
    private final DynamicGrpcProxyManager dynamicGrpcProxyManager;
    private final TlsCertificateManager tlsCertificateManager;

    @Autowired
    public DPB0292Service(DgrGrpcProxyMapDao dgrGrpcProxyMapDao,
                          DynamicGrpcProxyManager dynamicGrpcProxyManager,
                          TlsCertificateManager tlsCertificateManager) {
        super();
        this.dgrGrpcProxyMapDao = dgrGrpcProxyMapDao;
        this.dynamicGrpcProxyManager = dynamicGrpcProxyManager;
        this.tlsCertificateManager = tlsCertificateManager;
    }

    /**
     * 更新 gRPC 代理映射
     *
     * @param authorization 授權資訊
     * @param req 請求對象
     * @return 響應對象
     */
    @Transactional
    public DgrGrpcProxyMap updateGrpcProxyWithImport(TsmpAuthorization authorization, DPB0292Req req) {
        try {
            // 檢查參數
            checkParam(req);

            // 驗證 TLS 憑證（如果有提供）
            validateTlsCertificates(req);

            // 更新映射
            DgrGrpcProxyMap dgrGrpcProxyMap = updateGrpcProxyMap(authorization.getUserName(), req);

            // 註冊到動態代理管理器
            // 這將發布事件，觸發 GrpcProxyServer 重啟
            dynamicGrpcProxyManager.updateProxyMapping(dgrGrpcProxyMap);

            return dgrGrpcProxyMap;
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }


    /**
     * 更新 gRPC 代理映射
     *
     * @param authorization 授權資訊
     * @param req 請求對象
     * @return 響應對象
     */
    @Transactional
    public DPB0292Resp updateGrpcProxy(TsmpAuthorization authorization, DPB0292Req req) {
        try {
            // 檢查參數
            checkParam(req);

            // 驗證 TLS 憑證（如果有提供）
            validateTlsCertificates(req);

            // 更新映射
            DgrGrpcProxyMap dgrGrpcProxyMap = updateGrpcProxyMap(authorization.getUserName(), req);

            // 註冊到動態代理管理器
            // 這將發布事件，觸發 GrpcProxyServer 重啟
            dynamicGrpcProxyManager.updateProxyMapping(dgrGrpcProxyMap);

            return getResp(dgrGrpcProxyMap);
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    /**
     * 驗證 TLS 相關的證書內容
     *
     * @param req 請求對象
     * @throws TsmpDpAaException 如果證書無效或缺少必需的配置
     */
    protected void validateTlsCertificates(DPB0292Req req) {
        // 檢查如果有提供服務器證書，那麼必須同時提供私鑰
        if ((req.getServerCertContent() != null && !req.getServerCertContent().isEmpty()) &&
                (req.getServerKeyContent() == null || req.getServerKeyContent().isEmpty())) {
            throw TsmpDpAaRtnCode._1297.throwing("提供了服務器證書但缺少私鑰");
        }

        // 檢查如果有提供私鑰，那麼必須同時提供服務器證書
        if ((req.getServerKeyContent() != null && !req.getServerKeyContent().isEmpty()) &&
                (req.getServerCertContent() == null || req.getServerCertContent().isEmpty())) {
            throw TsmpDpAaRtnCode._1297.throwing("提供了私鑰但缺少服務器證書");
        }

        // 如果有提供服務器證書，驗證其有效性
        if (req.getServerCertContent() != null && !req.getServerCertContent().isEmpty()) {
            boolean isValid = tlsCertificateManager.validateCertificate(req.getServerCertContent());
            if (!isValid) {
                throw TsmpDpAaRtnCode._1297.throwing("服務器證書無效");
            }
        }

        // 如果有提供信任的 CA 證書，驗證其有效性
        if (req.getTrustedCertsContent() != null && !req.getTrustedCertsContent().isEmpty()) {
            boolean isValid = tlsCertificateManager.validateCertificate(req.getTrustedCertsContent());
            if (!isValid) {
                //throw TsmpDpAaRtnCode._1297.throwing("信任的 CA 證書無效");
            	throw TsmpDpAaRtnCode._1352.throwing("{{trustedCertsContent}}");
            }
        }

        // 檢查安全模式是否有效
        String secureMode = req.getSecureMode();
        if (secureMode != null && !secureMode.isEmpty() &&
                !secureMode.equals("AUTO") && !secureMode.equals("SECURE") && !secureMode.equals("PLAINTEXT")) {
            throw TsmpDpAaRtnCode._1297.throwing("安全模式無效，必須是 AUTO、SECURE 或 PLAINTEXT");
        }

        // 安全模式啟用時檢查證書是否存在
        String trustedCertsContent = req.getTrustedCertsContent();
        if("SECURE".equals(secureMode) && trustedCertsContent.isEmpty()) {
            throw TsmpDpAaRtnCode._1296.throwing("trustedCertsContent");
        }
    }

    /**
     * 更新 gRPC 代理映射實體
     *
     * @param username 更新用戶名
     * @param req 請求對象
     * @return 更新後的實體
     */
    public DgrGrpcProxyMap updateGrpcProxyMap(String username, DPB0292Req req) {
        // 獲取並解析 ID
        String gRPCProxyMapId = req.getgRPCProxyMapId();
        Long id = getId(gRPCProxyMapId);

        // 檢查記錄是否存在
        Optional<DgrGrpcProxyMap> opt = dgrGrpcProxyMapDao.findById(id);
        if (opt.isEmpty()) {
            throw TsmpDpAaRtnCode._1298.throwing();
        }
        DgrGrpcProxyMap updateInfo = opt.get();

        // 獲取請求參數
        String serviceName = req.getServiceName();
        String proxyHostname = req.getProxyHostName();
        String targetHostname = req.getTargetHostName();
        String targetPort = req.getTargetPort();
        String connectTimeout = req.getConnectTimeoutMs();
        String readTimeout = req.getReadTimeoutMs();
        String sendTimeout = req.getSendTimeoutMs();
        String secureMode = req.getSecureMode();
        String enable = req.getEnable();
        String serverCertContent = req.getServerCertContent();
        String serverKeyContent = req.getServerKeyContent();
        String autoTrustUpstreamCerts = req.getAutoTrustUpstreamCerts();
        String trustedCertsContent = null;

        if(!"PLAINTEXT".equals(req.getSecureMode())){
            trustedCertsContent = req.getTrustedCertsContent();
        }

        // 更新基本屬性
        updateInfo.setServiceName(serviceName);
        updateInfo.setProxyHostName(proxyHostname);
        updateInfo.setTargetHostName(targetHostname);
        updateInfo.setTargetPort(Integer.parseInt(targetPort));
        updateInfo.setConnectTimeoutMs(Integer.parseInt(connectTimeout));
        updateInfo.setReadTimeoutMs(Integer.parseInt(readTimeout));
        updateInfo.setSendTimeoutMs(Integer.parseInt(sendTimeout));

        // 設置安全模式，默認為 AUTO
        if (secureMode != null && !secureMode.isEmpty()) {
            updateInfo.setSecureMode(secureMode);
        } else {
            updateInfo.setSecureMode("AUTO");
        }

        // 設置 TLS 相關內容
        if (serverCertContent != null) {
            if (serverCertContent.isEmpty()) {
                // 明確設為空字串時清空
                updateInfo.setServerCertContent(null);
            } else {
                updateInfo.setServerCertContent(serverCertContent);
            }
        }

        if (serverKeyContent != null) {
            if (serverKeyContent.isEmpty()) {
                // 明確設為空字串時清空
                updateInfo.setServerKeyContent(null);
            } else {
                updateInfo.setServerKeyContent(serverKeyContent);
            }
        }

        // 設置是否自動信任上游證書
        if (autoTrustUpstreamCerts != null) {
            updateInfo.setAutoTrustUpstreamCerts(autoTrustUpstreamCerts);
        }

        // 設置信任的 CA 證書內容
        if (trustedCertsContent != null) {
            if (trustedCertsContent.isEmpty()) {
                // 明確設為空字串時清空
                updateInfo.setTrustedCertsContent(null);
            } else {
                updateInfo.setTrustedCertsContent(trustedCertsContent);
            }
        }

        // 設置更新人員和時間
        updateInfo.setUpdateUser(username);
        updateInfo.setUpdateDateTime(DateTimeUtil.now());
        updateInfo.setEnable(enable);

        // 保存到資料庫
        return getDgrGrpcProxyMapDao().save(updateInfo);
    }

    /**
     * 檢查請求參數是否有效
     *
     * @param req 請求對象
     */
    public void checkParam(DPB0292Req req) {
        String gRPCProxyMapId = req.getgRPCProxyMapId();
        String serviceName = req.getServiceName();
        String proxyHostname = req.getProxyHostName();
        String targetHostname = req.getTargetHostName();
        String targetPort = req.getTargetPort();
        String connectTimeout = req.getConnectTimeoutMs();
        String readTimeout = req.getReadTimeoutMs();
        String sendTimeout = req.getSendTimeoutMs();
        String enable = req.getEnable();

        // 檢查必填欄位
        if (gRPCProxyMapId == null || gRPCProxyMapId.isEmpty()) {
            throw TsmpDpAaRtnCode._1296.throwing();
        }

        if (serviceName == null || serviceName.isEmpty()) {
            throw TsmpDpAaRtnCode._1296.throwing();
        }

        if (proxyHostname == null || proxyHostname.isEmpty()) {
            throw TsmpDpAaRtnCode._1296.throwing();
        }

        if (targetHostname == null || targetHostname.isEmpty()) {
            throw TsmpDpAaRtnCode._1296.throwing();
        }

        if (targetPort == null || targetPort.isEmpty()) {
            throw TsmpDpAaRtnCode._1296.throwing();
        }

        if (connectTimeout == null || connectTimeout.isEmpty()) {
            throw TsmpDpAaRtnCode._1296.throwing();
        }

        if (readTimeout == null || readTimeout.isEmpty()) {
            throw TsmpDpAaRtnCode._1296.throwing();
        }

        if (sendTimeout == null || sendTimeout.isEmpty()) {
            throw TsmpDpAaRtnCode._1296.throwing();
        }

        if (enable == null || enable.isEmpty()) {
            throw TsmpDpAaRtnCode._1296.throwing();
        } else if (!enable.equals("Y") && !enable.equals("N")) {
            throw TsmpDpAaRtnCode._1352.throwing("enable");
        }

        // 檢查數值型參數是否有效
        try {
            int portNum = Integer.parseInt(targetPort);
            if (portNum <= 0 || portNum > 65535) {
                throw TsmpDpAaRtnCode._1297.throwing();
            }

            int connectTimeoutMs = Integer.parseInt(connectTimeout);
            if (connectTimeoutMs <= 0) {
                throw TsmpDpAaRtnCode._1297.throwing();
            }

            int readTimeoutMs = Integer.parseInt(readTimeout);
            if (readTimeoutMs <= 0) {
                throw TsmpDpAaRtnCode._1297.throwing();
            }

            int sendTimeoutMs = Integer.parseInt(sendTimeout);
            if (sendTimeoutMs <= 0) {
                throw TsmpDpAaRtnCode._1297.throwing();
            }
        } catch (NumberFormatException e) {
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    /**
     * 將字串 ID 轉換為 Long 類型
     *
     * @param id 字串 ID
     * @return Long 類型 ID
     */
    private Long getId(String id) {
        if (!StringUtils.hasText(id)) {
            throw TsmpDpAaRtnCode._2025.throwing("{{id}}");
        }

        return isValidLong(id) ? Long.parseLong(id)
                : RandomSeqLongUtil.toLongValue(id);
    }

    /**
     * 檢查字串是否可以轉換為 Long 類型
     *
     * @param str 要檢查的字串
     * @return 是否可以轉換為 Long
     */
    private boolean isValidLong(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 從代理映射實體創建響應對象
     *
     * @param dgrGrpcProxyMap 代理映射實體
     * @return 響應對象
     */
    private DPB0292Resp getResp(DgrGrpcProxyMap dgrGrpcProxyMap) {
        DPB0292Resp resp = new DPB0292Resp();
        if (dgrGrpcProxyMap != null) {
            resp.setgRPCProxyMapId(String.valueOf(dgrGrpcProxyMap.getGrpcproxyMapId()));
            resp.setServiceName(dgrGrpcProxyMap.getServiceName());
            resp.setProxyHostName(dgrGrpcProxyMap.getProxyHostName());
            resp.setTargetHostName(dgrGrpcProxyMap.getTargetHostName());
            resp.setTargetPort(String.valueOf(dgrGrpcProxyMap.getTargetPort()));
            resp.setConnectTimeoutMs(String.valueOf(dgrGrpcProxyMap.getConnectTimeoutMs()));
            resp.setReadTimeoutMs(String.valueOf(dgrGrpcProxyMap.getReadTimeoutMs()));
            resp.setSendTimeoutMs(String.valueOf(dgrGrpcProxyMap.getSendTimeoutMs()));
            resp.setSecureMode(dgrGrpcProxyMap.getSecureMode());
            resp.setUpdateUser(dgrGrpcProxyMap.getUpdateUser());
            resp.setUpdateDateTime(dgrGrpcProxyMap.getUpdateDateTime().toString());
            resp.setEnable(String.valueOf(dgrGrpcProxyMap.getEnable()));
            resp.setAutoTrustUpstreamCerts(dgrGrpcProxyMap.isAutoTrustUpstreamCerts());
        }

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