package tpi.dgrv4.dpaa.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.service.UdpLoginService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.UdpLoginResp;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

/**
 * 登入統一入口網
 * 
 * @author Mini
 */
@RestController
public class UdpLoginController {
	
	private UdpLoginService service;
	
	@Autowired
	public UdpLoginController(UdpLoginService service) {
		super();
		this.service = service;
	}

	/**
	 * @param headers
	 * @param req
	 * @return
	 */

	@PostMapping(value = "/dgrv4/udpssotoken/udpLogin", //
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE, //使用 Form Data 格式
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<UdpLoginResp> udpLogin(@RequestHeader HttpHeaders headers, HttpServletRequest req, HttpServletResponse res, @RequestParam("txSN") String txSN, @RequestParam("txDate") String txDate, @RequestParam("txID") String txID,
			@RequestParam("cID") String cID, @RequestParam("locale") String locale) {
		UdpLoginResp resp = null;
		ReqHeader reqHeader = new ReqHeader(); 
		try {
			reqHeader.setcID(cID);
			reqHeader.setTxDate(txDate);
			reqHeader.setTxID(txID);
			reqHeader.setTxSN(txSN);
			reqHeader.setLocale(locale);
			resp = service.udpLogin(headers, req, res, reqHeader);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, reqHeader);
		}

		// do Resp Header
		//reqHeader = null 
		return ControllerUtil.tsmpResponseBaseObj(reqHeader, resp);
	}
}