package tpi.dgrv4.gateway.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.web.context.request.async.DeferredResult;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.escape.ESAPI;
import tpi.dgrv4.gateway.component.ControllerExceptionHandler;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.ISysInfoService;

@RestController

public class HealthCheckingController {

	@Setter(onMethod_ = @Autowired(required = false))
	private ISysInfoService sysInfoService;
	
	private static final String NO_ENTERPRISE_SERVICE = "...No Enterprise Service...";

	private static final String LIVENESS_MSG_TEMPLATE = "dgrv4 .... alive !\n</br> %s... dgR-linkerClient-Node-Name\n</br>%s... httpReq.getRemoteAddr()\n</br>";

	@GetMapping(path = "/dgrv4/liveness")
	public ResponseEntity<String> liveness(HttpServletRequest httpReq,
									  HttpServletResponse httpRes) {

		String msg = String.format(LIVENESS_MSG_TEMPLATE, TPILogger.lcUserName, httpReq.getRemoteAddr());
		//checkmarx, Reflected XSS All Clients 
		msg = ESAPI.encoder().encodeForHTML(msg);
		return ResponseEntity.ok(msg);
	}

	@GetMapping(path = {"/dgrliveness", "/liveness"})
	public ResponseEntity<String> liveness2(HttpServletRequest httpReq,
			 HttpServletResponse httpRes) {
		return this.liveness(httpReq, httpRes);
	}
	
	@GetMapping(path = {"/dgrv4/sys-info", "/readiness"}, produces = "application/json")
	public ResponseEntity<String> sysInfo() throws IOException {

		String json = "";
		if (sysInfoService != null) {
			json = sysInfoService.getSysInfo();
		} else {
			json = NO_ENTERPRISE_SERVICE;
		}

		return ResponseEntity.ok(json);
	}
}
