package tpi.dgrv4.dpaa.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0148Req;
import tpi.dgrv4.dpaa.vo.DPB0148Resp;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.gateway.component.cache.proxy.AuthoritiesCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0148Service {

	private TPILogger logger = TPILogger.tl;

	private DgrAcIdpUserDao dgrAcIdpUserDao;
	private AuthoritiesDao authoritiesDao;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpSettingService tsmpSettingService;
	private AuthoritiesCacheProxy authoritiesCacheProxy;
	
	@Autowired
	public DPB0148Service(DgrAcIdpUserDao dgrAcIdpUserDao, AuthoritiesDao authoritiesDao,
			DgrAuditLogService dgrAuditLogService, TsmpSettingService tsmpSettingService,
			AuthoritiesCacheProxy authoritiesCacheProxy) {
		super();
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.authoritiesDao = authoritiesDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpSettingService = tsmpSettingService;
		this.authoritiesCacheProxy = authoritiesCacheProxy;
	}

	@Transactional
	public DPB0148Resp deleteIdPUser(TsmpAuthorization auth, DPB0148Req req, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
				String lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.DELETE_IDP_USER.value());

		DPB0148Resp resp = new DPB0148Resp();
		try {
			if(!StringUtils.hasText(req.getLongId())) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			DgrAcIdpUser userVo = getDgrAcIdpUserDao().findById(Long.valueOf(req.getLongId())).orElse(null);
			if(userVo == null) {
				throw TsmpDpAaRtnCode._1231.throwing();
			}
			
			String userName = userVo.getUserName();
			String authName = auth.getUserNameForQuery();
			checkPermissions(authName, userName);
			
			deleteDgrAcIdpUser(userVo, iip);
			deleteAuthorities(userVo.getUserName(), iip);
		

			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	protected void checkPermissions(String authName, String userName) {
		boolean settingFlag = getTsmpSettingService().getVal_USER_UPDATE_BY_SELF();
		List<Authorities> authoritiesList = getAuthoritiesCacheProxy().findByUsername(authName);
		boolean roleFlag = authoritiesList.stream()
			    .anyMatch(authorities -> "1000".equals(authorities.getAuthority()));
		// ADMIN角色可修改自己 &&是否允許修正自身帳號(預設是可以) && 自己不可異動自己的資訊
		if (!roleFlag && settingFlag && authName.equals(userName)) {
			throw TsmpDpAaRtnCode._1219.throwing();
		}
	}

	private void deleteDgrAcIdpUser(DgrAcIdpUser userVo, InnerInvokeParam iip) {
		getDgrAcIdpUserDao().delete(userVo);

		String lineNumber = StackTraceUtil.getLineNumber();
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, userVo); // 舊資料統一轉成 String
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, DgrAcIdpUser.class.getSimpleName(), TableAct.D.value(),
				oldRowStr, null);// D

	}
	
	
	private void deleteAuthorities(String userName, InnerInvokeParam iip) {
		List<Authorities> authoritiesList = getAuthoritiesDao().findByUsername(userName);
		getAuthoritiesDao().deleteAll(authoritiesList);
		if(iip != null) {
			String lineNumber = StackTraceUtil.getLineNumber();
			for (Authorities authorities : authoritiesList) {
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, authorities); //舊資料統一轉成 String
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						Authorities.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
			}
		}
		
	}
	
	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}
	
	protected AuthoritiesDao getAuthoritiesDao() {
		return this.authoritiesDao;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected AuthoritiesCacheProxy getAuthoritiesCacheProxy() {
		return authoritiesCacheProxy;
	}
	
}
