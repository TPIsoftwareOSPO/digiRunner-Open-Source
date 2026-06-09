package tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * DPB0319: 批次更新 Sticky 請求
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DPB0319Req {

	private List<SmartOnFhirProxyStickyUpdateDto> stickyList;
}
