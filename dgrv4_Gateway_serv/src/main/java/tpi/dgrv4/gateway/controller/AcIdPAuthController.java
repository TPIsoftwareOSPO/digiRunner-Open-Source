package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.service.AcIdPAuthService;

/**
 * 打 IdP 的 auth API, 會重新導向到 OAuth 同意畫面
 */

@RestController
public class AcIdPAuthController {

	private AcIdPAuthService service;

	@Autowired
	public AcIdPAuthController(AcIdPAuthService service) {
		super();
		this.service = service;
	}

	@GetMapping(value = "/dgrv4/ssotoken/acidp/{idPType}/acIdPAuth")
	public void acIdPAuth(@RequestHeader HttpHeaders headers, 
			HttpServletRequest req, 
			HttpServletResponse resp,
			@PathVariable("idPType") String idPType){

		try {
			service.acIdPAuth(headers, idPType, req, resp);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}
