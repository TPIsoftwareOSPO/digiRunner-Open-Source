package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.List;

/**
 * DPB0312: 批次更新 Smart on FHIR Proxy 請求
 */
public class DPB0312Req {

    /**
     * 要批次更新的 Proxy 列表
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
        return "DPB0312Req [proxyList=" + proxyList + "]";
    }
}
