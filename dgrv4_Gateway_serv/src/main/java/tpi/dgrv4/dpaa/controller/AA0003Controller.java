package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.AA0003Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0003Req;
import tpi.dgrv4.dpaa.vo.AA0003Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 
 * 在TSMP中查詢後台管理使用者資料。			
 * 
 * @author Mavis
 *
 */
@RestController
public class AA0003Controller {
		
	private AA0003Service service;
	
	@Autowired
	public AA0003Controller(AA0003Service service) {
		super();
		this.service = service;
	}

	/**
	 * 
	 * 
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/11/AA0003", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0003Resp> queryTUserDetail(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0003Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA0003Resp resp = null;
		
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryTUserDetail(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
