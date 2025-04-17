package tpi.dgrv4.gateway.controller;

import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.service.ComposerSwaggerService;

@RestController
public class ComposerSwaggerController {
	
	private ComposerSwaggerService service;
	
	@Autowired
	public ComposerSwaggerController(ComposerSwaggerService service) {
		super();
		this.service = service;
	}

	@GetMapping(value = "/composer/swagger3.0/**")
	public  Callable dispatch(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq, 
			HttpServletResponse httpRes) {
		
		return () -> service.forwardToGet(httpHeaders, httpReq, httpRes);
	}
}
