package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.service.KibanaService;

//@RestController
public class KibanaController {

	private KibanaService service;
	
	@Autowired
	public KibanaController(KibanaService service) {
		super();
		this.service = service;
	}

	//由/kibana改為走 /kibana2
//	@GetMapping(value = "/kibana/login")
	public void login(@RequestHeader HttpHeaders httpHeaders, @RequestParam String reportURL,
			@RequestParam String cuuid, @RequestParam(required = false) String capikey, HttpServletRequest request,
			HttpServletResponse response) throws Throwable {

		httpHeaders.add("cuuid", cuuid);
		httpHeaders.add("capi-key", capikey);
		
		service.login(httpHeaders, reportURL, request, response);

	}

//	@RequestMapping(value = "/kibana/**")
	public void resource(@RequestHeader HttpHeaders httpHeaders, HttpServletRequest request,
			HttpServletResponse response, @RequestBody(required = false) String payload) throws Throwable {

		service.resource(httpHeaders, request, response, payload);

	}

	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
//	@RequestMapping(value = "/_plugin/kibana/**")
	public void resourceAWS(@RequestHeader HttpHeaders httpHeaders, HttpServletRequest request,
			HttpServletResponse response, @RequestBody(required = false) String payload) throws Throwable {

		service.resource(httpHeaders, request, response, payload);

	}

	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
//	@RequestMapping(value = "/_dashboards/**")
	public void resourceOpenSearch(@RequestHeader HttpHeaders httpHeaders, HttpServletRequest request,
			HttpServletResponse response, @RequestBody(required = false) String payload) throws Throwable {

		service.resource(httpHeaders, request, response, payload);

	}

}
