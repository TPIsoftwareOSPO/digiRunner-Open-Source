package tpi.dgrv4.gateway.controller;

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
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.DataEncryptionService;
import tpi.dgrv4.gateway.vo.DataEncryptionReq;
import tpi.dgrv4.gateway.vo.DataEncryptionResp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

@RestController
public class DataEncryptionController {

	private DataEncryptionService service;

	@Autowired
	public DataEncryptionController(DataEncryptionService service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/ssotoken/dataEncryption", //
			consumes = MediaType.APPLICATION_JSON_VALUE, // 使用 application/json 格式
			produces = MediaType.APPLICATION_JSON_VALUE) // 使用 application/json 格式
	public TsmpBaseResp<DataEncryptionResp> dataEncryption(HttpServletRequest httpReq,
			@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DataEncryptionReq> req) {

		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		DataEncryptionResp resp = null;

		try {
			resp = service.dataEncryption(req.getBody());

		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		// do Resp Header
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
