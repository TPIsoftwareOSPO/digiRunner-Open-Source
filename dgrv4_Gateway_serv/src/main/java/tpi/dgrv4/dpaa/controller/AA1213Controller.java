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
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.service.AA1213Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA1213Req;
import tpi.dgrv4.dpaa.vo.AA1213Resp;
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
public class AA1213Controller {
	private final AA1213Service service;
	
	@PostMapping(value = "/dgrv4/11/AA1213", params = {"before"}, //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> queryApiAbnormal_before(@RequestHeader HttpHeaders headers
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req){
			try {
				return ControllerUtil.getReqConstraints(req, new AA1213Req());
			} catch (Exception e) {
				throw new TsmpDpAaException(e, req.getReqHeader());
			}
		}
	
	@PostMapping(value = "/dgrv4/11/AA1213", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA1213Resp> queryApiAbnormal(@RequestHeader HttpHeaders headers
			, @RequestBody TsmpBaseReq<AA1213Req> req){
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		AA1213Resp resp = null;
		try {
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryApiAbnormal(tsmpHttpHeader.getAuthorization(), req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
		
		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
