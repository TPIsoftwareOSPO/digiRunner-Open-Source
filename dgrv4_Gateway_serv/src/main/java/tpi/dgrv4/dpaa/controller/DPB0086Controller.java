
package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB0086Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0086Req;
import tpi.dgrv4.dpaa.vo.DPB0086Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0086Controller {
	
	private DPB0086Service service;
	
	@Autowired
	public DPB0086Controller(DPB0086Service service) {
		super();
		this.service = service;
	}

	/** 
	 *	移除ClientCA
	 *	1. 依指定的 clientId + client_cert_id 移除檔案
	 */
	@PostMapping(value = "/dgrv4/11/DPB0086", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0086Resp> deleteClientCA(@RequestHeader HttpHeaders headers, @RequestBody TsmpBaseReq<DPB0086Req> req) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0086Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.deleteClientCA(tsmpHttpHeader.getAuthorization(), req.getBody(), req.getReqHeader());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		// do Resp Header
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
