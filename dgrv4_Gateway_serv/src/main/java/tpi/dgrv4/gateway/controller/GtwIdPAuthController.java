package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.Optional;

import org.apache.poi.sl.usermodel.ObjectMetaData.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.GtwCusIdPLoginService;
import tpi.dgrv4.gateway.service.GtwIdPAuthService;

/**
 * @author Mini
 */
@RestController
public class GtwIdPAuthController {

	private GtwIdPAuthService service;
	private GtwCusIdPLoginService gtwCusIdPLoginService;

	@Autowired
	public GtwIdPAuthController(GtwIdPAuthService service, GtwCusIdPLoginService gtwCusIdPLoginService) {
		super();
		this.service = service;
		this.gtwCusIdPLoginService = gtwCusIdPLoginService;
	}

	@GetMapping(value = "/dgrv4/ssotoken/gtwidp/{idPType}/authorization", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> gtwIdPAuth(@RequestHeader HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, @PathVariable("idPType") String idPType,
			@RequestParam Map<String, String> queryParams) {

		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");

		if (DgrIdPType.CUS.equalsIgnoreCase(idPType)) {
			gtwCusIdPLoginService.gtwCusIdPLogin(httpHeaders, httpReq, httpResp, queryParams);
			return null;
		}

		try {
			ResponseEntity<?> respEntity = service.gtwIdPAuth(httpHeaders, httpReq, httpResp, idPType);
			return respEntity;

		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}
