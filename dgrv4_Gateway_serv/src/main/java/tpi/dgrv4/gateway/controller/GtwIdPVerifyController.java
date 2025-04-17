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
import tpi.dgrv4.gateway.service.GtwIdPVerifyService;

@RestController
public class GtwIdPVerifyController {

	GtwIdPVerifyService gtwIdPVerifyService;
	
	@Autowired
	public GtwIdPVerifyController(GtwIdPVerifyService gtwIdPVerifyService) {
		super();
		this.gtwIdPVerifyService = gtwIdPVerifyService;
	}

	@PostMapping(value = "/dgrv4/ssotoken/gtwidp/verify", 
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, // 使用 Form Urlencoded 格式
		produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> verify(HttpServletRequest httpReq, 
			HttpServletResponse httpResp,
			@RequestHeader HttpHeaders headers) {
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		try {
			ResponseEntity<?> resp = gtwIdPVerifyService.verify(httpReq, httpResp, headers);
			return resp;
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}
