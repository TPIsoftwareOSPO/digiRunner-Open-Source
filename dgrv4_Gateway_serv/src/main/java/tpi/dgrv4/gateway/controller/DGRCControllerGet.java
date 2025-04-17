package tpi.dgrv4.gateway.controller;

import java.util.concurrent.CompletableFuture;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.service.DGRCServiceGet;

@RestController
public class DGRCControllerGet {
	
	private DGRCServiceGet service;
	
	@Autowired
	public DGRCControllerGet(DGRCServiceGet service) {
		super();
		this.service = service;
	}

	@GetMapping(value = "/dgrc/**")
	public CompletableFuture<ResponseEntity<?>> dispatch(@RequestHeader HttpHeaders httpHeaders,
														 HttpServletRequest httpReq,
														 HttpServletResponse httpRes) throws Exception {
		
		String selectWorkThread = httpReq.getAttribute(GatewayFilter.SETWORK_THREAD).toString();
		if (selectWorkThread.equals(GatewayFilter.FAST)) {
			var resp = service.forwardToGetAsyncFast(httpHeaders, httpReq, httpRes);
			GatewayFilter.setApiRespThroughput();
			return resp;
		} else { // "slow"
			var resp = service.forwardToGetAsyncSlow(httpHeaders, httpReq, httpRes);
			GatewayFilter.setApiRespThroughput();
			return resp;
		}

	}
}
