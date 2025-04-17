package tpi.dgrv4.gateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.DgrIdPType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;
import tpi.dgrv4.gateway.component.AcIdPHelper;
import tpi.dgrv4.gateway.constant.DgrCusAcIdpLogin;
import tpi.dgrv4.gateway.funcInterFace.CusLoginFailedHandler;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.CusAcLoginStateStore;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Service
public class AcCusIdPCallbackService extends CusIdPService {

	private ObjectMapper objectMapper;
	private AcIdPHelper acIdPHelper;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public AcCusIdPCallbackService(DgrAuditLogService dgrAuditLogService, AcIdPHelper acIdPHelper,
			TsmpSettingService tsmpSettingService, ObjectMapper objectMapper, AcIdPHelper acIdPHelper2,
			TsmpSettingService tsmpSettingService2) {
		super(dgrAuditLogService, acIdPHelper, tsmpSettingService);
		this.objectMapper = objectMapper;
		acIdPHelper = acIdPHelper2;
		tsmpSettingService = tsmpSettingService2;
	}

	/**
	 * 處理 CUS 帳戶登入流程
	 */
	public void processCusAcLogin(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpResp,
			Map<String, String> queryParams) {

		// 建立登入失敗處理器
		CusLoginFailedHandler cusLoginFailedHandler = customErrorMsg -> getCusLoginFailedHandler(httpHeaders, httpReq,
				httpResp, customErrorMsg);

		try {
			// 從查詢參數中獲取 DGR STATE
			String dgrState = queryParams.get(DgrCusAcIdpLogin.DGR_STATE.value());
			// 獲取 CUS USER DATA URL
			String cusUserDataUrl = getCusUserDataUrl(dgrState, cusLoginFailedHandler);

			// 從查詢參數中獲取 CUS STATE
			String cusState = queryParams.get(DgrCusAcIdpLogin.CUS_STATE.value());

			// 獲取 USER 資料
			Map<String, String> cusUserData = getCusUserData(cusUserDataUrl, cusState, cusLoginFailedHandler);

			// 從 USER 資料中提取相關資訊
			String cusUserId = cusUserData.get(DgrCusAcIdpLogin.CUS_USER_ID.value());
			String cusUserAlias = cusUserData.get(DgrCusAcIdpLogin.CUS_USER_ALIAS.value());
			String cusUserEmail = cusUserData.get(DgrCusAcIdpLogin.CUS_USER_EMAIL.value());

			// 對 USER ID 進行編碼
			cusUserId = encodeCusUserId(cusUserId, cusLoginFailedHandler);

			if (!StringUtils.hasText(cusUserAlias)) {
				cusUserAlias = cusUserId;
			}

			// 準備進行 AC USER 建立或是登入
			sendMailOrCreateDgRcode(httpHeaders, httpReq, httpResp, DgrIdPType.CUS, cusUserId, cusUserAlias,
					cusUserEmail);

		} catch (RedirectException e) {
			TPILogger.tl.debug("Redirection executed: " + e.getMessage());

		} catch (Exception e) {
			// 處理其他未預期的異常
			TPILogger.tl.error("An unexpected error occurred during CUS AC Login redirection");
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	/**
	 * 準備進行 AC USER 建立或是登入
	 */
	protected void sendMailOrCreateDgRcode(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String cus, String cusUserId, String cusUserAlias, String cusUserEmail)
			throws Exception {

		// 獲取請求的 URI
		String reqUri = httpReq.getRequestURI();

		// 生成唯一識別碼
		String txnUid = CusAcLoginStateStore.INSTANCE.getState();

		// 獲取 IP 地址，優先使用 X-Forwarded-For 標頭，如果沒有則使用遠端地址
		String userIp = !StringUtils.hasLength(httpHeaders.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr()
				: httpHeaders.getFirst("x-forwarded-for");

		// 獲取主機名稱
		String userHostname = httpReq.getRemoteHost();

		String msgUrl = getTsmpSettingService().getVal_AC_IDP_MSG_URL();

		// 進行 AC USER 建立或是登入
		getAcIdPHelper().sendMailOrCreateDgRcode(httpReq, httpResp, DgrIdPType.CUS, cusUserId, cusUserAlias,
				cusUserEmail, null, null, null, reqUri, userIp, userHostname, txnUid, msgUrl, null);
	}

	/**
	 * 對 USER ID 進行編碼。
	 */
	private String encodeCusUserId(String cusUserId, CusLoginFailedHandler cusLoginFailedHandler)
			throws RedirectException {

		// 檢查 USER ID 是否為空
		if (!StringUtils.hasText(cusUserId)) {
			String msg = "Without cus User Id, the user cannot be identified.";
			TPILogger.tl.error(msg);
			cusLoginFailedHandler.handle(msg);
		}

		return Base64Util.base64URLEncode(cusUserId.getBytes());

	}

	/**
	 * 獲取 USER 資料。
	 */
	private Map<String, String> getCusUserData(String cusUserDataUrl, String cusState,
			CusLoginFailedHandler cusLoginFailedHandler) throws RedirectException {

		// 檢查 CUS STATE 是否為空
		if (!StringUtils.hasText(cusState)) {
			String msg = "The " + DgrCusAcIdpLogin.CUS_STATE.value() + " is empty.";
			TPILogger.tl.error(msg);
			cusLoginFailedHandler.handle(msg);
		}

		// 發送請求獲取 USER 資料
		String cusUserDataJson = sendRequestForUserData(cusUserDataUrl, cusState);

		// 檢查獲取的資料是否為空
		if (!StringUtils.hasText(cusUserDataJson)) {
			String msg = "The retrieved cus user data is empty.";
			TPILogger.tl.error(msg);
			cusLoginFailedHandler.handle(msg);
		}

		// 解析資料並返回
		return parseUserData(cusUserDataJson, cusLoginFailedHandler);
	}

	/**
	 * 解析資料並轉換為 Map 格式。
	 */
	public Map<String, String> parseUserData(String respStr, CusLoginFailedHandler cusLoginFailedHandler)
			throws RedirectException {

		// 初始化結果 Map
		Map<String, String> result = new HashMap<>();

		try {
			// 定義需要提取的目標欄位
			String[] targets = new String[] { //
					DgrCusAcIdpLogin.CUS_USER_ID.value(), //
					DgrCusAcIdpLogin.CUS_USER_ALIAS.value(), //
					DgrCusAcIdpLogin.CUS_USER_EMAIL.value() };

			// 將 JSON 字串解析為 JsonNode 物件
			JsonNode rootNode = getObjectMapper().readTree(respStr);

			// 遍歷目標欄位並提取值
			Arrays.stream(targets).forEach(target -> {
				String value = Optional.ofNullable(rootNode.get(target)).map(JsonNode::asText).orElse(null);
				result.put(target, value);
			});

		} catch (JsonProcessingException e) {
			// 捕獲 JSON 處理異常
			String msg = "An error occurred while converting cus user data json to map.";
			TPILogger.tl.error(msg);
			TPILogger.tl.error("The UserData respStr: " + respStr);
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			// 使用處理器處理登入失敗
			cusLoginFailedHandler.handle(msg);
		}

		// 返回解析後的結果 Map
		return result;
	}

	/**
	 * 發送請求以獲取 USER 資料。
	 */
	protected String sendRequestForUserData(String cusUserDataUrl, String cusState) {

		// 初始化 HTTP 回應資料物件
		HttpRespData resp = new HttpRespData();

		try {
			// 發送 GET 請求以獲取 USER 資料
			// 將 cusState 作為查詢參數添加到 URL 中
			resp = HttpUtil.httpReqByGet(cusUserDataUrl + "?cusState=" + cusState, Collections.emptyMap(), false);

		} catch (IOException e) {
			// 捕獲並記錄 IO 異常
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			// 發生錯誤時返回空字串
			return "";
		}

		// 返回回應的字串內容
		return resp.respStr;
	}

	/**
	 * 獲取 CUS IDP 資訊裡面的 USER 資料 URL。
	 */
	private String getCusUserDataUrl(String dgrState, CusLoginFailedHandler cusLoginFailedHandler)
			throws RedirectException {

		// 檢查 dgrState 是否為空
		if (!StringUtils.hasText(dgrState)) {
			String msg = "The value of " + DgrCusAcIdpLogin.DGR_STATE.value() + " is empty.";
			TPILogger.tl.error(msg);
			cusLoginFailedHandler.handle(msg);
		}

		CusAcLoginStateStore store = CusAcLoginStateStore.INSTANCE;
		// 嘗試獲取與 dgrState 對應的 CUS IDP 資訊
		Optional<DgrAcIdpInfoCus> opt = store.getDgrAcIdpInfoCus(dgrState);

		// 檢查是否成功獲取到資訊
		if (opt.isEmpty()) {
			String msg = "The ac idp info corresponding to " + DgrCusAcIdpLogin.DGR_STATE.value() + " is empty.";
			TPILogger.tl.error(msg + "The value of " + DgrCusAcIdpLogin.DGR_STATE.value() + " is: " + dgrState);
			cusLoginFailedHandler.handle(msg);
		}

		// 從 Optional 中獲取 CUS 使用者資料 URL
		String cusUserDataUrl = opt.map(DgrAcIdpInfoCus::getCusUserDataUrl).orElse("Not valid url");
		// 驗證 URL 是否有效
		boolean isValidURL = isValidURL(cusUserDataUrl);
		if (!isValidURL) {
			String msg = "The CUS User Data Url is invalid.";
			TPILogger.tl.error(msg + " The CUS User Data Url is: " + cusUserDataUrl);
			cusLoginFailedHandler.handle(msg);
		}

		// 返回有效的 CUS 使用者資料 URL
		return cusUserDataUrl;
	}

	/**
	 * 檢查給定的 URL 字串是否為有效的 URL。
	 *
	 * @param url 要檢查的 URL 字串
	 * @return 如果 URL 有效則返回 true，否則返回 false
	 */
	private boolean isValidURL(String url) {
		try {
			// 嘗試建立 URL 物件，如果成功則表示 URL 格式正確
			new URL(url);
			return true;
		} catch (MalformedURLException e) {
			// 如果 URL 格式不正確，會拋出 MalformedURLException 異常
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return false;
		}
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	@Override
	protected AcIdPHelper getAcIdPHelper() {
		return this.acIdPHelper;
	}

	@Override
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

}
