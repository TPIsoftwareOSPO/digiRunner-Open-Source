package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DPB0189Service;
import tpi.dgrv4.dpaa.vo.DPB0189Req;
import tpi.dgrv4.dpaa.vo.DPB0189Resp;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

@SuppressWarnings("java:S3649")  // 停用 SQL 注入檢查, 因為此入口必需通過 capikey 的檢查, 此技術只適用在 container 內部互相傳送使用

public class DPB0189Controller {
	private DPB0189Service service;
	
	@Autowired
	public DPB0189Controller(DPB0189Service service) {
		super();
		this.service = service;
	}


	@PostMapping(value = "/dgrv4/11/DPB0189", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0189Resp> execSql(@RequestHeader HttpHeaders headers, 
			                         HttpServletRequest httpReq, @RequestBody TsmpBaseReq<DPB0189Req> req) {
		DPB0189Resp resp = null;
		try {
			resp = service.executeSql(req.getBody(), headers);
			
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			resp = new DPB0189Resp();
			resp.setResult(e.getMessage());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
	
}
