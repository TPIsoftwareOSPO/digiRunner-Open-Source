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

import tpi.dgrv4.gateway.service.OAuthApproveService;
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
public class OAuthApproveController {
	
//	@Autowired
//	private OAuthApproveService oauthApproveService;
//	
//	@PostMapping(value = "/oauth/approve", 
//			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, // 使用 Form Urlencoded 格式
//			produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<?> approve(HttpServletRequest httpReq, 
//			HttpServletResponse httpRes, 
//			@RequestHeader HttpHeaders httpHeaders,
//			@RequestParam MultiValueMap<String, String> values)
//			throws Exception {
// 
//		ResponseEntity<?> resp = oauthApproveService.approve(httpReq, httpRes);
//		
//		return resp;
//	}
}
