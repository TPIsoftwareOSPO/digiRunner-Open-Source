package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthM;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoCus;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoCusDao;
import tpi.dgrv4.gateway.component.AcIdPHelper;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.funcInterFace.CusLoginFailedHandler;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.CusGatewayLoginStateStore;

@Service
public class GtwCusIdPLoginService extends CusIdPService {

	private ObjectMapper objectMapper;
	private AcIdPHelper acIdPHelper;
	private TsmpSettingService tsmpSettingService;
	private GtwIdPCallbackService gtwIdPCallbackService;
	private GtwIdPAuthService gtwIdPAuthService;
	private TokenHelper tokenHelper;
	private GtwIdPHelper gtwIdPHelper;
	private DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao;

	@Autowired
	public GtwCusIdPLoginService(DgrAuditLogService dgrAuditLogService, AcIdPHelper acIdPHelper,
			TsmpSettingService tsmpSettingService, ObjectMapper objectMapper,
			GtwIdPCallbackService gtwIdPCallbackService, GtwIdPAuthService gtwIdPAuthService, TokenHelper tokenHelper,
			GtwIdPHelper gtwIdPHelper, DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao) {

		super(dgrAuditLogService, acIdPHelper, tsmpSettingService);

		this.objectMapper = objectMapper;
		this.acIdPHelper = acIdPHelper;
		this.tsmpSettingService = tsmpSettingService;
		this.gtwIdPCallbackService = gtwIdPCallbackService;
		this.gtwIdPAuthService = gtwIdPAuthService;
		this.tokenHelper = tokenHelper;
		this.gtwIdPHelper = gtwIdPHelper;
		this.dgrGtwIdpInfoCusDao = dgrGtwIdpInfoCusDao;
	}

	public void gtwCusIdPLogin(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpResp,
			Map<String, String> queryParams) {

		CusLoginFailedHandler cusLoginFailedHandler = customErrorMsg -> getCusLoginFailedHandler(httpHeaders, httpReq,
				httpResp, customErrorMsg, false);

		try {

			String reqUri = httpReq.getRequestURI();
			String dgrClientRedirectUri = queryParams.get("redirect_uri");
			String responseType = queryParams.get("response_type");
			String dgrClientId = queryParams.get("client_id");
			String oidcScopeStr = queryParams.get("scope");
			String state = queryParams.get("state");
			String codeChallenge = queryParams.get("code_challenge");
			String codeChallengeMethod = queryParams.get("code_challenge_method");
			String dgrGtwIdpInfoCusId = queryParams.get("dgr_gtw_idp_info_cus_id");

			checkReqParam(responseType, dgrClientId, oidcScopeStr, dgrClientRedirectUri, state, codeChallenge,
					codeChallengeMethod, reqUri, cusLoginFailedHandler);

			String dgRcode = null;// 此時還沒有 dgRcode

			DgrGtwIdpAuthM dgrGtwIdpAuthM = getGtwIdPAuthService().createDgrGtwIdpAuthM(state, dgrClientId,
					DgrIdPType.CUS, dgRcode, dgrClientRedirectUri, codeChallenge, codeChallengeMethod);

			long gtwIdpAuthMId = dgrGtwIdpAuthM.getGtwIdpAuthMId();
			getGtwIdPAuthService().createDgrGtwIdpAuthD(gtwIdpAuthMId, oidcScopeStr);

			String cusLoginUrl = getCusLoginUrl(dgrClientId, dgrGtwIdpInfoCusId, state, queryParams,
					cusLoginFailedHandler);

			// 發送重新導向
			sendRedirect(httpResp, cusLoginUrl);

		} catch (RedirectException e) {
			TPILogger.tl.debug("Redirection executed: " + e.getMessage());

		} catch (Exception e) {
			// 處理其他未預期的異常
			TPILogger.tl.error("An unexpected error occurred during Gateway CUS Login redirection");
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	/**
	 * 發送重新導向至指定的 URL。
	 * 
	 * @throws IOException
	 */
	protected void sendRedirect(HttpServletResponse httpResp, String cusLoginUrl) throws IOException {
		// 發送重新導向至指定的 URL
		httpResp.sendRedirect(cusLoginUrl);
	}

	private String getCusLoginUrl(String dgrClientId, String dgrGtwIdpInfoCusId, String state,
			Map<String, String> queryParams, CusLoginFailedHandler cusLoginFailedHandler) throws RedirectException {

		try {
			Optional<DgrGtwIdpInfoCus> opt = getDgrGtwIdpInfoCus(dgrClientId, dgrGtwIdpInfoCusId);

			if (opt.isEmpty()) {
				String errorMessage = String.format(
						"No matching DgrGtwIdpInfoCus found for client ID: %s and IdP info ID: %s", dgrClientId,
						dgrGtwIdpInfoCusId);
				cusLoginFailedHandler.handle(errorMessage);
				return null;
			}

			DgrGtwIdpInfoCus info = opt.get();

			String cusLoginUrl = info.getCusLoginUrl();
			String cusUserDataUrl = info.getCusUserDataUrl();

			queryParams.put("cusUserDataUrl", cusUserDataUrl);

			CusGatewayLoginStateStore.INSTANCE.putQueryParams(state, queryParams);

			return cusLoginUrlRestructuring(cusLoginUrl, queryParams);

		} catch (MalformedURLException | UnsupportedEncodingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errorMessage = String.format("Error processing login URL: %s", e.getMessage());
			cusLoginFailedHandler.handle(errorMessage);
		}

		return null;

	}

	private String cusLoginUrlRestructuring(String cusLoginUrl, Map<String, String> queryParams)
			throws MalformedURLException, UnsupportedEncodingException {

		new URL(cusLoginUrl);

		String encode = "UTF-8"; // 設定編碼格式為 UTF-8
		StringJoiner joiner = new StringJoiner("&"); // 用於拼接查詢參數

		// 將 queryParams 轉換為查詢字符串
		for (Map.Entry<String, String> entry : queryParams.entrySet()) {
			String encodedKey = URLEncoder.encode(entry.getKey(), encode); // 將鍵編碼
			String encodedValue = URLEncoder.encode(entry.getValue(), encode); // 將值編碼
			joiner.add(encodedKey + "=" + encodedValue); // 加入編碼後的鍵值對
		}

		// 構建最終的 URL
		String separator = cusLoginUrl.contains("?") ? "&" : "?"; // 判斷是否已有查詢參數
		return cusLoginUrl + separator + joiner.toString(); // 拼接最終的 URL

	}

	private Optional<DgrGtwIdpInfoCus> getDgrGtwIdpInfoCus(String dgrClientId, String dgrGtwIdpInfoCusId) {
		if (StringUtils.hasText(dgrGtwIdpInfoCusId)) {

			Long id = isValidLong(dgrGtwIdpInfoCusId) ? Long.parseLong(dgrGtwIdpInfoCusId)
					: RandomSeqLongUtil.toLongValue(dgrGtwIdpInfoCusId);

			return getDgrGtwIdpInfoCusDao().findFirstByGtwIdpInfoCusIdAndClientIdAndStatusOrderByGtwIdpInfoCusIdDesc(id,
					dgrClientId, "Y");
		} else {
			return getDgrGtwIdpInfoCusDao().findFirstByClientIdAndStatusOrderByGtwIdpInfoCusIdDesc(dgrClientId, "Y");
		}
	}

	private boolean isValidLong(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}

		try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 檢查傳入的資料
	 * 
	 * @throws RedirectException
	 */
	private void checkReqParam(String responseType, String dgrClientId, String oidcScopeStr,
			String dgrClientRedirectUri, String state, String codeChallenge, String codeChallengeMethod, String reqUri,
			CusLoginFailedHandler cusLoginFailedHandler) throws RedirectException {

		// 檢查 response_type 是否有值 且 正確的值 "code"
		ResponseEntity<?> errRespEntity = getTokenHelper().checkResponseType(responseType);
		if (errRespEntity != null) {// response_type 資料驗證有錯誤
			String errorMessage = String.format("Invalid response_type: %s. Expected 'code'.", responseType);
			cusLoginFailedHandler.handle(errorMessage);
		}

		// 檢查是否有 code_challenge 和 code_challenge_method
		errRespEntity = getGtwIdPHelper().checkCodeChallengeParam(codeChallenge, codeChallengeMethod);
		if (errRespEntity != null) {// 資料驗證有錯誤
			String errorMessage = String.format(
					"Invalid code challenge parameters. code_challenge: %s, code_challenge_method: %s", codeChallenge,
					codeChallengeMethod);
			cusLoginFailedHandler.handle(errorMessage);
		}

		// 檢查 scope 是否為支援的 OpenID Connect scopes
		errRespEntity = getTokenHelper().checkOidcScope(oidcScopeStr);
		if (errRespEntity != null) {// scope 資料驗證有錯誤
			String errorMessage = String.format("Unsupported OpenID Connect scope: %s", oidcScopeStr);
			cusLoginFailedHandler.handle(errorMessage);
		}

		// 檢查 state 是否已存在
		errRespEntity = getGtwIdPHelper().checkStateExists(state, reqUri);
		if (errRespEntity != null) {// state 資料驗證有錯誤
			String errorMessage = String.format("State already exists or is invalid: %s", state);
			cusLoginFailedHandler.handle(errorMessage);
		}

		// 檢查傳入的 redirectUri 和 client 註冊在系統中的是否相同
		errRespEntity = getTokenHelper().checkRedirectUri(dgrClientId, dgrClientRedirectUri, reqUri);
		if (errRespEntity != null) {// redirectUri 驗證有錯誤
			String errorMessage = String.format("Invalid redirect URI for client ID %s: %s", dgrClientId,
					dgrClientRedirectUri);
			cusLoginFailedHandler.handle(errorMessage);
		}

		errRespEntity = getTokenHelper().checkClientStatus(dgrClientId, reqUri);
		if (errRespEntity != null) {// client 資料驗證有錯誤
			String errorMessage = String.format("Invalid client status for client ID: %s", dgrClientId);
			cusLoginFailedHandler.handle(errorMessage);
		}
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected AcIdPHelper getAcIdPHelper() {
		return this.acIdPHelper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected GtwIdPCallbackService getGtwIdPCallbackService() {
		return gtwIdPCallbackService;
	}

	protected GtwIdPAuthService getGtwIdPAuthService() {
		return gtwIdPAuthService;
	}

	protected GtwIdPHelper getGtwIdPHelper() {
		return gtwIdPHelper;
	}

	protected DgrGtwIdpInfoCusDao getDgrGtwIdpInfoCusDao() {
		return dgrGtwIdpInfoCusDao;
	}

}
