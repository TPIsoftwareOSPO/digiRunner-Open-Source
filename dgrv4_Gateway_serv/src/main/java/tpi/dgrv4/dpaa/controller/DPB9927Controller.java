package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.service.DPB9927Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB9927Req;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class DPB9927Controller {

	private DPB9927Service service;

	@Autowired
	public DPB9927Controller(DPB9927Service service) {
		super();
		this.service = service;
	}


	@PostMapping(value = "/dgrv4/17/DPB9927", params = { "before" }, //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> exportTsmpFunc_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		try {
			return ControllerUtil.getReqConstraints(req, new DPB9927Req());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}


	@PostMapping(value = "/dgrv4/17/DPB9927", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public void exportTsmpFunc(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB9927Req> req, HttpServletResponse response) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			doExportTsmpFunc(response, req.getBody(), req.getReqHeader());
			service.exportTsmpFunc(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader(), response);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}
	
	private void doExportTsmpFunc(HttpServletResponse response, DPB9927Req req, ReqHeader reqHeader) {
		if(!StringUtils.hasText(req.getFuncType())) {
			throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
		}
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmm");
        String nowDateTime = dateFormatter.format(new Date());
        String funcType = service.getFuncTypeByBcryptParamHelper(req.getFuncType(), reqHeader.getLocale());
        String fileName = "0".equals(funcType)?"TsmpFunc":"EmbededFunc";
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename="+fileName+"_" + nowDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
      //checkmarx, Missing HSTS Header
      	response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
	}
}
