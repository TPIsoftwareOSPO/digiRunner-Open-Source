package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.SmartOnFhirProxyStickyCreateUpdateDeleteService;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.SmartOnFhirProxyStickySearchService;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.DPB0317Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.DPB0319Req;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.DPB0319Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.DPB0320Req;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.DPB0320Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.SmartOnFhirProxyStickyDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.vo.SmartOnFhirProxyStickySearchReq;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * Smart on FHIR Proxy Sticky Controller
 * 提供 Sticky Session 紀錄的 CRUD 管理 API
 */
@RestController
public class SmartOnFhirProxyStickyController {

	private SmartOnFhirProxyStickySearchService searchService;
	private SmartOnFhirProxyStickyCreateUpdateDeleteService cudService;

	@Autowired
	public SmartOnFhirProxyStickyController(
			SmartOnFhirProxyStickySearchService searchService,
			SmartOnFhirProxyStickyCreateUpdateDeleteService cudService) {
		this.searchService = searchService;
		this.cudService = cudService;
	}

	/**
	 * DPB0317: 查詢 Sticky 列表（分頁）
	 */
	@PostMapping(value = "/dgrv4/11/DPB0317",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0317Resp> queryStickyList(
			@RequestHeader HttpHeaders headers,
			@RequestBody TsmpBaseReq<SmartOnFhirProxyStickySearchReq> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0317Resp resp = null;

		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = searchService.queryStickyList(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

	/**
	 * DPB0318: 新增 Sticky
	 */
	@PostMapping(value = "/dgrv4/11/DPB0318",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<SmartOnFhirProxyStickyDto> addSticky(
			@RequestHeader HttpHeaders headers,
			@RequestBody TsmpBaseReq<SmartOnFhirProxyStickyDto> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		SmartOnFhirProxyStickyDto resp = null;

		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = cudService.addSticky(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

	/**
	 * DPB0319: 批次更新 Sticky
	 */
	@PostMapping(value = "/dgrv4/11/DPB0319",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0319Resp> batchUpdateSticky(
			@RequestHeader HttpHeaders headers,
			@RequestBody TsmpBaseReq<DPB0319Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0319Resp resp = null;

		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = cudService.batchUpdateSticky(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

	/**
	 * DPB0320: 批次刪除 Sticky
	 */
	@PostMapping(value = "/dgrv4/11/DPB0320",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0320Resp> batchDeleteSticky(
			@RequestHeader HttpHeaders headers,
			@RequestBody TsmpBaseReq<DPB0320Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0320Resp resp = null;

		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = cudService.batchDeleteSticky(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
