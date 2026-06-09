package tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.constant.SofProxyStickySortBy;

/**
 * DPB0317: 查詢 Sticky 列表請求
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmartOnFhirProxyStickySearchReq {

	/** 頁碼（從 0 開始） */
	@Builder.Default
	private Integer pageNum = 0;

	/** 每頁筆數 */
	@Builder.Default
	private Integer pageSize = 20;

	/** 排序欄位 */
	@Builder.Default
	private String sortBy = SofProxyStickySortBy.SOF_PROXY_STICKY_ID.key();

	/** 排序方向（ASC / DESC） */
	@Builder.Default
	private String sortOrder = "DESC";

	/** Proxy ID（精確匹配） */
	private String sofProxyId;

	/** Diversion ID（精確匹配） */
	private String sofProxyDiversionId;

	/** Sticky Type（精確匹配，如 Patient） */
	private String sofProxyStickyType;

	/** 關鍵字列表（模糊查詢 type, typeId, path） */
	private List<String> keywords;
}
