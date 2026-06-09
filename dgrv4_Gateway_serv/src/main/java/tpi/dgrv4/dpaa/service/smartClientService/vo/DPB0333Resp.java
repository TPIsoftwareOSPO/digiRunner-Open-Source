package tpi.dgrv4.dpaa.service.smartClientService.vo;

/**
 * DPB0333 批次刪除 SMART Client 回應。
 *
 * @param deletedCount 實際刪除筆數
 */
public record DPB0333Resp(
    int deletedCount
) {}
