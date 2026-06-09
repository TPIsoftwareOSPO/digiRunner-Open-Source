package tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * DPB0320: 批次刪除 Sticky 請求
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DPB0320Req {

	/** 刪除清單（只需 sofProxyStickyId 和 version） */
	private List<SmartOnFhirProxyStickyUpdateDto> stickyList;
}
