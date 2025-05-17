package tpi.dgrv4.dpaa.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.nodeTask.LogoutNotifier;
import tpi.dgrv4.dpaa.util.DpaaHttpUtil;
import tpi.dgrv4.dpaa.vo.AA0009Req;
import tpi.dgrv4.dpaa.vo.AA0009Resp;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class AA0009Service {

	private TPILogger logger = TPILogger.tl;
	
	private LogoutNotifier logoutNotifier;
	private TsmpUserDao tsmpUserDao;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpSettingService tsmpSettingService;
	private TsmpTokenHistoryDao tsmpTokenHistoryDao;
	
	@Autowired
	public AA0009Service(LogoutNotifier logoutNotifier, TsmpUserDao tsmpUserDao, DgrAuditLogService dgrAuditLogService,
			TsmpSettingService tsmpSettingService, TsmpTokenHistoryDao tsmpTokenHistoryDao) {
		super();
		this.logoutNotifier = logoutNotifier;
		this.tsmpUserDao = tsmpUserDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpTokenHistoryDao = tsmpTokenHistoryDao;
	}

	public AA0009Resp logoutTUser (TsmpAuthorization auth, AA0009Req req, InnerInvokeParam iip) {
		String userNameForQuery = auth.getUserNameForQuery();
		String idPType = auth.getIdpType();
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogMForLogout(iip, lineNumber, AuditLogEvent.LOGOUT.value(), "SUCCESS", "",
				idPType, userNameForQuery);
		
		AA0009Resp resp = new AA0009Resp();
		
		String jti = auth.getJti();
		String clientId = auth.getClientId();
		Long exp = auth.getExp();
		
		try {
			if(StringUtils.hasLength(clientId)) {
				String param = clientId + ":" + userNameForQuery + ":" + jti + ":" + exp;
				//新增tsmp_node_task資料
				this.noticePublicEvent(param);
				
				if(!StringUtils.hasLength(idPType) && StringUtils.hasLength(userNameForQuery)) {
					//更新tsmp_user.logoff_Date
					TsmpUser vo = getTsmpUserDao().findFirstByUserName(userNameForQuery);
					if( vo != null) {
						vo.setLogoffDate(DateTimeUtil.now());
						getTsmpUserDao().save(vo);
					}
				}
			}
			
			String logoutApi = "";
			try {
				logoutApi = getTsmpSettingService().getVal_LOGOUT_API();
			}catch (Exception e){
				this.logger.debug(StackTraceUtil.logStackTrace(e));
				logoutApi = "";
			}
			
			forCustomPackageLogout(logoutApi, req, auth);
 
			// 撤銷 access token, 狀態設為 "R"
			TsmpTokenHistory tsmpTokenHistory = revokeAccessToken(jti);

			// 撤銷 refresh token, 狀態設為 "R"
			if (tsmpTokenHistory != null) {
				String retokenJti = tsmpTokenHistory.getRetokenJti();
				revokeRefreshToken(retokenJti);
			}
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			//執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	
	/**
	 * 撤銷 access token, 狀態設為 "R"
	 */
	private TsmpTokenHistory revokeAccessToken(String jti) {
		TsmpTokenHistory tsmpTokenHistory = getTsmpTokenHistoryDao().findFirstByTokenJti(jti);
		if (tsmpTokenHistory != null) {
			tsmpTokenHistory.setRevokedStatus("R");
			tsmpTokenHistory.setRevokedAt(DateTimeUtil.now());
			tsmpTokenHistory = getTsmpTokenHistoryDao().saveAndFlush(tsmpTokenHistory);
		}
		return tsmpTokenHistory;
	}

	/**
	 * 撤銷 refresh token, 狀態設為 "R"
	 */
	private void revokeRefreshToken(String retokenJti) {
		// 找出此 refresh token 的所有資料
		List<TsmpTokenHistory> tsmpTokenHistoryList = getTsmpTokenHistoryDao().findByRetokenJti(retokenJti);
		if (!CollectionUtils.isEmpty(tsmpTokenHistoryList)) {
			for (TsmpTokenHistory tsmpTokenHistory : tsmpTokenHistoryList) {
				tsmpTokenHistory.setRftRevokedStatus("R");
				tsmpTokenHistory.setRftRevokedAt(DateTimeUtil.now());
				tsmpTokenHistory = getTsmpTokenHistoryDao().saveAndFlush(tsmpTokenHistory);
			}
		}
	}
	
	protected void noticePublicEvent(String param) {
		getLogoutNotifier().noticePublicEvent(param);
	}

	private void forCustomPackageLogout(String logoutApi, AA0009Req req, TsmpAuthorization auth) {
		if (StringUtils.hasText(logoutApi)) {	
			new Thread(() -> {
				callCustomPackageLogout(logoutApi, req, auth);
			}).start();
		}
	}

	private void callCustomPackageLogout(String logoutApi, AA0009Req req, TsmpAuthorization auth) {
		try {
			Map<String, String> header = new HashMap<>();
            header.put("Content-Type", "application/json");
            String clientId = auth.getClientId();
			String reqBody = DpaaHttpUtil.toReqPayloadJson(auth,clientId);
			HttpRespData resp = HttpUtil.httpReqByRawData(logoutApi, "POST", reqBody, header, false);
			logger.debug("==== custom package return  ====");
			logger.debug(resp.getLogStr());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}
	}

	protected LogoutNotifier getLogoutNotifier() {
		return logoutNotifier;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
		return tsmpTokenHistoryDao;
	}
}
