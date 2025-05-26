package tpi.dgrv4.dpaa.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.service.UdpssotokenService;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 對接企業登入digiRunner取得token
 * 
 * @author Mini
 */
@RestController
public class UdpssotokenController {
	private UdpssotokenService service;

	@Autowired
	public UdpssotokenController(UdpssotokenService service) {
		super();
		this.service = service;
	}

	/**
	 * 由 Udpssotoken Bridge 轉接取得token
	 * 
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/udpssotoken/oauth/token", //
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE, // 使用 Form Data 格式
			produces = MediaType.APPLICATION_JSON_VALUE)
	public void getUdpssotoken(@RequestHeader HttpHeaders headers, HttpServletRequest req, 
			HttpServletResponse res, ReqHeader reqHeader) {
		try {
			service.getUdpssotoken(headers, req, res, reqHeader);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, reqHeader);
		}
	}
}