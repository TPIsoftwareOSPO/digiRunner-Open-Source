package tpi.dgrv4.gateway.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.constant.DgrTokenVersion;
import tpi.dgrv4.gateway.service.GtwIdPWellKnownService;

/**
 * @author Mini 
 * well-known url 內容<br>
 * 1. 原本 v1 版 <br>
 *   https://localhost:18080/dgrv4/ssotoken/OIDC/.well-known/openid-configuration <br>
 *   裡面的 "token_endpoint" : "https://localhost:18080/oauth/token" <br>
 * 2. 增加 v2 版 <br>
 *   https://localhost:18080/dgrv4/ssotoken/v2/OIDC/.well-known/openid-configuration <br>
 *   差異在裡面的 "token_endpoint" : "https://localhost:18080/oauth/v2/token" <br>
 */

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@RestController
public class GtwIdPWellKnownController {

	private final GtwIdPWellKnownService service;

	// v1
	@GetMapping(value = "/dgrv4/ssotoken/{idPType}/.well-known/openid-configuration", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getGtwIdPWellKnown(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			@PathVariable("idPType") String idPType) {
		try {
			return service.getGtwIdPWellKnown(httpHeaders, httpReq, httpResp, idPType, DgrTokenVersion.PATH_V1);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
	
	// v2
	@GetMapping(value = "/dgrv4/ssotoken/v2/{idPType}/.well-known/openid-configuration", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getGtwIdPWellKnownV2(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			@PathVariable("idPType") String idPType) {
		try {
			return service.getGtwIdPWellKnown(httpHeaders, httpReq, httpResp, idPType, DgrTokenVersion.PATH_V2);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}