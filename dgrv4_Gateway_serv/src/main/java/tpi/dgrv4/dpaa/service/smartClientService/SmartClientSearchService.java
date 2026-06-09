package tpi.dgrv4.dpaa.service.smartClientService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.record.PageResp;
import tpi.dgrv4.dpaa.service.smartClientService.constant.SmartClientSortBy;
import tpi.dgrv4.dpaa.service.smartClientService.vo.DPB0330Req;
import tpi.dgrv4.dpaa.service.smartClientService.vo.SmartClientDto;
import tpi.dgrv4.entity.entity.DgrSmartClient;
import tpi.dgrv4.entity.repository.DgrSmartClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * SMART Client 查詢服務。
 *
 * 提供 DPB0330 的分頁查詢邏輯，支援 clientId 關鍵字篩選與排序。
 */
@Service
@RequiredArgsConstructor
public class SmartClientSearchService {

    private TPILogger logger = TPILogger.tl;

    private final DgrSmartClientDao dgrSmartClientDao;

    public PageResp<SmartClientDto> querySmartClientList(TsmpAuthorization auth, DPB0330Req req) {
        try {
            // 分頁參數預設值與驗證
            int pageNum = req.pageNum() != null ? req.pageNum() : 0;
            int pageSize = req.pageSize() != null ? req.pageSize() : 20;

            if (pageNum < 0) {
                String msg = "Field 'pageNum' is invalid: " + pageNum
                    + ". Page number must be >= 0. Provide 0 for the first page.";
                this.logger.error(msg);
                throw TsmpDpAaRtnCode._1559.throwing(msg);
            }

            if (pageSize <= 0) {
                String msg = "Field 'pageSize' is invalid: " + pageSize
                    + ". Page size must be > 0. Provide a value between 1 and 1000.";
                this.logger.error(msg);
                throw TsmpDpAaRtnCode._1559.throwing(msg);
            }

            pageSize = Math.min(pageSize, 1000);

            // 排序欄位解析
            SmartClientSortBy sortBy = SmartClientSortBy.SMART_CLIENT_ID;
            if (StringUtils.hasText(req.sortBy())) {
                sortBy = SmartClientSortBy.fromKey(req.sortBy()).orElseThrow(() -> {
                    String msg = "Field 'sortBy' is invalid: '" + req.sortBy()
                        + "'. Allowed values are: " + SmartClientSortBy.allowedKeys()
                        + ". Remove 'sortBy' to use the default (smartClientId).";
                    this.logger.error(msg);
                    return TsmpDpAaRtnCode._1559.throwing(msg);
                });
            }

            Sort.Direction direction = Sort.Direction.fromOptionalString(req.sortOrder())
                .orElse(Sort.Direction.DESC);

            Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(direction, sortBy.key()));

            // 查詢
            Page<DgrSmartClient> page;
            if (StringUtils.hasText(req.keyword())) {
                page = dgrSmartClientDao.findByClientIdContainingIgnoreCase(req.keyword().trim(), pageable);
            } else {
                page = dgrSmartClientDao.findAll(pageable);
            }

            // Entity → DTO
            List<SmartClientDto> dtoList = page.getContent().stream()
                .map(SmartClientDto::from)
                .toList();

            return PageResp.from(page, dtoList);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error("DPB0330 querySmartClientList failed. input: " + req);
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }
}
