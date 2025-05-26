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
import tpi.dgrv4.dpaa.service.DPB0169Service;
import tpi.dgrv4.dpaa.vo.DPB0169Req;
import tpi.dgrv4.dpaa.vo.DPB0169Resp;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 查詢GTW IdP資訊依Client ID (GOOGLE / MS)
 * 
 * @author Mini
 */
@RestController
public class DPB0169Controller {
	private DPB0169Service service;

	@Autowired
	public DPB0169Controller(DPB0169Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/11/DPB0169", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0169Resp> queryGtwIdPInfoByClientId_oauth2(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0169Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0169Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryGtwIdPInfoByClientId_oauth2(tsmpHttpHeader.getAuthorization(), req.getBody(),
					req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
