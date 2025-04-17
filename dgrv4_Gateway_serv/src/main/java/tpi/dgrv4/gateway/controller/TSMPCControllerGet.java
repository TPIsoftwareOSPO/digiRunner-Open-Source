package tpi.dgrv4.gateway.controller;

import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.service.TSMPCServiceGet;

@RestController
public class TSMPCControllerGet {

	private TSMPCServiceGet service;
	
	@Autowired
	public TSMPCControllerGet(TSMPCServiceGet service) {
		super();
		this.service = service;
	}

	@GetMapping(value = {"/tsmpc/*/*/**", "/tsmpg/*/*/**"})
	public Callable dispatch(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq, 
			HttpServletResponse httpRes) {

		return () -> {
			ResponseEntity<?> resp = service.forwardToGet(httpHeaders, httpReq, httpRes);

			// 計算API每秒轉發吞吐量
			GatewayFilter.setApiRespThroughput();

			return resp;
		};
	}
}