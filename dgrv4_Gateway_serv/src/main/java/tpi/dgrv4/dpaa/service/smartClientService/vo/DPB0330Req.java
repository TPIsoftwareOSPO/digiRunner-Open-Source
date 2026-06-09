package tpi.dgrv4.dpaa.service.smartClientService.vo;

/**
 * DPB0330 查詢 SMART Client 列表請求。
 *
 * 所有欄位 nullable，預設值由 Service 層處理：
 * pageNum 預設 0、pageSize 預設 20、sortBy 預設 smartClientId、sortOrder 預設 DESC。
 *
 * @param pageNum   頁碼（從 0 開始）
 * @param pageSize  每頁筆數
 * @param sortBy    排序欄位（合法值見 {@link tpi.dgrv4.dpaa.service.smartClientService.constant.SmartClientSortBy}）
 * @param sortOrder 排序方向（ASC / DESC）
 * @param keyword   模糊比對 clientId
 */
public record DPB0330Req(
    Integer pageNum,
    Integer pageSize,
    String sortBy,
    String sortOrder,
    String keyword
) {}
