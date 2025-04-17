package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.gateway.service.NotifyLandingService;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.*;

@RestController
public class NotifyLandingController {

	private NotifyLandingService service;

	@Autowired
	public NotifyLandingController(NotifyLandingService service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/ImGTW/notifyLanding", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<NotifyLandingResp> notifyLanding(//
			@RequestHeader HttpHeaders headers, //
			@RequestBody TsmpBaseReq<NotifyLandingReq> req) {
		
		NotifyLandingResp resp;

		try {
			// To-Do return json data
			resp = service.confirmAPI(req.getBody(), req.getReqHeader(), headers);
		} catch (Exception e) {
			throw new DgrException(e, req.getReqHeader());
		}
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}

}
