package tpi.dgrv4.gateway.controller;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.escape.ESAPI;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.JweEncryptionService;
import tpi.dgrv4.gateway.vo.JweEncryptionReq;
import tpi.dgrv4.gateway.vo.JweEncryptionResp;

/**
 * 模擬 GTW IdP 流程中的 User 登入畫面
 * @author Mini
 */
@RestController
public class GtwIdPMockJSLoginUiController {
 
	private TokenHelper tokenHelper;
	private TsmpTAEASKHelper tsmpTAEASKHelper;
	private JweEncryptionService jweEncryptionService;
	
	@Autowired
	public GtwIdPMockJSLoginUiController(TokenHelper tokenHelper, TsmpTAEASKHelper tsmpTAEASKHelper,
			JweEncryptionService jweEncryptionService) {
		super();
		this.tokenHelper = tokenHelper;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
		this.jweEncryptionService = jweEncryptionService;
	}

	@GetMapping(value = "/dgrv4/mockac/gtwidp/{idPType}/loginui")
	public ResponseEntity<?> loginUi(@RequestHeader HttpHeaders headers, 
			HttpServletRequest httpReq,
			HttpServletResponse httpResp, 
			@PathVariable("idPType") String idPType) throws IOException {
		
		TPILogger.tl.info("\n--【" + httpReq.getRequestURL().toString() + "】--");
		
		String responseType = httpReq.getParameter("response_type");
		String dgrClientId = httpReq.getParameter("client_id");
		String openIdScopeStr = httpReq.getParameter("scope");
		String dgrClientRedirectUri = httpReq.getParameter("redirect_uri");
		String state = httpReq.getParameter("state");
		String reqUri = httpReq.getRequestURI();
		
		try {
			String dgrLoginUrl = "https://localhost:18080/dgrv4/ssotoken/gtwidp/" + idPType + "/gtwlogin";
			
//			String type = "AES";// 舊作法
			String type = "JWE";// 新作法
			
			String userName = null;
			String userMima = null;
			String credential = null;
			String redirectUrl = null;
 
			if ("AES".equals(type)) {
				// 程式中寫死 userName, userMima 表示人輸入的, 然後 302 到 gtwlogin
				// 輸入的 user
				userName = "mini";
				String reqUserMima = "1qaz@WSX3edc";
				try {
					// 因使用 get 傳送, userMima 先做 AES 加密再傳
					userMima = getTsmpTAEASKHelper().encrypt(reqUserMima);
				} catch (Exception e) {
					TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
				}
				
			} else {
				// 因使用 get 傳送, userName、userMima 先做 JWE 加密再傳(含 exp 到期時間)
				
				// 取得 JWE 加密值
				Map<String, String> reqDataMap = new HashedMap<String, String>();// 傳入值有資料
				reqDataMap.put("username", "mini");
//				reqDataMap.put("password", "123456789");// 測試時才打開, 因 sonarQube 會掃描不安全, 測試完要註解

				JweEncryptionReq req = new JweEncryptionReq();
				req.setDataMap(reqDataMap);

				JweEncryptionResp jweResp = getJweEncryptionService().jweEncryption(reqDataMap);
				credential = jweResp.getText();
				
				//System.out.println("------credential: " + credential);
			}
			
			
			// 轉導到後端的驗證登入資料 API
			redirectUrl = String.format( //
					"%s" // 
					+ "?response_type=%s" //
					+ "&client_id=%s" //
					+ "&scope=%s" //
					+ "&redirect_uri=%s" //
					+ "&state=%s" //
					+ "&username=%s" //
					+ "&password=%s" //
					+ "&credential=%s", //
					dgrLoginUrl, //
					responseType, // 
					IdPHelper.getUrlEncode(dgrClientId), // 
					IdPHelper.getUrlEncode(openIdScopeStr), // 
					IdPHelper.getUrlEncode(dgrClientRedirectUri), // 
					IdPHelper.getUrlEncode(state), 
					userName, // 以舊作法時,要輸入
					userMima, // 以舊作法時,要輸入
					credential // 以新作法時,要輸入
			);
 
			TPILogger.tl.debug("Redirect to URL【dgR Login URL】: " + redirectUrl);
			httpResp.sendRedirect(redirectUrl);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			//checkmarx, Reflected XSS All Clients 
			reqUri = ESAPI.encoder().encodeForHTML(reqUri);
			return getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
		}

		return null;
	}
 
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
	
	protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
		return tsmpTAEASKHelper;
	}
	
	protected JweEncryptionService getJweEncryptionService() {
		return jweEncryptionService;
	}
}
