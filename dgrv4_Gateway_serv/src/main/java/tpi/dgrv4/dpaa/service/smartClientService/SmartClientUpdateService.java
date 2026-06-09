package tpi.dgrv4.dpaa.service.smartClientService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.smartClientService.constant.SmartClientType;
import tpi.dgrv4.dpaa.service.smartClientService.constant.SmartLaunchMode;
import tpi.dgrv4.dpaa.service.smartClientService.vo.DPB0332Req;
import tpi.dgrv4.dpaa.service.smartClientService.vo.SmartClientDto;
import tpi.dgrv4.dpaa.service.smartClientService.vo.SmartClientUpdateItem;
import tpi.dgrv4.entity.entity.DgrSmartClient;
import tpi.dgrv4.entity.repository.DgrSmartClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * SMART Client 批次更新服務。
 *
 * 提供 DPB0332 的批次更新邏輯。PATCH 語意（JsonNullable 三態），
 * 整批 @Transactional，任一筆失敗全部 rollback。
 * 交叉驗證複用 {@link SmartClientValidator}。
 */
@Service
@RequiredArgsConstructor
public class SmartClientUpdateService {

    private TPILogger logger = TPILogger.tl;

    private final DgrSmartClientDao dgrSmartClientDao;

    @Transactional
    public List<SmartClientDto> batchUpdateSmartClient(TsmpAuthorization auth, DPB0332Req req) {
        try {
            if (req.updateList() == null || CollectionUtils.isEmpty(req.updateList())) {
                return List.of();
            }

            List<SmartClientDto> result = new ArrayList<>();
            List<SmartClientUpdateItem> items = req.updateList();

            for (int i = 0; i < items.size(); i++) {
                SmartClientUpdateItem item = items.get(i);
                final String itemIndex = "Item [" + (i + 1) + "]";

                // 必填識別欄位
                if (!StringUtils.hasText(item.getClientId())) {
                    throw TsmpDpAaRtnCode._1559.throwing(
                        itemIndex + ": Field 'clientId' is required. "
                        + "Provide the client ID to identify the update target.");
                }
                final String itemPrefix = itemIndex + " (clientId='" + item.getClientId() + "')";

                if (item.getVersion() == null) {
                    throw TsmpDpAaRtnCode._1559.throwing(
                        itemPrefix + ": Field 'version' is required. "
                        + "Fetch the current version from query API (DPB0330) and provide it for optimistic locking.");
                }

                // 查詢既有 Entity
                DgrSmartClient entity = dgrSmartClientDao.findByClientId(item.getClientId())
                    .orElseThrow(() -> TsmpDpAaRtnCode._1559.throwing(
                        itemPrefix + ": SMART configuration not found. "
                        + "Use create API (DPB0331) to create a new configuration."));

                // 樂觀鎖比對
                if (!entity.getVersion().equals(item.getVersion())) {
                    throw TsmpDpAaRtnCode._1559.throwing(
                        itemPrefix + ": Version mismatch. Current version is " + entity.getVersion()
                        + ", your version is " + item.getVersion()
                        + ". Fetch the latest data (DPB0330) and retry.");
                }

                // PATCH 套用（只覆蓋有傳的欄位）
                item.applyTo(entity);

                // 套用後驗證 NOT NULL 欄位不被清除
                SmartClientValidator.requireNonBlank(entity.getClientType(), "clientType",
                    itemPrefix + ": Cannot clear 'clientType'. It is required.");
                SmartClientValidator.requireNonBlank(entity.getIdpType(), "idpType",
                    itemPrefix + ": Cannot clear 'idpType'. It is required.");
                SmartClientValidator.requireNonBlank(entity.getIdpClientId(), "idpClientId",
                    itemPrefix + ": Cannot clear 'idpClientId'. It is required.");
                SmartClientValidator.requireNonEmpty(entity.getAllowedScopes(), "allowedScopes",
                    itemPrefix + ": Cannot clear 'allowedScopes'. It is required.");
                SmartClientValidator.requireNonBlank(entity.getAutoApprove(), "autoApprove",
                    itemPrefix + ": Cannot clear 'autoApprove'. It is required (Y/N).");

                // clientType 交叉驗證（用套用後的 Entity 最終值）
                SmartClientType clientType = SmartClientType.fromKey(entity.getClientType())
                    .orElseThrow(() -> TsmpDpAaRtnCode._1559.throwing(
                        itemPrefix + ": Field 'clientType' is invalid: '" + entity.getClientType() + "'. "
                        + "Allowed values: " + SmartClientType.allowedKeys() + "."));

                SmartClientValidator.validateClientTypeConstraints(clientType,
                    entity.getTokenEndpointAuthMethod(), entity.getJwksUri(), entity.getJwks());

                // launchMode 合法值（套用後的值）
                if (StringUtils.hasText(entity.getLaunchMode())) {
                    SmartLaunchMode.fromKey(entity.getLaunchMode())
                        .orElseThrow(() -> TsmpDpAaRtnCode._1559.throwing(
                            itemPrefix + ": Field 'launchMode' is invalid: '" + entity.getLaunchMode() + "'. "
                            + "Allowed values: " + SmartLaunchMode.allowedKeys() + "."));
                }

                // 審計欄位
                if (auth != null && StringUtils.hasText(auth.getUserName())) {
                    entity.setUpdateUser(auth.getUserName());
                }
                entity.setUpdateDateTime(tpi.dgrv4.common.utils.DateTimeUtil.now());

                // 儲存
                DgrSmartClient saved = dgrSmartClientDao.save(entity);
                result.add(SmartClientDto.from(saved));
            }

            return result;

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error("DPB0332 batchUpdateSmartClient failed. input: " + req);
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }
}
