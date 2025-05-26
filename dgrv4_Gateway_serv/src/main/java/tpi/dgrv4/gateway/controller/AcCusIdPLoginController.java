package tpi.dgrv4.gateway.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.gateway.service.AcCusIdPLoginService;
import tpi.dgrv4.gateway.util.CusAcLoginStateStore;
import tpi.dgrv4.gateway.util.CusGatewayLoginStateStore;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class AcCusIdPLoginController {

	private AcCusIdPLoginService acCusIdPLoginService;

	@Autowired
	public AcCusIdPLoginController(AcCusIdPLoginService acCusIdPLoginService) {
		super();
		this.acCusIdPLoginService = acCusIdPLoginService;
	}

	@GetMapping(value = { "/dgrv4/ssotoken/acCusIdp/login/{idpId}", "/dgrv4/ssotoken/acCusIdp/login" })
	public void preProcessCusAcLogin(HttpServletRequest httpReq, HttpServletResponse httpResp, //
			@RequestHeader HttpHeaders httpHeaders, @RequestParam Map<String, String> queryParams,
			@PathVariable(name = "idpId", required = false) String idpId) {

		acCusIdPLoginService.preProcessCusAcLogin(httpHeaders, httpReq, httpResp, idpId, queryParams);
	}

	@PostMapping(value = "/dgrv4/11/ssotoken/acCusIdp/info", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> getCusAcLoginStateStoreStatus(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<Map<String, String>> req) {

		try {

			TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);

			Map<String, Object> status = CusAcLoginStateStore.INSTANCE.getDetailedStatusAsMap(true);

			return status;

		} catch (Exception e) {
			ITPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			// Even in case of error, try to get basic status if possible
			Map<String, Object> errorStatus = new HashMap<>();

			// Try to get at least basic information with a simplified approach
			errorStatus.put("error", "An error occurred while retrieving status information");
			errorStatus.put("timestamp", System.currentTimeMillis());

			return errorStatus;
		}

	}
	
	@PostMapping(value = "/dgrv4/11/ssotoken/gatewayCusIdp/info", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> getCusGatewayLoginStateStoreStatus(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<Map<String, String>> req) {

		try {

			TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);

			Map<String, Object> status = CusGatewayLoginStateStore.INSTANCE.getDetailedStatusAsMap(true);

			return status;

		} catch (Exception e) {
			ITPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			// Even in case of error, try to get basic status if possible
			Map<String, Object> errorStatus = new HashMap<>();

			// Try to get at least basic information with a simplified approach
			errorStatus.put("error", "An error occurred while retrieving status information");
			errorStatus.put("timestamp", System.currentTimeMillis());

			return errorStatus;
		}

	}


}
