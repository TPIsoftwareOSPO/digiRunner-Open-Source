package tpi.dgrv4.gateway.component.check;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.filter.CusContentCachingRequestWrapper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.OAuthTokenService;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;
import tpi.dgrv4.gateway.vo.Whitelist;

@Component
public class CusTokenCheck implements ICheck {

	private TokenHelper tokenHelper;
	private TsmpSettingService tsmpSettingService;
	private DgrRtnMsgBuilder dgrRtnMsgBuilder;
	private ObjectMapper objectMapper;
	private OAuthTokenService tokenService;
	private TsmpRtnCodeService tsmpRtnCodeService;

	@Value("${server.port}")
	private String serverPort;
	
	@Value("${cus.whitelist}")
	private String cusWhitelistProperties;
	
	private Map<String, Whitelist> whitelistMap = new ConcurrentHashMap<>();

	@Autowired
	public CusTokenCheck(TokenHelper tokenHelper, TsmpSettingService tsmpSettingService,
			DgrRtnMsgBuilder dgrRtnMsgBuilder, ObjectMapper objectMapper, OAuthTokenService tokenService,
			TsmpRtnCodeService tsmpRtnCodeService) {
		super();
		this.tokenHelper = tokenHelper;
		this.tsmpSettingService = tsmpSettingService;
		this.dgrRtnMsgBuilder = dgrRtnMsgBuilder;
		this.objectMapper = objectMapper;
		this.tokenService = tokenService;
		this.tsmpRtnCodeService = tsmpRtnCodeService;
	}

	/*
	 * 檢查來源是否為客製包 1.有CAPI-key 2.有username  符合以上2點就是來自客製包
	 */
	public boolean check(HttpServletRequest req) {
		try {
			// 客製包打主包之判斷方法, 於 header 中粗略判斷三個 header
			boolean fromCustomPackage = isCus(req);
			if (fromCustomPackage) {

				CusContentCachingRequestWrapper request = (CusContentCachingRequestWrapper) req;
				// 以下是 '精確' 判斷, header 值是否合法
				// 1.檢核cApikey
				String cuuid = request.getHeader("cuuid");
				String cApikey = request.getHeader("capi-key");
				boolean isValidate = CApiKeyUtils.verifyCKey(cuuid, cApikey);
				if (!isValidate) {
					TPILogger.tl.error("Customized package cApikey verification failed");
					return true;
				}

				// 2.檢核url ,username與ip是否有在白名單內
				String uri = request.getRequestURI();
				String username = request.getHeader("username");
				String ipAddress = request.getRemoteAddr();
				TPILogger.tl.trace("Customized ip is " + ipAddress);
				if (!checkCusWhiteList(username, ipAddress, uri)) {
					TPILogger.tl.error("Customized package not in propreties whitelist");
					return true;
				}

				// 3.檢查是否有快取
//				if (cusTokenCache.containsKey(username)) {
//					String token = cusTokenCache.get(username);
//					// Token快取 檢查是否合法
//					boolean value = checkToken(token);
//					if (value) {
//						request.setToken(cusTokenCache.get(username));
//						return false;
//					}
//				}

				// 4.取AccessToken
				String token = getAccessToken(username);

				// 5.加入 Header 'Authorization'
				request.setToken(token);
			}
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return true;
		}
		return false;
	}

	private boolean isCus(HttpServletRequest request) {
		String cuuid = request.getHeader("cuuid");
		String capikey = request.getHeader("capi-key");
		String username = request.getHeader("username");
		if (StringUtils.hasLength(cuuid) && StringUtils.hasLength(capikey) && StringUtils.hasLength(username)) {
			return true;
		}
		return false;
	}
	
	private boolean checkCusWhiteList(String username, String ipAddress, String api) {
		//TPILogger.tl.debug("cus.whitelist properties is"+ cusWhitelistProperties);
	    //把白名單轉成map
	    if (!StringUtils.hasLength(cusWhitelistProperties)) {
	    	TPILogger.tl.error("cus.whitelist properties is null");
	    	return false;
	    }
	    
	    if (whitelistMap.isEmpty()) {
	    	try {
	    		whitelistMap = objectMapper.readValue(cusWhitelistProperties, new TypeReference<HashMap<String, Whitelist>>() {});
	    	} catch (Exception e) {
	    		TPILogger.tl.error("cus.whitelist properties converting json failed :" + cusWhitelistProperties);
	    		TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
	    		return false;
	    	}
		}
	    
	    //檢核username的白名單
	    if (!whitelistMap.containsKey(username)) {
	    	TPILogger.tl.error("username(cus app.name) not in whitelist :" + username);
	    	return false;
	    }
	    
	    //檢核ip與api權限
	    String whitelistUsername;
	    Whitelist whitelist;
	    boolean isTrust = false;
	    for (Map.Entry<String, Whitelist> entry : whitelistMap.entrySet()) {
	    	whitelistUsername = entry.getKey();
	    	if (username.equals(whitelistUsername)) {
	    		whitelist = entry.getValue();
	    		isTrust = checkIpAndApi(whitelist, ipAddress, api);
	    		if (isTrust) {
	    			return true;
				}
			}
	    }
	    return false;
	}

	private boolean checkIpAndApi(Whitelist whitelist, String ipAddress, String api) {
		boolean isTrustIP = false;
		List<String> whitelistIp = whitelist.getIps();
		for (String ip : whitelistIp) {
			if (ipAddress.equals(ip)) {
				isTrustIP = true;
			}
		}
		if (!isTrustIP) {
			TPILogger.tl.error("whitelist ip :" + whitelistIp);
			TPILogger.tl.error("IpAddress is not in whitelist:" + ipAddress);
		}

		boolean isTrustAPI = false;
		List<String> whitelistApis = whitelist.getApis();
		for (String whitelistApi : whitelistApis) {
			if (api.equals(whitelistApi)) {
				isTrustAPI = true;
			}
		}
		if (!isTrustAPI) {
			TPILogger.tl.error("whitelist api :" + whitelistApis);
			TPILogger.tl.error("api is not in whitelist:" + api);
		}
		boolean isTrust = isTrustIP && isTrustAPI;
		return isTrust;
	}

	//取得一個30秒效期的token
	private String getAccessToken(String username) {
		String accessToken ="";
		username = "cus(" + username + ")";
		try {
			accessToken = getOAuthTokenService().getCusAccessToken(username);
			accessToken = "Bearer " + accessToken;
		} catch (Exception e) {
			TPILogger.tl.error("get OAuthTokenService getCusAccessToken fail");
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		return accessToken;
	}

	private boolean checkToken(String token) {
		String tokenStr = token.substring(TokenHelper.BEARER.length());
		//驗證
		boolean value = false;
		try {
			value = getOAuthTokenService().verifyCusAccessToken(tokenStr);
		} catch (Exception e) {
			TPILogger.tl.error("get OAuthTokenService verifyCusAccessToken fail");
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		if (!value) {// 資料有錯誤
			return false;
		}
		return true;
	}

	/**
	 * 是否 access token exp 沒有值 或 access token 過期
	 * 
	 * @return
	 */
	public ResponseEntity<?> checkAccessTokenExp(String tokenStr, Long exp) {
		if (exp == null || exp == 0) {
			TPILogger.tl.debug("The exp of access token is 0");// refresh_token 的 exp 為 0
			String errMsg = TokenHelper.CANNOT_CONVERT_ACCESS_TOKEN_TO_JSON;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg),
					setContentTypeHeader(), HttpStatus.BAD_REQUEST);// 400
		}

		// access token 過期
		long nowTime = System.currentTimeMillis() / 1000;// 去掉亳秒
		if (exp < nowTime) {
			// access token 過期
			TPILogger.tl.debug(TokenHelper.ACCESS_TOKEN_EXPIRED + exp);
			String errMsg = TokenHelper.ACCESS_TOKEN_EXPIRED + tokenStr;
			TPILogger.tl.debug(errMsg);
			return new ResponseEntity<OAuthTokenErrorResp2>(getOAuthTokenErrorResp2(TokenHelper.INVALID_TOKEN, errMsg),
					setContentTypeHeader(), HttpStatus.UNAUTHORIZED);// 401
		}
		return null;
	}

	public OAuthTokenErrorResp2 getOAuthTokenErrorResp2(String error, String errDes) {
		OAuthTokenErrorResp2 resp = new OAuthTokenErrorResp2();
		resp.setError(error);
		resp.setErrorDescription(errDes);

		try {
			TPILogger.tl.debug(
					StackTraceUtil.logTpiShortStackTrace(new Throwable(new ObjectMapper().writeValueAsString(resp))));
		} catch (JsonProcessingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		return resp;
	}

	/**
	 * 為了postman 沒有帶 APPLICATION_JSON
	 * 
	 * @return
	 */
	private MultiValueMap<String, String> setContentTypeHeader() {
		// 為了postman 沒有帶 APPLICATION_JSON
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.put("Content-Type", Arrays.asList(MediaType.APPLICATION_JSON.toString()));
		return header;
	}
	
	protected TsmpRtnCodeService getTsmpRtnCodeService() {
		return this.tsmpRtnCodeService;
	}

	@Override
	public DgrRtnCode getRtnCode() {
		return DgrRtnCode._1510;
	}

	@Override
	public String getMessage(String locale) {
		TsmpRtnCode tokenInvalid_tsmpRtnCode = getTsmpRtnCodeService().findById(getRtnCode().getCode(), locale);
		if (tokenInvalid_tsmpRtnCode!=null) {
			return tokenInvalid_tsmpRtnCode.getTsmpRtnMsg();	
		}else {
			return getRtnCode().getDefaultMessage();
		}
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}

	protected OAuthTokenService getOAuthTokenService() {
		return tokenService;
	}
	
	protected DgrRtnMsgBuilder getDgrRtnMsgBuilder() {
		return dgrRtnMsgBuilder;
	}
}
