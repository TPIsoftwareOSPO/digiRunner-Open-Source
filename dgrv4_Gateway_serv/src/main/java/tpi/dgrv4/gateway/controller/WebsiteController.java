package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import tpi.dgrv4.gateway.service.SmartOnFhirProxyService;
import tpi.dgrv4.gateway.service.WebsiteService;

@RestController
public class WebsiteController {

	private WebsiteService service;
	private SmartOnFhirProxyService smartOnFhirProxyService;

	@Autowired
	public WebsiteController(WebsiteService service, SmartOnFhirProxyService smartOnFhirProxyService) {
		super();
		this.service = service;
		this.smartOnFhirProxyService = smartOnFhirProxyService;
	}

	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
	@RequestMapping(value = "/website/{websiteName}/**")
	public void resource(@PathVariable("websiteName") String websiteName, @RequestHeader HttpHeaders httpHeaders,
			HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) String payload) throws Throwable {

		service.resource(httpHeaders, request, response, websiteName, payload);

	}

	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
	@RequestMapping(value = "/http-api/**")
	public void httpApiComposer(@RequestHeader HttpHeaders httpHeaders,
			HttpServletRequest request, HttpServletResponse response) throws Throwable {

		service.resource(httpHeaders, request, response, "httpApiComposer", null);

	}

	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
	@CrossOrigin
	@RequestMapping(value = "/smart-on-fhir/{websiteName}/**")
	public void smartOnFhir(@PathVariable("websiteName") String websiteName, @RequestHeader HttpHeaders httpHeaders,
	HttpServletRequest request, HttpServletResponse response) throws Throwable {

		smartOnFhirProxyService.resource(httpHeaders, request, response, websiteName);

	}

}
