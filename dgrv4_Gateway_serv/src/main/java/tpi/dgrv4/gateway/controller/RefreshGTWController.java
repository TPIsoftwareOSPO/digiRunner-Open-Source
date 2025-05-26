package tpi.dgrv4.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.gateway.service.RefreshGTWService;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.RefreshGTWReq;
import tpi.dgrv4.gateway.vo.RefreshGTWResp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

@RestController
public class RefreshGTWController {

	private RefreshGTWService service;

	@Autowired
	public RefreshGTWController(RefreshGTWService service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/ImGTW/refreshGTW", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<RefreshGTWResp> refreshGTW(HttpServletRequest request, //
			@RequestHeader HttpHeaders headers, //
			@RequestBody TsmpBaseReq<RefreshGTWReq> req) {

		RefreshGTWResp resp = null;
		try {
			resp = service.updateGTWInfo(req.getBody(), request, headers);
		} catch (Exception e) {
			throw new DgrException(e, req.getReqHeader());
		}
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}