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
import tpi.dgrv4.dpaa.service.LdapService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.LdapReq;
import tpi.dgrv4.dpaa.vo.LdapResp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 刪除屏蔽條件資訊
 * @author Tom
 */
@RestController
public class LdapController {
	
	private LdapService service;
	
	@Autowired
	public LdapController(LdapService service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/ssotoken/ldap", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<LdapResp> checkAccountByLdap(HttpServletRequest httpReq, 
			@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<LdapReq> req ) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		LdapResp resp = null;
		try {
			String scheme = httpReq.getScheme() + "://";
		    String serverName = "127.0.0.1";
		    String serverPort = httpReq.getServerPort() == 80 ? "" : ":" + httpReq.getServerPort();
		    String contextPath = httpReq.getContextPath();
		    String localBaseUrl = scheme + serverName + serverPort + contextPath;
		    
			resp = service.checkAccountByLdap(tsmpHttpHeader.getAuthorization(), req.getBody(), localBaseUrl);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		// do Resp Header
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}