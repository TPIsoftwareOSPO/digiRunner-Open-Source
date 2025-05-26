package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.JweEncryptionService;
import tpi.dgrv4.gateway.vo.JweEncryptionReq;
import tpi.dgrv4.gateway.vo.JweEncryptionResp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

@RestController
public class JweEncryptionController {

	private JweEncryptionService service;

	@Autowired
	public JweEncryptionController(JweEncryptionService service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/ssotoken/jweEncryption", //
			consumes = MediaType.APPLICATION_JSON_VALUE, // 使用 application/json 格式
			produces = MediaType.APPLICATION_JSON_VALUE) // 使用 application/json 格式
	public TsmpBaseResp<JweEncryptionResp> jweEncryption(HttpServletRequest httpReq //
			, HttpServletResponse httpResp //
			, @RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<JweEncryptionReq> req) {

		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		JweEncryptionResp resp = null;

		try {
			resp = service.jweEncryption(httpReq, httpResp, req.getBody());

		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		// do Resp Header
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
