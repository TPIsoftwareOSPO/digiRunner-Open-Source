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

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.AA0326Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0326Req;
import tpi.dgrv4.dpaa.vo.AA0326Resp;
import tpi.dgrv4.gateway.service.CApiKeyService;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

/***
 * 
 * 取得composer相關flag
 * 
 * @author min
 *
 */
@RestController
public class AA0326Controller {

	private AA0326Service service;
	private CApiKeyService capiKeyService;

	@Autowired
	public AA0326Controller(AA0326Service service, CApiKeyService capiKeyService) {
		super();
		this.service = service;
		this.capiKeyService = capiKeyService;
	}


	@PostMapping(value = "/dgrv4/11/AA0326", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0326Resp> compSync(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA0326Req> req, HttpServletRequest httpReq) {

		AA0326Resp resp = null;

		try {

			// 驗證CApiKey
			capiKeyService.verifyCApiKey(headers, false, false);

			resp = service.getComposerAllFlag(req.getBody(), httpReq);

		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
