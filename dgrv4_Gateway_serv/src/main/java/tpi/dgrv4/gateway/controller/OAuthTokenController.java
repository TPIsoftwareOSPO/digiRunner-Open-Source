package tpi.dgrv4.gateway.controller;

import java.io.IOException;
import java.util.UUID;

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
import tpi.dgrv4.gateway.service.CommForwardProcService;
import tpi.dgrv4.gateway.service.OAuthTokenService;

@RestController
public class OAuthTokenController {
	
	private OAuthTokenService oauthTokenService;
	private CommForwardProcService commForwardProcService;
 
	@Autowired
	public OAuthTokenController(OAuthTokenService oauthTokenService, CommForwardProcService commForwardProcService) {
		super();
		this.oauthTokenService = oauthTokenService;
		this.commForwardProcService = commForwardProcService;
	}

	@PostMapping(value = "/oauth/token",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE,//使用 Form Data 格式
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getOauthToken(HttpServletRequest httpReq, 
			HttpServletResponse httpResp,
			@RequestHeader HttpHeaders headers) throws IOException{
		
//		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】【1】--");
		ResponseEntity<?> resp = oauthTokenService.getOAuthToken(httpReq, headers, httpResp);
		
		String uuid = UUID.randomUUID().toString();
		TPILogger.tl.trace("\n--【LOGUUID】【" + uuid + "】【End OAuth_Token】--\n" + 
				commForwardProcService.getLogResp(resp).toString());
		
		return resp;
	}
	
	@PostMapping(value = "/oauth/token", 
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,// 使用 Form Urlencoded 格式
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> token(HttpServletRequest httpReq, 
			HttpServletResponse httpResp,
			@RequestHeader HttpHeaders headers,
			@RequestParam MultiValueMap<String, String> values)
			throws Exception {
		
//		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】【2】--");
		
		ResponseEntity<?> resp = oauthTokenService.getOAuthToken(httpReq, headers, httpResp);
		
		String uuid = UUID.randomUUID().toString();
		TPILogger.tl.trace("\n--【LOGUUID】【" + uuid + "】【End OAuth_Token】--\n" + 
				commForwardProcService.getLogResp(resp).toString());
		
		return resp;
	}
	
	/*
	 *  for cookie token,
	 *  Request 沒有表頭和表身資料
	 */
	@PostMapping(value = "/oauth/token", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> token_cookie_token(HttpServletRequest httpReq, 
			HttpServletResponse httpResp,
			@RequestHeader HttpHeaders headers,
			@RequestParam MultiValueMap<String, String> values)
					throws Exception {

//		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】【3】--");
		ResponseEntity<?> resp = oauthTokenService.getOAuthToken(httpReq, headers, httpResp);
		
		String uuid = UUID.randomUUID().toString();
		TPILogger.tl.trace("\n--【LOGUUID】【" + uuid + "】【End OAuth_Token】--\n" + 
				commForwardProcService.getLogResp(resp).toString());
		
		return resp;
	}
}
