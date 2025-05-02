package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.service.Dgrv4MockAuthZCodeService;
/**
 * 2023/04/20 目前已不使用, 改用 GTW IdP 流程, 故全部註解
 * @author Mini
 */
@RestController
public class Dgrv4MockAuthZCodeController {
	
//	@Autowired
//	private Dgrv4MockAuthZCodeService service;
//	
//	/**
//	 * 模擬 authorization_code 流程中, 
//	 * User輸入帳密+同意畫面後, 轉導到 /oauth/approve
//	 */
//	@GetMapping(value="/oauth/mock_BANK_UserLogin")
//	public ResponseEntity<?> mock_BANK_UserLogin(@RequestHeader HttpHeaders httpHeaders, 
//			HttpServletRequest httpReq, 
//			HttpServletResponse httpRes) {
//		ResponseEntity<?> resp = service.mockBankUserLogin(httpHeaders, httpReq, httpRes);
//		
//		return resp;
//	}
//	
//	/*
//	 * 模擬 authorization_code 流程中, 
//	 * 執行第2道API /oauth/approve 後,
//	 * 轉導到此API /oauth/mock_TSP, 以執行第3道 API /oauth/token
//	 */
//	@RequestMapping(value = "/oauth/mock_TSP")
//	public ResponseEntity<?> mock_TSP(@RequestHeader HttpHeaders headers,
//			@RequestParam(required = false, name = "msg") String msg, 
//			HttpServletRequest httpReq,
//			HttpServletResponse httpRes) throws Exception {
//		// [使用 AuthZ Code + redirect_uri + Client ID + Client Secret 請求 token]
//		// POST /oauth/token HTTP/1.1
//		
//		String code = httpReq.getParameter("code");
//		String redirectUri = httpReq.getRequestURL().toString();
//		ResponseEntity<?> resp = service.test_authorization_code_step3(redirectUri, code, httpReq);
//		return resp;
//	}
}
