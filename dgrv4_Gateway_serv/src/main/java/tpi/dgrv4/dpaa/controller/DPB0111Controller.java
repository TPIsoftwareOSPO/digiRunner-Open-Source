package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.service.DPB0111Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0111Req;
import tpi.dgrv4.dpaa.vo.DPB0111Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 角色與API交易代碼對應檔維護<br>
 * 查詢對應清單
 * @author Kim
 */
@RestController
public class DPB0111Controller {

	private DPB0111Service service;

	@Autowired
	public DPB0111Controller(DPB0111Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/11/DPB0111", params = {"before"}, //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> queryRTMapList_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		try {
			return ControllerUtil.getReqConstraints(req, new DPB0111Req());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}
	
	/**
	 * (依關鍵字、名單類型)查詢角色代碼與API交易代碼(txID)的對應關係。
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/DPB0111", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0111Resp> queryRTMapList(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0111Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0111Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryRTMapList(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		// do Resp Header
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}