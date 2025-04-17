package tpi.dgrv4.gateway.controller;

import java.util.concurrent.CompletableFuture;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.service.DGRCServicePostForm;

@RestController
public class DGRCControllerPostForm {
	
	private DGRCServicePostForm service;
	
	@Autowired
	public DGRCControllerPostForm(DGRCServicePostForm service) {
		super();
		this.service = service;
	}

	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
	@RequestMapping(value = "/dgrc/**", 
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE, // 使用 Form Data 格式
			produces = MediaType.APPLICATION_JSON_VALUE)
	public CompletableFuture<ResponseEntity<?>> dispatch(HttpServletRequest httpReq,
														 HttpServletResponse httpRes,
														 @RequestHeader HttpHeaders headers) throws Exception {

		String selectWorkThread = httpReq.getAttribute(GatewayFilter.SETWORK_THREAD).toString();
		CompletableFuture<ResponseEntity<?>> resp;
		if (selectWorkThread.equals(GatewayFilter.FAST)) {
			resp = service.forwardToPostFormDataAsyncFast(headers, httpReq, httpRes);
		} else {
			resp = service.forwardToPostFormDataAsync(headers, httpReq, httpRes);
		}

		GatewayFilter.setApiRespThroughput();
		return resp;
//		return () -> {
////			ResponseEntity<?> resp = service.forwardToPostFormData(headers, httpReq, httpRes);
//			var resp = service.forwardToPostFormDataAsync(headers, httpReq, httpRes);
//
//			// 計算API每秒轉發吞吐量
//			GatewayFilter.setApiRespThroughput();
//
//			return resp;
//		};
	}
}
