package tpi.dgrv4.gateway.component;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthM;
import tpi.dgrv4.entity.repository.DgrGtwIdpAuthMDao;
import tpi.dgrv4.gateway.constant.DgrCodeChallengeMethod;
import tpi.dgrv4.gateway.constant.DgrOpenIDConnectScope;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

/**
 * GTW IdP 流程的共用程式
 * 
 * @author Mini
 */

@Component
public class GtwIdPHelper {

	private TokenHelper tokenHelper;
	private TsmpSettingService tsmpSettingService;
	private DgrGtwIdpAuthMDao dgrGtwIdpAuthMDao;

	// cookie 名稱
	public static String COOKIE_STATE = "state";// for GOOGLE / MS
	public static String COOKIE_CODE_VERIFIER = "code_verifier";// for GOOGLE/ MS

	public static String COOKIE_JTI = "jti";
	public static String COOKIE_IDP_TYPE = "idp_type";

	@Autowired
	public GtwIdPHelper(TokenHelper tokenHelper, TsmpSettingService tsmpSettingService,
			DgrGtwIdpAuthMDao dgrGtwIdpAuthMDao) {
		super();
		this.tokenHelper = tokenHelper;
		this.tsmpSettingService = tsmpSettingService;
		this.dgrGtwIdpAuthMDao = dgrGtwIdpAuthMDao;
	}

	/**
	 * 目前 GTW IdP 支援的 IdP Type
	 */
	public static List<String> getSupportGtwIdPType() {
		List<String> idPTypeList = new ArrayList<>();
		idPTypeList.add(DgrIdPType.GOOGLE);
		idPTypeList.add(DgrIdPType.MS);
		idPTypeList.add(DgrIdPType.OIDC);
		idPTypeList.add(DgrIdPType.JDBC);
		idPTypeList.add(DgrIdPType.LDAP);
		idPTypeList.add(DgrIdPType.API);
		idPTypeList.add(DgrIdPType.CUS);

		return idPTypeList;
	}

	/**
	 * 目前 GTW IdP 支援的 OIDC Scope
	 */
	public static List<String> getSupportScopeList() {
		List<String> supportScopeList = new ArrayList<String>();
		supportScopeList.add(DgrOpenIDConnectScope.OPENID);
		supportScopeList.add(DgrOpenIDConnectScope.EMAIL);
		supportScopeList.add(DgrOpenIDConnectScope.PROFILE);

		return supportScopeList;
	}

	/**
	 * 目前 GTW IdP 支援的 PKCE 的 Code Challenge Method
	 */
	public static List<String> getCodeChallengeMethodsSupported() {
		List<String> list = new ArrayList<String>();
		list.add(DgrCodeChallengeMethod.S256);

		return list;
	}

	/**
	 * 重新導向到前端,顯示訊息
	 */
	public void redirectToShowMsg(HttpServletResponse httpResp, ResponseEntity<?> errRespEntity, String idPType,
			String redirectUri) throws Exception {

		String msg = getTokenHelper().getErrMsgForRespEntity(errRespEntity);
		redirectToShowMsg(httpResp, msg, idPType, redirectUri);
	}

	/**
	 * 重新導向到前端,顯示訊息 1.若 idPType 為 LDAP, 則 URL 改成相對路徑, 例如. "/dgrv4/ac4/gtwidp/errMsg"
	 * 2.若 idPType 為 GOOGLE / MS, 則 URL 依 DB 的值為準
	 */
	public void redirectToShowMsg(HttpServletResponse httpResp, String msg, String idPType, String redirectUri)
			throws Exception {

		// 前端GTW IdP errMsg顯示訊息的URL
		String msgUrl = getTsmpSettingService().getVal_GTW_IDP_MSG_URL();

		if (DgrIdPType.LDAP.equals(idPType) || DgrIdPType.API.equals(idPType) || DgrIdPType.JDBC.equals(idPType)) {
			URL urlObj = new URL(msgUrl);
			msgUrl = urlObj.getPath();// 使用相對路徑
		}

		String msg_en = "";
		if (StringUtils.hasLength(msg)) {
			TPILogger.tl.debug("RedirectToShowMsg(error msg): " + msg);
			msg_en = Base64Util.base64URLEncode(msg.getBytes());// 訊息做 Base64Url Encode
		}

		if (StringUtils.hasLength(redirectUri)) {
			redirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString());
		}

		String redirect = String.format("%s" + "?redirect_uri=%s" + "&rtn_code=%s" + "&msg=%s", msgUrl, redirectUri,
				"error", msg_en);

		TPILogger.tl.debug("RedirectToShowMsg Url: " + redirect);
		httpResp.sendRedirect(redirect);
	}

	/**
	 * 從 cookies 取得值
	 */
	public static String getStateFromCookies(HttpServletRequest httpReq, String cookieName) {
		String value = null;

		Cookie[] cookies = httpReq.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookieName.equalsIgnoreCase(cookie.getName())) {
					value = cookie.getValue();
					break;
				}
			}
		}
		return value;
	}

	/**
	 * 檢查 Cookie 的資料
	 */
	public ResponseEntity<?> checkCookieParam(String state, String reqUri) {
		// 沒有 state
		if (!StringUtils.hasLength(state)) {
			String errMsg = "Missing cookie 'state', please relogin.";
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}

		return null;
	}

	/**
	 * 檢查 state 是否已存在
	 */
	public ResponseEntity<?> checkStateExists(String state, String reqUri) {
		// 沒有 state
		if (!StringUtils.hasText(state)) {
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "state";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// state 已存在
		DgrGtwIdpAuthM dgrGtwIdpAuthM = getDgrGtwIdpAuthMDao().findFirstByState(state);
		if (dgrGtwIdpAuthM != null) {
			String errMsg = String.format("Table [DGR_GTW_IDP_AUTH_M] state already exists, state: %s", state);
			TPILogger.tl.debug(errMsg);

			errMsg = String.format("The state already exists. state: %s", state);
			TPILogger.tl.debug(errMsg);
			return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);// 401
		}
		return null;
	}

	/**
	 * 檢查 code_challenge 和 code_challenge_method
	 */
	public ResponseEntity<?> checkCodeChallengeParam(String codeChallenge, String codeChallengeMethod) {
		// 1.有 code_challenge, 但沒有 code_challenge_method
		if (StringUtils.hasLength(codeChallenge) && !StringUtils.hasLength(codeChallengeMethod)) {
			String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "code_challenge_method";
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(
					getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
					HttpStatus.BAD_REQUEST);// 400
		}

		// 2.有 code_challenge_method, 但沒有 code_challenge
		if (StringUtils.hasLength(codeChallengeMethod)) {
			if (!StringUtils.hasLength(codeChallenge)) {
				String errMsg = TokenHelper.MISSING_REQUIRED_PARAMETER + "code_challenge";
				TPILogger.tl.debug(errMsg);
				return new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
			}

			// 3.檢查 code_challenge_method 是否是目前所支援的
			// Gateway IdP 支援的 Code challenge method
			List<String> codeChallengeMethodsSupportedList = GtwIdPHelper.getCodeChallengeMethodsSupported();

			// 無效的 code challenge method
			boolean isSupport = codeChallengeMethodsSupportedList.contains(codeChallengeMethod);
			if (!isSupport) {
				String errMsg = "Invalid parameter value for code_challenge_method: " + codeChallengeMethod;
				TPILogger.tl.debug(errMsg);
				return new ResponseEntity<OAuthTokenErrorResp2>(
						getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_REQUEST, errMsg),
						HttpStatus.BAD_REQUEST);// 400
			}
		}

		return null;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected DgrGtwIdpAuthMDao getDgrGtwIdpAuthMDao() {
		return dgrGtwIdpAuthMDao;
	}
}
