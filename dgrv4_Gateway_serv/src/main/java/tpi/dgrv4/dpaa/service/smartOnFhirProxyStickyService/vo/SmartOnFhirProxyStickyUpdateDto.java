package tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo;

import org.openapitools.jackson.nullable.JsonNullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Smart on FHIR Proxy Sticky 批次更新 DTO
 * 使用 JsonNullable 區分「不傳」、「清空」、「更新」三種狀態
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmartOnFhirProxyStickyUpdateDto {

	/** Sticky ID（必填） */
	private String sofProxyStickyId;

	/** 版本號（必填，樂觀鎖定） */
	private String version;

	/** Diversion ID */
	private JsonNullable<String> sofProxyDiversionId = JsonNullable.undefined();

	/** FHIR Interaction 類型 */
	private JsonNullable<String> sofProxyStickyInteraction = JsonNullable.undefined();

	/** FHIR Resource Type */
	private JsonNullable<String> sofProxyStickyType = JsonNullable.undefined();

	/** FHIR Resource ID */
	private JsonNullable<String> sofProxyStickyTypeId = JsonNullable.undefined();

	/** HTTP Method */
	private JsonNullable<String> sofProxyStickyVerb = JsonNullable.undefined();

	/** HTTP URL Path */
	private JsonNullable<String> sofProxyStickyPath = JsonNullable.undefined();
}
