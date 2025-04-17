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
import tpi.dgrv4.dpaa.service.DPB0166Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0166Req;
import tpi.dgrv4.dpaa.vo.DPB0166Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0166Controller {
	private DPB0166Service service;

	@Autowired
	public DPB0166Controller(DPB0166Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/11/DPB0166", params = {"before"}, //
	consumes = MediaType.APPLICATION_JSON_VALUE, //
	produces = MediaType.APPLICATION_JSON_VALUE)
public TsmpBaseResp<BeforeControllerResp> createGtwIdPInfo_ldap_before(@RequestHeader HttpHeaders headers //
		, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
	try {
		return ControllerUtil.getReqConstraints(req, new DPB0166Req());
	} catch (Exception e) {
		throw new TsmpDpAaException(e, req.getReqHeader());
	}
}


	@PostMapping(value = "/dgrv4/11/DPB0166", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0166Resp> createGtwIdPInfo_ldap(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0166Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0166Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.createGtwIdPInfo_ldap(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
