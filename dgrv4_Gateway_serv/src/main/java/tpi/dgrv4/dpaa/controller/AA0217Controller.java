package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.service.AA0217Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0217Req;
import tpi.dgrv4.dpaa.vo.AA0217Resp;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 
 * 更新Client Security Level
 * 
 * @author Mavis
 *
 */
@RestController
public class AA0217Controller {
		
	private AA0217Service service;
	
	@Autowired
	public AA0217Controller(AA0217Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/11/AA0217", params = {"before"}, //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> updateSecurityLVByClient_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		return ControllerUtil.getReqConstraints(req, new AA0217Req());
	}
	
	/**
	 * 
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/AA0217", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0217Resp> updateSecurityLVByClient(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0217Req> req, HttpServletRequest httpReq) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA0217Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			
			// 此 param 為內部使用, http 的處理放在 Controller 層級, 不要放在 Service 層級中
			InnerInvokeParam iip = InnerInvokeParam.getInstance(headers, httpReq, tsmpHttpHeader.getAuthorization());
			
			TsmpAuthorization auth = null;
			if(iip != null && StringUtils.hasText(iip.getAcToken())) {//若使用Api key調用此API,將auth換成acToken,以便後面得到真正的user name,否則會是DGRK 
				auth = ControllerUtil.parserAuthorization(iip.getAcToken());
			}else {
				auth = tsmpHttpHeader.getAuthorization();
			}
			
			resp = service.updateSecurityLVByClient(auth, req.getBody(), iip);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
