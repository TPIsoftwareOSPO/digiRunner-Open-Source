package tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * DPB0319: 批次更新 Sticky 回應
 */
@Getter
@Setter
public class DPB0319Resp {

	private Integer successCount;
	private List<SmartOnFhirProxyStickyDto> updatedStickies;
}
