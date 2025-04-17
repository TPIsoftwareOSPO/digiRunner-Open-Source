package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.GtwIdPApproveService;

/**
 * @author Mini
 */
@RestController
public class GtwIdPApproveController {
	
	private GtwIdPApproveService service;

	@Autowired
	public GtwIdPApproveController(GtwIdPApproveService service) {
		super();
		this.service = service;
	}

	// 前端使用
	@GetMapping(value = "/dgrv4/ssotoken/gtwidp/{idPType}/approve", 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> gtwIdPApprove_get(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			@PathVariable("idPType") String idPType) {
		
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		try {
			ResponseEntity<?> respEntity = service.gtwIdPApprove(httpHeaders, httpReq, httpResp, idPType);
			return respEntity;

		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
	
	@PostMapping(value = "/dgrv4/ssotoken/gtwidp/{idPType}/approve", 
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, // 使用 Form Urlencoded 格式
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> gtwIdPApprove(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			@PathVariable("idPType") String idPType) {
		try {
			ResponseEntity<?> respEntity = service.gtwIdPApprove(httpHeaders, httpReq, httpResp, idPType);
			return respEntity;

		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}
