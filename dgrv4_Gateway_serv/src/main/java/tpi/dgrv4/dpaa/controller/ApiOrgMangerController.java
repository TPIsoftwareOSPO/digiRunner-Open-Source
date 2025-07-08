package tpi.dgrv4.dpaa.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.ApiOrgMangerService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0434DTO;
import tpi.dgrv4.dpaa.vo.AA1002Req;
import tpi.dgrv4.dpaa.vo.AA1002Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class ApiOrgMangerController {


    private ApiOrgMangerService apiOrgMangerService;

    @Autowired
    public ApiOrgMangerController(ApiOrgMangerService apiOrgMangerService) {
        this.apiOrgMangerService = apiOrgMangerService;
    }


    @PostMapping( value = "/dgrv4/11/AA0433",produces = "application/json",consumes = "application/json")
    public TsmpBaseResp<AA1002Resp> queryOrgList(@RequestHeader HttpHeaders headers //
    , @RequestBody TsmpBaseReq<AA1002Req> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA1002Resp resp = null;
		
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = apiOrgMangerService.queryOrgList(tsmpHttpHeader.getAuthorization());
			
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }

    @PostMapping( value = "/dgrv4/11/AA0434",produces = "application/json",consumes = "application/json")
    public TsmpBaseResp<List<AA0434DTO>> batchApiOrg(@RequestHeader HttpHeaders headers //
    , @RequestBody TsmpBaseReq<List<AA0434DTO>> req) {

		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		List<AA0434DTO> resp = null;
		
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = apiOrgMangerService.batchApiOrg(tsmpHttpHeader.getAuthorization(), req.getBody());
			
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }























}
