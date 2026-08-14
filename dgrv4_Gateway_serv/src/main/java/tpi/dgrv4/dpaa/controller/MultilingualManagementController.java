package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.service.MultilingualManagementService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB9936Req;
import tpi.dgrv4.dpaa.vo.DPB9940Req;
import tpi.dgrv4.dpaa.vo.EmptyBodyReq;
import tpi.dgrv4.dpaa.vo.EmptyBodyResp;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class MultilingualManagementController {
    private MultilingualManagementService service;
    private ObjectMapper objectMapper;

    @Autowired
    public MultilingualManagementController(MultilingualManagementService service, ObjectMapper objectMapper) {
		super();
		this.service = service;
		this.objectMapper = objectMapper;
	}
    
    @PostMapping(value = "/dgrv4/17/DPB9940", params = { "before" }, //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> exportMultilingual_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		try {
			return ControllerUtil.getReqConstraints(req, new DPB9940Req());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}

    @PostMapping(value = "/dgrv4/17/DPB9940", //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void exportMultilingual(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<DPB9940Req> req, HttpServletResponse httpResp) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            service.exportMultilingual(tsmpHttpHeader.getAuthorization(), req.getBody(), httpResp);
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

    }
    
    @PostMapping(value = "/dgrv4/17/DPB9941", //
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<EmptyBodyResp> importMultilingual(@RequestHeader HttpHeaders headers //
            , @RequestParam("req") String strReq, @RequestParam("file") MultipartFile mFile) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        EmptyBodyResp resp = new EmptyBodyResp();
        TsmpBaseReq<EmptyBodyReq> req = null;
		try {
			req = objectMapper.readValue(strReq, new TypeReference<TsmpBaseReq<EmptyBodyReq>>() {});
		}catch(Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1290.throwing();
		}
        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            service.importMultilingual(tsmpHttpHeader.getAuthorization(), mFile);
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }
}
