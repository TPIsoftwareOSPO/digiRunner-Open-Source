package tpi.dgrv4.dpaa.service.smartClientService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.smartClientService.constant.SmartClientType;
import tpi.dgrv4.dpaa.service.smartClientService.constant.SmartLaunchMode;
import tpi.dgrv4.dpaa.service.smartClientService.vo.DPB0331Req;
import tpi.dgrv4.dpaa.service.smartClientService.vo.SmartClientDto;
import tpi.dgrv4.entity.entity.DgrSmartClient;
import tpi.dgrv4.entity.repository.DgrSmartClientDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * SMART Client 新增服務。
 *
 * 提供 DPB0331 的新增邏輯，包含必填驗證、clientType 交叉驗證、
 * clientId 存在性與重複性檢查。
 */
@Service
@RequiredArgsConstructor
public class SmartClientCreateService {

    private TPILogger logger = TPILogger.tl;

    private final DgrSmartClientDao dgrSmartClientDao;
    private final TsmpClientDao tsmpClientDao;

    @Transactional
    public SmartClientDto createSmartClient(TsmpAuthorization auth, DPB0331Req req) {
        try {
            // 必填欄位驗證
            SmartClientValidator.requireNonBlank(req.clientId(), "clientId",
                "Provide the client ID registered in tsmp_client.");
            SmartClientValidator.requireNonBlank(req.clientType(), "clientType",
                "Allowed values: " + SmartClientType.allowedKeys() + ".");
            SmartClientValidator.requireNonBlank(req.idpType(), "idpType",
                "Provide the IdP type (e.g. GOOGLE, MS, OIDC, LDAP, JDBC, API, CUS).");
            SmartClientValidator.requireNonEmpty(req.allowedScopes(), "allowedScopes",
                "Provide a list of SMART scopes (e.g. [\"openid\", \"patient/*.rs\"]).");
            SmartClientValidator.requireNonBlank(req.idpClientId(), "idpClientId",
                "Provide the ssotoken client ID for second-factor verification (must exist in tsmp_client).");

            // clientId 存在性與重複性檢查
            if (tsmpClientDao.findFirstByClientId(req.clientId()) == null) {
                throw TsmpDpAaRtnCode._1559.throwing(
                    "Client '" + req.clientId() + "' not found in tsmp_client. "
                    + "Register the client first, then create the SMART configuration.");
            }
            if (dgrSmartClientDao.findByClientId(req.clientId()).isPresent()) {
                throw TsmpDpAaRtnCode._1559.throwing(
                    "SMART configuration already exists for client '" + req.clientId() + "'. "
                    + "Use update API (DPB0332) to modify existing configuration.");
            }

            // idpClientId 存在性檢查
            if (tsmpClientDao.findFirstByClientId(req.idpClientId()) == null) {
                throw TsmpDpAaRtnCode._1559.throwing(
                    "idpClient '" + req.idpClientId() + "' not found in tsmp_client. "
                    + "Register the ssotoken client first.");
            }

            // clientType 解析（從 enum 取規則）
            SmartClientType clientType = SmartClientType.fromKey(req.clientType())
                .orElseThrow(() -> TsmpDpAaRtnCode._1559.throwing(
                    "Field 'clientType' is invalid: '" + req.clientType() + "'. "
                    + "Allowed values: " + SmartClientType.allowedKeys() + "."));

            // clientType 交叉驗證（規則來自 enum，邏輯在 Validator）
            SmartClientValidator.validateClientTypeConstraints(clientType,
                req.tokenEndpointAuthMethod(), req.jwksUri(), req.jwks());

            // launchMode 合法值
            if (StringUtils.hasText(req.launchMode())) {
                SmartLaunchMode.fromKey(req.launchMode())
                    .orElseThrow(() -> TsmpDpAaRtnCode._1559.throwing(
                        "Field 'launchMode' is invalid: '" + req.launchMode() + "'. "
                        + "Allowed values: " + SmartLaunchMode.allowedKeys()
                        + ". Remove 'launchMode' to use the default (standalone)."));
            }

            // Req → Entity（純映射）
            DgrSmartClient entity = req.toEntity();

            // Policy：預設值與審計欄位
            if (!StringUtils.hasText(entity.getAutoApprove())) {
                entity.setAutoApprove("N");
            }
            if (!StringUtils.hasText(entity.getLaunchMode())) {
                entity.setLaunchMode("standalone");
            }
            if (auth != null && StringUtils.hasText(auth.getUserName())) {
                entity.setCreateUser(auth.getUserName());
            }

            // 儲存
            DgrSmartClient saved = dgrSmartClientDao.save(entity);

            return SmartClientDto.from(saved);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error("DPB0331 createSmartClient failed. input: " + req);
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

}
