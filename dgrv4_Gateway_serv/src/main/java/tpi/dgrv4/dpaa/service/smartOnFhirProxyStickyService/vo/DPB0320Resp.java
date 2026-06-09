package tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * DPB0320: 批次刪除 Sticky 回應
 */
@Getter
@Setter
public class DPB0320Resp {

	private Integer successCount;
	private List<String> deletedStickyIds;
}
