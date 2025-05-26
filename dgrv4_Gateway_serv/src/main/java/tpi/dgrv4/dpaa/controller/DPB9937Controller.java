package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB9937Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB9937Req;
import tpi.dgrv4.dpaa.vo.DPB9937Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;
@ConditionalOnProperty(name = "db.connection.mode", havingValue = "api")
@RestController
public class DPB9937Controller {

	private DPB9937Service service;

	@Autowired
	public DPB9937Controller(DPB9937Service service) {
		super();
		this.service = service;
	}


	@PostMapping(value = "/dgrv4/17/DPB9937", consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB9937Resp> getDbInfo(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB9937Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB9937Resp resp = new DPB9937Resp();
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);

			service.getDbInfo(tsmpHttpHeader.getAuthorization(), headers, req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());

		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
