package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.service.AA0206Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0206Req;
import tpi.dgrv4.dpaa.vo.AA0206Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 
 * 更新Client端Password Setting(邏輯寫在AA0220)
 * 
 * @author tom
 *
 */
@RestController
public class AA0206Controller {
		
	private AA0206Service service;
	
	@Autowired
	public AA0206Controller(AA0206Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/11/AA0206", params = {"before"}, //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> clientEventLog_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		try {
			return ControllerUtil.getReqConstraints(req, new AA0206Req());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}
	
	/**
	 * 
	 * @param httpReq
	 * @param headers
	 * @param req
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/AA0206", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0206Resp> clientEventLog(HttpServletRequest httpReq, @RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0206Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA0206Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			String remoteAddr = httpReq.getRemoteAddr();
			resp = service.clientEventLog(tsmpHttpHeader.getAuthorization(), remoteAddr, req.getReqHeader(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
