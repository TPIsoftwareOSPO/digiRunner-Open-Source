package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.AA0325Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0325Req;
import tpi.dgrv4.dpaa.vo.AA0325Resp;
import tpi.dgrv4.gateway.service.CApiKeyService;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

/***
 * 
 * 取得Composer部署的時間戳記
 * 
 * @author min
 *
 */
@RestController
public class AA0325Controller {

	public static Long ts = -1l;
	
	private AA0325Service service;
	private CApiKeyService capiKeyService;

	@Autowired
	public AA0325Controller(AA0325Service service, CApiKeyService capiKeyService) {
		super();
		this.service = service;
		this.capiKeyService = capiKeyService;
	}

	@PostMapping(value = "/dgrv4/11/AA0325", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0325Resp> compSync(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0325Req> req, HttpServletRequest httpReq) {

		AA0325Resp resp = null;

		try {

			// 驗證CApiKey
			capiKeyService.verifyCApiKey(headers, false, false);

			resp = service.compSync(req.getBody(), httpReq);

		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
