package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.AA1121Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA1116Resp;
import tpi.dgrv4.dpaa.vo.AA1121Req;
import tpi.dgrv4.dpaa.vo.AA1121Resp;
import tpi.dgrv4.dpaa.vo.DPB9922Req;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 *
 * @author Tom
 */
@RestController
public class AA1121Controller {
	
	private AA1121Service service;
	private ObjectMapper objectMapper;

	@Autowired
	public AA1121Controller(AA1121Service service, ObjectMapper objectMapper) {
		super();
		this.service = service;
		this.objectMapper = objectMapper;
	}


	@PostMapping(value = "/dgrv4/11/AA1121", //
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA1121Resp> importClientRelated(@RequestHeader HttpHeaders headers //
			,@RequestParam("req") String strReq, @RequestParam("file") MultipartFile mFile) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA1121Resp resp = null;
		TsmpBaseReq<AA1121Req> req = null;
		try {
			req = objectMapper.readValue(strReq, new TypeReference<TsmpBaseReq<AA1121Req>>() {});
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1290.throwing();
		}
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.importClientRelated(tsmpHttpHeader.getAuthorization(), mFile);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
