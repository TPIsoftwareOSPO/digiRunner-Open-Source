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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.aspect.annotation.ThroughputPoint;
import tpi.dgrv4.gateway.component.ControllerExceptionHandler;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.DGRCServicePostForm;

@RestController
public class DGRCControllerPostForm {

	@Setter(onMethod_ = @Autowired)
	private DGRCServicePostForm service;

	@SuppressWarnings("java:S3752") // allow all methods for sonarqube scan
    @ThroughputPoint
	@RequestMapping(value = "/dgrc/**", 
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE, // 使用 Form Data 格式
			produces = MediaType.APPLICATION_JSON_VALUE)
	public void dispatch(HttpServletRequest httpReq,
														 HttpServletResponse httpRes,
														 @RequestHeader HttpHeaders headers) throws Exception {

        service.forwardToPostFormData(headers, httpReq, httpRes);
	}
}
