package tpi.dgrv4.gateway.controller;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.service.AcIdPService;

@RestController
public class AcIdPController {

	private AcIdPService service;

	@Autowired
	public AcIdPController(AcIdPService service) {
		super();
		this.service = service;
	}

	@GetMapping(value = "/dgrv4/ssotoken/acidp/oauth/token")
	public Callable getAcToken(@RequestHeader HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpRes) {

		try {

			String dgRcode = httpReq.getParameter("dgRcode");

			return () -> service.getAcToken(httpHeaders, httpReq, httpRes, dgRcode);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}
