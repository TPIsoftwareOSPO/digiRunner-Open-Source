package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import javax.management.ImmutableDescriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoCusDao;
import tpi.dgrv4.gateway.component.AcIdPHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.constant.DgrCusAcIdpLogin;
import tpi.dgrv4.gateway.funcInterFace.CusLoginFailedHandler;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.CusAcLoginStateStore;

@Service
public class AcCusIdPLoginService extends CusIdPService {

	private DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public AcCusIdPLoginService(DgrAuditLogService dgrAuditLogService, AcIdPHelper acIdPHelper,
			TsmpSettingService tsmpSettingService, DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao,
			TsmpSettingService tsmpSettingService2) {
		super(dgrAuditLogService, acIdPHelper, tsmpSettingService);
		this.dgrAcIdpInfoCusDao = dgrAcIdpInfoCusDao;
		tsmpSettingService = tsmpSettingService2;
	}

	/**
	 * 處理 CUS 帳號登入前的預處理邏輯。
	 */
	public void preProcessCusAcLogin(HttpHeaders httpHeaders, HttpServletRequest httpReq, HttpServletResponse httpResp,
			String idpId, Map<String, String> queryParams) {

		try {
			// 建立 CUS 登入失敗處理器
			CusLoginFailedHandler cusLoginFailedHandler = customErrorMsg -> getCusLoginFailedHandler(httpHeaders,
					httpReq, httpResp, customErrorMsg);

			// 取得對應的 CUS 資訊
			DgrAcIdpInfoCus info = getCorrespondingCusIdpInfo(idpId, cusLoginFailedHandler);
			// 取得 DGR State
			String dgrState = getDgrState(info);

			// 取得 CUS 後端登入重新導向 URL
			String cusBackendLoginRedirectUrl = getCusBackendLoginRedirectUrl(info, dgrState, queryParams,
					cusLoginFailedHandler);

			// 發送重新導向
			sendRedirect(httpResp, cusBackendLoginRedirectUrl);

		} catch (UnsupportedEncodingException e) {
			// 處理不支援的編碼異常
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));

		} catch (RedirectException e) {
			// 處理重新導向異常
			TPILogger.tl.debug("Redirection executed: " + e.getMessage());

		} catch (Exception e) {
			// 處理其他未預期的異常
			TPILogger.tl.error("An unexpected error occurred during CUS AC Login redirection");
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	/**
	 * 發送重新導向至指定的 URL。
	 */
	protected void sendRedirect(HttpServletResponse httpResp, String cusBackendLoginRedirectUrl) throws IOException {
		// 發送重新導向至指定的 URL
		httpResp.sendRedirect(cusBackendLoginRedirectUrl);
	}

	/**
	 * 生成 CUS 後端登入重新導向 URL。
	 */
	private String getCusBackendLoginRedirectUrl(DgrAcIdpInfoCus info, String dgrState, Map<String, String> queryParams,
			CusLoginFailedHandler cusLoginFailedHandler) throws RedirectException, UnsupportedEncodingException {

		try {
			String encode = "UTF-8"; // 設定編碼格式為 UTF-8

			String cusBackendLoginUrl = info.getCusBackendLoginUrl(); // 取得 CUS 客戶後端登入 URL

			// 檢查 cusBackendLoginUrl 是否為有效的 URL
			new URL(cusBackendLoginUrl);

			StringJoiner joiner = new StringJoiner("&"); // 用於拼接查詢參數

			// 將 queryParams 轉換為查詢字符串
			for (Map.Entry<String, String> entry : queryParams.entrySet()) {
				String encodedKey = URLEncoder.encode(entry.getKey(), encode); // 將鍵編碼
				String encodedValue = URLEncoder.encode(entry.getValue(), encode); // 將值編碼
				joiner.add(encodedKey + "=" + encodedValue); // 加入編碼後的鍵值對
			}

			// 添加 dgrState 和 dgrRedirectUrl 參數
			String encodedState = URLEncoder.encode(dgrState, encode); // 編碼 dgrState
			joiner.add(DgrCusAcIdpLogin.DGR_STATE.value() + "=" + encodedState); // 加入 dgrState 參數

			// 構建最終的 URL
			String separator = cusBackendLoginUrl.contains("?") ? "&" : "?"; // 判斷是否已有查詢參數
			return cusBackendLoginUrl + separator + joiner.toString(); // 拼接最終的 URL

		} catch (MalformedURLException e) {
			// 處理無效的 URL 異常
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			cusLoginFailedHandler.handle("The CUS Backend Login Url is not a valid url.");
		}

		return null; // 如果發生異常，返回 null
	}

	/**
	 * 取得 DGR State
	 */
	private String getDgrState(DgrAcIdpInfoCus info) {
		CusAcLoginStateStore store = CusAcLoginStateStore.INSTANCE;
		// 將 CUS 資訊存入，並返回 DGR State
		return store.putDgrAcIdpInfoCus(info);
	}

	/**
	 * 取得對應的 CUS 資訊。
	 */
	private DgrAcIdpInfoCus getCorrespondingCusIdpInfo(String idpId, CusLoginFailedHandler cusLoginFailedHandler)
			throws RedirectException {

		DgrAcIdpInfoCus info = null;

		// 檢查 idpId 是否有值
		if (StringUtils.hasText(idpId)) {
			// 如果有值，根據 idpId 取得 CUS 資訊
			info = getDgrAcIdpInfoCusById(idpId, cusLoginFailedHandler);
		} else {
			// 如果沒值，取得預設的 CUS 資訊
			info = getDgrAcIdpInfoCusByDefault(cusLoginFailedHandler);
		}

		// 返回取得的 CUS 資訊
		return info;
	}

	/**
	 * 取得預設的 CUS 資訊。
	 */
	private DgrAcIdpInfoCus getDgrAcIdpInfoCusByDefault(CusLoginFailedHandler cusLoginFailedHandler)
			throws RedirectException {

		// 從資料庫中查詢狀態為 "Y" 的 CUS 資訊，並按更新時間和 ID 進行排序
		List<DgrAcIdpInfoCus> ls = getDgrAcIdpInfoCusDao()
				.findByCusStatusOrderByUpdateDateTimeDescAcIdpInfoCusIdDesc("Y");

		// 如果查詢結果不為空，返回第一個結果
		if (!ls.isEmpty()) {
			return ls.get(0);

		} else {

			// 如果查詢結果為空，處理錯誤訊息並進行錯誤處理
			String errMsg = String.format(IdPHelper.MSG_NO_AVAILABLE_CUS_IDP_INFO);
			cusLoginFailedHandler.handle(errMsg);
			return null;
		}
	}

	/**
	 * 根據 ID 取得 CUS 資訊。
	 */
	private DgrAcIdpInfoCus getDgrAcIdpInfoCusById(String idpId, CusLoginFailedHandler cusLoginFailedHandler)
			throws RedirectException {

		Long id = isValidLong(idpId) ? Long.parseLong(idpId) : RandomSeqLongUtil.toLongValue(idpId);

		// 從資料庫中根據 ID 查詢 CUS 資訊
		Optional<DgrAcIdpInfoCus> opt = getDgrAcIdpInfoCusDao().findByAcIdpInfoCusId(id);

		DgrAcIdpInfoCus info = null;

		// 如果查詢結果存在，檢查是否啟用
		if (opt.isPresent()) {

			info = opt.get();
			if (!info.getCusStatus().equalsIgnoreCase("Y")) {
				String errMsg = "The Specified Cus Idp Info Is Disable ";
				cusLoginFailedHandler.handle(errMsg);
			}

		} else {

			// 如果查詢結果不存在，處理錯誤訊息並進行錯誤處理
			String errMsg = String.format(IdPHelper.MSG_SPECIFIED_CUS_IDP_INFO_NOT_FOUND, idpId);
			cusLoginFailedHandler.handle(errMsg);
			return null;
		}

		return info;
	}

	protected DgrAcIdpInfoCusDao getDgrAcIdpInfoCusDao() {
		return this.dgrAcIdpInfoCusDao;
	}

	@Override
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
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
}
