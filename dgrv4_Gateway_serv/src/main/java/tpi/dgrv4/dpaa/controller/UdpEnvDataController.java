package tpi.dgrv4.dpaa.controller;


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
import tpi.dgrv4.dpaa.service.UdpEnvDataService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.UdpEnvDataReq;
import tpi.dgrv4.dpaa.vo.UdpEnvDataResp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 取得User可登入的伺服器環境名稱和URL
 * 
 * @author Mini
 */
@RestController
public class UdpEnvDataController {
	
	private UdpEnvDataService service;
	
	@Autowired
	public UdpEnvDataController(UdpEnvDataService service) {
		super();
		this.service = service;
	}

	/**
	 * @param headers
	 * @param req
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/udpEnvData", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<UdpEnvDataResp> getUdpEnvData(HttpServletRequest httpReq, @RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<UdpEnvDataReq> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		UdpEnvDataResp resp = null;

		try {
//			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.getUdpEnvData(tsmpHttpHeader.getAuthorization(), httpReq, req.getBody(), 
					req.getReqHeader(), headers);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}

