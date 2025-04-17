package tpi.dgrv4.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.gateway.service.GtwIdPVgroupService;
import tpi.dgrv4.gateway.vo.GtwIdPVgroupResp;

/**
 * 取得Client 虛擬群組的API清單
 * 
 * @author Mini
 *
 */
@RestController
public class GtwIdPVgroupController {
	
	private GtwIdPVgroupService service;

	@Autowired
	public GtwIdPVgroupController(GtwIdPVgroupService service) {
		super();
		this.service = service;
	}

	@GetMapping(value = "/dgrv4/ssotoken/gtwidp/{idPType}/getVgroupList")
	public ResponseEntity<?> getVgroupList(@RequestHeader HttpHeaders headers, 
			HttpServletRequest httpReq, 
			HttpServletResponse httpRes,
			@PathVariable("idPType") String idPType) throws Exception{
		GtwIdPVgroupResp resp = service.getVgroupList(headers, httpReq, httpRes, idPType);
		ResponseEntity<?> respEntity = new ResponseEntity<GtwIdPVgroupResp>(resp, HttpStatus.OK);
		
		return respEntity;
	}
}
