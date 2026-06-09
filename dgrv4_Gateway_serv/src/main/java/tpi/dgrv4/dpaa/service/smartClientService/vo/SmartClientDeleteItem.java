package tpi.dgrv4.dpaa.service.smartClientService.vo;

/**
 * SMART Client 批次刪除的單筆項目。
 *
 * @param clientId 識別刪除目標（必填）
 * @param version  樂觀鎖版本號（必填），防止刪除已被他人修改的資料
 */
public record SmartClientDeleteItem(
    String clientId,
    Long version
) {}
