package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.List;

/**
 * DPB0312: 批次更新 Smart on FHIR Proxy 回應
 */
public class DPB0312Resp {

    /**
     * 成功更新的 Proxy 數量
     */
    private Integer successCount;
    
    /**
     * 更新後的 Proxy 完整資料列表
     */
    private List<SmartOnFhirProxyDto> updatedProxies;

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public List<SmartOnFhirProxyDto> getUpdatedProxies() {
        return updatedProxies;
    }

    public void setUpdatedProxies(List<SmartOnFhirProxyDto> updatedProxies) {
        this.updatedProxies = updatedProxies;
    }

    @Override
    public String toString() {
        return "DPB0312Resp [successCount=" + successCount + ", updatedProxies=" + updatedProxies + "]";
    }
}

