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
import tpi.dgrv4.gateway.service.OAuthIntrospectionService;

/**
 * @author Mini
 *
 * 當 client 帶 access token 到 Resource Server 存取使用者資源時，
 * authorization server 應提供此 API 供 resource server 
 * 確認 accesstoken 合法性。
 */
@RestController
public class OAuthIntrospectionController {

	private OAuthIntrospectionService oauthIntrospectionService;
	
	@Autowired
	public OAuthIntrospectionController(OAuthIntrospectionService oauthIntrospectionService) {
		super();
		this.oauthIntrospectionService = oauthIntrospectionService;
	}

	@PostMapping(value = "/oauth/introspection",
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> introspection(HttpServletRequest httpReq,
			HttpServletResponse httpRes,
			@RequestHeader HttpHeaders httpHeaders,
			@RequestParam MultiValueMap<String, String> values) {
		
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		ResponseEntity<?> resp = oauthIntrospectionService.introspection(httpReq, httpRes);
		
		return resp;
	}
}
