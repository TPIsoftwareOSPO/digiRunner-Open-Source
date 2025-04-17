package tpi.dgrv4.gateway.controller;

import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.escape.CheckmarxUtils;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.AcIdPLoginService;

/**
 * @author Mini
 * 
 * 驗證 LDAP User 帳號、密碼
 */

@RestController
public class AcIdPLoginController {
	
	private AcIdPLoginService service;

	@Autowired
	public AcIdPLoginController(AcIdPLoginService service) {
		super();
		this.service = service;
	}

	// 模擬畫面測試用
	@GetMapping(value = "/dgrv4/ssotoken/acidp/{idPType}/acIdPLogin")
	public Callable acIdPLogin_get(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			@PathVariable("idPType") String idPType) {

		login(httpHeaders, httpReq, httpResp, idPType);
		return null;
	}
	
	// 前端調用
	@PostMapping(value = "/dgrv4/ssotoken/acidp/{idPType}/acIdPLogin",
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE) // 使用 Form Urlencoded 格式
	public Callable acIdPLogin(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			@PathVariable("idPType") String idPType) {
		
		login(httpHeaders, httpReq, httpResp, idPType);
		return null;
	}
	
	private void login(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpResp,
			String idPType) {

		TPILogger.tl.info("\n--【" + httpReq.getRequestURI().toString() + "】--");

		try {
			service.acIdPLogin(httpHeaders, httpReq, httpResp, idPType);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}
