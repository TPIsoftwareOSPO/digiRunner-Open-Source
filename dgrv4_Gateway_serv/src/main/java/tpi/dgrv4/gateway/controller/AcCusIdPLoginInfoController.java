package tpi.dgrv4.gateway.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.gateway.service.AcCusIdPLoginUrlService;
import tpi.dgrv4.gateway.vo.AcCusIdPLoginUrl;

@RestController
public class AcCusIdPLoginInfoController {

	private AcCusIdPLoginUrlService acCusIdPLoginUrlService;

	@Autowired
	public AcCusIdPLoginInfoController(AcCusIdPLoginUrlService acCusIdPLoginUrlService) {
		super();
		this.acCusIdPLoginUrlService = acCusIdPLoginUrlService;
	}

	@GetMapping(value = { "/dgrv4/ssotoken/acCusIdp/login/getCusLoginUrl" })
	public List<AcCusIdPLoginUrl> getCusLoginUrl(HttpServletRequest httpReq, HttpServletResponse httpResp, //
			@RequestHeader HttpHeaders httpHeaders) {

		return acCusIdPLoginUrlService.getCusLoginUrl();
	}
	
	//因為弱掃掃到不能用GET,所以增加POST
	@PostMapping(value = { "/dgrv4/ssotoken/acCusIdp/login/getCusLoginUrl" })
	public List<AcCusIdPLoginUrl> getCusLoginUrlByPost(HttpServletRequest httpReq, HttpServletResponse httpResp, //
			@RequestHeader HttpHeaders httpHeaders) {

		return acCusIdPLoginUrlService.getCusLoginUrl();
	}
}
