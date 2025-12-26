package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.aspect.annotation.ThroughputPoint;
import tpi.dgrv4.gateway.component.ControllerExceptionHandler;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.DGRCServiceGet;

@RestController
public class DGRCControllerGet {

    @Setter(onMethod_ = @Autowired)
	private DGRCServiceGet service;

    @ThroughputPoint
	@GetMapping(value = "/dgrc/**")
	public void dispatch(@RequestHeader HttpHeaders httpHeaders,
														 HttpServletRequest httpReq,
														 HttpServletResponse httpRes) throws Exception {

        service.forwardToGet(httpHeaders, httpReq, httpRes);
	}
}
