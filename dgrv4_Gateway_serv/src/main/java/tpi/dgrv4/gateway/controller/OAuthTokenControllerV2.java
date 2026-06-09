package tpi.dgrv4.gateway.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.gateway.constant.DgrTokenVersion;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CommForwardProcService;
import tpi.dgrv4.gateway.service.OAuthTokenService;

/**
 * v2 版: <br>
 * 使取得的 Access token 符合 OIDC <br>
 * (目前僅支援 client_credentials) <br>
 * <br>
 * 1. Access token 內容修改 <br>
 *   1.1. header 增加 kid <br>
 *     例如. "kid": "f818d971-8dbb-47b3-9291-f42b1ea06e18" <br>
 *   1.2. payload 
 *     增加 "ver": "2.0", 版本識別 <br>
 *     增加 iss, 需為 v2 的, 例如. "iss": "https://localhost:18080/dgrv4/ssotoken/v2/OIDC" <br>
 *     增加 sub, 等於 client_id <br>
 *     更改 scope 值, 原本 "", 改為 "profile", "email" <br>
 *     更改 aud 值, 原本 "YWRtaW5BUEk"(adminAPI), 改為 "digiRunner" <br>
 *   1.3. 用 JWKS endpoint 的 private key 簽章 <br>
 * <br>
 * 2. well-known url 內容<br>
 *   2.1. 原本 v1 版 <br>
 *     https://localhost:18080/dgrv4/ssotoken/OIDC/.well-known/openid-configuration <br>
 *     裡面的 "token_endpoint" : "https://localhost:18080/oauth/token" <br>
 *     增加 v2 版: <br>
 *     https://localhost:18080/dgrv4/ssotoken/v2/OIDC/.well-known/openid-configuration <br>
 *     裡面的 "token_endpoint" : "https://localhost:18080/oauth/v2/token" <br>
 */

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@RestController
public class OAuthTokenControllerV2 {
	
	private final OAuthTokenService oauthTokenService;
	private final CommForwardProcService commForwardProcService;

	// v2
	@PostMapping(value = "/oauth/v2/token",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE,//使用 Form Data 格式
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getOauthToken(HttpServletRequest httpReq, 
			HttpServletResponse httpResp,
			@RequestHeader HttpHeaders headers) throws IOException{
		
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】【1】--");
		ResponseEntity<?> resp = oauthTokenService.getOAuthToken(httpReq, headers, httpResp, DgrTokenVersion.PATH_V2);
		
		String uuid = UUID.randomUUID().toString();
		TPILogger.tl.trace("\n--【LOGUUID】【" + uuid + "】【End OAuth_Token】--\n" + 
				commForwardProcService.getLogResp(resp).toString());
		
		return resp;
	}
	
	// v2
	@PostMapping(value = "/oauth/v2/token", 
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,// 使用 Form Urlencoded 格式
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> token(HttpServletRequest httpReq, 
			HttpServletResponse httpResp,
			@RequestHeader HttpHeaders headers,
			@RequestParam MultiValueMap<String, String> values)
			throws Exception {
		
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】【2】--");
		
		ResponseEntity<?> resp = oauthTokenService.getOAuthToken(httpReq, headers, httpResp, DgrTokenVersion.PATH_V2);
		
		String uuid = UUID.randomUUID().toString();
		TPILogger.tl.trace("\n--【LOGUUID】【" + uuid + "】【End OAuth_Token】--\n" + 
				commForwardProcService.getLogResp(resp).toString());
		
		return resp;
	}
	
	/*
	 *  for cookie token,
	 *  Request 沒有表頭和表身資料
	 */
	// v2
	@PostMapping(value = "/oauth/v2/token", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> tokenForCookieToken(HttpServletRequest httpReq, 
			HttpServletResponse httpResp,
			@RequestHeader HttpHeaders headers,
			@RequestParam MultiValueMap<String, String> values)
					throws Exception {

		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】【3】--");
		ResponseEntity<?> resp = oauthTokenService.getOAuthToken(httpReq, headers, httpResp, DgrTokenVersion.PATH_V2);
		
		String uuid = UUID.randomUUID().toString();
		TPILogger.tl.trace("\n--【LOGUUID】【" + uuid + "】【End OAuth_Token】--\n" + 
				commForwardProcService.getLogResp(resp).toString());
		
		return resp;
	}
}
