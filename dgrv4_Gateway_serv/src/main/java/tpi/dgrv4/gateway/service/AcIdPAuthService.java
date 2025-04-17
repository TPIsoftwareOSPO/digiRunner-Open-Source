package tpi.dgrv4.gateway.service;
    
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.entity.entity.DgrAcIdpInfo;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoDao;
import tpi.dgrv4.gateway.component.AcIdPHelper;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.component.IdPWellKnownHelper;
import tpi.dgrv4.gateway.component.IdPWellKnownHelper.WellKnownData;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AcIdPAuthService {

    private DgrAcIdpInfoDao dgrAcIdpInfoDao;
    private TsmpSettingService tsmpSettingService;
    private IdPWellKnownHelper idPWellKnownHelper;
    private AcIdPHelper acIdPHelper;
	private DgrAuditLogService dgrAuditLogService;
    
	@Autowired
	public AcIdPAuthService(DgrAcIdpInfoDao dgrAcIdpInfoDao, TsmpSettingService tsmpSettingService,
			IdPWellKnownHelper idPWellKnownHelper, AcIdPHelper acIdPHelper, DgrAuditLogService dgrAuditLogService) {
		super();
		this.dgrAcIdpInfoDao = dgrAcIdpInfoDao;
		this.tsmpSettingService = tsmpSettingService;
		this.idPWellKnownHelper = idPWellKnownHelper;
		this.acIdPHelper = acIdPHelper;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	public void acIdPAuth(HttpHeaders httpHeaders, String idPType, HttpServletRequest httpReq, HttpServletResponse httpResp)
			throws Exception {
		String acIdPMsgUrl = null;
		
		String reqUri = httpReq.getRequestURI();
		String txnUid = getDgrAuditLogService().getTxnUid();
		String userIp = !StringUtils.hasLength(httpHeaders.getFirst("x-forwarded-for")) ? httpReq.getRemoteAddr()
				: httpHeaders.getFirst("x-forwarded-for");
		String userHostname = httpReq.getRemoteHost();
		
		try {
			// 前端AC IdP errMsg顯示訊息的URL
			acIdPMsgUrl = getTsmpSettingService().getVal_AC_IDP_MSG_URL();
			
			String errMsg = acIdPAuth(idPType, httpResp, acIdPMsgUrl, userIp, userHostname, txnUid, reqUri);
			if (StringUtils.hasLength(errMsg)) {
				// 重新導向到前端,顯示訊息
				getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
				return;
			}
			
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			
    		// 重新導向到前端,顯示訊息
			String errMsg = "System error";
    		TPILogger.tl.error(errMsg);
 
    		getAcIdPHelper().redirectToShowMsg(httpResp, errMsg, acIdPMsgUrl, idPType);
    		return;
		}
	}

	public String acIdPAuth(String idPType, HttpServletResponse httpResp, String acIdPMsgUrl, String userIp,
			String userHostname, String txnUid, String reqUri) throws Exception {
		idPType = idPType.toUpperCase();
		String userName = "N/A";// 此時還沒有值
		String userAlias = null;// 此時還沒有值
		
		// 1.取得 IdP(GOOGLE / MS) info 資料
		DgrAcIdpInfo dgrAcIdpInfo = getDgrAcIdpInfoDao().findFirstByIdpTypeAndClientStatusOrderByCreateDateTimeDesc(idPType, "Y");
		if(dgrAcIdpInfo == null) {
			// Table [DGR_AC_IDP_INFO] 查不到資料
			TPILogger.tl.debug("Table [DGR_AC_IDP_INFO] can't find data");
			// 設定檔缺少參數 '%s'
			String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS,
					"AC IdP (" + idPType + ") info");
    		TPILogger.tl.debug(errMsg);
    		
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
			
    		return errMsg;
		}
 
		String idPClientId = dgrAcIdpInfo.getClientId();
		String idPWellKnownUrl = dgrAcIdpInfo.getWellKnownUrl();
		String dgrCallbackUrl = dgrAcIdpInfo.getCallbackUrl();
		String idPAuthUrl = dgrAcIdpInfo.getAuthUrl();
		String idPScopeStr = dgrAcIdpInfo.getScope();
		
		if(!StringUtils.hasLength(idPWellKnownUrl)) {
			// 設定檔缺少參數 '%s'
    		String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "wellKnownUrl");
    		TPILogger.tl.debug(errMsg);
    		
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
			
    		return errMsg;
		}
		
		if(!StringUtils.hasLength(dgrCallbackUrl)) {
			// 設定檔缺少參數 '%s'
    		String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "callbackUrl");
    		TPILogger.tl.debug(errMsg);
    		
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
    		
    		return errMsg;
		}
		
		// 2.打 IdP(GOOGLE / MS) Well Known URL, 取得 JSON 資料
		WellKnownData wellKnownData = null;
		if (!StringUtils.hasLength(idPAuthUrl) 
				|| !StringUtils.hasLength(idPScopeStr)) {
			wellKnownData = getIdPWellKnownHelper().getWellKnownData(idPWellKnownUrl, reqUri);
			ResponseEntity<?> errRespEntity = wellKnownData.errRespEntity;
			if (errRespEntity != null) {
				String errMsg = wellKnownData.errMsg;
				
				// 寫入 Audit Log M,登入失敗
				String lineNumber = StackTraceUtil.getLineNumber();
				getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
						idPType, userName, userAlias);

				return errMsg;
			}
			
			// 3.由 Well Known JSON 中取得資料
			// 若 authUrl 沒有值, 則從 Well Known 取得
			if(!StringUtils.hasLength(idPAuthUrl)) {
				idPAuthUrl = wellKnownData.authorizationEndpoint;
			}
			
			// 若 scope 沒有值, 則從 Well Known 取得
			if(!StringUtils.hasLength(idPScopeStr)) {
				idPScopeStr = wellKnownData.scopeStr;
			}
		}
		
		if(!StringUtils.hasLength(idPAuthUrl)) {
			// 設定檔缺少參數 '%s'
    		String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "authUrl");
    		TPILogger.tl.debug(errMsg);
    		
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
    		
    		return errMsg;
		}
		
		if(!StringUtils.hasLength(idPScopeStr)) {
			// 設定檔缺少參數 '%s'
    		String errMsg = String.format(AcIdPHelper.MSG_THE_PROFILE_IS_MISSING_PARAMETERS, "scope");
    		TPILogger.tl.debug(errMsg);
    		
			// 寫入 Audit Log M,登入失敗
			String lineNumber = StackTraceUtil.getLineNumber();
			getAcIdPHelper().createAuditLogMForLoginFailed(reqUri, lineNumber, userIp, userHostname, txnUid, errMsg,
					idPType, userName, userAlias);
    		
    		return errMsg;
		}
		
		// 4.打 IdP(GOOGLE / MS) 的 auth API, 會重新導向到 OAuth 同意畫面
		// codeVerifier 寫入 cookie
		long maxAge = 60L * 5L;// 以秒為單位, 設定5分鐘
		String codeVerifierForOauth2 = UUID.randomUUID().toString() + UUID.randomUUID().toString();// for GOOGLE / MS
		ResponseCookie codeVerifierCookie = TokenHelper.createCookie(GtwIdPHelper.COOKIE_CODE_VERIFIER,
				codeVerifierForOauth2, maxAge);
		httpResp.addHeader(HttpHeaders.SET_COOKIE, codeVerifierCookie.toString());
		//checkmarx, Missing HSTS Header
		httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
        
		String redirectUrl = IdPHelper.getRedirectUrl(idPClientId, idPAuthUrl, idPScopeStr, dgrCallbackUrl,
				codeVerifierForOauth2);

		TPILogger.tl.debug("Redirect(IdP Auth Url) : " + redirectUrl);
		httpResp.sendRedirect(redirectUrl);
		
		return null;
    }
 
	protected DgrAcIdpInfoDao getDgrAcIdpInfoDao() {
		return dgrAcIdpInfoDao;
	}
 
    protected TsmpSettingService getTsmpSettingService() {
    	return tsmpSettingService;
    }
 
    protected IdPWellKnownHelper getIdPWellKnownHelper() {
    	return idPWellKnownHelper;
    }
    
   	protected AcIdPHelper getAcIdPHelper() {
   		return acIdPHelper;
   	}
   	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
}
