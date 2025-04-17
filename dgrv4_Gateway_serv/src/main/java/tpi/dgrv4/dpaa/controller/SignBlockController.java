package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.SignBlockService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.ResHeaderSignBlock;
import tpi.dgrv4.dpaa.vo.SignBlockResp;
import tpi.dgrv4.dpaa.vo.TsmpRespSignBlock;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * tsmpac-v2 轉 tsmpac3-v3【tptoken機制】
 * 
 * @author Mini
 */
@RestController
public class SignBlockController {

	public static String identify = "getSignBlock";
	
	private SignBlockService service;
	
	@Autowired
	public SignBlockController(SignBlockService service) {
		super();
		this.service = service;
	}

	/**
	 * 依cliendId,取得SignBlock
	 */

	@GetMapping(value = "/dgrv4/getSignBlock", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpRespSignBlock<SignBlockResp> getSignBlock(@RequestHeader HttpHeaders headers) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		SignBlockResp resp = null;
		try {
			resp = service.getSignBlock(tsmpHttpHeader.getAuthorization());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null, identify);
		}
		return tsmpResponseSignBlockObj(resp);
	}

	private <T> TsmpRespSignBlock<T> tsmpResponseSignBlockObj(T responseBody) {
		try {
			ResHeaderSignBlock resHeader = new ResHeaderSignBlock();
			resHeader.setRtnCode("0000");
			resHeader.setRtnMsg("success");
			
			TsmpRespSignBlock<T> baseResp = new TsmpRespSignBlock<>();
			baseResp.setResHeader(resHeader);
			baseResp.setBody(responseBody);

			return baseResp;
		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
		}
		return null;
	}
}
