package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.List;

/**
 * DPB0313: 刪除 Smart on FHIR Proxy 回應
 */
public class DPB0313Resp {

    /**
     * 成功刪除的 Proxy 數量
     */
    private Integer successCount;

    /**
     * 已刪除的 Proxy ID 列表
     */
    private List<String> deletedProxyIds;

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public List<String> getDeletedProxyIds() {
        return deletedProxyIds;
    }

    public void setDeletedProxyIds(List<String> deletedProxyIds) {
        this.deletedProxyIds = deletedProxyIds;
    }

    @Override
    public String toString() {
        return "DPB0313Resp [successCount=" + successCount + ", deletedProxyIds=" + deletedProxyIds + "]";
    }
}
