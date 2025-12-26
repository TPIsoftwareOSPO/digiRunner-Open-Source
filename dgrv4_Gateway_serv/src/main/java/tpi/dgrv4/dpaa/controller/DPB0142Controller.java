package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tpi.dgrv4.codec.utils.ExpireKeyUtil;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.service.DPB0142Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0142Req;
import tpi.dgrv4.dpaa.vo.DPB0142Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * 取得AC入場券
 * @author Kim
 */
@RestController
public class DPB0142Controller {
    private String EXPIRE_KEY = "composerExpireKey";
	private DPB0142Service service;
	
	@Autowired
	public DPB0142Controller(DPB0142Service service) {
		super();
		this.service = service;
	}

	@PostMapping(value = "/dgrv4/11/DPB0142", params = {"before"}, //
		consumes = MediaType.APPLICATION_JSON_VALUE, //
		produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<BeforeControllerResp> getACEntryTicket_before(@RequestHeader HttpHeaders headers //
			, @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
		return ControllerUtil.getReqConstraints(req, new DPB0142Req());
	}

	/**
	 * Composer 取得 AC 產生的 JWE
	 * 提供外部 Client(ex. Composer) 取得授權碼AuthCode，即可藉此授權碼向 digiRunner 取得 AccessToken (DPB0143)。
	 * @param headers
	 * @param req
	 * @return
	 */
	
	@PostMapping(value = "/dgrv4/11/DPB0142", //
			consumes = MediaType.APPLICATION_JSON_VALUE, //
			produces = MediaType.APPLICATION_JSON_VALUE)
	public TsmpBaseResp<DPB0142Resp> getACEntryTicket(
            @RequestHeader HttpHeaders headers, //
            @RequestBody TsmpBaseReq<DPB0142Req> req, //
            HttpServletRequest request , HttpServletResponse servletResponse) {
		TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
		DPB0142Resp resp = null;
		try {
			
			ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
			
			String userIP = ServiceUtil.getIpAddress(request);
			String authorization = headers.getFirst("authorization");

//            List<String> cookies = headers.get("cookie");
            String expireKey = ExpireKeyUtil.getExpireKey_60sec();
            ResponseCookie cookie = ResponseCookie.from(EXPIRE_KEY, expireKey) // key & value
                    .path("/")
                    .httpOnly(true) // 禁止 JavaScript 存取 cookie, 防止 XSS Attack (Cross-Site Scripting，跨站腳本攻擊)
                    .secure(true) // 讓 cookie 只能透過 https 傳遞, 即只有 HTTPS 才能讀與寫
                    .sameSite("Lax") // 防止 CSRF Attack (Cross-site request forgery，跨站請求偽造)
                    .build();

//            headers.put("cookie", cookies);
            servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
			resp = service.getACEntryTicket(req.getBody(), userIP, authorization);
		} catch (Exception e) {
			throw new TsmpDpAaException(e, req.getReqHeader());
		}

		return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
	}
}
