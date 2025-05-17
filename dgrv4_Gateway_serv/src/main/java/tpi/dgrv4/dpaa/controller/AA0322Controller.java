package tpi.dgrv4.dpaa.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.AA0322Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0322Req;
import tpi.dgrv4.dpaa.vo.AA0322Resp;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/***
 * 
 * 將Composer節點資料，存檔到RDB
 * 
 * @author min
 *
 */
@RestController
public class AA0322Controller {

	private AA0322Service service;

	@Autowired
	public AA0322Controller(AA0322Service service) {
		super();
		this.service = service;
	}


	@PostMapping(value = "/dgrv4/11/AA0322", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0322Resp> saveComposerFlow(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<List<AA0322Req>> req, HttpServletRequest httpReq) {
		
		AA0322Resp resp = null;
		try {
			
			// 此 param 為內部使用, http 的處理放在 Controller 層級, 不要放在 Service 層級中
 			InnerInvokeParam iip = InnerInvokeParam.getInstance(headers, httpReq, new TsmpAuthorization());
 			
			resp = service.saveComposerFlow(req.getBody(), req.getReqHeader(),
					headers, iip);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
