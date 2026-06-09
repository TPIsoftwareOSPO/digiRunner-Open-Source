package tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * DPB0317: 查詢 Sticky 列表回應（分頁）
 */
@Getter
@Setter
public class DPB0317Resp {

	private List<SmartOnFhirProxyStickyDto> content;
	private Long totalElements;
	private Integer totalPages;
	private Integer number;
	private Integer size;
}
