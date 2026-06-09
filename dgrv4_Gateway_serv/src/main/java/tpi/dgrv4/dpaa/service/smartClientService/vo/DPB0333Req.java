package tpi.dgrv4.dpaa.service.smartClientService.vo;

import java.util.List;

/**
 * DPB0333 批次刪除 SMART Client 請求。
 *
 * @param deleteList 要刪除的項目清單，每筆帶 clientId + version
 */
public record DPB0333Req(
    List<SmartClientDeleteItem> deleteList
) {}
