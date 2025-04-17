package tpi.dgrv4.dpaa.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.service.DPB0118Service;
import tpi.dgrv4.dpaa.service.DPB9921Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0118Resp;
import tpi.dgrv4.dpaa.vo.DPB9921Req;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB9921Controller {

	private DPB0118Service dpb0118Service;
	private DPB9921Service dpb9921Service;
	
	@Autowired
	public DPB9921Controller(ApplicationContext applicationContext, DPB9921Service dpb9921Service) {
		super();
		if (applicationContext != null) { 
			this.dpb0118Service = applicationContext.getBean(DPB0118Service.class); // non-Singleton
		}
		this.dpb9921Service = dpb9921Service;
	}

	@PostMapping(value = "/dgrv4/17/DPB9921", params = { "before" }, //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> exportTsmpSetting_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		try {
			return ControllerUtil.getReqConstraints(req, new DPB9921Req());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}


	@PostMapping(value = "/dgrv4/17/DPB9921", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public void exportTsmpSetting(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB9921Req> req, HttpServletResponse response) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			doExportSetting(response);
			dpb9921Service.exportTsmpSetting(tsmpHttpHeader.getAuthorization(), req.getBody(), response.getOutputStream());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

	}
	
	private void doExportSetting(HttpServletResponse response) {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmm");
        String nowDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        DPB0118Resp dpb0118Resp = dpb0118Service.queryModuleVersion();
        String version = dpb0118Resp.getMajorVersionNo() == null ? "unknown" : dpb0118Resp.getMajorVersionNo();
        String headerValue = "attachment; filename=Setting_" + nowDateTime + "_" + version + ".xlsx";
        response.setHeader(headerKey, headerValue);
      //checkmarx, Missing HSTS Header
      	response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
	}
}
