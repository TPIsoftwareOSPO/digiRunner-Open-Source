package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.service.DPB0167Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0167Req;
import tpi.dgrv4.dpaa.vo.DPB0167Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;
@RestController
public class DPB0167Controller {
	private DPB0167Service service;

	@Autowired
	public DPB0167Controller(DPB0167Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/11/DPB0167", params = {"before"}, //
	consumes = MediaType.APPLICATION_JSON_VALUE, //
	produces = MediaType.APPLICATION_JSON_VALUE)
public TsmpBaseResp<BeforeControllerResp> updateGtwIdPInfo_ldap_before(@RequestHeader HttpHeaders headers //
		, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
	try {
		return ControllerUtil.getReqConstraints(req, new DPB0167Req());
	} catch (Exception e) {
		throw new TsmpDpAaException(e, req.getReqHeader());
	}
}


	@PostMapping(value = "/dgrv4/11/DPB0167", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0167Resp> updateGtwIdPInfo_ldap(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0167Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0167Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.updateGtwIdPInfo_ldap(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
