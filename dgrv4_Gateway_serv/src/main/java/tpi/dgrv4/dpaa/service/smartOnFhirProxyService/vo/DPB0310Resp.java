package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.List;

/**
 * DPB0310 查詢 Smart on FHIR Proxy 列表回應（分頁）
 */
public class DPB0310Resp {

    private List<SmartOnFhirProxyDto> content;  // 資料內容
    private Long totalElements;                     // 總筆數
    private Integer totalPages;                     // 總頁數
    private Integer number;                         // 當前頁碼
    private Integer size;                           // 每頁筆數

    public List<SmartOnFhirProxyDto> getContent() {
        return content;
    }

    public void setContent(List<SmartOnFhirProxyDto> content) {
        this.content = content;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "DPB0310Resp [content=" + content + ", totalElements=" + totalElements + ", totalPages=" + totalPages
                + ", number=" + number + ", size=" + size + "]";
    }
}

