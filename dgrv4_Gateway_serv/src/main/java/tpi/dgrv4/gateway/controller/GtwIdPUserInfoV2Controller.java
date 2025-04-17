package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.GtwIdPUserInfoV2Service;
/**
 * @author Mini
 */

@RestController
public class GtwIdPUserInfoV2Controller {

	private GtwIdPUserInfoV2Service gtwIdPUserInfoV2Service;

	@Autowired
	public GtwIdPUserInfoV2Controller(GtwIdPUserInfoV2Service gtwIdPUserInfoV2Service) {
		super();
		this.gtwIdPUserInfoV2Service = gtwIdPUserInfoV2Service;
	}

	@GetMapping(value = "/dgrv4/ssotoken/gtwidp/v2/userInfo", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getUserInfo(HttpServletRequest httpReq, 
			HttpServletResponse httpResp,
			@RequestHeader HttpHeaders httpHeaders) {
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		try {
			ResponseEntity<?> respEntity = gtwIdPUserInfoV2Service.getUserInfoV2(httpReq, httpResp, httpHeaders);
			return respEntity;
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}
