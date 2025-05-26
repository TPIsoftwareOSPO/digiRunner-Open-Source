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
import tpi.dgrv4.dpaa.service.AA0231Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0231Req;
import tpi.dgrv4.dpaa.vo.AA0231Resp;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
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
public class AA0231Controller {
		
	private AA0231Service service;
	
	@Autowired
	public AA0231Controller(AA0231Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/11/AA0231", params = {"before"}, //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> updatePasswordSettingByClient_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		try {
			return ControllerUtil.getReqConstraints(req, new AA0231Req());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}
	
	/**
	 * 
	 * 
	 * @param headers
	 * @param req
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/AA0231", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0231Resp> updatePasswordSettingByClient(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0231Req> req, HttpServletRequest httpReq) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA0231Resp resp = null;
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
						
			resp = service.updatePasswordSettingByClient(auth, req.getBody(), iip);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
