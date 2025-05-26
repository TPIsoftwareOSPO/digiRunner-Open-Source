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
import tpi.dgrv4.dpaa.service.UdpLdapService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.UdpLdapReq;
import tpi.dgrv4.dpaa.vo.UdpLdapResp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;


/**
 * 統一入口網LDAP驗證
 * @author Mini
 */
@RestController
public class UdpLdapController {

	private UdpLdapService service;
	
	@Autowired
	public UdpLdapController(UdpLdapService service) {
		super();
		this.service = service;
	}


	@PostMapping(value = "/dgrv4/udpssotoken/udpLdap", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<UdpLdapResp> udpCheckAccountByLdap(HttpServletRequest httpReq, @RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<UdpLdapReq> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		UdpLdapResp resp = null;
		try {
			String scheme = httpReq.getScheme() + "://";
		    String serverName = "127.0.0.1";
		    String serverPort = httpReq.getServerPort() == 80 ? "" : ":" + httpReq.getServerPort();
		    String contextPath = httpReq.getContextPath();
		    String localBaseUrl = scheme + serverName + serverPort + contextPath;
		    String userIp = StringUtils.isEmpty(headers.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr() : headers.getFirst("x-forwarded-for");
			resp = service.udpCheckAccountByLdap(userIp, httpReq, tsmpHttpHeader.getAuthorization(), req.getBody(), localBaseUrl);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		// do Resp Header
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}