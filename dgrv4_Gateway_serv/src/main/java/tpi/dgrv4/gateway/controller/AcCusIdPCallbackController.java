package tpi.dgrv4.gateway.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.gateway.service.AcCusIdPCallbackService;

@RestController
public class AcCusIdPCallbackController {

	private AcCusIdPCallbackService acCusIdPCallbackService;

	@Autowired
	public AcCusIdPCallbackController(AcCusIdPCallbackService acCusIdPCallbackService) {
		super();
		this.acCusIdPCallbackService = acCusIdPCallbackService;
	}

	@GetMapping(value = "/dgrv4/ssotoken/acCusIdp/callback")
	public void processCusAcLogin(@RequestHeader HttpHeaders httpHeaders, //
			HttpServletRequest httpReq, //
			HttpServletResponse httpResp, //
			@RequestParam Map<String, String> queryParams) {

		acCusIdPCallbackService.processCusAcLogin(httpHeaders, httpReq, httpResp, queryParams);
	}
}
