package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.SignBlockResetService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.ResHeaderSignBlockReset;
import tpi.dgrv4.dpaa.vo.SignBlockResetResp;
import tpi.dgrv4.dpaa.vo.TsmpRespSignBlockReset;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * tsmpac-v2 轉 tsmpac3-v3【tptoken機制】
 * 
 * @author Mini
 */
@RestController
public class SignBlockResetController {

	private TPILogger logger = TPILogger.tl;

	public static String identify = "resetSignBlock";
	
	private SignBlockResetService service;
	
	@Autowired
	public SignBlockResetController(SignBlockResetService service) {
		super();
		this.service = service;
	}
	
	/**
	 * 換一個新的signBlock，避免signBlock被盜用。
	 */
	
	@GetMapping(value = "/dgrv4/resetSignBlock", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpRespSignBlockReset<SignBlockResetResp> resetSignBlock(@RequestHeader HttpHeaders headers) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		SignBlockResetResp resp = null;
		try {
			resp = service.resetSignBlock(tsmpHttpHeader.getAuthorization());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null, identify);
		}
		return tsmpResponseSignBlockResetObj(resp);
	}

	private <T> TsmpRespSignBlockReset<T> tsmpResponseSignBlockResetObj(T responseBody) {
		try {
			ResHeaderSignBlockReset resHeader = new ResHeaderSignBlockReset();
			resHeader.setRtnCode("0000");
			resHeader.setRtnMsg("success");
			
			TsmpRespSignBlockReset<T> baseResp = new TsmpRespSignBlockReset<>();
			baseResp.setResHeader(resHeader);
			baseResp.setBody(responseBody);

			return baseResp;
		} catch (Exception e) {
			logger.debug("" + e);
		}
		return null;
	}
}
