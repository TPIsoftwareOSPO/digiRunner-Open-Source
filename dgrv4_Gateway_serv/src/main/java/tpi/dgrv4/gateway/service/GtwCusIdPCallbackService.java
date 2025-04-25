package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.common.constant.DgrAuthCodePhase;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.gateway.component.AcIdPHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.constant.DgrCusAcIdpLogin;
import tpi.dgrv4.gateway.funcInterFace.CusLoginFailedHandler;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.CusGatewayLoginStateStore;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class GtwCusIdPCallbackService extends CusIdPService {

	private ObjectMapper objectMapper;
	private GtwIdPCallbackService gtwIdPCallbackService;
	private GtwIdPAuthService gtwIdPAuthService;

	@Autowired
	public GtwCusIdPCallbackService(DgrAuditLogService dgrAuditLogService, AcIdPHelper acIdPHelper,
			TsmpSettingService tsmpSettingService, ObjectMapper objectMapper,
			GtwIdPCallbackService gtwIdPCallbackService, GtwIdPAuthService gtwIdPAuthService) {

		super(dgrAuditLogService, acIdPHelper, tsmpSettingService);

		this.objectMapper = objectMapper;
		this.gtwIdPCallbackService = gtwIdPCallbackService;
		this.gtwIdPAuthService = gtwIdPAuthService;
	}

	/**
	 * 處理 CUS 帳戶登入流程
	 */
	public void gtwCusIdPCallback(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpResp,
			Map<String, String> queryParams) {

		// 建立登入失敗處理器
		CusLoginFailedHandler cusLoginFailedHandler = customErrorMsg -> getCusLoginFailedHandler(httpHeaders, httpReq,
				httpResp, customErrorMsg, false);

		try {

			String state = queryParams.get("state");
			Map<String, String> params = getQueryParams(state, cusLoginFailedHandler);

			String cusUserDataUrl = params.get("cusUserDataUrl");
			String cusCode = queryParams.get("cusCode");
			String cusState = queryParams.get("cusState");

			if (!StringUtils.hasText(cusCode)) {
				cusCode = cusState;
			}

			Map<String, String> cusUserData = getCusUserData(cusUserDataUrl, cusCode, cusLoginFailedHandler);

			checkCusUserDataValue(cusUserData, cusLoginFailedHandler);

			String cusUserId = cusUserData.get(DgrCusAcIdpLogin.CUS_USER_ID.value());
			String cusUserAlias = cusUserData.get(DgrCusAcIdpLogin.CUS_USER_ALIAS.value());
			String cusUserEmail = cusUserData.get(DgrCusAcIdpLogin.CUS_USER_EMAIL.value());
			String cusUserPicture = cusUserData.get(DgrCusAcIdpLogin.CUS_USER_PICTURE.value());

			String dgrClientRedirectUri = params.get("redirect_uri");
			String responseType = params.get("response_type");
			String dgrClientId = params.get("client_id");
			String oidcScopeStr = params.get("scope");

			getGtwIdPCallbackService().createDgrGtwIdpAuthCode(state, null, DgrAuthCodePhase.STATE, 0, DgrIdPType.CUS,
					dgrClientId, cusUserId, cusUserAlias, cusUserEmail, cusUserPicture, null, null, null, null, null,
					null);

			String dgrConsentUiUrl = getDgrUserConsentUiUrl(DgrIdPType.CUS, responseType, dgrClientId, oidcScopeStr,
					dgrClientRedirectUri, state, cusUserId);

			// 發送重新導向
			sendRedirect(httpResp, dgrConsentUiUrl);

		} catch (RedirectException e) {
			TPILogger.tl.debug("Redirection executed: " + e.getMessage());

		} catch (Exception e) {
			// 處理其他未預期的異常
			TPILogger.tl.error("An unexpected error occurred during Gateway CUS Login redirection");
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	private void checkCusUserDataValue(Map<String, String> cusUserData, CusLoginFailedHandler cusLoginFailedHandler)
			throws RedirectException {

		String cusUserId = cusUserData.get(DgrCusAcIdpLogin.CUS_USER_ID.value());

		TPILogger.tl.debug(prettyPrintMap(cusUserData));

		if (!StringUtils.hasText(cusUserId)) {
			String msg = "The cusUserId is empty.";
			cusLoginFailedHandler.handle(msg);
		}

	}

	public String prettyPrintMap(Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return "CusUserData is empty";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("CusUserData content：\n");
		sb.append("{\n");

		map.forEach((key, value) -> {
			sb.append("  ").append(key).append(": ").append(value).append("\n");
		});

		sb.append("}");
		return sb.toString();
	}

	/**
	 * 發送重新導向至指定的 URL。
	 * 
	 * @throws IOException
	 */
	protected void sendRedirect(HttpServletResponse httpResp, String dgrConsentUiUrl) throws IOException {
		// 發送重新導向至指定的 URL
		httpResp.sendRedirect(dgrConsentUiUrl);
	}

	/**
	 * 取得 user 同意畫面 URL
	 */
	private String getDgrUserConsentUiUrl(String idPType, String responseType, String dgrClientId,
			String openIdScopeStr, String dgrClientRedirectUri, String state, String reqUserName) throws Exception {

		String dgrConsentUiUrl = getTsmpSettingService().getVal_GTW_IDP_CONSENT_URL();

		dgrConsentUiUrl = dgrConsentUiUrl.replace("{idPType}", idPType);

		URL urlObj = new URL(dgrConsentUiUrl);
		dgrConsentUiUrl = urlObj.getPath();// 使用相對路徑

		return String.format(
				"%s" + "?response_type=%s" + "&client_id=%s" + "&scope=%s" + "&redirect_uri=%s" + "&state=%s"
						+ "&username=%s",
				dgrConsentUiUrl, IdPHelper.getUrlEncode(responseType), IdPHelper.getUrlEncode(dgrClientId),
				IdPHelper.getUrlEncode(openIdScopeStr), IdPHelper.getUrlEncode(dgrClientRedirectUri),
				IdPHelper.getUrlEncode(state), IdPHelper.getUrlEncode(reqUserName));
	}

	private Map<String, String> getCusUserData(String cusUserDataUrl, String cusCode,
			CusLoginFailedHandler cusLoginFailedHandler) throws RedirectException {

		if (!StringUtils.hasText(cusCode)) {
			String msg = "The " + DgrCusAcIdpLogin.CUS_CODE.value() + " is empty.";
			TPILogger.tl.error(msg);
			cusLoginFailedHandler.handle(msg);
		}

		String cusUserDataJson = sendRequestForUserData(cusUserDataUrl, cusCode);

		if (!StringUtils.hasText(cusUserDataJson)) {
			String msg = "The retrieved cus user data is empty.";
			TPILogger.tl.error(msg);
			cusLoginFailedHandler.handle(msg);
		}

		return parseUserData(cusUserDataJson, cusLoginFailedHandler);
	}

	public Map<String, String> parseUserData(String respStr, CusLoginFailedHandler cusLoginFailedHandler)
			throws RedirectException {

		Map<String, String> result = new HashMap<>();

		try {
			String[] targets = new String[] { //
					DgrCusAcIdpLogin.CUS_USER_ID.value(), //
					DgrCusAcIdpLogin.CUS_USER_ALIAS.value(), //
					DgrCusAcIdpLogin.CUS_USER_PICTURE.value(), //
					DgrCusAcIdpLogin.CUS_USER_EMAIL.value() };

			JsonNode rootNode = getObjectMapper().readTree(respStr);

			Arrays.stream(targets).forEach(target -> {
				String value = Optional.ofNullable(rootNode.get(target)).map(JsonNode::asText).orElse(null);
				result.put(target, value);
			});

		} catch (JsonProcessingException e) {
			String msg = "An error occurred while converting cus user data json to map.";
			TPILogger.tl.error(msg);
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			cusLoginFailedHandler.handle(msg);
		}

		return result;
	}

	protected String sendRequestForUserData(String cusUserDataUrl, String cusCode) {
		HttpRespData resp = new HttpRespData();

		try {
			// 解析 URL
			URI uri = new URI(cusUserDataUrl);
			String query = uri.getQuery();

			// 構建新的查詢字符串
			String newQuery = (query == null) ? "" : query + "&";
			newQuery += DgrCusAcIdpLogin.CUS_CODE.value() + "="
					+ URLEncoder.encode(cusCode, StandardCharsets.UTF_8.toString());

			// 重建 URL
			URI newUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), newQuery, uri.getFragment());
			String finalUrl = newUri.toString();

			// 發送請求
			resp = HttpUtil.httpReqByGet(finalUrl, Collections.emptyMap(), false);

		} catch (IOException | URISyntaxException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return "";
		}

		return resp.respStr;
	}

	private Map<String, String> getQueryParams(String state, CusLoginFailedHandler cusLoginFailedHandler)
			throws RedirectException {

		if (!StringUtils.hasText(state)) {
			TPILogger.tl.error("The state parameter is empty.");
			cusLoginFailedHandler.handle("The state parameter is empty.");
		}

		Map<String, String> params = CusGatewayLoginStateStore.INSTANCE.getQueryParams(state);

		if (params.isEmpty()) {
			String msg = "No record of GtwCusIdPLogin trigger found. Please restart the process.";
			TPILogger.tl.error(msg);
			cusLoginFailedHandler.handle(msg);
		}

		return params;

	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected GtwIdPCallbackService getGtwIdPCallbackService() {
		return gtwIdPCallbackService;
	}

	protected GtwIdPAuthService getGtwIdPAuthService() {
		return gtwIdPAuthService;
	}

}
