package tpi.dgrv4.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.OAuthTokenService.OAuthTokenData;
import tpi.dgrv4.gateway.vo.NotifyLandingReq;
import tpi.dgrv4.gateway.vo.NotifyLandingResp;

@Service()
public class NotifyLandingService {

	private TPILogger logger = TPILogger.tl;

	private OAuthTokenService oAuthTokenService;
	private CApiKeyService cApiKeyService;
	private ObjectMapper objectMapper;

	@Autowired
	public NotifyLandingService(OAuthTokenService oAuthTokenService, CApiKeyService cApiKeyService,
			ObjectMapper objectMapper) {
		super();
		this.oAuthTokenService = oAuthTokenService;
		this.cApiKeyService = cApiKeyService;
		this.objectMapper = objectMapper;
	}

	public NotifyLandingResp confirmAPI(NotifyLandingReq req, ReqHeader reqHeader, HttpHeaders headers) {

		String reqJson = "";

		try {

			// 將請求轉換為 JSON 字串
			reqJson = getObjectMapper().writeValueAsString(req);

			// 驗證 CApiKey
			getcApiKeyService().verifyCApiKey(headers, true, false);

			// 驗證請求資料
			validateData(req);

			OAuthTokenData oauthTokenData = req.getOauthTokenData();
			boolean isHasRefreshToken = req.isHasRefreshToken();
			String userName = req.getUserName();
			String clientId = req.getClientId();
			String accessTokenJti = req.getAccessTokenJti();
			String refreshTokenJti = req.getRefreshTokenJti();
			String scopeStr = req.getScopeStr();
			Long stime = req.getStime();
			Long accessTokenExp = req.getAccessTokenExp();
			Long refreshTokenExp = req.getRefreshTokenExp();
			String grantType = req.getGrantType();
			String oldAccessTokenJti = req.getOldAccessTokenJti();
			String idPType = req.getIdPType();
			String idTokenJwt = req.getIdTokenJwt();
			String refreshTokenJwtstr = req.getRefreshTokenJwtstr();

			getoAuthTokenService().doTokenHistory(oauthTokenData, isHasRefreshToken, userName, clientId, accessTokenJti,
					refreshTokenJti, scopeStr, stime, accessTokenExp, refreshTokenExp, grantType, oldAccessTokenJti,
					idPType, idTokenJwt, refreshTokenJwtstr);

			// 更新時間
			TPILogger.updateTime4InMemory(DgrDataType.TOKEN.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			this.logger.error(reqJson);
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return new NotifyLandingResp();
	}

	/**
	 * 驗證請求資料的方法
	 *
	 * @param req NotifyLandingReq 物件，包含要驗證的資料
	 */
	private void validateData(NotifyLandingReq req) {
		// 取得 OAuthTokenData 物件
		OAuthTokenData oauthTokenData = req.getOauthTokenData();
		// 檢查 oauthTokenData 參數
		checkParameter("oauthTokenData", oauthTokenData);

		// 取得 clientId
		String clientId = req.getClientId();
		// 檢查 clientId 參數
		checkParameter("clientId", clientId);

		// 取得 accessTokenJti
		String accessTokenJti = req.getAccessTokenJti();
		// 檢查 accessTokenJti 參數
		checkParameter("accessTokenJti", accessTokenJti);

		// 取得 scopeStr
		String scopeStr = req.getScopeStr();
		// 檢查 scopeStr 參數
		checkParameter("scopeStr", scopeStr);

		// 取得 stime
		Long stime = req.getStime();
		// 檢查 stime 參數
		checkParameter("stime", stime);

		// 取得 accessTokenExp
		Long accessTokenExp = req.getAccessTokenExp();
		// 檢查 accessTokenExp 參數
		checkParameter("accessTokenExp", accessTokenExp);

		// 取得 grantType
		String grantType = req.getGrantType();
		// 檢查 grantType 參數
		checkParameter("grantType", grantType);
	}

	/**
	 * 檢查傳入的參數是否為 null 或空白字符串。 如果是，則記錄缺少該參數的日誌。
	 *
	 * @param parameterName  參數名稱，用於記錄哪個參數缺失
	 * @param parameterValue 參數值，將進行 null 和空白字符串檢查
	 */
	private void checkParameter(String parameterName, Object parameterValue) {
		// 判斷參數值是否為 null 或空白字符串
		if (parameterValue == null || //
				(parameterValue instanceof String && ((String) parameterValue).trim().isEmpty())) {
			// 如果是，則記錄缺少參數的日誌
			logMissingParameter(parameterName);
		}
	}

	/**
	 * 記錄遺失的參數並拋出異常
	 *
	 * @param parameter 遺失的參數名稱
	 */
	private void logMissingParameter(String parameter) {
		TPILogger.tl.error("Missing required parameter: " + parameter);
		throw TsmpDpAaRtnCode._2025.throwing(parameter);
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public CApiKeyService getcApiKeyService() {
		return cApiKeyService;
	}

	public OAuthTokenService getoAuthTokenService() {
		return oAuthTokenService;
	}

}
