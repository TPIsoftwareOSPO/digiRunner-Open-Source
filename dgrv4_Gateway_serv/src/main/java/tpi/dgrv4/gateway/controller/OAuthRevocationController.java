package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.OAuthRevocationService;

/**
 * @author Mini
 * 
 * 供 TSP 業者通知 authorization server 
 * 註銷 access token/refresh token 使用，
 * 如使用者不願意 TSP client 繼續使用該使用者之 access token。
 */
@RestController
public class OAuthRevocationController {
	
	private OAuthRevocationService oauthRevocationService;
	
	@Autowired
	public OAuthRevocationController(OAuthRevocationService oauthRevocationService) {
		super();
		this.oauthRevocationService = oauthRevocationService;
	}

	@PostMapping(value = "/oauth/revocation",
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> revocation(HttpServletRequest httpReq,
			HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders,
			@RequestParam MultiValueMap<String, String> values) {
		
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		ResponseEntity<?> resp = oauthRevocationService.revocation(httpReq, httpRes);
		
		return resp;
	}
}
