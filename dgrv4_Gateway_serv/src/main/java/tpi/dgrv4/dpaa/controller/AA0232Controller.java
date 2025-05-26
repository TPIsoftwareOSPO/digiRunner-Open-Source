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
import tpi.dgrv4.dpaa.service.AA0232Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0232Req;
import tpi.dgrv4.dpaa.vo.AA0232Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/***
 * 
 * 查詢TSMP Client事件紀錄
 * 
 * @author min
 *
 */
@RestController
public class AA0232Controller {
	
	private AA0232Service service;
	
	@Autowired	
	public AA0232Controller(AA0232Service service) {
		super();
		this.service = service;
	}


	@PostMapping(value = "/dgrv4/11/AA0232", params = {"before"}, //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> queryClientEventLog_1_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		return ControllerUtil.getReqConstraints(req, new AA0232Req());
	}
	
	
	/**
	 * 
	 * 
	 * @param headers
	 * @param req
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/AA0232", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0232Resp> queryClientEventLog_1(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0232Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA0232Resp resp = null;
		try {
			
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			
			resp = service.queryClientEventLog(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
