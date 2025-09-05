package tpi.dgrv4.gateway.controller;

import java.util.concurrent.CompletableFuture;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.service.DGRCServicePostRaw;

@RestController
public class DGRCControllerPostRaw {
	
	private DGRCServicePostRaw service;
	
	@Autowired
	public DGRCControllerPostRaw(DGRCServicePostRaw service) {
		super();
		this.service = service;
	}

	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
	@RequestMapping(value = "/dgrc/**",
			consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE },
			produces = MediaType.ALL_VALUE)
	public CompletableFuture<ResponseEntity<?>> dispatch(HttpServletRequest httpReq,
														 HttpServletResponse httpRes,
														 @RequestHeader HttpHeaders headers,
														 @RequestBody(required = false) String payload) throws Exception {

		String selectWorkThread = httpReq.getAttribute(GatewayFilter.SETWORK_THREAD).toString();
        CompletableFuture<ResponseEntity<?>> resp;
        if (selectWorkThread.equals(GatewayFilter.FAST)) {
            resp = service.forwardToPostRawDataAsyncFast(headers, httpReq, httpRes, payload);
        } else { // "slow"
            resp = service.forwardToPostRawDataAsync(headers, httpReq, httpRes, payload);
        }
        GatewayFilter.setApiRespThroughput();
        return resp;


    }
}
