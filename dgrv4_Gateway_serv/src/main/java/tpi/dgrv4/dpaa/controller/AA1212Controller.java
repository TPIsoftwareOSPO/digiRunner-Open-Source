package tpi.dgrv4.dpaa.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.AA1212Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA1212Resp;
import tpi.dgrv4.dpaa.vo.EmptyBodyReq;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;
/***
 * 
 * @author Tom
 *
 */
@RequiredArgsConstructor
@RestController
@Getter(AccessLevel.PROTECTED)
public class AA1212Controller {
	private final AA1212Service service;
	
	@PostMapping(value = "/dgrv4/11/AA1212", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA1212Resp> queryRealtimeDashboardData(@RequestHeader HttpHeaders headers
			, @RequestBody TsmpBaseReq<EmptyBodyReq> req){
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA1212Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryRealtimeDashboardData(tsmpHttpHeader.getAuthorization());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
		
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
