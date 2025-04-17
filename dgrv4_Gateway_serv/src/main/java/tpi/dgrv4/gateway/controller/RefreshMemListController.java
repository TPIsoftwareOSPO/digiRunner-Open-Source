package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

@RestController
public class RefreshMemListController {

	private ApptJobDispatcher apptJobDispatcher;

	@Autowired
	public RefreshMemListController(ApptJobDispatcher apptJobDispatcher) {
		super();
		this.apptJobDispatcher = apptJobDispatcher;
	}

	@PostMapping(value = "/dgrv4/v3/refreshMemList", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<Object> refreshMemList(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<Void> req) {
		try {
			getApptJobDispatcher().resetRefreshSchedule();
		} catch (Exception e) {
			throw new DgrException(e, req.getReqHeader());
		}
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), new Object());
	}

	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}

}