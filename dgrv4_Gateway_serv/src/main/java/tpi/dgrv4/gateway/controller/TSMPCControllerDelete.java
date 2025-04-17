package tpi.dgrv4.gateway.controller;

import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.service.TSMPCServiceDelete;
@Deprecated
@RestController
public class TSMPCControllerDelete {
	
	private TSMPCServiceDelete service;
	
	@Autowired
	public TSMPCControllerDelete(TSMPCServiceDelete service) {
		super();
		this.service = service;
	}

	@DeleteMapping(value = {"/tsmpc/*/*/**", "/tsmpg/*/*/**"})
	public Callable dispatch(HttpServletRequest httpReq, 
			HttpServletResponse httpRes,
			@RequestHeader HttpHeaders headers, 
			@RequestBody(required = false) String payload) {
		
		return () -> {
			ResponseEntity<?> resp = service.forwardToDelete(headers, httpReq, httpRes, payload);
			
			// 計算API每秒轉發吞吐量
			GatewayFilter.setApiRespThroughput();

			return resp;
		};
	}
}
