package tpi.dgrv4.gateway.controller;

import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.service.TSMPCServicePatch;
@Deprecated
@RestController
public class TSMPCControllerPatch {

	private TSMPCServicePatch service;

	@Autowired
	public TSMPCControllerPatch(TSMPCServicePatch service) {
		super();
		this.service = service;
	}

	@PatchMapping(value = {"/tsmpc/*/*/**", "/tsmpg/*/*/**"}, 
			produces = MediaType.ALL_VALUE)
	public Callable dispatch(HttpServletRequest httpReq, 
			HttpServletResponse httpRes,
			@RequestHeader HttpHeaders headers, 
			@RequestBody(required = false) String payload) {
				
		return () -> {
			ResponseEntity<?> resp = service.forwardToPatch(headers, httpReq, httpRes, payload);

			// 計算API每秒轉發吞吐量
			GatewayFilter.setApiRespThroughput();

			return resp;
		};
	}
}
