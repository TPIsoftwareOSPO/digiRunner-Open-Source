package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.service.GtwIdPWellKnownService;

/**
 * @author Mini 
 * 提供 well-known 內容
 */
@RestController
public class GtwIdPWellKnownController {

	private GtwIdPWellKnownService service;

	@Autowired
	public GtwIdPWellKnownController(GtwIdPWellKnownService service) {
		super();
		this.service = service;
	}

	@GetMapping(value = "/dgrv4/ssotoken/{idPType}/.well-known/openid-configuration", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getGtwIdPWellKnown(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			@PathVariable("idPType") String idPType) {
		try {
			return service.getGtwIdPWellKnown(httpHeaders, httpReq, httpResp, idPType);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}