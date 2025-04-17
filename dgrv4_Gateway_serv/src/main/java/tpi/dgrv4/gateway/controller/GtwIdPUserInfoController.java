package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.GtwIdPUserInfoService;

@RestController
public class GtwIdPUserInfoController {

	GtwIdPUserInfoService gtwIdPUserInfoService;
	
	@Autowired
	public GtwIdPUserInfoController(GtwIdPUserInfoService gtwIdPUserInfoService) {
		super();
		this.gtwIdPUserInfoService = gtwIdPUserInfoService;
	}

	@PostMapping(value = "/dgrv4/ssotoken/gtwidp/userInfo", 
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, // 使用 Form Urlencoded 格式
		produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getUserInfo(HttpServletRequest httpReq, 
			HttpServletResponse httpResp,
			@RequestHeader HttpHeaders headers) {
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		try {
			ResponseEntity<?> resp = gtwIdPUserInfoService.getUserInfo(httpReq, httpResp, headers);
			return resp;
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}
