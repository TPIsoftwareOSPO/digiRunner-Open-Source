package tpi.dgrv4.gateway.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.keeper.TPILogger;

@RestController
public class AcIdPMockJSCallbackController {
	

	@GetMapping(value = "/dgrv4/mockac/idpsso/accallback")
	public void compSync(@RequestHeader HttpHeaders headers, 
			@RequestParam String dgRcode, 
			@RequestParam String msg,
			HttpServletRequest req, 
			HttpServletResponse resp) throws IOException {
		
		String redirect = String.format("/dgrv4/ssotoken/acidp/oauth/token"
				+ "?dgRcode=%s"
				+ "&msg=%s", 
				dgRcode,
				msg);
		
		TPILogger.tl.debug("Redirect to URL: " + redirect);
		resp.sendRedirect(redirect);
	}
}
