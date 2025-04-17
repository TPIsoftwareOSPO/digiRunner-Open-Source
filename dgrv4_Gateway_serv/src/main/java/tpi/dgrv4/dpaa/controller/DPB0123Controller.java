package tpi.dgrv4.dpaa.controller;


import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.dpaa.service.DPB0123ApiService;
import tpi.dgrv4.dpaa.vo.DPB0123ApiResp;
import tpi.dgrv4.gateway.keeper.TPILogger;
 

/**
 * [對接企業]對接程式,確認User是否有登入
 */
@RestController
public class DPB0123Controller {

	private TPILogger logger = TPILogger.tl;
	
	private DPB0123ApiService service;
	
	@Autowired
	public DPB0123Controller(DPB0123ApiService service) {
		super();
		this.service = service;
	}


	@GetMapping(value = "/dgrv4/ssotoken/DPB0123", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public DPB0123ApiResp doubleCheckLogin(HttpServletRequest req) {
		
		DPB0123ApiResp resp = null;
		try {
			resp = service.doubleCheckLogin(req);
		} catch (Exception e) {
			// 不應該會執行到Exception這一段
			this.logger.error("DPB0123-doubleCheckLogin error: " + e.getMessage());
			resp = new DPB0123ApiResp();
			resp.setCode("1297");
			resp.setMessage("Execution error");
			resp.setLogin("N");
		}

		return resp;
	}
}