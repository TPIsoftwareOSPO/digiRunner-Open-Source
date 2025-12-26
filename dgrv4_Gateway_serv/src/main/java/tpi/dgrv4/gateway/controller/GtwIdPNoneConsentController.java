package tpi.dgrv4.gateway.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.entity.repository.TsmpClientVgroupDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.escape.CheckmarxUtils;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * [ZH] GTW IdP 流程中, 當不需要轉到前端 User 同意畫面時使用, 例如 GTW CUS IdP, <br>
 * 因為前端 Angular 打包後, 無法在 IOS 16.3 以前的版本上操作, <br>
 * 此為 IOS 正規表示式的 bug  <br>
 * [EN] In the GTW IdP process, it is used when there is no need to go to the front-end User consent screen, such as GTW CUS IdP, <br>
 * Because the front-end Angular is packaged, it cannot be operated on versions before IOS 16.3, <br>
 * This is a bug in IOS regular expressions <br>
 *  
 * @author Mini
 */
@RestController
public class GtwIdPNoneConsentController {
	
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	private TsmpVgroupDao tsmpVgroupDao;

	@Autowired
	public GtwIdPNoneConsentController(TsmpClientVgroupDao tsmpClientVgroupDao, TsmpVgroupDao tsmpVgroupDao) {
		super();
		this.tsmpClientVgroupDao = tsmpClientVgroupDao;
		this.tsmpVgroupDao = tsmpVgroupDao;
	}

	@GetMapping(value = "/dgrv4/gtwidp/{idPType}/noneconsent")
	public ResponseEntity<?> getGtwConsent(@RequestHeader HttpHeaders httpHeaders, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			@PathVariable("idPType") String idPType) throws IOException {
		
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		try {
			String state = httpReq.getParameter("state");
			String userName = httpReq.getParameter("username");
			String dgrClientRedirectUri = httpReq.getParameter("redirect_uri");

			// [ZH]不用取得client 的 vGroup, 預設空的
			// [EN]No need to get the client's vGroup, default is empty
			String dgrVGroupScopeStr = "";
			
			String dgrApproveUrl = "/dgrv4/ssotoken/gtwidp/" + idPType + "/approve";
			String redirectUrl = String.format(
					"%s" 
					+ "?username=%s" 
					+ "&scope=%s" 
					+ "&redirect_uri=%s" 
					+ "&state=%s",
					dgrApproveUrl, 
					IdPHelper.getUrlEncode(userName), 
					IdPHelper.getUrlEncode(dgrVGroupScopeStr), 
					IdPHelper.getUrlEncode(dgrClientRedirectUri), 
					IdPHelper.getUrlEncode(state)
			);
			
			TPILogger.tl.debug("Redirect to URL【dgR Approve URL】: " + redirectUrl);
			
			// [ZH] 不要用 httpResp.sendRedirect (302), 以免前端會轉導2次,
			// sendRedirect 是 HTTP 302 重定向, 會更改 URL, 是客戶端重定向 (http code 302),
			// RequestDispatcher.forward 是服務器端轉發, URL 不變 (http code 200).
			// [EN] Do not use httpResp.sendRedirect (302), to avoid the front end redirecting twice,
			// sendRedirect is HTTP 302 redirect, will change the URL, is a client redirect (http code 302),
			// RequestDispatcher.forward is server-side forwarding, the URL remains unchanged (http code 200).
			//checkmarx, Relative Path Traversal 
			CheckmarxUtils.sanitizeForCheckmarx(httpReq, httpResp, redirectUrl);
			return null;
		} catch (Exception e) {
			throw new TsmpDpAaException(e, null);
		}
	}
	
	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return tsmpClientVgroupDao;
	}
	
	protected TsmpVgroupDao getTsmpVgroupDao() {
		return tsmpVgroupDao;
	}
}
