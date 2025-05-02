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
import tpi.dgrv4.dpaa.service.AA0314Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0314Req;
import tpi.dgrv4.dpaa.vo.AA0314Resp;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;


/**
 * API組合與設計
 * 
 * @author Kim
 */
@RestController
public class AA0314Controller {

	private AA0314Service service;

	@Autowired
	public AA0314Controller(AA0314Service service) {
		super();
		this.service = service;
	}

	/**
	 * 確認佈署API 暫存組合 API 後，待 Composer 組合流程完成部署時回呼，用來確認此 API 已組合完成
	 * (TSMP_API_REG.reg_status = '1')。
	 * 
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/udpssotoken/11/AA0314", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0314Resp> confirmAPI(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0314Req> req, HttpServletRequest httpReq) {
		AA0314Resp resp = null;
		try {
		    
		 // 此 param 為內部使用, http 的處理放在 Controller 層級, 不要放在 Service 層級中
 			InnerInvokeParam iip = InnerInvokeParam.getInstance(headers, httpReq, new TsmpAuthorization());
		    
			resp = service.confirmAPI(req.getBody(), req.getReqHeader(), iip, headers);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}