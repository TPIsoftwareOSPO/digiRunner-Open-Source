package tpi.dgrv4.dpaa.service.smartClientService.vo;

import java.util.List;

/**
 * DPB0332 批次更新 SMART Client 請求。
 *
 * @param updateList 要更新的項目清單，每筆為 {@link SmartClientUpdateItem}
 */
public record DPB0332Req(
    List<SmartClientUpdateItem> updateList
) {}
