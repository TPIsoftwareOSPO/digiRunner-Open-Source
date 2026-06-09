package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.List;

import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.constant.SofProxySortBy;

/**
 * 查詢 Smart on FHIR Proxy 列表請求
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@FieldNameConstants
public class SmartOnFhirProxySearchReq {

    // 分頁參數
    @Builder.Default
    private Integer pageNum = 0; // 頁碼（從 0 開始）

    @Builder.Default
    private Integer pageSize = 20; // 每頁筆數

    @Builder.Default
    private String sortBy = SofProxySortBy.SOF_PROXY_ID.key(); // 排序欄位

    @Builder.Default
    private String sortOrder = Sort.Direction.DESC.name(); // 排序方向（asc/desc）

    // 篩選條件
    private List<String> keywords; // 關鍵字列表（模糊查詢 sofProxyId, sofProxyName, sofProxyRemark, sofProxyDiversionUrl）
    private String sofProxyStatus; // 狀態（Y/N）
}
