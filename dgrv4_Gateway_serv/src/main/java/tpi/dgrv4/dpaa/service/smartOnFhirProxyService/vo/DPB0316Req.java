package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.List;

/**
 * DPB0316 匯入 Smart on FHIR Proxy 請求
 * 支援 JSON 字串或檔案上傳
 */
public class DPB0316Req {

    // 匯入資料列表（從 JSON 字串或檔案解析）
    private List<SmartOnFhirProxyImportItemDto> importItems;
    
    // 是否強制更新（忽略 version 衝突）
    private Boolean forceUpdate = true;  // 預設為 true

    public List<SmartOnFhirProxyImportItemDto> getImportItems() {
        return importItems;
    }

    public void setImportItems(List<SmartOnFhirProxyImportItemDto> importItems) {
        this.importItems = importItems;
    }

    public Boolean getForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(Boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    @Override
    public String toString() {
        return "DPB0316Req [importItems=" + importItems + ", forceUpdate=" + forceUpdate + "]";
    }
}

