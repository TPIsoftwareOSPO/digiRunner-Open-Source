package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.service.OAuthAuthorizationService;
/**
 * 2023/04/20 目前已不使用, 改用 GTW IdP 流程, 故全部註解
 * @author Mini
 */

/**
 * @author Mini
 * 
 * unit test 寫在 OAuthTokenControllerMockTest.test_authorization_code()
 */
@RestController
public class OAuthAuthorizationController {
	
//	@Autowired
//	private OAuthAuthorizationService oauthAuthorizationService;
//	
//	@GetMapping(value="/oauth/authorization")
//	public ResponseEntity<?> authorization(@RequestHeader HttpHeaders httpHeaders, 
//			HttpServletRequest httpReq, 
//			HttpServletResponse httpRes) {
//		
//		ResponseEntity<?> resp = oauthAuthorizationService.authorization(httpHeaders, httpReq, httpRes);
//		return resp;
//	}
}
