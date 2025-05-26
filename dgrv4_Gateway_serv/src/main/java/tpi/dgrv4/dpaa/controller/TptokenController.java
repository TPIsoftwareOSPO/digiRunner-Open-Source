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
import tpi.dgrv4.dpaa.service.TptokenService;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * tsmpac-v2 轉 tsmpac3-v3【tptoken機制】
 * 
 * @author Mini
 */
@RestController
public class TptokenController {
	
	private TptokenService service;
	
	@Autowired
	public TptokenController(TptokenService service) {
		super();
		this.service = service;
	}

	/**
	 * 由 tptoken Bridge 轉接取得token
	 * 			
	 * @param headers
	 * @param req
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/tptoken/oauth/token", //
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE, //使用 Form Data 格式
		produces = MediaType.APPLICATION_JSON_VALUE)
	public void getTptoken(HttpServletRequest httpReq, HttpServletResponse httpRes, 
			@RequestHeader HttpHeaders httpHeaders,
			ReqHeader reqHeader) {
		
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		try {
			service.getTptoken(httpReq, httpRes, httpHeaders, reqHeader);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
}
