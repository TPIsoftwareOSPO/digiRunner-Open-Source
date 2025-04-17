package tpi.dgrv4.gateway.controller;

import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.service.TSMPCServicePostRaw;

@RestController
public class TSMPCControllerPostRaw {
	
	private TSMPCServicePostRaw service;
	
	@Autowired
	public TSMPCControllerPostRaw(TSMPCServicePostRaw service) {
		super();
		this.service = service;
	}


	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
	@RequestMapping(value = {"/tsmpc/*/*/**", "/tsmpg/*/*/**"}, 
			produces = MediaType.ALL_VALUE)
	public Callable dispatch(HttpServletRequest httpReq, 
			HttpServletResponse httpRes,
			@RequestHeader HttpHeaders headers, 
			@RequestBody(required = false) String payload) {
		
		return () -> {
			ResponseEntity<?> resp = service.forwardToPostRawData(headers, httpReq, httpRes, payload);
			
			// 計算API每秒轉發吞吐量
			GatewayFilter.setApiRespThroughput();

			return resp;
		};
	}
}
