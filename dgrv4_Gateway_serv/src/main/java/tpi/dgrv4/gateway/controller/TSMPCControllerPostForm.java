package tpi.dgrv4.gateway.controller;

import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.aspect.annotation.ThroughputPoint;
import tpi.dgrv4.gateway.service.TSMPCServicePostForm;

@RestController
public class TSMPCControllerPostForm {
	
	private TSMPCServicePostForm service;
	
	@Autowired
	public TSMPCControllerPostForm(TSMPCServicePostForm service) {
		super();
		this.service = service;
	}

	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
	@ThroughputPoint
	@RequestMapping(value = {"/tsmpc/*/*/**", "/tsmpg/*/*/**"},
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE, // 使用 Form Data 格式
			produces = MediaType.ALL_VALUE)
	public ResponseEntity<?> dispatch(HttpServletRequest httpReq,
			HttpServletResponse httpRes, 
			@RequestHeader HttpHeaders headers) throws Exception {

		return service.forwardToPostFormData(headers, httpReq, httpRes);

	}
}
