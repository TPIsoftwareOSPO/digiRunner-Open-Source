package tpi.dgrv4.dpaa.service;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0304Req;
import tpi.dgrv4.dpaa.vo.DPB0304Resp;
import tpi.dgrv4.dpaa.vo.DPB0304RespItem;
import tpi.dgrv4.entity.entity.DgrH2ConfigSyncHistory;
import tpi.dgrv4.entity.repository.DgrH2ConfigSyncHistoryDao;
import tpi.dgrv4.entity.vo.DPB0304SearchCriteria;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DPB0304Service {

    private Integer pageSize;

    @Setter(onMethod_ = @Autowired)
    private DgrH2ConfigSyncHistoryDao dgrH2ConfigSyncHistoryDao;
    @Setter(onMethod_ = @Autowired)
    private ServiceConfig serviceConfig;


    /**
     * 查詢資料庫同步歷史記錄
     *
     * @param authorization 授權資訊
     * @param req           請求參數
     * @param reqHeader     請求標頭
     * @return 歷史記錄列表
     */
    public DPB0304Resp queryH2ConfigSyncHistory(TsmpAuthorization authorization,
                                                DPB0304Req req,
                                                ReqHeader reqHeader) {
        DPB0304Resp resp = new DPB0304Resp();

        try {
            long startTime = System.currentTimeMillis();

            // 1. 設定查詢條件並檢核資料
            DPB0304SearchCriteria criteria = setCriteriaAndCheckData(req);

            // 2. 查詢資料庫
            List<DgrH2ConfigSyncHistory> records = getDgrH2ConfigSyncHistoryDao()
                    .query_DPB0304Service(criteria);

            // 3. 檢查是否有資料
            if (records.isEmpty()) {
                throw TsmpDpAaRtnCode._1298.throwing(); // 1298:查無資料
            }

            // 4. 轉換為 Response Item
            List<DPB0304RespItem> items = convertToRespItems(records);

            // 5. 設定回應
            resp.setDataList(items);

            long endTime = System.currentTimeMillis();
            TPILogger.tl.debug(String.format("DPB0304 execution time: %d ms", endTime - startTime));

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing(); // 1297:執行錯誤
        }

        return resp;
    }


    /**
     * 設定查詢條件並檢核資料
     *
     * @param req 請求參數
     * @return 查詢條件
     */
    private DPB0304SearchCriteria setCriteriaAndCheckData(DPB0304Req req) {
        DPB0304SearchCriteria criteria = new DPB0304SearchCriteria();

        // 1. 設定同步類型過濾
        String syncType = ServiceUtil.nvl(req.getSyncType());
        if (StringUtils.hasText(syncType) && !"ALL".equals(syncType)) {
            // 檢核 syncType 是否有效
            boolean isValidSyncType = Arrays.stream(DgrH2ConfigSyncHistoryDao.SyncType.values())
                    .anyMatch(e -> e.name().equals(syncType));
            if (!isValidSyncType) {
                throw TsmpDpAaRtnCode._1290.throwing(); // 1290:參數錯誤
            }
            criteria.setSyncType(syncType);
        }

        // 2. 設定狀態過濾
        String status = ServiceUtil.nvl(req.getStatus());
        if (StringUtils.hasText(status)) {
            // 檢核 status 是否有效（D/R/E）
            boolean isValidStatus = Arrays.stream(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.values())
                    .anyMatch(e -> e.getStatus().equals(status));
            if (!isValidStatus) {
                throw TsmpDpAaRtnCode._1290.throwing(); // 1290:參數錯誤
            }
            criteria.setStatus(status);
        }

        // 3. 關鍵字分詞（
        // 使用 ServiceUtil.getKeywords() 將關鍵字字串分解為陣列
        String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
        criteria.setKeyword(words);

        // 4. 設定分頁游標
        if (req.getLastSyncId() != null) {
            long lastSyncId = Long.parseLong(req.getLastSyncId());
            Optional<DgrH2ConfigSyncHistory> lastSync = getDgrH2ConfigSyncHistoryDao().findById(lastSyncId);
            lastSync.ifPresent(criteria::setLastId);
        }

        // 5. 設定每頁筆數
        criteria.setPageSize(getPageSize());

        // 6. 設定排序欄位和方向
        Map<String, String> sortBy = req.getSortBy();
        if (sortBy != null && !sortBy.isEmpty()) {
            // 檢核排序參數
            checkSortBy(sortBy);
            criteria.setSortBy(sortBy);
        } else {
            // 預設排序：開始時間降冪
            Map<String, String> defaultSort = new HashMap<>();
            defaultSort.put("startTime", "desc");
            criteria.setSortBy(defaultSort);
        }

        return criteria;
    }

    /**
     * 檢核排序參數
     *
     * @param sortBy 排序參數
     */
    private void checkSortBy(Map<String, String> sortBy) {
        // 允許的排序欄位
        List<String> validKeys = Arrays.asList(
                "syncId", "syncType", "status",
                "startTime", "endTime", "duration"
        );

        // 允許的排序方向
        List<String> validValues = Arrays.asList("asc", "desc");

        for (Map.Entry<String, String> entry : sortBy.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // 檢查欄位名稱
            if (!validKeys.contains(key)) {
                TPILogger.tl.error("Invalid sort key: " + key);
                throw TsmpDpAaRtnCode._1290.throwing(); // 1290:參數錯誤
            }

            // 檢查排序方向
            if (!validValues.contains(value.toLowerCase())) {
                TPILogger.tl.error("Invalid sort value: " + value);
                throw TsmpDpAaRtnCode._1290.throwing(); // 1290:參數錯誤
            }
        }
    }

    /**
     * 轉換 Entity List 為 Response Item List
     *
     * @param records Entity 列表
     * @return Response Item 列表
     */
    private List<DPB0304RespItem> convertToRespItems(List<DgrH2ConfigSyncHistory> records) {
        return records.stream()
                .map(this::convertToRespItem)
                .toList();
    }

    /**
     * 轉換 Entity 為 Response Item
     *
     * @param dgrH2ConfigSyncHistory Entity
     * @return Response Item
     */
    private DPB0304RespItem convertToRespItem(DgrH2ConfigSyncHistory dgrH2ConfigSyncHistory) {
        DPB0304RespItem item = new DPB0304RespItem();

        // 基本資訊
        item.setSyncId(String.valueOf(dgrH2ConfigSyncHistory.getSyncId()));
        item.setSyncType(dgrH2ConfigSyncHistory.getSyncType());
        item.setScheduleId(dgrH2ConfigSyncHistory.getScheduleId());
        item.setSourceId(dgrH2ConfigSyncHistory.getSourceId());

        // 解析 targetIds（逗號分隔 → List）
        String targetIdsStr = dgrH2ConfigSyncHistory.getTargetIds();
        if (StringUtils.hasText(targetIdsStr)) {
            List<String> targetIds = Arrays.stream(targetIdsStr.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toList();
            item.setTargetIds(targetIds);
        }

        item.setStatus(dgrH2ConfigSyncHistory.getStatus());
        item.setCurrentStep(dgrH2ConfigSyncHistory.getCurrentStep());

        Date startTime = dgrH2ConfigSyncHistory.getStartTime();
        item.setStartTime(startTime);

        Date endTime = dgrH2ConfigSyncHistory.getEndTime();
        item.setEndTime(endTime);

        item.setDuration(dgrH2ConfigSyncHistory.getDuration());
        if (dgrH2ConfigSyncHistory.getDuration() != null) {
            item.setDuration(dgrH2ConfigSyncHistory.getDuration());
        }

        item.setCreateUser(dgrH2ConfigSyncHistory.getCreateUser());

        return item;
    }

    /**
     * 取得每頁筆數配置
     *
     * @return 每頁筆數
     */
    protected Integer getPageSize() {
        if (this.pageSize == null) {
            this.pageSize = getServiceConfig().getPageSize("dpb0304");
            if (this.pageSize == null || this.pageSize <= 0) {
                this.pageSize = 20; // 預設 20 筆
            }
        }
        return this.pageSize;
    }

    protected DgrH2ConfigSyncHistoryDao getDgrH2ConfigSyncHistoryDao() {
        return dgrH2ConfigSyncHistoryDao;
    }

    protected ServiceConfig getServiceConfig() {
        return serviceConfig;
    }
}
