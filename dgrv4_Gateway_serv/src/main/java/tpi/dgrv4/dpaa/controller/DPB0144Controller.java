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
import tpi.dgrv4.dpaa.service.DPB0144Service;
import tpi.dgrv4.dpaa.vo.DPB0144Req;
import tpi.dgrv4.dpaa.vo.DPB0144Resp;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 查詢監控
 * @author Kevin Cheng
 */
@RestController
public class DPB0144Controller {

	private DPB0144Service dpb0144Service;

	@Autowired
	public DPB0144Controller(DPB0144Service dpb0144Service) {
		super();
		this.dpb0144Service = dpb0144Service;
	}


	@PostMapping(value = "/dgrv4/11/DPB0144", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0144Resp> queryMonitor(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<DPB0144Req> req){
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0144Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = dpb0144Service.queryMonitor(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
		
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
