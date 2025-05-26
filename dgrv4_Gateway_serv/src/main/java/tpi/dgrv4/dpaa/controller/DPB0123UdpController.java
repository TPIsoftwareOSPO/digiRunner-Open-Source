package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.dpaa.service.DPB0123UdpService;
import tpi.dgrv4.dpaa.vo.DPB0123UdpResp;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 對接程式,確認User是否有登入
 * 
 * @author Mini
 */
@RestController
public class DPB0123UdpController {

	private TPILogger logger = TPILogger.tl;

	private DPB0123UdpService service;

	@Autowired
	public DPB0123UdpController(DPB0123UdpService service) {
		super();
		this.service = service;
	}


	@GetMapping(value = "/dgrv4/udpssotoken/DPB0123Udp", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public DPB0123UdpResp udpDoubleCheckLogin(HttpServletRequest req) {

		DPB0123UdpResp resp = null;
		try {
			resp = service.udpDoubleCheckLogin(req);
		} catch (Exception e) {
			// 不應該會執行到Exception這一段
			this.logger.error("DPB0123-udpDoubleCheckLogin error: " + e.getMessage());
			resp = new DPB0123UdpResp();
			resp.setCode("1297");
			resp.setMessage("Execution error");
			resp.setLogin("N");
		}

		return resp;
	}
}