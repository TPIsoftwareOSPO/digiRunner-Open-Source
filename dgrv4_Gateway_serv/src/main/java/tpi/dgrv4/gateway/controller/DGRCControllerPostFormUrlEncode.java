package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.aspect.annotation.ThroughputPoint;
import tpi.dgrv4.gateway.component.ControllerExceptionHandler;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.DGRCServicePostFormUrlEncoded;

@RestController
public class DGRCControllerPostFormUrlEncode {

	@Setter(onMethod_ = @Autowired)
	private DGRCServicePostFormUrlEncoded service;


	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
	@ThroughputPoint
    @RequestMapping(value = "/dgrc/**",
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, // 使用 application/x-www-form-urlencoded 格式
			produces = MediaType.ALL_VALUE)
	public void dispatch(HttpServletRequest httpReq,
														 HttpServletResponse httpRes,
														 @RequestHeader HttpHeaders headers,
														 @RequestParam MultiValueMap< String, String > values) throws Exception {
        service.forwardToPostFormUrlEncoded(headers, httpReq, httpRes, values);
	}
}
