package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.List;

/**
 * DPB0315 預前匯入驗證請求
 * 支援 JSON 字串或檔案上傳
 */
public class DPB0315Req {

    // 匯入資料列表（從 JSON 字串或檔案解析）
    private List<SmartOnFhirProxyImportItemDto> importItems;

    public List<SmartOnFhirProxyImportItemDto> getImportItems() {
        return importItems;
    }

    public void setImportItems(List<SmartOnFhirProxyImportItemDto> importItems) {
        this.importItems = importItems;
    }

    @Override
    public String toString() {
        return "DPB0315Req [importItems=" + importItems + "]";
    }
}

