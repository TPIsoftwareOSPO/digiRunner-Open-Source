package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.service.DPB0120Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0120Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

import java.util.Optional;

/***
 * 
 * 是否啟用客製功能
 * 
 * @author min
 *
 */
@RestController
public class DPB0120Controller {

	private DPB0120Service service;

	@Autowired
	public DPB0120Controller(DPB0120Service service) {
		super();
		this.service = service;
	}

	/**
	 * 
	 * @param DPB0120Resp
	 * @return
	 */

	@GetMapping(value = "/dgrv4/11/DPB0120", params = { "1" }, //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0120Resp> queryCusEnable(HttpServletRequest httpReq, @RequestHeader HttpHeaders headers) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0120Resp resp = null;

		try {
			// ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			resp = service.queryCusEnable(httpReq, tsmpHttpHeader.getAuthorization());
		} catch (Exception e) {

			// 因為Querystring沒有locale資訊，所以前端將locale資訊放入HttpHeaders內
			String locale = Optional.ofNullable(headers.get("locale")).map(lc->lc.get(0)).orElse(LocaleType.EN_US);


			ReqHeader reqHeader = new ReqHeader();
			reqHeader.setLocale(locale);
			throw new TsmpDpAaException(e, reqHeader);
		}

		return ControllerUtil.tsmpResponseBaseObj(headers, resp);

	}
}
