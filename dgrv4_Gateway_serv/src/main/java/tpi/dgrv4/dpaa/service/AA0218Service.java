package tpi.dgrv4.dpaa.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0218Req;
import tpi.dgrv4.dpaa.vo.AA0218Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0218Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientDao tsmpClientDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private BcryptParamHelper bcryptParamHelper;
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired
	public AA0218Service(TsmpClientDao tsmpClientDao, OauthClientDetailsDao oauthClientDetailsDao,
			BcryptParamHelper bcryptParamHelper, DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	public AA0218Resp updateTokenSettingByClient(TsmpAuthorization authorization, AA0218Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
		
		AA0218Resp resp = new AA0218Resp();

		try {
			String clientId = req.getClientID();
			Long accessTokenValidity = req.getAccessTokenValidity();
			Set<String> authorizedGrantType = req.getAuthorizedGrantType();
			Long raccessTokenValidity = req.getRaccessTokenValidity();
			String accessTokenValidityTimeUnit = req.getAccessTokenValidityTimeUnit();
			String raccessTokenValidityTimeUnit = req.getRaccessTokenValidityTimeUnit();
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String webServerRedirectUri = req.getWebServerRedirectUri();
			String webServerRedirectUri1 = req.getWebServerRedirectUri1();
			String webServerRedirectUri2 = req.getWebServerRedirectUri2();
			String webServerRedirectUri3 = req.getWebServerRedirectUri3();
			String webServerRedirectUri4 = req.getWebServerRedirectUri4();
			String webServerRedirectUri5 = req.getWebServerRedirectUri5();

			checkParam(req);
			
			//3.更新oauth_client_details資料表，
			//欄位 access_token_validity = AA0218Req.accessTokenValidity ==0或null?null:AA0218Req.accessTokenValidity
			//、refresh_token_validity = AA0218Req.raccessTokenValidity、web_server_redirect_uri = AA0218Req.webServerRedirectUri
			//、authorized_grant_types = AA0218Req.authorizedGrantType(資料格式:refresh_token,client_credentials,password,authorization_code)，條件client_id = AA0218Req.clientID。
			OauthClientDetails oauthVo = getOauthClientDetailsDao().findById(clientId).orElse(null);
			if(oauthVo != null) {
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oauthVo); //舊資料統一轉成 String
				if (accessTokenValidity == null
//				        || accessTokenValidity.intValue() == 0
				) {
					oauthVo.setAccessTokenValidity(null);
				}else {
					//依單位換算成秒數
					String timeUnit = this.getValueByBcryptParamHelper(accessTokenValidityTimeUnit, "TIME_UNIT", locale);
					Long intAccessTokenValidity = this.convertSecond(accessTokenValidity, timeUnit);
					
					oauthVo.setAccessTokenValidity(intAccessTokenValidity);
				}
				if(raccessTokenValidity == null) {
					oauthVo.setRefreshTokenValidity(null);
				}else {
					//依單位換算成秒數
					String timeUnit = this.getValueByBcryptParamHelper(raccessTokenValidityTimeUnit, "TIME_UNIT", locale);
					Long intRaccessTokenValidity = this.convertSecond(raccessTokenValidity, timeUnit);
					
					oauthVo.setRefreshTokenValidity(intRaccessTokenValidity);
				}
 
				oauthVo.setWebServerRedirectUri(webServerRedirectUri);
				oauthVo.setWebServerRedirectUri1(webServerRedirectUri1);
				oauthVo.setWebServerRedirectUri2(webServerRedirectUri2);
				oauthVo.setWebServerRedirectUri3(webServerRedirectUri3);
				oauthVo.setWebServerRedirectUri4(webServerRedirectUri4);
				oauthVo.setWebServerRedirectUri5(webServerRedirectUri5);
				oauthVo.setAuthorizedGrantTypes(String.join(",", authorizedGrantType));
				
				oauthVo = getOauthClientDetailsDao().save(oauthVo);
				
				//寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
						OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, oauthVo);
			}
			
			//4.更新tsmp_clinet資料表
			//條件CLIENT_ID = AA0218Req.clientID
			//欄位
			//tsmp_clinet.ACCESS_TOKEN_QUOTA = AA0218Req.accessTokenQuota
			//tsmp_clinet.REFRESH_TOKEN_QUOTA = AA0218Req.refreshTokenQuota
			TsmpClient tsmpClientVo = getTsmpClientDao().findById(clientId).orElse(null);
			
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpClientVo); //舊資料統一轉成 String

			TsmpClient updatedVo = Optional.ofNullable(tsmpClientVo).map(vo-> {
				vo.setAccessTokenQuota(req.getAccessTokenQuota());
				vo.setRefreshTokenQuota(req.getRefreshTokenQuota());
				return vo;
			}).orElse(tsmpClientVo);

			tsmpClientVo = getTsmpClientDao().save(updatedVo);
			
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpClient.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpClientVo);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1286:更新失敗
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		return resp;
	}
	
	private Long convertSecond(Long value, String timeUnit) {
		if("d".equals(timeUnit)) {
			return value * 60 * 60 * 24;
		}else if("H".equals(timeUnit)) {
			return value * 60 * 60;
		}else if("m".equals(timeUnit)) {
			return value * 60;
		}else {
			return value;
		}
	}

	private void checkParam(AA0218Req req) {
		// 檢查輸入的網址 Default Redirect URI 和 Extends Redirect URI 1 ~ 5
		checkRedirectUri(req.getWebServerRedirectUri(), "");
		checkRedirectUri(req.getWebServerRedirectUri1(), "1");
		checkRedirectUri(req.getWebServerRedirectUri2(), "2");
		checkRedirectUri(req.getWebServerRedirectUri3(), "3");
		checkRedirectUri(req.getWebServerRedirectUri4(), "4");
		checkRedirectUri(req.getWebServerRedirectUri5(), "5");

		String clientId = req.getClientID();
		Set<String> authorizedGrantType = req.getAuthorizedGrantType();
		
		//1.查詢 tsmp_client 資料表，條件 CLIENT_ID = AA0218Req.clientID，若查無資料則throw RTN Code 1344。
		TsmpClient tsmpClientVo = getTsmpClientDao().findById(clientId).orElse(null);
		if(tsmpClientVo == null) {
			//1344:用戶端不存在
			throw TsmpDpAaRtnCode._1344.throwing();
		}
		
		//2.檢查AA0218Req.authorizedGrantType的值，是否存在於[authorization_code、client_credentials、refresh_token、password、public、implicit、group]，若沒有存在則throw RTN Code 1372。
		for(String type : authorizedGrantType) {
			if("authorization_code".equals(type) || "client_credentials".equals(type) || "refresh_token".equals(type)
					|| "password".equals(type) || "public".equals(type) || "implicit".equals(type) || "smsotp".equals(type)
					|| "group".equals(type)) {
				continue;
			}else {
				//1372:未知類型的Grant Type: [{{0}}] 
				throw TsmpDpAaRtnCode._1372.throwing(type);
			}
		}
	}
	
	private String getValueByBcryptParamHelper(String encodeValue, String itemNo, String locale) {
		String value = null;
		try {
			value = getBcryptParamHelper().decode(encodeValue, itemNo, BcryptFieldValueEnum.SUBITEM_NO, locale);// BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return value;
	}
	
	/**
	 * 檢查輸入的網址 Default Redirect URI 和 Extends Redirect URI 1 ~ 5, <br>
	 * 若為 https 或 https, 則網址不能有 * 號 <br>
	 * 否則, 則網址要有 * 號 <br>
	 */
	private void checkRedirectUri(String uri, String index) {
		if (uri == null) {
			return;
		}
 
		String errMsg = null;
		if (!uri.contains("://")) {
			errMsg = "Redirect URI " + index + " is incomplete: " + uri;
			// Redirect URI 不完整: uri
			this.logger.debug(errMsg);
			throw TsmpDpAaRtnCode._1559.throwing(errMsg);
		}
		
		if (isHttpsOrHttp(uri)) {// https 或 https
			if (uri.toLowerCase().equals("https://") || uri.toLowerCase().equals("http://")) {
				errMsg = "Redirect URI " + index + " is incomplete: " + uri;
				// Redirect URI 不完整: uri
				this.logger.debug(errMsg);
				throw TsmpDpAaRtnCode._1559.throwing(errMsg);
			}
			
			if (uri.contains("*")) {// 網址不能有 * 號
				errMsg = "Redirect URI " + index + " is incorrect, cannot have * sign: " + uri;
				// Redirect URI 錯誤,不能有 * 號: uri
				this.logger.debug(errMsg);
				throw TsmpDpAaRtnCode._1559.throwing(errMsg);
			}
		} else {// 不是 https 或 https, 則網址必須有 * 號
			if (!uri.contains("*")) {
				errMsg = "Redirect URI " + index + " is incorrect, must have * sign: " + uri;
				// Redirect URI 錯誤,必須有 * 號: uri
				this.logger.debug(errMsg);
				throw TsmpDpAaRtnCode._1559.throwing(errMsg);
			}
		}
	}
 
	/**
	 * 網址是否為 "https://" 或 "http://" 開頭
	 * 
	 * @return true: 是; false: 不是
	 */
	private boolean isHttpsOrHttp(String uri) {
		if (uri == null) {
			return false;
		}
		
		if (uri.toLowerCase().startsWith("https://") || uri.toLowerCase().startsWith("http://")) {
			return true;
		}

		return false;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
}
