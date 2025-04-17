package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.GtwIdPJwksService;

/**
 * @author Mini 
 * 提供 JWK Set 公鑰的內容
 */
@RestController
public class GtwIdPJwksController {

	private GtwIdPJwksService service;

	@Autowired
	public GtwIdPJwksController(GtwIdPJwksService service) {
		super();
		this.service = service;
	}

	@GetMapping(value = "/dgrv4/ssotoken/oauth2/certs", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getGtwIdPJwks(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp) {
		
		try {
			return service.getGtwIdPJwks(httpHeaders, httpReq, httpResp);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}
