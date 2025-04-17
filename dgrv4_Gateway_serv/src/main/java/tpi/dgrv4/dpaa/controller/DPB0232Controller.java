package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DPB0232Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0232Req;
import tpi.dgrv4.dpaa.vo.DPB0232Resp;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0232Controller {

	private DPB0232Service service;

	@Autowired
	public DPB0232Controller(DPB0232Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/11/DPB0232", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0232Resp> queryBotDetectionList(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0232Req> req) {
		// 將請求頭轉換為 TsmpHttpHeader 格式
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0232Resp resp = null;
		try {
			// 驗證請求的合法性
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryBotDetectionList(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		// 將結果封裝成 TsmpBaseResp 格式返回
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
