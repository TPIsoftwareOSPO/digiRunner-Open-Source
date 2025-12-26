package tpi.dgrv4.dpaa.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.autoInitSQL.Initializer.TsmpOrganizationTableInitializer;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.service.ForgetMimaService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0550Req;
import tpi.dgrv4.dpaa.vo.AA0550Resp;
import tpi.dgrv4.dpaa.vo.AA0551Req;
import tpi.dgrv4.dpaa.vo.AA0551Resp;
import tpi.dgrv4.dpaa.vo.AA0552Req;
import tpi.dgrv4.dpaa.vo.EmptyBodyResp;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

/**
 * 	
 * 
 * @author Tom
 *
 */
@RequiredArgsConstructor
@RestController
public class ForgetMimaController {
		
	private final ForgetMimaService service;

	@PostMapping(value = "/dgrv4/11/AA0550", params = {"before"}, //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> sendOtp_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		try {
			return ControllerUtil.getReqConstraints(req, new AA0550Req());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}

	@PostMapping(value = "/dgrv4/11/AA0550", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0550Resp> sendOtp(@RequestBody TsmpBaseReq<AA0550Req> req) {
		AA0550Resp resp = null;
		try {
			//Because I'm not logged in, So self-built
			TsmpAuthorization auth = new TsmpAuthorization();
			auth.setUserName(ForgetMimaService.AUTH_USER_NAME);
			auth.setClientId(ForgetMimaService.AUTH_CLIENT_ID);
			auth.setOrgId(TsmpOrganizationTableInitializer.DEFAULT_ORG_ID);
			
			resp = service.sendOtp(auth, req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
	
	@PostMapping(value = "/dgrv4/11/AA0551", params = {"before"}, //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> validationOtp_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		try {
			return ControllerUtil.getReqConstraints(req, new AA0551Req());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}

	@PostMapping(value = "/dgrv4/11/AA0551", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0551Resp> validationOtp(@RequestBody TsmpBaseReq<AA0551Req> req) {
		AA0551Resp resp = null;
		try {
			resp = service.validationOtp(req.getBody());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
	
	@PostMapping(value = "/dgrv4/11/AA0552", params = {"before"}, //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> changeMima_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		try {
			return ControllerUtil.getReqConstraints(req, new AA0552Req());
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}
	}

	@PostMapping(value = "/dgrv4/11/AA0552", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<EmptyBodyResp> changeMima(@RequestHeader HttpHeaders headers,
			@RequestBody TsmpBaseReq<AA0552Req> req,HttpServletRequest httpReq) {
		EmptyBodyResp resp = null;
		try {
			//Because I'm not logged in, So self-built
			TsmpAuthorization auth = new TsmpAuthorization();
			auth.setUserName(ForgetMimaService.AUTH_USER_NAME);
			auth.setClientId(ForgetMimaService.AUTH_CLIENT_ID);
			auth.setOrgId(TsmpOrganizationTableInitializer.DEFAULT_ORG_ID);
			InnerInvokeParam iip = InnerInvokeParam.getInstance(headers, httpReq, auth);

			resp = service.changeMima(req.getBody(),iip);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
