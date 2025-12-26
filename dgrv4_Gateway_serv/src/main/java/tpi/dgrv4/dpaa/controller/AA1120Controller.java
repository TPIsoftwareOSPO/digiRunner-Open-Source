package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.AA1120Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA1120Req;
import tpi.dgrv4.dpaa.vo.AA1120Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 
 * @author Tom
 */
@RestController
public class AA1120Controller {
	
	private AA1120Service service;
	
	@Autowired
	public AA1120Controller(AA1120Service service) {
		super();
		this.service = service;
	}



	@PostMapping(value = "/dgrv4/11/AA1120", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public void exportClientRelated(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<AA1120Req> req, HttpServletResponse httpResp) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			AA1120Resp resp = service.exportClientRelated(tsmpHttpHeader.getAuthorization(), req.getBody());
			service.exportClientRelatedByFile(httpResp, resp);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

	}
}
