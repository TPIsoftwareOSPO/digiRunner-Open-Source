package tpi.dgrv4.gateway.service;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import tpi.dgrv4.codec.utils.UUID64Util;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.DgrOauthApprovals;
import tpi.dgrv4.entity.entity.TsmpAuthCode;
import tpi.dgrv4.entity.repository.DgrOauthApprovalsDao;
import tpi.dgrv4.entity.repository.TsmpAuthCodeDao;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.constant.OauthApprovalsStatus;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

/**
 * 2023/04/20 目前已不使用, 改用 GTW IdP 流程, 故全部註解
 * @author Mini
 */

@Service
public class OAuthApproveService {
//	@Autowired
//	private OAuthTokenService oAuthTokenService;
//	
//	@Autowired
//	private OAuthAuthorizationService oAuthAuthorizationService;
//	
//	@Autowired
//	private TsmpAuthCodeDao tsmpAuthCodeDao;
//	
//	@Autowired
//	private DgrOauthApprovalsDao dgrOauthApprovalsDao;
//	
//	@Autowired
//	private TokenHelper tokenHelper;
//	
//	public ResponseEntity<?> approve(HttpServletRequest httpReq, HttpServletResponse httpRes) {
//		String apiUrl = httpReq.getRequestURI();
//		try {
//			StringBuffer reqUrl = httpReq.getRequestURL();
//			TPILogger.tl.info("\n--【" + reqUrl.toString() + "】--");
//			
//			String errMsg = "";
//			
//			Map<String, String> parameters = new HashMap<>();
//			httpReq.getParameterMap().forEach((k, vs) -> {
//				if (vs.length != 0) {
//					parameters.put(k, vs[0]);
//				}
//			});
//			
//			ResponseEntity<?> checkResp = checkData(parameters, apiUrl);
//			if(checkResp != null) {//資料驗證有錯誤
//				return checkResp;
//			}
//			
//			String scope = parameters.get("scope");
//			String username = parameters.get("username");
//			String state = parameters.get("state");
//			String redirectUri = parameters.get("redirect_uri");
//			
//			TsmpAuthCode tsmpAuthCode = getTsmpAuthCodeDao().findFirstByAuthCode(state);
//			if(tsmpAuthCode == null) {//資料驗證有錯誤
//				//Table [TSMP_AUTH_CODE] 查不到
//				TPILogger.tl.debug("Table [TSMP_AUTH_CODE] can't find, auth_code(state):" + state);
//				errMsg = "Bad credentials";
//				TPILogger.tl.debug(errMsg);
//				return new ResponseEntity<OAuthTokenErrorResp2>(getTokenHelper().getOAuthTokenErrorResp2("invalid_grant", 
//						errMsg), HttpStatus.BAD_REQUEST);//400
//			}
//			
//			ResponseEntity<?> checkRedirectUriResp = checkRedirectUri(tsmpAuthCode, state, redirectUri, apiUrl);
//			if(checkRedirectUriResp != null) {//redirectUri資料驗證有錯誤
//				return checkRedirectUriResp;
//			}
//			
//			//更新 TSMP_AUTH_CODE
//			tsmpAuthCode = updateAuthCode(tsmpAuthCode, apiUrl, username);
//			
//			//建立 DGR_OAUTH_APPROVALS, 寫入 scope
//			saveAndRefreshOauthApprovals(username, tsmpAuthCode.getClientName(), scope);
//			
//			//302 redirect 到 redirect_uri,並加上 code 和 state
//			String uri = redirectUri + "?code=" + tsmpAuthCode.getAuthCode() + "&state=" + state;
//			TPILogger.tl.info("Redirect URL(Callback URL):\n" + uri);
//			
//			HttpHeaders headers = new HttpHeaders();
//			headers.setLocation(URI.create(uri));//在表頭的 Location 放入要重新導向的網址
//			return new ResponseEntity<>(headers, HttpStatus.MOVED_TEMPORARILY);//302
//			
//		} catch (Exception e) {
//			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
//			String errMsg = TokenHelper.Internal_Server_Error;
//			TPILogger.tl.error(errMsg);
//			return getTokenHelper().getInternalServerErrorResp(apiUrl, errMsg);//500
//		}
//	}
//	
//	private ResponseEntity<?> checkData(Map<String, String> parameters, String apiUrl) {
//		
//		String scope = parameters.get("scope");
//		String username = parameters.get("username");
//		String state = parameters.get("state");
//		String redirectUri = parameters.get("redirect_uri");
//		
//		if(StringUtils.isEmpty(scope)) {
//			String word = "scope";
//			TPILogger.tl.debug("Body has no " + word);//Body 沒有 scope
//			return getOAuthTokenService().getResponseEntityError(word);
//		}
//		
//		if(StringUtils.isEmpty(username)) {
//			String word = "username";
//			TPILogger.tl.debug("Body has no " + word);//Body 沒有 username
//			return getOAuthTokenService().getResponseEntityError(word);
//		}
//		
//		if(StringUtils.isEmpty(state)) {
//			String word = "state";
//			TPILogger.tl.debug("Body has no " + word);//Body 沒有 state
//			return getOAuthTokenService().getResponseEntityError(word);
//		}
//		
//		if(StringUtils.isEmpty(redirectUri)) {
//			String word = "redirect_uri";
//			TPILogger.tl.debug("Body has no " + word);//Body 沒有 redirect_uri
//			return getOAuthTokenService().getResponseEntityError(word);
//		}
//		return null;
//	}
//	
//	private ResponseEntity<?> checkRedirectUri(TsmpAuthCode tsmpAuthCode, String state, String redirectUri, String apiUri) {
//
//		String clientName = tsmpAuthCode.getClientName();
//		return getTokenHelper().checkRedirectUri(clientName, redirectUri, apiUri);
//	}
//	
//	private TsmpAuthCode updateAuthCode(TsmpAuthCode tsmpAuthCode, String state, String username) {
//		
//		//產生 auth code(UUID 64位元)
//		UUID obj = UUID.randomUUID();
//		String authCode = UUID64Util.UUID64(obj);
//		
//		Date expiredTime = getOAuthAuthorizationService().getCodeExpiredTime();//現在時間+10分鐘
//		Long expireDateTime = expiredTime.getTime();
//		
//		tsmpAuthCode.setAuthCode(authCode);
//		tsmpAuthCode.setExpireDateTime(expireDateTime);
//		tsmpAuthCode.setUpdateDateTime(DateTimeUtil.now());
//		tsmpAuthCode.setUpdateUser(username);
//		tsmpAuthCode = this.getTsmpAuthCodeDao().saveAndFlush(tsmpAuthCode);
//		
//		return tsmpAuthCode;
//	}
//	
//	private void saveAndRefreshOauthApprovals(String userName, String clientId, String scopeStr) {
//		
//		List<DgrOauthApprovals> list = getDgrOauthApprovalsDao().findByUserNameAndClientId(userName, clientId);
//		if(!CollectionUtils.isEmpty(list)) {
//			getDgrOauthApprovalsDao().deleteAll(list);
//			getDgrOauthApprovalsDao().flush();
//		}
//		
//		String[] scopeArr = scopeStr.split(" ");
//		
//		for (String scope : scopeArr) {
//			DgrOauthApprovals dgrOauthApprovals = new DgrOauthApprovals();
//			dgrOauthApprovals.setUserName(userName);
//			dgrOauthApprovals.setClientId(clientId);
//			dgrOauthApprovals.setScope(scope);
//			dgrOauthApprovals.setStatus(OauthApprovalsStatus.APPROVED.value());
////			dgrOauthApprovals.setExpiresAt(); //TODO
////			dgrOauthApprovals.setLastModifiedAt(); //TODO
//			
//			dgrOauthApprovals.setCreateDateTime(DateTimeUtil.now());
//			dgrOauthApprovals.setCreateUser(userName);
//			dgrOauthApprovals = getDgrOauthApprovalsDao().saveAndFlush(dgrOauthApprovals);
//		}
//	}
//	
//	protected DgrOauthApprovalsDao getDgrOauthApprovalsDao() {
//		return dgrOauthApprovalsDao;
//	}
//	
//	protected TsmpAuthCodeDao getTsmpAuthCodeDao() {
//		return tsmpAuthCodeDao;
//	}
//	
//	protected OAuthTokenService getOAuthTokenService() {
//		return oAuthTokenService;
//	}
//	
//	protected OAuthAuthorizationService getOAuthAuthorizationService() {
//		return oAuthAuthorizationService;
//	}
//	
//	protected TokenHelper getTokenHelper() {
//		return tokenHelper;
//	}
}
