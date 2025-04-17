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
import tpi.dgrv4.dpaa.service.DPB0143Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0143Req;
import tpi.dgrv4.dpaa.vo.DPB0143Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

/**
 * 驗證AC入場券
 * @author Kim
 */
@RestController
public class DPB0143Controller {

	private DPB0143Service service;
	
	@Autowired
	public DPB0143Controller(DPB0143Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/ssotoken/DPB0143", params = {"before"}, //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> checkACEntryTicket_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		return ControllerUtil.getReqConstraints(req, new DPB0143Req());
	}

	/**
	 * 驗證傳入的 JWE 簽章是否正確, 及檢查 User IP, 時間的資料
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/ssotoken/DPB0143", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0143Resp> checkACEntryTicket(@RequestHeader HttpHeaders headers, //
			@RequestBody TsmpBaseReq<DPB0143Req> req) {
		DPB0143Resp resp = null;
		try {
			ControllerUtil.validateRequest(null, req);
			resp = service.checkACEntryTicket(req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
