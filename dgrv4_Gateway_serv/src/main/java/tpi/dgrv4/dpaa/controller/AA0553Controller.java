package tpi.dgrv4.dpaa.controller;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.service.AA0553Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.AA0553Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

/**
 * 	
 * 
 * @author Tom
 *
 */
@RequiredArgsConstructor
@RestController
public class AA0553Controller {
		
	private final AA0553Service service;

	@GetMapping(value = "/dgrv4/11/AA0553", //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<AA0553Resp> getMimaStrengthDesc(@RequestHeader HttpHeaders headers) {
		
		AA0553Resp resp = null;
		
		try {
			resp = service.getPwdStrengthInfo();
		} catch (Exception e) {
			//Because the query string does not have locale information, the front end puts the locale information into HttpHeaders
			// 因為Querystring沒有locale資訊，所以前端將locale資訊放入HttpHeaders內
			String locale = Optional.ofNullable(headers.get("locale")).map(lc->lc.get(0)).orElse(LocaleType.EN_US);

			ReqHeader reqHeader = new ReqHeader();
			reqHeader.setLocale(locale);
			throw new TsmpDpAaException(e, reqHeader);
		}

		return ControllerUtil.tsmpResponseBaseObj(headers, resp);
	}
}
