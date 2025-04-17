package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB9929Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB9929Req;
import tpi.dgrv4.dpaa.vo.DPB9929Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB9929Controller {

	private DPB9929Service service;

	@Autowired
	public DPB9929Controller(DPB9929Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/17/DPB9929", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB9929Resp> exportWebsiteProxy(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB9929Req> req, HttpServletResponse response) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB9929Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);

			resp = service.exportWebsiteProxy(tsmpHttpHeader.getAuthorization(), req.getBody(), response);
			

		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);

	}


}
