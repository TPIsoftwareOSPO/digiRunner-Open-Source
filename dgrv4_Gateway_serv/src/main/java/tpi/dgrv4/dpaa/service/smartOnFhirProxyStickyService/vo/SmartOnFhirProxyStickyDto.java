package tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Smart on FHIR Proxy Sticky 通用 DTO
 * 用於 Create 輸入和 Query 輸出
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmartOnFhirProxyStickyDto {

	/** Sticky ID（Create 時不需傳入，由系統產生） */
	private String sofProxyStickyId;

	/** Proxy ID（必填） */
	private String sofProxyId;

	/** Diversion ID（必填） */
	private String sofProxyDiversionId;

	/** FHIR Interaction 類型（選填，如 READ、CREATE、UPDATE） */
	private String sofProxyStickyInteraction;

	/** FHIR Resource Type（必填，如 Patient、Observation） */
	private String sofProxyStickyType;

	/** FHIR Resource ID（選填） */
	private String sofProxyStickyTypeId;

	/** HTTP Method（選填，如 GET、POST） */
	private String sofProxyStickyVerb;

	/** HTTP URL Path（選填） */
	private String sofProxyStickyPath;

	/** Hashcode（輸出用，由系統計算） */
	private String sofProxyStickyHashcode;

	/** Diversion URL（輸出用，來自關聯的 Diversion） */
	private String sofProxyDiversionUrl;

	private Date createDateTime;
	private String createUser;
	private Date updateDateTime;
	private String updateUser;
	private String version;
}
