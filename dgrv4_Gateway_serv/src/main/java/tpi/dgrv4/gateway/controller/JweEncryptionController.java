package tpi.dgrv4.gateway.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.JweEncryptionService;
import tpi.dgrv4.gateway.vo.JweEncryptionReq;
import tpi.dgrv4.gateway.vo.JweEncryptionResp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@RestController
public class JweEncryptionController {

	private final JweEncryptionService service;

	/**
	 * [ZH] 版本1, request 的格式有 TsmpBaseReq, <br>
	 * 且為 Form application/json <br>
	 * [EN] Version 1, the request format is TsmpBaseReq, <br>
	 * and is Form application/json <br>
	 */
	@PostMapping(value = "/dgrv4/ssotoken/jweEncryption", //
			consumes = MediaType.APPLICATION_JSON_VALUE, // Use application/json format
			produces = MediaType.APPLICATION_JSON_VALUE) // Use application/json format
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
	
	/**
	 * [ZH] 版本2, request 的格式拿掉 TsmpBaseReq, <br>
	 * 且為 Form x-www-form-urlencoded <br>
	 * [EN] Version 2, The request format removes TsmpBaseReq <br>
	 * and is Form x-www-form-urlencoded <br>
	 */
	@PostMapping(value = "/dgrv4/ssotoken/v2/jweEncryption", //
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, // Use Form x-www-form-urlencoded format
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JweEncryptionResp> jweEncryptionV2(HttpServletRequest httpReq //
			, HttpServletResponse httpResp //
			, @RequestHeader HttpHeaders heards) {

		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");

		return service.jweEncryptionV2(httpReq);
	}
}
