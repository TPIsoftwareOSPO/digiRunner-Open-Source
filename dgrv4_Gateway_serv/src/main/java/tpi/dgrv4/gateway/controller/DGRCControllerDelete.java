package tpi.dgrv4.gateway.controller;

import java.util.concurrent.CompletableFuture;

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
import tpi.dgrv4.gateway.service.DGRCServiceDelete;

@RestController
public class DGRCControllerDelete {
	
	private DGRCServiceDelete service;
	
	@Autowired
	public DGRCControllerDelete(DGRCServiceDelete service) {
		super();
		this.service = service;
	}

	@DeleteMapping(value = "/dgrc/**")
	public CompletableFuture<ResponseEntity<?>> dispatch(HttpServletRequest httpReq,
														 HttpServletResponse httpRes,
														 @RequestHeader HttpHeaders headers,
														 @RequestBody(required = false) String payload) throws Exception {
		String selectWorkThread = httpReq.getAttribute(GatewayFilter.SETWORK_THREAD).toString();
		CompletableFuture<ResponseEntity<?>> resp;
		if (selectWorkThread.equals(GatewayFilter.FAST)) {
			resp = service.forwardToDeleteAsyncFast(headers, httpReq, httpRes, payload);
		} else {
			resp = service.forwardToDeleteAsync(headers, httpReq, httpRes, payload);
		}

		GatewayFilter.setApiRespThroughput();
		return resp;

//		return () -> {
////			ResponseEntity<?> resp = service.forwardToDelete(headers, httpReq, httpRes, payload);
//
//			var resp = service.forwardToDeleteAsync(headers, httpReq, httpRes, payload);
//			// 計算API每秒轉發吞吐量
//			GatewayFilter.setApiRespThroughput();
//
//			return resp;
//		};
	}
}
