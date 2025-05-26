package tpi.dgrv4.gateway.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 驗證 ID token 並取得 User 個人資料,
 * 若為 GTW IdP(API),則回覆 GTW IdP(API) 調用 Login API 得到的 response 結果
 * 
 * @author Mini
 */

@Service
public class GtwIdPUserInfoService {

	private TokenHelper tokenHelper;
	private TsmpTokenHistoryDao tsmpTokenHistoryDao;
	private GtwIdPVerifyService gtwIdPVerifyService;

	@Autowired
	public GtwIdPUserInfoService(TokenHelper tokenHelper, TsmpTokenHistoryDao tsmpTokenHistoryDao,
			GtwIdPVerifyService gtwIdPVerifyService) {
		super();
		this.tokenHelper = tokenHelper;
		this.tsmpTokenHistoryDao = tsmpTokenHistoryDao;
		this.gtwIdPVerifyService = gtwIdPVerifyService;
	}

	public ResponseEntity<?> getUserInfo(HttpServletRequest httpReq, HttpServletResponse httpResp,
			HttpHeaders headers) {
		String reqUri = httpReq.getRequestURI();
		ResponseEntity<?> respEntity = null;
		try {
			String idTokenJwtstr = httpReq.getParameter("id_token");
			return getUserInfo(idTokenJwtstr, reqUri);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			respEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			return respEntity;
		}
	}
	
	protected ResponseEntity<?> getUserInfo(String idTokenJwtstr, String reqUri) throws Exception {
		// 1.驗 ID token 的簽章、期限, 並取得 ID token 內容
		ResponseEntity<?> respEntity = getGtwIdPVerifyService().verify(idTokenJwtstr, null, reqUri);
		
		// 2.用 ID token (JWT) 搜尋 token history
		TsmpTokenHistory tsmpTokenHistory = getTsmpTokenHistoryDao().findFirstByIdTokenJwtstr(idTokenJwtstr);
		if (tsmpTokenHistory != null) {
			// 3.檢核 Access token 是否撤銷
			String tokenJti = tsmpTokenHistory.getTokenJti();
			// 若已撤銷要回覆錯誤訊息
			ResponseEntity<?> revokedRespEntity = getTokenHelper().checkAccessTokenRevoked(tsmpTokenHistory, tokenJti);
			if(revokedRespEntity != null) {
				return revokedRespEntity;
			}
			
			// 4.若查詢有 apiResp,則回覆 GTW IdP(API) 調用 Login API 得到的 response 結果
			String apiResp = tsmpTokenHistory.getApiResp();
			if (StringUtils.hasLength(apiResp)) {// 表示為 GTW IdP(API)
				return new ResponseEntity<String>(apiResp, HttpStatus.OK);
			}
		}
		
		// 5.否則,回覆內容和 verify API 的結果相同
		return respEntity;
	}
	
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
	
	protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
		return tsmpTokenHistoryDao;
	}

	protected GtwIdPVerifyService getGtwIdPVerifyService() {
		return gtwIdPVerifyService;
	}
}
