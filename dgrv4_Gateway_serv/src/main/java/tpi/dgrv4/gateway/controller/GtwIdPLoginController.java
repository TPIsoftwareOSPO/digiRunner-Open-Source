package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.GtwIdPLoginService;

/**
 * @author Mini
 * JDBC IdP流程中, user在登入畫面輸入 name 和 pw 後,
 * 調用此 API 做 user name 和 pw 驗證
 */
@RestController
public class GtwIdPLoginController {
	
	GtwIdPLoginService service;
	
	@Autowired
	public GtwIdPLoginController(GtwIdPLoginService service) {
		super();
		this.service = service;
	}

	// 前端使用
	@GetMapping(value = "/dgrv4/ssotoken/gtwidp/{idPType}/gtwlogin",
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> gtwIdPLogin_get(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp,
			@PathVariable("idPType") String idPType){
		
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		try {
			ResponseEntity<?> respEntity = service.gtwIdPLogin(httpHeaders, httpReq, httpResp, idPType);
			return respEntity;
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
	

	@PostMapping(value = "/dgrv4/ssotoken/gtwidp/{idPType}/gtwlogin", 
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, // 使用 Form Urlencoded 格式
		produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> gtwIdPLogin(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			@PathVariable("idPType") String idPType) {

		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");

		try {
			ResponseEntity<?> respEntity = service.gtwIdPLogin(httpHeaders, httpReq, httpResp, idPType);
			return respEntity;
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}