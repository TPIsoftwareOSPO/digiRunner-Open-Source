package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.List;

/**
 * DPB0313: 刪除 Smart on FHIR Proxy 請求
 * 支援單一刪除和批次刪除
 */
public class DPB0313Req {

    /**
     * 要刪除的 Proxy 列表
     * 每個項目包含 sofProxyId 和 version（用於樂觀鎖定）
     */
    private List<SmartOnFhirProxyUpdateDto> proxyList;

    public List<SmartOnFhirProxyUpdateDto> getProxyList() {
        return proxyList;
    }

    public void setProxyList(List<SmartOnFhirProxyUpdateDto> proxyList) {
        this.proxyList = proxyList;
    }

    @Override
    public String toString() {
        return "DPB0313Req [proxyList=" + proxyList + "]";
    }
}
