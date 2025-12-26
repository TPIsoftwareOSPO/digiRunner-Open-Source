
package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB9936Req;
import tpi.dgrv4.dpaa.vo.DPB9936Resp;
import tpi.dgrv4.escape.DPB9936Service;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB9936Controller {
	private TPILogger logger = TPILogger.tl;
	
	private DPB9936Service service;
	private ObjectMapper objectMapper;

	@Autowired
	public DPB9936Controller(DPB9936Service service, ObjectMapper objectMapper) {
		super();
		this.service = service;
		this.objectMapper = objectMapper;
	}

	@PostMapping(value = "/dgrv4/17/DPB9936", //
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB9936Resp> importTsmpRtnCode(@RequestHeader HttpHeaders headers, 
			@RequestParam("req") String strReq, @RequestParam("file") MultipartFile mFile) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB9936Resp resp = null;
		TsmpBaseReq<DPB9936Req> req = null;
		try {
			req = objectMapper.readValue(strReq, new TypeReference<TsmpBaseReq<DPB9936Req>>() {});
		}catch(Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1290.throwing();
		}
		try {
			 
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.importTsmpRtnCode(tsmpHttpHeader.getAuthorization(), mFile);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
