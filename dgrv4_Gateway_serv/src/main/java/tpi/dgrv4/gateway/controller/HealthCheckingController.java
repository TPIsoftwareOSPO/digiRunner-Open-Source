package tpi.dgrv4.gateway.controller;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.HealthCheckService;
import tpi.dgrv4.gateway.service.ISysInfoService;

@RestController

public class HealthCheckingController {
//	@Autowired(required = false)
	private ISysInfoService sysInfoService;
	
	private static final String NO_ENTERPRISE_SERVICE = "...No Enterprise Service...";
	
	@Setter(onMethod_ = @Autowired, onParam_ = @Qualifier("async-workers-healthcheck"))
	ThreadPoolTaskExecutor healthCheckExecutor;
	
	@Setter(onMethod_ = @Autowired)
	private HealthCheckService healthCheckService;

	@Autowired
	public HealthCheckingController(@Nullable ISysInfoService sysInfoService) {
		super();
		this.sysInfoService = sysInfoService;
	}
	
	@GetMapping(path = "/dgrv4/liveness")
	public CompletableFuture<ResponseEntity<?>> liveness(HttpServletRequest httpReq,
														 HttpServletResponse httpRes) {
		return healthCheckService.liveness(httpReq);
	}

	@GetMapping(path = {"/dgrliveness", "/liveness"})
	public CompletableFuture<ResponseEntity<?>> liveness2(HttpServletRequest httpReq,
			 HttpServletResponse httpRes) {
		return healthCheckService.liveness(httpReq);
	}
	
	@GetMapping(path = {"/dgrv4/sys-info", "/readiness"}, produces = "application/json")
	public CompletableFuture<ResponseEntity<?>> sysInfo() throws IOException {
		return CompletableFuture.supplyAsync(()-> {
			String json = "";
			try {
				if (sysInfoService != null) {
					json = sysInfoService.getSysInfo();
				} else {
					json = NO_ENTERPRISE_SERVICE;
				}
				
			} catch (JsonProcessingException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			}
			return ResponseEntity.ok(json);
		}, healthCheckExecutor);
	}
}
