package tpi.dgrv4.dpaa.service.smartClientService;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.smartClientService.vo.DPB0333Req;
import tpi.dgrv4.dpaa.service.smartClientService.vo.DPB0333Resp;
import tpi.dgrv4.dpaa.service.smartClientService.vo.SmartClientDeleteItem;
import tpi.dgrv4.entity.entity.DgrSmartClient;
import tpi.dgrv4.entity.repository.DgrSmartClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * SMART Client 批次刪除服務。
 *
 * 提供 DPB0333 的批次刪除邏輯。整批 @Transactional，
 * 任一筆失敗全部 rollback（依據 Google AIP-235：同步批次刪除必須 atomic）。
 * 每筆帶樂觀鎖版本號，防止刪除已被他人修改的資料。
 */
@Service
@RequiredArgsConstructor
public class SmartClientDeleteService {

    private TPILogger logger = TPILogger.tl;

    private final DgrSmartClientDao dgrSmartClientDao;

    @Transactional
    public DPB0333Resp batchDeleteSmartClient(TsmpAuthorization auth, DPB0333Req req) {
        try {
            if (req.deleteList() == null || CollectionUtils.isEmpty(req.deleteList())) {
                return new DPB0333Resp(0);
            }

            List<SmartClientDeleteItem> items = req.deleteList();
            int deletedCount = 0;

            for (int i = 0; i < items.size(); i++) {
                SmartClientDeleteItem item = items.get(i);
                final String itemIndex = "Item [" + (i + 1) + "]";

                // 必填檢查
                if (!StringUtils.hasText(item.clientId())) {
                    throw TsmpDpAaRtnCode._1559.throwing(
                        itemIndex + ": Field 'clientId' is required. "
                        + "Provide the client ID to identify the delete target.");
                }
                final String itemPrefix = itemIndex + " (clientId='" + item.clientId() + "')";

                if (item.version() == null) {
                    throw TsmpDpAaRtnCode._1559.throwing(
                        itemPrefix + ": Field 'version' is required. "
                        + "Fetch the current version from query API (DPB0330) and provide it for optimistic locking.");
                }

                // 查詢既有 Entity
                DgrSmartClient entity = dgrSmartClientDao.findByClientId(item.clientId())
                    .orElseThrow(() -> TsmpDpAaRtnCode._1559.throwing(
                        itemPrefix + ": SMART configuration not found. "
                        + "The resource may have already been deleted."));

                // 樂觀鎖比對
                if (!entity.getVersion().equals(item.version())) {
                    throw TsmpDpAaRtnCode._1559.throwing(
                        itemPrefix + ": Version mismatch. Current version is " + entity.getVersion()
                        + ", your version is " + item.version()
                        + ". The resource may have been modified. Fetch the latest data (DPB0330) and retry.");
                }

                // 刪除
                dgrSmartClientDao.delete(entity);
                deletedCount++;
            }

            return new DPB0333Resp(deletedCount);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error("DPB0333 batchDeleteSmartClient failed. input: " + req);
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }
}
