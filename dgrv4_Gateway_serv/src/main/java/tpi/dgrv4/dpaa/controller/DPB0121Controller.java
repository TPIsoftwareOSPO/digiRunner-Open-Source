package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DPB0121Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0121Resp;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

@RestController
public class DPB0121Controller {

	private TPILogger logger = TPILogger.tl;

	private DPB0121Service service;

	@Autowired
	public DPB0121Controller(DPB0121Service service) {
		super();
		this.service = service;
	}


	@GetMapping(value = "/dgrv4/11/DPB0121", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0121Resp> queryRefreshMemListUrls(@RequestHeader HttpHeaders headers, HttpServletRequest req) {
		DPB0121Resp resp = new DPB0121Resp();
		try {
			resp = service.queryRefreshMemListUrls(req.getScheme());
		} catch (Exception e) {
			// 此API僅記錄但不拋任何錯誤
			this.logger.error("DPB0121-queryRefreshMemListUrls error: " + StackTraceUtil.logStackTrace(e));
		}
		return ControllerUtil.tsmpResponseBaseObj(headers, resp);
	}

}